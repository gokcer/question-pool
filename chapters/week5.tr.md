# Hafta 5 — Test Ölçütleri

**Tek fikir:** kapsama ölçütü (coverage criterion), "iyi test et" sözünü kapsanacak şeylerin listesine çevirir; böylece ne kadar ilerlediğimizi sayabilir ve ne zaman duracağımızı bilebiliriz.

Sınav İngilizce olduğu için terimlerin İngilizcesi korunmuştur.

## 1. İki tanım

| Terim | Anlamı |
|---|---|
| **Test requirement (TR)** | Bir yazılım ürününün (artifact), bir test case'in karşılaması veya kapsaması gereken belirli bir öğesi |
| **Coverage criterion (C)** | Bir test kümesine test gereksinimleri yükleyen kural veya kurallar bütünü |

**Criterion + artifact = bir TR kümesi.**

- *Branch coverage:* her karar 2 TR verir (karar doğru; karar yanlış).
- *Call coverage:* her metot 1 TR verir (metot en az bir kez çağrılır).
- TR, artifact'a bağlıdır: "42. deyimi çalıştır" ancak 42. deyim varsa anlamlıdır.

Test edenin işi tek cümleyle: **yazılımın bir modelini tanımla, sonra onu kapsamanın yollarını bul.**

## 2. Jelly bean örneği

Bir torbada altı tatta ve dört renkte jelly bean (jöle şeker) var:

| Flavor (tat) | Color (renk) |
|---|---|
| Lemon | Yellow |
| Pistachio | Green |
| Cantaloupe | Orange |
| Pear | White |
| Tangerine | Orange |
| Apricot | Yellow |

| Jelly bean dünyası | Yazılım dünyası |
|---|---|
| Bir şekeri tatmak | Bir test case çalıştırmak |
| Tattığımız şekerler | Test kümesi T |
| "Her tattan bir şeker tat" | Bir coverage criterion |
| Lemon, Pistachio, … | Test gereksinimleri |
| Bu sarı şeker Lemon mı Apricot mu? | Bir controllability sorunu |

- **Flavor criterion:** TR = {Lemon, Pistachio, Cantaloupe, Pear, Tangerine, Apricot} → 6 TR.
- **Color criterion:** TR = {Yellow, Green, Orange, White} → 4 TR.

## 3. Coverage ve coverage level

**Coverage:** TR'deki **her** test gereksinimi için T'de onu karşılayan en az bir test varsa, T test kümesi C ölçütünü sağlar.

**Coverage level** = (T'nin karşıladığı TR sayısı) ÷ (TR'deki TR sayısı).

| Test kümesi | Flavor criterion (6 TR) | Color criterion (4 TR) |
|---|---|---|
| T1 = 3 Lemon, 1 Pistachio, 2 Cantaloupe, 1 Pear, 1 Tangerine, 4 Apricot | 6/6 = %100 | 4/4 = %100 |
| T2 = 1 Lemon, 2 Pistachio, 1 Pear, 3 Tangerine | 4/6 ≈ %67 (Cantaloupe yok, Apricot yok) | 4/4 = %100 |

- T2 her rengi kapsar ama her tadı kapsamaz.
- Daha çok test daha çok kapsama demek değildir: T1'de 12 şeker var, ama iyi seçilmiş 6 şeker aynı tat kapsamasını verir.

**Minimal ve minimum**

- **Minimal** test kümesi: herhangi bir testi çıkarırsan kapsama bozulur. (T1'den 2 Lemon, 1 Cantaloupe ve 3 Apricot çıkar → 6 şeker, minimal.)
- **Minimum** test kümesi: tüm TR'leri karşılayan daha küçük bir küme yoktur.
- Bir kümenin minimal olduğunu kontrol etmek kolaydır. Minimum kümeyi bulmak genel olarak karar verilemezdir (undecidable).

## 4. Karşılanamaz test gereksinimleri (infeasible)

**Infeasible** bir test gereksinimi karşılanamaz: onu sağlayan test değeri yoktur.

```java
void process(int x) {
    if (x > 10 && x < 5) {
        System.out.println("impossible");   // ölü kod (dead code)
    }
}
```

Hiçbir `x` değeri `x > 10 && x < 5` ifadesini doğru yapmaz. Bu yüzden "println satırını çalıştır" infeasible bir TR'dir.

- Burada bunu görebiliyoruz. Genel olarak infeasible TR'leri saptamak çoğu ölçüt için **undecidable**'dır.
- Bu yüzden pratikte %100 kapsama çoğu zaman mümkün değildir.
- Jelly bean: hiçbir torbada Tangerine yoksa, olabilecek en iyi tat kapsaması 5/6 ≈ %83'tür.
- Pratikte infeasible TR'ler çıkarılır veya daha zayıf TR'lerle değiştirilir.

## 5. Bir ölçütü kullanmanın iki yolu

1. Ölçütü sağlayacak test değerlerini doğrudan **üretmek** (generate). En doğal yoldur ama araç olmadan çok zordur.
2. Testleri başka bir yolla yazıp sonra ölçüte göre **ölçmek** (measure). Sektör genellikle bunu tercih eder. Yanıltıcı olabilir: testler %100'e ulaşmıyorsa bu ne anlama gelir?

| | Anlamı |
|---|---|
| **Generator** | Bir ölçütü sağlayan test değerlerini otomatik üreten yordam |
| **Recognizer** | Verilen test değerlerinin bir ölçütü sağlayıp sağlamadığına karar veren yordam |

İki problem de çoğu ölçüt için undecidable'dır; ama tanımak (recognize), üretmekten çok daha sık mümkündür. Bu yüzden çok sayıda **coverage analysis aracı** vardır.

## 6. Subsumption

**C1 ölçütü C2'yi subsume eder (kapsar)**, ancak ve ancak C1'i sağlayan **her** test kümesi C2'yi de sağlıyorsa.

- Flavor criterion, color criterion'ı subsume eder: her tadı tadarsak her rengi de tatmış oluruz.
- Color criterion, flavor criterion'ı subsume **etmez** (T2'ye bak).
- **Branch coverage, statement coverage'ı subsume eder:** her dalı kapsayan bir test kümesi her deyimi de kapsar. Tersi doğru değildir.

