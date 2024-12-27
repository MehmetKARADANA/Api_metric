Load Test Metrics Service
Bu proje, API'lere yük testi yaparak metrik toplamak amacıyla yazılmış bir uygulamadır. Prometheus ile entegrasyon sağlar ve API'nin yanıt sürelerini, başarılı/başarısız istekleri izler. Ayrıca, test sırasında alınan metriklerin Prometheus formatında toplandığı ve sunulduğu bir HTTP sunucusu da çalıştırılır.

Teknolojiler
Java: Sunucu tarafında kullanılır.
Kotlin (Önerilen): Daha kısa ve daha verimli yazılım yazımı için Kotlin önerilmektedir. Java ile aynı işlevselliği sağlar.
Go: Alternatif olarak Go ile de aynı işlevsellik sağlanabilir, ancak bu projede Go kullanımı önerilmez.
Prometheus: Uygulamanın metriklerini toplamak ve sunmak için kullanılır.
Spring Boot (Java): Uygulama yönetimi ve HTTP isteklerini işlemek için kullanılır.
RestTemplate (Java): API istekleri göndermek için kullanılır.
Not: Bu projede kullanılan üç dil (Java, Kotlin, Go) aynı işlevselliği sağlar. Ancak, Kotlin kullanılması önerilir çünkü daha modern ve daha kısa bir yazım tarzı sunar.

Gereksinimler
Prometheus server'ı çalıştırılmalıdır.
Test edilen API'nin URL'si (http://localhost:8081/api/getRooms) doğru şekilde ayarlanmalıdır.
Token gereksinimleri:
API 2.0: API 2.0 sürümü için Authorization başlığında bir Bearer Token gereklidir.
OpenPerformance Version: OpenPerformance versiyonunda token gerekmez.
Kurulum ve Çalıştırma
1. Prometheus Sunucusunu Başlatma
Prometheus, metrikleri toplar ve izler. Prometheus sunucusunu 8085 portunda çalıştırmak için aşağıdaki komutu kullanabilirsiniz:

bash
Kodu kopyala
java -jar prometheus.jar --web.listen-address=":8085"
2. Uygulamayı Çalıştırma
Uygulama, toplamda 1000 istek göndererek API'nin performansını test eder. Uygulama çalıştırıldığında, Prometheus metrik sunucusu 8085 portunda açılır ve isteklerin yanı sıra yanıt süreleri gibi bilgileri toplar.

Java ile çalıştırmak için:

bash
Kodu kopyala
mvn clean install
mvn spring-boot:run
Kotlin ile çalıştırmak için:

Kotlin projesi için Gradle veya Maven kullanarak uygulamayı başlatabilirsiniz.

bash
Kodu kopyala
gradle run
Go ile çalıştırmak için:

Go projesini başlatmak için:

bash
Kodu kopyala
go run main.go
3. Test Sonuçlarını Görüntüleme
Uygulama çalıştıktan sonra, API isteklerinin metriklerini görmek için Prometheus UI'ını kullanabilirsiniz:

arduino
Kodu kopyala
http://localhost:8085
4. API Token
API 2.0: Authorization başlığında Bearer Token gereklidir. Bu token, uygulamaya belirli yetkilere sahip bir kullanıcı tarafından sağlanmalıdır.
OpenPerformance Version: OpenPerformance sürümünde token gerekmemektedir.
Örnek Authorization başlığı:

http
Kodu kopyala
Authorization: Bearer <your_token>
5. Yük Testi
Uygulama, belirlediğiniz API endpoint'ine 1000 istek gönderir. Her istek için yanıt süresi ve durum kodu ölçülür. Prometheus, yanıt sürelerini, başarılı ve başarısız istek sayılarını toplar.

Test Sonuçları
Test sonuçları, aşağıdaki metriklerle birlikte Prometheus tarafından toplandığı için:

total_requests: Gönderilen toplam istek sayısı.
successful_requests: Başarılı istek sayısı.
failed_requests: Başarısız istek sayısı.
response_time_ms: API'nin yanıt süreleri (ms).
Kod Açıklamaları
sendRequestAndMeasureTime(): Bu fonksiyon, her bir API isteği gönderildiğinde çalışır, yanıt süresini ölçer ve Prometheus metriklerini günceller.
Metrikler: Başarılı/başarısız istekler ve yanıt süreleri, Prometheus için toplanır.
Kullanıcılar için Notlar
Bu projede Kotlin kullanmak daha verimli olabilir. Ancak, Java veya Go da kullanılabilir.
API'ye yapılan isteklerin doğruluğu, API'nin yanıt verdiği statü kodlarına göre raporlanır.
Lisans
Bu proje, MIT Lisansı ile lisanslanmıştır.

