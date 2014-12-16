package firebrigade.prediction;

import firebrigade.FireArea;

public class DumbRREFPrediction implements RREFPrediction {

	@Override
	public int getPrediction(FireArea fireArea) {
		return 6;
	}

}
