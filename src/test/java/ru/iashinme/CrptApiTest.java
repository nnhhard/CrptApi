package ru.iashinme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CrptApiTest {

    private final static CrptApi.Document DOCUMENT = new CrptApi.Document(
            new CrptApi.Description("123456789012"),
            "doc1",
            "NEW",
            "TYPE1",
            true,
            "123456789012",
            "123456789012",
            "123456789012",
            "2023-01-01",
            "PROD_TYPE",
            List.of(new CrptApi.Product(
                    "cert_doc",
                    "2023-01-01",
                    "cert_number",
                    "123456789012",
                    "123456789012",
                    "2023-01-01",
                    "tnved_code",
                    "uit_code",
                    "uitu_code"
            )),
            "2023-01-01",
            "reg_number"
    );

    private CrptApi crptApi;

    private HttpClient httpClientMock;


    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        crptApi = new CrptApi(TimeUnit.SECONDS, 2);
        httpClientMock = mock(HttpClient.class);

        Field httpClientField = CrptApi.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(crptApi, httpClientMock);
    }

    @Test
    void testCreateDocumentSuccess() {
        boolean result = crptApi.createDocument(DOCUMENT);
        assertTrue(result, "The document should be created successfully when under the limit.");
        verify(httpClientMock, times(1)).sendAsync(any(), any());
    }

    @Test
    void testCreateDocumentFailOverLimit() throws InterruptedException {
        assertTrue(crptApi.createDocument(DOCUMENT), "The first document should be created successfully.");
        assertTrue(crptApi.createDocument(DOCUMENT), "The second document should be created successfully.");
        assertFalse(crptApi.createDocument(DOCUMENT), "The third document should fail as it exceeds the limit.");

        Thread.sleep(1000L);

        assertTrue(crptApi.createDocument(DOCUMENT), "The first document should be created successfully.");
        assertTrue(crptApi.createDocument(DOCUMENT), "The second document should be created successfully.");

        verify(httpClientMock, times(4)).sendAsync(any(), any());
    }
}