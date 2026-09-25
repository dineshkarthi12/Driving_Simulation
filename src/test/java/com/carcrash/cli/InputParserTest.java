package com.carcrash.cli;

import com.carcrash.domain.Command;
import com.carcrash.domain.Direction;
import com.carcrash.domain.Field;
import com.carcrash.domain.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputParserTest {

    @Test
    void parsesFieldSize() {
        assertThat(InputParser.parseField("10 10")).isEqualTo(new Field(10, 10));
    }

    @Test
    void parsesFieldSizeWithExtraWhitespace() {
        assertThat(InputParser.parseField("  5 \t 3  ")).isEqualTo(new Field(5, 3));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "10", "10 10 10", "a b", "10 x", "0 10", "10 0", "-1 5", "1.5 2",
            "99999999999 1"})
    void rejectsInvalidFieldSize(String input) {
        assertThatThrownBy(() -> InputParser.parseField(input)).isInstanceOf(InvalidInputException.class);
    }

    @Test
    void parsesPlacementIgnoringCaseAndWhitespace() {
        InputParser.Placement placement = InputParser.parsePlacement("  1   2 n ");
        assertThat(placement.position()).isEqualTo(new Position(1, 2));
        assertThat(placement.direction()).isEqualTo(Direction.N);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1 2", "1 2 N X", "a 2 N", "1 b N", "1 2 Q", "1 2 12"})
    void rejectsInvalidPlacement(String input) {
        assertThatThrownBy(() -> InputParser.parsePlacement(input)).isInstanceOf(InvalidInputException.class);
    }

    @Test
    void parsesCommandsIgnoringCaseAndWhitespace() {
        assertThat(InputParser.parseCommands(" ff r l ")).containsExactly(
                Command.F, Command.F, Command.R, Command.L);
    }

    @Test
    void emptyCommandsAreAllowed() {
        assertThat(InputParser.parseCommands("")).isEmpty();
    }

    @Test
    void rejectsInvalidCommands() {
        assertThatThrownBy(() -> InputParser.parseCommands("FFB"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("'B'");
    }

    @Test
    void trimsCarName() {
        assertThat(InputParser.parseName("  Herbie  ")).isEqualTo("Herbie");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlankCarName(String input) {
        assertThatThrownBy(() -> InputParser.parseName(input)).isInstanceOf(InvalidInputException.class);
    }

    @Test
    void parsesMenuChoice() {
        assertThat(InputParser.parseMenuChoice(" 2 ", 2)).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "0", "3", "-1", "one", "1 2"})
    void rejectsInvalidMenuChoice(String input) {
        assertThatThrownBy(() -> InputParser.parseMenuChoice(input, 2))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("from 1 to 2");
    }
}
