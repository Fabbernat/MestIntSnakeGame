///Fabbernat,Fabian.Bernat@stud.u-szeged.hu
import java.util.Random;

import game.snake.Direction;
import game.snake.SnakeGame;
import game.snake.SnakePlayer;
import game.snake.utils.Cell;
import game.snake.utils.SnakeGameState;

/**
 * Az Agent osztaly a kigyot vezerlo agens, amely a jatek allapota alapjan 
 * donti el a kovetkezo lepes iranyat. Az agens celja az etel felvetele es a falak,
 * valamint a sajat teste elkerulese.
 */
public class Agent extends SnakePlayer {

    /**
     * Konstruktor, amely inicializalja az Agent objektumot a megadott jatekkal.
     * @param gameState Az aktualis jatekter allapota.
     * @param color A kigyo szine.
     * @param random random szam generator
     */
    public Agent(SnakeGameState gameState, int color, Random random) {
        super(gameState, color, random);
    }

    /**
     * Meghatarozza az aktualis jatekallapot alapjan a kovetkezo lepes iranyat.
     * @param remainingTime A hatralevo ido a dontes meghozatalara.
     * @return Az uj irany, amely fele a kigyo mozdul.
     */
    @Override
    public Direction getAction(long remainingTime) {
        Cell food = null;
        for (int row = 0; row < gameState.board.length; row++) {
            for (int column = 0; column < gameState.board[row].length; column++) {
                if (gameState.board[row][column] == SnakeGame.FOOD) {
                    food = new Cell(row, column);
                }
            }
        }
        // find the closest cell to the food of the head's neighbors
        Cell closest = food;
        Cell head = gameState.snake.peekFirst();
        int distance = gameState.maxDistance();
        // Check if gameState or snake is null
        if (head == null) {
            // visszater alapertelmezett modon bal irannyal, ha headPostiton null
            return new Direction(0, -1); // Bal irany
        }
        for (Cell c : head.neighbors()) {
            if (gameState.isOnBoard(c) && gameState.getValueAt(c) != SnakeGame.SNAKE && c.distance(food) < distance) {
                distance = c.distance(food);
                closest = c;
            }
        }

        
        // Snake fej pozicioja
        var headPosition = gameState.snake.peekFirst();
        if (headPosition == null) {
            // visszater alapertelmezett modon bal irannyal, ha headPostiton null
            return new Direction(0, -1);
        }
        int headRow = headPosition.i;
        int headColumn = headPosition.j;

        // etel legkozelebbi pozicioja
        int targetRow = -1;
        int targetColumn = -1;
        for (int column = 0; column < gameState.board.length; column++) {
            for (int row = 0; row < gameState.board.length; row++) {
                if (gameState.board[column][row] == SnakeGame.FOOD) {
                    targetRow = row;
                    targetColumn = column;
                    break;
                }
            }
        }

        // Ha megtalaljuk az etelt, a megfelelo iranyba elmozdulunk fele
        if (targetRow != -1 && targetColumn != -1) {
            if (!isObstacleBetween(headRow, headColumn, targetRow, targetColumn)) {
            if (targetRow < headRow && isSafeMove(new Direction(0, -1))) return new Direction(0, -1); // Bal
            else if (targetRow > headRow && isSafeMove(new Direction(0, 1))) return new Direction(0, 1); // Jobb
            else if (targetColumn < headColumn && isSafeMove(new Direction(-1, 0))) return new Direction(-1, 0); // Fel
            else if (targetColumn > headColumn && isSafeMove(new Direction(1, 0))) return new Direction(1, 0); // Le
        } else {
            // Ha az etel es a fej kozott a test van, keresni kell egy biztonsagos utat
            return findSafePathToFood(headRow, headColumn, targetRow, targetColumn);
        }
    }

        // Alapertelmezett a legkozelebbi fele, ha nincs etel vagy nincs biztonsagos ut
        return head.directionTo(closest);

    }

