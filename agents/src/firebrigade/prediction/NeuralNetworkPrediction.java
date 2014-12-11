package firebrigade.prediction;

import java.io.IOException;

import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;
import firebrigade.FireArea;
import firebrigade.prediction.training.EnvironmentPaths;

public class NeuralNetworkPrediction implements RREFPrediction {
	public static boolean IS_TRAINING = true;
	public static final int FIREBRIGADES_LOWERBOUND = 1;
	public static final int FIREBRIGADES_UPPERBOUND = 20;
	
	private NeuralNetwork _neuralNetwork;
	private StandardWorldModel _model;
	
	/*
	 * The model needs to contain updated knowledge about the buildings
	 * to properly function. (it needs updates on buildings temperature and stuff).
	 */
	public NeuralNetworkPrediction(StandardWorldModel model) throws IOException
	{
		_model = model;
		_neuralNetwork = new NeuralNetwork();
		if(IS_TRAINING)
		{
			//load from scenario chromosome
			_neuralNetwork.load(EnvironmentPaths.NEURAL_NETWORK_SCENARIO);
		}
		else
		{
			//load trained
			_neuralNetwork.load(EnvironmentPaths.NEURAL_NETWORK_TRAINED);
		}
		
		if(!_neuralNetwork.isLoaded()) //Failed to load. Throw exception.
		{
			if(IS_TRAINING)
				throw new IOException("Failed to load NN from: " + EnvironmentPaths.NEURAL_NETWORK_SCENARIO);
			throw new IOException("Failed to load NN from file: " + EnvironmentPaths.NEURAL_NETWORK_TRAINED);
		}
	}
	
	@Override
	public int getPrediction(FireArea fireArea) {
		// TODO Auto-generated method stub
		double[] predictions = _neuralNetwork.getPredictions(getFactors(fireArea));
		return extractNeuralNetworkResult(predictions);
	}
	
	private double[] getFactors(FireArea fireArea)
	{
		double[] values = new double[6];
		values[0] = fireArea.getBuildingsInArea().size();
		values[1] = getSize(fireArea);
		values[2] = getAverageTemperature(fireArea);
		values[3] = getAverageBuildingMaterial(fireArea);
		values[4] = getAverageDamage(fireArea);
		values[5] = getAverageFieryness(fireArea);
		return values;
	}
	
	private int extractNeuralNetworkResult(double[] predictions)
	{
		double result = predictions[0];
		int amountOfFirebrigades = (int)Math.ceil(result * (FIREBRIGADES_UPPERBOUND - FIREBRIGADES_LOWERBOUND) + FIREBRIGADES_LOWERBOUND);
		return amountOfFirebrigades;
	}
	
	private double getSize(FireArea fireArea)
	{
		double totalSize = 0;
		for(Integer id : fireArea.getBuildingsInArea())
		{
			Building building = (Building)_model.getEntity(new EntityID(id));
			if(building == null)
			{
			}
			else
			{
				totalSize += building.getTotalArea();
			}
		}
		return totalSize;
	}
	
	private double getAverageTemperature(FireArea fireArea)
	{
		double totalTemperature = 0;
		double amountOfBuildings = fireArea.getBuildingsInArea().size();
		for(Integer id : fireArea.getBuildingsInArea())
		{
			Building building = (Building)_model.getEntity(new EntityID(id));
			if(building == null)
			{
				amountOfBuildings--;
			}
			else
			{
				totalTemperature += building.getTemperature();
			}
		}
		return totalTemperature / amountOfBuildings;
	}
	
	private double getAverageBuildingMaterial(FireArea fireArea)
	{
		double totalBuildingMaterial = 0;
		double amountOfBuildings = fireArea.getBuildingsInArea().size();
		for(Integer id : fireArea.getBuildingsInArea())
		{
			Building building = (Building)_model.getEntity(new EntityID(id));
			if(building == null)
			{
				amountOfBuildings--;
			}
			else
			{
				totalBuildingMaterial += building.getBuildingAttributes();
			}
		}
		return totalBuildingMaterial / amountOfBuildings;
	}
	
	private double getAverageDamage(FireArea fireArea)
	{
		double totalBuildingDamage = 0;
		double amountOfBuildings = fireArea.getBuildingsInArea().size();
		for(Integer id : fireArea.getBuildingsInArea())
		{
			Building building = (Building)_model.getEntity(new EntityID(id));
			if(building == null)
			{
				amountOfBuildings--;
			}
			else
			{
				totalBuildingDamage += building.getBrokenness();
			}
		}
		return totalBuildingDamage / amountOfBuildings;
	}
	
	private double getAverageFieryness(FireArea fireArea)
	{
		double totalBuildingFieryness = 0;
		double amountOfBuildings = fireArea.getBuildingsInArea().size();
		for(Integer id : fireArea.getBuildingsInArea())
		{
			Building building = (Building)_model.getEntity(new EntityID(id));
			if(building == null)
			{
				amountOfBuildings--;
			}
			else
			{
				totalBuildingFieryness += building.getFieryness();
			}
		}
		return totalBuildingFieryness / amountOfBuildings;
	}
}
