package navigation_emil;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import navigation_emil.AStarAlgorithm.PathLenTuple;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

public class GridRelaxation {
	
	private int worldWidth, worldHeight, boxWidth, boxHeight, gridSize;
	private GridBox[] grid;
	int nGates = 0;
	
	public GridRelaxation(StandardWorldModel fromModel, int gridSize) {
		worldWidth = (int) fromModel.getBounds().getWidth();
		worldHeight = (int) fromModel.getBounds().getHeight();
		this.gridSize = gridSize;
		boxWidth = worldWidth / gridSize;
		boxHeight = worldHeight / gridSize;
		
		BuildGrid(fromModel);
		ConnectGates(fromModel);
	}
	
	private void BuildGrid(StandardWorldModel model) {
		grid = new GridBox[gridSize*gridSize];
		for(int x = 0; x < gridSize; x++) {
			for(int y = 0; y < gridSize; y++) {
				grid[y*gridSize + x] = CreateBox(x, y, model);
			}
		}
	}
	
	public GridBox getBoxAtWorldCoord(int worldX, int worldY) {
		int gridX = worldX / boxWidth;
		int gridY = worldY / boxHeight;
		return grid[gridY*gridSize + gridX];
	}
	
	private void ConnectGates(StandardWorldModel model) {
		List<Gate> gates = getGates();
		Gate fromGate, toGate;
		int gSize = gates.size();
		Path path;
		for(int i = 0; i < gSize; i++) {
			fromGate = gates.get(i);
			for(int j = i+1; j < gSize; j++) {
				toGate = gates.get(j);
				if(fromGate.gridBox.GridX == toGate.gridBox.GridX &&
						fromGate.gridBox.GridY == toGate.gridBox.GridY) {
					// Inside same GridBox -> Internal gates
					path = new Path(fromGate, toGate, model, fromGate.gridBox.getRectangle());
					if(path.hasPath()) {
						fromGate.internalGates.add(path);
						toGate.internalGates.add(path.getReversedInstance());
					}
				} else {
					// Crossing borders, see if other gate is connected
					for(EntityID eId : fromGate.gateArea.getNeighbours()) {
						StandardEntity se = model.getEntity(eId);
						if(!(se instanceof Area)) continue;
						if(se.getID().getValue() == toGate.gateArea.getID().getValue()) {
							// It is connected! -> External gate
							path = new Path(fromGate, toGate, model);
							fromGate.externalGates.add(path);
							toGate.externalGates.add(path.getReversedInstance());
						}
					}
				}
			}
		}
	}
	
