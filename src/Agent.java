///Fabbernat DROP TABLE HALLGATOK,Fabian.Bernat@stud.u-szeged.hu

import game.snake.Direction;
import game.snake.SnakeGame;
import game.snake.SnakePlayer;
import game.snake.utils.Cell;
import game.snake.utils.SnakeGameState;

import java.util.LinkedList;
import java.util.Random;


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
        Cell food = getFoodCell();
        if (food == null) return gameState.direction;

        Cell head = gameState.snake.peekFirst();
        LinkedList<Direction> validDirections = getValidDirections(head);

        Direction bestDirection = null;
        int bestScore = Integer.MIN_VALUE;

        for (Direction direction : validDirections) {
            // Simulate the move
            LinkedList<Cell> simulatedSnake = simulateMove(direction, gameState.snake);

            // Check if food is reachable after the move
            boolean foodReachable = isFoodReachable(simulatedSnake, food);

            // Calculate accessibility and hole size
            int accessibilityScore = calculateAccessibility(simulatedSnake);
            int holeSize = calculateHoleSize(simulatedSnake);

            // Add weight for food proximity
            Cell newHead = new Cell(head.i + direction.i, head.j + direction.j);
            int distanceToFood = newHead.distance(food);
            int proximityWeight = foodReachable ? (100 - distanceToFood) : 0;

            // Penalize moves into small holes
            int holePenalty = holeSize < gameState.snake.size() ? 500 + (gameState.snake.size() - holeSize) * 10 : 0;

            // Score the move
            int moveScore = accessibilityScore + proximityWeight - holePenalty;

            // Select the direction with the best score
            if (moveScore > bestScore) {
                bestScore = moveScore;
                bestDirection = direction;
            }
        }

        // Fallback to the safest option if no better direction is found
        return bestDirection != null ? bestDirection : validDirections.stream()
                .max((d1, d2) -> Integer.compare(
                        calculateAccessibility(simulateMove(d1, gameState.snake)),
                        calculateAccessibility(simulateMove(d2, gameState.snake))
                )).orElse(gameState.direction);
    }

    /**
     * Calculates the size of the region the snake's head moves into.
     *
     * @param snake The simulated snake state after the move.
     * @return The size of the region (hole size) the snake occupies.
     */
    private int calculateHoleSize(LinkedList<Cell> snake) {
        Cell head = snake.peekFirst();
        boolean[][] visited = new boolean[gameState.board.length][gameState.board[0].length];
        LinkedList<Cell> queue = new LinkedList<>();
        queue.add(head);
        visited[head.i][head.j] = true;

        int regionSize = 0;

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            regionSize++;

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

        return regionSize; // Size of the reachable region from the snake's head
    }


    private boolean isFoodReachable(LinkedList<Cell> snake, Cell food) {
        Cell head = snake.peekFirst();
        boolean[][] visited = new boolean[gameState.board.length][gameState.board[0].length];
        LinkedList<Cell> queue = new LinkedList<>();
        queue.add(head);
        visited[head.i][head.j] = true;

        while (!queue.isEmpty()) {
            Cell current = queue.poll();

            // If food is reachable, return true
            if (current.equals(food)) {
                return true;
            }

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

        return false; // Food is not reachable
    }

    private LinkedList<Cell> simulateMove(Direction direction, LinkedList<Cell> snake) {
        LinkedList<Cell> simulatedSnake = new LinkedList<>(snake);
        Cell newHead = new Cell(snake.peekFirst().i + direction.i, snake.peekFirst().j + direction.j);
        simulatedSnake.addFirst(newHead);

        if (!newHead.equals(getFoodCell())) {
            simulatedSnake.removeLast(); // Tail moves forward if no food is consumed
        }
        return simulatedSnake;
    }

    private int calculateAccessibility(LinkedList<Cell> snake) {
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

        return accessibleCells; // Number of accessible cells from the current state
    }

    private LinkedList<Direction> getValidDirections(Cell head) {
        LinkedList<Direction> directions = new LinkedList<>();
        for (Cell neighbor : head.neighbors()) {
            if (gameState.isOnBoard(neighbor) && gameState.getValueAt(neighbor) != SnakeGame.SNAKE) {
                directions.add(head.directionTo(neighbor));
            }
        }
        return directions;
    }

    boolean wouldDie(Direction d, LinkedList<Cell> snake){
        Cell tail = gameState.getTail();
        Cell head = snake.peekFirst();
        assert (head != null);

        Cell newHead = new Cell(head.i + d.i, head.j + d.j);

        // ha kimenne a palyarol, vagy nekimenne maganak, akkor wouldDie
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
        int requiredCells = snake.size();

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            accessibleCells++;

            // Check if the snake can access enough open cells to survive
            if (accessibleCells >= requiredCells) {
                return true; // Early exit: enough cells are accessible
            }

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

        return false;
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