package firebrigade.prediction.training;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.util.Set;

import rescuecore2.Constants;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.components.TCPComponentLauncher;
import rescuecore2.config.Config;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.registry.Registry;
import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardPropertyFactory;
import rescuecore2.standard.messages.StandardMessageFactory;
import sample.LaunchSampleAgents;

public class RREFPredictionTrainingConductor {
		
    public static void main(String[] args) {
    	System.out.println("Trying to start kernel...");
    	String rescue_simulator_path = "/home/jocke/git/tddd10/RescueSimulator";
    	ProcessBuilder simulatorProcessBuilder = new ProcessBuilder("./start-fb-training-simulator.sh", 
    			"-m "+ rescue_simulator_path + "/maps/Kobe2013-fire/map/",
    			"--nomenu",
    			"--autorun");
    	simulatorProcessBuilder.directory(new File(rescue_simulator_path + "/boot/"));
    	//simulatorProcessBuilder.redirectError(Redirect.INHERIT);
    	//simulatorProcessBuilder.redirectOutput(Redirect.INHERIT);
    	
    	ProcessBuilder agentsProcessBuilder = new ProcessBuilder("./start-fb-training-agents.sh",
    			"-m " + rescue_simulator_path + "/maps/Kobe2013-fire/map",
    			"--nomenu",
    			"--autorun");
    	agentsProcessBuilder.directory(new File(rescue_simulator_path + "/boot/"));
    	//agentsProcessBuilder.redirectError(Redirect.INHERIT);
    	//agentsProcessBuilder.redirectOutput(Redirect.INHERIT);
    	
    	Process simulatorProcess;
    	Process agentsProcess = null;
    	try {
    		System.out.println("Starting kernel...");
			simulatorProcess = simulatorProcessBuilder.start();
			System.out.println("Kernel started.");
			
			boolean connected = false;
			do {
				try{
					Thread.sleep(4000);
					System.out.println("Starting agents...");	
					agentsProcess = agentsProcessBuilder.start();
					System.out.println("Agents started.");
					connected = true;
				}
				catch(Exception e)
				{
					System.out.println("Failed to start agents... Retrying again");
				}
			} while(!connected);
			TrainingFitnessObserver observer = new TrainingFitnessObserver();
			try{
	            Registry.SYSTEM_REGISTRY.registerEntityFactory(StandardEntityFactory.INSTANCE);
	            Registry.SYSTEM_REGISTRY.registerMessageFactory(StandardMessageFactory.INSTANCE);
	            Registry.SYSTEM_REGISTRY.registerPropertyFactory(StandardPropertyFactory.INSTANCE);
	            Config config = new Config();
	            String[] _args = new String [] { "-c " + rescue_simulator_path + "/boot/config/sample-agents.cfg" };
	            args = CommandLineOptions.processArgs(args, config);
	            int port = config.getIntValue(Constants.KERNEL_PORT_NUMBER_KEY, Constants.DEFAULT_KERNEL_PORT_NUMBER);
	            String host = config.getValue(Constants.KERNEL_HOST_NAME_KEY, Constants.DEFAULT_KERNEL_HOST_NAME);
				ComponentLauncher launcher = new TCPComponentLauncher("localhost", 8080, config);
				
				launcher.connect(observer);
			} catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println("Running simulation...");
			simulatorProcess.waitFor();
			System.out.println("Simulation done. Closing down agents");
			if(agentsProcess != null)
				agentsProcess.destroy();
			
			
//			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//			Thread[] threads = threadSet.toArray(new Thread[threadSet.size()]);
//			for(Thread t : threads)
//			{
//				if(t == Thread.currentThread())
//					continue;
//				
//				t.interrupt();
//				t.stop();
//			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("Exiting application.");
    }
}
