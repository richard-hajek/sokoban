import java.util.*;

import agents.ArtificialAgent;
import game.actions.EDirection;
import game.actions.compact.*;
import game.board.compact.BoardCompact;
import game.board.oop.EEntity;
import game.board.oop.EPlace;
import game.board.oop.ESpace;

import search.HeuristicProblem;
import search.Solution;

class Pos {
    Integer x;
    Integer y;

    Pos(Integer a, Integer b) {
        x = a;
        y = b;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pos))
            return false;

        Pos q = (Pos) o;
        return x.equals(q.x) && y.equals(q.y);
    }

    @Override
    public int hashCode() {
        return x.hashCode() ^ y.hashCode();
    }

    public int ManhattanDistance(Pos b){
        return Math.abs(x - b.x) + Math.abs(y - b.y);
    }

    public double EuclideanDistance(Pos b){
        return Math.sqrt(Math.pow(b.x - x, 2) + Math.pow(b.y - y, 2));
    }
}

class DeadSquareDetector {

    static boolean IsSpace(BoardCompact b, int x, int y) {
        return (b.tiles[x][y] & ESpace.WALL.getFlag()) == 0;
    }

    static boolean IsWall(BoardCompact b, int x, int y) {
        return !IsSpace(b, x, y);
    }

    static boolean IsGoal(BoardCompact b, int x, int y){
        return (b.tiles[x][y] & EPlace.SOME_BOX_PLACE_FLAG) != 0;
    }

    static boolean IsBox(BoardCompact b, int x, int y){
        return (b.tiles[x][y] & EEntity.SOME_BOX_FLAG) != 0;
    }

    public static boolean[][] detect(BoardCompact board) {
        int width = board.width();
        int height = board.height();

        boolean[][] deadTiles = new boolean[width][height];

        for (boolean[] array : deadTiles)
            Arrays.fill(array, true);

        HashSet<Pos> explored = new HashSet<>();
        Queue<Pos> frontier = new LinkedList<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (IsGoal(board, x, y))
                    frontier.add(new Pos(x, y));
            }
        }

        while (!frontier.isEmpty()) {
            Pos tile = frontier.poll();

            if (explored.contains(tile))
                continue;

            deadTiles[tile.x][tile.y] = false;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {

                    if (dx != 0 && dy != 0)
                        continue;

                    if (!IsSpace(board, tile.x + dx, tile.y + dy) || !IsSpace(board, tile.x + dx * 2, tile.y + dy * 2)) {
                        continue;
                    }

                    frontier.add(new Pos(tile.x + dx, tile.y + dy));
                }
            }

            explored.add(tile);
        }

        return deadTiles;
    }
}

class SokobanProblem implements HeuristicProblem<BoardCompact, EDirection> {

    BoardCompact initial;
    boolean[][] deadSquares;

    SokobanProblem(BoardCompact initial) {
        this.initial = initial;
        this.deadSquares = DeadSquareDetector.detect(initial);
    }

    @Override
    public double estimate(BoardCompact b) {
        List<Pos> goals = new ArrayList<>();
        double total = 0;

        for(int x = 0; x < b.width(); x++){
            for(int y = 0; y < b.height(); y++){
                if (DeadSquareDetector.IsGoal(b, x, y))
                    goals.add(new Pos(x, y));
            }
        }

        for(int x = 0; x < b.width(); x++){
            for(int y = 0; y < b.height(); y++){
                double shortest = Double.POSITIVE_INFINITY;
                if (DeadSquareDetector.IsBox(b, x, y)){
                    for(Pos g : goals){
                        if (g.ManhattanDistance(new Pos(x, y)) < shortest){
                            shortest = g.ManhattanDistance(new Pos(x, y));
                        }
                    }
                }
                total += shortest;
            }
        }

        return (int) total;
    }

    @Override
    public BoardCompact initialState() {
        return initial;
    }

    boolean safeState(BoardCompact b){
        for(int cx = 0; cx < b.width(); cx++){
            for(int cy = 0; cy < b.height(); cy++){
                if (deadSquares[cx][cy] && DeadSquareDetector.IsBox(b, cx, cy)){
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public List<EDirection> actions(BoardCompact b) {
        List<EDirection> l = new ArrayList<>();

//        System.out.println("\n\n\n");
//        System.out.println("ACTION SCANNING");
//        System.out.println("For board: " + b);
//        System.out.println("Found : ");

        for(EDirection eDirection : EDirection.arrows()){

            CMove move = new CMove(eDirection);
            CPush push = new CPush(eDirection);

            if (move.isPossible(b) || push.isPossible(b)){

                if (push.isPossible(b)){
                    BoardCompact test = b.clone();
                    push.perform(test);

                    if (! safeState(test))
                        continue;
                }

                l.add(eDirection);
//                System.out.println("Action: " + eDirection);
            }
        }
//        System.out.println("\n\n\n");

        return l;
    }

    @Override
    public BoardCompact result(BoardCompact boardCompact, EDirection eDirection) {
        BoardCompact next = boardCompact.clone();

//        System.out.println("\n\n\n\n");
//        System.out.println(next);

        CMove move = new CMove(eDirection);
        CPush push = new CPush(eDirection);

        if (! move.isPossible(next) && ! push.isPossible(next)){
            //System.out.println("[MEOW] AGENT ATTEMPTED INVALID MOVE(" + move + ", " + push + "), refusing to change state");
//            System.out.println("[MEOW] Err agent attempted invalid move " + eDirection);
            return next;
        }

        if (move.isPossible(next)){
            move.perform(next);
        }
        else if (push.isPossible(next)){
            push.perform(next);
        }

//        System.out.println("Successfuly moved in direction " +  eDirection +  " to:");
//        System.out.println(next);
//        System.out.println("\n\n\n\n");

        return next;
    }

    @Override
    public boolean isGoal(BoardCompact boardCompact) {
        return boardCompact.boxInPlaceCount == boardCompact.boxCount;
    }

    @Override
    public double cost(BoardCompact boardCompact, EDirection eDirection) {
        return 1;
    }
}

/**
 * The simplest Tree-DFS agent.
 *
 * @author Jimmy
 */
public class MyAgent extends ArtificialAgent {
    protected BoardCompact board;

    @Override
    protected List<EDirection> think(BoardCompact board) {
        this.board = board.clone();
        SokobanProblem problem = new SokobanProblem(this.board);
        Solution<BoardCompact, EDirection> solution = AStar.search(problem);
        return solution.actions;
    }
}
