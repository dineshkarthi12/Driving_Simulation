package com.carcrash.simulation;

import com.carcrash.domain.CarSpec;
import com.carcrash.domain.Direction;
import com.carcrash.domain.Field;
import com.carcrash.domain.Position;
import com.carcrash.domain.Simulation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.carcrash.domain.TestCars.car;
import static org.assertj.core.api.Assertions.assertThat;

class SimulationEngineTest {

    private final SimulationEngine engine = new SimulationEngine();

    private static Simulation simulation(int width, int height, CarSpec... cars) {
        Simulation simulation = new Simulation(new Field(width, height));
        for (CarSpec car : cars) {
            simulation.addCar(car);
        }
        return simulation;
    }

    private static CarOutcome finished(String name, int x, int y, String direction) {
        return new CarOutcome.Finished(name, new Position(x, y), Direction.fromSymbol(direction));
    }

    private static CarOutcome collided(String name, List<String> others, int x, int y, int step) {
        return new CarOutcome.Collided(name, others, new Position(x, y), step);
    }

    @Nested
    @DisplayName("Specification scenarios")
    class Scenarios {

        @Test
        void scenario1SingleCarEndsAt54FacingSouth() {
            SimulationResult result = engine.run(simulation(10, 10, car("A", 1, 2, "N", "FFRFFFFRRL")));

            assertThat(result.outcomes()).containsExactly(finished("A", 5, 4, "S"));
        }

