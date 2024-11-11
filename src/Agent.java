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
        for (int i = 0; i < gameState.board.length; i++) {
            for (int j = 0; j < gameState.board[i].length; j++) {
                if (gameState.board[i][j] == SnakeGame.FOOD) {
                    food = new Cell(i, j);
                }
            }
        }
        // find closest cell to the food of the head's neighbors
        Cell closest = food;
        Cell head = gameState.snake.peekFirst();
        int distance = gameState.maxDistance();
        assert head != null;
        for (Cell c : head.neighbors()) {
            if (gameState.isOnBoard(c) && gameState.getValueAt(c) != SnakeGame.SNAKE && c.distance(food) < distance) {
                distance = c.distance(food);
                closest = c;
            }
        }

        /*// Check if gameState or snake is null
        if (gameState == null || gameState.snake == null || gameState.snake.peekFirst() == null) {
            // visszater alapertelmezett modon fel irannyal, ha headPostiton null
            return new Direction(-1, 0); // Fel irany
        }
        // Snake fej pozicioja
        var headPosition = gameState.snake.peekFirst();
        if (headPosition == null) {
            // visszater alapertelmezett modon fel irannyal, ha headPostiton null
            return new Direction(-1, 0);
        }
        int headX = headPosition.i;
        int headY = headPosition.j;

        // etel legkozelebbi pozicioja
        int targetX = -1;
        int targetY = -1;
        for (int y = 0; y < gameState.board.length; y++) {
            for (int x = 0; x < gameState.board.length; x++) {
                if (gameState.board[y][x] == SnakeGame.FOOD) {
                    targetX = x;
                    targetY = y;
                    break;
                }
            }
        }

        // Ha megtalaljuk az etelt, a megfelelo iranyban mozgunk fele
        if (targetX != -1 && targetY != -1) {
            if (!isObstacleBetween(headX, headY, targetX, targetY)) {
            if (targetX < headX && isSafeMove(new Direction(0, -1))) return new Direction(0, -1); // Bal
            else if (targetX > headX && isSafeMove(new Direction(0, 1))) return new Direction(0, 1); // Jobb
            else if (targetY < headY && isSafeMove(new Direction(-1, 0))) return new Direction(-1, 0); // Fel
            else if (targetY > headY && isSafeMove(new Direction(1, 0))) return new Direction(1, 0); // Le
        } else {
            // Ha az etel es a fej kozott a test van, keresni kell egy biztonsagos utat
            return findSafePathToFood(headX, headY, targetX, targetY);
        }
    }*/

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
    private boolean isObstacleBetween(int headX, int headY, int targetX, int targetY) {
        // Egy egyszeru ellenorzes arra, hogy van-e akadaly a ket pont kozott
        // Peldaul, ha a target es a head kozott van kigyo test, akkor akadaly van
        if (targetX == headX) {
            // Ha ugyanazon az oszlopon van, akkor vertikalisan keresunk
            int minY = Math.min(headY, targetY);
            int maxY = Math.max(headY, targetY);
            for (int y = minY + 1; y < maxY; y++) {
                if (gameState.board[y][headX] == SnakeGame.SNAKE) {
                    return true;
                }
            }
        } else if (targetY == headY) {
            // Ha ugyanazon a sorban van, akkor horizontalisan keresunk
            int minX = Math.min(headX, targetX);
            int maxX = Math.max(headX, targetX);
            for (int x = minX + 1; x < maxX; x++) {
                if (gameState.board[headY][x] == SnakeGame.SNAKE) {
                    return true;
                }
            }
        }
        return false;
    }

    private Direction findSafePathToFood(int headX, int headY, int targetX, int targetY) {
        // List of possible directions to move (Up, Down, Left, Right)
        Direction[] directions = { new Direction(0, -1), new Direction(0, 1), new Direction(-1, 0), new Direction(1, 0) };

        // First, attempt to find the safest and closest direction
        for (Direction dir : directions) {
            int newX = headX + dir.i;
            int newY = headY + dir.j;

            // Check if the new position is within bounds and doesn't collide with the snake's body
            if (isSafeMove(dir)) {
                // If the new direction brings us closer to the target food, prioritize it
                if (isCloserToFood(newX, newY, targetX, targetY, headX, headY)) {
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
     * @param targetX The x-coordinate of the food.
     * @param targetY The y-coordinate of the food.
     * @param headX The current x-coordinate of the snake's head.
     * @param headY The current y-coordinate of the snake's head.
     * @return True if the new position brings us closer to the food, false otherwise.
     */
    private boolean isCloserToFood(int newX, int newY, int targetX, int targetY, int headX, int headY) {
        // Calculate the Manhattan distance (absolute difference in x and y) from the food to the current head
        int currentDistance = Math.abs(headX - targetX) + Math.abs(headY - targetY);
        int newDistance = Math.abs(newX - targetX) + Math.abs(newY - targetY);

        // Return true if the new position is closer to the food
        return newDistance < currentDistance;
    }
}