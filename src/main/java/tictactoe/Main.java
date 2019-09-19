package tictactoe;

import tictactoe.player.Player;
import tictactoe.player.PlayerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static tictactoe.Main.State.*;

public class Main {
    public static void main(String[] args) {
        List<String> supportedFunctionalCommands = new ArrayList<>();
        supportedFunctionalCommands.add("start");
        supportedFunctionalCommands.add("exit");
        List<String> supportedLevelCommands = new ArrayList<>();
        supportedLevelCommands.add("user");
        supportedLevelCommands.add("easy");
        supportedLevelCommands.add("medium");
        supportedLevelCommands.add("hard");

        CommandValidator commandValidator = new CommandValidator(supportedLevelCommands, supportedFunctionalCommands);


        StartMenu startMenu = new StartMenu(new Scanner(System.in), commandValidator);
        startMenu.start();

    }

    static class StartMenu {
        private AtomicBoolean isRunning;
        private final Scanner scanner;
        private final CommandValidator commandValidator;
        private String lastState;

        public StartMenu(Scanner scanner, CommandValidator commandValidator) {
            this.scanner = scanner;
            this.commandValidator = commandValidator;
            isRunning = new AtomicBoolean(true);
            lastState = "initial";
        }

        public void start() {
            while (isRunning.get()) {
                System.out.println("Input command: ");
                String command = scanner.nextLine();

                validateAndRun(command);
            }
        }

        public String validateAndRun(String command) {
            Either<String, String[]> validation = commandValidator.validate(command);

            if (validation.isLeft()) {
                System.out.println(validation.getLeft());
                lastState = validation.getLeft();
            } else if ("exit".equals(validation.getRight()[0])) {
                isRunning.set(false);
                lastState = "exiting";
            } else if ("start".equals(validation.getRight()[0])) {
                String[] commands = validation.getRight();
                Player player1 = PlayerFactory.create(commands[1], "X");
                Player player2 = PlayerFactory.create(commands[2], "O");

                GameLoop gameLoop = new GameLoop(new Player[]{player1, player2});
                State run = gameLoop.run();
                System.out.println(run);
                lastState = "game ended";
            }
            return lastState;
        }

        public boolean isRunning(){
            return isRunning.get();
        }

        public String getLastState() {
            return lastState;
        }
    }

    static class CommandValidator {
        private final List<String> supportedLevelCommands;
        private final List<String> supportedFunctionalCommands;

        CommandValidator(List<String> supportedLevelCommands, List<String> supportedFunctionalCommands) {
            this.supportedLevelCommands = supportedLevelCommands;
            this.supportedFunctionalCommands = supportedFunctionalCommands;
        }


        public Either<String, String[]> validate(String command) {
            String[] commands = Arrays.stream(command.split(" "))
                    .filter(this::isSupport)
                    .toArray(String[]::new);

            if (commands.length == 1 && "exit".equals(commands[0])){
                return Either.right(commands);
            }

            if (commands.length != 3) {
                return Either.left("Bad parameters!");
            }

            if (supportedFunctionalCommands.contains(commands[0]) &&
                    supportedLevelCommands.contains(commands[1]) &&
                    supportedLevelCommands.contains(commands[2])) {
                return Either.right(commands);
            }

            return Either.left("Bad parameters!");
        }

        private boolean isSupport(String command) {
            return !Objects.nonNull(command) ||
                    supportedLevelCommands.contains(command) ||
                    supportedFunctionalCommands.contains(command);
        }
    }

    static class GameLoop {
        private final Player[] players;
        private int moveCount;
        private Field field;

        GameLoop(Player[] players) {
            this.players = players;
            moveCount = 0;
            field = Field.fromCells("         ");
        }

        State run () {
            Player currentPlayer;
            do {
                if (moveCount % 2 == 0) {
                    currentPlayer = players[0];
                } else {
                    currentPlayer = players[1];
                }

                System.out.println(currentPlayer.moveMessage());
                Either<String, Field> nextField = currentPlayer.nextMove(field);
                if (nextField.isRight()) {
                    moveCount++;
                    field = nextField.getRight();
                    System.out.println(field.getPrintableField());
                } else {
                    System.out.println(nextField.getLeft());
                }
            } while (! field.getState().isTerminal());

            return field.getState();
        }

        int getMoveCount() {
            return moveCount;
        }

        Field getField() {
            return field;
        }
    }

