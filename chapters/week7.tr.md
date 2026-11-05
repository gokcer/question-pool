# SENG 403 · Hafta 7 · Spesifikasyon Tabanlı Test — Slayta Paralel Öğrenme Rehberi

**Gökçer Gökdal** · Deste: `SENG403_Week7_SpecBasedTesting.pptx` (37 slayt)

> **Bu belge ne?** Dersin tüm konusunu, ders kitabına ya da başka bir kaynağa ihtiyaç duymadan öğrenebilmen için yazıldı. Bölümler slayt sırasını izler; her bölümün başında hangi slayta karşılık geldiği yazar. Hem eğitmen hazırlığı hem öğrencinin kendi kendine çalışması için kullanılabilir. İngilizce sürümü aynı klasörde: `SENG403_Week7_Learning_Guide_EN.md`.
>
> Terimler sınavda İngilizce sorulacağı için İngilizce bırakıldı, yanlarında Türkçe karşılıkları var. Her ana bölümün sonunda **"Kendini dene"** soruları ve cevapları bulunur.

---

## İçindekiler

0. [Bu haftanın tek cümlesi](#0-bu-haftanın-tek-cümlesi)
1. [Haftanın felaketi: Zune 30 ve Ariane 5 (slayt 3–5)](#1-haftanın-felaketi-zune-30-ve-ariane-5-slayt-35)
2. [Neden teknik gerekir? Kapsamlı test imkânsızdır](#2-neden-teknik-gerekir-kapsamlı-test-imkânsızdır)
3. [Üç teknik ailesi (slayt 7)](#3-üç-teknik-ailesi-slayt-7)
4. [Denklik sınıflarına ayırma — EP (slayt 8–11)](#4-denklik-sınıflarına-ayırma--ep-slayt-811)
5. [Sınır değer analizi — BVA (slayt 12–15)](#5-sınır-değer-analizi--bva-slayt-1215)
6. [Karar tabloları (slayt 17–21)](#6-karar-tabloları-slayt-1721)
7. [Durum geçişi testi (slayt 22–24)](#7-durum-geçişi-testi-slayt-2224)
8. [Pairwise test (slayt 25–27)](#8-pairwise-test-slayt-2527)
9. [Real world 2026: yapay zekâ ve sınırlar (slayt 28–29)](#9-real-world-2026-yapay-zekâ-ve-sınırlar-slayt-2829)
10. [Juice Shop'ta sınır avı (slayt 30–34)](#10-juice-shopta-sınır-avı-slayt-3034)
11. [Teknikleri birlikte kullanmak: Myers'ın stratejisi (slayt 37)](#11-teknikleri-birlikte-kullanmak-myersın-stratejisi-slayt-37)
12. [Çıkış bileti ve ara sınav tuzakları (slayt 38, 40)](#12-çıkış-bileti-ve-ara-sınav-tuzakları-slayt-38-40)
13. [Terimler sözlüğü (EN–TR)](#13-terimler-sözlüğü-entr)
14. [Büyük alıştırma seti (cevaplı)](#14-büyük-alıştırma-seti-cevaplı)

---

## 0. Bu haftanın tek cümlesi

> **Bugs live at the boundaries.** — Hatalar sınırlarda yaşar.

Bir programın alabileceği değerlerin sayısı neredeyse sonsuzdur. Hepsini deneyemeyiz. Bu derste öğrenilen dört teknik tek bir soruya cevap verir: **"Sonsuz değer arasından hangi birkaç tanesini seçersem hata bulma şansım en yüksek olur?"**

| Teknik | Hangi soruya cevap verir? |
|---|---|
| Equivalence partitioning (EP) | Program hangi değer gruplarına **aynı** davranıyor? Her gruptan bir değer yeter. |
| Boundary value analysis (BVA) | Bu grupların **kenarları** nerede? Hatalar orada. |
| Decision table | Sonuç birkaç koşulun **birleşimine** bağlıysa, hangi birleşimler var? |
| State transition | Sonuç **daha önce ne olduğuna** bağlıysa, hangi geçişler mümkün, hangileri yasak? |

Dördü de **black-box** (kara kutu) tekniğidir: testleri kodu okuyarak değil, **spesifikasyonu** (kuralları, gereksinimleri) okuyarak tasarlarız.

---

## 1. Haftanın felaketi: Zune 30 ve Ariane 5 (slayt 3–5)

### 1.1 Zune 30 (slayt 3–4)

**Ne oldu?** Zune, Microsoft'un 2006'da çıkardığı, iPod'a rakip müzik çaları. 31 Aralık 2008 gece yarısı, dünyadaki bütün 30 GB'lık Zune'lar açılış ekranında dondu. Microsoft'un çözümü: "Pil bitene kadar bekleyin, 1 Ocak'tan sonra şarj edin." İşe yaradı.

**Neden?** 2008 bir **artık yıl** (leap year): 366 gün. Yani 31 Aralık 2008, yılın **366. günü**.

Cihazın saat yongası tarihi "1 Ocak 1980'den beri geçen gün sayısı" olarak tutuyordu. Bu sayıyı yıla çeviren kod (yongayı üreten Freescale firmasının sürücüsü) şöyleydi:

```c
year = 1980;
while (days > 365) {
    if (IsLeapYear(year)) {
        if (days > 366) {
            days -= 366;
            year += 1;
        }
    } else {
        days -= 365;
        year += 1;
    }
}
```

**Adım adım düşünelim.** Döngü her turda bir yılın günlerini çıkarır. Yıl 2008'e geldiğimizde kalan gün sayısına bakalım:

| Kalan gün (`days`) | `days > 365`? | Artık yıl mı? | `days > 366`? | Ne olur? |
|---|---|---|---|---|
| 365 (30 Aralık) | hayır | – | – | döngü biter ✓ |
| **366 (31 Aralık)** | **evet** | **evet** | **hayır** | **hiçbir şey: `days` değişmez, döngü baştan → sonsuz** |
| 367 (bir sonraki yıl) | evet | evet | evet | 366 çıkarılır ✓ |

366'da dış koşul "devam et" der, iç koşul "çıkarma" der. Program ne ilerleyebilir ne çıkabilir: **sonsuz döngü**.

**Neden "sınır" hatası?** Kodda iki eşik var: 365 ve 366. Hata, tam bu iki eşiğin **arasındaki tek değerde**, 366'da ortaya çıkıyor. "Artık yılın son günü" bir sınıfın (partition'ın) son değeridir; yani bir **sınır değerdir** (boundary value). Sınır değer analizi bize "artık yılda 365, 366, 367'yi dene" der; 366 ile tek bir test cihazı dondurur ve hata bulunur.

**Düzeltme fikri:** Artık yılda `days == 366` ise döngüden çıkmak (çünkü 366. gün hâlâ aynı yıldadır). Kısacası programcı "366'dan büyük"ü düşünmüş, "tam 366"yı unutmuş: klasik bir **off-by-one** (bir eksik/bir fazla) hatası.

### 1.2 Ariane 5 (slayt 5)

**Ne oldu?** 4 Haziran 1996. Avrupa Uzay Ajansı'nın yeni roketi Ariane 5'in ilk uçuşu. Kalkıştan yaklaşık 37 saniye sonra roket rotasından saptı ve parçalandı. İçinde insan yoktu; roket ve taşıdığı uydular kaybedildi.

**Neden?** Yön bulma (navigasyon) yazılımı, daha önce sorunsuz uçan **Ariane 4'ten aynen alınmıştı**. Yazılım, yatay hızla ilgili 64 bitlik bir ondalıklı sayıyı **16 bitlik işaretli tam sayıya** çeviriyordu. 16 bitlik işaretli bir tam sayı en fazla **32.767** olabilir. Ariane 4 yavaştı; değer hep bu sınırın altında kaldı. Ariane 5 çok daha hızlıydı; değer 32.767'yi aştı, dönüşüm hata verdi (**overflow** / taşma). Yedek bilgisayarda da **aynı yazılım** vardı; o da aynı saniyede çöktü.

**Ders:** Sınırlar yalnızca spesifikasyonun metninde değil, **veri tiplerinde** de vardır (16 bit, 32 bit, bir sütunun en büyük uzunluğu…). Girdi aralığı değiştiğinde (yeni, daha hızlı roket), eski sınırlar yeniden test edilmelidir. Bu, ileride göreceğimiz **regresyon testi** fikrine de bağlanır.

### Kendini dene (1)

1. Zune koduna hangi tek değerle test yazsaydın hata bulunurdu?
2. Ariane 5'te sınır nerden geliyordu: spesifikasyondan mı, veri tipinden mi?

> **Cevaplar:** 1) Artık yılda `days = 366`. 2) Veri tipinden: 16 bitlik işaretli tam sayının üst sınırı 32.767.

---

## 2. Neden teknik gerekir? Kapsamlı test imkânsızdır

Bu, Hafta 6'da gördüğümüz **7 test prensibinden** ikincisidir: *Exhaustive testing is impossible* (kapsamlı test imkânsızdır).

Basit bir örnek: Juice Shop'un kayıt formunda parola 5–40 karakter olabilir. Klavyede yaklaşık 95 yazılabilir karakter var. Sadece 5 karakterlik parolaların sayısı 95⁵ ≈ **7,7 milyar**. 40 karakterliklerin sayısı astronomiktir. Her birini denemek mümkün değil.

Lab 4'teki teslimat ücreti fonksiyonu için bile: km 0–30 (31 değer) × tutar 0,01–1.500,00 TL (150.000 değer) × Deluxe evet/hayır (2) ≈ **9,3 milyon** birleşim.

Bu yüzden **test tasarım teknikleri** kullanırız. Bir teknik iki şey verir:

1. **Hangi değerleri** seçeceğimizi (az sayıda ama akıllıca),
2. **Neden** bu değerleri seçtiğimizi (yöneticiye, ekibe, denetçiye açıklayabileceğimiz bir gerekçe) ve ne kadarını test ettiğimizi gösteren bir **kapsama (coverage)** ölçüsü.

> **Coverage item (kapsama öğesi):** Bir tekniğin "bunun için bir test istiyorum" dediği şey. EP'de her **sınıf**, BVA'da her **sınır değer**, karar tablosunda her **kural (sütun)**, durum geçişinde her **geçiş** bir kapsama öğesidir. Kapsama yüzdesi her zaman aynı formülle hesaplanır:
>
> **Coverage = test edilen kapsama öğeleri ÷ toplam kapsama öğeleri × %100**

---

## 3. Üç teknik ailesi (slayt 7)

ISTQB CTFL (Bölüm 4.1) test tekniklerini üç aileye ayırır:

| Aile | Testler nereden türetilir? | Örnek teknikler | Ne zaman kullanılabilir? |
|---|---|---|---|
| **Black-box** (kara kutu) = **specification-based** | Yazılımın **ne yapması gerektiğinden**: gereksinimler, user story'ler, iş kuralları, modeller | EP, BVA, karar tablosu, durum geçişi | Kod yazılmadan önce bile |
| **White-box** (beyaz kutu) = **structure-based** | Yazılımın **iç yapısından**: kodun satırları ve dalları | Statement coverage, branch coverage | Kod varken (Hafta 9) |
| **Experience-based** (deneyim tabanlı) | Testçinin **bilgi ve deneyiminden** | Error guessing, exploratory testing, checklist-based | Her zaman, diğerlerini tamamlar (Hafta 9) |

**Kara kutunun iki büyük avantajı:**

- Testler **kod yazılmadan** tasarlanabilir (spesifikasyon yeterli). Bu, "erken test" prensibine uyar.
- Kod baştan yazılsa bile testler **geçerli kalır**, çünkü iç yapıya bağlı değildir.

**Zayıf yanı:** Spesifikasyonda yazmayan bir şeyi (gizli bir özellik, unutulmuş bir durum) bulamaz. Spesifikasyon yanlışsa, testler de yanlış beklenen sonuç verir. Bu yüzden diğer ailelerle birlikte kullanılır.

---

## 4. Denklik sınıflarına ayırma — EP (slayt 8–11)

### 4.1 Fikir

**Equivalence partitioning** (denklik sınıflarına ayırma), girdi (veya çıktı) değerlerini, programın **aynı şekilde davranması beklenen** gruplara böler. Her gruba **partition** (sınıf; Myers'ın eski adıyla *equivalence class*) denir.

Temel varsayım: Bir sınıftan bir değer doğru çalışıyorsa, aynı sınıftaki diğer değerler de doğru çalışır; bir değer hata veriyorsa, diğerleri de büyük olasılıkla aynı hatayı verir. Bu yüzden **her sınıftan bir test yeterlidir**.

### 4.2 Çözümlü örnek: Juice Shop parolası (slayt 8)

Kural: **"Password must be 5–40 characters long."**

Parolanın **uzunluğu** için kaç farklı davranış var?

| Sınıf | Uzunluk | Geçerli mi? | Beklenen davranış | Örnek temsilci |
|---|---|---|---|---|
| P1 | 0–4 | **invalid** (geçersiz) | hata mesajı | "abc" (3) |
| P2 | 5–40 | **valid** (geçerli) | kabul edilir | "juice2026" (9) |
| P3 | 41 ve üstü | **invalid** | hata mesajı | 50 tane "a" |

Üç sınıf → **üç test**. Bu üç testle EP kapsaması 3/3 = **%100**.

Dikkat: P1 ve P3 aynı hata mesajını veriyor ama **ayrı sınıflardır**, çünkü farklı bir kuralı (alt sınır ve üst sınır) ihlal ediyorlar. Yazılım alt sınırı doğru, üst sınırı yanlış kontrol edebilir; tek bir "geçersiz" sınıf bunu gizler.

### 4.3 Sınıfları nasıl bulursun? (adım adım)

1. **Girdileri listele.** Her alan, parametre, dosya, hatta saat ve tarih bir girdidir.
2. **Her girdinin kurallarını oku.** "5–40", "en fazla 5", "evet/hayır", "Gold/Silver/Normal" gibi.
3. **Kurala göre böl:**

| Kural türü | Sınıflar | Örnek |
|---|---|---|
| Bir aralık (min–max) | 1 geçerli + 2 geçersiz (alt, üst) | yaş 18–65 → <18, 18–65, >65 |
| Tek bir alt sınır ("en az") | 1 geçerli + 1 geçersiz | sepet ≥ 1 → ≤0, ≥1 |
| Değer listesi (seçenekler) | her seçenek 1 geçerli sınıf + 1 geçersiz ("listede olmayan") | ödeme: Card, Wallet, Coupon, (başka) |
| Boolean (evet/hayır) | 2 sınıf | Deluxe üye: evet, hayır |
| "Şu biçimde olmalı" | 1 geçerli + biçimin her bozulma şekli | e-posta: geçerli, @ yok, alan adı yok |

4. **Çıktıları da düşün.** Farklı çıktılar farklı sınıflar demektir (örneğin ücret = 0, yarım, tam).
5. **Kontrol et:** Sınıflar **çakışmıyor** mu (bir değer iki sınıfta olmamalı) ve **boşluk** yok mu (her olası değer bir sınıfta olmalı)?

### 4.4 Dört kural (slayt 9)

1. **Her girdinin geçerli ve geçersiz sınıfları vardır.** Yeni başlayanlar geçersizleri unutur. Oysa gerçek kullanıcılar ve saldırganlar en çok geçersiz değer girer.
2. **Sınıflar çakışmaz ve boşluk bırakmaz.** Her değer tam olarak bir sınıfa aittir.
3. **Her sınıfa bir test yeter.** İçinden herhangi bir değer seçilebilir.
4. **Geçersiz değerler tek tek test edilir.** Buna dikkat:

**Masking (maskeleme) nedir?** Bir testte iki geçersiz değer olsun: 3 karakterlik parola **ve** geçersiz e-posta. Program yalnızca "e-posta geçersiz" der. Parola kontrolü çalıştı mı? Bilemeyiz; belki parola kontrolü hiç yapılmadı, ama e-posta hatası önce geldiği için bunu gizledi. Bu yüzden her testte **yalnızca bir geçersiz değer** olur, diğer girdiler geçerli tutulur.

> **Geçerli sınıflar ise birleştirilebilir:** Bir testte birden çok girdinin geçerli sınıflarını aynı anda kapsayabiliriz (ör. geçerli parola + geçerli e-posta + geçerli güvenlik sorusu). Bu test sayısını azaltır.

### 4.5 Birden çok girdi: "each choice" fikri

Birden çok girdi varsa, CTFL'nin varsayılan beklentisi her girdinin her sınıfının **en az bir kez** test edilmesidir (*each choice coverage*). Örnek:

- Girdi A: 3 sınıf, girdi B: 2 sınıf.
- En az test sayısı = en büyük sınıf sayısı = **3** (her testte A'dan bir, B'den bir sınıf). Ama geçersiz sınıflar ayrı testlerde olmalı (masking kuralı), bu yüzden pratikte biraz daha fazla test gerekir.

Tüm birleşimleri (3 × 2 = 6) test etmek gerekmez; birleşimler önemliyse karar tablosu (bölüm 6) ya da pairwise (bölüm 8) kullanılır.

### 4.6 EP kapsaması

> **EP coverage = test edilen sınıflar ÷ belirlenen sınıflar × %100**

Parolada 3 sınıftan 2'sini test ettiysen kapsama %66,7'dir. Ara sınavda sık sorulur.

### 4.7 Apple Juice alıştırması (slayt 10–11)

Kural: Bir müşteri bir siparişte **1 ile 5 arası** Apple Juice (1000ml) alabilir. Miktar tam sayıdır.

| Sınıf | Değerler | Geçerli mi? | Temsilci | Gerçek Juice Shop davranışı |
|---|---|---|---|---|
| Q1 | 0 ve altı | invalid | 0 | Ekranda girilemez: "−" butonu 1'de durur |
| Q2 | 1–5 | valid | 3 | sepete eklenir |
| Q3 | 6 ve üstü | invalid | 8 | "You can order only up to 5 items of this product." |

**3 sınıf → %100 EP kapsaması için 3 test.**

**Derin soru (slaytta "Think about"):** "−1'i ekrandan giremiyorum, demek ki test etmeme gerek yok" doğru mu? **Hayır.** Ekran (tarayıcı) bir şeyi engelliyor diye sunucu da engelliyor demek değildir. Bir saldırgan isteği doğrudan sunucuya gönderebilir. Juice Shop'ta bunun bir görevi var ("Payback Time": negatif miktarla sipariş verip zengin olmak). Bu konuya Hafta 11'de (güvenlik) döneceğiz. Bugünün dersi: **sınırı, kararın gerçekten verildiği yerde test et.**

**Stok da bir sınırdır:** Ürün stokta 3 tane kaldıysa geçerli aralık 1–3'e iner. Sınırlar sabit olmak zorunda değildir; "hareket eden sınır" da test edilmelidir.

### 4.8 EP'nin sınırlılığı → BVA'ya geçiş

EP, parolada 9 karakteri ve 3 karakteri seçmemize izin verir. Ama programcı `uzunluk >= 5` yerine `uzunluk > 5` yazsaydı? 9 karakter doğru çalışır, 3 karakter doğru reddedilir; **iki EP testi de geçer, hata kaçar.** Hata yalnızca 5 karakterde görünür. İşte bu yüzden BVA gerekir.

### Kendini dene (4)

1. "Yaş 18–65 olmalı, tam sayı" kuralı için sınıfları ve temsilcileri yaz.
2. Ödeme yöntemi bir listeden seçiliyor: Card, Wallet, Coupon. Kaç sınıf var?
3. Bir test: yaş = 10 ve ödeme = "Bitcoin". Ne sorun var?

> **Cevaplar:** 1) <18 (ör. 10, geçersiz), 18–65 (ör. 30, geçerli), >65 (ör. 80, geçersiz). 2) 3 geçerli (her seçenek) + 1 geçersiz (listede olmayan) = 4. 3) İki geçersiz değer aynı testte: masking. Ayrı testlere böl.

---

## 5. Sınır değer analizi — BVA (slayt 12–15)

### 5.1 Fikir

Geliştiriciler hataları ortada değil, **kenarlarda** yapar:

- `<=` yerine `<` (ya da tersi),
- 6 yerine 5 (bir eksik/bir fazla: **off-by-one**),
- bir kez fazla ya da eksik dönen döngü (Zune),
- yanlış sabit (40 yerine 20).

**Boundary value (sınır değer):** **Sıralı** bir sınıfın **en küçük** ya da **en büyük** değeri. BVA yalnızca değerleri sıralanabilen sınıflarda kullanılır: sayılar, uzunluklar, tarihler, saatler. (Renk listesi gibi sırasız seçeneklerde BVA anlamsızdır.)

### 5.2 İki yöntem: 2 değerli ve 3 değerli

Önce **sınır değerleri** bul: her sınıfın **en küçük** ve **en büyük** değeri. Geçersiz sınıfların da sınır değerleri vardır. Bir sınıfın bir ucu açıksa (ör. "41 ve üstü"nün en büyüğü yok) o uçta sınır değer yoktur. Ama **doğal bir alt sınır** varsa (bir uzunluk 0'dan küçük olamaz) o da bir sınır değerdir.

Parola 5–40 örneği. Uzunluğun sınıfları: **0–4** (geçersiz) · **5–40** (geçerli) · **41+** (geçersiz).

```
 uzunluk:   0   1   2   3   4 | 5   6  ...  39  40 | 41  42  ...
 sınıf:     çok kısa (0–4)    | geçerli (5–40)     | çok uzun (41+)
 sınır değerler:  0 ve 4        5 ve 40              41 (üst ucu açık)
```

Sınır değerler: **0, 4, 5, 40, 41**. (0 = boş parola. Unutulması çok kolay ve gerçek hayatta çok önemli bir test.)

CTFL v4 iki yöntem tanımlar:

**2-value BVA (2 değerli; CTFL varsayılanı):** Her sınır değer bir kapsama öğesidir; her sınır değerin yanında, komşu sınıftaki en yakın komşusu da zaten bir sınır değerdir. Yani 2 değerli BVA = **tüm sınır değerleri test et**.

**3-value BVA (3 değerli):** Her sınır değer **ve iki komşusu**. Pratik kural: **2 değerli listeyi al, her değerin bir altını ve bir üstünü ekle** (imkânsız olanları, ör. −1 uzunluğunu, at).

| Yöntem | Test değerleri | Test sayısı |
|---|---|---|
| 2 değerli | **0, 4, 5, 40, 41** | 5 |
| 3 değerli | **0, 1, 3, 4, 5, 6, 39, 40, 41, 42** | 10 |

**Uç açık sınıflarda (tam sayı, alt sınır yok):** "Yaş 18–65 tam sayı" ise sınıflar ≤17 · 18–65 · ≥66; ilk sınıfın alt ucu, son sınıfın üst ucu açık. Sınır değerler 17, 18, 65, 66. 2 değerli: **17, 18, 65, 66**. 3 değerli: **16, 17, 18, 19, 64, 65, 66, 67**.

> **Bu, ISTQB'nin resmî örnek sınavındaki hesaplamayla aynıdır** (CTFL v4.0 Sample Exam A: parola uzunluğu 6–12 için 2 değerli 0, 5, 6, 12, 13; 3 değerli 0, 1, 4, 5, 6, 7, 11, 12, 13, 14). Bazı eski kitaplar 3 değerliyi yalnızca geçerli sınıfın kenarlarına uygular (4, 5, 6, 39, 40, 41); CTFL sınavında **bu eksik sayılır**.

**Kısaca hatırla:**
- Sınır değer = her sınıfın ilk ve son değeri (geçersiz sınıflar dahil, doğal sınırlar dahil).
- 2 değerli = sınır değerlerin hepsi.
- 3 değerli = sınır değerlerin hepsi + her birinin iki komşusu.

### 5.3 3 değerli BVA neden var?

2 değerlinin kaçırdığı bir hata türü: Programcı `uzunluk >= 5` yerine `uzunluk == 5` yazmış olsun.

| Test | 2 değerli sonuç | 3 değerli sonuç |
|---|---|---|
| 4 → reddedilmeli | reddedildi ✓ | reddedildi ✓ |
| 5 → kabul | kabul ✓ | kabul ✓ |
| 6 → kabul | (test edilmedi) | **reddedildi ✗ → hata bulundu** |

Yani 3 değerli BVA biraz daha fazla test ister ama birkaç hatayı daha yakalar. Riskli girdilerde (para, güvenlik, limitler) tercih edilir.

### 5.4 BVA kapsaması

> **BVA coverage = test edilen sınır değerler ÷ belirlenen sınır değerler × %100**

Parolada 2 değerli BVA ile 5 sınır değer var (0, 4, 5, 40, 41); 4, 5 ve 40'ı test ettiysen kapsama 3/5 = %60.

### 5.5 Sınırlar nerede saklanır? (slayt 13)

**1. Spesifikasyonun kelimelerinde.** En kritik kelimeler:

| İfade | Anlamı | Sınır |
|---|---|---|
| "150 TL **or more**" / "en az 150" | 150 dahil | 149,99 \| 150,00 |
| "**more than** 150" / "150'den fazla" | 150 hariç | 150,00 \| 150,01 |
| "**up to** 5" / "en fazla 5" | 5 dahil | 5 \| 6 |
| "**between** 5 **and** 40" | çoğu zaman dahil, ama **belirsiz!** | Sor! |

"Between" belirsizse bu bir **spesifikasyon kusurudur**; testçi sorar. (Hafta 6: statik test burada başlar.)

**2. Adım büyüklüğünde (step size).** Komşu değer, değerin "bir adım" ötesidir:

- Tam sayılar: adım 1 → 5'in komşusu 4 ve 6.
- 2 ondalıklı para: adım 0,01 → **150,00'ın komşusu 149,99'dur, 149 değil!**
- Saat: adım 1 saniye (ya da sistem neyi ölçüyorsa).
- Tarih: adım 1 gün → 31 Aralık'ın komşusu 1 Ocak.

**3. Tarihlerde.** 365/366. gün (Zune), 28/29 Şubat, ayın son günü, yıl sonu, saat dilimi değişimi, yaz saati. **2038 problemi:** Birçok 32 bitlik sistem zamanı "1 Ocak 1970'ten beri geçen saniye" olarak tutar. 32 bitlik işaretli sayı 19 Ocak 2038'de taşar. Zune hatasının büyük kardeşi.

**4. Veri tiplerinde ve boyutlarda.** 16 bit = 32.767 (Ariane), 32 bit ≈ 2,1 milyar, boş metin (uzunluk 0), boş liste, bir veritabanı sütununun en fazla uzunluğu.

**5. Çıktılarda.** Myers çıktı tarafında da sınır aramayı önerir: en küçük ve en büyük çıktıyı üreten girdileri test et. Örneğin teslimat ücretinin tam **0 TL** olduğu durum, en büyük indirim, bir sayfaya sığan en fazla sonuç sayısı.

### 5.6 Çözümlü örnek: harf notu

Kural: `grade(score)`, puan 0–100 tam sayı. 0–49 → FF, 50–69 → CC, 70–84 → BB, 85–100 → AA. Diğer puanlar hata.

**Adım 1 — Sınıflar (EP):** <0 · 0–49 · 50–69 · 70–84 · 85–100 · >100 → **6 sınıf**.

**Adım 2 — Kenarlar:** −1|0 · 49|50 · 69|70 · 84|85 · 100|101 → **5 kenar**.

**Adım 3 — 2 değerli BVA:** −1, 0, 49, 50, 69, 70, 84, 85, 100, 101 → **10 değer**.

**Adım 4 — Beklenen sonuçlar:** −1 → hata, 0 → FF, 49 → FF, 50 → CC, 69 → CC, 70 → BB, 84 → BB, 85 → AA, 100 → AA, 101 → hata.

**Neden ikisi birlikte?** Kod `if score > 85: return "AA"` diyorsa: EP değeri 90 → AA (geçer), ama BVA değeri 85 → BB (**hata bulundu**). EP sınıfları bulur, BVA kenarları test eder.

> **Kural sayma kısayolu:** Uçları açık bir tam sayı aralığında n bitişik sınıf varsa (n−1) kenar vardır; 2 değerli BVA ile 2 × (n−1) değer çıkar. Notta 6 sınıf → 5 kenar → 10 değer. (Not: puanın alt ucu açık kabul edildi; −5 gibi değerler de "0'ın altı" sınıfındadır.)

### 5.7 Slayt 14–15'teki CTFL sorusu

"Yaş 18–65, tam sayı. 2 değerli BVA ile %100 kapsama veren küme?"

- a) 18, 65 → yalnızca geçerli taraf; 17 ve 66 eksik → %50.
- **b) 17, 18, 65, 66 → doğru.** Her kenarın iki yanı.
- c) 17, 18, 19, 64, 65, 66 → 2 değerli için gereksiz değerler (19, 64) var; tam 3 değerli de değil (16 ve 67 eksik). Soru 2 değerliyi istiyor.
- d) 0, 18, 40, 65, 100 → EP'ye benzer; 17 ve 66 yok.

**Ders:** Sınavda "2-value" mı "3-value" mı yazdığını mutlaka oku.

### Kendini dene (5)

1. Sepet miktarı 1–5. 2 değerli ve 3 değerli BVA değerlerini yaz.
2. "Sipariş 50,00 TL'den fazlaysa kargo bedava" (2 ondalık). Sınırın iki değeri nedir?
3. Bir metin alanı en fazla 160 karakter. Hangi uzunlukları test edersin (2 değerli)? Alan zorunluysa alt sınır ne olur?

> **Cevaplar:** 1) Sınıflar ≤0 · 1–5 · ≥6 (alt uç açık: −1, −2… de "≤0"). 2 değerli: 0, 1, 5, 6. 3 değerli: −1, 0, 1, 2, 4, 5, 6, 7. 2) "Fazla" → 50,00 hariç: **50,00 | 50,01**. 3) 160 | 161. Zorunluysa ayrıca 0 | 1 (boş metin geçersiz, 1 karakter geçerli).

---

## 6. Karar tabloları (slayt 17–21)

### 6.1 Ne zaman?

EP ve BVA **tek bir girdiye** bakar. Ama sonuç birkaç koşulun **birleşimine** bağlıysa (giriş yapılmış mı **ve** sepet boş mu **ve** adres kayıtlı mı), tek tek girdi testi birleşimleri kaçırır. **Decision table** (karar tablosu) tüm birleşimleri sistemli olarak listeler.

İş kuralları, fiyatlandırma, sigorta, kampanya, yetki kontrolleri karar tablolarının doğal alanıdır.

### 6.2 Parçaları ve gösterim

| Parça | Anlamı |
|---|---|
| **Conditions** (koşullar) | Evet/hayır sorular: "Giriş yapılmış mı?" |
| **Actions** (eylemler) | Sonuçlar: "girişe git", "ödemeye git" |
| **Rule** (kural) = bir **sütun** | Koşulların tek bir birleşimi ve o birleşimin eylem(ler)i |
| **"–"** | "Fark etmez" (don't care): bu koşul bu kuralda sonucu değiştirmez |

**Sınavdaki gösterim.** Bu derste **Y** (yes, evet) ve **N** (no, hayır) yazıyoruz. CTFL müfredatı ve sınavlar **T** (true, doğru) ve **F** (false, yanlış) yazar — anlamı tamamen aynıdır. Ayrıca mümkün olmayan bir birleşim için **N/A**, eylem satırında "bu eylem olur" için **X** kullanırlar (boş hücre "olmaz" demektir).

| Bizim notlar | CTFL / sınav | Anlamı |
|---|---|---|
| Y | T | koşul doğru |
| N | F | koşul yanlış |
| – | – | bu kuralda fark etmez |
| (silinen sütun) | N/A | imkânsız birleşim |
| X | X | eylem olur |

**Tek eylem satırı mı, her eyleme bir satır mı?** CTFL ikisine de izin verir:

- **Her eyleme bir satır, X ile** (*limited-entry*): standart biçim. Bir kural **birden çok** eylemi tetikleyebiliyorsa her zaman bunu kullan.
- **Değerli tek bir "Eylem" satırı** (*extended-entry*): daha kısa. Yalnızca her kuralın **tam olarak bir** eylemi varsa mümkündür; checkout'ta olduğu gibi (kullanıcı ya girişe ya adres sayfasına ya da ödemeye gider). Sadeleştirme slaytları (18–21) bu yüzden kısa biçimi kullanır.

**Gerçek hayat: bir sütunda birden çok eylem.** Müşteri bir siparişi iptal ettiğinde dükkân birden fazla şey yapar:

| | 1 | 2 | 3 |
|---|---|---|---|
| C1 Sipariş ödendi mi? | T | T | F |
| C2 Sipariş kargoya verildi mi? | F | T | – |
| Parayı iade et | X | | |
| İptal e-postası gönder | X | | X |
| Ürünleri stoğa geri ekle | X | | X |
| Müşteri hizmetlerine ilet | | X | |

1. sütunda **üç X** var: tek bir test üçünü de kontrol etmeli — para iade edildi mi, e-posta geldi mi, stok arttı mı? Gerçek hatalar çoğu zaman tam burada saklanır: üç eylemden biri unutulur. Bu, tek bir eylem satırına yazılamaz.

### 6.3 Adım adım kurulum (slayt 17'deki checkout örneği)

Spesifikasyon (öğretim için sadeleştirilmiş Juice Shop ödeme butonu):
- Giriş yapılmamışsa → giriş sayfasına git.
- Sepet boşsa → "sepet boş" mesajı.
- Kayıtlı adres yoksa → adres iste.
- Aksi hâlde → ödemeye git.

**Adım 1 — Koşulları ve eylemleri yaz.** C1 Logged in? · C2 Basket empty? · C3 Address saved? Eylemler: login, "empty", address, payment.

**Adım 2 — Sütun sayısı:** 3 Boolean koşul → **2³ = 8 sütun**. Genel formül: n koşul → **2ⁿ** sütun (4 koşul → 16, 5 koşul → 32).

**Adım 3 — Tüm Y/N birleşimlerini düzenli yaz.** Hile: ilk satırı 4Y-4N, ikincisini 2Y-2N-2Y-2N, üçüncüsünü Y-N-Y-N… diye doldur. Böylece hiçbir birleşimi unutmazsın.

| | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
|---|---|---|---|---|---|---|---|---|
| C1 Logged in? | Y | Y | Y | Y | N | N | N | N |
| C2 Basket empty? | Y | Y | N | N | Y | Y | N | N |
| C3 Address saved? | Y | N | Y | N | Y | N | Y | N |
| Go to login | | | | | X | X | X | X |
| Show "empty" | X | X | | | | | | |
| Ask for address | | | | X | | | | |
| Go to payment | | | X | | | | | |

**Adım 4 — Her sütunu spesifikasyondan doldur.** Sütun 5: giriş yok, sepet boş. Önce hangisi? Spesifikasyon açık değil! Biz "önce giriş" kararını verdik. **İşte karar tablosunun gizli gücü:** Spesifikasyondaki belirsizlikleri ve boşlukları kod yazılmadan ortaya çıkarır. Eylemsiz bir sütun ya da iki çelişen eylem = spesifikasyon kusuru → ürün sahibine sorulur.

### 6.4 Sadeleştirme, adım adım (slayt 18–21)

n koşullu tam bir tabloda 2ⁿ sütun vardır. Çoğu zaman birçok sütun aynı eyleme gider; bu yüzden tabloyu **sadeleştirir** (collapse, minimise) ve daha az kural test ederiz. Sadeleştirme mekanik bir iştir: tek bir kural ve biraz sabır yeter.

**Birleştirme kuralı:** şu iki şartı sağlayan iki sütunu al:

1. **eylemleri aynı**, ve
2. **yalnızca bir** koşulda farklılar (diğer bütün koşullar eşit).

Bunları tek sütunda birleştir ve farklı olan koşula **"–"** (*fark etmez*, don't care) yaz. Sonra yeni sütunlarla **tekrar et**; birleşecek bir şey kalmayınca dur. En sonda sonucu **kontrol et**.

**Adım 1 — ikilileri birleştir.** Tam checkout tablosuyla başla (bölüm 6.3). Yalnızca bir koşulda farklı olan sütunları ara. Burada C3 son satır, bu yüzden yan yana duran 1–2, 3–4, 5–6, 7–8 sütunları yalnızca C3'te farklı:

| İkili | Sütunlar (C1 C2 C3) | Eylemler | Birleşir mi? | Yeni sütun |
|---|---|---|---|---|
| 1 + 2 | Y Y Y · Y Y N | "boş" · "boş" | ✓ eylem aynı, yalnızca C3 farklı | **Y Y –** → "boş" |
| 3 + 4 | Y N Y · Y N N | ödeme · adres | ✗ **eylemler farklı** | ikisi de kalır |
| 5 + 6 | N Y Y · N Y N | giriş · giriş | ✓ | **N Y –** → giriş |
| 7 + 8 | N N Y · N N N | giriş · giriş | ✓ | **N N –** → giriş |

Adım 1'den sonra: (Y Y –) "boş" · (Y N Y) ödeme · (Y N N) adres · (N Y –) giriş · (N N –) giriş. Yani 5 sütun.

**Adım 2 — yeni sütunlarla tekrar et.** Tireli bir sütun yeniden birleşebilir, ama yalnızca tiresi **aynı yerde** olan bir sütunla:

| İkili | Sütunlar | Eylemler | Birleşir mi? | Yeni sütun |
|---|---|---|---|---|
| (N Y –) + (N N –) | ikisinde de tire C3'te; yalnızca C2 farklı | giriş · giriş | ✓ | **N – –** → giriş |
| (Y Y –) + (Y N Y) | tire yalnızca birinde | "boş" · ödeme | ✗ eylemler farklı (tireler de aynı yerde değil) | — |

Adım 2'den sonra: 4 sütun. Eylemi aynı olup yalnızca bir koşulda farklı olan başka sütun yok → **dur**.

**Adım 3 — kontrol et.** Bir tire hem Y'yi hem N'yi temsil eder, yani kuralın kapsadığı sütunları **ikiye katlar**. k tireli bir kural 2ᵏ orijinal sütunu kapsar:

| Kural | Koşullar | Tire | Kapsadığı | Eylem |
|---|---|---|---|---|
| R1 | N – – | 2 | 2² = 4 sütun (5, 6, 7, 8) | giriş |
| R2 | Y Y – | 1 | 2¹ = 2 sütun (1, 2) | "boş" |
| R3 | Y N N | 0 | 1 sütun (4) | adres |
| R4 | Y N Y | 0 | 1 sütun (3) | ödeme |

4 + 2 + 1 + 1 = **8** = 2³ ✓. Her orijinal sütun **tam olarak bir** kurala aittir. Toplam 2ⁿ değilse ya da bir sütun iki kurala giriyorsa, bir birleştirme yanlıştır.

8 sütun → **4 kural → 4 test**.

**Tireli bir kuralı test etmek.** "–", sonucun o koşula bağlı olmadığı anlamına gelir; ama gerçek bir test yine de gerçek bir değer ister. R1 için örneğin "giriş yapılmamış, sepet boş, adres kayıtlı" seçersin. Herhangi bir seçim olur. Riskli bir kuralda birden çok seçimi de test edebilirsin (yani tam sütunlara geri dönersin).

**Sık yapılan hatalar:**

- **İki farkı birden birleştirmek.** (Y N Y) ile (N Y N) üç koşulda da farklı. Eylemleri aynı olsa bile doğrudan asla birleşemez.
- **Farklı eylemleri birleştirmek.** 3. ve 4. sütun yalnızca C3'te farklı, ama biri ödeme, diğeri adres diyor. Ayrı kalırlar.
- **Adım 2'yi atlamak.** Birçok öğrenci ikililerden sonra durur ve 4 yerine 5 kural bulur. Bu yine doğrudur (büyük bir tablo yanlış değildir), yalnızca daha az sadedir. Gereğinden **fazla** sadeleştirilmiş bir tablo ise yanlıştır.
- **Kontrolü unutmak.** 2ᵏ kontrolü neredeyse her hatayı tek satırda yakalar.

**İkinci örnek: sinema (Lab 4 kopya kâğıdında da var).** Koşullar: C1 çocuk mu? C2 öğrenci kartı var mı? C3 çarşamba mı? Eylem: %25 indirim (var/yok). Tam tablo: 1–4. sütunlarda C1 = Y ve "yok"; 5–8. sütunlarda C1 = N: (Y Y) var, (Y N) var, (N Y) var, (N N) yok.

- Adım 1: 1+2 → (Y Y –) yok · 3+4 → (Y N –) yok · 5+6 → (N Y –) var · 7+8: (N N Y) var, (N N N) yok → eylemler farklı, kalır.
- Adım 2: (Y Y –) + (Y N –) → (Y – –) yok. (N Y –), (N N Y) ile birleşemez: tireler aynı yerde değil.
- Sonuç: R1 (Y – –) yok · R2 (N Y –) var · R3 (N N Y) var · R4 (N N N) yok. Kontrol: 4 + 2 + 1 + 1 = 8 ✓.

**Bir koşulun ikiden fazla değeri varsa.** Juice Shop teslimat ücreti tablosunda (av T4) "yöntem" koşulunun üç değeri var (One Day, Fast, Standard). Tam tablo 2 × 3 = 6 sütundur. Birleştirme kuralı aynıdır: Standard hem Deluxe hem normal üyeler için 0 → (Standard, Deluxe –). One Day ve Fast iki grupta farklı ücretli, bu yüzden ayrı kalır → 5 kural.

> **Uyarı:** sadeleştirme test tasarrufu içindir, riskli birleşimleri atlamak için değil. Lab 4'te her birleştirmeyi güvenmeden önce spesifikasyonla kontrol et.

### 6.5 İmkânsız birleşimler

Bazen bir birleşim gerçekte olamaz. Örnek: "Yaş < 18?" ve "Yaş > 65?" ikisi birden Y olamaz. Böyle sütunlar **silinir**, test edilmez ve kapsama hesabına girmez ("feasible" = mümkün sütunlar).

### 6.6 Karar tablosu kapsaması

> **Decision table coverage = test edilen kurallar (sütunlar) ÷ mümkün tüm kurallar × %100**

Sadeleştirilmiş tabloda 4 kural varsa ve 4'ünü test ettiysen %100.

### 6.7 Güçlü ve zayıf yanlar

- **Güçlü:** Birleşimleri unutmaz; spesifikasyon boşluklarını gösterir; test sayısı açık.
- **Zayıf:** Koşul sayısı arttıkça sütun sayısı **üstel** büyür (10 koşul → 1.024 sütun). O zaman sadeleştirme, risk bazlı seçim ya da pairwise gerekir.
- **Myers bağlantısı:** Myers'ın kitabındaki "cause-effect graphing" (neden-sonuç grafiği) yöntemi de sonunda bir karar tablosu üretir. Biz grafiği atlayıp tabloyu doğrudan kuruyoruz.

### Kendini dene (6)

1. 4 koşullu tam bir karar tablosunun kaç sütunu vardır?
2. Hediye paketi: Dijital ürün mü? Üye mi? Doğum günü mü? Kural: üyelere hediye paketi ücretsiz, doğum gününde de herkese ücretsiz — ama dijital ürünlerde asla. Tam tabloyu kur, adım adım sadeleştir ve 2ᵏ kontrolünü yap.
3. Eylemleri aynı olsa (Y N Y) ile (N Y N) birleştirilebilir mi? Neden?

> **Cevaplar:** 1) 2⁴ = 16.
> 2) Tam tablo (C1 dijital, C2 üye, C3 doğum günü): C1 = Y olan sütunlar → yok; C1 = N iken: (Y Y) var, (Y N) var, (N Y) var, (N N) yok. Adım 1: 1+2 → (Y Y –) yok, 3+4 → (Y N –) yok, 5+6 → (N Y –) var; 7 (N N Y) var ve 8 (N N N) yok kalır. Adım 2: (Y Y –) + (Y N –) → (Y – –) yok. Sonuç: (Y – –) yok · (N Y –) var · (N N Y) var · (N N N) yok. Kontrol: 4 + 2 + 1 + 1 = 8 ✓. **4 kural.**
> 3) Hayır. Üç koşulda da farklılar; birleştirme yalnızca tam olarak bir koşul farklıyken yapılır.

---

## 7. Durum geçişi testi (slayt 22–24)

### 7.1 Ne zaman?

Sonuç yalnızca şu anki girdiye değil, **daha önce ne olduğuna** (geçmişe) bağlıysa. Aynı olay, bir durumda geçerli, başka bir durumda yasak olabilir. Örnek: "İptal et" butonu, ödenmiş bir siparişte çalışmalı, kargodaki siparişte çalışmamalı.

Tipik alanlar: siparişler, ödeme akışları, giriş denemeleri (3 yanlış → kilit), oturumlar, ATM, trafik ışığı, belge onay süreçleri.

### 7.2 Parçaları

| Terim | Anlamı | Sipariş örneği |
|---|---|---|
| **State** (durum) | Sistemin bulunduğu koşul | Placed, Paid, Shipped, Delivered, Cancelled |
| **Event** (olay) | Durumu değiştirebilecek şey | pay, ship, deliver, cancel |
| **Transition** (geçiş) | Durum + olay → yeni durum | Placed + pay → Paid |
| **Guard** (koruma koşulu) | Geçişin olması için doğru olması gereken koşul | pay **[card valid]** |
| **Action** (eylem) | Geçiş sırasında yapılan iş | cancel (refund) → para iadesi |
| **Initial state** (başlangıç) | Siyah nokta | Placed |

### 7.3 Diyagram (slayt 22)

```
  ● → [Placed] --pay--> [Paid] --ship--> [Shipped] --deliver--> [Delivered]
          |                 |
        cancel        cancel (refund)
          ↓                 ↓
            [Cancelled]
```

5 durum, 4 olay, **5 geçerli geçiş**: Placed→Paid, Paid→Shipped, Shipped→Delivered, Placed→Cancelled, Paid→Cancelled.

### 7.4 Durum tablosu ve geçersiz geçişler (slayt 23)

Diyagram yalnızca **olabilecekleri** gösterir. **State table** (durum tablosu) her durum × her olay birleşimini gösterir; böylece **olmaması gerekenler** de görünür.

| Durum \ olay | pay | ship | deliver | cancel |
|---|---|---|---|---|
| Placed | Paid | — | — | Cancelled |
| Paid | — | Shipped | — | Cancelled |
| Shipped | — | — | Delivered | — |
| Delivered | — | — | — | — |
| Cancelled | — | — | — | — |

5 × 4 = **20 hücre**: 5 geçerli, **15 geçersiz** ("—"). Geçersiz geçiş = o durumda izin verilmeyen olay; sistem **reddetmeli** ve **aynı durumda kalmalıdır**. Gerçek hayatta en pahalı hatalar burada çıkar: teslim edilmiş siparişe para iadesi, iptal edilmiş siparişi kargolamak, iki kez ödeme.

### 7.5 Kapsama düzeyleri

| Düzey | Ne ister? | Güç |
|---|---|---|
| **All states** (tüm durumlar) | Her durum en az bir kez ziyaret edilir | En zayıf |
| **Valid transitions** (tüm geçerli geçişler) = **0-switch coverage** | Her geçerli geçiş (tablodaki her dolu hücre) en az bir kez | CTFL'de en yaygın hedef |
| **All transitions** (tüm geçişler) | Geçerli + her geçersiz geçiş denenir | En güçlü; güvenlik ve para için önerilir |

> **0-switch** adı şuradan gelir: tek tek geçişler (aralarında 0 ek geçiş) test edilir. **1-switch** art arda iki geçişlik dizileri test etmek demektir (ileri düzey, sınavda nadiren).

### 7.6 Test sayısını hesaplamak (slayt 24)

Önemli: **Bir test birden çok geçişten geçebilir.** Her test başlangıç durumundan (Placed) başlar.

**Soru 1 — Tüm durumları ziyaret etmek için en az kaç test?**
- T1: Placed → pay → Paid → ship → Shipped → deliver → Delivered (4 durum).
- T2: Placed → cancel → Cancelled (5. durum).
- **Cevap: 2 test.** (Cancelled ile Delivered aynı yolda olamaz; ikisi de "son" durum.)

**Soru 2 — Tüm 5 geçerli geçiş için en az kaç test?**
- T1: pay, ship, deliver (3 geçiş).
- T2: Placed → cancel (4. geçiş).
- T3: Placed → pay → Paid → cancel (5. geçiş; pay tekrar eder, sorun değil).
- **Cevap: 3 test.** Sık yanlış cevap: "5" (her geçişe ayrı test).

**Soru 3 — Bir geçersiz geçiş testi:** Placed → pay → ship → **cancel** → sistem reddetmeli, sipariş Shipped kalmalı.

### 7.7 Bir durum geçişi testi nasıl yazılır?

| Adım | Başlangıç durumu | Olay | Beklenen yeni durum | Beklenen çıktı |
|---|---|---|---|---|
| 1 | Placed | pay (geçerli kart) | Paid | ödeme onayı |
| 2 | Paid | cancel | Cancelled | para iadesi |
| 3 | Cancelled | ship | Cancelled (değişmez) | hata: "iptal edilmiş sipariş gönderilemez" |

Her adım için hem **yeni durum** hem **çıktı** kontrol edilir.

### Kendini dene (7)

1. Bir kapının durumları: Open, Closed, Locked. Olaylar: open, close, lock, unlock. Geçerli geçişler: Open–close→Closed, Closed–open→Open, Closed–lock→Locked, Locked–unlock→Closed. Kaç geçersiz geçiş var?
2. Tüm geçerli geçişleri kapsamak için (Open'dan başlayarak) en az kaç test?

> **Cevaplar:** 1) 3 durum × 4 olay = 12 hücre, 4 geçerli → **8 geçersiz**. 2) **1 test** yeter: Open → close → Closed → lock → Locked → unlock → Closed → open → Open. (Tek yol dört geçişin hepsinden geçiyor.)

---

## 8. Pairwise test (slayt 25–27)

> **Not:** yalnızca demo; ara sınavda **yok**. CTFL Foundation'da bunun bir öğrenme hedefi yok (ISTQB bunu Advanced Test Analyst müfredatında öğretir). Ama işte karşına çıkacak.

### 8.1 Sorun: birleşimler çarpılarak büyür (slayt 25)

Juice Shop'un ödeme sayfası her tarayıcı, işletim sistemi, ödeme yöntemi ve teslimat hızıyla çalışmalı:

| Ayar | Değerler | Sayı |
|---|---|---|
| Tarayıcı (Browser) | Chrome, Firefox, Safari, Edge | 4 |
| İşletim sistemi (OS) | Windows, macOS, Android | 3 |
| Ödeme (Payment) | Card, Wallet, Coupon | 3 |
| Teslimat (Delivery) | Standard, Fast | 2 |

**Her tam birleşimi** test etmek 4 × 3 × 3 × 2 = **72 test** ister. Her yeni ayar çarpar: 5 dil eklenirse → 360 test; 3 değerli on ayar → 3¹⁰ = **59.049 test**. Bunu kimse çalıştıramaz.

**İyi haber.** NIST gerçek arızaları inceledi (Kuhn, Wallace ve Gallo, IEEE Transactions on Software Engineering, 2004). İncelenen sistemlerde **arızaların %70'ten fazlası yalnızca bir ya da iki ayarın birlikte olmasıyla tetiklenmiş**, hiçbiri altıdan fazla ayar gerektirmemiş. Tipik örnek: "Safari **ve** Wallet'ta Öde butonu görünmüyor" — dört değil, iki ayar.

Yani: **her değer ikilisini** test edersek, birleşim hatalarının çoğunu çok daha az testle yakalarız.

### 8.2 "İkili" (pair) tam olarak ne? (slayt 26)

Bir **ikili**, bir ayarın bir değeri ile başka bir ayarın bir değeridir: (Safari, Wallet), (Android, Fast), (Edge, Windows)…

Tarayıcı ile ödeme arasında 4 × 3 = **12 ikili** var. Bunları bir ızgara olarak çiz; her hücre en az bir testte yer almalı:

| | Card | Wallet | Coupon |
|---|---|---|---|
| Chrome | ☐ | ☐ | ☐ |
| Firefox | ☐ | ☐ | ☐ |
| Safari | ☐ | ☐ (Safari, Wallet) | ☐ |
| Edge | ☐ | ☐ | ☐ |

Her iki ayar için böyle bir ızgara var (4 ayar için 6 ızgara):

| Ayarlar | İkili sayısı |
|---|---|
| Tarayıcı × OS | 4 × 3 = 12 |
| Tarayıcı × Ödeme | 4 × 3 = 12 |
| Tarayıcı × Teslimat | 4 × 2 = 8 |
| OS × Ödeme | 3 × 3 = 9 |
| OS × Teslimat | 3 × 2 = 6 |
| Ödeme × Teslimat | 3 × 2 = 6 |
| **Toplam** | **53 ikili** |

**İşin püf noktası:** bir test **aynı anda birkaç ikiliyi** kapsar. "Chrome · Windows · Card · Standard" testi 6 ikiliyi kapsar: (Chrome, Windows), (Chrome, Card), (Chrome, Standard), (Windows, Card), (Windows, Standard), (Card, Standard) — her ızgarada bir hücre.

**En az kaç test?** En büyük iki ayarın (Tarayıcı × OS) tek başına 12 ikilisi var ve bir test bunlardan yalnızca birini kapsayabilir. Yani **en az 4 × 3 = 12 test** gerekir. İyi seçilmiş 12 test 53 ikilinin hepsini kapsayabilir.

> **Tam birleşim: 72 test. Tüm ikililer: 12 test.** Daha az test, ama iki ayarlı her hata yine test edilir.

### 8.3 PICT: model girer, test tablosu çıkar (slayt 27)

12 satırı kimse elle seçmez. **PICT**, Microsoft'un ücretsiz komut satırı aracıdır (github.com/microsoft/pict, MIT lisansı).

**Girdi — model dosyası** `checkout.txt`, her satırda bir ayar:

```
Browser:  Chrome, Firefox, Safari, Edge
OS:       Windows, macOS, Android
Payment:  Card, Wallet, Coupon
Delivery: Standard, Fast
```

**Komut:** `pict checkout.txt`

**Çıktı — bir tablo.** Her satır, elle ya da otomatik testte çalıştıracağın **bir test durumudur**. Bu model için bir pairwise kümesi (bu ders için üretildi ve kontrol edildi: 53 ikilinin hepsi var):

| # | Tarayıcı | OS | Ödeme | Teslimat |
|---|---|---|---|---|
| 1 | Chrome | Windows | Card | Standard |
| 2 | Chrome | macOS | Wallet | Standard |
| 3 | Chrome | Android | Coupon | Fast |
| 4 | Firefox | Windows | Wallet | Standard |
| 5 | Firefox | macOS | Coupon | Standard |
| 6 | Firefox | Android | Card | Fast |
| 7 | Safari | Windows | Coupon | Standard |
| 8 | Safari | macOS | Card | Standard |
| 9 | **Safari** | **Android** | **Wallet** | **Fast** |
| 10 | Edge | Windows | Card | Fast |
| 11 | Edge | macOS | Wallet | Fast |
| 12 | Edge | Android | Coupon | Standard |

**Nasıl okunur:** (Safari, Wallet)'ı bul → 9. satır. (Edge, Fast)'ı bul → 10. ve 11. satır. Her ikili bir yerde var. PICT'in kendisi başka satırlar yazdırabilir (rastgele bir başlangıçla çalışır), ama onun tablosu da her ikiliyi kapsar, genelde 12–14 satırda.

**Kısıtlar (constraints).** 9. satıra tekrar bak: **Android'de Safari** gerçekte yok. Modele bir kural ekleriz:

```
IF [OS] = "Android" THEN [Browser] <> "Safari";
```

PICT o imkânsız birleşim olmadan yeni bir tablo üretir (genelde bir iki satır daha fazla). Kısıtlar, testçinin gerçek dünya bilgisini araca aktarma yoludur.

**Daha güçlü seçenek:** `pict checkout.txt /o:3` her değer **üçlüsünü** kapsar (daha çok test, daha çok güvence). Riske göre seç: ödeme sayfası için üçlüler, bir blog için ikililer.

### 8.4 Pairwise ve EP

- **EP**, **tek** bir girdinin değerlerini seçer ("hangi tarayıcılar? hangi tutarlar?").
- **Pairwise**, bu değerlerden **birçok girdinin birleşimlerini** seçer.
- Birlikte çalışırlar: önce her ayar için EP (ve BVA), sonra bunları pairwise ile birleştir.

> **Tek cümle:** her birleşimi değil, her değer ikilisini test et — hataların çoğu yalnızca iki ayarın buluşmasını ister.

---

## 9. Real world 2026: yapay zekâ ve sınırlar (slayt 28–29)

### 9.1 Ne bulundu?

**Çalışma 1 — Ekim 2026 (önyayın).** Shi, Luo, Yang ve Tian, "Beyond Coverage and Mutation: A Blind-Spot Analysis of LLM-Generated Unit Tests", arXiv 2610.32548 (23 Ekim 2026).
- Dört yapay zekâ modeli (GPT-5.4, Gemma-4-31B, Gemini-3.6-flash, Qwen3-Coder-30B), dört farklı prompt stratejisiyle, 895 karmaşık Java fonksiyonu için **14.320 test** yazdı.
- Testlerin sınır durumlarını ne kadar denediği ölçüldü:

| Sınır türü | Kapsanma oranı |
|---|---|
| true / false girdiler | **%93,1** (true), %93,8 (false) |
| boş girdi (empty) | **%38,8** |
| null | **%34,8** |
| negatif sayı | **%23,8** |

- Yazarların özeti: Yapay zekâ, koddan **doğrudan çıkarılabilen** sınırları kapsıyor; düşünmeyi gerektirenlerde zorlanıyor. Ayrıca başarısız testlerin %15,6'sı **yanlış beklenen değerden** (assertion) kaynaklanıyordu.
- **Önemli:** Bu bir **önyayın**; henüz hakem değerlendirmesinden geçmedi. Rakamlar Java fonksiyonları üzerinde; "yapay zekâ her zaman sınırları kaçırır" diye genelleştirilemez.

**Çalışma 2 — 2025.** Rodríguez, Rossi ve Fernandez (arXiv 2505.09830): GPT-4 **sınıfları doğru buldu**, ama test değerlerini "genellikle aralığın ortasına yakın" seçti; insanlar ise kenarlara olabildiğince yakın değerler seçti.

**ISTQB'nin tepkisi:** 2025'te **CT-GenAI** (Certified Tester – Testing with Generative AI) sertifikası çıktı; Nisan 2026'da v1.1 güncellemesi geldi. Test için prompt yazma, üretken yapay zekânın riskleri ve LLM tabanlı test altyapısı konularını kapsar.

### 9.2 Testçi gözüyle yorum

- Yapay zekâ test **yazmayı** (yazma işini) hızlandırır.
- Ama **hangi sınıfların ve sınırların önemli olduğuna** karar vermek, beklenen değerin doğru olup olmadığını kontrol etmek ve "yazılım canlıya çıkabilir" demek hâlâ testçinin işidir.
- Yapay zekâdan "BVA kullan" diye istemek işe yarar; ama bunu isteyebilmek ve cevabı denetleyebilmek için **BVA'yı bilmen** gerekir.
- Lab 4'teki ekibin testleri de tam olarak bu örüntüyü taşıyor: hep orta değerler (3, 10, 20 km; 50 ve 200 TL), hiç kenar yok.

### 9.3 Canlı deneme (slayt 29)

Telefonda herhangi bir ücretsiz LLM'e şu yazılır:

```
Write pytest tests for apple_juice_ok(qty).
Rule: a customer can order 1 to 5 bottles of Apple Juice in one order.
qty is a whole number. Return True if the order is allowed, False otherwise.
```

Kontrol listesi: 0 ve 1'i test etti mi? 5 ve 6'yı? Negatif sayı? Tam sayı olmayan değer (2,5)? Beklenen değerler kurala uyuyor mu? Sonuç ne olursa olsun ders aynı: yapay zekâyı **ancak kendi sınır listemiz olduğu için** değerlendirebildik.

---

## 10. Juice Shop'ta sınır avı (slayt 30–34)

### 10.1 Amaç

Teoriyi gerçek bir uygulamada denemek. **OWASP Juice Shop**, bilerek açıklı bırakılmış, eğitim amaçlı bir online meyve suyu dükkânıdır. Dönem boyu hedefimiz. **Sınıfın adresi: https://juice.armikom.com** (telefonunun tarayıcısında aç).

> **Etik kural:** Bu teknikler yalnızca Juice Shop'ta ya da **yazılı izinle** başka sistemlerde denenir. Gerçek bir siteye izinsiz deneme yapmak suçtur.

### 10.2 Beş hedefin tam cevapları (kaynak: Juice Shop v20.3.0 kodu)

**T1 — Kayıt formu, parola uzunluğu**
- Kural ekranda: "Password must be 5-40 characters long."
- Sınıflar: 0–4 (geçersiz) · 5–40 (geçerli) · 41+ (geçersiz).
- 2 değerli BVA: **0, 4, 5, 40, 41** (0 = boş parola).
- Beklenen: 0 → hata ("Please provide a password."), 4 → hata, 5 → kabul, 40 → kabul, 41 → hata.
- **Bulgu:** Parola alanının altındaki sayaç "x/**20**" gösteriyor, kural ise **40**. (Tekrar alanında "/40" yazıyor.) Ekran ile kural çelişiyor. Bu, testle bulunmuş gerçek bir tutarsızlıktır; hangisinin doğru olduğu ürün sahibine sorulur. Üstelik ekranın arkasında sunucu uzunluğu hiç kontrol etmiyor (bkz. 10.4).

**T2 — Sepet, Apple Juice (1000ml) miktarı**
- Kural: siparişte 1–5 adet.
- Sınıflar: ≤0 (geçersiz) · 1–5 (geçerli) · ≥6 (geçersiz).
- 2 değerli BVA: **0, 1, 5, 6**.
- Gerçek davranış: 1 ve 5 çalışır; 6'da "You can order only up to 5 items of this product."; "−" butonu 1'de durur, 0'a inmez.
- **Açık soru:** Sunucu 0 ya da −1'i de reddediyor mu? (Hafta 11.)

**T3 — Müşteri geri bildirimi (Customer Feedback)**
- Puan (rating): kaydırıcı 1–5 yıldız; puan seçilmeden Submit butonu kapalı.
- Yorum: en fazla 160 karakter; alan zorunlu.
- 2 değerli BVA: puan **0, 1, 5** (6 seçilemez); yorum uzunluğu **0, 1, 160, 161**.
- Gerçek davranış: 161. karakter yazılamaz (alan yazmayı durdurur).
- **Tartışma:** Yazılamayan bir değeri test etmeye değer mi? Evet; ama ekrandan değil, sunucuya giden yoldan (Hafta 11).

**T4 — Karar tablosu: teslimat ücreti**
- Spesifikasyon (dükkânın fiyat listesi): One Day Delivery 0.99, Deluxe üyeler için 0.50 · Fast Delivery 0.50, Deluxe üyeler için ücretsiz · Standard Delivery herkese ücretsiz.
- Koşullar: *Deluxe üye mi?* (E/H) ve *teslimat yöntemi* (One Day, Fast, Standard). Bir koşulu üç değerli bir karar tablosu; tam tablo 2 × 3 = **6 sütun**:

| | 1 | 2 | 3 | 4 | 5 | 6 |
|---|---|---|---|---|---|---|
| Deluxe üye mi? | E | E | E | H | H | H |
| Yöntem | One Day | Fast | Standard | One Day | Fast | Standard |
| **Ücret** | 0.50 | 0 | 0 | 0.99 | 0.50 | 0 |

- Sadeleştirme: 3. ve 6. sütun yalnızca "Deluxe"te farklı ve ikisi de 0 → tek kural "Standard, Deluxe –". Sonuç: **5 kural**.
- Telefonda (giriş → sepet → Checkout → adres → Continue): teslimat sayfası normal hesap için 0.99, 0.50 ve 0.00 gösterir — kuralın dediği gibi.
- **Bulgu:** üç Deluxe sütunu normal hesapla **test edilemez**. Test edilemeyen bir kural özel test verisi ister: burada bir Deluxe test hesabı. Bunu yazmak da iyi testin parçasıdır.

**T5 — Durum geçişleri: ödeme adımları**
- Durumlar: Sepet → Adres seçimi → Teslimat yöntemi → Ödeme → Sipariş özeti → Sipariş tamamlandı.
- Olaylar ve koruma koşulları: *Checkout* [sepet boş değil, giriş yapılmış] · *Continue* [bir adres seçilmiş] · *Continue* [bir teslimat yöntemi seçilmiş] · …
- Geçersiz geçişler ve ekranın yaptığı: boş sepetle Checkout → buton kapalı (reddedilir ✓); adres seçmeden Continue → kapalı (reddedilir ✓); giriş yapmadan Checkout → giriş sayfasına gönderir (koruma ✓).
- Ödeme sayfası kart ister: bu da bir koruma koşulu, Ödeme → Sipariş özeti [ödeme yöntemi seçilmiş]. Derste orada dururuz; gerçek kart asla girilmez.
- **Denenecek:** adres seçmeden doğrudan `juice.armikom.com/#/delivery-method` adresini aç. Kaynak kodda bu sayfanın giriş koruması yok. Yine de açılırsa bu bir bulgudur: adımların sırasını sistem değil, butonlar denetliyor.

### 10.3 "Beklenen sonuç" nereden gelir? (en önemli kural)

Avlanmada en sık yapılan hata: gerçekte ne olduysa onu "beklenen" sütununa yazmak. O zaman her test geçer ve hiçbir şey bulunmaz. **Beklenen sonuç her zaman kuraldan (spesifikasyondan) gelir**, uygulamanın davranışından değil. Test, ikisini karşılaştırmaktır.

### 10.4 Canlı demo: yalnızca tarayıcının kontrol ettiği iki sınır (slayt 34)

**1. Zero Stars.** Juice Shop görevi: dükkâna **0 yıldızlı** geri bildirim vermek.

- **Tarayıcı** sınırı uygular: kaydırıcı en az 1, puan 0 iken Submit kapalı.
- **Sunucu** sınırı kontrol etmez: 0 puanı kaydeder.
- Eğitmen, tarayıcının geliştirici araçlarında Submit butonundaki `disabled` özniteliğini silerek (ya da isteği doğrudan sunucuya göndererek) 0 yıldızlı geri bildirim gönderir.

**2. T1'de test ettiğin parola uzunluğu.** "5–40 karakter" kuralı **yalnızca kayıt formunda** var. Sunucu (`POST /api/Users`) her parolayı kaydeder, tek karakterlik olanı bile. Ekranda kural kusursuz çalışır; ekranın arkasında hiç kural yoktur.

**Ders:** Kapalı bir buton ya da kırmızı bir hata mesajı bir **kural** değildir. Sınırı, **kararın gerçekten verildiği yerde** (sunucuda) test et. 11. haftada bunu araçlarla test edeceğiz; bugün avın ekranda kalıyor.

---

## 11. Teknikleri birlikte kullanmak: Myers'ın stratejisi (slayt 37)

### 11.1 Myers'ın stratejisi (slayt 37)

Myers test tasarımı bölümünü basit bir mesajla bitirir: **tek bir teknik yetmez.** Her biri diğerlerinin kaçırdığı testleri ekler. Önerdiği çalışma sırası:

1. Spesifikasyonda **koşul kombinasyonları** varsa onlarla başla. Myers bir cause-effect grafiği çizip onu karar tablosuna çevirir; biz **karar tablosunu** doğrudan kuruyoruz.
2. **Her durumda BVA yap**, girdilerde **ve** çıktılarda (örneğin tam 0 TL ücret).
3. Eksik kalan her sınıf için **EP** testleri ekle, geçerli ve geçersiz.
4. **Deneyime dayalı** testler ekle: **error guessing** (9. hafta).
5. **Kod kapsamını** kontrol et, hiçbir testin ulaşmadığı dallar için test ekle (9. hafta).

1–3. adımlar bu haftanın konusu; 4–5. adımlar 9. haftada (yapı tabanlı ve deneyim tabanlı teknikler).

**Myers neden EP ile değil de tabloyla başlıyor?** Karar tablosunun sütunları zaten birçok sınıfı ve sınırı içerir; BVA ve EP sonra yalnızca eksik kalanı ekler. Yeni başlayanlar için önce sınıfları listelemek genelde daha kolaydır (aşağıdaki sıra gibi). İki sıra da sonunda aynı testlere varır; önemli olan bütün teknikleri kullanmak.

### 11.2 Spesifikasyonun sözcüklerinden seçmek

| Spesifikasyonda gördüğün | Kullanacağın teknik |
|---|---|
| Bir değer aralığı, liste ya da biçim | **EP** (sınıflar) |
| Sıralı bir aralığın kenarları (sayı, uzunluk, tarih, para) | **BVA** (EP'nin üstüne) |
| "Eğer A ve B ise X, değilse Y" gibi birleşik kurallar | **Karar tablosu** |
| "Önce şu olmalı, sonra bu", durumlar ve izin verilen/yasak adımlar | **Durum geçişi** |
| Çok sayıda bağımsız ayarın birleşimi (tarayıcı, OS, dil…) | **Pairwise** |

**Bir test birden çok tekniğe hizmet edebilir.** Sinema örneğinde (bölüm 6, "Kendini dene"), "yaş 13, öğrenci kartı var, çarşamba değil" testi hem yaş sınıflarının bir **sınır değeridir** (çocuk 0–12 yaş ise: 12 | 13) hem bir **karar tablosu kuralıdır** (çocuk değil, öğrenci kartı → indirim).

**İyi bir çalışma sırası (Lab 4 için de):**

1. Spesifikasyonu oku; her sayıyı ve her "or more / more than / up to" ifadesini işaretle.
2. EP: her girdinin geçerli ve geçersiz sınıflarını listele.
3. BVA: her sıralı sınıfın sınır değerlerini, **kuraldan gelen** beklenen sonuçla birlikte listele.
4. Karar tablosu: sonuç birkaç koşula birlikte bağlıysa önce tam tabloyu kur, sonra dikkatle sadeleştir.
5. Her satırı bir teste çevir. Beklenenle gerçeği karşılaştır. Kırmızı test bir bulgudur (ya da yanlış bir beklenen değer: kuralı tekrar kontrol et).

---

## 12. Çıkış bileti ve ara sınav tuzakları (slayt 38, 40)

### 12.1 Çıkış bileti cevapları (açıklamalı)

1. **1–100 tam sayı, 2 değerli BVA → b) 0, 1, 100, 101.** Tüm sınır değerler. (c)'de gereksiz değerler var (2, 99) ama tam 3 değerli de değil (−1 ve 102 eksik); (a) yalnızca geçerli taraf; (d) EP gibi.
2. **Parola 5–40 karakter, kaç sınıf? → c) 3.** Çok kısa, geçerli, çok uzun.
3. **3 evet/hayır koşul, kaç sütun? → c) 8.** 2³ = 8.
4. **Geçersiz geçiş testi → b) Shipped bir siparişi iptal etmek.** Diğerleri modelde izin verilen geçişler.

