import static java.lang.System.out;

import java.io.File;

import game.*;
import tournament.*;

public class Main {
    static String describe(SokobanResultType type) {
        switch (type) {
            case VICTORY: return "solved";
            case TIMEOUT: return "TIMEOUT";
            default: return "FAILED";
        }
    }

    static void runLevel(
        String agentName, String levelset, int level, int maxFail,
        String resultFile, int timeout, boolean verbose, boolean optimal) throws Exception {

        IAgent agent = (IAgent) Class.forName(agentName).getConstructor().newInstance();
        agent.init(optimal, verbose);

        if (verbose)
            System.out.println("====================");
        System.out.printf("solving level %d... ", level);
        if (verbose)
            System.out.println();

        SokobanResult result =
            Sokoban.simAgentLevel(null, levelset, level, timeout, agent, verbose, optimal);

        SokobanResultType resultType = result.getResult();
        System.out.printf("%s in %.1f ms",
            describe(resultType), (double) result.getSimDurationMillis());

        if (resultType == SokobanResultType.VICTORY)
            System.out.printf(" (%d steps)", result.getSteps());
        if (result.message != null)
            System.out.printf(" (%s)", result.message);

        System.out.println();

        if (resultFile != null)
            result.outputResult(new File(resultFile), levelset, level, agentName);
        System.exit(resultType.getExitValue());	    	    
    }

    static void runLevelSet(String agentName, String levelset, int maxFail, String resultFile,
                            int timeout, boolean verbose, boolean optimal) {
        SokobanLevels levels = SokobanLevels.fromString(levelset + ";all");
        System.out.printf("Running %s on levels in %s\n", agentName, levelset);

        SokobanConfig config = new SokobanConfig();
        config.requireOptimal = optimal;
        config.timeoutMillis = timeout;
        config.verbose = verbose;

        RunSokobanLevels run = new RunSokobanLevels(
            config, agentName, levels,
            resultFile == null ? null : new File(resultFile), maxFail);
        run.run();
    }

    static void usage() {
        out.println("usage: sokoban [<agent-classname>] [<option>...]");
        out.println("options:");
        out.println("  -level <num> : level number to play");
        out.println("  -levelset <name> : set of levels to play");
        out.println("  -maxfail <num> : maximum level failures allowed");
        out.println("  -optimal : require move-optimal solutions");
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
        boolean optimal = false;
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
                case "-optimal":
                    optimal = true;
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
                runLevel(agentName, levelset, level, maxFail, resultFile, timeout, verbose, optimal);
            else
                runLevelSet(agentName, levelset, maxFail, resultFile, timeout, verbose, optimal);
    }
}
