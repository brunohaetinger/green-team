package com.greenteam;

import com.greenteam.config.JobConfig;
import com.greenteam.model.SaleWithStoreEvent;
import com.greenteam.model.SalesEnrichedEvent;
import com.greenteam.model.SalesEvent;
import com.greenteam.model.SalesmanEvent;
import com.greenteam.model.StoreEvent;
import com.greenteam.operator.JoinSalesWithSalesman;
import com.greenteam.operator.JoinSalesWithStore;
import com.greenteam.operator.ParseSalesEvent;
import com.greenteam.operator.ParseSalesmanEvent;
import com.greenteam.operator.ParseStoreEvent;
import com.greenteam.serde.SalesEnrichedSerializer;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

public class EnrichSales {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.setParallelism(JobConfig.DEFAULT_PARALLELISM);
        env.enableCheckpointing(JobConfig.CHECKPOINT_INTERVAL_MS, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(JobConfig.CHECKPOINT_MIN_PAUSE_MS);
        env.getCheckpointConfig().setCheckpointTimeout(JobConfig.CHECKPOINT_TIMEOUT_MS);

        KafkaSource<String> salesSource = KafkaSource.<String>builder()
            .setBootstrapServers(JobConfig.BOOTSTRAP_SERVERS)
            .setTopics(JobConfig.SALES_TOPIC)
            .setGroupId(JobConfig.SALES_CONSUMER_GROUP_ID)
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new SimpleStringSchema())
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

        DataStream<SalesEvent> salesStream = env.fromSource(
                salesSource,
                WatermarkStrategy.noWatermarks(),
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

        DataStream<SaleWithStoreEvent> saleWithStoreStream = salesStream
            .keyBy(sale -> sale.storeId)
            .connect(storesStream.keyBy(store -> store.id))
            .process(new JoinSalesWithStore(JobConfig.PENDING_SALES_TTL_MS))
            .name("operator: join sales + stores");

        DataStream<SalesEnrichedEvent> enrichedStream = saleWithStoreStream
            .keyBy(sale -> sale.salesmanId)
            .connect(salesmansStream.keyBy(salesman -> salesman.id))
            .process(new JoinSalesWithSalesman(JobConfig.PENDING_SALES_TTL_MS))
            .name("operator: join sales + salesman");

        enrichedStream.print("sink: stdout");

        Properties producerConfig = new Properties();
        producerConfig.setProperty("transaction.timeout.ms", JobConfig.TRANSACTION_TIMEOUT_MS);

        KafkaSink<SalesEnrichedEvent> sink = KafkaSink.<SalesEnrichedEvent>builder()
            .setBootstrapServers(JobConfig.BOOTSTRAP_SERVERS)
            .setRecordSerializer(new SalesEnrichedSerializer())
            .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
            .setTransactionalIdPrefix(JobConfig.TRANSACTIONAL_ID_PREFIX)
            .setKafkaProducerConfig(producerConfig)
            .build();

        enrichedStream.sinkTo(sink).name("sink: " + JobConfig.OUTPUT_TOPIC);

        env.execute("enrich sales from topics");
    }
}