### 12.2 Ara sınavdaki tipik tuzaklar

| Tuzak | Doğrusu |
|---|---|
| "2 değerli" ile "3 değerli"yi karıştırmak | Soruyu oku; şıklarda ikisi de olur |
| 3 değerlide geçersiz sınıfların sınırlarını unutmak | CTFL: **her** sınır değerin iki komşusu (1–10 → −1, 0, 1, 2, 9, 10, 11, 12) |
| Uzunlukta 0'ı unutmak | Uzunluk 0'dan küçük olamaz: **0 da bir sınır değerdir** (boş metin) |
| Para sınırında 149 yazmak | Adım 0,01 → **149,99** |
| "More than 150" ile "150 or more"u karıştırmak | "More than" 150'yi **dışarıda** bırakır: sınır 150,00 \| 150,01 |
| Geçersiz sınıfları saymayı unutmak | 5–40 kuralında **3** sınıf var, 1 değil |
| İki geçersiz değeri aynı teste koymak | Masking: geçersizler tek tek |
| Karar tablosunda 2ⁿ yerine 2n yazmak | 3 koşul → 8, 4 koşul → 16 |
| Karar tablosunda T / F görünce şaşırmak | T = Y (doğru), F = N (yanlış); N/A = imkânsız birleşim |
| Durum geçişinde "her geçişe bir test" | Bir test birçok geçişten geçebilir |
| Kapsama yüzdesini ters bölmek | Test edilen ÷ toplam × %100 |
| BVA'yı sırasız değerlere (renkler) uygulamak | BVA yalnızca **sıralı** sınıflar için |

