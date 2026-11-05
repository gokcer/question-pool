# Week 6 — Static Testing

**The one idea:** some defects are cheapest to find by reading, without running the code.

This chapter has two parts: a recap of the seven testing principles and the test process (ISTQB CTFL 1.3–1.4), and static testing (Myers Chapter 3, CTFL Chapter 3).

## 1. Story: Heartbleed (2014)

OpenSSL protects passwords on a large part of the web. It has a "heartbeat" message: *"I am still here. Send me back these N letters."* The server trusted the number N. It never checked that the message really had N letters.

- An honest user: *say "HAT" (3 letters)* → the server answers `HAT`.
- An attacker: *say "HAT" (500 letters)* → the server answers `HAT` plus 497 letters from its memory: passwords, private keys, other users' data.

An attacker could read up to 64 KB of memory with each request. The defect was in the code for about two years. The fix was one check: if the claimed length is longer than the real data, discard the message.

**Who found it?** Neel Mehta (Google) found it by **reading the code** (a code review). The company Codenomicon found it at the same time with a **fuzzing tool** (by running the code with strange inputs).

**Why did the existing tests miss it for two years?** Every test sent an honest length. Nobody tested a lying length.

One defect, two ways to find it: reading (static) and running (dynamic).

## 2. The seven testing principles (CTFL 1.3)

| # | Principle | In simple words |
|---|---|---|
| 1 | Testing shows the presence of defects, not their absence | Passing tests do not prove that there are no defects |
| 2 | Exhaustive testing is impossible | We cannot try every input and every path |
| 3 | Early testing saves time and money | A defect found early is cheap to fix |
| 4 | Defects cluster together | A few modules contain most of the defects |
| 5 | Tests wear out (pesticide paradox) | The same tests stop finding new defects |
| 6 | Testing is context dependent | A game is not tested like a pacemaker |
| 7 | Absence-of-defects fallacy | No bugs does not mean a useful system |

Examples:

- "Our regression suite found zero bugs in six months, but users report new bugs every week." → principle **5**. Change the tests and add new ones.
- "The app passed all 2,000 tests, but nobody wants to use it because it solves the wrong problem." → principle **7**. (Validation: build the right product.)
- "A start-up tests its game for one day; a bank tests its payment module for one month. Both are right." → principle **6**. Risk decides how much testing is enough.

## 3. The test process (CTFL 1.4)

Seven groups of activities. Each one creates **testware** (work products):

| Activity | Question | Testware |
|---|---|---|
| **Planning** | Why, and how much? | Test plan |
| **Monitoring and control** | Are we on plan? (all the time) | Progress reports |
| **Analysis** | *What* to test? | Test conditions |
| **Design** | *How* to test it? | Test cases |
| **Implementation** | Is everything ready to run? | Test scripts, test data |
| **Execution** | Run and compare | Logs, defect reports |
| **Completion** | What did we learn? | Summary report |

The activities can overlap and repeat. In agile they happen in every sprint.

**Traceability** means keeping the links: requirement → test condition → test case → result → defect. Example: *REQ-12 "a valid coupon gives 10% off"* → *TC-31 "JUICE123 on 100 TL → 90 TL"* → *TC-31 failed* → *BUG-7*.

Why keep the links? **Coverage** (is every requirement tested?), **impact** (a requirement changed: which tests must change?), **reporting** (which requirements are at risk?).

**Two roles:** the **test management role** plans, monitors and controls testing. The **testing role** analyses, designs, implements and runs tests.

## 4. Static and dynamic testing (CTFL 3.1)

| Static testing: read, do not run | Dynamic testing: run and observe |
|---|---|
| Finds **defects** directly (the cause) | Finds **failures** (the symptom); then debugging finds the defect |
| Works on any work product: stories, designs, code, tests | Needs running software |
| Can start before any code exists | Comes later in the life cycle |
| Finds: unclear requirements, unreachable code, unused variables, security holes, breaking of coding standards | Finds: wrong output, crashes, slow responses, memory leaks, behaviour under load |

