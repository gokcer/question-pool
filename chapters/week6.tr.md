# Hafta 6 — Statik Test

**Tek fikir:** bazı kusurları bulmanın en ucuz yolu, kodu çalıştırmadan yalnızca okumaktır.

Bu bölümün iki kısmı var: yedi test prensibi ve test sürecinin toparlaması (ISTQB CTFL 1.3–1.4) ve statik test (Myers Bölüm 3, CTFL Bölüm 3). Sınav İngilizce olduğu için terimlerin İngilizcesi korunmuştur.

## 1. Hikâye: Heartbleed (2014)

OpenSSL, web'in büyük bir kısmında parolaları korur. İçinde bir "heartbeat" mesajı vardır: *"Hâlâ buradayım. Bana şu N harfi geri gönder."* Sunucu N sayısına güvendi. Mesajda gerçekten N harf olup olmadığını hiç kontrol etmedi.

- Dürüst kullanıcı: *"HAT" de (3 harf)* → sunucu `HAT` yanıtını verir.
- Saldırgan: *"HAT" de (500 harf)* → sunucu `HAT` ve belleğinden 497 harf daha gönderir: parolalar, özel anahtarlar, başka kullanıcıların verileri.

Saldırgan her istekte 64 KB'a kadar bellek okuyabiliyordu. Kusur kodda yaklaşık iki yıl kaldı. Düzeltme tek bir kontroldü: iddia edilen uzunluk gerçek veriden uzunsa mesajı at.

**Kim buldu?** Neel Mehta (Google) **kodu okuyarak** buldu (kod gözden geçirmesi). Codenomicon şirketi aynı dönemde bir **fuzzing aracıyla** buldu (kodu tuhaf girdilerle çalıştırarak).

**Mevcut testler iki yıl boyunca neden kaçırdı?** Her test dürüst bir uzunluk gönderiyordu. Kimse yalan söyleyen bir uzunluğu test etmedi.

Tek kusur, iki bulma yolu: okumak (statik) ve çalıştırmak (dinamik).

## 2. Yedi test prensibi (CTFL 1.3)

| # | Prensip | Basitçe |
|---|---|---|
| 1 | Testing shows the presence of defects, not their absence | Geçen testler kusur olmadığını kanıtlamaz |
| 2 | Exhaustive testing is impossible | Her girdiyi ve her yolu deneyemeyiz |
| 3 | Early testing saves time and money | Erken bulunan kusuru düzeltmek ucuzdur |
| 4 | Defects cluster together | Kusurların çoğu birkaç modüldedir |
| 5 | Tests wear out (pesticide paradox) | Aynı testler yeni kusur bulmayı bırakır |
| 6 | Testing is context dependent | Bir oyun, kalp pili gibi test edilmez |
| 7 | Absence-of-defects fallacy | Hata olmaması, sistemin yararlı olduğu anlamına gelmez |

Örnekler:

- "Regresyon paketimiz altı aydır sıfır hata buldu ama kullanıcılar her hafta yeni hata bildiriyor." → prensip **5**. Testleri değiştir, yenilerini ekle.
- "Uygulama 2.000 testin hepsini geçti ama kimse kullanmak istemiyor, çünkü yanlış sorunu çözüyor." → prensip **7**. (Validation: doğru ürünü yap.)
- "Bir start-up oyununu bir gün test ediyor; bir banka ödeme modülünü bir ay test ediyor. İkisi de haklı." → prensip **6**. Ne kadar testin yeterli olduğuna risk karar verir.

## 3. Test süreci (CTFL 1.4)

Yedi etkinlik grubu vardır. Her biri **testware** (iş ürünleri) üretir:

| Etkinlik | Soru | Testware |
|---|---|---|
| **Planning** | Neden ve ne kadar? | Test plan |
| **Monitoring and control** | Plana uyuyor muyuz? (sürekli) | İlerleme raporları |
| **Analysis** | *Neyi* test edeceğiz? | Test conditions |
| **Design** | *Nasıl* test edeceğiz? | Test cases |
| **Implementation** | Çalıştırmaya hazır mı? | Test scripts, test data |
| **Execution** | Çalıştır ve karşılaştır | Loglar, defect reports |
| **Completion** | Ne öğrendik? | Summary report |