    enum State {
        GAME_NOT_FINISHED("Game not finished"),
        DRAW("Draw"),
        X_WINS("X wins"),
        O_WINS("O wins"),
        IMPOSSIBLE("Impossible"),
        UNKNOWN("Unknown");

        private final String name;
        private static final List<State> terminalStates = new ArrayList<>();

        static {
            terminalStates.add(State.DRAW);
            terminalStates.add(State.O_WINS);
            terminalStates.add(State.X_WINS);
        }

        State(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean isTerminal() {
            return terminalStates.contains(this);
        }
    }

    public static class Field {
        private final String[][] array;
        private final String cells;
        private final String winner;
        private final State state;
        private final boolean isTwoWinners;
        private final Map<Coordinates, Integer> coordinateMapping;
        private final List<Coordinates> possibleMoves;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Field field = (Field) o;

            if (!Arrays.deepEquals(array, field.array)) return false;
            return state == field.state;
        }

        @Override
        public int hashCode() {
            int result = Arrays.deepHashCode(array);
            result = 31 * result + (state != null ? state.hashCode() : 0);
            return result;
        }

        private Field(String[][] array, String cells) {
            this.array = array;
            this.cells = cells;
            this.coordinateMapping = createCoordinateMapping();
            this.possibleMoves = createPossibleMoves();
            this.winner = calculateWinner();
            this.isTwoWinners = this.winner.equals("I");
            this.state = validate();
        }

        private List<Coordinates> createPossibleMoves() {
            return coordinateMapping.keySet()
                    .stream()
                    .filter(coordinates -> cells.charAt(coordinateMapping.get(coordinates)) == ' ')
                    .collect(Collectors.toList());
        }

        static Field fromCells(String cells) {
            String[][] array = new String[3][3];
            for (int i = 0, counter = 0; i < array.length; i++) {
                for (int j = 0; j < array.length; j++, counter++) {
                    array[i][j] = String.valueOf(cells.charAt(counter));
                }
            }

            return new Field(array, cells);
        }

        State getState() {
            return state;
        }

        public String getWinner() {
            return winner;
        }

        String getStateName() {
            return this.state.getName();
        }

        String getPrintableField() {
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < 3; i++) {
                String[] split = array[i];
                String row = String.format("%s %s %s", split[0], split[1], split[2]);
                result.append(String.format("| %s |\n", row));
            }
            String horizontal = "---------";
            return String.format("%s\n%s\n%s", horizontal, result.toString().trim(), horizontal);
        }

        State validate() {
            if (isImpossibleState()) {
                return IMPOSSIBLE;
            } else if (isSymbolXWin()) {
                return X_WINS;
            } else if (isSymbolOWin()) {
                return O_WINS;
            } else if (isDraw()) {
                return DRAW;
            } else if (!noMoreMoves()) {
                return GAME_NOT_FINISHED;
            }
            return UNKNOWN;
        }

        public Either<String, Field> nextMove(String coordinates, String nextSymbol) {
            char[] chars = cells.toCharArray();
            Either<String, Coordinates> coordinatesEither = Coordinates.fromString(coordinates);
            if (coordinatesEither.isLeft()) {
                return Either.left(coordinatesEither.getLeft());
            }

            Integer orDefault = coordinateMapping.getOrDefault(coordinatesEither.getRight(), -1);

            if (orDefault < 0) {
                return Either.left("Coordinates should be from 1 to 3!");
            } else if (!possibleMoves.contains(coordinatesEither.getRight())) {
                return Either.left("This cell is occupied! Choose another one!");
            } else {
                chars[orDefault] = nextSymbol.charAt(0);
            }

            return Either.right(Field.fromCells(String.valueOf(chars)));
        }

        private Map<Coordinates, Integer> createCoordinateMapping(){
            Map<Coordinates, Integer> map = new HashMap<>(9);
            map.put(Coordinates.fromString("1 1").getRight(), 6);
            map.put(Coordinates.fromString("1 2").getRight(), 3);
            map.put(Coordinates.fromString("1 3").getRight(), 0);
            map.put(Coordinates.fromString("2 1").getRight(), 7);
            map.put(Coordinates.fromString("2 2").getRight(), 4);
            map.put(Coordinates.fromString("2 3").getRight(), 1);
            map.put(Coordinates.fromString("3 1").getRight(), 8);
            map.put(Coordinates.fromString("3 2").getRight(), 5);
            map.put(Coordinates.fromString("3 3").getRight(), 2);
            return map;
        }

