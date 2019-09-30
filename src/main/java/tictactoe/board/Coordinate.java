package tictactoe.board;

import tictactoe.util.Either;

public class Coordinate {

    private final String coordinates;
    private final Integer x;
    private final Integer y;

    private Coordinate(String coordinates, int x, int y) {
        this.coordinates = coordinates;
        this.x = x;
        this.y = y;
    }

    public static Either<String, Coordinate> fromString(String coordinates) {
        String[] split = coordinates.split(" ");
        int x, y;
        try {
            x = Integer.parseInt(split[0]);
            y = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            return Either.left("You should enter numbers!");
        }
        return Either.right(new Coordinate(coordinates, x, y));
    }

    public String getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return String.format("(%s)", coordinates);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate) o;

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
