package com.greenteam.service;

import com.greenteam.config.S3Config;
import com.greenteam.model.SalesEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Config s3Config;

    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Client, s3Config);
    }

    private SalesEvent createTestEvent() {
        SalesEvent event = new SalesEvent();
        event.setSalesmanId(1);
        event.setSalesmanName("John Doe");
        event.setSaleId(100);
        event.setQuantity(5);
        event.setSaleDate("2024-01-15T10:30:00");
        event.setProductId(200);
        event.setStoreId(300);
        event.setCityName("New York");
        event.setStoreName("Main Store");
        event.setCountryName("USA");
        event.setAmount("1500.50");
        return event;
    }

    @Test
    void saveEvent_createsNewFileWithHeader_whenFileDoesNotExist() {
        SalesEvent event = createTestEvent();
        when(s3Config.getBucketName()).thenReturn("test-bucket");
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        s3Service.saveEvent(event);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest capturedRequest = requestCaptor.getValue();
        assertEquals("test-bucket", capturedRequest.bucket());
        assertEquals("2024-01-15/USA/New York/100.csv", capturedRequest.key());
        assertEquals("text/csv", capturedRequest.contentType());
    }

    @Test
    void saveEvent_appendsToExistingFile() {
        SalesEvent event = createTestEvent();
        when(s3Config.getBucketName()).thenReturn("test-bucket");

        String existingContent = SalesEvent.getCsvHeader() + System.lineSeparator() +
                "0,Existing,1,1,2024-01-14,1,1,City,Store,Country,100.00" + System.lineSeparator();

        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(
                GetObjectResponse.builder().build(),
                existingContent.getBytes()
        );
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(responseBytes);

        s3Service.saveEvent(event);

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void saveEvent_usesCorrectS3Key() {
        SalesEvent event = createTestEvent();
        event.setSaleDate("2024-03-20T14:00:00");
        event.setCountryName("Brazil");
        event.setCityName("Sao Paulo");
        event.setStoreName("Central");

        when(s3Config.getBucketName()).thenReturn("my-bucket");
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());

        s3Service.saveEvent(event);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        assertEquals("2024-03-20/Brazil/Sao Paulo/100.csv", requestCaptor.getValue().key());
    }

    @Test
    void saveEvent_throwsRuntimeException_onS3Error() {
        SalesEvent event = createTestEvent();
        when(s3Config.getBucketName()).thenReturn("test-bucket");
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().build());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("S3 connection failed"));

        assertThrows(RuntimeException.class, () -> s3Service.saveEvent(event));
    }

    @Test
    void buildKey_extractsDateCorrectly() throws Exception {
        Method buildKeyMethod = S3Service.class.getDeclaredMethod("buildKey", SalesEvent.class);
        buildKeyMethod.setAccessible(true);

        SalesEvent event = createTestEvent();
        event.setSaleDate("2024-06-15T23:59:59");
        event.setCountryName("France");
        event.setCityName("Paris");
        event.setSaleId(999);

        String key = (String) buildKeyMethod.invoke(s3Service, event);

        assertEquals("2024-06-15/France/Paris/999.csv", key);
    }

    @Test
    void extractDateOnly_handlesIsoFormat() throws Exception {
        Method extractDateOnlyMethod = S3Service.class.getDeclaredMethod("extractDateOnly", String.class);
        extractDateOnlyMethod.setAccessible(true);

        assertEquals("2024-01-15", extractDateOnlyMethod.invoke(s3Service, "2024-01-15T10:30:00"));
        assertEquals("2024-12-31", extractDateOnlyMethod.invoke(s3Service, "2024-12-31T00:00:00Z"));
    }

    @Test
    void extractDateOnly_handlesDateOnlyFormat() throws Exception {
        Method extractDateOnlyMethod = S3Service.class.getDeclaredMethod("extractDateOnly", String.class);
        extractDateOnlyMethod.setAccessible(true);

        assertEquals("2024-01-15", extractDateOnlyMethod.invoke(s3Service, "2024-01-15"));
    }

    @Test
    void extractDateOnly_handlesNullAndEmpty() throws Exception {
        Method extractDateOnlyMethod = S3Service.class.getDeclaredMethod("extractDateOnly", String.class);
        extractDateOnlyMethod.setAccessible(true);

        assertEquals("unknown-date", extractDateOnlyMethod.invoke(s3Service, (String) null));
        assertEquals("unknown-date", extractDateOnlyMethod.invoke(s3Service, ""));
    }

    @Test
    void sanitizePathComponent_removesSpecialCharacters() throws Exception {
        Method sanitizeMethod = S3Service.class.getDeclaredMethod("sanitizePathComponent", String.class);
        sanitizeMethod.setAccessible(true);

        assertEquals("New York", sanitizeMethod.invoke(s3Service, "New York"));
        assertEquals("Store123", sanitizeMethod.invoke(s3Service, "Store#123!"));
        assertEquals("CityNamePath", sanitizeMethod.invoke(s3Service, "City/Name\\Path"));
    }

    @Test
    void sanitizePathComponent_handlesNullAndEmpty() throws Exception {
        Method sanitizeMethod = S3Service.class.getDeclaredMethod("sanitizePathComponent", String.class);
        sanitizeMethod.setAccessible(true);

        assertEquals("unknown", sanitizeMethod.invoke(s3Service, (String) null));
        assertEquals("unknown", sanitizeMethod.invoke(s3Service, ""));
    }
}
