package com.greenteam;

import com.greenteam.config.JobConfig;
import com.greenteam.model.ExpiredPendingSaleEvent;
import com.greenteam.model.SaleWithSalesmanEvent;
import com.greenteam.model.SaleWithStoreEvent;
import com.greenteam.model.SalesEnrichedEvent;
import com.greenteam.model.SalesEvent;
import com.greenteam.model.SalesmanEvent;
import com.greenteam.model.StoreEvent;
import com.greenteam.openlineage.OpenLineageIntegration;
import com.greenteam.operator.CheckpointNotifier;
import com.greenteam.operator.EnrichSalesWithSalesman;
import com.greenteam.operator.EnrichSalesWithStore;
import com.greenteam.operator.MergeEnrichments;
import com.greenteam.operator.ParseSalesEvent;
import com.greenteam.operator.ParseSalesmanEvent;
import com.greenteam.operator.ParseStoreEvent;
import com.greenteam.serde.ExpiredPendingSaleEventSerializer;
import com.greenteam.serde.SalesEnrichedSerializer;
import io.openlineage.client.OpenLineage;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.core.execution.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.util.Properties;

import static com.greenteam.config.JobConfig.JOB_NAME;

public class EnrichSales {

    public static void main(String[] args) throws Exception {
        // The main method is the entry point of the Flink job. 
        // It sets up the execution environment, defines the sources and sinks for the job, and connects the operators to create the data processing pipeline.
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final var jobExecutionId = java.util.UUID.randomUUID().toString();
        var openLineage = new OpenLineageIntegration(jobExecutionId);

        // Emit lineage event com ambos os tópicos de saída
        openLineage.emitKafkaToKafkaEvent(OpenLineage.RunEvent.EventType.START);
        
        // We set the parallelism for the job and configure checkpointing to ensure fault tolerance and exactly-once processing guarantees.
        env.setParallelism(JobConfig.DEFAULT_PARALLELISM);

        // Checkpointing is enabled with a specified interval, checkpointing mode, minimum pause between checkpoints, and checkpoint timeout.
        // checking is used to ensure that the state of the job is periodically saved, so that in case of a failure, the job can be restarted from the last checkpoint instead of starting from scratch.
        // We use the EXACTLY_ONCE checkpointing mode to ensure that each record is processed exactly once, even in the case of failures.
        env.enableCheckpointing(JobConfig.CHECKPOINT_INTERVAL_MS, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(JobConfig.CHECKPOINT_MIN_PAUSE_MS);
        env.getCheckpointConfig().setCheckpointTimeout(JobConfig.CHECKPOINT_TIMEOUT_MS);

        // We define the Kafka sources for the sales, stores, and salesmans topics. 
        // Each source is configured with the appropriate bootstrap servers, topic, consumer group ID, starting offsets, and deserializer.
        KafkaSource<String> salesSource = KafkaSource.<String>builder()
            .setBootstrapServers(JobConfig.BOOTSTRAP_SERVERS)
            .setTopics(JobConfig.SALES_TOPIC)
            .setGroupId(JobConfig.SALES_CONSUMER_GROUP_ID)
            .setStartingOffsets(OffsetsInitializer.earliest()) // we start consuming from the earliest offsets to ensure that we process all the existing data in the topics when the job starts for the first time
            .setValueOnlyDeserializer(new SimpleStringSchema()) // we use a simple string deserializer to read the raw JSON messages from the Kafka topics, and then we will parse these JSON messages into our event objects using the ParseSalesEvent, ParseStoreEvent, and ParseSalesmanEvent operators
            .build();

        KafkaSource<String> storesSource = KafkaSource.<String>builder()
            .setBootstrapServers(JobConfig.BOOTSTRAP_SERVERS)
            .setTopics(JobConfig.STORES_TOPIC)
            .setGroupId(JobConfig.STORES_CONSUMER_GROUP_ID)
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new SimpleStringSchema())
            .build();

        KafkaSource<String> salesmansSource = KafkaSource.<String>builder()
            .setBootstrapServers(JobConfig.BOOTSTRAP_SERVERS)
            .setTopics(JobConfig.SALESMANS_TOPIC)
            .setGroupId(JobConfig.SALESMANS_CONSUMER_GROUP_ID)
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new SimpleStringSchema())
            .build();

        // We create data streams for the sales, stores, and salesmans topics by connecting the sources to the appropriate parsing operators.
        DataStream<SalesEvent> salesStream = env.fromSource(
                salesSource,
                WatermarkStrategy.noWatermarks(), // we don't need watermarks in this job because we are not doing any time-based operations, such as windowing or event time joins. We are only doing key-based joins between the sales, stores, and salesmans streams, so we can use a no-watermark strategy for the sources.
                "source: " + JobConfig.SALES_TOPIC
            )
            .flatMap(new ParseSalesEvent())
            .name("operator: parse sales");