They are partners, not rivals. Each finds defects that the other misses.

Static testing is done **by people (reviews)** and **by tools (static analysis)**.

**What can be reviewed?** Anything a person can read and understand: requirements, user stories, acceptance criteria, designs, source code, test plans and test cases, contracts, user manuals, web pages. Tools need a formal structure (code, models).

**Why is it worth the time?**

- Inspections and walkthroughs find 30–70% of logic-design and coding errors in typical programs (Myers).
- A defect found in a review costs much less than the same defect found by users.
- **Shift left:** requirement and design defects are found before coding starts.
- Reviewers learn the code; the author learns better style.

## 5. The review process (CTFL 3.2)

| Step | What happens |
|---|---|
| 1. **Planning** | Scope, purpose, checklist, who, how long, exit criteria |
| 2. **Review initiation** | Everyone has the material and knows the goal and their role |
| 3. **Individual review** | Each reviewer reads alone, uses the checklist, notes anomalies |
| 4. **Communication and analysis** | Meeting: discuss the anomalies, decide what is a defect, decide the next step |
| 5. **Fixing and reporting** | Defect reports; the author fixes; the fixes are checked; metrics are reported |

Most defects are found in **step 3**. The meeting is for agreeing, not for reading the material for the first time.

**Roles**

| Role | Job |
|---|---|
| Manager | Decides what is reviewed; gives time and people |
| Author | Created the work product; fixes the defects |
| Moderator (facilitator) | Runs the meeting; keeps it focused and polite |
| Scribe (recorder) | Writes down every anomaly and decision |
| Reviewer | Finds anomalies; can be a tester, developer, user or expert |
| Review leader | Overall responsibility: who, where, when |

One person can have more than one role. But in an inspection the author is never the moderator or the scribe.

**Four review types** (from less formal to more formal):

| | Informal review | Walkthrough | Technical review | Inspection |
|---|---|---|---|---|
| Led by | nobody in particular | **the author** | a moderator | a trained moderator |
| Process | none | light | defined | the full 5-step process |
| Main goal | quick feedback | learn, agree, find anomalies | agree, decide, find anomalies | find as many anomalies as possible |
| Output | maybe nothing | notes | documented decisions | defect log and metrics |
| Example | "Can you look at my pull request?" | the author shows a design to the team | architects review an API design | safety-critical code, line by line |

More formal means more expensive, and it finds more.

**What makes a review succeed?** A clear goal and exit criteria; small pieces (not 2,000 lines at once); time to prepare; checklists based on the defects this team really makes; review the product, not the producer; trained people and support from managers.

## 6. Inspections according to Myers

- A team of **4 people**: a moderator, the programmer, the designer, and a test specialist.
- A session takes **90–120 minutes**; after that, attention drops.
- Speed: about **150 statements per hour**.
- The programmer reads the code aloud, line by line, and explains the logic. The programmer often finds errors just by explaining.
- The team checks the code against a **checklist** of common errors.
- The team **finds** errors; it does not fix them in the meeting. The programmer fixes them later.
- Leave your ego at the door. The results are confidential and are never used to grade the programmer.

**Myers' error checklist: eight categories**

| Category | Example question |
|---|---|
| Data reference | Is every variable set before it is used? Is the index inside the array? |
| Data declaration | Right types? Right default values? |
| Computation | Integer division? Rounding? Overflow? |
| Comparison | `<` or `<=`? Is `and` / `or` correct? |
| Control flow | Does every loop end? Off by one? |
| Interface | Same number, order and units of parameters? |
| Input / output | Is bad input handled? Are errors reported, not hidden? |
| Other | Unused variables? Compiler warnings read? |

## 7. Two examples of static testing

**Reviewing a user story.** *"As a customer, I want to use a coupon, so that I pay less."* Acceptance criteria: (1) a valid coupon gives a discount; (2) the page should respond fast; (3) a coupon can be used many times; (4) an expired coupon shows an error message.

