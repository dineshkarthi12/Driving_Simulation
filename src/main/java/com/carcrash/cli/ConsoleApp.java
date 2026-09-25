package com.carcrash.cli;

import com.carcrash.domain.CarSpec;
import com.carcrash.domain.Command;
import com.carcrash.domain.Field;
import com.carcrash.domain.Position;
import com.carcrash.domain.Simulation;
import com.carcrash.simulation.SimulationEngine;
import com.carcrash.simulation.SimulationResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Interactive command-line front end. Input and output streams are injected so
 * the full dialogue can be driven from tests.
 *
 * <p>Every prompt re-asks until valid input is given. End of input (e.g. Ctrl+D)
 * at any prompt ends the program gracefully.
 */
public final class ConsoleApp {

    private static final String ERROR_PREFIX = "Error: ";

    private static final int ADD_CAR = 1;
    private static final int RUN_SIMULATION = 2;
    private static final int START_OVER = 1;
    private static final int EXIT = 2;

    private final BufferedReader in;
    private final PrintStream out;
    private final SimulationEngine engine;

    public ConsoleApp(BufferedReader in, PrintStream out, SimulationEngine engine) {
        this.in = Objects.requireNonNull(in, "in");
        this.out = Objects.requireNonNull(out, "out");
        this.engine = Objects.requireNonNull(engine, "engine");
    }

    /** Runs the interactive session until the user exits or input ends. */
    public void run() {
        out.println("Welcome to Car Crash Java!");
        out.println();
        try {
            do {
                Simulation simulation = new Simulation(askField());
                buildAndRun(simulation);
            } while (askStartOver());
        } catch (EndOfInputException e) {
            // The user closed the input stream: fall through to the farewell.
        }
        out.println("Thank you for running the simulation. Goodbye!");
    }

    private Field askField() {
        Field field = ask("Please enter the width and height of the simulation field in x y format:",
                InputParser::parseField);
        out.println("You have created a field of " + field.width() + " x " + field.height() + ".");
        out.println();
        return field;
    }

    /** Shows the main menu until a simulation has been run. */
    private void buildAndRun(Simulation simulation) {
        while (true) {
            int choice = askMenu("Add a car to field", "Run simulation");
            if (choice == ADD_CAR) {
                addCar(simulation);
                printCars(simulation);
            } else if (choice == RUN_SIMULATION) {
                if (!simulation.hasCars()) {
                    printError("There are no cars on the field yet. Please add at least one car first.");
                    continue;
                }
                printCars(simulation);
                printResult(engine.run(simulation));
                return;
            }
        }
    }

    private void addCar(Simulation simulation) {
        String name = ask("Please enter the name of the car:", input -> {
            String parsed = InputParser.parseName(input);
            simulation.validateName(parsed);
            return parsed;
        });
        InputParser.Placement placement = ask(
                "Please enter initial position of car " + name + " in x y Direction format:", input -> {
                    InputParser.Placement parsed = InputParser.parsePlacement(input);
                    simulation.validateStartPosition(parsed.position());
                    return parsed;
                });
        List<Command> commands = ask("Please enter the commands for car " + name + ":",
                InputParser::parseCommands);

        Position start = placement.position();
        simulation.addCar(new CarSpec(name, start, placement.direction(), commands));
    }

    private boolean askStartOver() {
        return askMenu("Start over", "Exit") == START_OVER;
    }

    private void printCars(Simulation simulation) {
        out.println("Your current list of cars are:");
        simulation.cars().forEach(car -> out.println(OutputFormatter.formatCar(car)));
        out.println();
    }

    private void printResult(SimulationResult result) {
        out.println("After simulation, the result is:");
        result.outcomes().forEach(outcome -> out.println(OutputFormatter.formatOutcome(outcome)));
        out.println();
    }

    private int askMenu(String... options) {
        StringBuilder prompt = new StringBuilder("Please choose from the following options:");
        for (int i = 0; i < options.length; i++) {
            prompt.append(System.lineSeparator()).append('[').append(i + 1).append("] ").append(options[i]);
        }
        prompt.append(System.lineSeparator());
        return ask(prompt.toString(), input -> InputParser.parseMenuChoice(input, options.length));
    }

    /**
     * Prints the prompt and reads a line, repeating until {@code parser} accepts
     * the input. Parsers signal invalid input with {@link IllegalArgumentException},
     * whose message is shown to the user.
     */
    private <T> T ask(String prompt, Function<String, T> parser) {
        while (true) {
            out.println(prompt);
            String line = readLine();
            out.println();
            try {
                return parser.apply(line);
            } catch (IllegalArgumentException e) {
                printError(e.getMessage());
            }
        }
    }

    private void printError(String message) {
        out.println(ERROR_PREFIX + message);
        out.println();
    }

    private String readLine() {
        try {
            String line = in.readLine();
            if (line == null) {
                throw new EndOfInputException();
            }
            return line;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read input", e);
        }
    }

    /** Signals that the input stream has been closed. */
    private static final class EndOfInputException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        EndOfInputException() {
            super(null, null, false, false);
        }
    }
}
