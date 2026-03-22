package com.greenteam;

import com.greenteam.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesEventConsumerTest {

    @Mock
    private S3Service s3Service;

    private SalesEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new SalesEventConsumer(s3Service);
    }

    @Test
    void consume_parsesMessageAndSavesToS3() {
        String validMessage = """
            {
                "salesman_id": 1,
                "salesman_name": "John Doe",
                "sale_id": 100,
                "quantity": 5,
                "sale_date": "2024-01-15T10:30:00",
                "product_id": 200,
                "store_id": 300,
                "city_name": "New York",
                "store_name": "Main Store",
                "country_name": "USA",
                "amount": "1500.50"
            }
            """;

        consumer.consume(validMessage);

        verify(s3Service).saveEvent(argThat(event ->
                event.getSalesmanId() == 1 &&
                "John Doe".equals(event.getSalesmanName()) &&
                event.getSaleId() == 100 &&
                event.getQuantity() == 5 &&
                "New York".equals(event.getCityName()) &&
                "Main Store".equals(event.getStoreName()) &&
                "USA".equals(event.getCountryName()) &&
                "1500.50".equals(event.getAmount())
        ));
    }

    @Test
    void consume_handlesInvalidJson_doesNotThrow() {
        String invalidMessage = "not valid json";

        consumer.consume(invalidMessage);

        verify(s3Service, never()).saveEvent(any());
    }

    @Test
    void consume_handlesS3Error_doesNotThrow() {
        String validMessage = """
            {
                "salesman_id": 1,
                "salesman_name": "Test",
                "sale_id": 1,
                "quantity": 1,
                "sale_date": "2024-01-15",
                "product_id": 1,
                "store_id": 1,
                "city_name": "City",
                "store_name": "Store",
                "country_name": "Country",
                "amount": "100.00"
            }
            """;

        doThrow(new RuntimeException("S3 error")).when(s3Service).saveEvent(any());

        consumer.consume(validMessage);

        verify(s3Service).saveEvent(any());
    }

    @Test
    void consume_handlesEmptyMessage() {
        consumer.consume("");

        verify(s3Service, never()).saveEvent(any());
    }

    @Test
    void consume_handlesPartialJson() {
        String partialMessage = """
            {
                "salesman_id": 1,
                "salesman_name": "Partial"
            }
            """;

        consumer.consume(partialMessage);

        verify(s3Service).saveEvent(argThat(event ->
                event.getSalesmanId() == 1 &&
                "Partial".equals(event.getSalesmanName())
        ));
    }

    @Test
    void consume_ignoresUnknownFields() {
        String messageWithExtraFields = """
            {
                "salesman_id": 1,
                "salesman_name": "Test",
                "sale_id": 1,
                "quantity": 1,
                "sale_date": "2024-01-15",
                "product_id": 1,
                "store_id": 1,
                "city_name": "City",
                "store_name": "Store",
                "country_name": "Country",
                "amount": "100.00",
                "unknown_field": "ignored",
                "another_unknown": 123
            }
            """;

        consumer.consume(messageWithExtraFields);

        verify(s3Service).saveEvent(any());
    }
}
