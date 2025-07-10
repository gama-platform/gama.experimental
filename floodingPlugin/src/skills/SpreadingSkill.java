package skills;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.shape.IShape;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.core.util.matrix.GamaField;
import gama.core.util.matrix.IField;
import gama.core.util.matrix.IMatrix;
import gama.gaml.operators.spatial.SpatialProperties;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;
import gama.gaml.types.*;

@vars({ @variable(name = "flow_threshold", type = IType.FLOAT, init = "0.01", doc = @doc("Minimum water depth required for flow (in meters)")),
		@variable(name = "rising_rate", type = IType.FLOAT, init = "0.3", doc = @doc("Rate at which water rises (in meters per step)")),
		@variable(name = "min_flow_diff", type = IType.FLOAT, init = "0.001", doc = @doc("Minimum elevation difference needed for water flow")),
		@variable(name = "equalization_threshold", type = IType.FLOAT, init = "0.1", doc = @doc("Level difference threshold for water equalization")),
		@variable(name = "simulation_active", type = IType.BOOL, init = "false", doc = @doc("Whether the spreading simulation is currently active")),
		@variable(name = "simulation_step", type = IType.INT, init = "0", doc = @doc("Current simulation step counter")),
		@variable(name = "grid_width", type = IType.INT, init = "0", doc = @doc("Width of the internal grid")),
		@variable(name = "grid_height", type = IType.INT, init = "0", doc = @doc("Height of the internal grid")),
		@variable(name = "water_field", type = IType.MATRIX, doc = @doc("Field representing water elevations for visualization"))
})
@skill(name = "spreading", concept = { "spreading", "simulation", "water",
		"flood" }, doc = @doc("A skill for managing spreading simulations with optimized water flow mechanics"))
public class SpreadingSkill extends Skill {
	// === SKILL VARIABLES ===
	public static final String FLOW_THRESHOLD = "flow_threshold";
	public static final String RISING_RATE = "rising_rate";
	public static final String MIN_FLOW_DIFF = "min_flow_diff";
	public static final String EQUALIZATION_THRESHOLD = "equalization_threshold";
	public static final String SIMULATION_ACTIVE = "simulation_active";
	public static final String WATER_FIELD = "water_field";
	public static final String SIMULATION_STEP = "simulation_step";
	public static final String GRID_WIDTH = "grid_width";
	public static final String GRID_HEIGHT = "grid_height";
	
	// === INTERNAL GRID CELL CLASS ===
	public static class GridCell {
		public int x, y;
		public boolean isWater;
		public double waterElevation;
		public double terrainElevation;
		public boolean isEdgeCell;
		public List<GridCell> neighbors;
		public IShape shape;
		
		public GridCell(int x, int y, double terrainElev, IShape cellShape) {
			this.x = x;
			this.y = y;
			this.isWater = false;
			this.waterElevation = 0.0;
			this.terrainElevation = terrainElev;
			this.isEdgeCell = false;
			this.neighbors = new ArrayList<>();
			this.shape = cellShape;
		}
	}
	
	// === INTERNAL DATA STRUCTURES ===
	private GridCell[][] internalGrid;
	private List<GridCell> activeWaterCells;
	private HashSet<GridCell> edgeWaterCells;
	private Map<IAgent, SpreadingSkill> skillInstances = new ConcurrentHashMap<>();
	
	// === CONSTRUCTOR ===
	public SpreadingSkill() {
		super();
		this.activeWaterCells = new ArrayList<>();
		this.edgeWaterCells = new HashSet<>();
	}
	
	// === HELPER METHODS ===
	private Double getFloatAttribute(IAgent agent, String attr) {
		return (Double) agent.getAttribute(attr);
	}
	
	private Integer getIntAttribute(IAgent agent, String attr) {
		return (Integer) agent.getAttribute(attr);
	}
	
	private Boolean getBoolAttribute(IAgent agent, String attr) {
		return (Boolean) agent.getAttribute(attr);
	}
	
	private void setIntAttribute(IAgent agent, String attr, int value) {
		agent.setAttribute(attr, value);
	}
	
	private void setBoolAttribute(IAgent agent, String attr, boolean value) {
		agent.setAttribute(attr, value);
	}
	
	private void setFloatAttribute(IAgent agent, String attr, double value) {
		agent.setAttribute(attr, value);
	}
	