Etkinlikler üst üste binebilir ve tekrarlanabilir. Çevik geliştirmede her sprintte yapılırlar.

**Traceability (izlenebilirlik)** bağlantıları korumaktır: requirement → test condition → test case → result → defect. Örnek: *REQ-12 "geçerli kupon %10 indirim verir"* → *TC-31 "100 TL'de JUICE123 → 90 TL"* → *TC-31 başarısız* → *BUG-7*.

Bağlantıları neden tutarız? **Coverage** (her gereksinim test edildi mi?), **impact** (bir gereksinim değişti: hangi testler değişmeli?), **reporting** (hangi gereksinimler risk altında?).

**İki rol:** **test management role** testi planlar, izler ve kontrol eder. **Testing role** testleri analiz eder, tasarlar, hazırlar ve çalıştırır.

## 4. Statik ve dinamik test (CTFL 3.1)

| Static testing: oku, çalıştırma | Dynamic testing: çalıştır ve gözle |
|---|---|
| Doğrudan **defect** bulur (nedeni) | **Failure** bulur (belirtiyi); sonra debugging defect'i bulur |
| Her iş ürününde çalışır: hikâye, tasarım, kod, test | Çalışan yazılım gerekir |
| Hiç kod yokken başlayabilir | Yaşam döngüsünde daha geç gelir |
| Bulur: belirsiz gereksinim, ulaşılamayan kod, kullanılmayan değişken, güvenlik açığı, kodlama standardına aykırılık | Bulur: yanlış çıktı, çökme, yavaş yanıt, bellek sızıntısı, yük altındaki davranış |

Rakip değil ortaktırlar. Her biri diğerinin kaçırdığı kusurları bulur.

Statik testi **insanlar (reviews)** ve **araçlar (static analysis)** yapar.

**Ne gözden geçirilebilir?** Bir insanın okuyup anlayabileceği her şey: gereksinimler, user story'ler, kabul ölçütleri, tasarımlar, kaynak kod, test planları ve test case'ler, sözleşmeler, kullanıcı kılavuzları, web sayfaları. Araçlar ise biçimsel bir yapı ister (kod, model).

**Zamana neden değer?**

- Inspection ve walkthrough'lar tipik programlarda mantık tasarımı ve kodlama hatalarının %30–70'ini bulur (Myers).
- Gözden geçirmede bulunan bir kusur, aynı kusurun kullanıcı tarafından bulunmasından çok daha ucuzdur.
- **Shift left:** gereksinim ve tasarım kusurları kodlama başlamadan bulunur.
- Gözden geçirenler kodu öğrenir; yazar daha iyi bir üslup öğrenir.

## 5. Review süreci (CTFL 3.2)

| Adım | Ne olur |
|---|---|
| 1. **Planning** | Kapsam, amaç, kontrol listesi, kimler, ne kadar süre, çıkış ölçütleri |
| 2. **Review initiation** | Herkes malzemeyi alır; amacı ve rolünü bilir |
| 3. **Individual review** | Her reviewer tek başına okur, kontrol listesini kullanır, anomalileri not eder |
| 4. **Communication and analysis** | Toplantı: anomaliler tartışılır, hangisinin defect olduğuna ve sonraki adıma karar verilir |
| 5. **Fixing and reporting** | Defect raporları; yazar düzeltir; düzeltmeler kontrol edilir; metrikler raporlanır |

Kusurların çoğu **3. adımda** bulunur. Toplantı uzlaşmak içindir, malzemeyi ilk kez okumak için değil.

**Roller**

| Rol | İşi |
|---|---|
| Manager | Neyin gözden geçirileceğine karar verir; zaman ve insan sağlar |
| Author | İş ürününü üretti; kusurları düzeltir |
| Moderator (facilitator) | Toplantıyı yönetir; odaklı ve nazik kalmasını sağlar |
| Scribe (recorder) | Her anomaliyi ve kararı yazar |
| Reviewer | Anomali bulur; test eden, geliştirici, kullanıcı veya uzman olabilir |
| Review leader | Genel sorumluluk: kim, nerede, ne zaman |

