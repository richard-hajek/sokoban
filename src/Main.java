import static java.lang.System.out;

import cz.sokoban4j.*;
import cz.sokoban4j.simulation.SokobanResult;
import cz.sokoban4j.simulation.agent.IAgent;
import cz.sokoban4j.tournament.run.*;

public class Main {
    static void usage() {
        out.println("usage: sokoban [<agent-classname>] [<option>...]");
        out.println("options:");
        out.println("  -levelset <name> : set of levels to play");
        out.println("  -level <num> : level number to play");
        System.exit(1);
    }

	public static void main(String[] args) throws Exception {
        String levelset = "easy.sok";
        int level = 0;
        String agentName = null;

        for (int i = 0 ; i < args.length ; ++i) {
            String s = args[i];
            switch (s) {
                case "-level":
                    level = Integer.parseInt(args[++i]);
                    break;
                case "-levelset":
                    levelset = args[++i];
                    if (levelset.indexOf('.') == -1)
                        levelset += ".sok";
                    break;
                default:
                    if (s.startsWith("-"))
                        usage();
                    agentName = s;
            }
        }

        if (agentName == null)
            if (level > 0)
                Sokoban.playHumanLevel(levelset, level);
            else
                Sokoban.playHumanFile(levelset);
        else
            if (level > 0) {
                IAgent agent = (IAgent) Class.forName(agentName).getConstructor().newInstance();
                SokobanResult result = Sokoban.simAgentLevel(levelset, level, agent);
                System.exit(result.getResult().getExitValue());	    	    
            } else {
                SokobanLevels levels = SokobanLevels.fromString(levelset + ";all");
                SokobanConfig config = new SokobanConfig();
                RunSokobanLevels run = new RunSokobanLevels(config, agentName, levels, null, null, 1);
                run.run();
            }
	}
	
}
