package firebrigade.prediction.training;

public class TrainingResult {
	//private int _fitnessResult
	
	private int _fitnessResult;
	
	public TrainingResult(int result)
	{
		_fitnessResult = result;
	}
	
	public int getFitnessResult()
	{
		return _fitnessResult;
	}
	
	public String toPersistanceFormat()
	{
		return "TRAINING_RESULT: " + _fitnessResult;
	}
	
	public static TrainingResult fromPersistanceFormat(String string)
	{
		return new TrainingResult(2);
	}
}
