# Hafta 1 — Yazılım Testine Giriş

**Tek fikir:** test edenler, hatalar kullanıcıyı bulmadan önce hataları bulmaya çalışır.

Bu metin Hafta 1 dersinin kısa bir özetidir. Önce oku, sonra sorularını yaz. İyi bir soru yalnızca tanım sormaz, küçük bir örnek kullanır. Sınav İngilizce olduğu için terimlerin İngilizcesi parantez içinde verilmiştir.

## 1. Yazılımı neden test ederiz?

Yazılım uçakları, arabaları, bankaları, hastaneleri ve telefonları yönetiyor. Yazılım arıza yaptığında insanlar para, zaman, hatta hayatını kaybedebilir. Dersten örnekler:

- Bir sağlık sitesi açıldığı gün defalarca çöktü. Kimse siteyi aynı anda çok kullanıcıyla denememişti (yük testi yapılmamıştı).
- Boeing 737 Max, uçuş yazılımı (MCAS) uçağın burnunu gereğinden sert aşağı ittiği için düştü.
- ABD Kuzeydoğu elektrik kesintisinde yaklaşık 50 milyon kişi elektriksiz kaldı. Alarm yazılımı arıza yapmıştı, operatörler sorunu zamanında göremedi.

Test edenin amacı hataları **olabildiğince erken** ortadan kaldırmaktır. Bu kaliteyi artırır, maliyeti düşürür, müşteriyi memnun eder.

## 2. Fault, error, failure

Bu üç kelime aynı şey değildir.

| Terim | Anlamı | Doktor örneği |
|---|---|---|
| **Fault** (kusur) | Koddaki durağan hata (orada yazılı duran yanlış) | Hastalığın kendisi |
| **Error** (yanlış iç durum) | Program çalışırken oluşan yanlış iç durum | Yüksek tansiyon, kandaki bakteri |
| **Failure** (arıza) | Dışarıdan görülebilen yanlış davranış | Hastanın hissettiği belirtiler |

Yazılım kusurları donanımdaki tasarım hatalarına benzer. Yazılım eskimez, aşınmaz: kusur baştan beri oradadır.

**Kod örneği.** Bu metot bir dizideki sıfırları saymalıdır:

```java
public static int numZero(int[] arr) {
    int count = 0;
    for (int i = 1; i < arr.length; i++) {   // fault: 0'dan başlamalı
        if (arr[i] == 0) count++;
    }
    return count;
}
```

- Girdi `[2, 7, 0]` → beklenen 1, gerçekleşen 1. Kusurlu satır çalışır ve durum yanlıştır (`i` 0 değil 1: bir **error**), ama **failure yoktur**.
- Girdi `[0, 2, 7]` → beklenen 1, gerçekleşen 0. Yanlış durum çıktıya ulaşır: bir **failure**.

Yani kusurlu bir program yine de bir testi geçebilir. Failure'ı görüp görmeyeceğimizi test girdisi belirler.

## 3. Üçgen programı (Myers)

Program üç tam sayı okur: bir üçgenin kenar uzunlukları. Ekrana *scalene* (çeşitkenar), *isosceles* (ikizkenar) veya *equilateral* (eşkenar) yazar. Hangi testler gerekir? Çoğu kişi çok az test yazar. İyi testler şunları içerir:

- geçerli bir çeşitkenar, geçerli bir ikizkenar ve geçerli bir eşkenar üçgen;
- ikizkenarın üç sıralaması: (3, 3, 4), (3, 4, 3), (4, 3, 3);
- bir kenarı 0 olan, bir kenarı negatif olan ve (0, 0, 0) girdileri;
- iki kenarın toplamı üçüncüye eşit olan üç sayı, örneğin (1, 2, 3): bu bir üçgen **değildir**;
- iki kenarın toplamı üçüncüden küçük olan üç sayı, örneğin (1, 2, 4);
- tam sayı olmayan değerler ve yanlış sayıda değer (üç yerine iki sayı);
- **her** test için beklenen çıktı.

Ders: çok küçük bir program bile çok test ister; geçersiz girdiler geçerli girdiler kadar önemlidir.

## 4. Verification ve validation (V&V)

- **Validation (geçerleme):** **doğru ürünü** mü yapıyoruz? Ürün müşterinin ihtiyacını karşılıyor mu?
- **Verification (doğrulama):** **ürünü doğru** mu yapıyoruz? Her adım, bir önceki adımın gereksinimlerini karşılıyor mu?
- **IV&V:** bağımsız doğrulama ve geçerleme; geliştirme ekibinin dışındaki kişiler yapar.

## 5. Yazılım kalite güvencesi (SQA)

SQA'nın amacı kusurları bulmak, raporlamak **ve** düzeltilmelerini sağlamaktır.

**Kusur (defect) nedir?**

1. Program spesifikasyonuna uymuyor. (Yalnızca eksiksiz ve doğru bir spesifikasyon varsa işe yarar.)
2. Program, kullanıcısının makul olarak beklediği şeyi yapmıyor. (Spesifikasyon olmasa bile her zaman kullanılabilir.)

**Kusur kategorileri:** işlevsel (functional), kullanıcı arayüzü (user interface), performans, hata işleme (error handling), güvenlik (security), yük (load), yapılandırma (configuration), yarış durumu (race condition), dokümantasyon.

