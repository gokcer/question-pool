# SENG 403 · Week 7 · Specification-Based Testing — Learning Guide (parallel to the slides)

**Gökçer Gökdal** · Deck: `SENG403_Week7_SpecBasedTesting.pptx` (37 slides)

> **What is this?** This guide teaches the whole topic of the lecture, so you can learn it without a textbook or other sources. The sections follow the slides; each section says which slides it belongs to. It is for the instructor's preparation and for students who study alone. A Turkish version is in the same folder: `SENG403_Week7_Ogrenme_Rehberi_TR.md`.
>
> Every main section ends with **"Check yourself"** questions and answers.

---

## Contents

0. [The one sentence of this week](#0-the-one-sentence-of-this-week)
1. [Disaster of the week: Zune 30 and Ariane 5 (slides 3–5)](#1-disaster-of-the-week-zune-30-and-ariane-5-slides-35)
2. [Why do we need techniques? Exhaustive testing is impossible](#2-why-do-we-need-techniques-exhaustive-testing-is-impossible)
3. [Three families of techniques (slide 7)](#3-three-families-of-techniques-slide-7)
4. [Equivalence partitioning — EP (slides 8–11)](#4-equivalence-partitioning--ep-slides-811)
5. [Boundary value analysis — BVA (slides 12–15)](#5-boundary-value-analysis--bva-slides-1215)
6. [Decision tables (slides 17–21)](#6-decision-tables-slides-1721)
7. [State transition testing (slides 22–24)](#7-state-transition-testing-slides-2224)
8. [Pairwise testing (slides 25–27)](#8-pairwise-testing-slides-2527)
9. [Real world 2026: AI and boundaries (slides 28–29)](#9-real-world-2026-ai-and-boundaries-slides-2829)
10. [Boundary hunting in Juice Shop (slides 30–34)](#10-boundary-hunting-in-juice-shop-slides-3034)
11. [Using the techniques together: Myers' strategy (slide 37)](#11-using-the-techniques-together-myers-strategy-slide-37)
12. [Exit ticket and midterm traps (slides 38, 40)](#12-exit-ticket-and-midterm-traps-slides-38-40)
13. [Glossary (EN–TR)](#13-glossary-entr)
14. [Big practice set (with answers)](#14-big-practice-set-with-answers)

---

## 0. The one sentence of this week

> **Bugs live at the boundaries.**

A program can receive an almost endless number of values. We cannot try them all. The four techniques of this lecture answer one question: **"Which few values give me the best chance to find a bug?"**

| Technique | Question it answers |
|---|---|
| Equivalence partitioning (EP) | Which groups of values does the program treat **the same way**? One value per group is enough. |
| Boundary value analysis (BVA) | Where are the **edges** of these groups? Bugs live there. |
| Decision table | If the result depends on a **combination** of conditions, which combinations exist? |
| State transition | If the result depends on **what happened before**, which steps are allowed and which are forbidden? |

All four are **black-box** techniques: we design the tests by reading the **specification** (the rules, the requirements), not the code.

---

## 1. Disaster of the week: Zune 30 and Ariane 5 (slides 3–5)

### 1.1 Zune 30 (slides 3–4)

**What happened?** The Zune was Microsoft's music player (a competitor of the iPod). At midnight on 31 December 2008, every 30 GB Zune in the world froze on the start-up screen. Microsoft's solution: "Wait until the battery is empty, then charge it after 1 January." It worked.

**Why?** 2008 was a **leap year**: 366 days. So 31 December 2008 was **day 366** of the year.

The clock chip stored the date as "days since 1 January 1980". The code that turned this number into a year (a driver from Freescale, the chip maker) was:

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

**Step by step.** Each turn of the loop removes the days of one year. When we reach 2008, look at the days left:

| Days left (`days`) | `days > 365`? | Leap year? | `days > 366`? | What happens? |
|---|---|---|---|---|
| 365 (30 Dec) | no | – | – | loop ends ✓ |
| **366 (31 Dec)** | **yes** | **yes** | **no** | **nothing: `days` does not change, back to the top → forever** |
| 367 (next year) | yes | yes | yes | subtract 366 ✓ |

At 366 the outer condition says "go on" and the inner condition says "do not subtract". The program can neither move forward nor leave: an **infinite loop**.

**Why is it a boundary bug?** The code has two thresholds: 365 and 366. The bug appears at **the one value between them**, 366. "The last day of a leap year" is the last value of a partition: a **boundary value**. Boundary value analysis tells us: in a leap year, test 365, 366 and 367. One test with 366 freezes the player and the bug is found.

**The fix idea:** in a leap year, leave the loop when `days == 366` (day 366 is still in the same year). The programmer thought about "more than 366" and forgot "exactly 366": a classic **off-by-one** error.

### 1.2 Ariane 5 (slide 5)

**What happened?** 4 June 1996, the first flight of the European Space Agency's new rocket, Ariane 5. About 37 seconds after launch the rocket turned off course and broke up. Nobody was on board; the rocket and its satellites were lost.

**Why?** The navigation software was **reused from Ariane 4**, which had flown without problems. It converted a 64-bit decimal number (related to horizontal speed) into a **16-bit signed integer**. A 16-bit signed integer can be at most **32,767**. Ariane 4 was slow; the value always stayed below this limit. Ariane 5 was much faster; the value went above 32,767 and the conversion failed (**overflow**). The backup computer had **the same software**, so it failed in the same second.

**Lesson:** Boundaries are not only in the words of the specification; they are also in **data types** (16 bits, 32 bits, the maximum length of a database column…). When the input range changes (a new, faster rocket), test the old boundaries again. This also connects to **regression testing**, which comes later.

### Check yourself (1)

1. With which single test value would you have found the Zune bug?
2. In Ariane 5, did the boundary come from the specification or from a data type?

> **Answers:** 1) `days = 366` in a leap year. 2) From the data type: the maximum of a 16-bit signed integer, 32,767.

---

## 2. Why do we need techniques? Exhaustive testing is impossible

This is the second of the **7 testing principles** from week 6: *exhaustive testing is impossible*.

A simple example: in Juice Shop a password can have 5–40 characters. A keyboard has about 95 printable characters. The number of 5-character passwords alone is 95⁵ ≈ **7.7 billion**. Testing them all is not possible.

Even for the Lab 4 delivery fee: km 0–30 (31 values) × total 0.01–1,500.00 TL (150,000 values) × Deluxe yes/no (2) ≈ **9.3 million** combinations.

So we use **test design techniques**. A technique gives us two things:

1. **Which values** to choose (few, but smart),
2. **Why** we chose them (a reason we can explain to a manager or an auditor) and a **coverage** measure that shows how much we tested.

> **Coverage item:** the thing a technique wants a test for. In EP each **partition**, in BVA each **boundary value**, in a decision table each **rule (column)**, in state transition testing each **transition** is a coverage item. Coverage always uses the same formula:
>
> **Coverage = coverage items tested ÷ all coverage items × 100 %**

---

## 3. Three families of techniques (slide 7)

ISTQB CTFL (section 4.1) puts test techniques into three families:

| Family | Tests come from… | Example techniques | When? |
|---|---|---|---|
| **Black-box** = **specification-based** | what the software **must do**: requirements, user stories, business rules, models | EP, BVA, decision tables, state transitions | even before the code exists |
| **White-box** = **structure-based** | the **inside** of the software: lines and branches of code | statement coverage, branch coverage | when there is code (week 9) |
| **Experience-based** | the tester's **knowledge and experience** | error guessing, exploratory testing, checklist-based testing | always; it completes the others (week 9) |

**Two big advantages of black-box testing:**

- Tests can be designed **before the code is written** (the specification is enough). This follows the principle "early testing saves time and money".
- If the code is rewritten, the tests **stay valid**, because they do not depend on the inside.

**Weakness:** it cannot find something the specification does not mention (a hidden feature, a forgotten case). If the specification is wrong, the expected results are wrong too. That is why we use it together with the other families.

---

## 4. Equivalence partitioning — EP (slides 8–11)

### 4.1 The idea

**Equivalence partitioning** splits the input (or output) values into groups that the program should treat **the same way**. Each group is a **partition** (Myers' older name: *equivalence class*).

The basic assumption: if one value of a partition works, the other values of the same partition work too; if one fails, the others probably fail the same way. So **one test per partition is enough**.

### 4.2 Worked example: the Juice Shop password (slide 8)

Rule: **"Password must be 5–40 characters long."**

How many different behaviours are there for the **length** of the password?

| Partition | Length | Valid? | Expected behaviour | Example value |
|---|---|---|---|---|
| P1 | 0–4 | **invalid** | error message | "abc" (3) |
| P2 | 5–40 | **valid** | accepted | "juice2026" (9) |
| P3 | 41 or more | **invalid** | error message | 50 × "a" |

Three partitions → **three tests**. With these three tests EP coverage is 3/3 = **100 %**.

Note: P1 and P3 show the same kind of error, but they are **different partitions**, because they break different parts of the rule (the lower and the upper limit). The software could check the lower limit correctly and the upper one wrongly; one single "invalid" partition would hide that.

### 4.3 How do you find partitions? (step by step)

1. **List the inputs.** Every field, parameter, file, even the time and date is an input.
2. **Read the rule of each input.** "5–40", "at most 5", "yes/no", "Gold/Silver/Normal"…
3. **Split by the rule:**

| Kind of rule | Partitions | Example |
|---|---|---|
| A range (min–max) | 1 valid + 2 invalid (below, above) | age 18–65 → <18, 18–65, >65 |
| Only a lower limit ("at least") | 1 valid + 1 invalid | basket ≥ 1 → ≤0, ≥1 |
| A list of options | each option is 1 valid partition + 1 invalid ("not in the list") | payment: Card, Wallet, Coupon, (other) |
| Boolean (yes/no) | 2 partitions | Deluxe member: yes, no |
| "Must have this format" | 1 valid + each way the format can break | email: valid, no @, no domain |

4. **Think about outputs too.** Different outputs mean different partitions (e.g. fee = 0, half, full).
5. **Check:** do the partitions **not overlap** (no value in two partitions) and leave **no gaps** (every possible value is in a partition)?

### 4.4 Four rules (slide 9)

1. **Every input has valid and invalid partitions.** Beginners forget the invalid ones, but real users and attackers enter invalid values all the time.
2. **Partitions do not overlap and leave no gap.** Every value belongs to exactly one partition.
3. **One test per partition is enough.** Any value inside it can be chosen.
4. **Invalid values: one at a time.** Be careful here:

**What is masking?** One test contains two invalid values: a 3-character password **and** an invalid email. The program says only "email is invalid". Did the password check work? We cannot know: maybe it never ran, but the email error came first and hid it. So each test contains **only one invalid value**; the other inputs stay valid.

> **Valid partitions can be combined:** one test may cover the valid partitions of several inputs at the same time (valid password + valid email + valid security answer). This reduces the number of tests.

### 4.5 Several inputs: "each choice"

With several inputs, the usual CTFL expectation is that every partition of every input is tested **at least once** (*each choice coverage*). Example:

- Input A: 3 partitions, input B: 2 partitions.
- Minimum number of tests = the largest number of partitions = **3** (each test takes one partition of A and one of B). But invalid partitions must be in separate tests (masking rule), so in practice a few more tests are needed.

We do not need all combinations (3 × 2 = 6). If combinations matter, use a decision table (section 6) or pairwise testing (section 8).

### 4.6 EP coverage

> **EP coverage = partitions tested ÷ partitions identified × 100 %**

If you tested 2 of the 3 password partitions, coverage is 66.7 %. A typical midterm question.

### 4.7 The Apple Juice exercise (slides 10–11)

Rule: a customer can order **1 to 5** bottles of Apple Juice (1000ml) in one order. The quantity is a whole number.

| Partition | Values | Valid? | Value | Real Juice Shop behaviour |
|---|---|---|---|---|
| Q1 | 0 or less | invalid | 0 | cannot be entered on screen: the "−" button stops at 1 |
| Q2 | 1–5 | valid | 3 | added to the basket |
| Q3 | 6 or more | invalid | 8 | "You can order only up to 5 items of this product." |

**3 partitions → 3 tests for 100 % EP coverage.**

**A deeper question ("Think about" on the slide):** "I cannot enter −1 on the screen, so I do not need to test it." True? **No.** If the browser blocks something, that does not mean the server blocks it. An attacker can send a request directly to the server. Juice Shop has a challenge for this ("Payback Time": place an order with a negative quantity and get rich). We come back to it in week 11 (security). Today's lesson: **test the boundary where the decision is really made.**

**Stock is a boundary too:** if only 3 bottles are left, the valid range becomes 1–3. Boundaries do not have to be fixed; a "moving boundary" must be tested as well.

### 4.8 The limit of EP → why we need BVA

EP lets us choose 9 characters and 3 characters for the password. But what if the developer wrote `length > 5` instead of `length >= 5`? 9 characters work, 3 characters are rejected correctly: **both EP tests pass and the bug escapes.** The bug appears only at exactly 5 characters. That is why we need BVA.

### Check yourself (4)

1. Write the partitions and example values for "age must be 18–65, whole number".
2. Payment method is chosen from a list: Card, Wallet, Coupon. How many partitions?
3. A test enters age = 10 and payment = "Bitcoin". What is wrong?

> **Answers:** 1) <18 (e.g. 10, invalid), 18–65 (e.g. 30, valid), >65 (e.g. 80, invalid). 2) 3 valid (one per option) + 1 invalid (not in the list) = 4. 3) Two invalid values in one test: masking. Split into two tests.

---

## 5. Boundary value analysis — BVA (slides 12–15)

### 5.1 The idea

Developers make mistakes at the **edges**, not in the middle:

- `<` instead of `<=` (or the other way round),
- 5 instead of 6 (**off-by-one**),
- a loop that runs once too often or once too little (Zune),
- a wrong constant (20 instead of 40).

**Boundary value:** the **minimum** or **maximum** value of an **ordered** partition. BVA works only where the values can be put in order: numbers, lengths, dates, times. (For an unordered list such as colours, BVA makes no sense.)

### 5.2 Two versions: 2-value and 3-value

First find the **boundary values**: the **minimum** and **maximum** of every partition. Invalid partitions have boundary values too. If one end of a partition is open (e.g. "41 or more" has no maximum), there is no boundary value at that end. But if there is a **natural limit** (a length cannot be less than 0), that is a boundary value too.

The password 5–40 example. The partitions of the length: **0–4** (invalid) · **5–40** (valid) · **41+** (invalid).

```
 length:    0   1   2   3   4 | 5   6  ...  39  40 | 41  42  ...
 partition: too short (0–4)   | valid (5–40)       | too long (41+)
 boundary values: 0 and 4       5 and 40             41 (no upper end)
```

Boundary values: **0, 4, 5, 40, 41**. (0 = an empty password. Easy to forget, and very important in real life.)

CTFL v4 defines two versions:

**2-value BVA (CTFL default):** each boundary value is a coverage item; for each boundary value, its closest neighbour in the next partition is itself a boundary value. So 2-value BVA = **test all boundary values**.

**3-value BVA:** each boundary value **and both its neighbours**. A practical rule: **take the 2-value list and add the value just below and just above each value** (drop impossible ones, such as a length of −1).

| Version | Test values | Number of tests |
|---|---|---|
| 2-value | **0, 4, 5, 40, 41** | 5 |
| 3-value | **0, 1, 3, 4, 5, 6, 39, 40, 41, 42** | 10 |

**With open-ended partitions (whole numbers, no natural limit):** for "age 18–65, whole number" the partitions are ≤17 · 18–65 · ≥66; the first has no lower end, the last no upper end. Boundary values: 17, 18, 65, 66. 2-value: **17, 18, 65, 66**. 3-value: **16, 17, 18, 19, 64, 65, 66, 67**.

> **This is exactly how the official ISTQB sample exam counts** (CTFL v4.0 Sample Exam A: password length 6–12 → 2-value 0, 5, 6, 12, 13; 3-value 0, 1, 4, 5, 6, 7, 11, 12, 13, 14). Some older books apply 3-value only to the edges of the valid partition (4, 5, 6, 39, 40, 41); in a CTFL exam **that is incomplete**.

**Remember:**
- Boundary value = the first and last value of every partition (invalid partitions and natural limits included).
- 2-value = all boundary values.
- 3-value = all boundary values + both neighbours of each.

### 5.3 Why does 3-value BVA exist?

A defect that 2-value misses: the developer wrote `length == 5` instead of `length >= 5`.

| Test | 2-value result | 3-value result |
|---|---|---|
| 4 → must be rejected | rejected ✓ | rejected ✓ |
| 5 → accepted | accepted ✓ | accepted ✓ |
| 6 → accepted | (not tested) | **rejected ✗ → bug found** |

So 3-value BVA needs a few more tests but finds a few more defects. Use it for risky inputs (money, safety, limits).

### 5.4 BVA coverage

> **BVA coverage = boundary values tested ÷ boundary values identified × 100 %**

The password has 5 boundary values with 2-value BVA (0, 4, 5, 40, 41). If you tested 4, 5 and 40, coverage is 3/5 = 60 %.

### 5.5 Where do boundaries hide? (slide 13)

**1. In the words of the specification.** The most important words:

| Phrase | Meaning | Boundary |
|---|---|---|
| "150 TL **or more**" / "at least 150" | 150 included | 149.99 \| 150.00 |
| "**more than** 150" | 150 not included | 150.00 \| 150.01 |
| "**up to** 5" / "at most 5" | 5 included | 5 \| 6 |
| "**between** 5 **and** 40" | usually included, but **unclear!** | Ask! |

If "between" is unclear, that is a **defect in the specification**; a tester asks. (Week 6: this is where static testing begins.)

**2. In the step size.** The neighbour of a value is "one step" away:

- whole numbers: step 1 → the neighbours of 5 are 4 and 6,
- money with 2 decimals: step 0.01 → **the neighbour of 150.00 is 149.99, not 149!**
- time: one second (or whatever the system measures),
- dates: one day → the neighbour of 31 December is 1 January.

**3. In dates.** Day 365/366 (Zune), 28/29 February, the last day of a month, the end of the year, time zones, daylight saving time. **The 2038 problem:** many 32-bit systems store time as "seconds since 1 January 1970". A 32-bit signed number overflows on 19 January 2038: the big brother of the Zune bug.

**4. In data types and sizes.** 16 bits = 32,767 (Ariane), 32 bits ≈ 2.1 billion, an empty text (length 0), an empty list, the maximum length of a database column.

**5. In outputs.** Myers recommends boundaries on the output side too: test the inputs that produce the smallest and the largest output. For example a delivery fee of exactly **0 TL**, the largest discount, the maximum number of results on one page.

### 5.6 Worked example: grades

Rule: `grade(score)`, score is a whole number 0–100. 0–49 → FF, 50–69 → CC, 70–84 → BB, 85–100 → AA. Any other score raises an error.

**Step 1 — Partitions (EP):** <0 · 0–49 · 50–69 · 70–84 · 85–100 · >100 → **6 partitions**.

**Step 2 — Edges:** −1|0 · 49|50 · 69|70 · 84|85 · 100|101 → **5 edges**.

**Step 3 — 2-value BVA:** −1, 0, 49, 50, 69, 70, 84, 85, 100, 101 → **10 values**.

**Step 4 — Expected results:** −1 → error, 0 → FF, 49 → FF, 50 → CC, 69 → CC, 70 → BB, 84 → BB, 85 → AA, 100 → AA, 101 → error.

**Why both techniques?** If the code says `if score > 85: return "AA"`: the EP value 90 → AA (passes), but the BVA value 85 → BB (**bug found**). EP finds the partitions, BVA tests their edges.

> **Counting shortcut:** for whole numbers with open ends, n neighbouring partitions have (n−1) edges, and 2-value BVA gives 2 × (n−1) values. Grades: 6 partitions → 5 edges → 10 values. (Here the score has no natural lower limit: −5 is also in "below 0".)

### 5.7 The CTFL question on slides 14–15

"Age 18–65, whole number. Which set gives 100 % coverage with 2-value BVA?"

- a) 18, 65 → only the valid side; 17 and 66 are missing → 50 %.
- **b) 17, 18, 65, 66 → correct.** All boundary values.
- c) 17, 18, 19, 64, 65, 66 → has extra values (19, 64) that 2-value does not need; and it is not full 3-value either (16 and 67 are missing). The question asks for 2-value.
- d) 0, 18, 40, 65, 100 → looks like EP; 17 and 66 are missing.

**Lesson:** in the exam always read whether it says "2-value" or "3-value".

### Check yourself (5)

1. Basket quantity 1–5. Write the 2-value and 3-value BVA values.
2. "Free delivery if the order is more than 50.00 TL" (2 decimals). Which two values test the boundary?
3. A text field has at most 160 characters. Which lengths do you test (2-value)? If the field is required, what is the lower boundary?

> **Answers:** 1) Partitions ≤0 · 1–5 · ≥6 (open lower end: −1, −2… are also "≤0"). 2-value: 0, 1, 5, 6. 3-value: −1, 0, 1, 2, 4, 5, 6, 7. 2) "More than" → 50.00 not included: **50.00 | 50.01**. 3) 160 | 161. If required, also 0 | 1 (empty text invalid, 1 character valid).

---

## 6. Decision tables (slides 17–21)

### 6.1 When?

EP and BVA look at **one input at a time**. If the result depends on a **combination** of conditions (logged in **and** basket empty **and** address saved), testing inputs one by one misses combinations. A **decision table** lists all combinations systematically.

Business rules, pricing, insurance, campaigns and permission checks are the natural home of decision tables.

### 6.2 Parts and notation

| Part | Meaning |
|---|---|
| **Conditions** | yes/no questions: "Logged in?" |
| **Actions** | results: "go to login", "go to payment" |
| **Rule** = one **column** | one combination of conditions and its action(s) |
| **"–"** | "don't care": this condition does not change the result in this rule |

**Notation in the exam.** In this course we write **Y** (yes) and **N** (no). The CTFL syllabus and the exams write **T** (true) and **F** (false) — it means exactly the same. They also use **N/A** for a combination that is impossible, and **X** in an action row for "this action happens" (an empty cell means "does not happen").

| Our notes | CTFL / exam | Meaning |
|---|---|---|
| Y | T | the condition is true |
| N | F | the condition is false |
| – | – | does not matter for this rule |
| (deleted column) | N/A | impossible combination |
| X | X | the action happens |

**One action row, or one row per action?** CTFL allows both:

- **One row per action, with X** (*limited-entry*): the standard form. Use it always when a rule can trigger **several** actions.
- **One "Action" row with values** (*extended-entry*): shorter. Possible only when every rule has **exactly one** action, as in the checkout (the user goes either to login, or to the address page, or to payment). The collapse slides (18–21) use this short form for that reason.

**Real life: several actions in one column.** When a customer cancels an order, the shop does more than one thing:

| | 1 | 2 | 3 |
|---|---|---|---|
| C1 Order paid? | T | T | F |
| C2 Order shipped? | F | T | – |
| Refund the money | X | | |
| Send a cancellation e-mail | X | | X |
| Put the items back in stock | X | | X |
| Send to customer service | | X | |

Column 1 has **three X**: one test must check all three — was the money refunded, did the e-mail arrive, did the stock go up? Real bugs often hide exactly here: one of the three actions is forgotten. This cannot be written in one action row.

### 6.3 Building it step by step (the checkout on slide 17)

Specification (a simplified Juice Shop checkout button, for teaching):
- Not logged in → go to the login page.
- Basket empty → message "basket is empty".
- No saved address → ask for an address.
- Otherwise → go to payment.

**Step 1 — Write conditions and actions.** C1 Logged in? · C2 Basket empty? · C3 Address saved? Actions: login, "empty", address, payment.

**Step 2 — Number of columns:** 3 Boolean conditions → **2³ = 8 columns**. General formula: n conditions → **2ⁿ** columns (4 → 16, 5 → 32).

**Step 3 — Write all Y/N combinations in order.** Trick: first row 4Y-4N, second row 2Y-2N-2Y-2N, third row Y-N-Y-N… This way you never forget a combination.

| | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |
|---|---|---|---|---|---|---|---|---|
| C1 Logged in? | Y | Y | Y | Y | N | N | N | N |
| C2 Basket empty? | Y | Y | N | N | Y | Y | N | N |
| C3 Address saved? | Y | N | Y | N | Y | N | Y | N |
| Go to login | | | | | X | X | X | X |
| Show "empty" | X | X | | | | | | |
| Ask for address | | | | X | | | | |
| Go to payment | | | X | | | | | |

**Step 4 — Fill each column from the specification.** Column 5: not logged in, basket empty. Which comes first? The specification does not say! We decided "login first". **This is the hidden power of a decision table:** it shows unclear and missing rules before the code exists. A column with no action, or two conflicting actions = a defect in the specification → ask the product owner.

### 6.4 Collapsing, step by step (slides 18–21)

A full table with n conditions has 2ⁿ columns. Often many columns lead to the same action, so we can **collapse** (minimise) the table and test fewer rules. Collapsing is mechanical: you only need one rule and some patience.

**The merge rule:** take two columns that

1. have **the same action**, and
2. differ in **exactly one** condition (all other conditions are equal).

Merge them into one column and write **"–"** (*don't care*) for the condition that differed. Then **repeat** with the new columns until nothing can merge. Finally **check** the result.

**Step 1 — merge pairs.** Start with the full checkout table (section 6.3). Look for columns that are equal except in one condition. Here C3 is the last row, so neighbouring columns 1–2, 3–4, 5–6, 7–8 differ only in C3:

| Pair | Columns (C1 C2 C3) | Actions | Merge? | New column |
|---|---|---|---|---|
| 1 + 2 | Y Y Y · Y Y N | "empty" · "empty" | ✓ same action, only C3 differs | **Y Y –** → "empty" |
| 3 + 4 | Y N Y · Y N N | payment · address | ✗ **different actions** | keep both |
| 5 + 6 | N Y Y · N Y N | login · login | ✓ | **N Y –** → login |
| 7 + 8 | N N Y · N N N | login · login | ✓ | **N N –** → login |

After step 1: (Y Y –) "empty" · (Y N Y) payment · (Y N N) address · (N Y –) login · (N N –) login. That is 5 columns.

**Step 2 — repeat with the new columns.** A column with a dash can merge again, but only with a column that has the dash **in the same place**:

| Pair | Columns | Actions | Merge? | New column |
|---|---|---|---|---|
| (N Y –) + (N N –) | dash in C3 in both; only C2 differs | login · login | ✓ | **N – –** → login |
| (Y Y –) + (Y N Y) | dash only in one of them | "empty" · payment | ✗ different actions (and dashes not in the same place) | — |

After step 2: 4 columns. Nothing else has the same action and differs in only one condition → **stop**.

**Step 3 — check.** A dash stands for both Y and N, so it **doubles** the columns a rule covers. A rule with k dashes covers 2ᵏ original columns:

| Rule | Conditions | Dashes | Covers | Action |
|---|---|---|---|---|
| R1 | N – – | 2 | 2² = 4 columns (5, 6, 7, 8) | login |
| R2 | Y Y – | 1 | 2¹ = 2 columns (1, 2) | "empty" |
| R3 | Y N N | 0 | 1 column (4) | address |
| R4 | Y N Y | 0 | 1 column (3) | payment |

4 + 2 + 1 + 1 = **8** = 2³ ✓. Every original column belongs to **exactly one** rule. If the sum is not 2ⁿ, or a column is in two rules, a merge was wrong.

8 columns → **4 rules → 4 tests**.

**Testing a rule with a dash.** "–" means the result does not depend on that condition, but a real test still needs a real value. For R1 you choose, for example, "not logged in, basket empty, address saved". Any choice is fine. For a risky rule you may test more than one choice (that is, go back to the full columns).

**Typical mistakes:**

- **Merging two differences at once.** (Y N Y) and (N Y N) differ in all three conditions. They can never merge directly, even if the actions were the same.
- **Merging different actions.** Columns 3 and 4 differ only in C3, but one says payment and the other address. They stay separate.
- **Skipping step 2.** Many students stop after pairs and get 5 rules instead of 4. That is still correct (a larger table is not wrong), just less compact. A table that is collapsed too much **is** wrong.
- **Forgetting the check.** The 2ᵏ check catches almost every mistake in one line.

**Second example: the cinema (also in the Lab 4 cheatsheet).** Conditions: C1 child? C2 student card? C3 Wednesday? Action: 25 % off (yes/no). Full table: columns 1–4 have C1 = Y and "no"; columns 5–8 have C1 = N: (Y Y) yes, (Y N) yes, (N Y) yes, (N N) no.

- Step 1: 1+2 → (Y Y –) no · 3+4 → (Y N –) no · 5+6 → (N Y –) yes · 7+8: (N N Y) yes, (N N N) no → different actions, keep.
- Step 2: (Y Y –) + (Y N –) → (Y – –) no. (N Y –) cannot merge with (N N Y): the dashes are not in the same place.
- Result: R1 (Y – –) no · R2 (N Y –) yes · R3 (N N Y) yes · R4 (N N N) no. Check: 4 + 2 + 1 + 1 = 8 ✓.

**When a condition has more than two values.** In the Juice Shop delivery-price table (hunt T4) the condition "method" has three values (One Day, Fast, Standard). The full table has 2 × 3 = 6 columns. The merge rule is the same: Standard costs 0 for Deluxe and for normal members → (Standard, Deluxe –). One Day and Fast cost different amounts for the two groups, so they stay separate → 5 rules.

> **Warning:** collapsing is for saving tests, not for skipping risky combinations. In Lab 4, check every merge against the specification before you trust it.

### 6.5 Impossible combinations

Sometimes a combination cannot happen. Example: "age < 18?" and "age > 65?" cannot both be Y. Such columns are **deleted**, not tested, and do not count in coverage ("feasible" = possible columns).

### 6.6 Decision table coverage

> **Decision table coverage = rules (columns) tested ÷ all feasible rules × 100 %**

With 4 rules in the collapsed table, 4 tests give 100 %.

### 6.7 Strengths and weaknesses

- **Strengths:** never forgets a combination; shows gaps in the specification; the number of tests is clear.
- **Weakness:** the number of columns grows **exponentially** with the number of conditions (10 conditions → 1,024 columns). Then you need collapsing, risk-based selection or pairwise testing.
- **Link to Myers:** Myers' "cause-effect graphing" also ends in a decision table. We skip the graph and build the table directly.

### Check yourself (6)

1. How many columns does a full decision table with 4 conditions have?
2. Gift wrap: Digital product? Member? Birthday? Rule: members get free gift wrap, and so does everybody on their birthday — but never for digital products. Build the full table, collapse it step by step, and do the 2ᵏ check.
3. Can (Y N Y) and (N Y N) be merged if both have the same action? Why?

> **Answers:** 1) 2⁴ = 16.
> 2) Full table (C1 digital, C2 member, C3 birthday): columns with C1 = Y → no; with C1 = N: (Y Y) yes, (Y N) yes, (N Y) yes, (N N) no. Step 1: 1+2 → (Y Y –) no, 3+4 → (Y N –) no, 5+6 → (N Y –) yes; 7 (N N Y) yes and 8 (N N N) no stay. Step 2: (Y Y –) + (Y N –) → (Y – –) no. Result: (Y – –) no · (N Y –) yes · (N N Y) yes · (N N N) no. Check: 4 + 2 + 1 + 1 = 8 ✓. **4 rules.**
> 3) No. They differ in all three conditions; a merge is allowed only when exactly one condition differs.

---

## 7. State transition testing (slides 22–24)

### 7.1 When?

When the result depends not only on the current input but on **what happened before** (history). The same event may be allowed in one state and forbidden in another. Example: "Cancel" must work for a paid order but not for a shipped one.

Typical areas: orders, payment flows, login attempts (3 wrong → locked), sessions, ATMs, traffic lights, document approval.

### 7.2 Parts

| Term | Meaning | Order example |
|---|---|---|
| **State** | the condition the system is in | Placed, Paid, Shipped, Delivered, Cancelled |
| **Event** | something that can change the state | pay, ship, deliver, cancel |
| **Transition** | state + event → new state | Placed + pay → Paid |
| **Guard** | a condition that must be true for the transition | pay **[card valid]** |
| **Action** | work done during the transition | cancel (refund) → money back |
| **Initial state** | the black dot | Placed |

### 7.3 The diagram (slide 22)

```
  ● → [Placed] --pay--> [Paid] --ship--> [Shipped] --deliver--> [Delivered]
          |                 |
        cancel        cancel (refund)
          ↓                 ↓
            [Cancelled]
```

5 states, 4 events, **5 valid transitions**: Placed→Paid, Paid→Shipped, Shipped→Delivered, Placed→Cancelled, Paid→Cancelled.

### 7.4 The state table and invalid transitions (slide 23)

The diagram shows only what **can** happen. A **state table** shows every state × every event, so we also see what **must not** happen.

| State \ event | pay | ship | deliver | cancel |
|---|---|---|---|---|
| Placed | Paid | — | — | Cancelled |
| Paid | — | Shipped | — | Cancelled |
| Shipped | — | — | Delivered | — |
| Delivered | — | — | — | — |
| Cancelled | — | — | — | — |

5 × 4 = **20 cells**: 5 valid, **15 invalid** ("—"). An invalid transition is an event that is not allowed in that state; the system must **refuse** it and **stay in the same state**. In real life the most expensive bugs are here: a refund for a delivered order, shipping a cancelled order, paying twice.

### 7.5 Coverage levels

| Level | What it needs | Strength |
|---|---|---|
| **All states** | every state is visited at least once | weakest |
| **Valid transitions** (= **0-switch coverage**) | every valid transition (every filled cell) at least once | the usual CTFL target |
| **All transitions** | valid transitions + every invalid transition tried | strongest; recommended for safety and money |

> The name **0-switch** means single transitions (0 extra transitions in between) are tested. **1-switch** would test sequences of two transitions (advanced, rare in the exam).

### 7.6 Counting tests (slide 24)

Important: **one test can go through several transitions.** Every test starts in the initial state (Placed).

**Question 1 — minimum number of tests to visit all states?**
- T1: Placed → pay → Paid → ship → Shipped → deliver → Delivered (4 states).
- T2: Placed → cancel → Cancelled (the 5th state).
- **Answer: 2 tests.** (Cancelled and Delivered cannot be on the same path; both are final states.)

**Question 2 — minimum number of tests to cover all 5 valid transitions?**
- T1: pay, ship, deliver (3 transitions).
- T2: Placed → cancel (4th).
- T3: Placed → pay → Paid → cancel (5th; pay repeats, which is fine).
- **Answer: 3 tests.** Common wrong answer: "5" (one test per transition).

**Question 3 — one invalid-transition test:** Placed → pay → ship → **cancel** → the system must refuse; the order stays Shipped.

### 7.7 How to write a state transition test

| Step | Start state | Event | Expected new state | Expected output |
|---|---|---|---|---|
| 1 | Placed | pay (valid card) | Paid | payment confirmation |
| 2 | Paid | cancel | Cancelled | refund |
| 3 | Cancelled | ship | Cancelled (no change) | error: "a cancelled order cannot be shipped" |

At every step check both the **new state** and the **output**.

### Check yourself (7)

1. A door has the states Open, Closed, Locked and the events open, close, lock, unlock. Valid transitions: Open–close→Closed, Closed–open→Open, Closed–lock→Locked, Locked–unlock→Closed. How many invalid transitions are there?
2. What is the minimum number of tests (starting from Open) to cover all valid transitions?

> **Answers:** 1) 3 states × 4 events = 12 cells, 4 valid → **8 invalid**. 2) **1 test**: Open → close → Closed → lock → Locked → unlock → Closed → open → Open. (One path goes through all four transitions.)

