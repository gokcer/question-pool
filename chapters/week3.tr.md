# Hafta 3 — Test Otomasyonu

**Tek fikir:** otomasyon, iyi bir test fikri ile kimse düşünmeden her gün çalışan bir test arasındaki köprüdür.

Bu bölüm Hafta 3 dersinin 1–3. kısımlarını kapsar: neden otomasyon, test edilebilirlik, bir test case'in parçaları, JUnit, veri güdümlü testler ve iyi bir testin özellikleri. (4. kısım — arayüz testleri, mobil testler ve CI araçları — işlenmedi ve bu bölümde yoktur.) Sınav İngilizce olduğu için terimlerin İngilizcesi korunmuştur.

## 1. Test otomasyonu nedir?

**Regression testing**, bir değişiklikten sonra mevcut testleri yeniden çalıştırıp önceden çalışan bir şeyin bozulmadığını kontrol etmektir. 500 özelliği olan bir uygulamada bir hatayı düzelttin: diğer 499 özelliğin hâlâ çalıştığını nereden biliyorsun? Kimse bunları her gün elle kontrol edemez. Bu yüzden otomasyon yaparız.

**Test automation**, yazılımı şu işler için kullanmaktır:

- testlerin çalıştırılmasını **yönetmek** (control),
- ön koşulları **hazırlamak** (set up),
- gerçekleşen sonucu beklenen sonuçla **karşılaştırmak** (compare),
- sonuçları **raporlamak** (report).

*Girdileri seçmek* bu listede yoktur. O, test tasarımıdır.

**Yararları:** daha az sıkıcı iş, test tasarımına daha çok zaman, testler ek maliyet olmadan binlerce kez çalışır, daha az unutulan adım, testi kim çalıştırırsa çalıştırsın aynı kalite.

**Revenue task ve excise task**

| | Anlamı | Örnekler |
|---|---|---|
| **Revenue task** | Sorunu doğrudan çözer | Test değerlerini seçmek; doğru çıktıya karar vermek |
| **Excise task** | Gereklidir ama kendi başına değer katmaz | Derlemek; testleri yeniden çalıştırmak; çıktıları gözle karşılaştırmak |

Excise işleri otomatikleştir ki insanlar zamanını revenue işlere harcasın.

**Otomasyon ne zaman karşılığını vermez:** test yalnızca bir kez çalışacaksa; betik kazandırdığından pahalıysa; kullanılabilirlik ve "görünüm ve his" (insan gözü gerekir); keşif testi (exploratory testing: amaç tekrar etmek değil öğrenmektir); oracle yoksa (doğru çıktının ne olduğunu kimse söyleyemiyorsa).

## 2. Test edilebilirlik (testability)

**Testability**, bir yazılımdaki fault'ları bulmanın ne kadar kolay olduğunu söyler. İki soruya iner:

| | Soru | Kolay | Zor |
|---|---|---|---|
| **Controllability** | Girdi değerlerini içeri verebiliyor muyum? | Metot parametreleri, klavye | Sensörler, zaman, rastgele sayılar, dağıtık yazılım |
| **Observability** | Çıkanı görebiliyor muyum? | Dönüş değeri | Veritabanı, uzak dosya, e-posta, motor |

**Örnek.** Bu sınıfı test etmek zordur:

```java
public void greet(String name) {
    int hour = LocalTime.now().getHour();          // girdi sistem saatinden geliyor
    if (hour < 12) System.out.println("Good morning, " + name);
    else           System.out.println("Good afternoon, " + name);
}
```

- Düşük controllability: test saati seçemez.
- Düşük observability: metot bir şey döndürmez, yalnızca ekrana yazar.

Test edilebilir sürüm, kurucusunda bir `Clock` nesnesi alır ve bir `String` **döndürür**. Artık test saati seçebilir (09:00 veya 12:00) ve sonucu `assertEquals` ile kontrol edebilir. Davranış aynıdır; yalnızca girdilerin ve çıktıların yeri değişti.

