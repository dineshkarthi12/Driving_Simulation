package com.carcrash.cli;

import com.carcrash.simulation.SimulationEngine;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests of the interactive dialogue.
 *
 * <p>Sessions are written as transcripts in the same shape as the specification:
 * lines starting with {@code "> "} are what the user types, every other line is
 * expected program output. The helper feeds the typed lines to the app and
 * asserts that the output equals the transcript with the typed lines removed.
 */
class ConsoleAppTest {

    private static final String INPUT_MARKER = "> ";

    private static String run(String input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new ConsoleApp(new BufferedReader(new StringReader(input)), out, new SimulationEngine()).run();
        return buffer.toString(StandardCharsets.UTF_8).replace(System.lineSeparator(), "\n");
    }

    private static void assertSession(String transcript) {
        String input = transcript.lines()
                .filter(line -> line.startsWith(INPUT_MARKER))
                .map(line -> line.substring(INPUT_MARKER.length()) + "\n")
                .collect(Collectors.joining());
        String expectedOutput = transcript.lines()
                .filter(line -> !line.startsWith(INPUT_MARKER))
                .map(line -> line + "\n")
                .collect(Collectors.joining());

        assertThat(run(input)).isEqualTo(expectedOutput);
    }

    @Test
    void scenario1RunningSimulationWithOneCar() {
        assertSession("""
                Welcome to Car Crash Java!

                Please enter the width and height of the simulation field in x y format:
                > 10 10

                You have created a field of 10 x 10.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 1

                Please enter the name of the car:
                > A

                Please enter initial position of car A in x y Direction format:
                > 1 2 N

                Please enter the commands for car A:
                > FFRFFFFRRL

                Your current list of cars are:
                - A, (1,2) N, FFRFFFFRRL

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 2

                Your current list of cars are:
                - A, (1,2) N, FFRFFFFRRL

                After simulation, the result is:
                - A, (5,4) S

                Please choose from the following options:
                [1] Start over
                [2] Exit

                > 2

                Thank you for running the simulation. Goodbye!
                """);
    }

    @Test
    void scenario2RunningSimulationWithMultipleCars() {
        assertSession("""
                Welcome to Car Crash Java!

                Please enter the width and height of the simulation field in x y format:
                > 10 10

                You have created a field of 10 x 10.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 1

                Please enter the name of the car:
                > A

                Please enter initial position of car A in x y Direction format:
                > 1 2 N

                Please enter the commands for car A:
                > FFRFFFFRRL

                Your current list of cars are:
                - A, (1,2) N, FFRFFFFRRL

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 1

                Please enter the name of the car:
                > B

                Please enter initial position of car B in x y Direction format:
                > 7 8 W

                Please enter the commands for car B:
                > FFLFFFFFFF

                Your current list of cars are:
                - A, (1,2) N, FFRFFFFRRL
                - B, (7,8) W, FFLFFFFFFF

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 2

                Your current list of cars are:
                - A, (1,2) N, FFRFFFFRRL
                - B, (7,8) W, FFLFFFFFFF

                After simulation, the result is:
                - A, collides with B at (5,4) at step 7
                - B, collides with A at (5,4) at step 7

                Please choose from the following options:
                [1] Start over
                [2] Exit

                > 2

                Thank you for running the simulation. Goodbye!
                """);
    }

    @Test
    void invalidFieldSizesAreRejectedUntilAValidOneIsEntered() {
        assertSession("""
                Welcome to Car Crash Java!

                Please enter the width and height of the simulation field in x y format:
                > ten ten

                Error: Width must be a whole number, but got 'ten'.

                Please enter the width and height of the simulation field in x y format:
                > 0 10

                Error: Width and height must be positive whole numbers.

                Please enter the width and height of the simulation field in x y format:
                > -5 5

                Error: Width and height must be positive whole numbers.

                Please enter the width and height of the simulation field in x y format:
                > 10

                Error: Please enter exactly two numbers: width and height, e.g. 10 10.

                Please enter the width and height of the simulation field in x y format:
                > \s 5    3\s

                You have created a field of 5 x 3.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation


                Thank you for running the simulation. Goodbye!
                """);
    }

    @Test
    void invalidMenuOptionsAreRejected() {
        assertSession("""
                Welcome to Car Crash Java!

                Please enter the width and height of the simulation field in x y format:
                > 10 10

                You have created a field of 10 x 10.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 3

                Error: Invalid option '3'. Please enter a number from 1 to 2.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > abc

                Error: Invalid option 'abc'. Please enter a number from 1 to 2.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation


                Thank you for running the simulation. Goodbye!
                """);
    }

    @Test
    void runningWithoutCarsShowsMessageAndReturnsToMenu() {
        assertSession("""
                Welcome to Car Crash Java!

                Please enter the width and height of the simulation field in x y format:
                > 10 10

                You have created a field of 10 x 10.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 2

                Error: There are no cars on the field yet. Please add at least one car first.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation


                Thank you for running the simulation. Goodbye!
                """);
    }