        DataStream<StoreEvent> storesStream = env.fromSource(
                storesSource,
                WatermarkStrategy.noWatermarks(),
                "source: " + JobConfig.STORES_TOPIC
            )
            .flatMap(new ParseStoreEvent())
            .name("operator: parse stores");

        DataStream<SalesmanEvent> salesmansStream = env.fromSource(
                salesmansSource,
                WatermarkStrategy.noWatermarks(),
                "source: " + JobConfig.SALESMANS_TOPIC
            )
            .flatMap(new ParseSalesmanEvent())
            .name("operator: parse salesmans");

        // Parallel enrichment: SalesEvent with StoreEvent
        SingleOutputStreamOperator<SaleWithStoreEvent> saleWithStoreStream = salesStream
            .keyBy(sale -> sale.storeId)
            .connect(storesStream.keyBy(store -> store.id))
            .process(new EnrichSalesWithStore(JobConfig.PENDING_SALES_TTL_MS))
            .name("operator: enrich sales + store");

        // Parallel enrichment: SalesEvent with SalesmanEvent
        SingleOutputStreamOperator<SaleWithSalesmanEvent> saleWithSalesmanStream = salesStream
            .keyBy(sale -> sale.salesmanId)
            .connect(salesmansStream.keyBy(salesman -> salesman.id))
            .process(new EnrichSalesWithSalesman(JobConfig.PENDING_SALES_TTL_MS))
            .name("operator: enrich sales + salesman");

        // Merge enrichments
        SingleOutputStreamOperator<SalesEnrichedEvent> enrichedStream = saleWithStoreStream
            .connect(saleWithSalesmanStream)
            .keyBy(store -> store.saleId, salesman -> salesman.saleId)
            .process(new MergeEnrichments(JobConfig.PENDING_SALES_TTL_MS))
            .name("operator: merge enrichments");

        // Retrieve side outputs for expired sales
        DataStream<ExpiredPendingSaleEvent> expiredSalesFromStore = saleWithStoreStream
                .getSideOutput(EnrichSalesWithStore.EXPIRED_SALES_TAG);
        DataStream<ExpiredPendingSaleEvent> expiredSalesFromSalesman = saleWithSalesmanStream
                .getSideOutput(EnrichSalesWithSalesman.EXPIRED_SALES_TAG);
        DataStream<ExpiredPendingSaleEvent> expiredSalesFromMerge = enrichedStream
                .getSideOutput(MergeEnrichments.EXPIRED_SALES_TAG);

        // Union all expired sales
        DataStream<ExpiredPendingSaleEvent> expiredSalesStream = expiredSalesFromStore
                .union(expiredSalesFromSalesman)
                .union(expiredSalesFromMerge);

        Properties producerConfig = new Properties();
        producerConfig.setProperty("transaction.timeout.ms", JobConfig.TRANSACTION_TIMEOUT_MS);

        // We define the Kafka sink for the enriched sales events. The sink is configured with the appropriate bootstrap servers, record serializer, delivery guarantee, transactional ID prefix, and producer configuration.
        KafkaSink<SalesEnrichedEvent> sink = KafkaSink.<SalesEnrichedEvent>builder()
            .setBootstrapServers(JobConfig.BOOTSTRAP_SERVERS)
            .setRecordSerializer(new SalesEnrichedSerializer())
            .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
            .setTransactionalIdPrefix(JobConfig.TRANSACTIONAL_ID_PREFIX) // we use a transactional ID prefix to ensure that the sink can participate in Flink's two-phase commit protocol for exactly-once guarantees. Each instance of the sink will have a unique transactional ID based on this prefix, which allows Flink to track the transactions and ensure that records are not duplicated or lost in case of failures.
            .setKafkaProducerConfig(producerConfig)
            .build();

        // Define the Kafka sink for expired sales events
        KafkaSink<ExpiredPendingSaleEvent> expiredSalesSink = KafkaSink.<ExpiredPendingSaleEvent>builder()
            .setBootstrapServers(JobConfig.BOOTSTRAP_SERVERS)
            .setRecordSerializer(new ExpiredPendingSaleEventSerializer())
            .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
            .setTransactionalIdPrefix(JobConfig.TRANSACTIONAL_ID_PREFIX + "-expired")
            .setKafkaProducerConfig(producerConfig)
            .build();

        // We connect the enriched sales stream to the Kafka sink to emit the fully enriched sale events to the output topic in Kafka.
        enrichedStream
                .flatMap(new CheckpointNotifier<>(jobExecutionId))
                .name("operator: lineage checkpoint notifier")
                .sinkTo(sink).name("sink: " + JobConfig.OUTPUT_TOPIC);

        expiredSalesStream.sinkTo(expiredSalesSink).name("sink: sales-expired");

        try {
            env.execute(JOB_NAME);
        } catch (Exception e) {
            openLineage.emitKafkaToKafkaEvent(OpenLineage.RunEvent.EventType.FAIL);
        } finally {
            openLineage.close();
        }
    }
}