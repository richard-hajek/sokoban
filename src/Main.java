import static java.lang.System.out;

import cz.sokoban4j.*;

public class Main {
    static void usage() {
        out.println("usage: sokoban [<option>...]");
        out.println("options:");
        out.println("  -levelset <name> : set of levels to play");
        System.exit(1);
    }

	public static void main(String[] args) {
        String levelset = "easy.sok";

        for (int i = 0 ; i < args.length ; ++i) {
            String s = args[i];
            switch (s) {
                case "-levelset":
                    levelset = args[++i];
                    if (levelset.indexOf('.') == -1)
                        levelset += ".sok";
                    break;
                default:
                    usage();
            }
        }

		Sokoban.playHumanFile(levelset);
	}
	
}
