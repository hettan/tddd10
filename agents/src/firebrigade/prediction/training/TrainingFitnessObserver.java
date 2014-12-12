package firebrigade.prediction.training;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import javax.swing.SwingUtilities;

import firebrigade.prediction.NeuralNetwork;
import firebrigade.prediction.NeuralNetworkPrediction;
import rescuecore2.Timestep;
import rescuecore2.messages.control.KVTimestep;
import rescuecore2.standard.components.StandardViewer;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;

/*
 * Is attached to the simulation and calculates the fitness value of the current simulation.
 * Outputs the result to a file.
 */
public class TrainingFitnessObserver extends StandardViewer {

	//Not really 100, because map scenario is a bit messed up.
	public static double MAX_FITNESS = 100;
	
    @Override
    protected void postConnect() {
        super.postConnect();
        System.out.println("Fitness observer connected");
        
    }
	
	@Override
    protected void handleTimestep(final KVTimestep t) {
        super.handleTimestep(t);
        System.out.println("Calculated fitness");
        this.model.merge(t.getChangeSet());
        double fitness = 0.0;
        if(t.getTime() == 10){
        	Collection<StandardEntity> buildings = model.getEntitiesOfType(StandardEntityURN.BUILDING);
        	double maxTotalHealth = buildings.size() * 100;
        	double totalDamage = 0.0;
        	for(StandardEntity b : buildings)
        	{
        		Building building = (Building)b;
        		if(building != null)
        		{
        			/*
        			 * brokenness
        			 * 0 = no damage
        			 * 25 = partly damaged
        			 * 50 = half collapsed
        			 * 100 = fullyCollapsed
        			 */
        			totalDamage += building.getBrokenness();
        		}
        	}
        	
        	double percentageSaved = (maxTotalHealth - totalDamage) / maxTotalHealth;
        	percentageSaved *= 100; //Scale it so it is between 0-100.

//        	String extra  = "";
//        	try
//        	{
//        		NeuralNetwork nn = new NeuralNetwork();
//        		nn.load(System.getProperty("user.dir") + "/" + EnvironmentPaths.NEURAL_NETWORK_SCENARIO);
//        		extra = "Loaded.";
//        	} catch(Exception e)
//        	{
//        		extra = "Failed to load: " + e.getMessage();
//        	}
//        	
//        	try
//        	{
//        		File file = new File("observers_logs.txt");
//        		if(!file.exists())
//        			file.createNewFile();
//                FileWriter fw = new FileWriter(file.getAbsoluteFile());
//    			BufferedWriter bw = new BufferedWriter(fw);
//    			bw.write("Working directory: " + System.getProperty("user.dir") + "\n" + extra);
//    			bw.close();
//        	}
//        	catch(Exception e)
//        	{
//        		
//        	}
        	
        	
    		try {
            	File _file = new File("fitnessObserver.txt");
            	if(!_file.exists())
            		_file.createNewFile();
                FileWriter fw = new FileWriter(_file.getAbsoluteFile());
    			BufferedWriter bw = new BufferedWriter(fw);
    			bw.write(String.valueOf(percentageSaved));
    			bw.close();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
        }
        
    }
	
    @Override
    public String toString() {
        return "Fitness Observer";
    }
}
