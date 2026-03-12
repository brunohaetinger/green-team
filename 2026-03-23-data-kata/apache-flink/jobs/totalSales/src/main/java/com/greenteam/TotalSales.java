package com.greenteam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class TotalSales {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String INPUT_TOPIC = "sales-events";
    private static final String OUTPUT_TOPIC = "total-sales";
    private static final int WINDOW_MINUTES = 1;

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String>builder()
            .setBootstrapServers("kafka:29092")
            .setTopics(INPUT_TOPIC)
            .setGroupId("sales-events-pipeline")
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new org.apache.flink.api.common.serialization.SimpleStringSchema())
            .build();

        DataStream<String> inputStream = env.fromSource(
            source,
            WatermarkStrategy.noWatermarks(),
            "sales_events source topic"
        );

        // Parse valid sales events and aggregate by city in 1-minute processing windows.
        DataStream<CitySalesResult> aggregatedStream = inputStream
            .flatMap(new ParseSalesEvent())
            .name("Parse sales-events")
            .keyBy(event -> event.cityId)
            .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(WINDOW_MINUTES)))
            .aggregate(new CitySalesAggregate(), new CitySalesWindowFormatter())
            .name("Aggregate total sales per city");

        aggregatedStream.print("total_sales");

        Properties kafkaProducerConfig = new Properties();
        kafkaProducerConfig.setProperty("transaction.timeout.ms", "600000");

        KafkaSink<CitySalesResult> sink = KafkaSink.<CitySalesResult>builder()
            .setBootstrapServers("kafka:29092")
            .setRecordSerializer(new KafkaRecordSerializationSchema<>() {
                @Override
                public ProducerRecord<byte[], byte[]> serialize(
                    CitySalesResult element,
                    KafkaSinkContext context,
                    Long timestamp
                ) {
                    byte[] key = (element.cityId + "|" + element.windowEnd).getBytes(StandardCharsets.UTF_8);
                    byte[] value = element.payload.getBytes(StandardCharsets.UTF_8);
                    return new ProducerRecord<>(OUTPUT_TOPIC, key, value);
                }
            })
            .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
            .setTransactionalIdPrefix("total-sales-")
            .setKafkaProducerConfig(kafkaProducerConfig)
            .build();

        aggregatedStream.sinkTo(sink).name("total_sales topic sink");

        env.execute("Aggregation SalesEvents to TotalSalesPerCity");
    }

    private static class ParseSalesEvent implements FlatMapFunction<String, SaleEvent> {
        @Override
        public void flatMap(String message, Collector<SaleEvent> out) {
            try {
                JsonNode jsonNode = objectMapper.readTree(message);

                String cityId = requiredText(jsonNode, "city_id");
                String countryId = optionalText(jsonNode, "country_id");
                String saleId = requiredText(jsonNode, "sale_id");
                int quantity = jsonNode.path("quantity").asInt(-1);
                String amountRaw = jsonNode.path("amount").asText();

                if (quantity <= 0 || amountRaw == null || amountRaw.isBlank()) {
                    return;
                }

                BigDecimal amount = new BigDecimal(amountRaw);
                out.collect(new SaleEvent(cityId, countryId, saleId, quantity, amount));
            } catch (Exception ignored) {
                // Ignore malformed records so the stream keeps running.
            }
        }
    }

    private static class CitySalesAggregate
        implements AggregateFunction<SaleEvent, CitySalesAccumulator, CitySalesAccumulator> {

        @Override
        public CitySalesAccumulator createAccumulator() {
            return new CitySalesAccumulator();
        }

        @Override
        public CitySalesAccumulator add(SaleEvent value, CitySalesAccumulator accumulator) {
            accumulator.totalAmount = accumulator.totalAmount.add(
                value.amount.multiply(BigDecimal.valueOf(value.quantity))
            );
            accumulator.totalUnits += value.quantity;
            accumulator.eventCount += 1;
            accumulator.saleIds.add(value.saleId);

            if (accumulator.countryId == null && value.countryId != null) {
                accumulator.countryId = value.countryId;
            }

            return accumulator;
        }

        @Override
        public CitySalesAccumulator getResult(CitySalesAccumulator accumulator) {
            return accumulator;
        }

        @Override
        public CitySalesAccumulator merge(CitySalesAccumulator left, CitySalesAccumulator right) {
            left.totalAmount = left.totalAmount.add(right.totalAmount);
            left.totalUnits += right.totalUnits;
            left.eventCount += right.eventCount;
            left.saleIds.addAll(right.saleIds);
            if (left.countryId == null) {
                left.countryId = right.countryId;
            }
            return left;
        }
    }

    private static class CitySalesWindowFormatter
        extends ProcessWindowFunction<CitySalesAccumulator, CitySalesResult, String, TimeWindow> {

        @Override
        public void process(
            String cityId,
            Context context,
            Iterable<CitySalesAccumulator> elements,
            Collector<CitySalesResult> out
        ) {
            CitySalesAccumulator acc = elements.iterator().next();
            String windowStart = Instant.ofEpochMilli(context.window().getStart()).toString();
            String windowEnd = Instant.ofEpochMilli(context.window().getEnd()).toString();

            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("schema_version", "1.0");
            payload.put("aggregation_type", "city_sales");
            payload.put("city_id", cityId);
            if (acc.countryId != null) {
                payload.put("country_id", acc.countryId);
            }
            payload.put("window_start", windowStart);
            payload.put("window_end", windowEnd);
            payload.put("total_amount", acc.totalAmount.setScale(2, RoundingMode.HALF_UP));
            payload.put("total_units", acc.totalUnits);
            payload.put("total_orders", acc.saleIds.size());
            payload.put("event_count", acc.eventCount);
            payload.put("processed_at", Instant.now().toString());

            out.collect(new CitySalesResult(cityId, windowEnd, payload.toString()));
        }
    }

    private static class SaleEvent {
        private final String cityId;
        private final String countryId;
        private final String saleId;
        private final int quantity;
        private final BigDecimal amount;

        private SaleEvent(String cityId, String countryId, String saleId, int quantity, BigDecimal amount) {
            this.cityId = cityId;
            this.countryId = countryId;
            this.saleId = saleId;
            this.quantity = quantity;
            this.amount = amount;
        }
    }

    private static class CitySalesAccumulator {
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private long totalUnits = 0;
        private long eventCount = 0;
        private String countryId;
        private final Set<String> saleIds = new HashSet<>();
    }

    private static class CitySalesResult {
        private final String cityId;
        private final String windowEnd;
        private final String payload;

        private CitySalesResult(String cityId, String windowEnd, String payload) {
            this.cityId = cityId;
            this.windowEnd = windowEnd;
            this.payload = payload;
        }

        @Override
        public String toString() {
            return payload;
        }
    }

    private static String requiredText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        return value.asText();
    }

    private static String optionalText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.asText().isBlank()) {
            return null;
        }
        return value.asText();
    }
}
