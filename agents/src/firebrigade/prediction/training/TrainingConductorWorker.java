package firebrigade.prediction.training;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import firebrigade.prediction.NeuralNetworkPrediction;
import firebrigade.prediction.training.Logger.Level;
import firebrigade.prediction.training.genetics.Chromosome;
import firebrigade.prediction.training.genetics.GenerationGeneticResult;

public class TrainingConductorWorker {
	
	private boolean _isActive;
	private boolean _isDone;
	private boolean _isInitialized;
	
	private Process _simulatorProcess;
	private Process _agentsProcess;
	private Thread _simulationStarter;
	
	private GenerationGeneticResult _result;
	
	public TrainingConductorWorker()
	{
		_isActive = false;
		_isDone = false;
	}
	
	public void initialize()
	{
		try {
			generateBootFolder();
			_isInitialized = true;
		} catch (IOException e) {
			_isInitialized = false;
			Logger.Write(Level.ERROR, "Could not copy boot directory to: " + this.getBootFolderPath());
		}
	}
	
	/*
	 * Starts an asynhcronous training conductor which
	 * runs the simulation of the specified port and host
	 */
	public void runSimulationAsync(TrainingScenario scenario)
	{
		if(!_isInitialized)
			return;
		
		if(_isActive)
			return; //Handle this better maybe	
		
		try {
			updateConnectionDetails(scenario);		
			setPermissions();
			_simulationStarter = new Thread(new Runnable(){
				public void run() {
					startSimulation();
				}
			});
			_isActive = true;
			_isDone = false;
			_simulationStarter.start();
			
		} catch (IOException e) {
			return; //Handle this better aswell.
		}
	}
	
	private void startSimulation()
	{
		ProcessBuilder simulatorProcessBuilder = new ProcessBuilder("./start-fb-training-simulator.sh", 
    			"-m "+ EnvironmentPaths.RESUCE_TRAINING_MAP_PATH + "/map/",
    			"--nomenu",
    			"--autorun",
    			"--nogui");
		simulatorProcessBuilder.directory(new File(getBootFolderPath()));
//    	simulatorProcessBuilder.redirectError(Redirect.INHERIT);
//    	simulatorProcessBuilder.redirectOutput(Redirect.INHERIT);
		ProcessBuilder agentsProcessBuilder = new ProcessBuilder("./start-fb-training-agents.sh",
    			"-m " + EnvironmentPaths.RESUCE_TRAINING_MAP_PATH + "/map/",
    			"--nomenu",
    			"--autorun",
    			"--nogui");
		agentsProcessBuilder.directory(new File(getBootFolderPath()));
		
		// If i don't redirect the output, the process refuses to close itself.
    	agentsProcessBuilder.redirectError(Redirect.INHERIT);
    	agentsProcessBuilder.redirectOutput(Redirect.INHERIT);
		
		try {
			_simulatorProcess = simulatorProcessBuilder.start();
			Logger.Write("Simulator process started. (" + this.getID() + ")");
			boolean connected = false;
			int count = 1;
			do {
				try{
					Thread.sleep(10000);
					_agentsProcess = agentsProcessBuilder.start();
					Logger.Write("Agents process started. (" + this.getID() + ")");
					connected = true;
				}
				catch(Exception e)
				{
					Logger.Write(Level.ERROR, "Could not connect agents. (#" + count + "/6)");
					if(count == 6)
						break;
				}
			} while(!connected);
			
			if(!connected)
			{
				//Failed to connect agents. Error!
				throw new IOException("Could not connect agents.");
			}
		} catch (IOException e) {
			
			Logger.Write(Level.ERROR, "Failed to start simulation (" + this.getID() + "): " + e.getMessage());
			_isActive = false;
			return; //Handle better?
		}
		
		Logger.Write("Simulation started: " + this.getID());
		_isActive = true;
	}
	
	/*
	 * Checks if the current training conductor is
	 * running a simulation.
	 */
	public boolean isActive()
	{
		return _isActive;
	}
	
