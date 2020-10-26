import static java.lang.System.out;

import java.io.File;

import cz.sokoban4j.*;
import cz.sokoban4j.simulation.*;
import cz.sokoban4j.simulation.agent.IAgent;
import cz.sokoban4j.tournament.run.*;

public class Main {
    static String describe(SokobanResultType type) {
        switch (type) {
            case VICTORY: return "solved";
            case TIMEOUT: return "TIMEOUT";
            default: return "FAILED";
        }
    }

    static void runLevel(String agentName, String levelset, int level, int maxFail,
                         String resultFile, int timeout, boolean verbose) throws Exception {
        IAgent agent = (IAgent) Class.forName(agentName).getConstructor().newInstance();
        agent.init(verbose);

        if (!verbose)
            System.out.printf("solving level %d... ", level);

        SokobanResult result = Sokoban.simAgentLevel(null, levelset, level, timeout, agent);

        if (!verbose) {
            System.out.printf("%s in %.1f ms",
                describe(result.getResult()), (double) result.getSimDurationMillis());

            if (result.getResult() == SokobanResultType.VICTORY)
                System.out.printf(" (%d steps)", result.getSteps());
            
            System.out.println();
        }

        if (resultFile != null)
            result.outputResult(new File(resultFile), levelset, level, agentName);
        System.exit(result.getResult().getExitValue());	    	    
    }

    static void runLevelSet(String agentName, String levelset, int maxFail, String resultFile,
                            int timeout, boolean verbose) {
        SokobanLevels levels = SokobanLevels.fromString(levelset + ";all");
        SokobanConfig config = new SokobanConfig();
        config.timeoutMillis = timeout;
        RunSokobanLevels run = new RunSokobanLevels(
            config, agentName, levels,
            resultFile == null ? null : new File(resultFile), maxFail, verbose);
        run.run();
    }

    static void usage() {
        out.println("usage: sokoban [<agent-classname>] [<option>...]");
        out.println("options:");
        out.println("  -level <num> : level number to play");
        out.println("  -levelset <name> : set of levels to play");
        out.println("  -maxfail <num> : maximum level failures allowed");
        out.println("  -resultfile <filename> : file to append results to");
        out.println("  -timeout <num> : maximum thinking time in milliseconds");
        out.println("  -v : verbose output");
        System.exit(1);
    }

	public static void main(String[] args) throws Exception {
        String agentName = null;
        String levelset = "easy.sok";
        int level = 0;
        int maxFail = 0;
        String resultFile = null;
        int timeout = 0;
        boolean verbose = false;

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
                case "-maxfail":
                    maxFail = Integer.parseInt(args[++i]);
                    break;
                case "-resultfile":
                    resultFile = args[++i];
                    break;
                case "-timeout":
                    timeout = Integer.parseInt(args[++i]);
                    break;
                case "-v":
                    verbose = true;
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
            if (level > 0)
                runLevel(agentName, levelset, level, maxFail, resultFile, timeout, verbose);
            else
                runLevelSet(agentName, levelset, maxFail, resultFile, timeout, verbose);
    }
}
