package sample;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class GridRelaxation extends StandardWorldModel {
	
	private int worldWidth, worldHeight, boxWidth, boxHeight, gridSize;
	private GridBox[] grid;
	int nGates = 0;
	
	public GridRelaxation(StandardWorldModel fromModel, int gridSize) {
		worldWidth = (int) fromModel.getBounds().getWidth();
		worldHeight = (int) fromModel.getBounds().getHeight();
		this.gridSize = gridSize;
		boxWidth = worldWidth / gridSize;
		boxHeight = worldHeight / gridSize;
		grid = new GridBox[gridSize*gridSize];
		for(int x = 0; x < gridSize; x++) {
			for(int y = 0; y < gridSize; y++) {
				grid[y*gridSize + x] = CreateBox(x, y, fromModel);
			}
		}
		System.out.println("Grid initialized. Total gates: " + nGates);
	}
	
	private GridBox CreateBox(int x, int y, StandardWorldModel model) {
		GridBox box = new GridBox();
		box.X = boxWidth * x;
		box.Y = boxHeight * y;
		box.GridX = x;
		box.GridY = y;
		box.Width = boxWidth;
		box.Height = boxHeight;
		Rectangle boxRect = new Rectangle(box.X, box.Y,
				box.X + box.Width, box.Y + box.Height);
		StandardEntity entity;
		Area area;
		Iterator<StandardEntity> entityIterator = model.getObjectsInRectangle(box.X, box.Y,
				box.X + box.Width, box.Y + box.Height).iterator();
		while(entityIterator.hasNext()) {
			entity = entityIterator.next();
			if(entity instanceof Area) {
				area = (Area) entity;
				if(!boxRect.contains(area.getX(), area.getY())) continue;
				if(isEdgeArea(area, boxRect, model)) {
					box.Gates.add(new Gate(area));
					nGates++;
				}
			}
		}
		return box;
	}
	
	private boolean isEdgeArea(Area area, Rectangle boxRect, StandardWorldModel model) {
		StandardEntity childEntity;
		Area childArea;
		for(EntityID childId : area.getNeighbours()) {
			if(childId.getValue() == area.getID().getValue()) {
				continue;
			}
			childEntity = model.getEntity(childId);
			if(!(childEntity instanceof Area)) continue;
			childArea = (Area) childEntity;
			if(!boxRect.contains(childArea.getX(), childArea.getY())) return true;
		}
		return false;
	}
	
	public List<Area> getGates() {
		List<Area> gates = new ArrayList<Area>();
		for(GridBox box : grid) {
			for(Gate gate : box.Gates) {
				gates.add(gate.GateArea);
			}
		}
		return gates;
	}
	
	class GridBox {
		public int X, Y;
		public int GridX, GridY;
		public int Width, Height;
		public List<Gate> Gates = new ArrayList<Gate>();
	}
	
	class Gate {
		public Gate(Area area) {
			GateArea = area;
		}
		public Area GateArea;
		public Pair<Gate, List<Area>> externalGate = null;
		public List<Pair<Gate, List<Area>>> internalGates = new ArrayList<Pair<Gate, List<Area>>>();
	}
}
