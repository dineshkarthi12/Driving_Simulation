package com.carcrash.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.carcrash.domain.Command.F;
import static com.carcrash.domain.Command.L;
import static com.carcrash.domain.Command.R;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandTest {

    @Test
    void parsesSingleCharactersIgnoringCase() {
        assertThat(Command.fromChar('L')).isEqualTo(L);
        assertThat(Command.fromChar('r')).isEqualTo(R);
        assertThat(Command.fromChar('f')).isEqualTo(F);
    }

    @Test
    void parsesCommandString() {
        assertThat(Command.parseAll("FFRL")).containsExactly(F, F, R, L);
    }

    @Test
    void parsesMixedCaseAndSkipsWhitespace() {
        assertThat(Command.parseAll("  ff R\tl ")).containsExactly(F, F, R, L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t"})
    void blankStringMeansNoCommands(String input) {
        assertThat(Command.parseAll(input)).isEmpty();
    }

    @Test
    void rejectsInvalidCharacterAndNamesIt() {
        assertThatThrownBy(() -> Command.parseAll("FFXR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'X'");
    }

    @Test
    void rendersCommandsBackToText() {
        assertThat(Command.toText(List.of(F, F, R, L))).isEqualTo("FFRL");
        assertThat(Command.toText(List.of())).isEmpty();
    }
}