---

## 8. Pairwise testing (slides 25–27)

> **Note:** demo only; **not** in the midterm. CTFL Foundation has no learning objective for it (ISTQB teaches it in the Advanced Test Analyst syllabus). But you will meet it at work.

### 8.1 The problem: combinations multiply (slide 25)

Juice Shop's checkout must work with every browser, operating system, payment method and delivery speed:

| Setting | Values | Number |
|---|---|---|
| Browser | Chrome, Firefox, Safari, Edge | 4 |
| OS | Windows, macOS, Android | 3 |
| Payment | Card, Wallet, Coupon | 3 |
| Delivery | Standard, Fast | 2 |

Testing **every full combination** needs 4 × 3 × 3 × 2 = **72 tests**. Every new setting multiplies: add 5 languages → 360 tests; ten settings with 3 values each → 3¹⁰ = **59,049 tests**. Nobody can run that.

**The good news.** NIST studied real failures (Kuhn, Wallace and Gallo, IEEE Transactions on Software Engineering, 2004). In the systems they studied, **more than 70 % of the failures were triggered by only one or two settings together**, and no failure needed more than six. A typical example: "the Pay button is invisible with Safari **and** Wallet" — two settings, not four.

So: if we test **every pair of values**, we catch most combination bugs with far fewer tests.

