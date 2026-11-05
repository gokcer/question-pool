# Week 4 — Traditional vs Agile Development and Agile Testing

**The one idea:** how we build software decides how we test it. In agile development, the automated tests become the specification.

## 1. The traditional way

### Waterfall

Phases follow each other: **Requirements → Design → Implementation → Testing → Deploy and maintain**.

- Each phase ends with a signed document.
- Going back to an earlier phase is expensive ("water does not flow back up").
- Testing starts only when the code is "finished".

Waterfall still fits when requirements really do not change: safety-critical systems (avionics, medical devices, railways), regulated work that needs full traceability, and small, fixed, well-understood problems.

### V-Model

The V-Model connects each design level with a test level:

| Design level (left side) | Test level (right side) |
|---|---|
| Requirements | Acceptance testing |
| System design | System testing |
| Architecture design | Integration testing |
| Module design | Unit testing |

Each design level writes the test plan for its partner. So tests are **planned early**, but they still **run late**, after coding.

### The problem: late feedback

- The customer sees the product only at the end (for example after 12 months).
- **Requirements drift:** the market and the customer change during that time.
- **Late surprises:** misunderstandings appear when fixing them costs the most.
- **Testing gets squeezed:** when coding is late, the test phase becomes shorter.

Root problem: *people are good at approximating and bad at perfecting.* Customers learn what they want when they see it.

## 2. The agile way

### The Agile Manifesto (2001)

| We value more | over |
|---|---|
| Individuals and interactions | processes and tools |
| Working software | comprehensive documentation |
| Customer collaboration | contract negotiation |
| Responding to change | following a plan |

The items on the right still have value. The items on the left matter more.

**Incremental delivery:** give the customer something usable in every iteration (skateboard → scooter → bicycle → motorbike → car), not a wheel, then an axle, then a body, with a usable car only at the end.

### Scrum: how we organise the work

- **3 roles:** *Product Owner* (decides what to build and in which order), *Scrum Master* (coaches the team, removes blockers), *Developers* (decide how, and build it).
- **5 events:** *Sprint* (1–4 weeks, fixed length), *Sprint Planning*, *Daily Scrum* (15 minutes), *Sprint Review* (demo to stakeholders), *Retrospective* (improve the team).
- **3 artifacts:** *Product Backlog* (the ordered wish list), *Sprint Backlog* (the plan for this sprint), *Increment* (usable software that meets the Definition of Done).

### User stories

A user story is a need written in the user's words:

> As a **support technician**, I want **to see a customer's history on demand**, so that **I can solve their problem faster**.

Template: *As a \<role\>, I want \<goal\>, so that \<benefit\>.*

**Acceptance criteria** are the future tests, often written as *Given … when … then …*:

- Given an existing customer, when the technician takes the call, then the history is shown.
- Given a new customer, when the technician takes the call, then "no history" is shown.

A good story is **INVEST**: Independent, Negotiable, Valuable, Estimable, Small, Testable.

### XP (Extreme Programming): how we write good code

XP practices: **test-first (TDD)**, **continuous integration**, **refactoring**, **customer tests** (acceptance tests written with the customer), pair programming, small releases, simple design (YAGNI: "you ain't gonna need it"), on-site customer.

**TDD cycle: Red → Green → Refactor**

1. **Red:** write a small test that fails.
2. **Green:** write just enough code to pass the test.
3. **Refactor:** clean the code; the tests stay green.

One cycle takes minutes. Every test stays in the suite, so the tests become the specification and the safety net.

### Scrum and XP together

| | Scrum | XP |
|---|---|---|
| Main question | How do we organise the work? | How do we write good code? |
| Iteration | Sprint, 1–4 weeks | Iteration, 1–2 weeks |
| Testing | Not prescribed; the team decides | TDD, CI and customer tests are required |

Most teams use Scrum for the process and XP for the engineering.

## 3. Traditional vs agile

