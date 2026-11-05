# Hafta 4 — Geleneksel ve Çevik Geliştirme, Çevik Test

**Tek fikir:** yazılımı nasıl geliştirdiğimiz, onu nasıl test edeceğimizi belirler. Çevik geliştirmede otomatik testler spesifikasyonun yerini alır.

Sınav İngilizce olduğu için terimlerin İngilizcesi korunmuştur.

## 1. Geleneksel yol

### Waterfall (şelale)

Aşamalar birbirini izler: **Requirements → Design → Implementation → Testing → Deploy and maintain**.

- Her aşama imzalanmış bir belgeyle biter.
- Önceki bir aşamaya dönmek pahalıdır ("su yukarı akmaz").
- Test ancak kod "bittiğinde" başlar.

Gereksinimler gerçekten değişmiyorsa waterfall hâlâ uygundur: güvenlik açısından kritik sistemler (havacılık, tıbbi cihaz, demiryolu), tam izlenebilirlik isteyen düzenlemeye tabi işler ve küçük, sabit, iyi anlaşılmış problemler.

### V-Model

V-Model her tasarım seviyesini bir test seviyesiyle eşler:

| Tasarım seviyesi (sol kol) | Test seviyesi (sağ kol) |
|---|---|
| Requirements | Acceptance testing |
| System design | System testing |
| Architecture design | Integration testing |
| Module design | Unit testing |

Her tasarım seviyesi, eşinin test planını yazar. Yani testler **erken planlanır** ama yine de **geç çalıştırılır**: kodlamadan sonra.

### Sorun: geç geri bildirim

- Müşteri ürünü ancak sonda görür (örneğin 12 ay sonra).
- **Requirements drift:** bu sürede pazar ve müşteri değişir.
- **Geç sürprizler:** yanlış anlamalar, düzeltmenin en pahalı olduğu anda ortaya çıkar.
- **Test sıkışır:** kodlama gecikince kısalan aşama test olur.

Kök sorun: *insanlar yaklaşık yapmakta iyi, mükemmelleştirmekte kötüdür.* Müşteri ne istediğini ürünü görünce öğrenir.

## 2. Çevik yol

### Agile Manifesto (2001)

| Daha çok değer veririz | yerine |
|---|---|
| Individuals and interactions (bireyler ve etkileşimler) | processes and tools (süreçler ve araçlar) |
| Working software (çalışan yazılım) | comprehensive documentation (kapsamlı dokümantasyon) |
| Customer collaboration (müşteriyle iş birliği) | contract negotiation (sözleşme pazarlığı) |
| Responding to change (değişime yanıt vermek) | following a plan (bir plana uymak) |

Sağdakilerin de değeri vardır. Soldakiler daha önemlidir.

**Artımlı teslim (incremental delivery):** müşteriye her yinelemede kullanılabilir bir şey ver (kaykay → scooter → bisiklet → motosiklet → araba); önce tekerlek, sonra aks, sonra kaporta verip kullanılabilir arabayı en sona bırakma.

### Scrum: işi nasıl düzenleriz