### 8.2 What exactly is a "pair"? (slide 26)

A **pair** is one value of one setting together with one value of another setting: (Safari, Wallet), (Android, Fast), (Edge, Windows)…

Between Browser and Payment there are 4 × 3 = **12 pairs**. Draw them as a grid; every cell must appear in at least one test:

| | Card | Wallet | Coupon |
|---|---|---|---|
| Chrome | ☐ | ☐ | ☐ |
| Firefox | ☐ | ☐ | ☐ |
| Safari | ☐ | ☐ (Safari, Wallet) | ☐ |
| Edge | ☐ | ☐ | ☐ |

There is one such grid for every two settings (6 grids for 4 settings):

| Settings | Pairs |
|---|---|
| Browser × OS | 4 × 3 = 12 |
| Browser × Payment | 4 × 3 = 12 |
| Browser × Delivery | 4 × 2 = 8 |
| OS × Payment | 3 × 3 = 9 |
| OS × Delivery | 3 × 2 = 6 |
| Payment × Delivery | 3 × 2 = 6 |
| **Total** | **53 pairs** |

**The trick:** one test covers **several pairs at once**. The test "Chrome · Windows · Card · Standard" covers 6 pairs: (Chrome, Windows), (Chrome, Card), (Chrome, Standard), (Windows, Card), (Windows, Standard), (Card, Standard) — one cell in each grid.