        private String calculateWinner() {
            boolean isXWin = isSymbolWin("X");
            boolean isOWin = isSymbolWin("O");

            if (isXWin && isOWin) {
                return "I";
            } else if (isXWin) {
                return "X";
            } else if (isOWin) {
                return "O";
            }

            return "";
        }

        private int countSymbol(String symbol) {
            int counter = 0;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (symbol.charAt(0) == array[i][j].charAt(0)) {
                        counter++;
                    }
                }
            }
            return counter;
        }

        private boolean isSymbolXWin() {
            return isTwoWinners || this.winner.equals("X");
        }

        private boolean isSymbolOWin() {
            return isTwoWinners || this.winner.equals("O");
        }

        private boolean isSymbolWin(String symbol) {
            // checking horizontally
            for (int i = 0; i < 3; i++) {
                String[] strings = array[i];
                if (Objects.equals(strings[0], strings[1]) &&
                        Objects.equals(strings[0], strings[2]) &&
                        Objects.equals(strings[0], symbol)) {
                    return true;
                }
            }

            // checking vertically
            for (int i = 0; i < 3; i++) {
                String[] strings = new String[]{array[0][i], array[1][i], array[2][i]};
                if (Objects.equals(strings[0], strings[1]) &&
                        Objects.equals(strings[0], strings[2]) &&
                        Objects.equals(strings[0], symbol)) {
                    return true;
                }
            }

            // checking diagonally
            if (Objects.equals(array[0][0], array[1][1]) &&
                    Objects.equals(array[0][0], array[2][2]) &&
                    Objects.equals(array[0][0], symbol)) {
                return true;
            }

            if (Objects.equals(array[2][0], array[1][1]) &&
                    Objects.equals(array[2][0], array[0][2]) &&
                    Objects.equals(array[2][0], symbol)) {
                return true;
            }

            return false;
        }

        private boolean isDraw() {
            return !isSymbolXWin() && !isSymbolOWin() && noMoreMoves();
        }

        private boolean noMoreMoves() {
            return possibleMoves.size() == 0;
        }

        private Boolean isImpossibleState() {
            int quantityOfX = countSymbol("X");
            int quantityOfY = countSymbol("O");

            boolean isTooMuchSymbol = Math.abs(quantityOfX - quantityOfY) > 1;
            boolean isThereTwoWinners = (isSymbolXWin() && isSymbolOWin());

            return isTooMuchSymbol || isThereTwoWinners;
        }

        public List<Coordinates> getPossibleMoves() {
            return possibleMoves;
        }
    }

    public static class Coordinates {

        private final String coordinates;
        private final Integer x;
        private final Integer y;
        private Coordinates(String coordinates, int x, int y) {
            this.coordinates = coordinates;
            this.x = x;
            this.y = y;
        }

        public String getCoordinates() {
            return coordinates;
        }

        static Either<String, Coordinates> fromString(String coordinates) {
            String[] split = coordinates.split(" ");
            int x, y;
            try {
                x = Integer.parseInt(split[0]);
                y = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                return Either.left("You should enter numbers!");
            }
            return Either.right(new Coordinates(coordinates, x, y));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Coordinates that = (Coordinates) o;

            if (coordinates != null ? !coordinates.equals(that.coordinates) : that.coordinates != null) return false;
            if (x != null ? !x.equals(that.x) : that.x != null) return false;
            return y != null ? y.equals(that.y) : that.y == null;
        }

        @Override
        public int hashCode() {
            int result = coordinates != null ? coordinates.hashCode() : 0;
            result = 31 * result + (x != null ? x.hashCode() : 0);
            result = 31 * result + (y != null ? y.hashCode() : 0);
            return result;
        }
    }

    // todo remove this impl and add vavr
    public static class Either<L, R> {
        private final L left;
        private final R right;
        private final boolean isRight;

        private Either(L left, R right, boolean isRight) {
            this.left = left;
            this.right = right;
            this.isRight = isRight;
        }

        static <L,R> Either<L,R> right(R right){
            return new Either<>(null, right, true);
        }

        public static <L,R> Either<L,R> left(L left){
            return new Either<>(left, null, false);
        }

        boolean isLeft() {
            return !isRight;
        }

        public boolean isRight() {
            return isRight;
        }

        L getLeft() {
            return left;
        }

        public R getRight() {
            return right;
        }
    }
}
