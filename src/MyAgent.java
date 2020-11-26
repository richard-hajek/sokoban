import java.util.*;

import agents.ArtificialAgent;
import game.actions.EDirection;
import game.actions.compact.*;
import game.board.compact.BoardCompact;
import game.board.oop.*;

import game.board.oop.entities.Entity;
import search.HeuristicProblem;
import search.Solution;

class InstaBoxPushAction {
    Pos pos;
    int cost;
    List<EDirection> steps;
    EDirection pushDir;

    public InstaBoxPushAction(Pos pos, List<EDirection> steps, int cost, EDirection eDirection) {
        this.pos = pos;
        this.steps = steps;
        this.cost = cost;
        this.pushDir = eDirection;
    }
}

class DFSSearchState {
    List<EDirection> steps = new LinkedList<>();
    Pos pos;
    int cost;

    DFSSearchState(Integer a, Integer b) {
        pos = new Pos(a, b);
    }

    DFSSearchState(Integer a, Integer b, List<EDirection> steps, EDirection next, int cost){
        this(a,b);
        this.steps = new LinkedList<>(steps);
        this.steps.add(next);
        this.cost = cost;
    }
}

class Pos {
    Integer x;
    Integer y;
    int i;

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

    static boolean IsSpaceYouAbsolutelySure(BoardCompact b, int x, int y){
        return
                ((b.tiles[x][y] & ESpace.WALL.getFlag()) == 0) &
                ((b.tiles[x][y] & EEntity.SOME_BOX_FLAG) == 0);
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

    static void PrintBoardCompact(BoardCompact c, int px, int py){
//        c = c.clone();
//        c.tiles[px][py] |= EEntity.PLAYER.getFlag();
//
//        if ((c.tiles[px][py] & EEntity.SOME_BOX_FLAG) != 0){
//            System.err.println("[MEOW] ERR ADDING PLAYER OVER ANOTHER ENTITY AT x: " + px + ", y: " + py);
//        }

        c.debugPrint();
    }

    static EDirection FromDeltas(int dx, int dy){
        if (dx == -1)
            return EDirection.LEFT;
        if (dx == 1)
            return EDirection.RIGHT;
        if (dy == 1)
            return EDirection.DOWN;
        if (dy == -1)
            return EDirection.UP;


        System.err.println("WARNING!!!!! Direction switch failed!\n!\n!\n!\n!\n!");
        return null;
    }

    static ArrayList<InstaBoxPushAction> FindReachableBoxes(BoardCompact b, int x, int y, boolean[][] deadSquares){

       System.out.println("On ");
       PrintBoardCompact(b, x, y);

        ArrayList<InstaBoxPushAction> boxes = new ArrayList<>();

        HashSet<Pos> explored = new HashSet<>();
        Queue<DFSSearchState> frontier = new LinkedList<>();

        frontier.add(new DFSSearchState(x, y));

        while (!frontier.isEmpty()) {
            DFSSearchState tile = frontier.poll();

            if (explored.contains(tile.pos))
                continue;

            for (int dx = 1; dx >= -1; dx--) {
                for (int dy = 1; dy >= -1; dy--) {
                    if (dx != 0 && dy != 0) continue;
                    if (dx == 0 && dy == 0) continue;

                    int c_x = tile.pos.x + dx;
                    int c_y = tile.pos.y + dy;

                    if (IsWall(b, c_x, c_y))
                        continue;

                    DFSSearchState state = new DFSSearchState(tile.pos.x + dx, tile.pos.y + dy, tile.steps, FromDeltas(dx, dy), tile.cost + 1);

                    if (IsBox(b, c_x, c_y)){
                        if (IsSpaceYouAbsolutelySure(b, c_x + dx, c_y + dy) && ! deadSquares[c_x + dx][c_y + dy]){
                            boxes.add(new InstaBoxPushAction(new Pos(tile.pos.x, tile.pos.y),  state.steps, tile.cost + 1, FromDeltas(dx, dy)));
                        }
                        continue;
                    }

                    frontier.add(state);
                }
            }

            explored.add(tile.pos);
        }

        System.out.println("Found reachable boxes:");

        for (InstaBoxPushAction box : boxes){
            System.out.print("x: " + box.pos.x + ", y: " + box.pos.y + ", with cost: " + box.cost + ", with path: ");

            for(EDirection dir : box.steps){
                System.out.print(", " + dir.name());
            }
            System.out.println();

            System.out.println(" and push dir: " + box.pushDir);
        }

        return boxes;
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

class SokobanProblem implements HeuristicProblem<BoardCompact, InstaBoxPushAction> {

    BoardCompact initial;
    boolean[][] deadSquares;

    SokobanProblem(BoardCompact initial) {
        this.initial = initial;

        //initial.tiles[initial.playerX][initial.playerY] ^= (EEntity.PLAYER.getFlag()); // remove player flag from board bcuz it sux and i hate it

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
//
//    boolean safeState(BoardCompact b){
//        for(int cx = 0; cx < b.width(); cx++){
//            for(int cy = 0; cy < b.height(); cy++){
//                if (deadSquares[cx][cy] && DeadSquareDetector.IsBox(b, cx, cy)){
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

    @Override
    public List<InstaBoxPushAction> actions(BoardCompact b) {
        List<InstaBoxPushAction> l = DeadSquareDetector.FindReachableBoxes(b, b.playerX, b.playerY, deadSquares);



        return l;
    }

    @Override
    public BoardCompact result(BoardCompact boardCompact, InstaBoxPushAction action) {
        BoardCompact next = boardCompact.clone();

        System.out.println("Moving to: x: " + action.pos.x + ", y: " + action.pos.y + ", and pushing to: " + action.pushDir);

        next.movePlayer(next.playerX, next.playerY, action.pos.x, action.pos.y);

        System.out.println("At: ");
        DeadSquareDetector.PrintBoardCompact(next, next.playerX, next.playerY);

        CPush push = new CPush(action.pushDir);

        if (! push.isPossible(next)){
            System.out.println("[MEOW] Err agent attempted invalid push " + push.getDirection().name());
            return boardCompact;
        }

        push.perform(next);

        System.out.println("Returning ");
        DeadSquareDetector.PrintBoardCompact(next, next.playerX, next.playerY);

        return next;
    }

    @Override
    public boolean isGoal(BoardCompact boardCompact) {
        return boardCompact.boxInPlaceCount == boardCompact.boxCount;
    }

    @Override
    public double cost(BoardCompact boardCompact, InstaBoxPushAction push) {
        return push.cost;
    }

    static List<EDirection> Walk(Solution<BoardCompact, InstaBoxPushAction> solution){
        List<EDirection> steps = new LinkedList<>();
        for(InstaBoxPushAction p : solution.actions){
            steps.addAll(p.steps);
        }
        return steps;
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

//        DeadSquareDetector.FindReachableBoxes(board, board.playerX, board.playerY, DeadSquareDetector.detect(board));

//        if ( 1 == 1) return null;

        SokobanProblem problem = new SokobanProblem(this.board);
        Solution<BoardCompact, InstaBoxPushAction> solution = AStar.search(problem);
        return SokobanProblem.Walk(solution);
    }
}