**How many tests at least?** The two largest settings, Browser × OS, alone have 12 pairs, and one test can cover only one of them. So we need **at least 4 × 3 = 12 tests**. Twelve well-chosen tests can cover all 53 pairs.

> **Full combinations: 72 tests. All pairs: 12 tests.** Fewer tests, but every two-setting bug is still tested.

### 8.3 PICT: model in, test table out (slide 27)

Nobody chooses the 12 rows by hand. **PICT** is a free command-line tool from Microsoft (github.com/microsoft/pict, MIT licence).

**Input — the model file** `checkout.txt`, one line per setting:

```
Browser:  Chrome, Firefox, Safari, Edge
OS:       Windows, macOS, Android
Payment:  Card, Wallet, Coupon
Delivery: Standard, Fast
```

**Command:** `pict checkout.txt`

**Output — a table.** Each row is **one test case** you run by hand or in an automated test. A pairwise set for this model (generated and checked for this course: all 53 pairs appear):

| # | Browser | OS | Payment | Delivery |
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

**How to read it:** find (Safari, Wallet) → row 9. Find (Edge, Fast) → rows 10 and 11. Every pair is somewhere. PICT itself may print other rows (it starts from a random seed), but its table also covers every pair, usually in 12–14 rows.

**Constraints.** Look at row 9 again: **Safari on Android** does not exist in real life. We add a rule to the model:

```
IF [OS] = "Android" THEN [Browser] <> "Safari";
```

PICT then builds a new table without that impossible combination (usually one or two rows more). Constraints are how a tester puts real-world knowledge into the tool.

**Stronger option:** `pict checkout.txt /o:3` covers every **triple** of values (more tests, more safety). Choose by risk: triples for a payment page, pairs for a blog.

### 8.4 Pairwise and EP

- **EP** chooses the values of **one** input ("which browsers? which amounts?").
- **Pairwise** chooses **combinations of many inputs** from those values.
- They work together: first EP (and BVA) for each setting, then pairwise to combine them.

> **One sentence:** test every pair of values, not every combination — most bugs need only two settings to meet.

---

## 9. Real world 2026: AI and boundaries (slides 28–29)

### 9.1 What was found?

**Study 1 — October 2026 (preprint).** Shi, Luo, Yang and Tian, "Beyond Coverage and Mutation: A Blind-Spot Analysis of LLM-Generated Unit Tests", arXiv 2610.32548 (23 October 2026).
- Four AI models (GPT-5.4, Gemma-4-31B, Gemini-3.6-flash, Qwen3-Coder-30B), with four prompting strategies, wrote **14,320 tests** for 895 complex Java functions.
- The researchers measured how often the tests checked boundary cases:

| Kind of boundary | Covered |
|---|---|
| true / false inputs | **93.1 %** (true), 93.8 % (false) |
| empty input | **38.8 %** |
| null | **34.8 %** |
| negative number | **23.8 %** |

- The authors' summary: AI covers boundaries that can be **directly inferred**, and struggles with the ones that need thinking. Also, 15.6 % of failing tests failed because of a **wrong expected value** (assertion).
- **Important:** this is a **preprint**, not yet peer reviewed. The numbers come from Java functions; do not generalise them to "AI always misses boundaries".

**Study 2 — 2025.** Rodríguez, Rossi and Fernandez (arXiv 2505.09830): GPT-4 **found the partitions correctly**, but chose test values "usually towards the middle of the range"; humans chose values as close as possible to the edges.

**ISTQB's reaction:** the **CT-GenAI** certificate (Certified Tester – Testing with Generative AI) appeared in 2025, with a v1.1 update in April 2026. It covers prompting for testing, the risks of generative AI, and LLM-based test infrastructure.

### 9.2 The tester's view

- AI speeds up **writing** tests.
- But deciding **which partitions and boundaries matter**, checking that expected values are right, and saying "this software can go live" are still the tester's job.
- Asking an AI to "use BVA" helps, but to ask for it and to check the answer you must **know BVA**.
- The team's tests in Lab 4 show exactly this pattern: only middle values (3, 10, 20 km; 50 and 200 TL), no edges.

### 9.3 Live try (slide 29)

Type into any free LLM on a phone:

```
Write pytest tests for apple_juice_ok(qty).
Rule: a customer can order 1 to 5 bottles of Apple Juice in one order.
qty is a whole number. Return True if the order is allowed, False otherwise.
```

Checklist: did it test 0 and 1? 5 and 6? A negative number? A value that is not a whole number (2.5)? Do the expected values match the rule? Whatever the result, the lesson is the same: we could judge the AI **only because we had our own boundary list first**.

---

## 10. Boundary hunting in Juice Shop (slides 30–34)

### 10.1 Goal

Try the theory on a real application. **OWASP Juice Shop** is a deliberately insecure online juice shop for training. It is our target for the whole semester. **Class address: https://juice.armikom.com** (open it in your phone's browser).

> **Ethics rule:** these techniques are used only on Juice Shop, or on other systems **with written permission**. Testing a real site without permission is a crime.

### 10.2 Full answers for the five targets (source: Juice Shop v20.3.0 code)