---

## 13. Terimler sözlüğü (EN–TR)

| İngilizce terim | Türkçe karşılığı | Kısa açıklama |
|---|---|---|
| Black-box technique | Kara kutu tekniği | Spesifikasyona dayanır, koda bakmaz |
| Specification-based | Spesifikasyon tabanlı | Kara kutunun diğer adı |
| White-box / structure-based | Beyaz kutu / yapı tabanlı | Koda dayanır |
| Experience-based | Deneyim tabanlı | Testçinin bilgisine dayanır |
| Exhaustive testing | Kapsamlı (her şeyi) test | İmkânsızdır |
| Equivalence partition (class) | Denklik sınıfı | Aynı davranış beklenen değer grubu |
| Valid / invalid partition | Geçerli / geçersiz sınıf | Kabul / ret beklenen değerler |
| Representative value | Temsilci değer | Sınıftan seçilen test değeri |
| Masking | Maskeleme | Bir hatanın diğerini gizlemesi |
| Boundary value | Sınır değer | Sıralı bir sınıfın ilk/son değeri |
| 2-value BVA | 2 değerli sınır değer analizi | Tüm sınır değerler (her sınır + komşu sınıftaki en yakın komşu) |
| 3-value BVA | 3 değerli sınır değer analizi | Her sınır değer + iki komşusu |
| Off-by-one error | Bir eksik/fazla hatası | `<` ile `<=` karışması gibi |
| Step size | Adım büyüklüğü | Komşu değerler arası fark (1, 0,01…) |
| Overflow | Taşma | Değerin veri tipine sığmaması |
| Coverage item | Kapsama öğesi | Testi istenen öğe |
| Coverage | Kapsama | Test edilen ÷ toplam öğe |
| Decision table | Karar tablosu | Koşul–eylem tablosu |
| Condition / action | Koşul / eylem | Y/N sorusu / sonuç |
| Rule (column) | Kural (sütun) | Bir koşul birleşimi |
| Don't care ("–") | Fark etmez | Sonucu değiştirmeyen koşul |
| Collapse | Sadeleştirme | Aynı eylemli sütunları birleştirmek |
| Feasible | Mümkün | Gerçekte olabilen birleşim |
| State | Durum | Sistemin bulunduğu koşul |
| Event | Olay | Durumu değiştirebilen şey |
| Transition | Geçiş | Durum + olay → yeni durum |
| Guard condition | Koruma koşulu | Geçişin şartı |
| Invalid transition | Geçersiz geçiş | İzin verilmeyen olay; reddedilmeli |
| State table | Durum tablosu | Durum × olay tablosu |
| 0-switch coverage | 0-switch kapsaması | Tüm geçerli tekil geçişler |
| Pairwise testing | İkili birleşim testi | Her değer çifti en az bir kez |
| Preprint | Önyayın | Hakem değerlendirmesinden geçmemiş makale |