    /**
     * Ellenorzi, hogy az adott irany biztonsagos-e.
     * @param direction A vizsgalando irany.
     * @return Igaz, ha az irany biztonsagos, hamis kulonben.
     */
    private boolean isSafeMove(Direction direction) {
        assert gameState.snake.peekFirst() != null;
        int nextX = gameState.snake.peekFirst().i + direction.i;
        int nextY = gameState.snake.peekFirst().j + direction.j;

        // Ellenorzi, hogy a lepes hatarokon belul marad-e es elkeruli-e a kigyo testet
        return (nextX >= 0 && nextX < gameState.board.length &&
                nextY >= 0 && nextY < gameState.board.length &&
                gameState.board[nextY][nextX] != SnakeGame.SNAKE);
    }

    /**
     * Ellenorzi, hogy van-e akadaly (a kigyo teste) az etel es a fej kozott.
     */
    private boolean isObstacleBetween(int headRow, int headColumn, int targetRow, int targetColumn) {
        // Egy egyszeru ellenorzes arra, hogy van-e akadaly a ket pont kozott
        // Peldaul, ha a target es a head kozott van kigyo test, akkor akadaly van
        if (targetRow == headRow) {
            // Ha ugyanazon az oszlopon van, akkor vertikalisan keresunk
            int minY = Math.min(headColumn, targetColumn);
            int maxY = Math.max(headColumn, targetColumn);
            for (int column = minY + 1; column < maxY; column++) {
                if (gameState.board[column][headRow] == SnakeGame.SNAKE) {
                    return true;
                }
            }
        } else if (targetColumn == headColumn) {
            // Ha ugyanazon a sorban van, akkor horizontalisan keresunk
            int minX = Math.min(headRow, targetRow);
            int maxX = Math.max(headRow, targetRow);
            for (int row = minX + 1; row < maxX; row++) {
                if (gameState.board[headColumn][row] == SnakeGame.SNAKE) {
                    return true;
                }
            }
        }
        return false;
    }

    private Direction findSafePathToFood(int headRow, int headColumn, int targetRow, int targetColumn) {
        // List of possible directions to move (Up, Down, Left, Right)
        Direction[] directions = { new Direction(0, -1), new Direction(0, 1), new Direction(-1, 0), new Direction(1, 0) };

        // First, attempt to find the safest and closest direction
        for (Direction dir : directions) {
            int newX = headRow + dir.i;
            int newY = headColumn + dir.j;

            // Check if the new position is within bounds and doesn't collide with the snake's body
            if (isSafeMove(dir)) {
                // If the new direction brings us closer to the target food, prioritize it
                if (isCloserToFood(newX, newY, targetRow, targetColumn, headRow, headColumn)) {
                    return dir;
                }
            }
        }

        // If no good path was found, default to the first safe direction found
        for (Direction dir : directions) {
            if (isSafeMove(dir)) {
                return dir; // Return the first safe direction found
            }
        }

        return new Direction(0, -1); // Fallback if no safe path is available
    }

    /**
     * Helper method to check if the new position brings us closer to the food.
     * @param newX The x-coordinate of the new position.
     * @param newY The y-coordinate of the new position.
     * @param targetRow The x-coordinate of the food.
     * @param targetColumn The y-coordinate of the food.
     * @param headRow The current x-coordinate of the snake's head.
     * @param headColumn The current y-coordinate of the snake's head.
     * @return True if the new position brings us closer to the food, false otherwise.
     */
    private boolean isCloserToFood(int newX, int newY, int targetRow, int targetColumn, int headRow, int headColumn) {
        // Calculate the Manhattan distance (absolute difference in x and y) from the food to the current head
        int currentDistance = Math.abs(headRow - targetRow) + Math.abs(headColumn - targetColumn);
        int newDistance = Math.abs(newX - targetRow) + Math.abs(newY - targetColumn);

        // Return true if the new position is closer to the food
        return newDistance < currentDistance;
    }
}