**T1 — Register form, password length**
- Rule on screen: "Password must be 5-40 characters long."
- Partitions: 0–4 (invalid) · 5–40 (valid) · 41+ (invalid).
- 2-value BVA: **0, 4, 5, 40, 41** (0 = empty password).
- Expected: 0 → error ("Please provide a password."), 4 → error, 5 → accepted, 40 → accepted, 41 → error.
- **Finding:** the counter under the password field shows "x/**20**", but the rule says **40**. (The repeat field shows "/40".) The screen contradicts the rule: a real inconsistency found by testing; ask the product owner which one is right. And behind the screen the server does not check the length at all (see 10.4).

**T2 — Basket, Apple Juice (1000ml) quantity**
- Rule: 1–5 per order.
- Partitions: ≤0 (invalid) · 1–5 (valid) · ≥6 (invalid).
- 2-value BVA: **0, 1, 5, 6**.
- Real behaviour: 1 and 5 work; 6 gives "You can order only up to 5 items of this product."; the "−" button stops at 1.
- **Open question:** does the server also refuse 0 or −1? (Week 11.)

**T3 — Customer Feedback**
- Rating: slider 1–5 stars; Submit is disabled until a rating is chosen.
- Comment: at most 160 characters; required.
- 2-value BVA: rating **0, 1, 5** (6 cannot be chosen); comment length **0, 1, 160, 161**.
- Real behaviour: a 161st character cannot be typed (the field stops).
- **Discussion:** is it worth testing a value you cannot type? Yes, but not through the screen; through the path to the server (week 11).

**T4 — Decision table: delivery price**
- Specification (the shop's price list): One Day Delivery 0.99, for Deluxe members 0.50 · Fast Delivery 0.50, free for Deluxe members · Standard Delivery free for everyone.
- Conditions: *Deluxe member?* (Y/N) and *delivery method* (One Day, Fast, Standard). This is a decision table where one condition has three values, so the full table has 2 × 3 = **6 columns**:

| | 1 | 2 | 3 | 4 | 5 | 6 |
|---|---|---|---|---|---|---|
| Deluxe member? | Y | Y | Y | N | N | N |
| Method | One Day | Fast | Standard | One Day | Fast | Standard |
| **Price** | 0.50 | 0 | 0 | 0.99 | 0.50 | 0 |

- Collapse: columns 3 and 6 differ only in "Deluxe" and both cost 0 → one rule "Standard, Deluxe –". Result: **5 rules**.
- On the phone (log in → basket → Checkout → address → Continue): the delivery page shows 0.99, 0.50 and 0.00 for a normal account — as the rule says.
- **Finding:** the three Deluxe columns **cannot be tested** with a normal account. A rule you cannot test needs special test data: here a Deluxe test account. Writing this down is part of good testing.

**T5 — State transitions: checkout steps**
- States: Basket → Select address → Delivery method → Payment → Order summary → Order completed.
- Events and guards: *Checkout* [basket not empty, logged in] · *Continue* [an address is chosen] · *Continue* [a delivery method is chosen] · …
- Invalid transitions and what the screen does: Checkout with an empty basket → the button is disabled (refused ✓); Continue without an address → disabled (refused ✓); Checkout while logged out → sent to the login page (guard ✓).
- The payment page asks for a card: another guard, Payment → Order summary [payment method chosen]. In class we stop there; never enter a real card.
- **To try:** open `juice.armikom.com/#/delivery-method` directly, without choosing an address. In the source this page has no login guard. If it opens anyway, that is a finding: the order of the steps is checked by the buttons, not by the system.

### 10.3 Where does the expected result come from? (the most important rule)

The most common mistake in the hunt: writing what actually happened into the "expected" column. Then every test passes and nothing is found. **The expected result always comes from the rule (the specification)**, never from the application's behaviour. Testing means comparing the two.

### 10.4 The live demo: two boundaries that only the browser checks (slide 34)

**1. Zero Stars.** Juice Shop challenge: give the shop a feedback with **0 stars**.

- The **browser** enforces the boundary: slider minimum 1, Submit disabled at 0.
- The **server** does not check it: it saves rating 0.
- The instructor deletes the `disabled` attribute of the Submit button in the browser's developer tools (or sends the request directly to the server) and sends a 0-star feedback.

**2. The password length you tested in T1.** The rule "5–40 characters" exists **only in the register form**. The server (`POST /api/Users`) stores any password, even a single character. On the screen the rule works perfectly; behind the screen there is no rule at all.

**Lesson:** a disabled button or a red error message is not a **rule**. Test the boundary **where the decision is really made** (on the server). In week 11 we test this with tools; today your hunt stays on the screen.

---

## 11. Using the techniques together: Myers' strategy (slide 37)

### 11.1 Myers' strategy (slide 37)

Myers ends his chapter on test-case design with a simple message: **no technique is enough alone.** Each one adds tests the others miss. His order of work:

1. If the specification has **combinations of conditions**, start with them. Myers draws a cause-effect graph and turns it into a decision table; we build the **decision table** directly.
2. **Always do BVA**, on the inputs **and** on the outputs (for example a fee of exactly 0 TL).
3. Add **EP** tests for every partition that is still missing, valid and invalid.
4. Add tests from **experience: error guessing** (week 9).
5. Check the **code coverage** and add tests for branches that no test reached (week 9).

Steps 1–3 are this week; steps 4–5 come in week 9 (structure-based and experience-based techniques).

**Why does Myers start with the table, not with EP?** The columns of a decision table already contain many partitions and boundaries, so BVA and EP then only add what is missing. Beginners often find it easier to list the partitions first, as in the order below. Both orders end with the same tests; what matters is that you use all the techniques.

### 11.2 Choosing from the words of the specification

| What you see in the specification | Technique |
|---|---|
| a range of values, a list, a format | **EP** (partitions) |
| the edges of an ordered range (number, length, date, money) | **BVA** (on top of EP) |
| combined rules: "if A and B then X, otherwise Y" | **Decision table** |
| "first this, then that", states and allowed/forbidden steps | **State transition** |
| many independent settings combined (browser, OS, language…) | **Pairwise** |

**One test can serve several techniques.** In the cinema example (section 6, "Check yourself"), the test "age 13, student card, not Wednesday" is both a **boundary value** of the age partitions (if a child is 0–12 years old: 12 | 13) and a **decision table rule** (not a child, student card → discount).

**A good order of work (also for Lab 4):**

1. Read the specification and mark every number and every "or more / more than / up to".
2. EP: list the partitions of every input, valid and invalid.
3. BVA: list the boundary values of every ordered partition, with the expected result **from the rule**.
4. Decision table: if the result depends on several conditions together, build the full table first, then collapse carefully.
5. Turn every row into one test. Compare expected and actual. A red test is a finding (or a wrong expected value: check the rule again).

---

## 12. Exit ticket and midterm traps (slides 38, 40)

### 12.1 Exit ticket answers, explained

1. **Whole numbers 1–100, 2-value BVA → b) 0, 1, 100, 101.** All boundary values. (c) has extra values (2, 99) but is not full 3-value either (−1 and 102 are missing); (a) is only the valid side; (d) looks like EP.
2. **Password 5–40 characters, how many partitions? → c) 3.** Too short, valid, too long.
3. **3 yes/no conditions, how many columns? → c) 8.** 2³ = 8.
4. **Invalid-transition test → b) cancel a Shipped order.** The others are allowed transitions in the model.

### 12.2 Typical midterm traps

