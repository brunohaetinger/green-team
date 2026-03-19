package com.greenteam;

import com.greenteam.config.JobConfig;
import com.greenteam.model.CitySalesResult;
import com.greenteam.model.EventLineageRecord;
import com.greenteam.operator.CitySalesAggregate;
import com.greenteam.operator.CitySalesWindowFormatter;
import com.greenteam.operator.ParseSalesEvent;
import com.greenteam.serde.CitySalesResultSerializer;
import com.greenteam.serde.EventLineageRecordSerializer;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;

import java.time.Duration;
import java.util.Properties;

public class TopSalesByCity {

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // --- Source ---
        KafkaSource<String> source = KafkaSource.<String>builder()
            .setBootstrapServers(JobConfig.BOOTSTRAP_SERVERS)
            .setTopics(JobConfig.INPUT_TOPIC)
            .setGroupId(JobConfig.CONSUMER_GROUP_ID)
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new SimpleStringSchema())
            .build();

        DataStream<String> inputStream = env.fromSource(
            source,
            WatermarkStrategy.noWatermarks(),
            "source: " + JobConfig.INPUT_TOPIC
        );

        DataStream<com.greenteam.model.SaleEvent> parsedStream = inputStream
                .flatMap(new ParseSalesEvent())
                .name("operator: parse sales-enriched");

        DataStream<EventLineageRecord> lineageStream = parsedStream
                .map(event -> new EventLineageRecord(
                        event.eventId,
                        event.traceId,
                        event.saleId,
                        "TopSalesByCity",
                        "AGGREGATION_INPUT",
                        JobConfig.INPUT_TOPIC,
                        JobConfig.OUTPUT_TOPIC,
                        event.cityName + "|" + event.saleDate,
                        System.currentTimeMillis()
                ))
                .name("operator: top-sales lineage audit");

        // --- Pipeline ---
        DataStream<CitySalesResult> aggregatedStream = parsedStream
            .keyBy(event -> event.cityName + "|" + event.saleDate) // composite key of city and date for aggregation, it is important to include the date in the key to ensure that sales from different dates are not aggregated together, even if they are from the same city.
            .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(JobConfig.WINDOW_MINUTES))) // Tumbling window of 5 minutes, it will create non-overlapping windows, and all events that arrive within the same window will be grouped together for processing.
            .aggregate(new CitySalesAggregate(), new CitySalesWindowFormatter())
            .name("operator: aggregate total sales by city");

        aggregatedStream.print("sink: stdout");

        // --- Sink ---
        Properties producerConfig = new Properties();
        producerConfig.setProperty("transaction.timeout.ms", JobConfig.TRANSACTION_TIMEOUT_MS); // Set the transaction timeout to a value that is longer than the maximum expected processing time of a window, this ensures that transactions will not be aborted prematurely while waiting for late events or during long processing times.

        KafkaSink<CitySalesResult> sink = KafkaSink.<CitySalesResult>builder()
            .setBootstrapServers(JobConfig.BOOTSTRAP_SERVERS)
            .setRecordSerializer(new CitySalesResultSerializer())
            .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE) // Exactly-once semantics to ensure that each aggregated result is written to Kafka exactly once, even in the case of failures or retries.
            .setTransactionalIdPrefix(JobConfig.TRANSACTIONAL_ID_PREFIX)
            .setKafkaProducerConfig(producerConfig)
            .build();

        KafkaSink<EventLineageRecord> lineageSink = KafkaSink.<EventLineageRecord>builder()
                .setBootstrapServers(JobConfig.BOOTSTRAP_SERVERS)
                .setRecordSerializer(new EventLineageRecordSerializer())
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .setTransactionalIdPrefix(JobConfig.TRANSACTIONAL_ID_PREFIX + "lineage-")
                .setKafkaProducerConfig(producerConfig)
                .build();

        aggregatedStream.sinkTo(sink).name("sink: " + JobConfig.OUTPUT_TOPIC);
        lineageStream.sinkTo(lineageSink).name("sink: " + JobConfig.EVENT_LINEAGE_TOPIC);

        env.execute("top sales by city");
    }
}
