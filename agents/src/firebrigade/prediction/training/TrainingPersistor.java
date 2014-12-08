package firebrigade.prediction.training;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

import firebrigade.prediction.training.Logger.Level;

public class TrainingPersistor {

	private final String STORAGE_PREFIX = "generation";
	private final String STORAGE_PATH = EnvironmentPaths.FBPREDICTION_RESOURCES_PATH + "/Storage";
	
	public void initialize()
	{
		File storage = new File(STORAGE_PATH);
		if(!storage.exists())
		{
			if(!storage.mkdirs())
				Logger.Write(Level.ERROR, "Could not create storage.");
		}
		
		Logger.Write("Storage created.");
	}
	
	/*
	 * Allocates storage for a bunch of training results.
	 * returns the identifier used to store and fetch results
	 * to this storage.
	 */
	public int allocateStorage()
	{
		int count = getStorageCount();
		int id = count + 1;
		
		File newStorage = new File(getStoragePath(id));
		newStorage.mkdir();
		
		Logger.Write("Allocated new storage: " + getStoragePath(id));
		
		return id;
	}
	
	/*
	 * Gets the newest storage created.
	 */
	public int getNewestStorage()
	{
		return getStorageCount();
	}
	
	/*
	 * Gets the amount of storages.
	 */
	public int getStorageCount()
	{
		File storage = new File(STORAGE_PATH);
		if(storage.exists())
		{
			String [] content = storage.list();
			return content.length;
		}
		return 0;
	}
	
	/*
	 * Returns the storage identifier for the #<count> storage.
	 */
	public int getStorageIdentifier(int count)
	{
		return count;
	}
	
	public boolean storeData(int storageIdentifier, TrainingResult[] results)
	{
		for(int i = 0; i < results.length; i++)
		{
			try
			{
				File result = new File(getStoragePath(storageIdentifier) + "/result:"+(i+1)+".nn");
				if(!result.exists())
					result.createNewFile();
				
				String resultStr = results[i].toPersistanceFormat();
				FileWriter fw = new FileWriter(result.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(resultStr);
				bw.close();
			}
			catch(Exception e)
			{
				Logger.Write(Level.ERROR, "Could not save results to: " + e.getMessage());
			}
		}
		Logger.Write("Saved training results to: " + getStoragePath(storageIdentifier));
		return false;
	}
	
	public TrainingResult[] fetchData(int storageIdentifier)
	{
		File directory = new File(getStoragePath(storageIdentifier));
		String[] resultFiles = directory.list();
		TrainingResult[] ret = new TrainingResult[resultFiles.length];
		for(int i = 0; i < resultFiles.length; i++)
		{
			File file = new File(getStoragePath(storageIdentifier) + "/" + resultFiles[i]);	         
	        String trainingResultStr;
			try {
				trainingResultStr = IOUtils.toString(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				Logger.Write(Level.ERROR, "Could not open file: " + e.getMessage());
				continue;
			} catch (IOException e) {
				Logger.Write(Level.ERROR, "Could not read from file: " + e.getMessage());
				continue;
			}
			ret[i] = TrainingResult.fromPersistanceFormat(trainingResultStr);
		}
		Logger.Write("Fetched training results from: " + getStoragePath(storageIdentifier));
		return ret;
	}
	
	private String getStoragePath(int storageIdentifier)
	{
		return STORAGE_PATH + "/" + STORAGE_PREFIX + ":" + storageIdentifier;
	}
}
