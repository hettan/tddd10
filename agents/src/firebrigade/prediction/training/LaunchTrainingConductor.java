package firebrigade.prediction.training;

import java.io.IOException;

import firebrigade.prediction.training.Logger.Level;

public class LaunchTrainingConductor {

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub		
		TrainingConductor conductor = new TrainingConductor();
		
		try {
			conductor.initialize();
		} catch (IOException e) {
			Logger.Write(Level.ERROR, "Could not initialize conductor. Aborting training.");
		}
		
		//Run conductors training.
		for(int i = 0; i < 3; i++)
			conductor.runGeneration();
		
		//Removes all temporary files created for the training.
		conductor.cleanup();
		
		//Saves the best network.
		try {
			conductor.saveBestNetwork();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
