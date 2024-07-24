package ru.iashinme;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

    private final ObjectMapper objectMapper;
    private final int requestLimit;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        objectMapper = new ObjectMapper().findAndRegisterModules();

        this.scheduler.scheduleAtFixedRate(() -> requestCount.set(0), 0, 1, timeUnit);
    }

    public boolean createDocument(Document document) {
        if (requestCount.incrementAndGet() <= requestLimit) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(document)))
                        .build();


                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

                return true;
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        } else {
            requestCount.decrementAndGet();
            return false;
        }
    }

    public record Document(
            Description description,
            String doc_id,
            String doc_status,
            String doc_type,
            boolean importRequest,
            String owner_inn,
            String participant_inn,
            String producer_inn,
            String production_date,
            String production_type,
            List<Product> products,
            String reg_date,
            String reg_number
    ) {
    }

    public record Description(
            String participantInn
    ) {
    }

    public record Product(
            String certificate_document,
            String certificate_document_date,
            String certificate_document_number,
            String owner_inn,
            String producer_inn,
            String production_date,
            String tnved_code,
            String uit_code,
            String uitu_code
    ) {
    }
}