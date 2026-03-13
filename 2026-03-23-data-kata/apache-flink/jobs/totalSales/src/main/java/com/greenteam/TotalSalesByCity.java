package com.greenteam;

import com.greenteam.config.JobConfig;
import com.greenteam.model.CitySalesResult;
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

public class TotalSalesByCity {

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
        DataStream<CitySalesResult> aggregatedStream = inputStream
            .flatMap(new ParseSalesEvent())
            .name("operator: parse sales-events")
            .keyBy(event -> event.cityName + "|" + event.storeName + "|" + event.saleDate)
            .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(JobConfig.WINDOW_MINUTES)))
            .aggregate(new CitySalesAggregate(), new CitySalesWindowFormatter())
            .name("operator: aggregate total sales by city");

        aggregatedStream.print("sink: stdout");

        // --- Sink ---
        Properties producerConfig = new Properties();
        producerConfig.setProperty("transaction.timeout.ms", JobConfig.TRANSACTION_TIMEOUT_MS);

        KafkaSink<CitySalesResult> sink = KafkaSink.<CitySalesResult>builder()
            .setBootstrapServers(JobConfig.BOOTSTRAP_SERVERS)
            .setRecordSerializer(new CitySalesResultSerializer())
            .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
            .setTransactionalIdPrefix(JobConfig.TRANSACTIONAL_ID_PREFIX)
            .setKafkaProducerConfig(producerConfig)
            .build();

        aggregatedStream.sinkTo(sink).name("sink: " + JobConfig.OUTPUT_TOPIC);

        env.execute("total sales by city");
    }
}
