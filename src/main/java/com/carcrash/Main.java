package com.carcrash;

import com.carcrash.cli.ConsoleApp;
import com.carcrash.simulation.SimulationEngine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;

/** Entry point: wires the console application to standard input and output. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Charset charset = Charset.defaultCharset();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, charset));
        PrintStream out = new PrintStream(System.out, true, charset);
        try {
            new ConsoleApp(in, out, new SimulationEngine()).run();
        } catch (RuntimeException e) {
            System.err.println("Unexpected error: " + e.getMessage());
            System.exit(1);
        }
    }
}
