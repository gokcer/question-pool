# Hafta 2 — Test Tasarımı

**Tek fikir:** her şeyi test edemeyiz; bu yüzden hangi testleri yazacağımızı ve ne zaman duracağımızı söyleyen açık bir kurala (kapsama ölçütü, coverage criterion) ihtiyacımız var.

Sınav İngilizce olduğu için terimlerin İngilizcesi korunmuştur.

## 1. Test ve risk

- **Test yalnızca arızaların (failure) var olduğunu gösterebilir. Hiç arıza olmadığını gösteremez.**
- Yazılımı her kullandığımızda bir risk alırız. Bazen risk küçüktür, bazen felakettir.
- Test edenler ve geliştiriciler **riski azaltmak** için birlikte çalışır.
- Büyük bir sistem için "doğru" (correct) kelimesinin pek anlamı yoktur (bir bina "doğru" mudur?). Mühendisler bu karmaşıklığı **model** ve **soyutlama (abstraction)** ile yönetir.

## 2. Test ve hata ayıklama

| Terim | Anlamı |
|---|---|
| **Testing** | Yazılımı, çalışmasını gözlemleyerek değerlendirmek |
| **Test failure** | Yazılım arızasıyla sonuçlanan bir test çalıştırması |
| **Debugging** | Bir failure görüldükten sonra fault'u bulmak |

Her girdi bir fault'un failure üretmesine yol açmaz.

## 3. RIPR modeli

Bir test bir fault'u ancak **dört** şey gerçekleşirse bulur:

1. **Reachability (ulaşma)** — test, kodda fault'un bulunduğu yeri çalıştırır.
2. **Infection (bulaşma)** — bundan sonra programın durumu yanlıştır.
3. **Propagation (yayılma)** — yanlış durum bir çıktıya veya son duruma taşınır.
4. **Revealability (açığa çıkma)** — test eden (veya testin assertion'ı) çıktının o yanlış kısmına bakar.

Hafta 1'deki örnek (`numZero` döngüye 0 yerine 1'den başlıyor):

- `[2, 7, 0]` fault'a ulaşır ve durumu bozar (`i` yanlış), ama yanlış durum **yayılmaz**: çıktı yine doğrudur.
- `[0, 2, 7]` ulaşır, bozar ve yayılır: çıktı 1 değil 0 olur. Test çıktıyı kontrol ediyorsa failure **açığa çıkar**.

