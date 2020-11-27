import game.board.compact.BoardCompact;
import game.board.oop.ESpace;

import java.util.ArrayList;
import java.util.Arrays;

public class Validator {
    static public boolean AreValidReachableBoxes(BoardCompact template, BoardState s, int x, int y, boolean[][] deadSquares, ArrayList<BoxPushAction> actions){
        for(BoxPushAction a : actions){

            boolean foundPushingBox = false;

            int bx = a.player.x + a.direction.dX;
            int by = a.player.y + a.direction.dY;

            int[] expected = new int[]{bx, by};

            // check if there is a box to be pushed
            for (int i = 0; i < s.boxCount; i++){
                if (Arrays.equals(s.boxes[i], expected)){
                    foundPushingBox = true;
                    break;
                }
            }

            if (! foundPushingBox){
                return false;
            }

            int check_x = bx + a.direction.dX;
            int check_y = by + a.direction.dY;

            int[] sus = new int[]{check_x, check_y};

            // check if there is a wall or another box behind this box
            for(int i = 0; i < s.boxCount; i++){
                if (Arrays.equals(s.boxes[i], sus)){
                    return false; // found a box where a box should be pushed
                }
            }

            // check if there's a wall behind that box that we want to push
            if ((template.tiles[check_x][check_y] & ESpace.WALL.getFlag()) != 0){
                return false;
            }
        }

        return true;
    }
}