- **3 rol:** *Product Owner* (neyin hangi sırayla yapılacağına karar verir), *Scrum Master* (ekibe koçluk eder, engelleri kaldırır), *Developers* (nasıl yapılacağına karar verir ve yapar).
- **5 etkinlik:** *Sprint* (1–4 hafta, sabit uzunluk), *Sprint Planning*, *Daily Scrum* (15 dakika), *Sprint Review* (paydaşlara demo), *Retrospective* (ekibi iyileştirme).
- **3 çıktı (artifact):** *Product Backlog* (sıralı istek listesi), *Sprint Backlog* (bu sprintin planı), *Increment* (Definition of Done'ı karşılayan kullanılabilir yazılım).

### User story (kullanıcı hikâyesi)

User story, kullanıcının sözleriyle yazılmış bir ihtiyaçtır:

> As a **support technician**, I want **to see a customer's history on demand**, so that **I can solve their problem faster**.

Kalıp: *As a \<role\>, I want \<goal\>, so that \<benefit\>.*

**Acceptance criteria (kabul ölçütleri)** geleceğin testleridir; çoğu zaman *Given … when … then …* biçiminde yazılır:

- Given an existing customer, when the technician takes the call, then the history is shown.
- Given a new customer, when the technician takes the call, then "no history" is shown.

İyi bir hikâye **INVEST**'tir: Independent (bağımsız), Negotiable (pazarlık edilebilir), Valuable (değerli), Estimable (tahmin edilebilir), Small (küçük), Testable (test edilebilir).

### XP (Extreme Programming): iyi kodu nasıl yazarız

XP pratikleri: **test-first (TDD)**, **continuous integration**, **refactoring**, **customer tests** (müşteriyle birlikte yazılan kabul testleri), pair programming, küçük sürümler, basit tasarım (YAGNI: "ihtiyacın olmayacak"), yerinde müşteri.

**TDD döngüsü: Red → Green → Refactor**

1. **Red:** başarısız olan küçük bir test yaz.
2. **Green:** testi geçirecek kadar kod yaz.
3. **Refactor:** kodu temizle; testler yeşil kalır.

Bir döngü dakikalar sürer. Her test pakette kalır; böylece testler hem spesifikasyon hem güvenlik ağı olur.

### Scrum ve XP birlikte

| | Scrum | XP |
|---|---|---|
| Ana soru | İşi nasıl düzenleriz? | İyi kodu nasıl yazarız? |
| Yineleme | Sprint, 1–4 hafta | Iteration, 1–2 hafta |
| Test | Belirtilmez; ekip karar verir | TDD, CI ve customer tests zorunludur |

Çoğu ekip süreç için Scrum'ı, mühendislik için XP'yi kullanır.

## 3. Geleneksel ve çevik karşılaştırması

| | Geleneksel | Çevik |
|---|---|---|
| Gereksinimler | Başta sabitlenir, tek büyük belge | Backlog'daki user story'ler, zamanla gelişir |
| Planlama | Tüm proje, başta | Her sprintte yeniden |
| Teslim | Sonda tek büyük sürüm | Küçük çalışan artımlar |
| Müşteri | Başta ve sonda | Her sprint review'da, çoğu zaman her gün |
| Dokümantasyon | Kapsamlı, imzalı | Yeterli kadar; kod ve testler |
| Değişiklik | Pahalı, istenmez | Beklenir, hoş karşılanır |

**Test nasıl değişir**

| Geleneksel test | Çevik test |
|---|---|
| Sona yakın bir aşama | Her gün, ilk günden |
| Ayrı bir QA ekibi | Kaliteden tüm ekip sorumlu |
| Testler gereksinim belgelerinden gelir | Testler user story'lerden gelir |
| "Doğru" = spesifikasyona uyuyor | "Doğru" = otomatik testleri geçiyor |
| Çoğunlukla elle; regresyon pahalı | Otomatik paket her değişiklikte çalışır (CI) |

**Bağlama göre seç.** Sor: *Değişiklik ne kadar olası?* ve *Sahadaki bir hata ne kadar pahalı?* Kararlı ve yüksek riskli (kalp pili yazılımı, uçuş kontrolü) → waterfall / V-Model. Belirsiz ve hızlı değişen (start-up ürünü, e-ticaret sitesi) → agile. Birçok şirket karma (hybrid) kullanır.

## 4. Agile testing (Ammann & Offutt, Bölüm 4)

**Cost-of-change eğrisi.** Geleneksel düşüncede bir değişiklik ne kadar geç gelirse o kadar pahalıdır; bu yüzden önce çok analiz ve modelleme yapılır. Bu yalnızca gereksinimler eksiksizse ve güncel kalıyorsa doğrudur. Birçok projede öyle değildir. Çevik yöntemler yazılımın küçük başlayıp **evrildiğini** kabul eder.

**Farklı bir doğruluk anlayışı.**

- Geleneksel: *tüm* doğru davranışı baştan eksiksiz tanımla (universal).
- Çevik: doğruluk **belirli bir test kümesine göredir**. Yazılım testleri geçiyorsa "doğru"dur (existential). Tüm davranışları tanımlamayız; bazılarını gösteririz.

**Test harness.** Test harness tüm otomatik testleri verimli biçimde çalıştırır ve sonuçları geliştiricilere bildirir.

- Testler **otomatik** olmalıdır. Test otomasyonu TDD'nin ön koşuludur (TDD ile test otomasyonu aynı şey değildir).
- Her testin, geçip geçmediğine karar veren bir **test oracle**'ı olmalıdır.
- Testler gereksinimlerin yerini alır; bu yüzden kaliteli ve hızlı olmalıdırlar: her değişiklikten sonra çalıştırılırlar.

**Continuous integration (CI).** Biri bir değişiklik gönderdiğinde CI sunucusu sistemi yeniden derler ve testleri yeniden çalıştırır. Hatalar daha erken bulunur, diğer geliştiriciler değişiklikleri erken görür, entegre edilmemiş kodun riski azalır. Değişen sistemin hâlâ doğru olup olmadığına CI sunucusu karar verir.

**User story'den testlere.** Bir user story ile başla → bir **acceptance test** yaz (başarısız olur) → kodu değiştirip refactor ederek TDD testlerini tek tek ekle → acceptance test geçene kadar devam et.

**Legacy (eski) yazılım.** Yazılımların çoğu eskidir ve testi yoktur. Baştan tam bir test seti yazmak çok pahalıdır. Çözüm: **incremental TDD** — bir değişiklik yaparken yalnızca o değişiklik için test ekle. Test koleksiyonu zamanla büyür.

**Testing shortfall (test açığı).** TDD testleri yazılımı iyi test eder mi? **Hayır.** Çevik testlerin çoğu **happy path**'i kontrol eder: normal kullanımda ne olduğunu. Kafası karışmış kullanıcı, yaratıcı kullanıcı ve kötü niyetli kullanıcı yollarını çoğu zaman kaçırır.

**Test edenler ne yapmalı?**

1. *Human-based:* mutlu olmayan yollar için ek user story'ler yaz. Ama ne zaman bittiğini bilmek zordur.
2. *Criteria-based:* girdi alanını veya davranışı modelle (çizge, mantık) ve testleri modelden tasarla. Bu, işin bittiğini kendiliğinden gösterir.

## Soru fikirleri

- Bir projeyi anlat ve hangi sürecin neden daha uygun olduğunu sor.
- Bir Scrum durumu ver; hangi rol, etkinlik veya çıktı olduğunu sor.
- Bir user story göster; nesinin yanlış olduğunu (hangi INVEST harfi) veya hangi kabul ölçütünün test edilebilir olduğunu sor.
- Bir iş adımını anlat ve hangi TDD rengi olduğunu sor.
- Çevikte "doğru"nun ne demek olduğunu ya da çevik testlerin neden tek başına yetmediğini sor.
