package firebrigade.prediction;

import firebrigade.FireArea;

/*
 * Required Resources for Extinguishing Fires prediction
 * 
 * Estimates the amount of fire brigades needed to extinguish and handle fire areas.
 */
public interface RREFPrediction {
	
	/*
	 * Gets the estimated amount of fire brigades needed to extinguish
	 * the specified fire area.
	 * 
	 * @param fireArea: The fire area to be extinguished.
	 */
	int getPrediction(FireArea fireArea);
}