**Maliyet.** Bir kusur sistemde ne kadar uzun kalırsa düzeltmesi o kadar pahalı olur. Gereksinim aşamasında bulunan bir gereksinim kusuru ucuzdur. Aynı kusur ürün teslim edildikten sonra bulunursa çok pahalıdır. Bu yüzden SQA yalnızca sonda değil, tüm yaşam döngüsü boyunca yapılır.

İki ana SQA etkinliği **teknik gözden geçirme (technical review)** ve **yazılım testidir**.

## 6. Teknik gözden geçirmeler (reviews)

Gözden geçirmede küçük ve yetkin bir grup bir iş ürününü okur ve geri bildirim verir. Her şey gözden geçirilebilir: gereksinimler, tasarım, kod, test planı, kullanıcı kılavuzu. Gözden geçirme gayriresmî (arkadaşa sormak, pair programming) veya resmî (planlı toplantı, toplantı öncesi hazırlık) olabilir.

Toplantı kuralları: **üreteni değil ürünü** incele; gündeme bağlı kal; sorunları söyle ama toplantıda düzeltmeye çalışma; yazılı not tut; grubu küçük tut; toplantıdan önce hazırlan; kontrol listesi (checklist) kullan.

Gözden geçirme, maliyet ve bulunan kusur sayısı açısından en etkili kalite tekniğidir, ama ekipler çoğu zaman atlar.

## 7. Yazılım testi: temel bilgiler

- Test, yazılımı **çalıştırmak** ve gerçekleşen sonucu beklenen sonuçla karşılaştırmaktır.
- Test en yaygın kalite etkinliğidir ama en etkilisi değildir. Tüm test türleri birlikte genellikle kusurların %60'ından azını bulur. Tipik bir proje kaynaklarının yaklaşık %50'sini teste harcayabilir.
- **Tüketici test (exhaustive testing) mümkün değildir.** Girdi kombinasyonlarının ve koddaki yolların sayısı pratikte sonsuzdur.
- Bu yüzden **kısmi test** yaparız; kısmi test kusur olmadığını asla kanıtlayamaz.
- İyi test, az sayıda testi akıllıca seçmektir: tüm gereksinimleri test et, kullanıcıların gerçekten yaptıklarını test et, birbirini tekrar eden testlerden kaçın, tasarım ve koddaki zayıf yerleri ara.

**Kara kutu ve beyaz kutu testi**

| | Black-box (kara kutu) | White-box (beyaz kutu) |
|---|---|---|
| Test eden neyi bilir | yalnızca dış davranışı (spesifikasyon) | spesifikasyonu ve iç kodu |
| Testler nereden gelir | spesifikasyondan | spesifikasyondan ve program mantığından |
| "Her şeyi test et" ne demek olurdu | olası her girdi | olası her yol |

İki "her şeyi test et" fikri de pratikte imkânsızdır.

**Doğru tutum.** Testin amacı yazılımı *kırmaktır*. Test eden kusur bulmayı beklemeli ve istemelidir. Hem geliştiriciler hem bağımsız bir test grubu test etmelidir; çünkü farklı düşünürler ve farklı kusurlar bulurlar.

**Kusurlar kümelenir (defects cluster).** Kusurların yaklaşık %80'i kodun yaklaşık %20'sinde bulunur. Karmaşık kodda daha çok kusur olur; karmaşık modülleri daha çok test et.

**Kaç kusur?** Çoğu projede 1000 satır kodda 1 ile 25 arasında kusur vardır.

**Otomasyon.** Testler tekrar tekrar çalıştırılır, bu yüzden otomasyon gereklidir. Bazı testleri otomatikleştirmek zordur, elle çalıştırılır.

## 8. Test seviyeleri

- **Unit testing (birim testi):** tek bir modülü kontrollü bir ortamda tek başına test et. **Driver**, birimi çağıran koddur. **Stub**, birimin çağırdığı modülün yerine geçen basit koddur.
- **Integration testing (entegrasyon testi):** birimleri birleştir ve birlikte çalışmalarını test et (yukarıdan aşağı veya aşağıdan yukarı).
- **System testing (sistem testi):** tüm sistemi sistem gereksinimlerine göre test et: işlevsellik, performans, güvenilirlik, kullanılabilirlik.
- **Regression testing (regresyon testi):** bir değişiklikten sonra eski testleri yeniden çalıştır; önceden çalışan bir şeyin bozulmadığını kontrol et. Regresyon paketi genellikle büyüktür; otomatik olmalı ve güncel tutulmalıdır (her hata düzeltmesi ve her yeni özellik için bir test ekle).

## 9. Formal verification

Üçüncü bir kalite yaklaşımı: sistemin matematiksel bir modelini kur ve doğru davrandığını **kanıtla**. Büyük sistemler için pahalı ve zordur; küçük ve kritik parçalarda kullanılır.

## Soru fikirleri

- Bir hata hikâyesi ver ve sor: hangi kısım fault, hangisi error, hangisi failure?
- Üçgen programı için bir test listesi ver ve hangi önemli testin eksik olduğunu sor.
- Bir etkinliği anlat ve sor: verification mı validation mı? Kara kutu mu beyaz kutu mu? Hangi test seviyesi?
- Bir kusuru anlat ve kategorisini sor.