---

## 14. Büyük alıştırma seti (cevaplı)

Ara sınava hazırlık için. Önce cevaplara bakmadan çöz.

**S1.** Bir alan 1 ile 10 arası tam sayı kabul ediyor (alt ve üst ucu açık geçersiz sınıflar). 3 değerli BVA değerleri nelerdir?
a) 0, 1, 10, 11 b) 0, 1, 2, 9, 10, 11 c) −1, 0, 1, 2, 9, 10, 11, 12 d) 1, 5, 10

**S2.** Bir dükkân "en az 200,00 TL" alışverişte hediye veriyor (2 ondalık). Hangi çift sınırı en iyi test eder?
a) 199 ve 200 b) 199,99 ve 200,00 c) 200,00 ve 200,01 d) 100,00 ve 300,00

**S3.** Bir sıcaklık alarmı: <0 °C "donma", 0–30 °C "normal", >30 °C "sıcak" (tam sayı). EP ile %100 kapsama için en az kaç test?
a) 1 b) 2 c) 3 d) 6

**S4.** Aynı alarm için 2 değerli BVA kaç değer verir?
a) 2 b) 3 c) 4 d) 6

**S5.** 5 Boolean koşullu tam bir karar tablosunun kaç sütunu vardır?
a) 5 b) 10 c) 25 d) 32

