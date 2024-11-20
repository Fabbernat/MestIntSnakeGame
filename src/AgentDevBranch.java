/*
///Fabbernat,Fabian.Bernat@stud.u-szeged.hu

import game.snake.Direction;
import game.snake.SnakeGame;
import game.snake.SnakePlayer;
import game.snake.utils.Cell;
import game.snake.utils.SnakeGameState;

import java.util.*;

*/
/**
 * Az Agent osztaly a kigyot vezerlo agens, amely a jatek allapota alapjan
 * donti el a kovetkezo lepes iranyat. Az agens celja az etel felvetele es a falak,
 * valamint a sajat teste elkerulese.
 *//*

public class AgentDevBranch extends SnakePlayer {

    */
/**
     * Konstruktor, amely inicializalja az Agent objektumot a megadott jatekkal.
     *
     * @param gameState Az aktualis jatekter allapota.
     * @param color     A kigyo szine.
     * @param random    random szam generator
     *//*

    public AgentDevBranch(SnakeGameState gameState, int color, Random random) {
        super(gameState, color, random);
    }

    */
/**
     * Meghatarozza az aktualis jatekallapot alapjan a kovetkezo lepes iranyat.
     *
     * @param remainingTime A hatralevo ido a dontes meghozatalara.
     * @return Az uj irany, amely fele a kigyo mozdul.
     *//*

    @Override
    public Direction getAction(long remainingTime) {
        Cell food = getFoodCell();
        Cell closest = food;
        Cell head = gameState.snake.peekFirst();
        int distance = gameState.maxDistance();
        // Check if gameState or snake is null
        assert (head != null);
        for (Cell c : head.neighbors()) {
            if (gameState.isOnBoard(c) && gameState.getValueAt(c) != SnakeGame.SNAKE && c.distance(food) < distance) {
                distance = c.distance(food);
                closest = c;
            }
        }


        // Snake fej pozicioja
        var headPosition = gameState.snake.peekFirst();
        assert (headPosition == null);
        AStarSearch search = new AStarSearch(gameState);
        List<Cell> direction = search.findPath(head, food);

        if (direction == null) {
            return new Direction(-1, 0);
        } else {
            // If A* doesn't find a path, fall back to the default behavior
            distance = gameState.maxDistance();
            closest = food;
            for (Cell c : head.neighbors()) {
                if (gameState.isOnBoard(c) && gameState.getValueAt(c) != SnakeGame.SNAKE && c.distance(food) < distance) {
                    distance = c.distance(food);
                    closest = c;
                }
            }
            return head.directionTo(closest);
        }
    }

    private Cell getFoodCell() {
        Cell food = null;
        for (int row = 0; row < gameState.board.length; row++) {
            for (int column = 0; column < gameState.board[row].length; column++) {
                if (gameState.board[row][column] == SnakeGame.FOOD) {
                    food = new Cell(row, column);
                    break; // A dupla for ciklusnak nem kell végigiterálnia a tömbön, ha már megtalálta az ételt. Ez időpocsékolás lenne.
                }
            }
        }
        // find the closest cell to the food of the head's neighbors
        assert (food != null);
        return food;
    }
}

class AStarSearch {

    private SnakeGameState gameState;
    private Set<List<Cell>> visitedPaths; // To track visited paths and avoid cycles
    private static final int MAX_SEARCH_DEPTH = 15;

    public AStarSearch(SnakeGameState gameState) {
        this.gameState = gameState;
        this.visitedPaths = new HashSet<>();
    }

    public List<Cell> findPath(Cell start, Cell goal) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::getF));
        Set<Node> closedSet = new HashSet<>();
        visitedPaths.clear(); // Clear visited paths for each new search

        Node startNode = new Node(start, null, 0, heuristic(start, goal));
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.cell.equals(goal)) {
                return reconstructPath(current);
            }

            closedSet.add(current);

            for (Node neighbor : getNeighbors(current)) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                int tentativeGScore = current.g + 1; // A költségfüggvény mozgásonként 1 pont

                if (!openSet.contains(neighbor) || tentativeGScore < neighbor.g) {
                    neighbor.parent = current;
                    neighbor.g = tentativeGScore;
                    neighbor.h = heuristic(neighbor.cell, goal);
                    neighbor.f = neighbor.g + neighbor.h;

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null; // No path found
    }

    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        Cell currentCell = node.cell;
        // Check the four possible directions (bal, jobb, fel, le)
        for (Direction dir : new Direction[]{new Direction(0, -1), new Direction(0, 1), new Direction(-1, 0), new Direction(1, 0)}) {
            Cell nextCell = new Cell(currentCell.i + dir.i, currentCell.j + dir.j);
            if (isValidMove(node, nextCell)) {
                neighbors.add(new Node(nextCell, null, 0, 0));
            }
        }
        return neighbors;
    }

    private boolean isValidMove(Node node, Cell nextCell) {
        // Check boundaries, snake body, and avoid creating cuts
        if (!gameState.isOnBoard(nextCell) || gameState.getValueAt(nextCell) == SnakeGame.SNAKE || createsCut(node, nextCell)) {
            return false;
        }
        // Check for cycles (revisiting the same cell)
        var path = reconstructPath(node);
        path.add(nextCell);
        if (visitedPaths.contains(path)) {
            return false;
        }
        visitedPaths.add(path);
        return true;
    }

    private boolean createsCut(Node node, Cell nextCell) {
        // Simulate the move and check for board connectivity
        SnakeGameState tempState = new SnakeGameState(gameState); // Create a copy of the game state
        tempState.snake.addFirst(nextCell); // Simulate the move
        // Check if the board is still connected after the move (implementation not provided)
        return isTrapped(tempState, nextCell);
    }

    private boolean isTrapped(SnakeGameState tempState, Cell head) {
        // 1. Mark all cells as unvisited
        boolean[][] visited = new boolean[tempState.board.length][tempState.board[0].length];

        // 2. Start a DFS or BFS from the head
        dfs1(tempState, visited, head); // Or bfs(tempState, visited, head);

        // 3. Check if any empty cell adjacent to the head is unvisited
        for (Direction dir : new Direction[]{new Direction(0, -1), new Direction(0, 1), new Direction(-1, 0), new Direction(1, 0)}) {
            Cell neighbor = new Cell(head.i + dir.i, head.j + dir.j);
            if (tempState.isOnBoard(neighbor) && tempState.getValueAt(neighbor) == SnakeGame.EMPTY && !visited[neighbor.i][neighbor.j]) {
                return false; // Head is not trapped
            }
        }

        return true; // Head is trapped
    }

    private void dfs1(SnakeGameState tempState, boolean[][] visited, Cell cell) {
        visited[cell.i][cell.j] = true;
        for (Direction dir : new Direction[]{new Direction(0, -1), new Direction(0, 1), new Direction(-1, 0), new Direction(1, 0)}) {
            Cell neighbor = new Cell(cell.i + dir.i, cell.j + dir.j);
            if (tempState.isOnBoard(neighbor) && tempState.getValueAt(neighbor) == SnakeGame.EMPTY && !visited[neighbor.i][neighbor.j]) {
                dfs1(tempState, visited, neighbor);
            }
        }
    }

    private boolean isConnected(SnakeGameState gameState) {
        // 1. Create a graph representation of the board
        Map<Cell, Set<Cell>> graph = createGraph(gameState);

        // 2. Start a Depth-First Search (DFS) or Breadth-First Search (BFS) from any empty cell
        Set<Cell> visited = new HashSet<>();
        Cell startCell = findEmptyCell(gameState);
        if (startCell == null) {
            return false; // No empty cells, board is not connected
        }
        dfs2(graph, visited, startCell); // Or bfs(graph, visited, startCell);

        // 3. Check if all empty cells have been visited
        for (int i = 0; i < gameState.board.length; i++) {
            for (int j = 0; j < gameState.board[i].length; j++) {
                Cell cell = new Cell(i, j);
                if (gameState.getValueAt(cell) == SnakeGame.EMPTY && !visited.contains(cell)) {
                    return false; // Not all empty cells are reachable
                }
            }
        }

        return true; // All empty cells are connected
    }

    private Map<Cell, Set<Cell>> createGraph(SnakeGameState gameState) {
        Map<Cell, Set<Cell>> graph = new HashMap<>();
        for (int i = 0; i < gameState.board.length; i++) {
            for (int j = 0; j < gameState.board[i].length; j++) {
                Cell cell = new Cell(i, j);
                if (gameState.getValueAt(cell) == SnakeGame.EMPTY) {
                    Set<Cell> neighbors = new HashSet<>();
                    for (Direction dir : new Direction[]{new Direction(0, -1), new Direction(0, 1), new Direction(-1, 0), new Direction(1, 0)}) {
                        Cell neighbor = new Cell(cell.i + dir.i, cell.j + dir.j);
                        if (gameState.isOnBoard(neighbor) && gameState.getValueAt(neighbor) == SnakeGame.EMPTY) {
                            neighbors.add(neighbor);
                        }
                    }
                    graph.put(cell, neighbors);
                }
            }
        }
        return graph;
    }

    private Cell findEmptyCell(SnakeGameState gameState) {
        for (int i = 0; i < gameState.board.length; i++) {
            for (int j = 0; j < gameState.board[i].length; j++) {
                Cell cell = new Cell(i, j);
                if (gameState.getValueAt(cell) == SnakeGame.EMPTY) {
                    return cell;
                }
            }
        }
        return null;
    }

    private void dfs2(Map<Cell, Set<Cell>> graph, Set<Cell> visited, Cell cell) {
        visited.add(cell);
        for (Cell neighbor : graph.getOrDefault(cell, Collections.emptySet())) {
            if (!visited.contains(neighbor)) {
                dfs2(graph, visited, neighbor);
            }
        }
    }

    private void bfs(Map<Cell, Set<Cell>> graph, Set<Cell> visited, Cell startCell) {
        Queue<Cell> queue = new LinkedList<>();
        queue.offer(startCell);
        visited.add(startCell);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();
            for (Cell neighbor : graph.getOrDefault(current, Collections.emptySet())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
    }


    private List<Cell> reconstructPath(Node current) {
        List<Cell> path = new ArrayList<>();
        while (current != null) {
            path.add(current.cell);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private int heuristic(Cell a, Cell b) {
        // Manhattan distance as base heuristic
        int manhattanDistance = Math.abs(a.i - b.i) + Math.abs(a.j - b.j);

        // Hozzáadunk egy bonuszt for moves that get closer to completing a Hamiltonian cycle
        int hamiltonianBonus = hamiltonianBonus(a, b);

        int untrappingBonus = untrappingBonus(a, b);


        return manhattanDistance - hamiltonianBonus + untrappingBonus;
    }

    private int hamiltonianBonus(Cell current, Cell next) {
        // (Implementation not provided - needs to assess progress towards a Hamiltonian cycle)
        // Example: Check if the move brings the snake closer to a cell that would help complete a cycle
        return 0; // Replace with actual bonus calculation
    }

    private int untrappingBonus(Cell current, Cell next) {
        int openSpaceCount = 0;
        SnakeGameState tempState = new SnakeGameState(gameState);
        tempState.snake.addFirst(next); // Simulate the move

        Set<Cell> visited = new HashSet<>();
        Queue<Cell> queue = new LinkedList<>();
        queue.offer(next);
        visited.add(next);

        while (!queue.isEmpty() && openSpaceCount < MAX_SEARCH_DEPTH) {
            Cell cell = queue.poll();
            for (Direction dir : new Direction[]{new Direction(0, -1), new Direction(0, 1), new Direction(-1, 0), new Direction(1, 0)}) {
                Cell neighbor = new Cell(cell.i + dir.i, cell.j + dir.j);
                if (tempState.isOnBoard(neighbor) && tempState.getValueAt(neighbor) == SnakeGame.EMPTY && !visited.contains(neighbor)) {
                    openSpaceCount++;
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return openSpaceCount;
    }

    private static class Node {
        Cell cell;
        Node parent;
        int g; // Cost from start to this node
        int h; // Heuristic estimate to goal
        int f; // Total cost (g + h)

        public Node(Cell cell, Node parent, int g, int h) {
            this.cell = cell;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        public int getF() {
            return f;
        }
    }
}
*/
