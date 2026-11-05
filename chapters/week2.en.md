# Week 2 — Test Design

**The one idea:** we cannot test everything, so we need a clear rule (a coverage criterion) that tells us which tests to write and when to stop.

## 1. Testing and risk

- **Testing can only show that failures are present. It cannot show that there are none.**
- Every time we use software, there is some risk. Sometimes the risk is small. Sometimes it is a catastrophe.
- Testers and developers work together to **reduce risk**.
- For a large system the word "correct" has little meaning (is a building "correct"?). Engineers use **models** and **abstraction** to manage this complexity.

## 2. Testing and debugging

| Term | Meaning |
|---|---|
| **Testing** | Evaluating software by observing its execution |
| **Test failure** | A test execution that results in a software failure |
| **Debugging** | Finding the fault, after we have seen a failure |

Not every input makes a fault produce a failure.

## 3. The RIPR model

A test finds a fault only when **four** things happen:

1. **Reachability** — the test executes the place in the code that has the fault.
2. **Infection** — after that, the program state is wrong.
3. **Propagation** — the wrong state travels to an output or to the final state.
4. **Revealability** — the tester (or the test's assertion) looks at that wrong part of the output.

Example from Week 1 (`numZero` starts its loop at 1, not 0):

- `[2, 7, 0]` reaches the fault and infects the state (`i` is wrong), but the wrong state does **not propagate**: the output is still correct.
- `[0, 2, 7]` reaches, infects and propagates: the output is 0, not 1. If the test checks the output, the failure is **revealed**.

A test with no check of the result (no oracle) can reach, infect and propagate — and still find nothing.

## 4. Who does testing?

- **Test engineer:** designs test inputs, produces test values, runs test scripts, analyses results, reports to developers and managers.
- **Test manager:** leads the test engineers, sets test policies and processes, talks with other managers.

## 5. Test levels

| Level | Question it answers |
|---|---|
| **Acceptance testing** | Is the software acceptable to the user? |
| **System testing** | Does the whole system work? |
| **Integration testing** | Do the modules work together? |
| **Module testing** | Does each class, file or component work? |
| **Unit testing** | Does each unit (method) work alone? |

Unit and module testing are also called developer testing.

## 6. Coverage criteria

Even a tiny program has too many inputs. The method `computeAverage(int A, int B, int C)` has three 32-bit integers: more than 80 octillion possible tests. The input space is practically infinite.

A **coverage criterion** gives a structured way to search this space: few tests, little overlap, many problems found.

| Term | Meaning | Example |
|---|---|---|
| **Test requirement (TR)** | One specific thing that a test must satisfy or cover | "Execute statement 12" |
| **Coverage criterion** | A rule that produces test requirements | "Cover every statement" |

Why criteria are useful:

- most "bang for the buck": more faults found with fewer tests;
- **traceability**: each test has a reason;
- easier regression testing;
- a **stopping rule**: we know when testing is finished;
- good tool support.

All criteria are built on only **four structures**: input domains, graphs, logic expressions, and syntax descriptions.

## 7. Black-box, white-box, model-based

- **Black-box testing:** tests come from external descriptions (requirements, specifications, design).
- **White-box testing:** tests come from the source code (statements, branches, conditions).
- **Model-based testing:** tests come from a model of the software, for example a UML diagram.

A more useful question than the colour of the box: **from which level of abstraction do we derive the tests?**

## 8. The four test activities

| Activity | What it does | Needs |
|---|---|---|
| **1. Test design** | Chooses test values | (a) *criteria-based*: maths, programming, testing knowledge. (b) *human-based*: knowledge of the domain and of users |
| **2. Test automation** | Puts the values into scripts that can run | Programming |
| **3. Test execution** | Runs the tests and records the results | Basic computer skills; careful work |
| **4. Test evaluation** | Judges the results and reports to developers | Domain knowledge |

- Criteria-based design can be blind to special situations, so human-based design is also needed.
- These activities need different skills. Many teams still use the same people for all four, and this wastes resources.
- Other activities: **test management** (policy, team, how much automation), **test maintenance** (keep tests for reuse when the software changes), **test documentation** (each test should say *why* it exists: this is traceability).

## 9. Model-driven test design (MDTD)

MDTD lets the test designer work at a higher level of abstraction:

1. **Software artifact** (code, requirements, design) →
2. **Model / structure** (for example a graph) →
3. **Test requirements** (from a criterion) →
4. **Refined test requirements** →
5. **Input values** → **test cases** → **test scripts** → **test results** → **pass / fail**.

Steps 1–4 are at the *design abstraction level*. Step 5 is at the *implementation level*.

**Small example.** The method `indexOf(Node n)` searches a list with a `for` loop.

- Model: a control flow graph with 5 nodes. Edges: 1→2, 2→3, 3→2, 3→4, 2→5. Start node 1. End nodes 4 and 5.
- Criterion: *edge-pair coverage* (every path of length 2). This gives 6 test requirements: [1,2,3], [1,2,5], [2,3,4], [2,3,2], [3,2,3], [3,2,5].
- Three test paths cover all six: [1,2,5], [1,2,3,2,5], [1,2,3,2,3,4].
- Last step: find input values that make the program follow each path.

The designer does the maths once. Other people can then find values, automate, run and evaluate.

## Key terms

| English | Turkish |
|---|---|
| reachability, infection, propagation, revealability | ulaşma, bulaşma, yayılma, açığa çıkma |
| test requirement | test gereksinimi |
| coverage criterion | kapsama ölçütü |
| traceability | izlenebilirlik |
| test oracle | test kâhini (beklenen sonucu bilen şey) |
| acceptance testing | kabul testi |

## Ideas for your questions

- Give a small piece of code with a fault and one test input. Ask which RIPR step fails.
- Describe a task (for example "writes JUnit scripts for given values") and ask which test activity it is.
- Give a criterion and an artifact and ask how many test requirements there are.
- Describe a situation and ask which test level it is.
