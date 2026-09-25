package com.carcrash.cli;

/** Raised when user input cannot be parsed. The message is shown to the user as is. */
public class InvalidInputException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}
