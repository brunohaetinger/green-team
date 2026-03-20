package com.greenteam;

import com.greenteam.config.JobConfig;
import com.greenteam.model.CitySalesResult;
import com.greenteam.openlineage.OpenLineageIntegration;
import com.greenteam.operator.CitySalesAggregate;
import com.greenteam.operator.CitySalesWindowFormatter;
import com.greenteam.operator.ParseSalesEvent;
import com.greenteam.serde.CitySalesResultSerializer;
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
    private static final String JOB_NAME = "top-sales-by-city";
    private static final String JOB_NAMESPACE = "green-team-data-kata";

    public static void main(String[] args) throws Exception {

        OpenLineageIntegration lineage = new OpenLineageIntegration(
            JOB_NAME,
            JOB_NAMESPACE,
            "http://marquez-api:4000/api/v1/lineage",
            JobConfig.BOOTSTRAP_SERVERS
        );

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

        // --- Pipeline ---
        DataStream<CitySalesResult> aggregatedStream = inputStream
            .flatMap(new ParseSalesEvent())
            .name("operator: parse sales-enriched")
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

        aggregatedStream.sinkTo(sink).name("sink: " + JobConfig.OUTPUT_TOPIC);

        // Emite o evento OpenLineage de Kafka para Kafka logo antes de iniciar o job
        lineage.emitKafkaToKafkaEvent(
            JobConfig.INPUT_TOPIC,
            JobConfig.OUTPUT_TOPIC,
            io.openlineage.client.OpenLineage.RunEvent.EventType.START
        );

        try {
            env.execute(JOB_NAME);
        } finally {
            lineage.close();
        }
    }
}
