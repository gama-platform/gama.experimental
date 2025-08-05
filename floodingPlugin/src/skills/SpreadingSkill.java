package skills;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
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
import gama.core.metamodel.topology.ITopology;
import gama.gaml.types.GamaFieldType;
import gama.gaml.types.Types;
import gama.core.util.GamaListFactory;
import gama.core.common.interfaces.IKeyword;
import gama.gaml.operators.Maths;
import gama.core.metamodel.shape.GamaPoint;

@vars({ @variable(name = "flow_threshold", type = IType.FLOAT, init = "0.01", doc = @doc("Minimum water depth required for flow (in meters)")),
		@variable(name = "rising_rate", type = IType.FLOAT, init = "0.3", doc = @doc("Rate at which water rises (in meters per step)")),
		@variable(name = "min_flow_diff", type = IType.FLOAT, init = "0.001", doc = @doc("Minimum elevation difference needed for water flow")),
		@variable(name = "equalization_threshold", type = IType.FLOAT, init = "0.1", doc = @doc("Level difference threshold for water equalization")),
		@variable(name = "simulation_active", type = IType.BOOL, init = "false", doc = @doc("Whether the spreading simulation is currently active")),
		@variable(name = "simulation_step", type = IType.INT, init = "0", doc = @doc("Current simulation step counter")),
		@variable(name = "grid_width", type = IType.INT, init = "0", doc = @doc("Width of the internal grid")),
		@variable(name = "grid_height", type = IType.INT, init = "0", doc = @doc("Height of the internal grid")),
		@variable(name = "water_field", type = IType.MATRIX, doc = @doc("Field representing water elevations for visualization")),
		// RAIN VARIABLES
		@variable(name = "rain_active", type = IType.BOOL, init = "false", doc = @doc("Whether rain is currently active")),
		@variable(name = "rain_rate", type = IType.FLOAT, init = "0.0", doc = @doc("Rate of rain affecting water rising and spreading (in meters per step)")),
		@variable(name = "rain_intensity", type = IType.FLOAT, init = "1.0", doc = @doc("Multiplier for rain effects on spreading (1.0 = normal, >1.0 = more aggressive spreading)")),
		// DYKE VARIABLES
		@variable(name = "dyke_field", type = IType.MATRIX, doc = @doc("Field representing dyke elevations for visualization")),
		@variable(name = "dyke_building_mode", type = IType.BOOL, init = "false", doc = @doc("Whether dyke building mode is active")),
		@variable(name = "dyke_removal_mode", type = IType.BOOL, init = "false", doc = @doc("Whether dyke removal mode is active")),
		@variable(name = "dyke_height", type = IType.FLOAT, init = "5.0", doc = @doc("Height of dykes in meters")),
		@variable(name = "dyke_destruction_time", type = IType.FLOAT, init = "10.0", doc = @doc("Time in cycles before dyke cell under water attack is destroyed"))
})
@skill(name = "spreading", concept = { "spreading", "simulation", "water",
		"flood", "rain", "dykes" }, doc = @doc("A skill for managing spreading simulations with optimized water flow mechanics, rain system, dyke building and removal"))
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
	// RAIN CONSTANTS
	public static final String RAIN_ACTIVE = "rain_active";
	public static final String RAIN_RATE = "rain_rate";
	public static final String RAIN_INTENSITY = "rain_intensity";
	// DYKE CONSTANTS
	public static final String DYKE_FIELD = "dyke_field";
	public static final String DYKE_BUILDING_MODE = "dyke_building_mode";
	public static final String DYKE_REMOVAL_MODE = "dyke_removal_mode";
	public static final String DYKE_HEIGHT = "dyke_height";
	public static final String DYKE_DESTRUCTION_TIME = "dyke_destruction_time";
	
	// === INTERNAL GRID CELL CLASS ===
	public static class GridCell {
		public int x, y;
		public boolean isWater;
		public double waterElevation;
		public double terrainElevation;
		public double originalTerrainElevation; // Store original elevation for dyke management
		public boolean isEdgeCell;
		public List<GridCell> neighbors;
		public IShape shape;
		private Map<String, Object> attributes; // For temporary data storage
		
		public GridCell(int x, int y, double terrainElev, IShape cellShape) {
			this.x = x;
			this.y = y;
			this.isWater = false;
			this.waterElevation = 0.0;
			this.terrainElevation = terrainElev;
			this.originalTerrainElevation = terrainElev;
			this.isEdgeCell = false;
			this.neighbors = new ArrayList<>();
			this.shape = cellShape;
			this.attributes = new HashMap<>();
		}
		
		public void setAttribute(String key, Object value) {
			attributes.put(key, value);
		}
		
		public Object getAttribute(String key) {
			return attributes.get(key);
		}
	}
	
	// === DYKE CELL CLASS ===
	public static class DykeCell {
		public GridCell gridCell;
		public double creationTime;
		public double waterAttackStartTime;  // Track when water attack started
		public boolean isUnderWaterAttack;   // Track if adjacent to water
		public boolean isDestroyed;
		
		public DykeCell(GridCell cell, double time) {
			this.gridCell = cell;
			this.creationTime = time;
			this.waterAttackStartTime = -1;
			this.isUnderWaterAttack = false;
			this.isDestroyed = false;
		}
	}
	
	// === INTERNAL DATA STRUCTURES ===
	private GridCell[][] internalGrid;
	private List<GridCell> activeWaterCells;
	private HashSet<GridCell> edgeWaterCells;
	private List<DykeCell> activeDykes;
	private HashSet<GridCell> dykeGridCells;
	private Map<IAgent, SpreadingSkill> skillInstances = new ConcurrentHashMap<>();
	
	// === CONSTRUCTOR ===
	public SpreadingSkill() {
		super();
		this.activeWaterCells = new ArrayList<>();
		this.edgeWaterCells = new HashSet<>();
		this.activeDykes = new ArrayList<>();
		this.dykeGridCells = new HashSet<>();
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
	
	// === ORIGINAL GRID INITIALIZATION (FOR BACKWARDS COMPATIBILITY) ===
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
		activeDykes.clear();
		dykeGridCells.clear();
		
		// Create water field with same dimensions as DEM
		IField waterField = GamaFieldType.withObject(scope, 0.0, gridWidth, gridHeight, Types.FLOAT);
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				waterField.set(scope, i, j, 0.0);
			}
		}
		
		// Create dyke field with same dimensions
		IField dykeField = GamaFieldType.withObject(scope, 0.0, gridWidth, gridHeight, Types.FLOAT);
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				dykeField.set(scope, i, j, 0.0);
			}
		}
		
		// Initialize grid (same as dyke version)
		initializeGridCells(scope, demField, waterGeometries, initialWaterDepth, waterField);
		
		// Store the fields
		agent.setAttribute(WATER_FIELD, waterField);
		agent.setAttribute(DYKE_FIELD, dykeField);
		
		System.out.println("INIT: Grid initialized with " + activeWaterCells.size() + " water cells");
		
		return true;
	}
	
	// === NEW GRID INITIALIZATION WITH PRE-CREATED DYKE FIELD ===
	@action(name = "initialize_spreading_grid_with_dyke_field", args = {
			@arg(name = "dem_field", type = IType.MATRIX, doc = @doc("Digital elevation model field")),
			@arg(name = "dyke_field", type = IType.MATRIX, doc = @doc("Pre-created dyke field with correct coordinate system")),
			@arg(name = "water_geometries", type = IType.LIST, doc = @doc("List of water polygon geometries")),
			@arg(name = "initial_water_depth", type = IType.FLOAT, optional = true, doc = @doc("Initial water depth (default: 1.5m)")),
			@arg(name = "flow_threshold", type = IType.FLOAT, optional = true, doc = @doc("Flow threshold parameter")),
			@arg(name = "rising_rate", type = IType.FLOAT, optional = true, doc = @doc("Rising rate parameter")),
			@arg(name = "min_flow_diff", type = IType.FLOAT, optional = true, doc = @doc("Minimum flow difference parameter")),
			@arg(name = "equalization_threshold", type = IType.FLOAT, optional = true, doc = @doc("Equalization threshold parameter")) }, doc = @doc("Initializes the spreading grid with DEM and pre-created dyke field"))
	public Boolean initializeSpreadingGridWithDykeField(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		// Get parameters
		final IField demField = (IField) scope.getArg("dem_field", IType.FIELD);
		final IField dykeField = (IField) scope.getArg("dyke_field", IType.FIELD);
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
		activeDykes.clear();
		dykeGridCells.clear();
		
		// Create water field with same dimensions as DEM
		IField waterField = GamaFieldType.withObject(scope, 0.0, gridWidth, gridHeight, Types.FLOAT);
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				waterField.set(scope, i, j, 0.0);
			}
		}
		
		// Use the pre-created dyke field (already has correct coordinate system)
		// Just ensure it's initialized to zero
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				dykeField.set(scope, i, j, 0.0);
			}
		}
		
		// Initialize grid cells
		initializeGridCells(scope, demField, waterGeometries, initialWaterDepth, waterField);
		
		// Store the fields
		agent.setAttribute(WATER_FIELD, waterField);
		agent.setAttribute(DYKE_FIELD, dykeField);
		
		System.out.println("Grid initialized successfully with coordinate-aligned dyke field:");
		System.out.println("  Grid dimensions: " + gridWidth + "x" + gridHeight);
		System.out.println("  Water cells: " + activeWaterCells.size());
		System.out.println("  Dyke field properly aligned with DEM coordinate system");
		
		return true;
	}
	
	// === SHARED GRID CELL INITIALIZATION METHOD ===
	private void initializeGridCells(IScope scope, IField demField, IList<IShape> waterGeometries,
									 Double initialWaterDepth, IField waterField) throws GamaRuntimeException {
		int gridWidth = demField.getCols(scope);
		int gridHeight = demField.getRows(scope);
		
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
		
		System.out.println("Grid cells initialized: " + waterCellCount + " water cells");
		System.out.println("Initial water level: " + (uniformWaterLevel > 0 ? String.format("%.2f", uniformWaterLevel) : "0.0") + "m");
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
	
	// === RAIN APPLICATION METHOD ===
	private void applyRainfall(IScope scope, IField waterField) {
		final IAgent agent = getCurrentAgent(scope);
		final double rainRate = getFloatAttribute(agent, RAIN_RATE);
		
		if (rainRate > 0.0) {
			// Apply rain to all water cells (increases water level uniformly)
			for (GridCell cell : activeWaterCells) {
				cell.waterElevation += rainRate;
				if (waterField != null) {
					waterField.set(scope, cell.x, cell.y, cell.waterElevation);
				}
			}
		}
	}
	
	// === DYKE STATUS UPDATE METHOD ===
	private void updateDykeStatus(IScope scope, IField waterField) {
		final IAgent agent = getCurrentAgent(scope);
		final double destructionTime = getFloatAttribute(agent, DYKE_DESTRUCTION_TIME);
		final double currentTime = scope.getSimulation().getClock().getCycle();
		final IField dykeField = (IField) agent.getAttribute(DYKE_FIELD);
		
		List<DykeCell> dykesToRemove = new ArrayList<>();
		
		for (DykeCell dykeCell : activeDykes) {
			if (dykeCell.isDestroyed) continue;
			
			GridCell cell = dykeCell.gridCell;
			
			// Check if dyke cell is under water attack (adjacent to water)
			boolean currentlyUnderAttack = false;
			for (GridCell neighbor : cell.neighbors) {
				if (neighbor.isWater) {
					currentlyUnderAttack = true;
					break;
				}
			}
			
			// Update water attack status for individual cells
			if (currentlyUnderAttack && !dykeCell.isUnderWaterAttack) {
				// Water attack just started
				dykeCell.isUnderWaterAttack = true;
				dykeCell.waterAttackStartTime = currentTime;
			} else if (!currentlyUnderAttack && dykeCell.isUnderWaterAttack) {
				// No longer under attack (water receded)
				dykeCell.isUnderWaterAttack = false;
				dykeCell.waterAttackStartTime = -1;
			}
			
			// Check for destruction after sustained water attack
			if (dykeCell.isUnderWaterAttack &&
				(currentTime - dykeCell.waterAttackStartTime) >= destructionTime) {
				
				// Destroy the individual dyke cell
				dykeCell.isDestroyed = true;
				
				// Restore original terrain elevation
				cell.terrainElevation = cell.originalTerrainElevation;
				
				// Clear individual dyke cell in field
				if (dykeField != null) {
					dykeField.set(scope, cell.x, cell.y, 0.0);
				}
				
				// Remove from dyke structures
				dykeGridCells.remove(cell);
				dykesToRemove.add(dykeCell);
			}
		}
		
		// Remove destroyed dykes
		activeDykes.removeAll(dykesToRemove);
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
		
		// Get rain parameters
		boolean rainActive = getBoolAttribute(agent, RAIN_ACTIVE);
		double rainRate = getFloatAttribute(agent, RAIN_RATE);
		double rainIntensity = getFloatAttribute(agent, RAIN_INTENSITY);
		
		// Apply rain effects to parameters
		double effectiveRisingRate = risingRate;
		double effectiveFlowThreshold = flowThreshold;
		double effectiveMinFlowDiff = minFlowDiff;
		
		if (rainActive && rainRate > 0.0) {
			// Rain increases rising speed
			effectiveRisingRate += rainRate;
			
			// Rain makes spreading easier (reduces thresholds)
			effectiveFlowThreshold = flowThreshold * (1.0 / rainIntensity);
			effectiveMinFlowDiff = minFlowDiff * (1.0 / rainIntensity);
		}
		
		// Get water field for immediate updates
		IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		
		// Apply rainfall first if rain is active
		if (rainActive) {
			applyRainfall(scope, waterField);
		}
		
		// Check and update dyke status
		updateDykeStatus(scope, waterField);
		
		// 1. SPREAD WATER - only from current edge cells (WITH RAIN EFFECTS)
		List<GridCell> newWaterCells = new ArrayList<>();
		HashSet<GridCell> affectedNeighbors = new HashSet<>();
		
		final double finalEffectiveFlowThreshold = effectiveFlowThreshold;
		final double finalEffectiveRisingRate = effectiveRisingRate;
		
		// PARALLEL: Find minimum spreading level (safe - only reading)
		double minSpreadingLevel = edgeWaterCells.parallelStream()
			.filter(cell -> cell.waterElevation > cell.terrainElevation + finalEffectiveFlowThreshold)
			.mapToDouble(cell -> cell.waterElevation)
			.min()
			.orElse(Double.MAX_VALUE);
		
		// Sequential spreading (modifies neighbor states) - WITH RAIN AND DYKE EFFECTS
		for (GridCell edgeCell : edgeWaterCells) {
			if (edgeCell.waterElevation > edgeCell.terrainElevation + effectiveFlowThreshold) {
				for (GridCell neighbor : edgeCell.neighbors) {
					if (!neighbor.isWater && !dykeGridCells.contains(neighbor) &&
						(edgeCell.waterElevation - neighbor.terrainElevation) > effectiveMinFlowDiff) {
						
						neighbor.isWater = true;
						
						// Rain affects initial water level in newly flooded cells
						double baseWaterLevel = Math.max(
							neighbor.terrainElevation + effectiveFlowThreshold,
							minSpreadingLevel - 0.01
						);
						
						// Add rain bonus to new water cells for faster spreading
						if (rainActive && rainRate > 0.0) {
							baseWaterLevel += rainRate * rainIntensity * 0.5; // 50% of rain effect for new cells
						}
						
						neighbor.waterElevation = baseWaterLevel;
						newWaterCells.add(neighbor);
						
						// PERFORMANCE: Only update field for new water cells (not entire field)
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
		
		// 3. RISE WATER - REALISTIC PHYSICS WITH RAIN EFFECTS
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
						newElevations.put(cell, Math.min(cell.waterElevation + finalEffectiveRisingRate, targetLevel));
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
				// Uniform rise - update all cells (WITH RAIN EFFECTS)
				final double uniformRise = effectiveRisingRate;
				
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
	
	// === RAIN CONTROL ACTIONS ===
	@action(name = "start_rain", args = {
			@arg(name = "rain_rate", type = IType.FLOAT, doc = @doc("Rate of rainfall (meters per step)")),
			@arg(name = "rain_intensity", type = IType.FLOAT, optional = true, doc = @doc("Intensity multiplier for rain effects (default: 1.0)")) }, doc = @doc("Starts rainfall with specified rate"))
	public Boolean startRain(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final double rainRate = scope.getFloatArg("rain_rate");
		final double rainIntensity = scope.hasArg("rain_intensity") ? scope.getFloatArg("rain_intensity") : 1.0;
		
		setBoolAttribute(agent, RAIN_ACTIVE, true);
		setFloatAttribute(agent, RAIN_RATE, rainRate);
		setFloatAttribute(agent, RAIN_INTENSITY, rainIntensity);
		
		return true;
	}
	
	@action(name = "stop_rain", doc = @doc("Stops the rainfall"))
	public Boolean stopRain(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		setBoolAttribute(agent, RAIN_ACTIVE, false);
		setFloatAttribute(agent, RAIN_RATE, 0.0);
		return true;
	}
	
	@action(name = "set_rain_rate", args = {
			@arg(name = "rain_rate", type = IType.FLOAT, doc = @doc("New rain rate (meters per step)")) }, doc = @doc("Updates the rain rate while rain is active"))
	public Boolean setRainRate(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final double rainRate = scope.getFloatArg("rain_rate");
		setFloatAttribute(agent, RAIN_RATE, rainRate);
		return true;
	}
	
	@action(name = "set_rain_intensity", args = {
			@arg(name = "rain_intensity", type = IType.FLOAT, doc = @doc("Rain intensity multiplier")) }, doc = @doc("Updates the rain intensity while rain is active"))
	public Boolean setRainIntensity(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final double rainIntensity = scope.getFloatArg("rain_intensity");
		setFloatAttribute(agent, RAIN_INTENSITY, rainIntensity);
		return true;
	}
	
	@action(name = "is_rain_active", doc = @doc("Returns whether rain is currently active"))
	public Boolean isRainActive(final IScope scope) {
		return getBoolAttribute(getCurrentAgent(scope), RAIN_ACTIVE);
	}
	
	@action(name = "get_rain_rate", doc = @doc("Returns the current rain rate"))
	public Double getRainRate(final IScope scope) {
		return getFloatAttribute(getCurrentAgent(scope), RAIN_RATE);
	}
	
	@action(name = "get_rain_intensity", doc = @doc("Returns the current rain intensity"))
	public Double getRainIntensity(final IScope scope) {
		return getFloatAttribute(getCurrentAgent(scope), RAIN_INTENSITY);
	}
	
	// === DYKE BUILDING ACTIONS ===
	@action(name = "toggle_dyke_building_mode", doc = @doc("Toggles dyke building mode on/off"))
	public Boolean toggleDykeBuildingMode(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		boolean currentMode = getBoolAttribute(agent, DYKE_BUILDING_MODE);
		setBoolAttribute(agent, DYKE_BUILDING_MODE, !currentMode);
		return !currentMode;
	}
	
	@action(name = "is_dyke_building_mode", doc = @doc("Returns whether dyke building mode is active"))
	public Boolean isDykeBuildingMode(final IScope scope) {
		return getBoolAttribute(getCurrentAgent(scope), DYKE_BUILDING_MODE);
	}
	
	// === DYKE REMOVAL MODE CONTROL ===
	@action(name = "toggle_dyke_removal_mode", doc = @doc("Toggles dyke removal mode on/off"))
	public Boolean toggleDykeRemovalMode(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		boolean currentMode = getBoolAttribute(agent, DYKE_REMOVAL_MODE);
		setBoolAttribute(agent, DYKE_REMOVAL_MODE, !currentMode);
		return !currentMode;
	}
	
	@action(name = "is_dyke_removal_mode", doc = @doc("Returns whether dyke removal mode is active"))
	public Boolean isDykeRemovalMode(final IScope scope) {
		return getBoolAttribute(getCurrentAgent(scope), DYKE_REMOVAL_MODE);
	}
	
	// === IMPROVED DYKE BUILDING METHOD ===
	@action(name = "build_dyke", args = {
			@arg(name = "point1", type = IType.POINT, doc = @doc("First point of the dyke")),
			@arg(name = "point2", type = IType.POINT, doc = @doc("Second point of the dyke")) },
			doc = @doc("Builds a dyke between two points with terrain-following and water-aware adaptive heights"))
	public Boolean buildDyke(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		if (!getBoolAttribute(agent, DYKE_BUILDING_MODE)) {
			return false;
		}
		
		// Get points and basic setup
		GamaPoint point1 = (GamaPoint) scope.getArg("point1", IType.POINT);
		GamaPoint point2 = (GamaPoint) scope.getArg("point2", IType.POINT);
		
		int width = getIntAttribute(agent, GRID_WIDTH);
		int height = getIntAttribute(agent, GRID_HEIGHT);
		IField dykeField = (IField) agent.getAttribute(DYKE_FIELD);
		IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		
		if (dykeField == null) {
			System.out.println("ERROR: Dyke field is null!");
			return false;
		}
		
		// Convert world coordinates to grid coordinates
		int[] coords1 = worldToGrid(point1, scope, width, height);
		int[] coords2 = worldToGrid(point2, scope, width, height);
		
		double dykeHeight = getFloatAttribute(agent, DYKE_HEIGHT);
		double currentTime = scope.getSimulation().getClock().getCycle();
		
		// STEP 1: Get main dyke line cells
		List<int[]> dykeCoords = getLineCoordinates(coords1[0], coords1[1], coords2[0], coords2[1]);
		List<GridCell> mainDykeCells = new ArrayList<>();
		
		for (int[] coord : dykeCoords) {
			int x = coord[0], y = coord[1];
			if (x >= 0 && x < width && y >= 0 && y < height) {
				GridCell cell = internalGrid[x][y];
				if (!dykeGridCells.contains(cell)) {
					mainDykeCells.add(cell);
				}
			}
		}
		
		if (mainDykeCells.isEmpty()) {
			return false;
		}
		
		// STEP 2: Calculate adaptive heights for main dyke line
		Map<GridCell, Double> dykeElevations = new HashMap<>();
		
		for (GridCell cell : mainDykeCells) {
			double adaptiveHeight = calculateAdaptiveDykeHeight(cell, dykeHeight);
			dykeElevations.put(cell, adaptiveHeight);
		}
		
		// STEP 3: Apply smoothing along dyke line (like water initialization)
		smoothDykeElevations(dykeElevations, mainDykeCells);
		
		// STEP 4: Add neighbor cells for continuity and thickness
		Set<GridCell> allDykeCells = new HashSet<>(mainDykeCells);
		
		for (GridCell mainCell : mainDykeCells) {
			double mainHeight = dykeElevations.get(mainCell);
			
			// Add neighboring cells for thickness
			for (GridCell neighbor : mainCell.neighbors) {
				if (!dykeGridCells.contains(neighbor) && !allDykeCells.contains(neighbor)) {
					double neighborHeight = calculateNeighborDykeHeight(neighbor, mainHeight, dykeHeight);
					dykeElevations.put(neighbor, neighborHeight);
					allDykeCells.add(neighbor);
				}
			}
		}
		
		// STEP 5: Build all dyke cells (handle water displacement)
		int dykesBuilt = 0;
		List<GridCell> displacedWaterCells = new ArrayList<>();
		
		for (GridCell cell : allDykeCells) {
			double elevation = dykeElevations.get(cell);
			
			// Handle water displacement if building through water
			if (cell.isWater) {
				displacedWaterCells.add(cell);
				displaceWaterFromCell(cell, waterField, scope);
			}
			
			// Build dyke cell
			DykeCell dykeCell = new DykeCell(cell, currentTime);
			activeDykes.add(dykeCell);
			dykeGridCells.add(cell);
			
			// Set elevations
			dykeField.set(scope, cell.x, cell.y, elevation);
			cell.terrainElevation = elevation;
			
			dykesBuilt++;
		}
		
		// STEP 6: Update water system after displacement
		if (!displacedWaterCells.isEmpty()) {
			redistributeDisplacedWater(displacedWaterCells, waterField, scope);
			identifyEdgeCells(); // Recalculate edge cells
		}
		
		System.out.println("Dyke built: " + dykesBuilt + " cells, " + displacedWaterCells.size() + " water cells displaced");
		return dykesBuilt > 0;
	}
	
	// === DYKE AREA REMOVAL METHOD ===
	@action(name = "remove_dyke_area", args = {
			@arg(name = "click_point", type = IType.POINT, doc = @doc("Point where user clicked to remove dyke area"))
	}, doc = @doc("Removes a connected component of dyke cells starting from the clicked point"))
	public Boolean removeDykeArea(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		if (!getBoolAttribute(agent, DYKE_REMOVAL_MODE)) {
			return false;
		}
		
		// Get click point and convert to grid coordinates
		GamaPoint clickPoint = (GamaPoint) scope.getArg("click_point", IType.POINT);
		int width = getIntAttribute(agent, GRID_WIDTH);
		int height = getIntAttribute(agent, GRID_HEIGHT);
		
		int[] coords = worldToGrid(clickPoint, scope, width, height);
		int x = coords[0], y = coords[1];
		
		// Check if clicked cell is within bounds and is a dyke cell
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}
		
		GridCell clickedCell = internalGrid[x][y];
		if (!dykeGridCells.contains(clickedCell)) {
			System.out.println("Clicked point is not a dyke cell");
			return false;
		}
		
		// Find connected component of dyke cells using BFS
		Set<GridCell> connectedDykes = findConnectedDykeComponent(clickedCell);
		
		if (connectedDykes.isEmpty()) {
			return false;
		}
		
		// Remove the connected component
		int removedCount = removeConnectedDykes(scope, connectedDykes);
		
		System.out.println("Dyke area removed: " + removedCount + " connected dyke cells");
		return removedCount > 0;
	}
	
	// === DYKE AREA ANALYSIS METHODS ===
	@action(name = "get_dyke_area_size", args = {
			@arg(name = "click_point", type = IType.POINT, doc = @doc("Point to analyze dyke area size"))
	}, doc = @doc("Returns the size of the connected dyke component at the clicked point (for preview)"))
	public Integer getDykeAreaSize(final IScope scope) throws GamaRuntimeException {
		GamaPoint clickPoint = (GamaPoint) scope.getArg("click_point", IType.POINT);
		int width = getIntAttribute(getCurrentAgent(scope), GRID_WIDTH);
		int height = getIntAttribute(getCurrentAgent(scope), GRID_HEIGHT);
		
		int[] coords = worldToGrid(clickPoint, scope, width, height);
		int x = coords[0], y = coords[1];
		
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return 0;
		}
		
		GridCell clickedCell = internalGrid[x][y];
		if (!dykeGridCells.contains(clickedCell)) {
			return 0;
		}
		
		Set<GridCell> connectedDykes = findConnectedDykeComponent(clickedCell);
		return connectedDykes.size();
	}
	
	// === HELPER METHODS FOR DYKE REMOVAL ===
	
	/**
	 * Finds all dyke cells connected to the starting cell using BFS
	 */
	private Set<GridCell> findConnectedDykeComponent(GridCell startCell) {
		Set<GridCell> visited = new HashSet<>();
		Set<GridCell> connectedComponent = new HashSet<>();
		Queue<GridCell> queue = new LinkedList<>();
		
		// Start BFS from the clicked dyke cell
		queue.add(startCell);
		visited.add(startCell);
		connectedComponent.add(startCell);
		
		while (!queue.isEmpty()) {
			GridCell current = queue.poll();
			
			// Check all neighbors
			for (GridCell neighbor : current.neighbors) {
				// If neighbor is a dyke cell and not yet visited
				if (dykeGridCells.contains(neighbor) && !visited.contains(neighbor)) {
					visited.add(neighbor);
					connectedComponent.add(neighbor);
					queue.add(neighbor);
				}
			}
		}
		
		return connectedComponent;
	}
	
	/**
	 * Removes a connected component of dyke cells and handles cleanup
	 */
	private int removeConnectedDykes(IScope scope, Set<GridCell> dykesToRemove) {
		final IAgent agent = getCurrentAgent(scope);
		final IField dykeField = (IField) agent.getAttribute(DYKE_FIELD);
		final IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		
		int removedCount = 0;
		List<DykeCell> dykeCellsToRemove = new ArrayList<>();
		
		// Remove dyke cells and restore terrain
		for (GridCell dykeCell : dykesToRemove) {
			// Find corresponding DykeCell object
			DykeCell correspondingDykeCell = null;
			for (DykeCell dc : activeDykes) {
				if (dc.gridCell == dykeCell && !dc.isDestroyed) {
					correspondingDykeCell = dc;
					break;
				}
			}
			
			if (correspondingDykeCell != null) {
				// Mark as destroyed
				correspondingDykeCell.isDestroyed = true;
				dykeCellsToRemove.add(correspondingDykeCell);
			}
			
			// Restore original terrain elevation
			dykeCell.terrainElevation = dykeCell.originalTerrainElevation;
			
			// Clear dyke field at this location
			if (dykeField != null) {
				dykeField.set(scope, dykeCell.x, dykeCell.y, 0.0);
			}
			
			// Remove from dyke tracking structures
			dykeGridCells.remove(dykeCell);
			removedCount++;
		}
		
		// Remove destroyed dyke cells from active list
		activeDykes.removeAll(dykeCellsToRemove);
		
		// Check if water should flow into newly opened areas
		handleWaterFlowAfterDykeRemoval(scope, dykesToRemove, waterField);
		
		return removedCount;
	}
	
	/**
	 * Handles potential water flow when dykes are removed
	 */
	private void handleWaterFlowAfterDykeRemoval(IScope scope, Set<GridCell> removedDykes, IField waterField) {
		// Check if any removed dyke cells are adjacent to water
		Set<GridCell> potentialFlowCells = new HashSet<>();
		
		for (GridCell removedDyke : removedDykes) {
			// Check if this cell should become a water cell
			for (GridCell neighbor : removedDyke.neighbors) {
				if (neighbor.isWater) {
					// There's water adjacent to this removed dyke
					// The removed dyke cell might become flooded
					double waterLevel = neighbor.waterElevation;
					if (waterLevel > removedDyke.terrainElevation + getFloatAttribute(getCurrentAgent(scope), FLOW_THRESHOLD)) {
						potentialFlowCells.add(removedDyke);
						break;
					}
				}
			}
		}
		
		// Convert potential flow cells to water cells if appropriate
		for (GridCell flowCell : potentialFlowCells) {
			if (!flowCell.isWater) {
				// Find the highest adjacent water level
				double maxAdjacentWater = 0.0;
				boolean hasWaterNeighbor = false;
				
				for (GridCell neighbor : flowCell.neighbors) {
					if (neighbor.isWater) {
						hasWaterNeighbor = true;
						maxAdjacentWater = Math.max(maxAdjacentWater, neighbor.waterElevation);
					}
				}
				
				if (hasWaterNeighbor && maxAdjacentWater > flowCell.terrainElevation + getFloatAttribute(getCurrentAgent(scope), FLOW_THRESHOLD)) {
					// Convert to water cell
					flowCell.isWater = true;
					flowCell.waterElevation = Math.max(flowCell.terrainElevation + getFloatAttribute(getCurrentAgent(scope), FLOW_THRESHOLD), 
													 maxAdjacentWater - 0.1); // Slightly lower than source
					
					activeWaterCells.add(flowCell);
					
					// Update water field
					if (waterField != null) {
						waterField.set(scope, flowCell.x, flowCell.y, flowCell.waterElevation);
					}
				}
			}
		}
		
		// Recalculate edge cells after potential new water cells
		if (!potentialFlowCells.isEmpty()) {
			identifyEdgeCells();
		}
	}
	
	// === DYKE BUILDING HELPER METHODS ===
	
	// Helper method: Calculate adaptive dyke height
	private double calculateAdaptiveDykeHeight(GridCell cell, double standardDykeHeight) {
		// 1. Local terrain average (prevents floating walls)
		double localAvgTerrain = getLocalTerrainAverage(cell, 3);
		
		// 2. Terrain-based height
		double terrainBasedHeight = localAvgTerrain + standardDykeHeight;
		
		// 3. Water-threat-based height (like water initialization)
		double nearbyMaxWater = getNearbyMaxWaterLevel(cell, 5);
		double waterThreatHeight = nearbyMaxWater + 2.0; // 2m safety margin
		
		// 4. Use the higher of the two for effective protection
		return Math.max(terrainBasedHeight, waterThreatHeight);
	}
	
	// Helper method: Get local terrain average
	private double getLocalTerrainAverage(GridCell cell, int radius) {
		double sum = cell.originalTerrainElevation;
		int count = 1;
		
		// BFS to get cells within radius
		Set<GridCell> visited = new HashSet<>();
		Queue<GridCell> queue = new LinkedList<>();
		queue.add(cell);
		visited.add(cell);
		
		for (int r = 0; r < radius && !queue.isEmpty(); r++) {
			int levelSize = queue.size();
			for (int i = 0; i < levelSize; i++) {
				GridCell current = queue.poll();
				
				for (GridCell neighbor : current.neighbors) {
					if (!visited.contains(neighbor)) {
						visited.add(neighbor);
						queue.add(neighbor);
						sum += neighbor.originalTerrainElevation;
						count++;
					}
				}
			}
		}
		
		return sum / count;
	}
	
	// Helper method: Get nearby maximum water level
	private double getNearbyMaxWaterLevel(GridCell cell, int radius) {
		double maxWaterLevel = 0.0;
		
		Set<GridCell> visited = new HashSet<>();
		Queue<GridCell> queue = new LinkedList<>();
		queue.add(cell);
		visited.add(cell);
		
		if (cell.isWater) {
			maxWaterLevel = Math.max(maxWaterLevel, cell.waterElevation);
		}
		
		for (int r = 0; r < radius && !queue.isEmpty(); r++) {
			int levelSize = queue.size();
			for (int i = 0; i < levelSize; i++) {
				GridCell current = queue.poll();
				
				for (GridCell neighbor : current.neighbors) {
					if (!visited.contains(neighbor)) {
						visited.add(neighbor);
						queue.add(neighbor);
						
						if (neighbor.isWater) {
							maxWaterLevel = Math.max(maxWaterLevel, neighbor.waterElevation);
						}
					}
				}
			}
		}
		
		return maxWaterLevel;
	}
	
	// Helper method: Smooth dyke elevations (like water initialization)
	private void smoothDykeElevations(Map<GridCell, Double> elevations, List<GridCell> dykeList) {
		// Apply smoothing iterations
		for (int iteration = 0; iteration < 3; iteration++) {
			Map<GridCell, Double> smoothedElevations = new HashMap<>();
			
			for (GridCell cell : dykeList) {
				double sum = elevations.get(cell);
				int count = 1;
				
				// Average with neighboring dyke cells
				for (GridCell neighbor : cell.neighbors) {
					if (elevations.containsKey(neighbor)) {
						sum += elevations.get(neighbor);
						count++;
					}
				}
				
				smoothedElevations.put(cell, sum / count);
			}
			
			// Update elevations
			elevations.putAll(smoothedElevations);
		}
	}
	
	// Helper method: Calculate neighbor dyke height
	private double calculateNeighborDykeHeight(GridCell neighbor, double mainHeight, double standardDykeHeight) {
		// Supporting cells are 70% of main dyke height
		double reductionFactor = 0.7;
		double neighborLocalTerrain = getLocalTerrainAverage(neighbor, 2);
		
		// Ensure neighbor is still effective but not as tall
		double minHeight = neighborLocalTerrain + (standardDykeHeight * reductionFactor);
		double adaptiveHeight = mainHeight * reductionFactor;
		
		return Math.max(minHeight, adaptiveHeight);
	}
	
	// Helper method: Displace water from cell
	private void displaceWaterFromCell(GridCell cell, IField waterField, IScope scope) {
		if (cell.isWater) {
			double waterVolume = Math.max(0, cell.waterElevation - cell.terrainElevation);
			
			// Remove from water system
			cell.isWater = false;
			cell.waterElevation = 0.0;
			activeWaterCells.remove(cell);
			edgeWaterCells.remove(cell);
			
			// Clear water field
			if (waterField != null) {
				waterField.set(scope, cell.x, cell.y, 0.0);
			}
			
			// Store for redistribution
			cell.setAttribute("displaced_water", waterVolume);
		}
	}
	
	// Helper method: Redistribute displaced water
	private void redistributeDisplacedWater(List<GridCell> displacedCells, IField waterField, IScope scope) {
		for (GridCell displacedCell : displacedCells) {
			Double waterVolumeObj = (Double) displacedCell.getAttribute("displaced_water");
			double waterVolume = waterVolumeObj != null ? waterVolumeObj : 0.0;
			
			if (waterVolume > 0) {
				// Find valid neighbors for redistribution
				List<GridCell> validNeighbors = displacedCell.neighbors.stream()
					.filter(n -> n.isWater && !dykeGridCells.contains(n))
					.collect(Collectors.toList());
				
				if (!validNeighbors.isEmpty()) {
					double waterPerNeighbor = waterVolume / validNeighbors.size();
					
					for (GridCell neighbor : validNeighbors) {
						neighbor.waterElevation += waterPerNeighbor;
						if (waterField != null) {
							waterField.set(scope, neighbor.x, neighbor.y, neighbor.waterElevation);
						}
					}
				}
			}
			
			// Clean up
			displacedCell.setAttribute("displaced_water", 0.0);
		}
	}
	
	// Helper method: Convert world coordinates to grid coordinates
	private int[] worldToGrid(GamaPoint point, IScope scope, int width, int height) {
		double worldMinX = scope.getSimulation().getEnvelope().getMinX();
		double worldMinY = scope.getSimulation().getEnvelope().getMinY();
		double worldMaxX = scope.getSimulation().getEnvelope().getMaxX();
		double worldMaxY = scope.getSimulation().getEnvelope().getMaxY();
		
		double worldWidth = worldMaxX - worldMinX;
		double worldHeight = worldMaxY - worldMinY;
		
		double cellWorldWidth = worldWidth / width;
		double cellWorldHeight = worldHeight / height;
		
		int x = (int) Math.floor((point.getX() - worldMinX) / cellWorldWidth);
		int y = (int) Math.floor((point.getY() - worldMinY) / cellWorldHeight);
		
		// Clamp to grid bounds
		x = Math.max(0, Math.min(width - 1, x));
		y = Math.max(0, Math.min(height - 1, y));
		
		return new int[]{x, y};
	}
	
	// Helper method for Bresenham's line algorithm
	private List<int[]> getLineCoordinates(int x1, int y1, int x2, int y2) {
		List<int[]> coordinates = new ArrayList<>();
		
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		int sx = x1 < x2 ? 1 : -1;
		int sy = y1 < y2 ? 1 : -1;
		int err = dx - dy;
		
		int x = x1;
		int y = y1;
		
		while (true) {
			coordinates.add(new int[]{x, y});
			
			if (x == x2 && y == y2) break;
			
			int e2 = 2 * err;
			if (e2 > -dy) {
				err -= dy;
				x += sx;
			}
			if (e2 < dx) {
				err += dx;
				y += sy;
			}
		}
		
		return coordinates;
	}
	
	// === DYKE STATUS ACTIONS ===
	@action(name = "get_active_dyke_count", doc = @doc("Returns the number of active dykes"))
	public Integer getActiveDykeCount(final IScope scope) {
		return (int) activeDykes.stream().filter(d -> !d.isDestroyed).count();
	}
	
	@action(name = "get_surrounded_dyke_count", doc = @doc("Returns the number of dykes currently under water attack"))
	public Integer getSurroundedDykeCount(final IScope scope) {
		return (int) activeDykes.stream().filter(d -> !d.isDestroyed && d.isUnderWaterAttack).count();
	}
	
	@action(name = "clear_all_dykes", doc = @doc("Removes all dykes from the simulation"))
	public Boolean clearAllDykes(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		final IField dykeField = (IField) agent.getAttribute(DYKE_FIELD);
		final int width = getIntAttribute(agent, GRID_WIDTH);
		final int height = getIntAttribute(agent, GRID_HEIGHT);
		
		// Restore terrain elevations for all dykes
		for (DykeCell dykeCell : activeDykes) {
			if (!dykeCell.isDestroyed) {
				GridCell cell = dykeCell.gridCell;
				// Restore original terrain elevation
				cell.terrainElevation = cell.originalTerrainElevation;
				
				// Clear dyke field at this specific location
				if (dykeField != null) {
					dykeField.set(scope, cell.x, cell.y, 0.0);
				}
			}
		}
		
		// Clear data structures
		activeDykes.clear();
		dykeGridCells.clear();
		
		return true;
	}
	
	// === SIMULATION CONTROL ACTIONS ===
	@action(
		name = "start_spreading_simulation",
		doc = @doc("Starts the spreading simulation")
	)
	public Boolean startSpreadingSimulation(final IScope scope){
		final IAgent agent = getCurrentAgent(scope);
		setBoolAttribute(agent, SIMULATION_ACTIVE, true);
		System.out.println("Simulation started with " + activeWaterCells.size() + " water cells");
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
		
		// Reset rain state
		setBoolAttribute(agent, RAIN_ACTIVE, false);
		setFloatAttribute(agent, RAIN_RATE, 0.0);
		setFloatAttribute(agent, RAIN_INTENSITY, 1.0);
		
		// Clear dykes during reset
		clearAllDykes(scope);
		setBoolAttribute(agent, DYKE_BUILDING_MODE, false);
		setBoolAttribute(agent, DYKE_REMOVAL_MODE, false);
		
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
				
				// Restore original terrain elevation
				cell.terrainElevation = cell.originalTerrainElevation;
				
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
		System.out.println("Reset complete: " + activeWaterCells.size() + " water cells restored");
		
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