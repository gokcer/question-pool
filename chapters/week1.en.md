# Week 1 — Introduction to Software Testing

**The one idea:** testers try to find faults before the faults find the users.

This text is a short summary of the Week 1 lecture. Read it, then write your questions. A good question uses a small example, not only a definition.

## 1. Why do we test software?

Software controls planes, cars, banks, hospitals and phones. When software fails, people can lose money, time, or even their lives. Examples from the lecture:

- A healthcare website crashed again and again on its first day. Nobody had tested it with many users at the same time (no load test).
- The Boeing 737 Max crashed because its flight software (MCAS) pushed the nose of the plane down too strongly.
- In the US Northeast blackout, about 50 million people lost electricity. The alarm software had failed, so the operators did not see the problem in time.

A tester's goal is to remove faults **as early as possible**. This improves quality, lowers cost, and keeps customers happy.

## 2. Fault, error, failure

These three words are not the same.

| Term | Meaning | Doctor example |
|---|---|---|
| **Fault** | A static defect in the code (the mistake that is written there) | The illness itself |
| **Error** | A wrong internal state while the program runs | High blood pressure, bacteria in the blood |
| **Failure** | Wrong behaviour that can be seen from outside | The symptoms the patient feels |

Software faults are like design mistakes in hardware. Software does not get old or wear out: the fault was there from the beginning.

**Code example.** This method must count the zeros in an array:

```java
public static int numZero(int[] arr) {
    int count = 0;
    for (int i = 1; i < arr.length; i++) {   // fault: must start at 0
        if (arr[i] == 0) count++;
    }
    return count;
}
```

- Input `[2, 7, 0]` → expected 1, actual 1. The fault is executed and the state is wrong (`i` is 1, not 0: an **error**), but there is **no failure**.
- Input `[0, 2, 7]` → expected 1, actual 0. The error reaches the output: a **failure**.

So a program with a fault can still pass a test. The test input decides if we see the failure.

## 3. The triangle program (Myers)

A program reads three integers: the side lengths of a triangle. It prints *scalene* (all sides different), *isosceles* (two sides equal) or *equilateral* (three sides equal). Which tests do you need? Most people write too few. Good tests include:

- a valid scalene, a valid isosceles and a valid equilateral triangle;
- isosceles in all three orders: (3, 3, 4), (3, 4, 3), (4, 3, 3);
- a side with value 0, a negative side, and (0, 0, 0);
- three numbers where the sum of two equals the third, for example (1, 2, 3): this is **not** a triangle;
- three numbers where the sum of two is less than the third, for example (1, 2, 4);
- non-integer values, and the wrong number of values (two numbers, not three);
- for **every** test, the expected output.

Lesson: even a very small program needs many tests, and invalid inputs are as important as valid ones.

## 4. Verification and validation (V&V)

- **Validation:** are we building the **right product**? Does it meet what the customer needs?
- **Verification:** are we building the **product right**? Does each step meet the requirements from the step before?
- **IV&V:** independent verification and validation, done by people outside the development team.

## 5. Software quality assurance (SQA)

The purpose of SQA is to find and report defects **and** to get them fixed.

**What is a defect?**

1. The program does not match its specification. (Good only when a complete, correct specification exists.)
2. The program does not do what its user reasonably expects. (Always usable, also when there is no specification.)

**Categories of defects:** functional, user interface, performance, error handling, security, load, configuration, race conditions, documentation.

**Cost.** The longer a defect stays in the system, the more it costs to fix. A requirement defect found in the requirements phase is cheap. The same defect found after the product ships is very expensive. So SQA must happen during the whole life cycle, not only at the end.

The two main SQA activities are **technical reviews** and **software testing**.

## 6. Technical reviews

In a review, a small group of qualified people reads a work product and gives feedback. Anything can be reviewed: requirements, design, code, test plans, user manuals. Reviews can be informal (asking a friend, pair programming) or formal (planned meeting, preparation before the meeting).

Rules for review meetings: review the **product, not the producer**; keep to the agenda; name the problems but do not try to fix them in the meeting; take written notes; keep the group small; prepare before the meeting; use a checklist.

Reviews are the most effective QA technique for cost and for the number of defects found, but teams often skip them.

## 7. Software testing: basic facts

- Testing means **running** the software and comparing actual results with expected results.
- Testing is the most popular QA activity, but not the most effective. All forms of testing together usually find less than 60% of the defects. A typical project may spend about 50% of its resources on testing.
- **Exhaustive testing is not possible.** The number of input combinations and the number of paths through the code are effectively infinite.
- So we do **partial testing**, and partial testing can never prove that there are no defects.
- Good testing means choosing a small number of tests in a smart way: test all requirements, test what users really do, avoid tests that repeat each other, look for weak places in the design and code.

**Black-box and white-box testing**

| | Black-box | White-box |
|---|---|---|
| The tester knows | only the external behaviour (specification) | the specification and the internal code |
| Tests come from | the specification | the specification and the program logic |
| "Test everything" would mean | every possible input | every possible path |

Both "test everything" ideas are impossible in practice.

**The right attitude.** The goal of testing is to *break* the software. A tester must expect and want to find defects. Developers and an independent test group should both test, because they think differently and find different defects.

**Defects cluster.** About 80% of the defects are found in about 20% of the code. Complex code has more defects, so test complex modules more.

**How many defects?** Most projects have between 1 and 25 defects per 1000 lines of code.

**Automation.** Tests must be run again and again, so automation is necessary. Some tests are hard to automate and are run by hand.

## 8. Test levels

- **Unit testing:** test one module alone, in a controlled environment. A **driver** is code that calls the unit. A **stub** is simple code that stands in for a module the unit calls.
- **Integration testing:** put units together and test how they work together (top-down or bottom-up).
- **System testing:** test the whole system against the system requirements: functionality, performance, reliability, usability.
- **Regression testing:** after a change, run the old tests again to check that nothing that worked before is now broken. The regression suite is usually large, so it must be automated and kept up to date (add a test for every bug fix and every new feature).

## 9. Formal verification

A third QA approach: build a mathematical model of the system and **prove** that it behaves correctly. It is expensive and hard for large systems, so it is used for small, critical parts.

## Key terms

| English | Turkish |
|---|---|
| fault / error / failure | hata (kusur) / yanlış iç durum / arıza |
| verification / validation | doğrulama / geçerleme |
| review | gözden geçirme |
| black-box / white-box testing | kara kutu / beyaz kutu testi |
| exhaustive testing | tüketici (eksiksiz) test |
| unit / integration / system testing | birim / entegrasyon / sistem testi |
| regression testing | regresyon testi |
| stub / driver | koçan / sürücü |

## Ideas for your questions

- Give a short story about a bug and ask: which part is the fault, the error, the failure?
- Give a test list for the triangle program and ask which important test is missing.
- Describe an activity and ask: is it verification or validation? Black-box or white-box? Which test level?
- Describe a defect and ask for its category.