	// === GRID INITIALIZATION ===
	@action(name = "initialize_spreading_grid", args = {
			@arg(name = "dem_field", type = IType.MATRIX, doc = @doc("Digital elevation model field")),
			@arg(name = "water_geometries", type = IType.LIST, doc = @doc("List of water polygon geometries")),
			@arg(name = "initial_water_depth", type = IType.FLOAT, optional = true, doc = @doc("Initial water depth (default: 1.5m)")),
			@arg(name = "flow_threshold", type = IType.FLOAT, optional = true, doc = @doc("Flow threshold parameter")),
			@arg(name = "rising_rate", type = IType.FLOAT, optional = true, doc = @doc("Rising rate parameter")),
			@arg(name = "min_flow_diff", type = IType.FLOAT, optional = true, doc = @doc("Minimum flow difference parameter")),
			@arg(name = "equalization_threshold", type = IType.FLOAT, optional = true, doc = @doc("Equalization threshold parameter")) }, doc = @doc("Initializes the internal spreading grid with DEM and water data"))
	public Boolean initializeSpreadingGrid(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		// Get parameters
		final IField demField = (IField) scope.getArg("dem_field", IType.FIELD);
		final IList<IShape> waterGeometries = scope.getListArg("water_geometries");
		final Double initialWaterDepth = scope.hasArg("initial_water_depth")
				? scope.getFloatArg("initial_water_depth")
				: 1.5;
		
		// Set parameters if provided
		if (scope.hasArg("flow_threshold")) {
			agent.setAttribute(FLOW_THRESHOLD, scope.getFloatArg("flow_threshold"));
		}
		if (scope.hasArg("rising_rate")) {
			agent.setAttribute(RISING_RATE, scope.getFloatArg("rising_rate"));
		}
		if (scope.hasArg("min_flow_diff")) {
			agent.setAttribute(MIN_FLOW_DIFF, scope.getFloatArg("min_flow_diff"));
		}
		if (scope.hasArg("equalization_threshold")) {
			agent.setAttribute(EQUALIZATION_THRESHOLD, scope.getFloatArg("equalization_threshold"));
		}
		
		// Get grid dimensions
		int gridWidth = demField.getCols(scope);
		int gridHeight = demField.getRows(scope);
		setIntAttribute(agent, GRID_WIDTH, gridWidth);
		setIntAttribute(agent, GRID_HEIGHT, gridHeight);
		
		// Initialize internal grid
		internalGrid = new GridCell[gridWidth][gridHeight];
		activeWaterCells.clear();
		edgeWaterCells.clear();
		
		// Create water field properly initialized to 0
		IField waterField = GamaFieldType.withObject(scope, 0.0, gridWidth, gridHeight, Types.FLOAT);
		
		// Sequential initialization of field (GAMA fields are not thread-safe)
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				waterField.set(scope, i, j, 0.0);
			}
		}
		
		// Create grid cells and determine water areas
		int waterCellCount = 0;
		double totalWaterTerrainElev = 0.0;
		int waterCellsFound = 0;
		
		// Sequential cell creation (GAMA shape operations are not thread-safe)
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				// Get terrain elevation from DEM
				double terrainElev = ((Number) demField.get(scope, i, j)).doubleValue();
				
				// Create cell shape (using DEM's cell shape)
				IShape cellShape = demField.getCellShapeAt(scope, i, j);
				
				// Create grid cell
				GridCell cell = new GridCell(i, j, terrainElev, cellShape);
				internalGrid[i][j] = cell;
				
				// Check if cell intersects with water geometries
				boolean isWaterCell = false;
				if (cellShape != null && waterGeometries != null && !waterGeometries.isEmpty()) {
					for (IShape waterGeom : waterGeometries) {
						if (waterGeom != null) {
							try {
								if (SpatialProperties.overlaps(scope, cellShape, waterGeom)) {
									isWaterCell = true;
									totalWaterTerrainElev += terrainElev;
									waterCellsFound++;
									break;
								}
							} catch (Exception e) {
								if (waterGeom.covers(cellShape.getLocation())) {
									isWaterCell = true;
									totalWaterTerrainElev += terrainElev;
									waterCellsFound++;
									break;
								}
							}
						}
					}
				}
				
				if (isWaterCell) {
					cell.isWater = true;
					activeWaterCells.add(cell);
					waterCellCount++;
				}
			}
		}
		
		// Calculate uniform initial water level
		double uniformWaterLevel = 0.0;
		if (waterCellsFound > 0) {
			double avgWaterTerrainElev = totalWaterTerrainElev / waterCellsFound;
			uniformWaterLevel = avgWaterTerrainElev + initialWaterDepth;
		}
		
		// Set uniform water elevation for all water cells
		final double finalWaterLevel = uniformWaterLevel;
		for (GridCell cell : activeWaterCells) {
			cell.waterElevation = finalWaterLevel;
			waterField.set(scope, cell.x, cell.y, cell.waterElevation);
		}
		
		// Build neighbor relationships (can be parallelized safely)
		buildNeighborRelationships(gridWidth, gridHeight);
		
		// Identify initial edge cells
		identifyEdgeCells();
		
		// Store the water field
		agent.setAttribute(WATER_FIELD, waterField);
		
		return true;
	}
	
	private void buildNeighborRelationships(int width, int height) {
		// 8-connected neighborhood (matching GAMA grid default)
		final int[] dx = { -1, -1, -1, 0, 0, 1, 1, 1 };
		final int[] dy = { -1, 0, 1, -1, 1, -1, 0, 1 };
		
		// PARALLEL: Each cell builds its neighbors independently (safe because only reading grid)
		IntStream.range(0, width).parallel().forEach(i -> {
			for (int j = 0; j < height; j++) {
				GridCell cell = internalGrid[i][j];
				
				for (int k = 0; k < 8; k++) {
					int ni = i + dx[k];
					int nj = j + dy[k];
					
					if (ni >= 0 && ni < width && nj >= 0 && nj < height) {
						cell.neighbors.add(internalGrid[ni][nj]);
					}
				}
			}
		});
	}
	
	private void identifyEdgeCells() {
		edgeWaterCells.clear();
		
		for (GridCell cell : activeWaterCells) {
			boolean isEdge = false;
			for (GridCell neighbor : cell.neighbors) {
				if (!neighbor.isWater) {
					isEdge = true;
					break;
				}
			}
			if (isEdge) {
				cell.isEdgeCell = true;
				edgeWaterCells.add(cell);
			}
		}
	}
	
	// === WATER SMOOTHING METHOD ===
	private void smoothWaterSurface(IScope scope, IField waterField, double smoothingFactor) {
		// Create temporary storage for smoothed values
		int width = getIntAttribute(getCurrentAgent(scope), GRID_WIDTH);
		int height = getIntAttribute(getCurrentAgent(scope), GRID_HEIGHT);
		double[][] smoothedValues = new double[width][height];
		
		// PARALLEL: Calculate smoothed values (only reading, not writing)
		activeWaterCells.parallelStream().forEach(cell -> {
			double sum = cell.waterElevation * (1.0 - smoothingFactor);
			double weight = 1.0 - smoothingFactor;
			int waterNeighborCount = 0;
			
			// Average with water neighbors
			for (GridCell neighbor : cell.neighbors) {
				if (neighbor.isWater) {
					sum += neighbor.waterElevation * (smoothingFactor / 8.0);
					weight += smoothingFactor / 8.0;
					waterNeighborCount++;
				}
			}
			
			// Only smooth if there are water neighbors
			if (waterNeighborCount > 0) {
				smoothedValues[cell.x][cell.y] = sum / weight;
			} else {
				smoothedValues[cell.x][cell.y] = cell.waterElevation;
			}
		});
		
		// Sequential update of cells and field (GAMA field operations must be sequential)
		for (GridCell cell : activeWaterCells) {
			cell.waterElevation = smoothedValues[cell.x][cell.y];
			waterField.set(scope, cell.x, cell.y, cell.waterElevation);
		}
	}
	
	// === SIMULATION ACTIONS ===
	@action(name = "simulate_spreading_step", doc = @doc("Executes one step of the spreading simulation"))
	public Boolean simulateSpreadingStep(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		if (!getBoolAttribute(agent, SIMULATION_ACTIVE)) {
			return false;
		}
		
		// Increment simulation step
		int currentStep = getIntAttribute(agent, SIMULATION_STEP) + 1;
		setIntAttribute(agent, SIMULATION_STEP, currentStep);
		
		// Get parameters
		double flowThreshold = getFloatAttribute(agent, FLOW_THRESHOLD);
		double risingRate = getFloatAttribute(agent, RISING_RATE);
		double minFlowDiff = getFloatAttribute(agent, MIN_FLOW_DIFF);
		double equalizationThreshold = getFloatAttribute(agent, EQUALIZATION_THRESHOLD);
		
		// Get water field for immediate updates
		IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		
		// 1. SPREAD WATER - only from current edge cells
		List<GridCell> newWaterCells = new ArrayList<>();
		HashSet<GridCell> affectedNeighbors = new HashSet<>();
		
		// PARALLEL: Find minimum spreading level (safe - only reading)
		double minSpreadingLevel = edgeWaterCells.parallelStream()
			.filter(cell -> cell.waterElevation > cell.terrainElevation + flowThreshold)
			.mapToDouble(cell -> cell.waterElevation)
			.min()
			.orElse(Double.MAX_VALUE);
		
		// Sequential spreading (modifies neighbor states)
		for (GridCell edgeCell : edgeWaterCells) {
			if (edgeCell.waterElevation > edgeCell.terrainElevation + flowThreshold) {
				for (GridCell neighbor : edgeCell.neighbors) {
					if (!neighbor.isWater &&
						(edgeCell.waterElevation - neighbor.terrainElevation) > minFlowDiff) {
						
						neighbor.isWater = true;
						neighbor.waterElevation = Math.max(
							neighbor.terrainElevation + flowThreshold,
							minSpreadingLevel - 0.01
						);
						newWaterCells.add(neighbor);
						
						if (waterField != null) {
							waterField.set(scope, neighbor.x, neighbor.y, neighbor.waterElevation);
						}
						
						for (GridCell neighborOfNeighbor : neighbor.neighbors) {
							if (neighborOfNeighbor.isWater && !affectedNeighbors.contains(neighborOfNeighbor)) {
								affectedNeighbors.add(neighborOfNeighbor);
							}
						}
					}
				}
			}
		}
		
		// 2. Update active water cells list and edge list incrementally
		if (!newWaterCells.isEmpty()) {
			activeWaterCells.addAll(newWaterCells);
			updateEdgeCells(newWaterCells, affectedNeighbors);
			smoothWaterSurface(scope, waterField, 0.5);
		}
		
		// 3. RISE WATER - REALISTIC PHYSICS: Equalize levels first, then rise together
		if (newWaterCells.isEmpty()) {
			// PARALLEL: Find water level range (safe - only reading)
			double minWaterLevel = activeWaterCells.parallelStream()
				.mapToDouble(cell -> cell.waterElevation)
				.min()
				.orElse(0.0);
			
			double maxWaterLevel = activeWaterCells.parallelStream()
				.mapToDouble(cell -> cell.waterElevation)
				.max()
				.orElse(0.0);
			
			double levelDifference = maxWaterLevel - minWaterLevel;
			
			if (levelDifference > equalizationThreshold) {
				final double targetLevel = maxWaterLevel;
				
				// PARALLEL: Calculate new elevations (safe - only local computation)
				Map<GridCell, Double> newElevations = new ConcurrentHashMap<>();
				activeWaterCells.parallelStream().forEach(cell -> {
					if (cell.waterElevation < targetLevel) {
						newElevations.put(cell, Math.min(cell.waterElevation + risingRate, targetLevel));
					}
				});
				
				// Sequential update of cells and field
				for (Map.Entry<GridCell, Double> entry : newElevations.entrySet()) {
					GridCell cell = entry.getKey();
					cell.waterElevation = entry.getValue();
					if (waterField != null) {
						waterField.set(scope, cell.x, cell.y, cell.waterElevation);
					}
				}
				
				smoothWaterSurface(scope, waterField, 0.3);
				
			} else {
				// Uniform rise - update all cells
				final double uniformRise = risingRate;
				
				// Sequential update (GAMA field operations must be sequential)
				for (GridCell cell : activeWaterCells) {
					cell.waterElevation += uniformRise;
					if (waterField != null) {
						waterField.set(scope, cell.x, cell.y, cell.waterElevation);
					}
				}
			}
		}
		
		return true;
	}
	
	private void updateEdgeCells(List<GridCell> newWaterCells, HashSet<GridCell> affectedNeighbors) {
		// Sequential processing for consistency
		for (GridCell cell : newWaterCells) {
			boolean isEdge = false;
			for (GridCell neighbor : cell.neighbors) {
				if (!neighbor.isWater) {
					isEdge = true;
					break;
				}
			}
			if (isEdge && !cell.isEdgeCell) {
				cell.isEdgeCell = true;
				edgeWaterCells.add(cell);
			}
		}
		
		for (GridCell cell : affectedNeighbors) {
			if (cell.isWater && cell.isEdgeCell) {
				boolean stillEdge = false;
				for (GridCell neighbor : cell.neighbors) {
					if (!neighbor.isWater) {
						stillEdge = true;
						break;
					}
				}
				if (!stillEdge) {
					cell.isEdgeCell = false;
					edgeWaterCells.remove(cell);
				}
			}
		}
	}
	
	// === SIMULATION CONTROL ACTIONS ===
	@action(
		name = "start_spreading_simulation",
		doc = @doc("Starts the spreading simulation")
	)
	public Boolean startSpreadingSimulation(final IScope scope){
		final IAgent agent = getCurrentAgent(scope);
		setBoolAttribute(agent, SIMULATION_ACTIVE, true);
		return true;
	}
	
	@action(name = "stop_spreading_simulation", doc = @doc("Stops the spreading simulation"))
	public Boolean stopSpreadingSimulation(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		setBoolAttribute(agent, SIMULATION_ACTIVE, false);
		return true;
	}
	
	@action(name = "reset_spreading_simulation", args = {
			@arg(name = "water_geometries", type = IType.LIST, doc = @doc("List of water polygon geometries")),
			@arg(name = "initial_water_depth", type = IType.FLOAT, optional = true, doc = @doc("Initial water depth (default: 1.5m)")) }, doc = @doc("Resets the spreading simulation to initial state"))
	public Boolean resetSpreadingSimulation(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		setBoolAttribute(agent, SIMULATION_ACTIVE, false);
		setIntAttribute(agent, SIMULATION_STEP, 0);
		
		final IList<IShape> waterGeometries = scope.getListArg("water_geometries");
		final Double initialWaterDepth = scope.hasArg("initial_water_depth") ? scope.getFloatArg("initial_water_depth")
				: 1.5;
		
		// Clear lists
		activeWaterCells.clear();
		edgeWaterCells.clear();
		
		int width = getIntAttribute(agent, GRID_WIDTH);
		int height = getIntAttribute(agent, GRID_HEIGHT);
		
		// Get water field
		IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		
		// Sequential field clearing (GAMA fields are not thread-safe)
		if (waterField != null) {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					waterField.set(scope, i, j, 0.0);
				}
			}
		}
		
		// Reset grid to initial state
		int waterCellCount = 0;
		double totalWaterTerrainElev = 0.0;
		
		// Sequential processing (GAMA shape operations are not thread-safe)
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				GridCell cell = internalGrid[i][j];
				
				boolean isWaterCell = false;
				if (cell.shape != null && waterGeometries != null && !waterGeometries.isEmpty()) {
					for (IShape waterGeom : waterGeometries) {
						if (waterGeom != null) {
							try {
								if (SpatialProperties.overlaps(scope, cell.shape, waterGeom)) {
									isWaterCell = true;
									break;
								}
							} catch (Exception e) {
								if (waterGeom.covers(cell.shape.getLocation())) {
									isWaterCell = true;
									break;
								}
							}
						}
					}
				}
				
				if (isWaterCell) {
					cell.isWater = true;
					cell.isEdgeCell = false;
					activeWaterCells.add(cell);
					totalWaterTerrainElev += cell.terrainElevation;
					waterCellCount++;
				} else {
					cell.isWater = false;
					cell.waterElevation = 0.0;
					cell.isEdgeCell = false;
				}
			}
		}
		
		// Calculate uniform water level
		double uniformWaterLevel = 0.0;
		if (waterCellCount > 0) {
			double avgWaterTerrainElev = totalWaterTerrainElev / waterCellCount;
			uniformWaterLevel = avgWaterTerrainElev + initialWaterDepth;
		}
		
		// Set uniform water elevation
		for (GridCell cell : activeWaterCells) {
			cell.waterElevation = uniformWaterLevel;
			if (waterField != null) {
				waterField.set(scope, cell.x, cell.y, cell.waterElevation);
			}
		}
		
		// Rebuild initial edge list
		identifyEdgeCells();
		
		return true;
	}
	
	// === STATUS QUERY ACTIONS ===
	@action(name = "get_active_water_count", doc = @doc("Returns the number of active water cells"))
	public Integer getActiveWaterCount(final IScope scope) {
		return activeWaterCells.size();
	}
	
	@action(name = "get_edge_cell_count", doc = @doc("Returns the number of edge cells"))
	public Integer getEdgeCellCount(final IScope scope) {
		return edgeWaterCells.size();
	}
	
	@action(name = "is_simulation_active", doc = @doc("Returns whether the simulation is currently active"))
	public Boolean isSimulationActive(final IScope scope) {
		return getBoolAttribute(getCurrentAgent(scope), SIMULATION_ACTIVE);
	}
	
	@action(name = "get_current_step", doc = @doc("Returns the current simulation step"))
	public Integer getCurrentStep(final IScope scope) {
		return getIntAttribute(getCurrentAgent(scope), SIMULATION_STEP);
	}
}