Bir kişinin birden çok rolü olabilir. Ama inspection'da author asla moderator veya scribe olmaz.

**Dört review türü** (az resmîden çok resmîye):

| | Informal review | Walkthrough | Technical review | Inspection |
|---|---|---|---|---|
| Kim yönetir | belirli biri yok | **author** | bir moderator | eğitimli bir moderator |
| Süreç | yok | hafif | tanımlı | 5 adımlı tam süreç |
| Ana amaç | hızlı geri bildirim | öğrenmek, uzlaşmak, anomali bulmak | uzlaşmak, karar vermek, anomali bulmak | olabildiğince çok anomali bulmak |
| Çıktı | belki hiçbir şey | notlar | belgelenmiş kararlar | defect log ve metrikler |
| Örnek | "PR'ıma bakar mısın?" | yazar ekibe bir tasarımı gösterir | mimarlar bir API tasarımını inceler | güvenlik açısından kritik kod, satır satır |

Daha resmî olan daha pahalıdır ve daha çok kusur bulur.

**Bir review'ı ne başarılı kılar?** Açık amaç ve çıkış ölçütleri; küçük parçalar (bir seferde 2.000 satır değil); hazırlık zamanı; ekibin gerçekten yaptığı hatalara dayanan kontrol listeleri; üreteni değil ürünü incelemek; eğitimli insanlar ve yönetici desteği.

## 6. Myers'a göre inspection

- **4 kişilik** ekip: moderator, programcı, tasarımcı ve bir test uzmanı.
- Oturum **90–120 dakika** sürer; sonrasında dikkat düşer.
- Hız: saatte yaklaşık **150 deyim**.
- Programcı kodu satır satır yüksek sesle okur ve mantığı açıklar. Programcı çoğu zaman yalnızca açıklarken hata bulur.
- Ekip kodu yaygın hataların **kontrol listesiyle** karşılaştırır.
- Ekip hataları **bulur**; toplantıda düzeltmez. Programcı sonra düzeltir.
- Egonu kapıda bırak. Sonuçlar gizlidir ve programcıya not vermek için asla kullanılmaz.

**Myers'ın hata kontrol listesi: sekiz kategori**

| Kategori | Örnek soru |
|---|---|
| Data reference | Her değişken kullanılmadan önce atanmış mı? İndis dizinin içinde mi? |
| Data declaration | Türler doğru mu? Varsayılan değerler doğru mu? |
| Computation | Tam sayı bölmesi? Yuvarlama? Taşma (overflow)? |
| Comparison | `<` mi `<=` mi? `and` / `or` doğru mu? |
| Control flow | Her döngü bitiyor mu? Bir eksik / bir fazla (off by one)? |
| Interface | Parametrelerin sayısı, sırası ve birimi aynı mı? |
| Input / output | Kötü girdi ele alınıyor mu? Hatalar gizlenmeden bildiriliyor mu? |
| Other | Kullanılmayan değişken? Derleyici uyarıları okundu mu? |

## 7. İki statik test örneği

**Bir user story'yi gözden geçirmek.** *"As a customer, I want to use a coupon, so that I pay less."* Kabul ölçütleri: (1) a valid coupon gives a discount; (2) the page should respond fast; (3) a coupon can be used many times; (4) an expired coupon shows an error message.

Anomaliler: Bir kuponu "valid" yapan nedir? "A discount" ne kadar? "Fast" test edilemez (daha iyisi: "isteklerin %95'inde 2 saniyenin altında yanıt verir"). "Many times" — kaç kez? Dolandırıcılık riski gibi duruyor. Hangi hata mesajı? Eksik durumlar: yanlış biçimli kupon, iki kupon birlikte? Henüz hiç kod yok ve şimdiden birkaç kusur bulduk.

