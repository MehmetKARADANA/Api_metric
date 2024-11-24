package com.example.test

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.prometheus.client.Counter
import io.prometheus.client.Summary
import io.prometheus.client.exporter.HTTPServer
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

object LoadTest {
    private const val TOTAL_REQUESTS = 1000 // Toplam istek sayısı
    private const val CONCURRENT_REQUESTS = 100 // Aynı anda gönderilecek istek sayısı
    private const val URL = "http://localhost:8082/api/getRooms" // Test etmek istediğiniz endpoint

    private val client = HttpClient()

    // Prometheus Metrikleri
    private val totalRequests = Counter.build()
        .name("total_requests")
        .help("Total number of requests sent.")
        .register()

    private val successfulRequests = Counter.build()
        .name("successful_requests")
        .help("Total number of successful requests.")
        .register()

    private val failedRequests = Counter.build()
        .name("failed_requests")
        .help("Total number of failed requests.")
        .register()

    private val responseTimeSummary = Summary.build()
        .name("response_time_ms")
        .help("Request response times in milliseconds.")
        .labelNames("status_code") // Durum kodu etiketi ekliyoruz
        .quantile(0.5, 0.05)
        .quantile(0.9, 0.01)
        .quantile(0.99, 0.001)
        .register()

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        // Prometheus HTTP sunucusunu başlatıyoruz
        HTTPServer(8086)


        for (i in 0 until TOTAL_REQUESTS step CONCURRENT_REQUESTS) {
            val jobs = List(CONCURRENT_REQUESTS) {
                launch {
                    val responseTime = sendRequestAndMeasureTime()

                }
            }

            // Her grup için tüm işlerin tamamlanmasını bekle
            jobs.forEach { it.join() }
        }

    }
    private suspend fun sendRequestAndMeasureTime(): Long? {
        return try {
            lateinit var response: HttpResponse

            // Yanıt süresini ölç
            val responseTime = measureTimeMillis {
                response = client.get(URL) {
                    header(HttpHeaders.Authorization, "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3NDM1YjllZjRkNDljMDlkODI1OWMwNCIsImlhdCI6MTczMjQ2NzYxNCwiZXhwIjoxNzM1MDU5NjE0fQ.CUqVqB9iqG30HPU0MjJaIQXmq9YD0OOBN0ncEa2vngM")
                }
            }

            // Yanıt kontrolü ve metrik güncelleme
            totalRequests.inc() // Her durumda toplam istek sayısını artır
            if (response.status.isSuccess()) {
                successfulRequests.inc() // Başarılı istek sayısını artır
            } else {
                failedRequests.inc() // Başarısız istek sayısını artır
                println("Error: ${response.status}")
                return null
            }

            // Yanıt süresini Prometheus metriğine kaydet
            responseTimeSummary.labels(response.status.value.toString()).observe(responseTime.toDouble())

            return responseTime
        } catch (e: Exception) {
            failedRequests.inc()
            println("Error: ${e.message}")
            return null
        }
    }

    /*
    private suspend fun sendRequestAndMeasureTime(): Long? {
        return try {
            // Yanıt süresini ölç
            val responseTime = measureTimeMillis {
                val response: HttpResponse = client.get(URL) {
                    header(HttpHeaders.Authorization, "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3MWEyOTFiNzY0YThlMTEwNzlkMGYyZSIsImlhdCI6MTcyOTc2NzcwNywiZXhwIjoxNzMyMzU5NzA3fQ.ijMcqGw4OUOWG79jrXhmkAwGolVJUjNXp6Y15y-OY7Y")
                }

                if (response.status.isSuccess()) {
                    // Başarılı istek, metrikleri güncelle
                    totalRequests.inc()
                    successfulRequests.inc()
                } else {
                    // Başarısız istek, metrikleri güncelle
                    failedRequests.inc()
                    println("Error: ${response.status}")
                    //return@measureTimeMillis
                   return null
                }
            }

            // Yanıt süresini Prometheus metriğine kaydet
            responseTimeSummary.labels("200").observe(responseTime.toDouble())

            return responseTime
        } catch (e: Exception) {
            failedRequests.inc()
            println("Error: ${e.message}")
            return null
        }
    }
    */
/*
    private fun analyzeResults(responseTimes: List<Long>, errorCount: Int) {
        val total = responseTimes.sum()
        val maxTime = responseTimes.maxOrNull() ?: 0
        val minTime = responseTimes.minOrNull() ?: 0

        println("Total Requests: ${responseTimes.size + errorCount}")
        println("Successful Requests: ${responseTimes.size}")
        println("Failed Requests: $errorCount")
        println("Average Response Time: ${if (responseTimes.isNotEmpty()) total / responseTimes.size else 0} ms") // Ortalama yanıt süresi
        println("Maximum Response Time: $maxTime ms")
        println("Minimum Response Time: $minTime ms")
    }*/
}