Anomalies: What makes a coupon "valid"? How much is "a discount"? "Fast" is not testable (better: "responds in under 2 seconds for 95% of requests"). "Many times" — how many? It sounds like a fraud risk. Which error message? And what about cases that are missing: a coupon with the wrong format, two coupons together? No code exists yet, and we already found several defects.

**Reading code.** A login function builds its SQL query by putting the user's e-mail text directly inside the SQL string. This is **SQL injection**. If the e-mail is `' OR 1=1--`, the condition is always true and the rest of the query becomes a comment, so the attacker logs in without the password. The fix is to use **parameters**: send the values separately from the SQL text. Checklist category: input / output (the input is not checked).

## 8. Static analysis tools

Tools are programs that read programs.

| Kind | Examples |
|---|---|
| Compiler warnings | `javac -Xlint`, `gcc -Wall` |
| Linters (style and simple bugs) | Ruff (Python), ESLint (JavaScript), Checkstyle (Java) |
| Bug finders | PMD, SpotBugs, Error Prone |
| Security scanners | Semgrep, CodeQL, Bandit |
| Quality platforms | SonarQube / SonarLint |

They run in the editor while you type, and in CI on every push. The earlier, the cheaper.

**A tool and a human read the same code:**

```python
def average(nums=[]):
    total = 0
    for n in nums:
        total += n
    return total / len(nums)
```

- The tool (Ruff) reports B006: a list as a default value is shared between calls.
- A human also asks: what if the list is empty? → `ZeroDivisionError`. The tool said nothing about this.

| People (reviews) | Tools (static analysis) |
|---|---|
| Know the specification and the user | Do not know what the code should do |
| Find missing features, wrong logic, unclear requirements | Find patterns: unused variables, `==` on strings, empty `catch`, injection |
| Slow (about 150 lines per hour), get tired | Fast, consistent, never tired; run on every commit |
| | Give **false positives** (warnings that are not real defects) |

Good practice: let the tool clean the simple things first, so that people can spend their time on the logic.

## 9. Reviews in the age of LLMs

- AI writes code much faster than people can inspect line by line, so a 4-person meeting for every module is not realistic any more.
- What stays: checklists (now also as rules for linters and LLMs), "find, do not fix", "review the product, not the producer".
- Three layers: **tools** (cheap, always the same answer, on every commit) → an **LLM reviewer** (a first read; it can also read the specification, but it does not always give the same answer) → a **human** (decides, owns the result, inspects the risky code).
- **Automation bias:** "the AI said so" is not a review.

**Real world 2026.** Anthropic reported in May 2026 that its model Claude Mythos Preview (Project Glasswing) found more than 10,000 high or critical vulnerabilities, including a 27-year-old bug in OpenBSD. Of 530 bugs reported to developers, 75 were fixed at the time of the report. These numbers come from the company and were not yet checked by others. The lesson: AI is a very strong bug finder, but people must still confirm, rate and fix every bug, and people decide what "correct" means.

## Key terms

| English | Turkish |
|---|---|
| static / dynamic testing | statik / dinamik test |
| review, walkthrough, inspection | gözden geçirme, üzerinden geçme, denetim |
| anomaly | anomali (şüpheli bulgu) |
| moderator / scribe / reviewer / author | moderatör / yazman / gözden geçiren / yazar |
| checklist | kontrol listesi |
| static analysis | statik analiz |
| false positive | yanlış alarm |
| traceability | izlenebilirlik |
| testware | test ürünleri |
| pesticide paradox | böcek ilacı paradoksu |

## Ideas for your questions

- Tell a short story and ask which of the seven principles it shows.
- Describe an activity or a work product and ask which test process activity it belongs to.
- Describe a review meeting and ask which review type it is, or which role a person has.
- Give a defect and ask: can static testing find it, dynamic testing, or both? A tool or only a human?
- Show a small piece of code or a user story and ask which checklist category or which anomaly applies.