	/*
	 * Checks if the training conductor is done with
	 * running the simulator.
	 */
	public boolean isDone()
	{
		return _isDone;
	}
	
	public void saveResults()
	{
		String fitnessResult = "0.0";
		File fitnessFile = new File(getBootFolderPath() + "/fitnessObserver.txt");
		try {
			fitnessResult = IOUtils.toString(new FileInputStream(fitnessFile));
		} catch (FileNotFoundException e) {
			Logger.Write(Level.ERROR, "Could not open file: " + e.getMessage());
		} catch (IOException e) {
			Logger.Write(Level.ERROR, "Could not read from file: " + e.getMessage());
		}
		String chromosomeResult = "0";
		File chromosomeFile = new File(getBootFolderPath() + "/chromosome.txt");
		try {
			chromosomeResult = IOUtils.toString(new FileInputStream(chromosomeFile));
		} catch (FileNotFoundException e) {
			Logger.Write(Level.ERROR, "Could not open file: " + e.getMessage());
		} catch (IOException e) {
			Logger.Write(Level.ERROR, "Could not read from file: " + e.getMessage());
		}
		
		_result = GenerationGeneticResult.fromPersistanceFormat(fitnessResult + "\n" + chromosomeResult);
	}
	
	/*
	 * Releases all the resources used for
	 * the simulation.
	 */
	public void releaseResources()
	{
		File agentsStartPidFile = new File(getBootFolderPath() + "/" + "agents_status.txt"); 
		try{
			if(!agentsStartPidFile.exists())
				agentsStartPidFile.createNewFile();
			
			//This is hardcoded in the agents startup script, it's waiting  for the done to be written to the file.
			//The reason is because the script alone has all the PIDs, which it can then kill by itself, much easier than
			//trying ugly hacks from eclipse.
			FileWriter fw = new FileWriter(agentsStartPidFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("done");
			bw.close();
			Logger.Write("Wrote done to file.");
		}
		catch(Exception e)
		{
			Logger.Write(Level.ERROR, "Could not remove the agents script process: " + e.getMessage());
		}
		
		_isActive = false;
		Logger.Write("Relased resouces: " + this.getID());
	}
	
	
	/*
	 * Gets the result from the previous simulation run.
	 */
	public GenerationGeneticResult getResult()
	{
		return _result;
	}
	
	/*
	 * Halts the current thread and waits for the worker
	 * to finish.
	 */
	public void waitFor()
	{
		//Wait for startup first.
		try {
			_simulationStarter.join();
			_simulatorProcess.waitFor();
			Logger.Write("Done waiting for this worker, simulation is done: " + getID());
		} catch (Exception e) {
			Logger.Write(Level.ERROR, "Could not join the simulation starter thread: " + e.getMessage());
		}
	}
	
	public boolean isInitialized()
	{
		return _isInitialized;
	}
	/*
	 * Copies the boot folder, and creates a new boot
	 * folder for this worker. The new folder will have the
	 * name: "boot-<ID of this worker>". 
	 * 
	 */
	private void generateBootFolder() throws IOException
	{
		File originBootFolder = new File(EnvironmentPaths.RESCUE_SIMULATOR_PATH + "/boot/");
		File copiedBootFolder = new File(this.getBootFolderPath());
		
		if(!copiedBootFolder.exists())
		{
			FileUtils.copyDirectory(originBootFolder, copiedBootFolder);
			Logger.Write("Generated boot folder for worker: " + this.getBootFolderPath());
			_isInitialized = true;
		}
	}
	
	/*
	 * Updates the connection details for the new simulation.
	 * This require a boot folder to exist, otherwise it will fail.
	 */
	private void updateConnectionDetails(TrainingScenario scenario) throws IOException
	{
		if(_isInitialized)
		{
			String path = getBootFolderPath() + "/config/common.cfg";
			File commonConfig = new File(path);
			if(commonConfig.exists())
			{
				String content = "!include types.cfg\n# Random seed for all components\nrandom.seed: 1023\n";
				content += "kernel.host:" + scenario.getHost() + "\n";
				content += "kernel.port: " + scenario.getPort() + "\nsenario.human.random-id: true";
				
				try {
					FileWriter fw = new FileWriter(commonConfig.getAbsoluteFile());
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(content);
					bw.close();
					Logger.Write("Added connection details to commons config.");
				} catch (IOException e) {
					Logger.Write(Level.ERROR, "Could not write to commons cfg: " + e.getMessage());
					throw e;
				}
			}
			else
			{
				Logger.Write(Level.ERROR, "Could not locate commons.cfg: " + path);
				throw new IOException("Could not locate file: " + path);
			}
			
			File chromosomeFile = new File(getBootFolderPath() + "/chromosome.txt");
			if(!chromosomeFile.exists())
				chromosomeFile.createNewFile();
			try
			{
				BufferedWriter writer = new BufferedWriter(new FileWriter(chromosomeFile));
				writer.write(scenario.getChromosome().toString());
				writer.close();
				Logger.Write("Added chromosome details to scenario.");
			} 
			catch(Exception e)
			{
				Logger.Write(Level.ERROR, "Could not store chromosome file. Aborting.");
				throw e;				
			}
		}
	}
	
	private void setPermissions()
	{
		File simulationStart = new File(getBootFolderPath() + "/" + EnvironmentPaths.SIMULATOR_START_SCRIPT);
		simulationStart.setExecutable(true);
		simulationStart.setReadable(true);
		simulationStart.setWritable(true);
		File agentStart = new File(getBootFolderPath() + "/" + EnvironmentPaths.AGENT_START_SCRIPT);
		agentStart.setExecutable(true);
		agentStart.setReadable(true);
		agentStart.setWritable(true);
	}
	
	public String getID()
	{
		return String.valueOf(this.hashCode());
	}
	
	public String getBootFolderPath()
	{
		return EnvironmentPaths.RESCUE_SIMULATOR_PATH + "/" + "boot-" + getID();
	}
	
	public void cleanup()
	{
		//Remove the directoyr
		if(_isInitialized)
		{
			try {
				FileUtils.deleteDirectory(new File(getBootFolderPath()));
				Logger.Write("Disposed worker. Removed: " + this.getBootFolderPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Logger.Write("Failed at disposing worker: " + e.getMessage());
			}
			_isInitialized = false;
		}
	}
	/**
	 * Get the process id (PID) associated with a {@code Process}
	 * @param process {@code Process}, or null
	 * @return Integer containing the PID of the process; null if the
	 *  PID could not be retrieved or if a null parameter was supplied
	 */
	private Integer retrievePID(final Process process) {
	    if (process == null) {
	        return null;
	    }

	    //--------------------------------------------------------------------
	    // Jim Tough - 2014-11-04
	    // NON PORTABLE CODE WARNING!
	    // The code in this block works on the company UNIX servers, but may
	    // not work on *any* UNIX server. Definitely will not work on any
	    // Windows Server instances.
	    final String EXPECTED_IMPL_CLASS_NAME = "java.lang.UNIXProcess";
	    final String EXPECTED_PID_FIELD_NAME = "pid";
	    final Class<? extends Process> processImplClass = process.getClass();
	    if (processImplClass.getName().equals(EXPECTED_IMPL_CLASS_NAME)) {
	        try {
	            Field f = processImplClass.getDeclaredField(
	                    EXPECTED_PID_FIELD_NAME);
	            f.setAccessible(true); // allows access to non-public fields
	            int pid = f.getInt(process);
	            return pid;
	        } catch (Exception e) {
	            Logger.Write(Level.WARNING, "Unable to get PID");
	        }
	    } else {
	    }
	    //--------------------------------------------------------------------

	    return null; // If PID was not retrievable, just return null
	}
}
