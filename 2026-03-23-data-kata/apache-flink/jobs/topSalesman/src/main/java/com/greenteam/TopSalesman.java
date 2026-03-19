package com.greenteam;

import com.greenteam.config.JobConfig;
import com.greenteam.model.TopSalesmanResult;
import com.greenteam.operator.ParseSalesEvent;
import com.greenteam.serde.TopSalesmanResultSerializer;
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

public class TopSalesman {
    private static final String JOB_NAME = "topSalesman: aggregate top salesman nationwide";

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

        // --- Pipeline ---
        DataStream<TopSalesmanResult> aggregatedStream = inputStream
                .flatMap(new ParseSalesEvent())
                .name("operator: parse sales-enriched")
                .keyBy(event -> event.salesmanId + "|" + event.saleDate)
                .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(JobConfig.WINDOW_MINUTES)))
                .aggregate(new com.greenteam.operator.TopSalesmanAggregate(), new com.greenteam.operator.TopSalesmanWindowFormatter())
                .name("operator: aggregate salesman by id and date");

        aggregatedStream.print("sink: stdout");

        // --- Sink ---
        Properties producerConfig = new Properties();
        producerConfig.setProperty("transaction.timeout.ms", JobConfig.TRANSACTION_TIMEOUT_MS);

        KafkaSink<TopSalesmanResult> sink = KafkaSink.<TopSalesmanResult>builder()
                .setBootstrapServers(JobConfig.BOOTSTRAP_SERVERS)
                .setRecordSerializer(new TopSalesmanResultSerializer())
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .setTransactionalIdPrefix(JobConfig.TRANSACTIONAL_ID_PREFIX)
                .setKafkaProducerConfig(producerConfig)
                .build();

        aggregatedStream.sinkTo(sink).name("sink: " + JobConfig.OUTPUT_TOPIC);

        env.execute(JOB_NAME);
    }
}
