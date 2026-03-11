package com.greenteam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

public class TopSalesman {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        // 1. Create execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 2. Configure Kafka source
        KafkaSource<String> source = KafkaSource.<String>builder()
            .setBootstrapServers("kafka:29092")
            .setTopics("sales-events")
            .setGroupId("flink-pipeline-sales-events") // group id means that if you restart the job, it will continue from where it left off, it is important for exactly-once processing
            .setStartingOffsets(OffsetsInitializer.earliest()) // start from the earliest offset, so we process all existing data in the topic when the job starts
            .setValueOnlyDeserializer(new SimpleStringSchema()) // we only care about the value of the Kafka message, and it is a string, so we use SimpleStringSchema
            .build();

        // 3. Create input stream
        DataStream<String> inputStream = env.fromSource(
            source,
            WatermarkStrategy.noWatermarks(), // we are not doing any time-based processing, so we don't need watermarks, this is a simple pass-through job
            "Kafka Flink Sales Events Source" // name of the source operator, useful for debugging and monitoring,m source operator is the component that reads data from Kafka and creates a stream of data in Flink
        );

        // 4. Transform the data
        DataStream<String> processedStream = inputStream
            .map(message -> {
                // Try to parse as JSON
                JsonNode jsonNode = objectMapper.readTree(message);
                ObjectNode outputNode = objectMapper.createObjectNode();
                
                // Copy original fields
                outputNode.setAll((ObjectNode) jsonNode);
                
                // Add processing metadata
                outputNode.put("processed", true);
                outputNode.put("processedAt", System.currentTimeMillis());
                
                return objectMapper.writeValueAsString(outputNode);
            })
            .name("Transform Flink Sales Events"); // name of the map operator, useful for debugging and monitoring, map operator is the component that transforms the data, in this case we are parsing the JSON and adding some metadata

        // Also print for debugging
        processedStream.print("Processed");

        // Keep transaction timeout under the broker's max timeout.
        Properties kafkaProducerConfig = new Properties();
        kafkaProducerConfig.setProperty("transaction.timeout.ms", "600000");

        // 5. Configure Kafka sink
        KafkaSink<String> sink = KafkaSink.<String>builder()
            .setBootstrapServers("kafka:29092")
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder()
                    .setTopic("top-salesman")
                    .setValueSerializationSchema(new SimpleStringSchema())
                    .build()
            )
            .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE) // ensure that each message is processed and written to Kafka exactly once, even in case of failures, this is important for data integrity
            .setTransactionalIdPrefix("top-salesman-") // required for EXACTLY_ONCE to provide unique transaction IDs
            .setKafkaProducerConfig(kafkaProducerConfig)
            .build();

        // 6. Write to Kafka
        processedStream.sinkTo(sink).name("Kafka Flink Top Salesman Sink"); // name of the sink operator, useful for debugging and monitoring, sink operator is the component that writes data to Kafka

        // 7. Execute the job
        env.execute("Top Salesman Pipeline");
    }
}