Sonucu kontrol etmeyen (oracle'ı olmayan) bir test ulaşabilir, bozabilir, yayabilir — ve yine de hiçbir şey bulamaz.

## 4. Testi kim yapar?

- **Test engineer:** test girdilerini tasarlar, test değerlerini üretir, test betiklerini çalıştırır, sonuçları analiz eder, geliştiricilere ve yöneticilere rapor verir.
- **Test manager:** test mühendislerini yönetir, test politikalarını ve süreçlerini belirler, diğer yöneticilerle görüşür.

## 5. Test seviyeleri

| Seviye | Yanıtladığı soru |
|---|---|
| **Acceptance testing** (kabul testi) | Yazılım kullanıcı için kabul edilebilir mi? |
| **System testing** (sistem testi) | Sistemin tamamı çalışıyor mu? |
| **Integration testing** (entegrasyon testi) | Modüller birlikte çalışıyor mu? |
| **Module testing** (modül testi) | Her sınıf, dosya veya bileşen çalışıyor mu? |
| **Unit testing** (birim testi) | Her birim (metot) tek başına çalışıyor mu? |

Birim ve modül testine geliştirici testi (developer testing) de denir.

## 6. Kapsama ölçütleri (coverage criteria)

Çok küçük bir programın bile çok fazla girdisi vardır. `computeAverage(int A, int B, int C)` metodunda üç tane 32 bitlik tam sayı vardır: 80 oktilyondan fazla olası test. Girdi uzayı pratikte sonsuzdur.

**Coverage criterion** bu uzayı düzenli biçimde aramanın yoludur: az test, az örtüşme, çok sorun.

| Terim | Anlamı | Örnek |
|---|---|---|
| **Test requirement (TR)** | Bir testin karşılaması veya kapsaması gereken belirli bir şey | "12. deyimi çalıştır" |
| **Coverage criterion** | Test gereksinimlerini üreten kural | "Her deyimi kapsa" |

Ölçütler neden yararlıdır:

- harcanan emeğe göre en çok sonuç: daha az testle daha çok fault;
- **traceability (izlenebilirlik)**: her testin bir nedeni vardır;
- regresyon testi kolaylaşır;
- **stopping rule (durma kuralı)**: testin ne zaman bittiğini biliriz;
- iyi araç desteği.

Tüm ölçütler yalnızca **dört yapı** üzerine kuruludur: girdi alanları (input domains), çizgeler (graphs), mantık ifadeleri (logic expressions) ve sözdizimi tanımları (syntax descriptions).

## 7. Black-box, white-box, model-based

- **Black-box testing:** testler dış tanımlardan gelir (gereksinimler, spesifikasyon, tasarım).
- **White-box testing:** testler kaynak koddan gelir (deyimler, dallar, koşullar).
- **Model-based testing:** testler yazılımın bir modelinden gelir, örneğin bir UML diyagramı.

Kutunun renginden daha yararlı soru: **testleri hangi soyutlama seviyesinden türetiyoruz?**

## 8. Dört test etkinliği

| Etkinlik | Ne yapar | Ne gerektirir |
|---|---|---|
| **1. Test design** | Test değerlerini seçer | (a) *criteria-based*: matematik, programlama, test bilgisi. (b) *human-based*: alan ve kullanıcı bilgisi |
| **2. Test automation** | Değerleri çalıştırılabilir betiklere koyar | Programlama |
| **3. Test execution** | Testleri çalıştırır, sonuçları kaydeder | Temel bilgisayar becerisi; dikkatli çalışma |
| **4. Test evaluation** | Sonuçları değerlendirir, geliştiricilere bildirir | Alan bilgisi |

- Ölçüte dayalı tasarım özel durumları göremeyebilir; bu yüzden insana dayalı tasarım da gerekir.
- Bu etkinlikler farklı beceriler ister. Birçok ekip dördü için de aynı kişileri kullanır; bu kaynak israfıdır.
- Diğer etkinlikler: **test management** (politika, ekip, ne kadar otomasyon), **test maintenance** (yazılım değişince testleri yeniden kullanmak için saklamak), **test documentation** (her test *neden* var olduğunu söylemeli: bu izlenebilirliktir).

## 9. Model-driven test design (MDTD)

MDTD, test tasarımcısının daha yüksek bir soyutlama seviyesinde çalışmasını sağlar:

1. **Software artifact** (kod, gereksinim, tasarım) →
2. **Model / structure** (örneğin bir çizge) →
3. **Test requirements** (bir ölçütten) →
4. **Refined test requirements** →
5. **Input values** → **test cases** → **test scripts** → **test results** → **pass / fail**.

1–4. adımlar *tasarım soyutlama seviyesindedir*. 5. adım *uygulama seviyesindedir*.

**Küçük örnek.** `indexOf(Node n)` metodu bir listeyi `for` döngüsüyle arar.

- Model: 5 düğümlü bir kontrol akış çizgesi. Kenarlar: 1→2, 2→3, 3→2, 3→4, 2→5. Başlangıç düğümü 1. Bitiş düğümleri 4 ve 5.
- Ölçüt: *edge-pair coverage* (uzunluğu 2 olan her yol). 6 test gereksinimi verir: [1,2,3], [1,2,5], [2,3,4], [2,3,2], [3,2,3], [3,2,5].
- Üç test yolu altısını da kapsar: [1,2,5], [1,2,3,2,5], [1,2,3,2,3,4].
- Son adım: programın her yolu izlemesini sağlayan girdi değerlerini bulmak.

Tasarımcı matematiği bir kez yapar. Sonra başkaları değer bulabilir, otomatikleştirebilir, çalıştırabilir ve değerlendirebilir.

## Soru fikirleri

- Kusurlu küçük bir kod ve bir test girdisi ver. Hangi RIPR adımının gerçekleşmediğini sor.
- Bir işi anlat (örneğin "verilen değerler için JUnit betiği yazar") ve hangi test etkinliği olduğunu sor.
- Bir ölçüt ve bir yapı ver, kaç test gereksinimi çıktığını sor.
- Bir durumu anlat ve hangi test seviyesi olduğunu sor.
