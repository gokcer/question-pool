# Week 5 — Testing Criteria

**The one idea:** a coverage criterion turns "test it well" into a list of things to cover, so we can count how far we are and know when to stop.

## 1. Two definitions

| Term | Meaning |
|---|---|
| **Test requirement (TR)** | A specific element of a software artifact that a test case must satisfy or cover |
| **Coverage criterion (C)** | A rule, or a collection of rules, that imposes test requirements on a test set |

**Criterion + artifact = a set of test requirements.**

- *Branch coverage:* each decision gives 2 TRs (the decision is true; the decision is false).
- *Call coverage:* each method gives 1 TR (the method is called at least once).
- A TR depends on the artifact: "execute statement 42" has a meaning only if there is a statement 42.

The tester's job in one sentence: **define a model of the software, then find ways to cover it.**

## 2. The jelly bean example

A bag has jelly beans with six flavours and four colours:

| Flavour | Colour |
|---|---|
| Lemon | Yellow |
| Pistachio | Green |
| Cantaloupe | Orange |
| Pear | White |
| Tangerine | Orange |
| Apricot | Yellow |

| Jelly bean world | Software world |
|---|---|
| Tasting one bean | Running one test case |
| The beans we taste | The test set T |
| "Taste one bean of each flavour" | A coverage criterion |
| Lemon, Pistachio, … | The test requirements |
| Is this yellow bean Lemon or Apricot? | A controllability problem |

- **Flavour criterion:** TR = {Lemon, Pistachio, Cantaloupe, Pear, Tangerine, Apricot} → 6 TRs.
- **Colour criterion:** TR = {Yellow, Green, Orange, White} → 4 TRs.

## 3. Coverage and coverage level

**Coverage:** a test set T satisfies criterion C if, for **every** test requirement in TR, at least one test in T satisfies it.

**Coverage level** = (number of TRs satisfied by T) ÷ (number of TRs in TR).

| Test set | Flavour criterion (6 TRs) | Colour criterion (4 TRs) |
|---|---|---|
| T1 = 3 Lemon, 1 Pistachio, 2 Cantaloupe, 1 Pear, 1 Tangerine, 4 Apricot | 6/6 = 100% | 4/4 = 100% |
| T2 = 1 Lemon, 2 Pistachio, 1 Pear, 3 Tangerine | 4/6 ≈ 67% (no Cantaloupe, no Apricot) | 4/4 = 100% |

- T2 covers every colour but not every flavour.
- More tests do not mean more coverage: T1 has 12 beans, but 6 well-chosen beans give the same flavour coverage.

**Minimal and minimum**

- A **minimal** test set: if you remove any one test, coverage is lost. (T1 without 2 Lemon, 1 Cantaloupe and 3 Apricot → 6 beans, minimal.)
- A **minimum** test set: no smaller test set satisfies all the TRs.
- Checking that a set is minimal is easy. Finding a minimum set is, in general, undecidable.

## 4. Infeasible test requirements

An **infeasible** test requirement cannot be satisfied: no test values exist for it.

```java
void process(int x) {
    if (x > 10 && x < 5) {
        System.out.println("impossible");   // dead code
    }
}
```

No value of `x` makes `x > 10 && x < 5` true. So "execute the println line" is an infeasible TR.

- Here we can see it. In general, detecting infeasible TRs is **undecidable** for most criteria.
- So 100% coverage is often impossible in practice.
- Jelly beans: if no bag contains a Tangerine bean, the best possible flavour coverage is 5/6 ≈ 83%.
- In practice we drop infeasible TRs, or replace them with weaker TRs.

## 5. Two ways to use a criterion

1. **Generate** test values directly to satisfy the criterion. This is the obvious way, but it is very hard without tools.
2. Write the tests in another way, then **measure** them against the criterion. Industry usually prefers this. It can mislead: if the tests do not reach 100%, what does that mean?

| | Meaning |
|---|---|
| **Generator** | A procedure that automatically creates test values that satisfy a criterion |
| **Recognizer** | A procedure that decides if a given set of test values satisfies a criterion |

Both problems are undecidable for most criteria, but recognizing is possible far more often than generating. This is why there are many **coverage analysis tools**.

## 6. Subsumption

**Criterion C1 subsumes C2** if and only if **every** test set that satisfies C1 also satisfies C2.

- The flavour criterion subsumes the colour criterion: if we taste every flavour, we taste every colour.
- The colour criterion does **not** subsume the flavour criterion (see T2).
- **Branch coverage subsumes statement coverage:** a test set that covers every branch also covers every statement. The opposite is not true.

## 7. Statement coverage vs branch coverage

```java
int divideBy(int x) {
    int y = 0;
    if (x > 0) {
        y = 10;
    }
    return 100 / y;
}
```

- Statement coverage: 4 TRs (the four statements).
- Branch coverage: 2 TRs (`x > 0` is true; `x > 0` is false).

| Test set | Statement | Branch | Result |
|---|---|---|---|
| { x = 5 } | 100% | 50% | Returns 10, the test passes |
| { x = 5, x = 0 } | 100% | 100% | x = 0 → `ArithmeticException: / by zero` |

**100% statement coverage, and the fault was still missed.** The second test, needed for branch coverage, finds it.

Be careful: subsumption is about test requirements, not about specific values. It is only a rough guide to how many faults a criterion finds. One test set for a weaker criterion can still find a fault that one test set for a stronger criterion misses.

## 8. Why use criteria?

- Fewer tests that are better at finding faults; little overlap.
- **Traceability:** the "why" of each test is answered.
- A **stopping rule:** we know in advance how many tests are needed.
- Built-in support for regression testing; natural to automate.

**A good coverage criterion:** (1) its test requirements are fairly easy to compute automatically; (2) test values can be generated efficiently; (3) the resulting tests reveal as many faults as possible.

## 9. Four structures, and RIPR

All criteria are defined on four structures. Each one goes one step deeper in the RIPR model:

| Structure | Criteria ask the tests to … | RIPR |
|---|---|---|
| **Input space** | explore the input domain (blocks of values) | — |
| **Graphs** | reach nodes, edges, paths | Reachability |
| **Logic** | reach a predicate and try truth values | + Infection |
| **Syntax** | reach, infect, and make the change visible | + Propagation |

**Revealability** always depends on the automated test itself (its assertions), whatever criterion we choose.

These structures can come from many artifacts. For example, a graph can come from source code, a design, a use case or a state machine.

## 10. In practice

- Many companies still do "monkey testing": a person clicks around with no plan. Some automate tests that people designed. Companies that use both automation **and** criteria save money and find more faults.
- LLMs and coding agents can generate test cases. You still need criteria to judge if those tests are enough.

## Key terms

| English | Turkish |
|---|---|
| test requirement | test gereksinimi |
| coverage criterion | kapsama ölçütü |
| coverage level | kapsama düzeyi |
| infeasible | karşılanamaz |
| subsumption / subsumes | kapsayıcılık / kapsar |
| statement / branch coverage | deyim / dal kapsaması |
| dead code | ölü kod |
| minimal / minimum test set | indirgenemez / en küçük test kümesi |

## Ideas for your questions

- Give a small piece of code and a test set. Ask for the statement or branch coverage level.
- Give a criterion and an artifact. Ask how many test requirements there are.
- Give two criteria and ask which one subsumes the other, or give a test set that shows that one does not.
- Show code and ask which test requirement is infeasible.
- Make your own "jelly bean" example (for example pizzas, phones or bus lines) and ask about coverage.