	private GridBox CreateBox(int x, int y, StandardWorldModel model) {
		GridBox box = new GridBox();
		box.X = boxWidth * x;
		box.Y = boxHeight * y;
		box.GridX = x;
		box.GridY = y;
		box.Width = boxWidth;
		box.Height = boxHeight;
		Rectangle boxRect = box.getRectangle();
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
					box.Gates.add(new Gate(area, box));
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
	
	public List<Area> getGateAreas() {
		List<Area> gates = new ArrayList<Area>();
		for(GridBox box : grid) {
			for(Gate gate : box.Gates) {
				gates.add(gate.gateArea);
			}
		}
		return gates;
	}
	
	public List<Gate> getGates() {
		List<Gate> gates = new ArrayList<Gate>();
		for(GridBox box : grid) {
			for(Gate gate : box.Gates) {
				gates.add(gate);
			}
		}
		return gates;
	}
	
	class GridBox {
		public int X, Y;
		public int GridX, GridY;
		public int Width, Height;
		public List<Gate> Gates = new ArrayList<Gate>();
		public Rectangle getRectangle() {
			return new Rectangle(X, Y, Width, Height);
		}
	}
	
	static class Gate {
		public Gate(Area area, GridBox gridBox) {
			gateArea = area;
			this.gridBox = gridBox;
		}
		public GridBox gridBox;
		public Area gateArea;
		public List<Path> externalGates = new ArrayList<Path>();
		public List<Path> internalGates = new ArrayList<Path>();
	}
	
	static class Path {
		protected PathLenTuple<Area> mPath;
		protected Gate fromGate, toGate;
		public Path(Gate from, Gate to, StandardWorldModel model) {
			this(from, to, model, null);
		}
		public Path(Gate from, Gate to, StandardWorldModel model, Rectangle limitArea) {
			SearchArea start = new SearchArea(from.gateArea, model, limitArea);
			SearchArea goal = new SearchArea(to.gateArea, model, limitArea);
			if(start != null && goal != null)
				mPath = AStarAlgorithm.PerformSearch(start, goal);
			fromGate = from;
			toGate = to;
		}
		protected Path() {
			
		}
		public boolean hasPath() {
			return (mPath != null && mPath.getPath().size() != 0);
		}
		public List<Area> getPath() {
			return mPath.getPath();
		}
		public double getLength() {
			return mPath.getLength();
		}
		public Gate getFromGate() {
			return fromGate;
		}
		public Gate getToGate() {
			return toGate;
		}
		public Path getReversedInstance() {
			Path rPath = new Path();
			rPath.fromGate = toGate;
			rPath.toGate = fromGate;
			rPath.mPath = new PathLenTuple<Area>();
			rPath.mPath.setLength(getLength());
			for(Area a : mPath.getPath()) {
				rPath.mPath.addPathElement(0, a);
			}
			return rPath;
		}
	}
	
	public static class SearchArea extends Searchable<Area> {

		StandardWorldModel model;
		Rectangle limitationArea = null;
		boolean avoidBlocks = false;
		
		public SearchArea(Area area, StandardWorldModel model, Rectangle limit) {
			super(area, area.getID().getValue(), 0, 0, null);
			this.model = model;
			this.limitationArea = limit;
			fillChildren();
		}
		
		public SearchArea(Area base, int uniqueId, double distance, double heuristic,
				SearchArea parentNode, StandardWorldModel model, Rectangle limit) {
			super(base, uniqueId, distance, heuristic, parentNode);
			this.model = model;
			this.limitationArea = limit;
			fillChildren();
		}

		@Override
		public Searchable<Area> createNode(Area baseObject,
				Searchable<Area> parentObject, Searchable<Area> goalObject) {
			double dx = baseObject.getX() - parentObject.getNodeObject().getX();
			double dy = baseObject.getY() - parentObject.getNodeObject().getY();
			double d = parentObject.getDistance() + Math.sqrt(dx*dx + dy*dy);
			dx = baseObject.getX() - goalObject.getNodeObject().getX();
			dy = baseObject.getY() - goalObject.getNodeObject().getY();
			double h = Math.sqrt(dx*dx + dy*dy);
			if(avoidBlocks) {
				double covered = getCoverPercentage(baseObject);
				h += h*Math.pow(covered, 3);
			}
			SearchArea node =  new SearchArea(baseObject, baseObject.getID().getValue(), d, h, (SearchArea)parentObject, model, limitationArea);
			node.setAvoidBlocks(avoidBlocks);
			node.fillChildren();
			return node;
		}
		
		public void setAvoidBlocks(boolean avoid) {
			avoidBlocks = avoid;
		}
		
		protected void fillChildren() {
			for(EntityID eId : getNodeObject().getNeighbours()) {
				StandardEntity se = model.getEntity(eId);
				if(se instanceof Area) {
					if(limitationArea == null || limitationArea.contains(((Area) se).getX(), ((Area) se).getY())) {
						addChild((Area) se);
					}
				}
			}
		}
		
		protected double getCoverPercentage(Area area) {
			double areaSum = 0;
			if(area == null) return areaSum;
			List<EntityID> bIds = area.getBlockades();
			if(bIds == null) return areaSum;
			for(EntityID id : bIds) {
				StandardEntity bE = model.getEntity(id);
				if(!(bE instanceof Blockade)) continue;
				Blockade blockade = (Blockade)bE;
				areaSum += MathUtils.areaOfShape(blockade.getShape());
			}
			return areaSum / MathUtils.areaOfShape(area.getShape());
		}
	}

	public static class SearchPath extends Searchable<Path> {

		StandardWorldModel model;
		Rectangle limitationArea = null;
		boolean avoidBlocks = false;
		Searchable<Path> goalPath = null;
		
		public SearchPath(Path path, StandardWorldModel model, Rectangle limit, Searchable<Path> goal) {
			super(path, path.toGate.gateArea.getID().getValue(), 0, 0, null);
			this.model = model;
			this.limitationArea = limit;
			goalPath = goal;
			fillChildren();
		}
		
		public SearchPath(Path path, StandardWorldModel model, Rectangle limit) {
			super(path, path.toGate.gateArea.getID().getValue(), 0, 0, null);
			this.model = model;
			this.limitationArea = limit;
			fillChildren();
		}
		
		public SearchPath(Path baseObject, int uniqueId, double distance,
				double heuristic, Searchable<Path> parentNode, StandardWorldModel model, Rectangle limit) {
			super(baseObject, uniqueId, distance, heuristic, parentNode);
			this.model = model;
			this.limitationArea = limit;
			fillChildren();
		}

		@Override
		public Searchable<Path> createNode(Path baseObject,
				Searchable<Path> parentObject, Searchable<Path> goalObject) {

			Area from = baseObject.toGate.gateArea;
			Area to = goalObject.getNodeObject().toGate.gateArea;
			double h = MathUtils.distance(from.getX(), from.getY(), to.getX(), to.getY());
			SearchPath node = new SearchPath(baseObject, baseObject.toGate.gateArea.getID().getValue(),
					parentObject.getDistance() + baseObject.getLength(), h, parentObject, model, limitationArea);
			
			node.goalPath = goalObject;
			node.fillChildren();
			return node;
		}
		
		public static SearchPath CreateStartPath(Gate startGate, StandardWorldModel model, Rectangle initLimit, Searchable<Path> goal) {
			for(Gate gate : startGate.gridBox.Gates) {
				Path tPath = new Path(startGate, gate, model, initLimit);
				if(tPath.hasPath()) {
					startGate.internalGates.add(tPath);
				}
			}
			Path dummy = new Path(startGate, startGate, model, initLimit);
			return new SearchPath(dummy, model, null, goal);
		}
		
		public static SearchPath CreateGoalPath(Gate goalGate, StandardWorldModel model, Rectangle limit) {
			Path dummy = new Path(goalGate, goalGate, model, limit);
			return new SearchPath(dummy, model, null);
		}
		
		protected void fillChildren() {
			if(goalPath != null &&
					getNodeObject().toGate.gridBox.GridX == goalPath.getNodeObject().toGate.gridBox.GridX &&
					getNodeObject().toGate.gridBox.GridY == goalPath.getNodeObject().toGate.gridBox.GridY) {
				goalPath.setParent(this);
				Path gPath = new Path(getNodeObject().toGate, goalPath.getNodeObject().toGate, model, limitationArea);
				addChild(gPath);
			}
			
			List<Path> connected = getNodeObject().toGate.internalGates;
			connected.addAll(getNodeObject().toGate.externalGates);
			for(Path p : connected) {
				if(limitationArea == null || limitationArea.contains(p.toGate.gateArea.getX(), p.toGate.gateArea.getY())) {
					addChild(p);
				}
			}
		}
	}
}