| Trap | Correct |
|---|---|
| mixing up "2-value" and "3-value" | read the question; both appear in the options |
| forgetting the boundaries of invalid partitions in 3-value | CTFL: neighbours of **every** boundary value (1–10 → −1, 0, 1, 2, 9, 10, 11, 12) |
| forgetting 0 for a length | a length cannot be below 0: **0 is a boundary value too** (empty text) |
| writing 149 for a money boundary | step 0.01 → **149.99** |
| mixing up "more than 150" and "150 or more" | "more than" **excludes** 150: boundary 150.00 \| 150.01 |
| forgetting invalid partitions | the rule 5–40 has **3** partitions, not 1 |
| two invalid values in one test | masking: invalid values one at a time |
| 2n instead of 2ⁿ columns | 3 conditions → 8, 4 conditions → 16 |
| surprised by T / F in a decision table | T = Y (true), F = N (false); N/A = impossible combination |
| "one test per transition" | one test can go through many transitions |
| dividing the coverage the wrong way | tested ÷ total × 100 % |
| BVA on unordered values (colours) | BVA only for **ordered** partitions |

---

## 13. Glossary (EN–TR)

| English term | Turkish | Short meaning |
|---|---|---|
| Black-box technique | Kara kutu tekniği | based on the specification, not the code |
| Specification-based | Spesifikasyon tabanlı | another name for black-box |
| White-box / structure-based | Beyaz kutu / yapı tabanlı | based on the code |
| Experience-based | Deneyim tabanlı | based on the tester's knowledge |
| Exhaustive testing | Kapsamlı test | testing everything; impossible |
| Equivalence partition (class) | Denklik sınıfı | a group of values with the same expected behaviour |
| Valid / invalid partition | Geçerli / geçersiz sınıf | values to accept / to reject |
| Representative value | Temsilci değer | the test value chosen from a partition |
| Masking | Maskeleme | one error hides another |
| Boundary value | Sınır değer | the first/last value of an ordered partition |
| 2-value BVA | 2 değerli BVA | all boundary values (each boundary + closest neighbour in the next partition) |
| 3-value BVA | 3 değerli BVA | each boundary value + both neighbours |
| Off-by-one error | Bir eksik/fazla hatası | e.g. `<` instead of `<=` |
| Step size | Adım büyüklüğü | the distance between neighbours (1, 0.01…) |
| Overflow | Taşma | a value does not fit its data type |
| Coverage item | Kapsama öğesi | the thing a technique wants a test for |
| Coverage | Kapsama | tested ÷ total items |
| Decision table | Karar tablosu | table of conditions and actions |
| Condition / action | Koşul / eylem | yes/no question / result |
| Rule (column) | Kural (sütun) | one combination of conditions |
| Don't care ("–") | Fark etmez | a condition that does not change the result |
| Collapse | Sadeleştirme | merging columns with the same action |
| Feasible | Mümkün | a combination that can really happen |
| State | Durum | the condition the system is in |
| Event | Olay | something that can change the state |
| Transition | Geçiş | state + event → new state |
| Guard condition | Koruma koşulu | the condition for a transition |
| Invalid transition | Geçersiz geçiş | a forbidden event; must be refused |
| State table | Durum tablosu | state × event table |
| 0-switch coverage | 0-switch kapsaması | all valid single transitions |
| Pairwise testing | İkili birleşim testi | every pair of values at least once |
| Preprint | Önyayın | a paper not yet peer reviewed |

---

## 14. Big practice set (with answers)

For midterm preparation. Solve first, then look at the answers.

**Q1.** A field accepts whole numbers from 1 to 10 (the invalid partitions are open at both ends). Which are the 3-value BVA values?
a) 0, 1, 10, 11 b) 0, 1, 2, 9, 10, 11 c) −1, 0, 1, 2, 9, 10, 11, 12 d) 1, 5, 10

**Q2.** A shop gives a gift for purchases of "at least 200.00 TL" (2 decimals). Which pair tests the boundary best?
a) 199 and 200 b) 199.99 and 200.00 c) 200.00 and 200.01 d) 100.00 and 300.00

**Q3.** A temperature alarm: <0 °C "freezing", 0–30 °C "normal", >30 °C "hot" (whole numbers). Minimum tests for 100 % EP coverage?
a) 1 b) 2 c) 3 d) 6

**Q4.** For the same alarm, how many values does 2-value BVA give?
a) 2 b) 3 c) 4 d) 6

**Q5.** How many columns does a full decision table with 5 Boolean conditions have?
a) 5 b) 10 c) 25 d) 32

**Q6.** A collapsed decision table has 6 rules. The tests cover 4 of them. Coverage?
a) 40 % b) 60 % c) 66.7 % d) 75 %

**Q7.** When is a decision table most useful?
a) a single numeric range b) rules where the result depends on a combination of conditions c) behaviour that depends on the system's history d) browser compatibility

**Q8.** A test enters age −5 and email "abc"; only the email error appears. What is this called?
a) Overflow b) Masking c) Off-by-one d) Guard

**Q9.** An ATM has the states Idle, CardInserted, PinOK. Valid transitions: Idle–insert→CardInserted, CardInserted–pinOK→PinOK, CardInserted–eject→Idle, PinOK–eject→Idle. Which is an invalid transition?
a) Idle + insert b) CardInserted + eject c) Idle + pinOK d) PinOK + eject

**Q10.** Minimum number of tests (each starting in Idle) to cover all valid transitions of Q9?
a) 1 b) 2 c) 3 d) 4

**Q11.** Which is **not** a black-box technique?
a) boundary value analysis b) state transition testing c) branch coverage d) decision table testing

**Q12.** "A message cannot be longer than 160 characters; an empty message cannot be sent." 2-value BVA for the length?
a) 0, 160 b) 0, 1, 160, 161 c) 1, 160 d) 0, 1, 2, 159, 160, 161

### Answers and explanations

| Q | Answer | Explanation |
|---|---|---|
| Q1 | **c** | Boundary values 0, 1, 10, 11; add both neighbours of each → −1…2 and 9…12. (b) is the incomplete version from older books |
| Q2 | **b** | "at least" → 200 included; step 0.01 → neighbour 199.99 |
| Q3 | **c** | 3 partitions: <0, 0–30, >30 |
| Q4 | **c** | 2 edges (−1\|0 and 30\|31) → 4 values: −1, 0, 30, 31 |
| Q5 | **d** | 2⁵ = 32 |
| Q6 | **c** | 4 ÷ 6 = 66.7 % |
| Q7 | **b** | combined conditions; (c) is state transition, (d) pairwise |
| Q8 | **b** | one error hid the other |
| Q9 | **c** | there is no PIN check in Idle |
| Q10 | **b** | T1: Idle→insert→CardInserted→pinOK→PinOK→eject→Idle (3 transitions). T2: Idle→insert→CardInserted→eject→Idle (the 4th) |
| Q11 | **c** | branch coverage is a white-box (structure-based) technique |
| Q12 | **b** | two edges: 0\|1 (empty is invalid) and 160\|161 |

---

*Sources: Myers, The Art of Software Testing, 3rd ed., Ch. 4 · ISTQB® CTFL Syllabus v4.0.1, 4.1–4.2, and CTFL v4.0 Sample Exam A (simplified, with acknowledgement) · Ammann & Offutt, Ch. 6 (pairwise) · ESA Ariane 501 Inquiry Board report (1996) · OWASP Juice Shop v20.3.0 (MIT) · Shi et al., arXiv 2610.32548 (2026, preprint) · Rodríguez et al., arXiv 2505.09830 (2025) · ISTQB CT-GenAI v1.1 (2026). Prepared 2026-11-01.*