**S6.** Sadeleştirilmiş bir karar tablosunda 6 kural var. Testler 4 kuralı kapsıyor. Kapsama?
a) %40 b) %60 c) %66,7 d) %75

**S7.** Hangi durumda karar tablosu en uygundur?
a) Tek bir sayısal aralık b) Sonucun birkaç koşulun birleşimine bağlı olduğu kurallar c) Bir sistemin geçmişine bağlı davranış d) Tarayıcı uyumluluğu

**S8.** Bir test, yaşı −5 ve e-postayı "abc" olarak giriyor; yalnızca e-posta hatası görülüyor. Bu durumun adı?
a) Overflow b) Masking c) Off-by-one d) Guard

**S9.** Bir ATM'nin durumları: Idle, CardInserted, PinOK. Geçerli geçişler: Idle–insert→CardInserted, CardInserted–pinOK→PinOK, CardInserted–eject→Idle, PinOK–eject→Idle. Hangisi geçersiz geçiştir?
a) Idle + insert b) CardInserted + eject c) Idle + pinOK d) PinOK + eject

**S10.** S9'daki tüm geçerli geçişleri (Idle'dan başlayan testlerle) kapsamak için en az kaç test?
a) 1 b) 2 c) 3 d) 4

**S11.** Hangisi kara kutu tekniği **değildir**?
a) Sınır değer analizi b) Durum geçişi c) Branch coverage d) Karar tablosu