**Kod okumak.** Bir giriş (login) fonksiyonu, SQL sorgusunu kullanıcının e-posta metnini doğrudan SQL dizgisinin içine koyarak kuruyor. Bu **SQL injection**'dır. E-posta `' OR 1=1--` olursa koşul her zaman doğru olur ve sorgunun geri kalanı yoruma dönüşür; saldırgan parolayı bilmeden giriş yapar. Çözüm **parametre** kullanmaktır: değerler SQL metninden ayrı gönderilir. Kontrol listesi kategorisi: input / output (girdi kontrol edilmiyor).

## 8. Statik analiz araçları

Araçlar, program okuyan programlardır.

| Tür | Örnekler |
|---|---|
| Derleyici uyarıları | `javac -Xlint`, `gcc -Wall` |
| Linter'lar (üslup ve basit hatalar) | Ruff (Python), ESLint (JavaScript), Checkstyle (Java) |
| Hata bulucular | PMD, SpotBugs, Error Prone |
| Güvenlik tarayıcıları | Semgrep, CodeQL, Bandit |
| Kalite platformları | SonarQube / SonarLint |

Sen yazarken editörde ve her push'ta CI'da çalışırlar. Ne kadar erken, o kadar ucuz.

**Bir araç ve bir insan aynı kodu okuyor:**

```python
def average(nums=[]):
    total = 0
    for n in nums:
        total += n
    return total / len(nums)
```

- Araç (Ruff) B006 uyarısını verir: varsayılan değer olarak verilen liste çağrılar arasında paylaşılır.
- İnsan şunu da sorar: liste boşsa ne olur? → `ZeroDivisionError`. Araç bu konuda bir şey söylemedi.

| İnsanlar (reviews) | Araçlar (static analysis) |
|---|---|
| Spesifikasyonu ve kullanıcıyı bilir | Kodun ne yapması gerektiğini bilmez |
| Eksik özelliği, yanlış mantığı, belirsiz gereksinimi bulur | Kalıpları bulur: kullanılmayan değişken, string'lerde `==`, boş `catch`, injection |
| Yavaştır (saatte yaklaşık 150 satır), yorulur | Hızlıdır, tutarlıdır, yorulmaz; her commit'te çalışır |
| | **False positive** verir (gerçek kusur olmayan uyarılar) |

İyi uygulama: basit şeyleri önce araç temizlesin; insanlar zamanını mantığa harcasın.

## 9. LLM çağında gözden geçirme

- Yapay zekâ, insanların satır satır inceleyebileceğinden çok daha hızlı kod yazıyor; her modül için 4 kişilik toplantı artık gerçekçi değil.
- Kalanlar: kontrol listeleri (artık linter ve LLM kuralları olarak da), "bul, düzeltme", "üreteni değil ürünü incele".
- Üç katman: **araçlar** (ucuz, her zaman aynı yanıt, her commit'te) → **LLM reviewer** (ilk okuma; spesifikasyonu da okuyabilir ama her zaman aynı yanıtı vermez) → **insan** (karar verir, sonucun sahibidir, riskli kodu inceler).
- **Automation bias:** "yapay zekâ öyle dedi" bir gözden geçirme değildir.

**Real world 2026.** Anthropic, Mayıs 2026'da Claude Mythos Preview modelinin (Project Glasswing) 10.000'den fazla yüksek veya kritik güvenlik açığı bulduğunu bildirdi; bunlardan biri OpenBSD'de 27 yıllık bir hataydı. Geliştiricilere bildirilen 530 hatadan 75'i rapor tarihinde düzeltilmişti. Bu sayılar şirketin kendi bildirimidir ve henüz başkalarınca doğrulanmamıştır. Ders: yapay zekâ çok güçlü bir hata bulucudur; ama her hatayı doğrulamak, derecelendirmek ve düzeltmek yine insanların işidir ve "doğru"nun ne olduğuna insanlar karar verir.

## Soru fikirleri

- Kısa bir hikâye anlat ve yedi prensipten hangisini gösterdiğini sor.
- Bir etkinliği veya iş ürününü anlat; test sürecinin hangi etkinliğine ait olduğunu sor.
- Bir review toplantısını anlat; hangi review türü olduğunu veya bir kişinin hangi rolde olduğunu sor.
- Bir kusur ver ve sor: statik test mi bulur, dinamik test mi, ikisi de mi? Araç mı, yalnızca insan mı?
- Küçük bir kod veya user story göster; hangi kontrol listesi kategorisinin veya hangi anomalinin geçerli olduğunu sor.