        @Test
        void scenario2CarsCollideAt54AtStep7() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("A", 1, 2, "N", "FFRFFFFRRL"),
                    car("B", 7, 8, "W", "FFLFFFFFFF")));

            assertThat(result.outcomes()).containsExactly(
                    collided("A", List.of("B"), 5, 4, 7),
                    collided("B", List.of("A"), 5, 4, 7));
        }
    }

    @Nested
    @DisplayName("Single car movement")
    class Movement {

        @Test
        void moveBeyondBoundaryIsIgnoredAndLaterCommandsStillApply() {
            SimulationResult result = engine.run(simulation(10, 10, car("A", 0, 0, "S", "FLF")));

            assertThat(result.outcomes()).containsExactly(finished("A", 1, 0, "E"));
        }

        @Test
        void carStaysInTopRightCornerWhenDrivingIntoBoundary() {
            SimulationResult result = engine.run(simulation(10, 10, car("A", 9, 9, "N", "FFRFF")));

            assertThat(result.outcomes()).containsExactly(finished("A", 9, 9, "E"));
        }

        @Test
        void oneByOneFieldAllowsOnlyRotation() {
            SimulationResult result = engine.run(simulation(1, 1, car("A", 0, 0, "N", "FRFRFRF")));

            assertThat(result.outcomes()).containsExactly(finished("A", 0, 0, "W"));
        }

        @Test
        void carWithFewerCommandsStopsAndStaysOnField() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("A", 0, 0, "N", "F"),
                    car("B", 5, 5, "E", "FFFF")));

            assertThat(result.outcomes()).containsExactly(
                    finished("A", 0, 1, "N"),
                    finished("B", 9, 5, "E"));
        }

        @Test
        void outcomesFollowRegistrationOrder() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("Z", 0, 0, "N", "F"),
                    car("A", 5, 5, "N", "F")));

            assertThat(result.outcomes()).extracting(CarOutcome::name).containsExactly("Z", "A");
        }
    }

    @Nested
    @DisplayName("Parked cars (empty command list)")
    class ParkedCars {

        @Test
        void parkedCarStaysAtStartPosition() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("P", 4, 4, "E", ""),
                    car("A", 0, 0, "N", "FFF")));

            assertThat(result.outcomes()).containsExactly(
                    finished("P", 4, 4, "E"),
                    finished("A", 0, 3, "N"));
        }

        @Test
        void onlyParkedCarsProduceNoMovement() {
            SimulationResult result = engine.run(simulation(10, 10, car("P", 4, 4, "E", "")));

            assertThat(result.outcomes()).containsExactly(finished("P", 4, 4, "E"));
        }

        @Test
        void carCrashingIntoParkedCarCollidesWithIt() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("P", 5, 5, "N", ""),
                    car("A", 5, 2, "N", "FFFF")));

            assertThat(result.outcomes()).containsExactly(
                    collided("P", List.of("A"), 5, 5, 3),
                    collided("A", List.of("P"), 5, 5, 3));
        }
    }

    @Nested
    @DisplayName("Collision rules")
    class Collisions {

        @Test
        void collidedCarsIgnoreRemainingCommands() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("A", 0, 0, "E", "FFFFF"),
                    car("B", 2, 0, "W", "FFFFF")));

            assertThat(result.outcomes()).containsExactly(
                    collided("A", List.of("B"), 1, 0, 1),
                    collided("B", List.of("A"), 1, 0, 1));
        }

        @Test
        void headOnSwapIsACollisionAndBothCarsStayOnTheirPreMoveCells() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("A", 1, 1, "E", "LRFFF"),
                    car("B", 2, 1, "W", "RLFFF")));

            assertThat(result.outcomes()).containsExactly(
                    collided("A", List.of("B"), 1, 1, 3),
                    collided("B", List.of("A"), 2, 1, 3));
        }

        @Test
        void swapCancellationThatLeavesACarOnAnOccupiedCellAlsoCollidesThere() {
            // A and B swap-collide; A stays on (1,1), which C has just moved into.
            SimulationResult result = engine.run(simulation(10, 10,
                    car("A", 1, 1, "E", "F"),
                    car("B", 2, 1, "W", "F"),
                    car("C", 1, 2, "S", "F")));

            assertThat(result.outcomes()).containsExactly(
                    collided("A", List.of("B", "C"), 1, 1, 1),
                    collided("B", List.of("A"), 2, 1, 1),
                    collided("C", List.of("A"), 1, 1, 1));
        }

        @Test
        void threeCarsInOneCellEachListAllOthers() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("C", 1, 0, "N", "F"),
                    car("A", 0, 1, "E", "F"),
                    car("B", 2, 1, "W", "F")));

            assertThat(result.outcomes()).containsExactly(
                    collided("C", List.of("A", "B"), 1, 1, 1),
                    collided("A", List.of("B", "C"), 1, 1, 1),
                    collided("B", List.of("A", "C"), 1, 1, 1));
        }

        @Test
        void carEnteringCellOfCrashedCarsCollidesWithAllOfThem() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("A", 0, 0, "E", "F"),
                    car("B", 2, 0, "W", "F"),
                    car("C", 1, 3, "S", "FFF")));

            assertThat(result.outcomes()).containsExactly(
                    collided("A", List.of("B"), 1, 0, 1),
                    collided("B", List.of("A"), 1, 0, 1),
                    collided("C", List.of("A", "B"), 1, 0, 3));
        }

        @Test
        void carEnteringCellOfCarThatFinishedItsCommandsCollidesWithIt() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("A", 0, 0, "N", "F"),
                    car("B", 3, 1, "W", "FFF")));

            assertThat(result.outcomes()).containsExactly(
                    collided("A", List.of("B"), 0, 1, 3),
                    collided("B", List.of("A"), 0, 1, 3));
        }

        @Test
        void carRotatingInPlaceIsHitByMovingCar() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("A", 1, 1, "N", "LLL"),
                    car("B", 1, 0, "N", "F")));

            assertThat(result.outcomes()).containsExactly(
                    collided("A", List.of("B"), 1, 1, 1),
                    collided("B", List.of("A"), 1, 1, 1));
        }

        @Test
        void followingIntoCellVacatedInSameStepIsNotACollision() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("A", 0, 0, "E", "FF"),
                    car("B", 1, 0, "E", "FF")));

            assertThat(result.outcomes()).containsExactly(
                    finished("A", 2, 0, "E"),
                    finished("B", 3, 0, "E"));
        }

        @Test
        void carsBlockedByBoundaryDoNotSwap() {
            SimulationResult result = engine.run(simulation(2, 1,
                    car("A", 0, 0, "W", "F"),
                    car("B", 1, 0, "E", "F")));

            assertThat(result.outcomes()).containsExactly(
                    finished("A", 0, 0, "W"),
                    finished("B", 1, 0, "E"));
        }

        @Test
        void independentCollisionsInSameStepAreReportedSeparately() {
            SimulationResult result = engine.run(simulation(10, 10,
                    car("A", 0, 0, "E", "F"),
                    car("B", 2, 0, "W", "F"),
                    car("C", 5, 5, "E", "F"),
                    car("D", 7, 5, "W", "F")));

            assertThat(result.outcomes()).containsExactly(
                    collided("A", List.of("B"), 1, 0, 1),
                    collided("B", List.of("A"), 1, 0, 1),
                    collided("C", List.of("D"), 6, 5, 1),
                    collided("D", List.of("C"), 6, 5, 1));
        }
    }

    @Test
    void runningTwiceGivesTheSameResultAndDoesNotModifyTheSimulation() {
        Simulation simulation = simulation(10, 10,
                car("A", 1, 2, "N", "FFRFFFFRRL"),
                car("B", 7, 8, "W", "FFLFFFFFFF"));
        List<CarSpec> before = List.copyOf(simulation.cars());

        SimulationResult first = engine.run(simulation);
        SimulationResult second = engine.run(simulation);

        assertThat(second).isEqualTo(first);
        assertThat(simulation.cars()).isEqualTo(before);
    }
}