**S12.** "Mesaj 160 karakterden fazla olamaz; boş mesaj gönderilemez." Uzunluk için 2 değerli BVA?
a) 0, 160 b) 0, 1, 160, 161 c) 1, 160 d) 0, 1, 2, 159, 160, 161

### Cevaplar ve açıklamalar

| Soru | Cevap | Açıklama |
|---|---|---|
| S1 | **c** | Sınır değerler 0, 1, 10, 11; her birine iki komşusu eklenir → −1…2 ve 9…12. (b) eski kitaplardaki eksik hâl |
| S2 | **b** | "En az" → 200 dahil; adım 0,01 → komşu 199,99 |
| S3 | **c** | 3 sınıf: <0, 0–30, >30 |
| S4 | **c** | 2 kenar (−1\|0 ve 30\|31) × 2 = 4 değer: −1, 0, 30, 31 |
| S5 | **d** | 2⁵ = 32 |
| S6 | **c** | 4 ÷ 6 = %66,7 |
| S7 | **b** | Birleşik koşullar karar tablosunun alanıdır; (c) durum geçişi, (d) pairwise |
| S8 | **b** | Bir hata diğerini gizledi |
| S9 | **c** | Idle'da PIN onayı olamaz |
| S10 | **b** | T1: Idle→insert→CardInserted→pinOK→PinOK→eject→Idle (3 geçiş). T2: Idle→insert→CardInserted→eject→Idle (4. geçiş) |
| S11 | **c** | Branch coverage beyaz kutu (yapı tabanlı) tekniğidir |
| S12 | **b** | İki kenar: 0\|1 (boş geçersiz) ve 160\|161 |

---

*Kaynaklar: Myers, The Art of Software Testing, 3. baskı, Böl. 4 · ISTQB® CTFL Syllabus v4.0.1, 4.1–4.2 (sadeleştirilmiş, kaynak gösterilerek) · Ammann & Offutt, Böl. 6 (pairwise) · ESA Ariane 501 soruşturma raporu (1996) · OWASP Juice Shop v20.3.0 (MIT) · Shi ve ark., arXiv 2610.32548 (2026, önyayın) · Rodríguez ve ark., arXiv 2505.09830 (2025) · ISTQB CT-GenAI v1.1 (2026). Hazırlanma: 2026-11-01.*
