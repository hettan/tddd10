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
		conductor.runGeneration();
		
		conductor.cleanup();
	}

}