## 7. Statement coverage ve branch coverage

```java
int divideBy(int x) {
    int y = 0;
    if (x > 0) {
        y = 10;
    }
    return 100 / y;
}
```

- Statement coverage: 4 TR (dört deyim).
- Branch coverage: 2 TR (`x > 0` doğru; `x > 0` yanlış).

| Test kümesi | Statement | Branch | Sonuç |
|---|---|---|---|
| { x = 5 } | %100 | %50 | 10 döner, test geçer |
| { x = 5, x = 0 } | %100 | %100 | x = 0 → `ArithmeticException: / by zero` |

**%100 statement coverage var ve fault yine de kaçtı.** Branch coverage için gereken ikinci test onu bulur.

Dikkat: subsumption belirli değerlerle değil, test gereksinimleriyle ilgilidir. Bir ölçütün kaç fault bulacağı konusunda yalnızca kaba bir göstergedir. Daha zayıf bir ölçüt için yazılmış bir test kümesi, daha güçlü bir ölçüt için yazılmış bir test kümesinin kaçırdığı bir fault'u yine de bulabilir.

## 8. Neden ölçüt kullanırız?

- Fault bulmada daha iyi, daha az sayıda test; az örtüşme.
- **Traceability:** her testin "neden"i yanıtlanır.
- **Stopping rule:** kaç test gerektiğini önceden biliriz.
- Regresyon testine hazır destek; otomasyona uygun.

**İyi bir coverage criterion:** (1) test gereksinimleri otomatik olarak kolayca hesaplanabilir; (2) test değerleri verimli biçimde üretilebilir; (3) ortaya çıkan testler olabildiğince çok fault bulur.

## 9. Dört yapı ve RIPR

Tüm ölçütler dört yapı üzerinde tanımlanır. Her biri RIPR modelinde bir adım daha derine iner:

| Yapı | Ölçütler testlerden ne ister | RIPR |
|---|---|---|
| **Input space** (girdi uzayı) | girdi alanını keşfet (değer blokları) | — |
| **Graphs** (çizgeler) | düğümlere, kenarlara, yollara ulaş | Reachability |
| **Logic** (mantık) | bir yüklem ifadesine ulaş ve doğruluk değerlerini dene | + Infection |
| **Syntax** (sözdizimi) | ulaş, boz ve değişikliği görünür kıl | + Propagation |

**Revealability**, hangi ölçütü seçersek seçelim, her zaman otomatik testin kendisine (assertion'larına) bağlıdır.

Bu yapılar birçok artifact'tan çıkarılabilir. Örneğin bir çizge kaynak koddan, bir tasarımdan, bir use case'ten veya bir durum makinesinden gelebilir.

## 10. Pratikte

- Birçok şirket hâlâ "monkey testing" yapar: biri plansız biçimde tıklar. Bazıları insanların tasarladığı testleri otomatikleştirir. Hem otomasyonu **hem** ölçütleri kullanan şirketler para tasarruf eder ve daha çok fault bulur.
- LLM'ler ve kod ajanları test case üretebilir. Bu testlerin yeterli olup olmadığını değerlendirmek için yine ölçütlere ihtiyaç vardır.

## Soru fikirleri

- Küçük bir kod ve bir test kümesi ver. Statement veya branch coverage level'ı sor.
- Bir ölçüt ve bir artifact ver. Kaç test gereksinimi olduğunu sor.
- İki ölçüt ver ve hangisinin diğerini subsume ettiğini sor ya da etmediğini gösteren bir test kümesi ver.
- Kod göster ve hangi test gereksiniminin infeasible olduğunu sor.
- Kendi "jelly bean" örneğini kur (örneğin pizzalar, telefonlar veya otobüs hatları) ve kapsama sor.
