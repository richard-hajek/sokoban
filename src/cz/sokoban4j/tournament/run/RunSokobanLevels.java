package cz.sokoban4j.tournament.run;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import cz.sokoban4j.SokobanConfig;
import cz.sokoban4j.simulation.SokobanResult.SokobanResultType;

/**
 * Runs {@link SokobanLevels} executing SEPARATE JVM for every {@link SokobanLevels#levels} sequentially.
 * Stops executing levels once an agent fails to solve the level.
 *  
 * @author Jimmy
 */
public class RunSokobanLevels {
	
	private SokobanLevels levels;
	
	private String agentClass;
	
	private File resultFile;
	
	private SokobanConfig config;
	
	private String[] extraJavaArgs;
	
	private int maxFail;
	
	public RunSokobanLevels(SokobanConfig config, String agentClass, SokobanLevels levels,
			                File resultFile, String[] extraJavaArgs, int maxFail) {
		super();
		this.config = config;
		this.agentClass = agentClass;
		this.levels = levels;
		this.resultFile = resultFile;
		this.extraJavaArgs = extraJavaArgs;
		this.maxFail = maxFail;
	}

	public void run() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%4$s] %1$tT.%1$tL %5$s%n");
		
		int failed = 0;
		
		for (int i = 0; i < levels.levels.size(); ++i) {			
	    	SokobanLevel level = levels.levels.get(i);
	    	
			// CONFIGURE PROGRAM PARAMS
            List<String> args = new ArrayList<String>();
            args.add("java");

            args.add("-cp");
            args.add(System.getProperty("java.class.path"));

			// READ JAVA PARAMS
			RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
	    	List<String> jvmArgs = runtimeMXBean.getInputArguments();
	    	for (String arg : jvmArgs) {
	    		if (arg.contains("agentlib") && arg.contains("suspend")) {
	    			// ECLIPSE DEBUGGING, IGNORE
	    			continue;
	    		}
	    	    args.add(arg);
	    	}
	    	
	    	// ADD EXTRA JAVA PARAMS
	    	if (extraJavaArgs != null && extraJavaArgs.length > 0) {
	    		for (String extraArg : extraJavaArgs) {
	    			args.add(extraArg);
	    		}
	    	}
	    	
	    	// ADD CLASS TO RUN	    	
	    	args.add("Main");
	    	
            // ADD SOKOBAN CONSOLE ARGUMENTS
            
            args.add(agentClass);

            args.add("-levelset");
            args.add(level.file.getAbsolutePath());

            args.add("-level");
            args.add("" + level.levelNumber);

            if (resultFile != null) {
                args.add("-resultfile");
                args.add(resultFile.getAbsolutePath());
            }

            if (config.timeoutMillis > 0) {
                args.add("-timeout");
                args.add("" + config.timeoutMillis);
            }
			
	    	System.out.println("");
	    	System.out.println("===============================================");
	    	System.out.println("RUNNING " + (i+1) + " / " + levels.levels.size() + " FOR " + agentClass);
	    	System.out.println("===============================================");
            
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectErrorStream(true);

            Process p;

            try {
                p = pb.start();
                p.waitFor();
            } catch (Exception e) { throw new RuntimeException(e); }
			
	    	if (p.exitValue() > 0) {
	    		System.out.println("========================================================");
		    	System.out.println("AGENT FAILED TO SOLVE THE LEVEL " + (i+1));
		    	System.out.println(level.file.getName() + " / " + level.levelNumber);
		    	System.out.println("Exit code: " + p.exitValue() + " ~ " + SokobanResultType.getForExitValue(p.exitValue()));
		    	System.out.println("========================================================");
	    		if (++failed == maxFail)
	    		    break;
	    	}
		}
		
	}
	
}
