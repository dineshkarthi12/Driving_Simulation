# Car Crash Java – Driving Simulation

A command-line driving simulation. You create a rectangular field, add cars with a
unique name, a starting position, a facing direction and a list of commands, and
then run the simulation. Cars move simultaneously, one command per step, and the
program reports where every car ended up or where and when it collided.

- Java 21, Maven, no runtime dependencies
- JUnit 5 + AssertJ for tests (test scope only)
- No binary files in the repository: the Maven Wrapper is used in *script-only*
  mode, so there is no `maven-wrapper.jar`

---

## Contents

1. [Prerequisites](#prerequisites)
2. [How to run](#how-to-run)
3. [How to run the tests](#how-to-run-the-tests)
4. [User manual](#user-manual)
5. [Example sessions](#example-sessions)
6. [Architecture and design](#architecture-and-design)
7. [Simulation rules](#simulation-rules)
8. [Special cases, assumptions and decisions](#special-cases-assumptions-and-decisions)
9. [Deviations from the specification](#deviations-from-the-specification)

---

## Prerequisites

- **Java 21 or newer** with `java` on `PATH` (or `JAVA_HOME` set).
- **Internet access on the first run.** The Maven Wrapper (`./mvnw`) downloads
  Maven 3.9.11 and the build plugins into `~/.m2`. Later runs work offline.

You do not need to install Maven yourself.

## How to run

```bash
./start.sh
```

`start.sh` builds the application quietly with the Maven Wrapper (tests are
neither compiled nor run, so it starts quickly) and then launches the interactive CLI. Build output
goes to stderr, so the program's own output on stdout stays clean. For example,
you can pipe a scripted session in:

```bash
printf '10 10\n1\nA\n1 2 N\nFFRFFFFRRL\n2\n2\n' | ./start.sh
```

Every run starts from a clean state. The application keeps everything in memory
and writes nothing to disk, so no cars or fields carry over between runs.

To run the built jar directly:

```bash
./mvnw -q -Dmaven.test.skip=true package
java -jar target/driving-simulation.jar
```

## How to run the tests

```bash
./mvnw test
```

The suite has 140 tests (unit tests plus end-to-end CLI tests) and runs in a
few seconds.

| Test class | Covers |
|---|---|
| `DirectionTest` | left/right rotation, movement deltas, parsing (case-insensitive) |
| `CommandTest` | parsing `L`/`R`/`F`, case and whitespace handling, empty and invalid input |
| `PositionTest` | movement, immutability, `(x,y)` formatting |
| `FieldTest` | boundary checks (`0..W-1`, `0..H-1`), rejecting non-positive sizes |
| `SimulationTest` | registration rules: non-blank unique names, free and in-bounds start cells, full field |
| `CarTest` | per-run car state: executing commands, ignoring moves off the field, planned moves, finishing, crashing |
| `SimulationEngineTest` | spec scenarios 1 and 2, parked cars, head-on swaps, 3+ car pile-ups, hitting stopped or crashed cars, following, re-running |
| `InputParserTest` | every kind of user input, valid and invalid |
| `OutputFormatterTest` | exact output line formats |
| `ConsoleAppTest` | full sessions: scenarios 1 and 2 verbatim, error re-prompts, zero cars, full field, parked car, start over, EOF |

`ConsoleAppTest` writes each session as a transcript in the same format as the
specification. Lines starting with `> ` are user input and every other line is
expected output. The test feeds the input lines to the app and compares
everything it prints, character for character.

---

## User manual

1. **Create the field.** Enter the width and height separated by whitespace,
   e.g. `10 10`. Both must be positive whole numbers. A `W x H` field has valid
   coordinates `0..W-1` and `0..H-1`: `(0,0)` is the bottom-left cell and
   `(W-1,H-1)` the top-right one.
2. **Choose from the menu.**
   - `1` adds a car:
     - **Name:** any non-blank text, unique within the field.
     - **Initial position:** `x y Direction`, e.g. `1 2 N`. The direction is one of `N`, `E`, `S`, `W`, in upper or lower case.
     - **Commands:** a string of `L` (turn left 90°), `R` (turn right 90°) and `F` (move forward one cell), e.g. `FFRFF`. Case doesn't matter and spaces are ignored. Pressing Enter without typing any commands adds a *parked* car.
   - `2` runs the simulation and prints the result.
3. **After a run**, choose `1` to start over (new field, no cars) or `2` to exit.

When you enter something invalid, the program prints a message starting with
`Error:` and asks the same question again. Earlier answers are kept, so a bad
position does not make you retype the car's name. Press **Ctrl+D** (end of
input) at any prompt to quit cleanly.

## Example sessions

User input is shown on the lines after each prompt. Both sessions below run
exactly like this and are checked verbatim by `ConsoleAppTest`.

### Scenario 1 – one car

```
Welcome to Car Crash Java!

Please enter the width and height of the simulation field in x y format:
10 10

You have created a field of 10 x 10.

Please choose from the following options:
[1] Add a car to field
[2] Run simulation

1

Please enter the name of the car:
A

Please enter initial position of car A in x y Direction format:
1 2 N

Please enter the commands for car A:
FFRFFFFRRL

Your current list of cars are:
- A, (1,2) N, FFRFFFFRRL

Please choose from the following options:
[1] Add a car to field
[2] Run simulation

2

Your current list of cars are:
- A, (1,2) N, FFRFFFFRRL

After simulation, the result is:
- A, (5,4) S

Please choose from the following options:
[1] Start over
[2] Exit

2

Thank you for running the simulation. Goodbye!
```

### Scenario 2 – two cars colliding

```
...
Your current list of cars are:
- A, (1,2) N, FFRFFFFRRL
- B, (7,8) W, FFLFFFFFFF

After simulation, the result is:
- A, collides with B at (5,4) at step 7
- B, collides with A at (5,4) at step 7
...
```

### Validation and a parked car

```
Please enter initial position of car B in x y Direction format:
1 2 E

Error: Position (1,2) is already occupied by car A.

Please enter initial position of car B in x y Direction format:
3 3 e

Please enter the commands for car B:


Your current list of cars are:
- A, (1,2) N, FFRFFFFRRL
- B, (3,3) E, (no commands)
```

---

## Architecture and design

```
com.carcrash
├── Main                      wires System.in/System.out to ConsoleApp
├── domain                    pure model, no I/O
│   ├── Direction             enum N/E/S/W: turnLeft(), turnRight(), dx/dy, fromSymbol()
│   ├── Command               enum L/R/F: fromChar(), parseAll(), toText()
│   ├── Position              immutable record (x, y): moved(Direction)
│   ├── Field                 immutable record (width, height): contains(Position)
│   ├── CarSpec               immutable record: name, start, direction, commands
│   └── Simulation            aggregate: field + registered cars; enforces registration rules
├── simulation
│   ├── SimulationEngine      stateless step-by-step runner with collision detection
│   ├── Car                   package-private mutable per-run state of one car
│   ├── SimulationResult      ordered list of outcomes
│   └── CarOutcome            sealed: Finished(name, position, direction) | Collided(name, others, position, step)
└── cli
    ├── ConsoleApp            dialogue flow; BufferedReader + PrintStream are injected
    ├── InputParser           text → domain values (shape of the input only)
    └── OutputFormatter       domain values → output lines
```

### Key design choices

- **The domain is separate from I/O.** Nothing in `domain` or `simulation` reads
  or prints anything, so the whole simulation can be tested without a console,
  and a different front end (web, REST) could reuse the model unchanged.
- **Immutable where possible.** `Position`, `Field`, `CarSpec`, `CarOutcome` and
  `SimulationResult` are records, and their lists are copied defensively.
- **Registered cars and running cars are different types.**
  - `CarSpec` is what the user registered and never changes.
  - `Car` holds the mutable state of one run. It is package-private to the
    engine, which creates fresh `Car`s from the specs each time, so running a
    simulation never changes the registered cars (a test checks this).
- **Each rule lives in one place.** The domain types enforce their own
  invariants: `Field` requires a positive size, `Direction` and `Command` reject
  unknown symbols, and `Simulation` checks names and start cells. `InputParser`
  only checks the shape of the text, such as the number of tokens and whether
  they are whole numbers.
- **Swaps are planned before any car moves.** In each step the engine first works
  out where every car would end up, cancels the moves of cars that would swap
  cells, and only then carries out the remaining commands. No move ever has to be
  undone.
- **`Direction` owns the rotation and movement rules.** Directions are declared
  clockwise, so turning is index arithmetic. The movement deltas live in the enum,
  which keeps `Position.moved()` a one-liner with no `switch`.
- **A sealed `CarOutcome` with pattern matching** makes the two result shapes
  explicit. The formatter's `switch` is checked for exhaustiveness by the compiler.
- **Errors are handled in one place.**
  - Domain validation and parsing throw `IllegalArgumentException` with a
    message meant for the user.
  - `ConsoleApp.ask()` is the only place that catches them. It prints `Error: …`
    and asks again, so the user never sees a stack trace.
  - End of input is turned into a private control-flow exception that ends the
    session politely.
- **Easy to test.** `ConsoleApp` takes its reader, writer and engine through its
  constructor, and the CLI tests use in-memory streams.
- **No frameworks.** The problem is small, so plain Java 21 keeps the solution
  easy to read. JUnit and AssertJ are test-scope only.

---

## Simulation rules

- **Simultaneous steps.** In step *N*, every car that is still active runs its
  *N*th command: `L` and `R` rotate in place, and `F` moves one cell forward.
  Collisions are checked only after **all** cars have acted in that step.
- **Boundaries.** An `F` that would leave the field is ignored and the car stays
  where it is. The car's later commands still run.
- **Collision.** Two or more cars on the same cell after a step have collided.
  - Every car involved is reported as
    `- A, collides with B at (x,y) at step N`.
  - Collided cars stop and ignore their remaining commands. They stay on the field
    and can still be hit.
- **Finished cars.** A car that runs out of commands stops, stays on the field and
  can still be hit.
- **Result.** Cars are reported in the order they were added. Cars that did not
  collide are shown as `- A, (x,y) D`.
- **End of the run.** The simulation ends when no car has commands left.

## Special cases, assumptions and decisions

The specification leaves the following cases open. Each one was decided as
described below and is covered by tests.

### 1. Two cars swapping cells in one step (head-on pass-through): **collision**

If car A moves from `p` to `q` while car B moves from `q` to `p` in the same
step, the cars would have to pass through each other. On a real road that is a
head-on crash. A check that only compares cells would miss it just because time
moves in discrete steps.

- **Neither move completes.** Both cars stay on their **pre-move cells** and are
  marked as collided at that step.
- **Each car is reported at its own cell,** colliding with the other. For A at
  `(1,1)` facing E and B at `(2,1)` facing W, both driving forward:

  ```
  - A, collides with B at (1,1) at step 1
  - B, collides with A at (2,1) at step 1
  ```

  Reporting each car at the cell where it actually is keeps "position" meaning
  the same thing everywhere.
- **Only real moves count.** Both cars must actually have moved. A car whose `F`
  was blocked by the boundary did not move, so it cannot swap.
- **Knock-on collisions.** Swaps are handled before same-cell checks. If
  cancelling a move leaves a car on a cell that a third car has just entered,
  that also counts as a collision, and the car lists every car it collided with
  (`SimulationEngineTest#swapCancellationThatLeavesACarOnAnOccupiedCellAlsoCollidesThere`).

### 2. Moving into the cell of a stopped car: **collision**

A car that drives into a cell occupied by a parked car, a car that has finished
its commands, or a car that has already crashed collides with every car in that
cell.

- **Parked or finished cars** that are hit become collided too, at that step,
  because they were struck.
- **Cars that had already crashed** keep their original report (first
  collision, original step). The newcomer lists all of them, e.g.
  `- C, collides with A, B at (1,0) at step 3`.

### 3. Three or more cars in one cell: **list all other cars**

Each car lists every other car in the cell in alphabetical order, separated by
commas: `- A, collides with B, C at (1,1) at step 1`. Sorting gives the same
output regardless of the order in which cars were added.

### 4. Following into a cell vacated in the same step: **no collision**

Positions are compared only after every car has moved. So if A moves into the
cell B is leaving in that same step, there is no collision (think of cars driving
in convoy).

### 5. Empty command string: **allowed, the car is parked**

A car with no commands stays at its starting position for the whole simulation
and can still be hit. This is useful for modelling obstacles. In the car list it
is shown as `- P, (2,2) N, (no commands)` rather than with nothing after the
comma, so it doesn't look like a formatting bug.

### 6. Duplicate car names: **rejected, prompt repeats**

Names are trimmed and compared **case-sensitively**, so `A` and `a` are
different cars. This matches the spec's use of names as plain labels. A blank
name is rejected too.

### 7. Starting position occupied or outside the field: **rejected, prompt repeats**

Two cars cannot start on the same cell, since they would already have collided
before step 1. Positions outside `0..W-1 × 0..H-1` are rejected with a message
that states the valid range. Only the position prompt is repeated; the name you
already entered is kept.

If every cell of the field is already taken, choosing *Add a car* shows
`Error: The field is full; …` and returns to the menu straight away, instead of
asking for a name and then rejecting every possible position.

### 8. Input formatting: **forgiving**

- Directions and commands are case-insensitive (`1 2 n`, `ffrl`).
- Leading, trailing and repeated whitespace is ignored everywhere.
- Spaces inside a command string are ignored (`FF RL` is the same as `FFRL`).

### 9. Invalid command characters: **the whole string is rejected**

For example, `FFX` is rejected with `Invalid command 'X'` and the prompt
repeats. Quietly dropping the bad characters would run a different route from
the one the user meant.

### 10. Running with zero cars: **message, back to the menu**

`Error: There are no cars on the field yet. Please add at least one car first.`

### 11. Invalid menu choices and field sizes: **clear error, prompt repeats**

- **Menu:** anything that isn't one of the listed numbers is rejected (`3`, `0`,
  `abc`, blank).
- **Field size:**
  - it must be exactly two whole numbers;
  - `0`, negative numbers and values too large for an `int` are rejected;
  - a `1 x 1` field is valid.

### 12. End of input (Ctrl+D / closed stdin): **graceful exit**

At any prompt, end of input prints the goodbye message and exits with status 0,
with no stack trace. This also makes piped, scripted sessions safe.

### 13. "Start over": **full reset**

Choosing `1` after a run throws away the field and all cars and asks for a new
field size. Names and positions from the previous round can be reused.

### 14. Other assumptions

- Coordinates are whole numbers. A car's start must be inside the field, and the
  engine ensures it stays there.
- Rotating (`L`/`R`) takes a full step like `F` does. A car that is rotating in
  place can be hit.
- Step numbers start at 1: the first command is step 1, which matches the
  spec's "at step 7".
- The input does not limit the number of cars or commands.

## Deviations from the specification

- **Typo fixes in the prompts.**
  - Scenario 1 says *"width and heigh"*. The app prints *"width and height"*, as
    in scenario 2.
  - In scenario 2, the position and command prompts for car **B** say *"car A"*.
    The app uses the name of the car being added (*"car B"*). This is clearly a
    copy-paste error in the spec.
- **Error messages.**
  - The spec doesn't define any. Every error line starts with `Error:` and is
    followed by a blank line and the original prompt again.
  - If input ends at a menu, the menu's trailing blank line is followed by one
    more before the goodbye.
- **Additional output.** The spec allows extra output. Apart from the error lines
  above, the app prints nothing that isn't in the spec's sessions, so scenarios
  1 and 2 match them exactly (except the typo fixes).
- **Menu markers.** The spec's `\[1]` is Markdown escaping for `[1]`, so the app
  prints `[1]`.