**Test edilebilir tasarım için beş alışkanlık**

| Alışkanlık | Neyi iyileştirir |
|---|---|
| Bağımlılıkları dışarıdan ver (saat, rastgele, veritabanı) | Controllability |
| Sonucu yazdırma, döndür | Observability |
| Tek iş yapan küçük metotlar | İkisi de |
| Arayüzlere (interface) bağımlı ol (gerçeğin yerine sahte konabilsin) | İkisi de |
| Gizli global durumdan kaçın (static sayaç, singleton) | Controllability |

**Information hiding** (private alanlar) iyi tasarımdır ama durumu testlerden de gizler. Public metotlar üzerinden test et. Alanları yalnızca test için public yapma.

## 3. Bir test case'in dört parçası

| Parça | Nedir | RIPR harfi |
|---|---|---|
| **Prefix values** | Yazılımı doğru duruma getiren girdiler | **R**each |
| **Test case values** | Gerçekten test etmek istediğimiz girdiler | **I**nfect |
| **Postfix values** | *Verification* değerleri sonucu görünür kılar; *exit* değerleri yazılımı kararlı duruma döndürür | **P**ropagate (verification kısmı) |
| **Expected results** | Doğru yazılımın üreteceği sonuç | **R**eveal |

Telefon örneği: telefonu aç (prefix) → numarayı çevir (test case values) → "ara"ya, sonra "bitir"e bas (postfix) → arama bağlanır (expected result).

Sektörde aynı fikre **Arrange – Act – Assert** denir: Arrange = prefix, Act = test case values, Assert = expected results. Teardown = exit values.

- **Test case:** bir çalıştırma için dört parçanın tamamı.
- **Test set** (test suite): bir grup test case. Coverage tek bir testin değil, bir test setinin özelliğidir.
- **Executable test script:** makinenin otomatik çalıştırabileceği biçimdeki test case.

## 4. Test çatıları ve JUnit

**Test framework**, test otomasyonunu destekleyen fikirler ve araçlar kümesidir. **Test driver**, yazılımı her testle çalıştırır, sonucu karşılaştırır ve raporlar. JUnit (Java) ve pytest (Python) xUnit ailesindendir. Sistem testi için değil, birim ve entegrasyon testi için yapılmışlardır.