    @Test
    void invalidCarDetailsAreRejectedAndOnlyThatPromptIsRepeated() {
        assertSession("""
                Welcome to Car Crash Java!

                Please enter the width and height of the simulation field in x y format:
                > 10 10

                You have created a field of 10 x 10.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 1

                Please enter the name of the car:
                > A

                Please enter initial position of car A in x y Direction format:
                > 1 2 N

                Please enter the commands for car A:
                > F

                Your current list of cars are:
                - A, (1,2) N, F

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 1

                Please enter the name of the car:
                >\s\s\s

                Error: Car name must not be empty.

                Please enter the name of the car:
                > A

                Error: A car named A already exists.

                Please enter the name of the car:
                > B

                Please enter initial position of car B in x y Direction format:
                > 1 2 E

                Error: Position (1,2) is already occupied by car A.

                Please enter initial position of car B in x y Direction format:
                > 10 0 E

                Error: Position (10,0) is outside the field. Valid x is 0..9 and valid y is 0..9.

                Please enter initial position of car B in x y Direction format:
                > 3 3 X

                Error: Invalid direction 'X'. Use one of N, E, S, W.

                Please enter initial position of car B in x y Direction format:
                > 3 3

                Error: Please enter the position as x y Direction, e.g. 1 2 N.

                Please enter initial position of car B in x y Direction format:
                > 3 3 e

                Please enter the commands for car B:
                > FFX

                Error: Invalid command 'X'. Only L, R and F are allowed.

                Please enter the commands for car B:
                > ff rl

                Your current list of cars are:
                - A, (1,2) N, F
                - B, (3,3) E, FFRL

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation


                Thank you for running the simulation. Goodbye!
                """);
    }

    @Test
    void parkedCarIsListedWithoutCommandsAndCanBeHit() {
        assertSession("""
                Welcome to Car Crash Java!

                Please enter the width and height of the simulation field in x y format:
                > 5 5

                You have created a field of 5 x 5.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 1

                Please enter the name of the car:
                > P

                Please enter initial position of car P in x y Direction format:
                > 2 2 N

                Please enter the commands for car P:
                >\s

                Your current list of cars are:
                - P, (2,2) N, (no commands)

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 1

                Please enter the name of the car:
                > A

                Please enter initial position of car A in x y Direction format:
                > 0 2 E

                Please enter the commands for car A:
                > FFF

                Your current list of cars are:
                - P, (2,2) N, (no commands)
                - A, (0,2) E, FFF

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 2

                Your current list of cars are:
                - P, (2,2) N, (no commands)
                - A, (0,2) E, FFF

                After simulation, the result is:
                - P, collides with A at (2,2) at step 2
                - A, collides with P at (2,2) at step 2

                Please choose from the following options:
                [1] Start over
                [2] Exit

                > 2

                Thank you for running the simulation. Goodbye!
                """);
    }

    @Test
    void startOverResetsFieldAndCars() {
        assertSession("""
                Welcome to Car Crash Java!

                Please enter the width and height of the simulation field in x y format:
                > 10 10

                You have created a field of 10 x 10.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 1

                Please enter the name of the car:
                > A

                Please enter initial position of car A in x y Direction format:
                > 1 2 N

                Please enter the commands for car A:
                > FFRFFFFRRL

                Your current list of cars are:
                - A, (1,2) N, FFRFFFFRRL

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 2

                Your current list of cars are:
                - A, (1,2) N, FFRFFFFRRL

                After simulation, the result is:
                - A, (5,4) S

                Please choose from the following options:
                [1] Start over
                [2] Exit

                > 1

                Please enter the width and height of the simulation field in x y format:
                > 3 3

                You have created a field of 3 x 3.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 1

                Please enter the name of the car:
                > A

                Please enter initial position of car A in x y Direction format:
                > 1 2 N

                Please enter the commands for car A:
                > FFF

                Your current list of cars are:
                - A, (1,2) N, FFF

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 2

                Your current list of cars are:
                - A, (1,2) N, FFF

                After simulation, the result is:
                - A, (1,2) N

                Please choose from the following options:
                [1] Start over
                [2] Exit

                > 2

                Thank you for running the simulation. Goodbye!
                """);
    }

    @Test
    void endOfInputAtFirstPromptExitsGracefully() {
        assertSession("""
                Welcome to Car Crash Java!

                Please enter the width and height of the simulation field in x y format:

                Thank you for running the simulation. Goodbye!
                """);
    }

    @Test
    void endOfInputWhileAddingCarExitsGracefully() {
        assertSession("""
                Welcome to Car Crash Java!

                Please enter the width and height of the simulation field in x y format:
                > 10 10

                You have created a field of 10 x 10.

                Please choose from the following options:
                [1] Add a car to field
                [2] Run simulation

                > 1

                Please enter the name of the car:
                > A

                Please enter initial position of car A in x y Direction format:

                Thank you for running the simulation. Goodbye!
                """);
    }
}
