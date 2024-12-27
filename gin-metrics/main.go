package main

import (
    "fmt"
    "net/http"
    "sync"
    "time"
    "github.com/prometheus/client_golang/prometheus"
    "github.com/prometheus/client_golang/prometheus/promhttp"
)

// LoadTestResult yapısı, yük testi sonuçlarını tutar
type LoadTestResult struct {
    TotalRequests       int
    SuccessfulCount     int
    ErrorCount          int
    AverageResponseTime float64
    MaxResponseTime     time.Duration
    MinResponseTime     time.Duration
}


// Prometheus Metrikleri
var (
    totalRequests = prometheus.NewCounter(prometheus.CounterOpts{
        Name: "total_requests_total",
        Help: "Total number of requests sent.",
    })
    successfulRequests = prometheus.NewCounter(prometheus.CounterOpts{
        Name: "successful_requests_total",
        Help: "Total number of successful requests.",
    })
    failedRequests = prometheus.NewCounter(prometheus.CounterOpts{
        Name: "failed_requests_total",
        Help: "Total number of failed requests.",
    })
    responseTimeSummary = prometheus.NewSummaryVec(prometheus.SummaryOpts{
        Name:       "response_time_ms",
        Help:       "Request response times in milliseconds.",
        Objectives: map[float64]float64{0.5: 0.05, 0.9: 0.01, 0.99: 0.001}, // Percentiles
    }, []string{"status_code"})
)

func init() {
    prometheus.MustRegister(totalRequests, successfulRequests, failedRequests, responseTimeSummary)
}
// performLoadTest işlevi, yük testi gerçekleştirir ve sonuçları döndürür
func performLoadTest() {
    const (
        numRequests = 1000 // Gönderilecek toplam istek sayısı
        concurrency = 1000  // Aynı anda kaç istek gönderilecek
        token       = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3NDM1YjllZjRkNDljMDlkODI1OWMwNCIsImlhdCI6MTczMjQ2NzYxNCwiZXhwIjoxNzM1MDU5NjE0fQ.CUqVqB9iqG30HPU0MjJaIQXmq9YD0OOBN0ncEa2vngM" // Token
    )

    var wg sync.WaitGroup
    sem := make(chan struct{}, concurrency) // Eş zamanlılık kontrolü için kanal

    startTime := time.Now()

    for i := 0; i < numRequests; i++ {
        wg.Add(1)
        sem <- struct{}{} // Kanalın kapasitesini kontrol et

        go func(requestID int) {
            defer wg.Done()
            defer func() { <-sem }() // Kanalı serbest bırak

            // İstek gönderme işlemi
            req, err := http.NewRequest("GET", "http://localhost:8083/api/getRooms", nil)
            if err != nil {
                fmt.Printf("Request %d failed: %v\n", requestID, err)
                failedRequests.Inc() // Hata sayısını arttır
                return
            }
            req.Header.Set("Authorization", "Bearer "+token)

            client := &http.Client{}
            start := time.Now() // Yanıt süresi ölçümü
            resp, err := client.Do(req)
            responseTime := time.Since(start).Milliseconds() // Yanıt süresi hesaplama

            if err != nil {
                fmt.Printf("Request %d failed: %v\n", requestID, err)
                failedRequests.Inc() // Hata sayısını arttır
                return
            }
            defer resp.Body.Close()

            // Yanıt kontrolü
            totalRequests.Inc() // Toplam istek sayısını arttır
            if resp.StatusCode == http.StatusOK {
                successfulRequests.Inc() // Başarılı istek sayısını arttır
                fmt.Printf("Request %d succeeded: Status Code %d\n", requestID, resp.StatusCode)

                // Yanıt süresini Prometheus'a gönder
                responseTimeSummary.WithLabelValues(fmt.Sprintf("%d", resp.StatusCode)).Observe(float64(responseTime))
            } else {
                failedRequests.Inc() // Başarısız istek sayısını arttır
                fmt.Printf("Request %d failed: Status Code %d\n", requestID, resp.StatusCode)
            }
        }(i)
    }

    wg.Wait() // Tüm isteklerin tamamlanmasını bekle
    fmt.Printf("Load test completed in %v\n", time.Since(startTime))
}

func main() {
    // Prometheus için HTTP sunucusu başlatma
    http.Handle("/", promhttp.Handler())
    go func() {
        http.ListenAndServe(":8087", nil)
    }()

    // Yük testini başlat
    performLoadTest()

    // Sunucu kapanmasın, sürekli çalışmaya devam et
    select {} // sonsuz bekleme, böylece uygulama sürekli çalışır
}
