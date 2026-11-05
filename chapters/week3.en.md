# Week 3 — Test Automation

**The one idea:** automation is the bridge between a good idea for a test and a test that runs every day without anyone thinking about it.

This chapter covers Parts 1–3 of the Week 3 lecture: why we automate, testability, the parts of a test case, JUnit, data-driven tests, and what makes a test good. (Part 4 — UI tests, mobile tests and CI tools — was not taught and is not in this chapter.)

## 1. What is test automation?

**Regression testing** means running the existing tests again after a change, to check that nothing that worked before is now broken. You fixed one bug in an app with 500 features: how do you know the other 499 still work? Nobody can check them by hand every day. This is why we automate.

**Test automation** is the use of software to:

- **control** the execution of tests,
- **set up** the preconditions,
- **compare** actual results with expected results,
- **report** the results.

*Choosing the inputs* is not on this list. That is test design.

**Benefits:** less boring work, more time for test design, tests can run thousands of times at no extra cost, fewer forgotten steps, the same quality whoever runs the test.

**Revenue tasks and excise tasks**

| | Meaning | Examples |
|---|---|---|
| **Revenue task** | Directly solves the problem | Choosing test values; deciding the correct output |
| **Excise task** | Necessary, but adds nothing itself | Compiling; re-running tests; comparing outputs by eye |

Automate the excise tasks, so people can spend their time on the revenue tasks.

**When automation does not pay off:** the test runs only once; the script costs more than it saves; usability and "look and feel" (needs human eyes); exploratory testing (the goal is to learn, not to repeat); there is no oracle (nobody can say what the correct output is).

## 2. Testability

**Testability** tells us how easy it is to find the faults in a piece of software. It comes down to two questions:

| | Question | Easy | Hard |
|---|---|---|---|
| **Controllability** | Can I get the input values in? | Method parameters, keyboard | Sensors, time, random numbers, distributed software |
| **Observability** | Can I see what came out? | A return value | A database, a remote file, an e-mail, a motor |

**Example.** This class is hard to test:

```java
public void greet(String name) {
    int hour = LocalTime.now().getHour();          // input from the system clock
    if (hour < 12) System.out.println("Good morning, " + name);
    else           System.out.println("Good afternoon, " + name);
}
```

- Low controllability: the test cannot choose the hour.
- Low observability: the method returns nothing; it only prints.

The testable version gets a `Clock` object in its constructor and **returns** a `String`. Now the test can choose the time (09:00 or 12:00) and check the result with `assertEquals`. The behaviour is the same; only the place of the inputs and outputs changed.

**Five habits for testable design**

| Habit | Improves |
|---|---|
| Pass dependencies in (clock, random, database) | Controllability |
| Return results, do not print them | Observability |
| Small methods with one job each | Both |
| Depend on interfaces (so a fake can replace the real thing) | Both |
| Avoid hidden global state (static counters, singletons) | Controllability |

**Information hiding** (private fields) is good design, but it also hides state from tests. Test through the public methods. Do not make fields public only for tests.

## 3. The four parts of a test case

| Part | What it is | RIPR letter |
|---|---|---|
| **Prefix values** | Inputs that put the software in the right state | **R**each |
| **Test case values** | The inputs we really want to test | **I**nfect |
| **Postfix values** | *Verification* values make the result visible; *exit* values return the software to a stable state | **P**ropagate (verification part) |
| **Expected results** | What correct software would produce | **R**eveal |

Phone example: turn the phone on (prefix) → dial a number (test case values) → press "talk", then "end" (postfix) → the call connects (expected result).

In industry the same idea is called **Arrange – Act – Assert**: Arrange = prefix, Act = test case values, Assert = expected results. Teardown = exit values.

- **Test case:** all four parts together, for one execution.
- **Test set** (test suite): a group of test cases. Coverage is a property of a test set, not of one test.
- **Executable test script:** a test case in a form that a machine can run automatically.

## 4. Test frameworks and JUnit

A **test framework** is a set of ideas and tools that support test automation. A **test driver** runs the software on each test, compares the result, and reports. JUnit (Java) and pytest (Python) belong to the xUnit family. They are made for unit and integration testing, not for system testing.

