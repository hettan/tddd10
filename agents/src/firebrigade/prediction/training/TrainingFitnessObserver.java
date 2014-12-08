package firebrigade.prediction.training;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.SwingUtilities;

import rescuecore2.Timestep;
import rescuecore2.messages.control.KVTimestep;
import rescuecore2.standard.components.StandardViewer;

/*
 * Is attached to the simulation and calculates the fitness value of the current simulation.
 * Outputs the result to a file.
 */
public class TrainingFitnessObserver extends StandardViewer {

	File _file;
	

    @Override
    protected void postConnect() {
        super.postConnect();
        System.out.println("Fitness observer connected");
        _file = new File("fitnessObserver.txt");
        FileWriter fw;
		try {
			fw = new FileWriter(_file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("hi");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
	
	@Override
    protected void handleTimestep(final KVTimestep t) {
        super.handleTimestep(t);
        System.out.println("Calculated fitness");
    }
	
    @Override
    public String toString() {
        return "Fitness Observer";
    }
}