Bir framework şunları verir: **assertion**'lar (beklenen sonuçlar), **fixture**'lar (prefix ve postfix: `@BeforeEach`, `@AfterEach`), **test setleri** (sınıflar, suite'ler), **çalıştırıcılar ve raporlar**.

Neden `main()` ve `println` ile test etmeyelim? Her satırı bir insanın okuması gerekir; pass/fail sayısı yoktur; ilk exception'da durur; kimse yeniden çalıştırmayı hatırlamaz.

```java
class CalcTest {
    @Test
    void addTwoPositiveNumbers() {
        assertEquals(5, Calc.add(2, 3), "Calc sum incorrect");
    }
}
```

- `@Test` bir test metodunu işaretler. `2, 3` test case değerleridir. `5` beklenen sonuçtur.
- Sıra `assertEquals(expected, actual, message)` şeklindedir. Mesaj yalnızca test başarısız olunca yazdırılır.
- **Yaşam döngüsü:** `@BeforeAll` bir kez → her test için: yeni nesne, `@BeforeEach`, `@Test` metodu, `@AfterEach` → `@AfterAll` bir kez. Her test yeni bir nesne alır; bir test diğerinin verisini değiştiremez.
- `assertEquals(5, x)`, `assertTrue(x == 5)`'ten iyidir: başarısız olunca yalnızca *false* değil, *expected 5 but was 6* der.
- Raporda **failure**, bir assertion'ın tutmadığı anlamına gelir. **Error**, beklenmeyen bir exception demektir.
- JUnit 5'te exception test etmek için `assertThrows(SomeException.class, () -> ...)` kullan. Tam olarak tek bir satırı kontrol eder.

**Min örneği.** `min(list)` en küçük elemanı döndürür. Javadoc'tan **yedi** test çıkar: beş istisnai durum (null liste; başka elemanlarla birlikte null eleman; tek bir null eleman; karşılaştırılamayan elemanlar; boş liste) ve yalnızca iki normal durum (tek eleman; en küçüğün ilk sırada olmadığı iki eleman). Testleri gözden geçirirken önce istisnai durumlara bak.

## 5. Tek girdili tek testin ötesi

**Data-driven tests (veri güdümlü testler).** Aynı testi farklı sayılarla 20 kez kopyalama. Testi bir kez yaz, veriyi tabloya koy: JUnit 5'te `@ParameterizedTest` ve `@CsvSource`, pytest'te `@pytest.mark.parametrize`. Rapor her satır için bir sonuç gösterir.

**Theories ve property-based testing.** Veri güdümlü test her girdiyi çıktısıyla birlikte listeler. **Theory** ise tüm girdiler için bir kural söyler: *P ön koşulu sağlanıyorsa, eylemden sonra Q son koşulu sağlanır.* Adımlar **Assume – Act – Assert**'tür. Örnek: "bir küme x'i içeriyorsa, x'i çıkarıp yeniden eklemek aynı kümeyi verir." Property-based araçlar (jqwik, Hypothesis) yüzlerce girdi üretir ve başarısız olan girdiyi en küçük örneğe indirger.

**Testleri çalıştırmak:** IDE'de, komut satırından (`mvn test`, `pytest`) veya continuous integration'da (her push'ta tüm testler çalışır).

## 6. İyi testler FIRST'tür

| | Anlamı |
|---|---|
| **F**ast | Milisaniyelerde çalışır, bu yüzden sık çalıştırılır |
| **I**ndependent | Başka bir teste veya sıraya bağlı değildir |
| **R**epeatable | Her seferinde, her makinede aynı sonucu verir |
| **S**elf-validating | Pass veya fail der; çıktıyı bir insanın okuması gerekmez |
| **T**imely | Aylar sonra değil, kodla birlikte yazılır |

**Beş test kokusu (test smell)**

| Koku | Neden zararlı | Çözüm |
|---|---|---|
| Assertion yok | Test yalnızca çökerek başarısız olabilir | Her zaman beklenen bir sonucu kontrol et |
| Testler arasında paylaşılan durum | Sonuç çalıştırma sırasına bağlı olur | `@BeforeEach` içinde taze fixture |
| `assertTrue(a == b)` | Hata mesajı yalnızca "false" der | `assertEquals(expected, actual)` |
| Tek test on şeyi kontrol ediyor | İlk hata diğerlerini gizler | Her teste tek davranış |
| Flaky test (zaman, rastgelelik, ağ) | Kod değişmeden kırmızı ve yeşil | Saati, seed'i veya bir sahteyi dışarıdan ver |

Bir test geçebilir ama yine de zayıf olabilir. Örnek: bir test listeye "Ada", "Bora", "Cem" ekler (zaten sıralı), listeyi sıralar ve yalnızca ilk elemanı kontrol eder. RIPR diliyle: `sort` bozuk olsa bile durum bozulmaz ya da yanlış durum açığa çıkmaz.

## Soru fikirleri

- Küçük bir metot göster; controllability'yi veya observability'yi neyin düşürdüğünü ya da hangi alışkanlığın bunu düzelteceğini sor.
- Bir testin adımlarını anlat; hangisinin prefix, test case value, postfix, expected result olduğunu sor.
- Kısa bir JUnit testi göster; hangi FIRST harfini bozduğunu veya hangi test kokusunu taşıdığını sor.
- Bir iş ver ve sor: revenue mu excise mı? Otomatikleştirmeye değer mi?