A framework gives you: **assertions** (the expected results), **fixtures** (prefix and postfix: `@BeforeEach`, `@AfterEach`), **test sets** (classes, suites), and **runners and reports**.

Why not test with `main()` and `println`? A person must read every line; there is no pass/fail count; it stops at the first exception; nobody remembers to run it again.

```java
class CalcTest {
    @Test
    void addTwoPositiveNumbers() {
        assertEquals(5, Calc.add(2, 3), "Calc sum incorrect");
    }
}
```

- `@Test` marks a test method. `2, 3` are the test case values. `5` is the expected result.
- The order is `assertEquals(expected, actual, message)`. The message is printed only when the test fails.
- **Lifecycle:** `@BeforeAll` once → for each test: new object, `@BeforeEach`, the `@Test` method, `@AfterEach` → `@AfterAll` once. Every test gets a new object, so one test cannot change the data of another.
- `assertEquals(5, x)` is better than `assertTrue(x == 5)`: when it fails it says *expected 5 but was 6*, not only *false*.
- In a report, a **failure** means an assertion did not hold. An **error** means an unexpected exception.
- To test an exception in JUnit 5, use `assertThrows(SomeException.class, () -> ...)`. It checks exactly one line.

**The Min example.** `min(list)` returns the smallest element. From the Javadoc we get **seven** tests: five exceptional cases (null list; a null element with other elements; one single null element; elements that cannot be compared; empty list) and only two normal cases (one element; two elements where the minimum is not the first). When you review tests, check the exceptional cases first.

## 5. Beyond one test with one input

**Data-driven tests.** Do not copy the same test 20 times with different numbers. Write the test once and put the data in a table: `@ParameterizedTest` with `@CsvSource` in JUnit 5, `@pytest.mark.parametrize` in pytest. The report shows one line for each row.

**Theories and property-based testing.** A data-driven test lists each input with its output. A **theory** states a rule for all inputs: *if precondition P holds, then after the action, postcondition Q holds.* The steps are **Assume – Act – Assert**. Example: "if a set contains x, then removing x and adding x again gives the same set." Property-based tools (jqwik, Hypothesis) generate hundreds of inputs and reduce a failing input to the smallest example.

**Running tests:** in the IDE, from the command line (`mvn test`, `pytest`), or in continuous integration, where every push runs every test.

## 6. Good tests are FIRST

| | Meaning |
|---|---|
| **F**ast | Runs in milliseconds, so people run it often |
| **I**ndependent | Does not depend on another test or on the order |
| **R**epeatable | Same result every time, on every machine |
| **S**elf-validating | Says pass or fail; no person has to read the output |
| **T**imely | Written together with the code, not months later |

**Five test smells**

| Smell | Why it hurts | Fix |
|---|---|---|
| No assertion | The test can only fail by crashing | Always check an expected result |
| Shared state between tests | Results depend on the run order | Fresh fixture in `@BeforeEach` |
| `assertTrue(a == b)` | The failure message says only "false" | `assertEquals(expected, actual)` |
| One test checks ten things | The first failure hides the others | One behaviour per test |
| Flaky test (time, random, network) | Red and green with no code change | Inject the clock, the seed, or a fake |

A test can pass and still be weak. Example: a test adds "Ada", "Bora", "Cem" to a list (already in order), sorts it, and checks only the first element. In RIPR words: even with a broken `sort`, the state is not infected, or the wrong state is not revealed.

## Key terms

| English | Turkish |
|---|---|
| controllability / observability | kontrol edilebilirlik / gözlemlenebilirlik |
| prefix / postfix values | ön / son değerler |
| expected result | beklenen sonuç |
| test fixture | test düzeneği |
| assertion | doğrulama ifadesi |
| data-driven test | veri güdümlü test |
| flaky test | kararsız test |

## Ideas for your questions

- Show a small method and ask what lowers its controllability or observability, or which habit would fix it.
- Describe the steps of a test and ask which part is the prefix, the test case value, the postfix, the expected result.
- Show a short JUnit test and ask which FIRST letter it breaks, or which test smell it has.
- Give a task and ask: revenue or excise? Worth automating or not?
