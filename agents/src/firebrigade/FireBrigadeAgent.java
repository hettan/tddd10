package firebrigade;

import java.util.List;

import rescuecore2.worldmodel.EntityID;

public class FireBrigadeAgent {
	
	EntityID ID;
	double waterAmount;
	boolean busy;
	
	
	public FireBrigadeAgent(EntityID ID, int waterAmount, boolean busy){
		this.ID = ID;
		this.waterAmount = waterAmount;
		this.busy = busy;
	}

	
	public EntityID getID() {
		return ID;
	}
	public void setID(EntityID iD) {
		this.ID = iD;
	}
	public double getWaterAmount() {
		return waterAmount;
	}
	public void setWaterAmount(double waterAmount) {
		this.waterAmount = waterAmount;
	}
	public boolean isBusy() {
		return busy;
	}
	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	
	public String toString() {
	        return "Professor id: " + getID() + " waterAmount: " + getWaterAmount() + " isBusy: " + isBusy();
	}
}
