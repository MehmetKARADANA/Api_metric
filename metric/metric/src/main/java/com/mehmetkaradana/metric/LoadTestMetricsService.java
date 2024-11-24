package com.mehmetkaradana.metric;

import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.HTTPServer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.*;

public class LoadTestMetricsService {

    private static final int TOTAL_REQUESTS = 1000; // Toplam istek sayısı
    private static final int THREAD_COUNT = 100; // Aynı anda gönderilecek istek sayısı
    private static final String URL = "http://localhost:8081/api/getRooms"; // Test etmek istediğiniz endpoint

    private static final RestTemplate restTemplate = new RestTemplate();

    // Prometheus Metrikleri
    private static final Counter totalRequests = Counter.build()
            .name("total_requests")
            .help("Total number of requests sent.")
            .register();

    private static final Counter successfulRequests = Counter.build()
            .name("successful_requests")
            .help("Total number of successful requests.")
            .register();

    private static final Counter failedRequests = Counter.build()
            .name("failed_requests")
            .help("Total number of failed requests.")
            .register();

    private static final Summary responseTimeSummary = Summary.build()
            .name("response_time_ms")
            .help("Request response times in milliseconds.")
            .labelNames("status_code") // status_code etiketi ekleniyor
            .quantile(0.5, 0.05)  // Medyan (50. yüzdelik dilim) %5 hata toleransı ile
            .quantile(0.9, 0.01)  // 90. yüzdelik dilim %1 hata toleransı ile
            .quantile(0.99, 0.001) // 99. yüzdelik dilim %0.1 hata toleransı ile
            .register();

    public static void main(String[] args) throws IOException {
        // Prometheus HTTP Sunucusunu Başlatma
      /*  HTTPServer prometheusServer = new HTTPServer(8085);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);*/
        HTTPServer prometheusServer = new HTTPServer(8085);
             ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

            for (int i = 0; i < TOTAL_REQUESTS; i++) {

                executor.submit(LoadTestMetricsService::sendRequestAndMeasureTime);
            }

            executor.shutdown();

    }


    private static Long sendRequestAndMeasureTime() {
        try {
            long start = System.currentTimeMillis();

            // Header'ları ayarlama (örneğin, Authorization için)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3NDM1YjllZjRkNDljMDlkODI1OWMwNCIsImlhdCI6MTczMjQ2NzYxNCwiZXhwIjoxNzM1MDU5NjE0fQ.CUqVqB9iqG30HPU0MjJaIQXmq9YD0OOBN0ncEa2vngM");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // İstek gönderme
            ResponseEntity<String> response = restTemplate.exchange(URL, HttpMethod.GET, entity, String.class);

            long end = System.currentTimeMillis();
            long responseTime = end - start;

            // Metrikleri Güncelleme
            totalRequests.inc(); // Toplam istek sayısını artır
                                                       //status code 200 atanıyor
            responseTimeSummary.labels(String.valueOf(response.getStatusCodeValue())).observe(responseTime); // Yanıt süresi ve durum kodu gözlemi

            if (response.getStatusCode().is2xxSuccessful()) {
                successfulRequests.inc(); // Başarılı istek sayısını artır
                System.out.println("Request succeded: "+response.getStatusCode());
                return responseTime;
            } else {
                failedRequests.inc(); // Başarısız istek sayısını artır
                System.out.println("Error: " + response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            failedRequests.inc();
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }


/*
    private static void analyzeResults(List<Future<Long>> responseTimes) {
        List<Long> times = new ArrayList<>();
        int errorCount = 0;

        for (Future<Long> future : responseTimes) {
            try {
                Long time = future.get();
                if (time != null) {
                    times.add(time);
                } else {
                    errorCount++;
                }
            } catch (Exception e) {
                errorCount++;
            }
        }

        long total = times.stream().mapToLong(Long::longValue).sum();
        long maxTime = times.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = times.stream().mapToLong(Long::longValue).min().orElse(0);

        System.out.println("Total Requests: " + (times.size() + errorCount));
        System.out.println("Successful Requests: " + times.size());
        System.out.println("Failed Requests: " + errorCount);
        System.out.println("Average Response Time: " + (times.size() > 0 ? (total / times.size()) : 0) + " ms");
        System.out.println("Maximum Response Time: " + maxTime + " ms");
        System.out.println("Minimum Response Time: " + minTime + " ms");
    }*/
}