| | Traditional | Agile |
|---|---|---|
| Requirements | Fixed at the start, one big document | User stories in a backlog, they evolve |
| Planning | Whole project, at the start | Re-planned every sprint |
| Delivery | One big release at the end | Small working increments |
| Customer | At the start and at the end | Every sprint review, often daily |
| Documentation | Comprehensive, signed | Just enough; code and tests |
| Change | Expensive, resisted | Expected, welcomed |

**How testing changes**

| Traditional testing | Agile testing |
|---|---|
| A phase near the end | Every day, from day one |
| A separate QA team | The whole team owns quality |
| Tests come from requirement documents | Tests come from user stories |
| "Correct" = matches the specification | "Correct" = passes the automated tests |
| Mostly manual; regression is costly | Automated suite runs on every change (CI) |

**Choose by context.** Ask: *How likely is change?* and *How expensive is a mistake in the field?* Stable and high-risk (pacemaker firmware, flight control) → waterfall / V-Model. Uncertain and fast-changing (start-up product, e-commerce site) → agile. Many companies use a hybrid.

## 4. Agile testing (Ammann & Offutt, Chapter 4)

**Cost-of-change curve.** In traditional thinking, a change costs more the later it comes, so we do a lot of analysis and modelling first. This is true only if requirements are complete and stay current. For many projects they do not. Agile methods accept that software starts small and **evolves**.

**A different view of correctness.**

- Traditional: define *all* correct behaviour completely at the beginning (universal).
- Agile: correctness is **relative to a specific set of tests**. If the software passes the tests, it is "correct" (existential). We do not define all behaviours; we demonstrate some.

**The test harness.** A test harness runs all automated tests efficiently and reports the results to the developers.

- Tests must be **automated**. Test automation is a prerequisite for TDD (TDD is not the same thing as test automation).
- Every test needs a **test oracle** that decides if the test passed.
- The tests replace the requirements, so they must be high quality and they must run fast: we run them after every change.

**Continuous integration (CI).** A CI server rebuilds the system and runs the tests again whenever someone checks in a change. Mistakes are found earlier, other developers see changes early, and the risk of code that is not integrated goes down. The CI server decides if the changed system is still correct.

**From user story to tests.** Start with a user story → write an **acceptance test** (it fails) → add TDD tests one by one, changing and refactoring the code → continue until the acceptance test passes.

**Legacy software.** Most software is old and has no tests. Writing a full test set is too expensive. Solution: **incremental TDD** — when you make a change, add tests for only that change. The test collection grows over time.

**The testing shortfall.** Do TDD tests test the software well? **No.** Most agile tests check the **happy path**: what happens in normal use. They often miss confused-user paths, creative-user paths and malicious-user paths.

**What should testers do?**

1. *Human-based:* write more user stories for the non-happy paths. But it is hard to know when you are finished.
2. *Criteria-based:* model the input domain or the behaviour (graphs, logic) and design tests from the model. This gives a built-in sense of completion.

## Key terms

| English | Turkish |
|---|---|
| waterfall | şelale modeli |
| user story / acceptance criteria | kullanıcı hikâyesi / kabul ölçütleri |
| sprint / backlog / increment | sprint / iş listesi / artım |
| test-driven development (TDD) | test güdümlü geliştirme |
| continuous integration (CI) | sürekli entegrasyon |
| test harness | test koşum takımı (tüm testleri çalıştıran düzenek) |
| happy path | mutlu yol (normal kullanım) |
| legacy software | eski (miras) yazılım |

## Ideas for your questions

- Describe a project and ask which process fits best, and why.
- Give a Scrum situation and ask which role, event or artifact it is.
- Show a user story and ask what is wrong with it (which INVEST letter), or which acceptance criterion is testable.
- Describe a step of work and ask which TDD colour it is.
- Ask what "correct" means in agile, or why agile tests alone are not enough.
