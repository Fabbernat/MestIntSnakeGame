///raharap_a_farkara,Fabian.Bernat@stud.u-szeged.hu

import game.snake.Direction;
import game.snake.SnakeGame;
import game.snake.SnakePlayer;
import game.snake.utils.Cell;
import game.snake.utils.SnakeGameState;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;

enum Directions{
    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);

    Directions(int row, int column) {

    }
}

/**
 * Az Agent osztaly a kigyot vezerlo agens, amely a jatek allapota alapjan
 * donti el a kovetkezo lepes iranyat. Az agens celja az etel felvetele es a falak,
 * valamint a sajat teste elkerulese.
 */
public class Agent extends SnakePlayer {

    /**
     * Konstruktor, amely inicializalja az Agent objektumot a megadott jatekkal.
     *
     * @param gameState Az aktualis jatekter allapota.
     * @param color     A kigyo szine.
     * @param random    random szam generator
     */
    public Agent(SnakeGameState gameState, int color, Random random) {
        super(gameState, color, random);
    }

    /**
     * Meghatarozza az aktualis jatekallapot alapjan a kovetkezo lepes iranyat.
     *
     * @param remainingTime A hatralevo ido a dontes meghozatalara.
     * @return Az uj irany, amely fele a kigyo mozdul.
     */
    @Override
    public Direction getAction(long remainingTime) {
        // find food on the table
        Cell food = getFoodCell();
        if (food == null) return gameState.direction;

        // find the closest cell to the food of the head's neighbors
        Cell closest = food;
        Cell head = gameState.snake.peekFirst();
        int distance = gameState.maxDistance();

        for (Cell c : head.neighbors()) {
            if (gameState.isOnBoard(c) && gameState.getValueAt(c) != SnakeGame.SNAKE && c.distance(food) < distance) {
                distance = c.distance(food);
                closest = c;
            }
        }
       // return head.directionTo(closest);


        Direction desiredDirection = head.directionTo(closest);
        while (wouldMakeATrapCircle(desiredDirection, gameState.snake)) {
            int random = this.random.nextInt() * 2;
            if (desiredDirection.i == head.i - 1){
                desiredDirection = (random >= 1 ? SnakeGame.LEFT : SnakeGame.RIGHT);
            } else if (desiredDirection.i == head.i + 1) {
                desiredDirection = (random < 1 ? SnakeGame.LEFT : SnakeGame.RIGHT);
            } else if (desiredDirection.j == head.j - 1) {
                desiredDirection = (random >= 1 ? SnakeGame.UP : SnakeGame.DOWN);
            } else if (desiredDirection.j == head.j + 1) {
                desiredDirection = (random < 1 ? SnakeGame.UP : SnakeGame.DOWN);
            }
        }
        return desiredDirection;
    }

    boolean wouldMakeATrapCircle(Direction d, LinkedList<Cell> snake){
        Cell tail = gameState.getTail();
        Cell head = snake.peekFirst();
        assert (head != null);

        Cell newHead = new Cell(head.i + d.i, head.j + d.j);

        if (!gameState.isOnBoard(newHead) || snake.contains(newHead)) {
            return true;
        }

        LinkedList<Cell> simulatedSnake = new LinkedList<>(snake);
        simulatedSnake.addFirst(newHead);


        if (!newHead.equals(getFoodCell())) {
            simulatedSnake.removeLast();
        }

        return !hasEscapeRoute(simulatedSnake);
    }

    private boolean hasEscapeRoute(LinkedList<Cell> snake) {
        Cell head = snake.peekFirst();
        boolean[][] visited = new boolean[gameState.board.length][gameState.board[0].length];
        LinkedList<Cell> queue = new LinkedList<>();
        queue.add(head);
        visited[head.i][head.j] = true;

        int accessibleCells = 0;
        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            accessibleCells++;

            for (Cell neighbor : current.neighbors()) {
                if (gameState.isOnBoard(neighbor) &&
                        !visited[neighbor.i][neighbor.j] &&
                        (gameState.getValueAt(neighbor) != SnakeGame.SNAKE || neighbor.equals(snake.peekLast())) &&
                        !snake.contains(neighbor)) {
                    visited[neighbor.i][neighbor.j] = true;
                    queue.add(neighbor);
                }
            }
        }

        // Check if the snake can access enough open cells to survive
        return accessibleCells >= snake.size();
    }


    private Cell getFoodCell() {
        Cell food = null;
        outer: for (int row = 0; row < gameState.board.length; row++) {
            for (int column = 0; column < gameState.board[row].length; column++) {
                if (gameState.board[row][column] == SnakeGame.FOOD) {
                    food = new Cell(row, column);
                    break outer; // A dupla for ciklusnak nem kell vegigiteralnia a tombon, ha mar megtalalta az etelt. Ez idopocsekolas lenne.
                }
            }
        }
        return food;
    }
}