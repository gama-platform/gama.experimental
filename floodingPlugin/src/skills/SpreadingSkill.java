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
		// OBSTACLE VARIABLES
		@variable(name = "obstacle_field", type = IType.MATRIX, doc = @doc("Field representing obstacle elevations for visualization")),
		@variable(name = "obstacle_building_mode", type = IType.BOOL, init = "false", doc = @doc("Whether obstacle building mode is active")),
		@variable(name = "obstacle_removal_mode", type = IType.BOOL, init = "false", doc = @doc("Whether obstacle removal mode is active")),
		@variable(name = "default_obstacle_height", type = IType.FLOAT, init = "5.0", doc = @doc("Default height of obstacles in meters")),
		@variable(name = "obstacle_destruction_time", type = IType.FLOAT, init = "10.0", doc = @doc("Time in cycles before obstacle cell under water attack is destroyed"))
})
@skill(name = "spreading", concept = { "spreading", "simulation", "water", "flood", "rain", "obstacles", "buildings" }, 
       doc = @doc("A skill for managing spreading simulations with optimized water flow mechanics, rain system, and generic obstacle management"))
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
	// OBSTACLE CONSTANTS
	public static final String OBSTACLE_FIELD = "obstacle_field";
	public static final String OBSTACLE_BUILDING_MODE = "obstacle_building_mode";
	public static final String OBSTACLE_REMOVAL_MODE = "obstacle_removal_mode";
	public static final String DEFAULT_OBSTACLE_HEIGHT = "default_obstacle_height";
	public static final String OBSTACLE_DESTRUCTION_TIME = "obstacle_destruction_time";
	
	// === OBSTACLE TYPES ===
	public enum ObstacleType {
		DYKE("dyke", true, 10.0, true, 1.0),
		BUILDING("building", false, 0.0, false, 1.0),
		WALL("wall", false, 0.0, false, 1.0),
		EARTHWORK("earthwork", true, 15.0, true, 0.8),
		BARRIER("barrier", true, 8.0, true, 0.9);
		
		public final String name;
		public final boolean destroyable;
		public final double defaultDestructionTime;
		public final boolean followTerrain;
		public final double waterResistance; // 1.0 = full resistance, 0.0 = no resistance
		
		ObstacleType(String name, boolean destroyable, double destructionTime, boolean followTerrain, double waterResistance) {
			this.name = name;
			this.destroyable = destroyable;
			this.defaultDestructionTime = destructionTime;
			this.followTerrain = followTerrain;
			this.waterResistance = waterResistance;
		}
		
		public static ObstacleType fromString(String name) {
			for (ObstacleType type : ObstacleType.values()) {
				if (type.name.equalsIgnoreCase(name)) {
					return type;
				}
			}
			return DYKE; // Default fallback
		}
	}
	
	// === INTERNAL GRID CELL CLASS ===
	public static class GridCell {
		public int x, y;
		public boolean isWater;
		public double waterElevation;
		public double terrainElevation;
		public double originalTerrainElevation; // Store original elevation for obstacle management
		public boolean isEdgeCell;
		public List<GridCell> neighbors;
		public IShape shape;
		private Map<String, Object> attributes; // For temporary data storage
		
		// Store original water topology for restoration
		public boolean wasOriginalWater; // Track if this was originally a water cell
		public double originalWaterElevation; // Store original water level
		
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
			// Initialize water topology tracking
			this.wasOriginalWater = false;
			this.originalWaterElevation = 0.0;
		}
		
		public void setAttribute(String key, Object value) {
			attributes.put(key, value);
		}
		
		public Object getAttribute(String key) {
			return attributes.get(key);
		}
	}
	
	// === ENHANCED INDIVIDUAL OBSTACLE STRUCTURE CLASS ===
	public static class ObstacleStructure {
		public int obstacleId;
		public ObstacleType type;
		public Set<GridCell> obstacleCells;
		public double creationTime;
		public boolean isDestroyed;
		public Map<GridCell, Double> cellElevations; // Store individual cell elevations
		// Store displaced water cells for restoration
		public Set<GridCell> displacedWaterCells;
		public double customDestructionTime;
		public boolean isDestroyable;
		
		public ObstacleStructure(int id, ObstacleType type, double time, boolean destroyable, double destructionTime) {
			this.obstacleId = id;
			this.type = type;
			this.creationTime = time;
			this.isDestroyed = false;
			this.obstacleCells = new HashSet<>();
			this.cellElevations = new HashMap<>();
			this.displacedWaterCells = new HashSet<>(); // Track displaced water
			this.isDestroyable = destroyable;
			this.customDestructionTime = destructionTime;
		}
		
		public void addCell(GridCell cell, double elevation) {
			obstacleCells.add(cell);
			cellElevations.put(cell, elevation);
		}
		
		// Track displaced water cells
		public void addDisplacedWaterCell(GridCell cell) {
			displacedWaterCells.add(cell);
		}
		
		public boolean containsCell(GridCell cell) {
			return obstacleCells.contains(cell);
		}
		
		public int getSize() {
			return obstacleCells.size();
		}
		
		public Double getCellElevation(GridCell cell) {
			return cellElevations.get(cell);
		}
		
		public double getDestructionTime() {
			return customDestructionTime > 0 ? customDestructionTime : type.defaultDestructionTime;
		}
	}
	
	// === ENHANCED OBSTACLE CELL CLASS (for water attack tracking) ===
	public static class ObstacleCell {
		public GridCell gridCell;
		public int obstacleId; // Which obstacle this cell belongs to
		public ObstacleType type;
		public double creationTime;
		public double waterAttackStartTime;  // Track when water attack started
		public boolean isUnderWaterAttack;   // Track if adjacent to water
		public boolean isDestroyed;
		public boolean isDestroyable;
		
		public ObstacleCell(GridCell cell, int obstacleId, ObstacleType type, double time, boolean destroyable) {
			this.gridCell = cell;
			this.obstacleId = obstacleId;
			this.type = type;
			this.creationTime = time;
			this.waterAttackStartTime = -1;
			this.isUnderWaterAttack = false;
			this.isDestroyed = false;
			this.isDestroyable = destroyable;
		}
	}
	
	// === INTERNAL DATA STRUCTURES ===
	private GridCell[][] internalGrid;
	private List<GridCell> activeWaterCells;
	private HashSet<GridCell> edgeWaterCells;
	private List<ObstacleCell> activeObstacles;
	private HashSet<GridCell> obstacleGridCells;
	private Map<IAgent, SpreadingSkill> skillInstances = new ConcurrentHashMap<>();
	
	// === INDIVIDUAL OBSTACLE TRACKING DATA STRUCTURES ===
	private int nextObstacleId = 1; // Counter for unique obstacle IDs
	private Map<Integer, ObstacleStructure> individualObstacles; // Map obstacle ID to obstacle structure
	private Map<GridCell, Set<Integer>> cellToObstacleIds; // Map cell to which obstacles it belongs to
	
	// === ENHANCED CONSTRUCTOR ===
	public SpreadingSkill() {
		super();
		this.activeWaterCells = new ArrayList<>();
		this.edgeWaterCells = new HashSet<>();
		this.activeObstacles = new ArrayList<>();
		this.obstacleGridCells = new HashSet<>();
		this.individualObstacles = new HashMap<>();
		this.cellToObstacleIds = new HashMap<>();
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
		activeObstacles.clear();
		obstacleGridCells.clear();
		individualObstacles.clear();
		cellToObstacleIds.clear();
		nextObstacleId = 1;
		
		// Create water field with same dimensions as DEM
		IField waterField = GamaFieldType.withObject(scope, 0.0, gridWidth, gridHeight, Types.FLOAT);
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				waterField.set(scope, i, j, 0.0);
			}
		}
		
		// Create obstacle field with same dimensions
		IField obstacleField = GamaFieldType.withObject(scope, 0.0, gridWidth, gridHeight, Types.FLOAT);
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				obstacleField.set(scope, i, j, 0.0);
			}
		}
		
		// Initialize grid (same as obstacle version)
		initializeGridCells(scope, demField, waterGeometries, initialWaterDepth, waterField);
		
		// Store the fields
		agent.setAttribute(WATER_FIELD, waterField);
		agent.setAttribute(OBSTACLE_FIELD, obstacleField);
		
		System.out.println("INIT: Grid initialized with " + activeWaterCells.size() + " water cells and individual obstacle tracking");
		
		return true;
	}
	
	// === NEW GRID INITIALIZATION WITH PRE-CREATED OBSTACLE FIELD ===
	@action(name = "initialize_spreading_grid_with_obstacle_field", args = {
			@arg(name = "dem_field", type = IType.MATRIX, doc = @doc("Digital elevation model field")),
			@arg(name = "obstacle_field", type = IType.MATRIX, doc = @doc("Pre-created obstacle field with correct coordinate system")),
			@arg(name = "water_geometries", type = IType.LIST, doc = @doc("List of water polygon geometries")),
			@arg(name = "initial_water_depth", type = IType.FLOAT, optional = true, doc = @doc("Initial water depth (default: 1.5m)")),
			@arg(name = "flow_threshold", type = IType.FLOAT, optional = true, doc = @doc("Flow threshold parameter")),
			@arg(name = "rising_rate", type = IType.FLOAT, optional = true, doc = @doc("Rising rate parameter")),
			@arg(name = "min_flow_diff", type = IType.FLOAT, optional = true, doc = @doc("Minimum flow difference parameter")),
			@arg(name = "equalization_threshold", type = IType.FLOAT, optional = true, doc = @doc("Equalization threshold parameter")) }, doc = @doc("Initializes the spreading grid with DEM and pre-created obstacle field"))
	public Boolean initializeSpreadingGridWithObstacleField(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		// Get parameters
		final IField demField = (IField) scope.getArg("dem_field", IType.FIELD);
		final IField obstacleField = (IField) scope.getArg("obstacle_field", IType.FIELD);
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
		activeObstacles.clear();
		obstacleGridCells.clear();
		individualObstacles.clear();
		cellToObstacleIds.clear();
		nextObstacleId = 1;
		
		// Create water field with same dimensions as DEM
		IField waterField = GamaFieldType.withObject(scope, 0.0, gridWidth, gridHeight, Types.FLOAT);
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				waterField.set(scope, i, j, 0.0);
			}
		}
		
		// Use the pre-created obstacle field (already has correct coordinate system)
		// Just ensure it's initialized to zero
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				obstacleField.set(scope, i, j, 0.0);
			}
		}
		
		// Initialize grid cells
		initializeGridCells(scope, demField, waterGeometries, initialWaterDepth, waterField);
		
		// Store the fields
		agent.setAttribute(WATER_FIELD, waterField);
		agent.setAttribute(OBSTACLE_FIELD, obstacleField);
		
		System.out.println("Grid initialized successfully with coordinate-aligned obstacle field and individual obstacle tracking:");
		System.out.println("  Grid dimensions: " + gridWidth + "x" + gridHeight);
		System.out.println("  Water cells: " + activeWaterCells.size());
		System.out.println("  Individual obstacle management system ready");
		
		return true;
	}
	
	// === NEW: ADD OBSTACLES FROM SHAPEFILE ===
	@action(name = "add_obstacles_from_shapefile", args = {
			@arg(name = "obstacle_shapefile", type = IType.LIST, doc = @doc("List of obstacle geometries from shapefile")),
			@arg(name = "obstacle_type", type = IType.STRING, doc = @doc("Type of obstacle (dyke, building, wall, earthwork, barrier)")),
			@arg(name = "height", type = IType.FLOAT, doc = @doc("Height of obstacles in meters")),
			@arg(name = "uniform_height", type = IType.BOOL, optional = true, doc = @doc("Whether to use uniform height (default: true)")),
			@arg(name = "destroyable", type = IType.BOOL, optional = true, doc = @doc("Whether obstacles can be destroyed by water (default: based on obstacle type)")),
			@arg(name = "destruction_time", type = IType.FLOAT, optional = true, doc = @doc("Time in cycles before obstacle is destroyed under water attack (default: based on obstacle type)"))
	}, doc = @doc("Adds obstacles from shapefile geometries to the simulation"))
	public Boolean addObstaclesFromShapefile(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		final IList<IShape> obstacleGeometries = scope.getListArg("obstacle_shapefile");
		final String obstacleTypeStr = scope.getStringArg("obstacle_type");
		final double height = scope.getFloatArg("height");
		final boolean uniformHeight = scope.hasArg("uniform_height") ? scope.getBoolArg("uniform_height") : true;
		
		// Parse obstacle type
		ObstacleType obstacleType = ObstacleType.fromString(obstacleTypeStr);
		
		// Determine destroyable property
		boolean destroyable = scope.hasArg("destroyable") ? scope.getBoolArg("destroyable") : obstacleType.destroyable;
		
		// Determine destruction time
		double destructionTime = scope.hasArg("destruction_time") ? scope.getFloatArg("destruction_time") : obstacleType.defaultDestructionTime;
		
		if (obstacleGeometries == null || obstacleGeometries.isEmpty()) {
			System.out.println("No obstacle geometries provided");
			return false;
		}
		
		// Get grid dimensions and fields
		int width = getIntAttribute(agent, GRID_WIDTH);
		int height_grid = getIntAttribute(agent, GRID_HEIGHT);
		IField obstacleField = (IField) agent.getAttribute(OBSTACLE_FIELD);
		IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		
		if (obstacleField == null) {
			System.out.println("ERROR: Obstacle field is null!");
			return false;
		}
		
		double currentTime = scope.getSimulation().getClock().getCycle();
		int totalObstaclesCreated = 0;
		int totalCellsAffected = 0;
		
		// Process each obstacle geometry
		for (IShape obstacleGeom : obstacleGeometries) {
			if (obstacleGeom == null) continue;
			
			// Create individual obstacle structure
			int currentObstacleId = nextObstacleId++;
			ObstacleStructure newObstacle = new ObstacleStructure(currentObstacleId, obstacleType, currentTime, destroyable, destructionTime);
			
			// Find all grid cells that intersect with this obstacle
			List<GridCell> affectedCells = new ArrayList<>();
			List<GridCell> displacedWaterCells = new ArrayList<>();
			
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height_grid; j++) {
					GridCell cell = internalGrid[i][j];
					if (cell != null && cell.shape != null) {
						try {
							// Check if cell intersects with obstacle geometry
							if (SpatialProperties.overlaps(scope, cell.shape, obstacleGeom) || 
								obstacleGeom.covers(cell.shape.getLocation())) {
								affectedCells.add(cell);
							}
						} catch (Exception e) {
							// Fallback to location-based check
							if (obstacleGeom.covers(cell.shape.getLocation())) {
								affectedCells.add(cell);
							}
						}
					}
				}
			}
			
			if (affectedCells.isEmpty()) {
				continue; // Skip this geometry if no cells affected
			}
			
			// Calculate obstacle elevations for affected cells
			for (GridCell cell : affectedCells) {
				double obstacleElevation;
				
				if (uniformHeight) {
					// Uniform height mode
					if (obstacleType.followTerrain) {
						obstacleElevation = cell.originalTerrainElevation + height;
					} else {
						obstacleElevation = height; // Absolute elevation
					}
				} else {
					// Variable height mode (could be enhanced later with height attributes from shapefile)
					obstacleElevation = cell.originalTerrainElevation + height;
				}
				
				// Handle water displacement if building through water
				if (cell.isWater) {
					displacedWaterCells.add(cell);
					newObstacle.addDisplacedWaterCell(cell);
					displaceWaterFromCell(cell, waterField, scope);
				}
				
				// Add to obstacle tracking
				newObstacle.addCell(cell, obstacleElevation);
				cellToObstacleIds.computeIfAbsent(cell, k -> new HashSet<>()).add(currentObstacleId);
				obstacleGridCells.add(cell);
				
				// Create ObstacleCell for water attack tracking
				ObstacleCell obstacleCell = new ObstacleCell(cell, currentObstacleId, obstacleType, currentTime, destroyable);
				activeObstacles.add(obstacleCell);
				
				// Update terrain elevation and obstacle field
				double currentFieldElevation = ((Number) obstacleField.get(scope, cell.x, cell.y)).doubleValue();
				double newElevation = Math.max(currentFieldElevation, obstacleElevation);
				
				obstacleField.set(scope, cell.x, cell.y, newElevation);
				cell.terrainElevation = newElevation;
				
				totalCellsAffected++;
			}
			
			// Store the obstacle
			individualObstacles.put(currentObstacleId, newObstacle);
			totalObstaclesCreated++;
			
			// Handle water redistribution
			if (!displacedWaterCells.isEmpty()) {
				redistributeDisplacedWater(displacedWaterCells, waterField, scope);
			}
		}
		
		// Update water system after all obstacles are placed
		if (totalCellsAffected > 0) {
			identifyEdgeCells();
		}
		
		System.out.println("Obstacles from shapefile added successfully:");
		System.out.println("  Type: " + obstacleType.name);
		System.out.println("  Obstacles created: " + totalObstaclesCreated);
		System.out.println("  Cells affected: " + totalCellsAffected);
		System.out.println("  Destroyable: " + destroyable);
		if (destroyable) {
			System.out.println("  Destruction time: " + destructionTime + " cycles");
		}
		
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
					// Mark as original water for restoration
					cell.wasOriginalWater = true;
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
			// Store original water elevation
			cell.originalWaterElevation = finalWaterLevel;
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
	
	// === ENHANCED OBSTACLE STATUS UPDATE METHOD ===
	private void updateObstacleStatus(IScope scope, IField waterField) {
		final IAgent agent = getCurrentAgent(scope);
		final double destructionTime = getFloatAttribute(agent, OBSTACLE_DESTRUCTION_TIME);
		final double currentTime = scope.getSimulation().getClock().getCycle();
		final IField obstacleField = (IField) agent.getAttribute(OBSTACLE_FIELD);
		
		List<ObstacleCell> obstaclesToRemove = new ArrayList<>();
		
		for (ObstacleCell obstacleCell : activeObstacles) {
			if (obstacleCell.isDestroyed || !obstacleCell.isDestroyable) continue;
			
			GridCell cell = obstacleCell.gridCell;
			
			// Check if obstacle cell is under water attack (adjacent to water)
			boolean currentlyUnderAttack = false;
			for (GridCell neighbor : cell.neighbors) {
				if (neighbor.isWater) {
					currentlyUnderAttack = true;
					break;
				}
			}
			
			// Update water attack status for individual cells
			if (currentlyUnderAttack && !obstacleCell.isUnderWaterAttack) {
				// Water attack just started
				obstacleCell.isUnderWaterAttack = true;
				obstacleCell.waterAttackStartTime = currentTime;
			} else if (!currentlyUnderAttack && obstacleCell.isUnderWaterAttack) {
				// No longer under attack (water receded)
				obstacleCell.isUnderWaterAttack = false;
				obstacleCell.waterAttackStartTime = -1;
			}
			
			// Check for destruction after sustained water attack
			if (obstacleCell.isUnderWaterAttack) {
				ObstacleStructure obstacle = individualObstacles.get(obstacleCell.obstacleId);
				double destructionTimeToUse = obstacle != null ? obstacle.getDestructionTime() : destructionTime;
				
				if ((currentTime - obstacleCell.waterAttackStartTime) >= destructionTimeToUse) {
					// Destroy the individual obstacle cell
					obstacleCell.isDestroyed = true;
					obstaclesToRemove.add(obstacleCell);
					
					// Handle destruction with individual obstacle system
					handleObstacleCellDestruction(scope, obstacleCell, obstacleField);
				}
			}
		}
		
		// Remove destroyed obstacles
		activeObstacles.removeAll(obstaclesToRemove);
	}
	
	// === HANDLE INDIVIDUAL OBSTACLE CELL DESTRUCTION ===
	private void handleObstacleCellDestruction(IScope scope, ObstacleCell destroyedObstacleCell, IField obstacleField) {
		GridCell cell = destroyedObstacleCell.gridCell;
		int obstacleId = destroyedObstacleCell.obstacleId;
		
		// Remove this obstacle ID from the cell's obstacle set
		Set<Integer> cellObstacles = cellToObstacleIds.get(cell);
		if (cellObstacles != null) {
			cellObstacles.remove(obstacleId);
			
			// If cell has no more obstacles, remove it completely
			if (cellObstacles.isEmpty()) {
				cellToObstacleIds.remove(cell);
				obstacleGridCells.remove(cell);
				
				// Restore original terrain elevation
				cell.terrainElevation = cell.originalTerrainElevation;
				obstacleField.set(scope, cell.x, cell.y, 0.0);
				
			} else {
				// Cell still has other obstacles - recalculate elevation
				double maxElevation = cell.originalTerrainElevation;
				for (int remainingObstacleId : cellObstacles) {
					ObstacleStructure remainingObstacle = individualObstacles.get(remainingObstacleId);
					if (remainingObstacle != null && !remainingObstacle.isDestroyed) {
						Double cellElevation = remainingObstacle.getCellElevation(cell);
						if (cellElevation != null) {
							maxElevation = Math.max(maxElevation, cellElevation);
						}
					}
				}
				
				cell.terrainElevation = maxElevation;
				obstacleField.set(scope, cell.x, cell.y, maxElevation);
			}
		}
		
		// Remove cell from the obstacle structure
		ObstacleStructure obstacle = individualObstacles.get(obstacleId);
		if (obstacle != null) {
			obstacle.obstacleCells.remove(cell);
			obstacle.cellElevations.remove(cell);
		}
	}
	
	// === NEW: ENHANCED WATER FLOW CHECK FOR OBSTACLES ===
	private boolean canWaterFlowToCell(GridCell targetCell, double sourceWaterLevel, double minFlowDiff) {
		// Check if target cell has obstacles
		if (obstacleGridCells.contains(targetCell)) {
			// Get obstacle information for this cell
			Set<Integer> obstacleIds = cellToObstacleIds.get(targetCell);
			if (obstacleIds != null && !obstacleIds.isEmpty()) {
				// Check if any obstacle in this cell is non-destroyable
				for (int obstacleId : obstacleIds) {
					ObstacleStructure obstacle = individualObstacles.get(obstacleId);
					if (obstacle != null && !obstacle.isDestroyed) {
						if (!obstacle.isDestroyable) {
							// Non-destroyable obstacle - water can only flow if it's high enough to go over
							if (sourceWaterLevel <= targetCell.terrainElevation + minFlowDiff) {
								return false; // Water level not high enough to flow over obstacle
							}
						}
						// For destroyable obstacles, water can flow normally (obstacle may be destroyed later)
					}
				}
			}
		}
		
		// Normal flow check
		return (sourceWaterLevel - targetCell.terrainElevation) > minFlowDiff;
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
		
		// Check and update obstacle status
		updateObstacleStatus(scope, waterField);
		
		// 1. SPREAD WATER - only from current edge cells (WITH RAIN EFFECTS AND OBSTACLE HANDLING)
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
		
		// Sequential spreading (modifies neighbor states) - WITH RAIN AND OBSTACLE EFFECTS
		for (GridCell edgeCell : edgeWaterCells) {
			if (edgeCell.waterElevation > edgeCell.terrainElevation + effectiveFlowThreshold) {
				for (GridCell neighbor : edgeCell.neighbors) {
					// Enhanced obstacle checking
					if (!neighbor.isWater && canWaterFlowToCell(neighbor, edgeCell.waterElevation, effectiveMinFlowDiff)) {
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
	
	// === OBSTACLE BUILDING ACTIONS ===
	@action(name = "toggle_obstacle_building_mode", doc = @doc("Toggles obstacle building mode on/off"))
	public Boolean toggleObstacleBuildingMode(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		boolean currentMode = getBoolAttribute(agent, OBSTACLE_BUILDING_MODE);
		setBoolAttribute(agent, OBSTACLE_BUILDING_MODE, !currentMode);
		return !currentMode;
	}
	
	@action(name = "is_obstacle_building_mode", doc = @doc("Returns whether obstacle building mode is active"))
	public Boolean isObstacleBuildingMode(final IScope scope) {
		return getBoolAttribute(getCurrentAgent(scope), OBSTACLE_BUILDING_MODE);
	}
	
	// === OBSTACLE REMOVAL MODE CONTROL ===
	@action(name = "toggle_obstacle_removal_mode", doc = @doc("Toggles obstacle removal mode on/off"))
	public Boolean toggleObstacleRemovalMode(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		boolean currentMode = getBoolAttribute(agent, OBSTACLE_REMOVAL_MODE);
		setBoolAttribute(agent, OBSTACLE_REMOVAL_MODE, !currentMode);
		return !currentMode;
	}
	
	@action(name = "is_obstacle_removal_mode", doc = @doc("Returns whether obstacle removal mode is active"))
	public Boolean isObstacleRemovalMode(final IScope scope) {
		return getBoolAttribute(getCurrentAgent(scope), OBSTACLE_REMOVAL_MODE);
	}
	
	// === ENHANCED INDIVIDUAL OBSTACLE BUILDING METHOD ===
	@action(name = "build_obstacle", args = {
			@arg(name = "point1", type = IType.POINT, doc = @doc("First point of the obstacle")),
			@arg(name = "point2", type = IType.POINT, doc = @doc("Second point of the obstacle")),
			@arg(name = "obstacle_type", type = IType.STRING, optional = true, doc = @doc("Type of obstacle (default: dyke)")),
			@arg(name = "destroyable", type = IType.BOOL, optional = true, doc = @doc("Whether obstacle can be destroyed (default: based on type)")),
			@arg(name = "destruction_time", type = IType.FLOAT, optional = true, doc = @doc("Destruction time in cycles (default: based on type)")) },
			doc = @doc("Builds an individual obstacle between two points with smart overlap handling"))
	public Boolean buildObstacle(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		if (!getBoolAttribute(agent, OBSTACLE_BUILDING_MODE)) {
			return false;
		}
		
		// Get points and parameters
		GamaPoint point1 = (GamaPoint) scope.getArg("point1", IType.POINT);
		GamaPoint point2 = (GamaPoint) scope.getArg("point2", IType.POINT);
		String obstacleTypeStr = scope.hasArg("obstacle_type") ? scope.getStringArg("obstacle_type") : "dyke";
		
		ObstacleType obstacleType = ObstacleType.fromString(obstacleTypeStr);
		boolean destroyable = scope.hasArg("destroyable") ? scope.getBoolArg("destroyable") : obstacleType.destroyable;
		double destructionTime = scope.hasArg("destruction_time") ? scope.getFloatArg("destruction_time") : obstacleType.defaultDestructionTime;
		
		int width = getIntAttribute(agent, GRID_WIDTH);
		int height = getIntAttribute(agent, GRID_HEIGHT);
		IField obstacleField = (IField) agent.getAttribute(OBSTACLE_FIELD);
		IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		
		if (obstacleField == null) {
			System.out.println("ERROR: Obstacle field is null!");
			return false;
		}
		
		// Convert world coordinates to grid coordinates
		int[] coords1 = worldToGrid(point1, scope, width, height);
		int[] coords2 = worldToGrid(point2, scope, width, height);
		
		double obstacleHeight = getFloatAttribute(agent, DEFAULT_OBSTACLE_HEIGHT);
		double currentTime = scope.getSimulation().getClock().getCycle();
		
		// Create individual obstacle structure
		int currentObstacleId = nextObstacleId++;
		ObstacleStructure newObstacle = new ObstacleStructure(currentObstacleId, obstacleType, currentTime, destroyable, destructionTime);
		
		// STEP 1: Get main obstacle line cells
		List<int[]> obstacleCoords = getLineCoordinates(coords1[0], coords1[1], coords2[0], coords2[1]);
		List<GridCell> mainObstacleCells = new ArrayList<>();
		
		for (int[] coord : obstacleCoords) {
			int x = coord[0], y = coord[1];
			if (x >= 0 && x < width && y >= 0 && y < height) {
				GridCell cell = internalGrid[x][y];
				mainObstacleCells.add(cell);
			}
		}
		
		if (mainObstacleCells.isEmpty()) {
			return false;
		}
		
		// STEP 2: Calculate adaptive heights for main obstacle line
		Map<GridCell, Double> obstacleElevations = new HashMap<>();
		
		for (GridCell cell : mainObstacleCells) {
			double adaptiveHeight = calculateAdaptiveObstacleHeight(cell, obstacleHeight, obstacleType);
			obstacleElevations.put(cell, adaptiveHeight);
		}
		
		// STEP 3: Apply smoothing along obstacle line
		smoothObstacleElevations(obstacleElevations, mainObstacleCells);
		
		// STEP 4: Add neighbor cells for continuity and thickness
		Set<GridCell> allObstacleCells = new HashSet<>(mainObstacleCells);
		
		for (GridCell mainCell : mainObstacleCells) {
			double mainHeight = obstacleElevations.get(mainCell);
			
			// Add neighboring cells for thickness
			for (GridCell neighbor : mainCell.neighbors) {
				if (!allObstacleCells.contains(neighbor)) {
					double neighborHeight = calculateNeighborObstacleHeight(neighbor, mainHeight, obstacleHeight, obstacleType);
					obstacleElevations.put(neighbor, neighborHeight);
					allObstacleCells.add(neighbor);
				}
			}
		}
		
		// STEP 5: Build all obstacle cells with individual tracking
		int obstaclesBuilt = 0;
		List<GridCell> displacedWaterCells = new ArrayList<>();
		
		for (GridCell cell : allObstacleCells) {
			double elevation = obstacleElevations.get(cell);
			
			// Handle water displacement if building through water
			if (cell.isWater) {
				displacedWaterCells.add(cell);
				// Track which obstacle displaced this water cell
				newObstacle.addDisplacedWaterCell(cell);
				displaceWaterFromCell(cell, waterField, scope);
			}
			
			// Add to individual obstacle tracking
			newObstacle.addCell(cell, elevation);
			
			// Track which obstacles this cell belongs to
			cellToObstacleIds.computeIfAbsent(cell, k -> new HashSet<>()).add(currentObstacleId);
			
			// Add to global obstacle tracking (for compatibility)
			obstacleGridCells.add(cell);
			
			// Create ObstacleCell for water attack tracking
			ObstacleCell obstacleCell = new ObstacleCell(cell, currentObstacleId, obstacleType, currentTime, destroyable);
			activeObstacles.add(obstacleCell);
			
			// Smart elevation handling for overlaps
			// If cell already has an obstacle, use the higher elevation
			double currentFieldElevation = ((Number) obstacleField.get(scope, cell.x, cell.y)).doubleValue();
			double newElevation = Math.max(currentFieldElevation, elevation);
			
			obstacleField.set(scope, cell.x, cell.y, newElevation);
			cell.terrainElevation = newElevation;
			
			obstaclesBuilt++;
		}
		
		// Store the individual obstacle
		individualObstacles.put(currentObstacleId, newObstacle);
		
		// STEP 6: Update water system after displacement
		if (!displacedWaterCells.isEmpty()) {
			redistributeDisplacedWater(displacedWaterCells, waterField, scope);
			identifyEdgeCells();
		}
		
		System.out.println("Individual obstacle built: Type=" + obstacleType.name + ", ID=" + currentObstacleId + ", " + obstaclesBuilt + " cells, " + 
						  displacedWaterCells.size() + " water cells displaced");
		return obstaclesBuilt > 0;
	}
	
	// === SMART INDIVIDUAL OBSTACLE REMOVAL METHOD ===
	@action(name = "remove_obstacle_area", args = {
			@arg(name = "click_point", type = IType.POINT, doc = @doc("Point where user clicked to remove obstacle area"))
	}, doc = @doc("Removes a specific individual obstacle at the clicked point"))
	public Boolean removeObstacleArea(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		if (!getBoolAttribute(agent, OBSTACLE_REMOVAL_MODE)) {
			return false;
		}
		
		// Get click point and convert to grid coordinates
		GamaPoint clickPoint = (GamaPoint) scope.getArg("click_point", IType.POINT);
		int width = getIntAttribute(agent, GRID_WIDTH);
		int height = getIntAttribute(agent, GRID_HEIGHT);
		
		int[] coords = worldToGrid(clickPoint, scope, width, height);
		int x = coords[0], y = coords[1];
		
		// Check if clicked cell is within bounds and is an obstacle cell
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}
		
		GridCell clickedCell = internalGrid[x][y];
		if (!obstacleGridCells.contains(clickedCell)) {
			System.out.println("Clicked point is not an obstacle cell");
			return false;
		}
		
		// Find which specific obstacle to remove
		Set<Integer> obstaclesAtCell = cellToObstacleIds.get(clickedCell);
		if (obstaclesAtCell == null || obstaclesAtCell.isEmpty()) {
			System.out.println("No individual obstacles found at clicked location");
			return false;
		}
		
		// Find the most recent (highest ID) active obstacle at this location
		int obstacleToRemove = -1;
		for (int obstacleId : obstaclesAtCell) {
			ObstacleStructure obstacle = individualObstacles.get(obstacleId);
			if (obstacle != null && !obstacle.isDestroyed) {
				obstacleToRemove = Math.max(obstacleToRemove, obstacleId);
			}
		}
		
		if (obstacleToRemove == -1) {
			System.out.println("No active obstacles found at clicked location");
			return false;
		}
		
		// Remove the specific obstacle
		int removedCount = removeSpecificObstacle(scope, obstacleToRemove);
		
		System.out.println("Individual obstacle removed: ID=" + obstacleToRemove + ", " + removedCount + " cells affected");
		return removedCount > 0;
	}
	
	// === REMOVE SPECIFIC OBSTACLE METHOD WITH ENHANCED WATER RESTORATION ===
	private int removeSpecificObstacle(IScope scope, int obstacleId) {
		final IAgent agent = getCurrentAgent(scope);
		final IField obstacleField = (IField) agent.getAttribute(OBSTACLE_FIELD);
		final IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		
		ObstacleStructure obstacleToRemove = individualObstacles.get(obstacleId);
		if (obstacleToRemove == null || obstacleToRemove.isDestroyed) {
			return 0;
		}
		
		// Mark obstacle as destroyed
		obstacleToRemove.isDestroyed = true;
		
		int removedCount = 0;
		Set<GridCell> cellsToUpdate = new HashSet<>();
		List<ObstacleCell> obstacleCellsToRemove = new ArrayList<>();
		Set<GridCell> waterCellsToRestore = new HashSet<>();
		
		for (GridCell cell : obstacleToRemove.obstacleCells) {
			// Remove this obstacle ID from the cell's obstacle set
			Set<Integer> cellObstacles = cellToObstacleIds.get(cell);
			if (cellObstacles != null) {
				cellObstacles.remove(obstacleId);
				
				// If cell has no more obstacles, remove it completely
				if (cellObstacles.isEmpty()) {
					cellToObstacleIds.remove(cell);
					obstacleGridCells.remove(cell);
					
					// Restore original terrain elevation
					cell.terrainElevation = cell.originalTerrainElevation;
					obstacleField.set(scope, cell.x, cell.y, 0.0);
					cellsToUpdate.add(cell);
					
					// Check if this cell should be restored as water
					if (cell.wasOriginalWater) {
						waterCellsToRestore.add(cell);
					}
					
				} else {
					// Cell still has other obstacles - recalculate elevation
					double maxElevation = cell.originalTerrainElevation;
					for (int remainingObstacleId : cellObstacles) {
						ObstacleStructure remainingObstacle = individualObstacles.get(remainingObstacleId);
						if (remainingObstacle != null && !remainingObstacle.isDestroyed) {
							Double cellElevation = remainingObstacle.getCellElevation(cell);
							if (cellElevation != null) {
								maxElevation = Math.max(maxElevation, cellElevation);
							}
						}
					}
					
					cell.terrainElevation = maxElevation;
					obstacleField.set(scope, cell.x, cell.y, maxElevation);
				}
			}
			
			// Remove corresponding ObstacleCell objects
			for (ObstacleCell oc : activeObstacles) {
				if (oc.obstacleId == obstacleId && oc.gridCell == cell && !oc.isDestroyed) {
					oc.isDestroyed = true;
					obstacleCellsToRemove.add(oc);
				}
			}
			
			removedCount++;
		}
		
		// Remove destroyed obstacle cells from active list
		activeObstacles.removeAll(obstacleCellsToRemove);
		
		// Enhanced water restoration for rivers
		if (!waterCellsToRestore.isEmpty()) {
			restoreOriginalWaterCells(scope, waterCellsToRestore, waterField);
		}
		
		// Check if water should flow into newly opened areas
		if (!cellsToUpdate.isEmpty()) {
			handleWaterFlowAfterObstacleRemoval(scope, cellsToUpdate, waterField);
		}
		
		return removedCount;
	}
	
	// === RESTORE ORIGINAL WATER CELLS METHOD ===
	private void restoreOriginalWaterCells(IScope scope, Set<GridCell> cellsToRestore, IField waterField) {
		List<GridCell> newWaterCells = new ArrayList<>();
		
		for (GridCell cell : cellsToRestore) {
			if (!cell.isWater && cell.wasOriginalWater) {
				// Restore as water cell
				cell.isWater = true;
				cell.waterElevation = cell.originalWaterElevation;
				activeWaterCells.add(cell);
				newWaterCells.add(cell);
				
				// Update water field
				if (waterField != null) {
					waterField.set(scope, cell.x, cell.y, cell.waterElevation);
				}
				
				System.out.println("Restored original water cell at (" + cell.x + "," + cell.y + ")");
			}
		}
		
		// If we restored water cells, need to recalculate water network
		if (!newWaterCells.isEmpty()) {
			// Reconnect water network and equalize levels
			reconnectWaterNetwork(scope, newWaterCells, waterField);
			// Recalculate edge cells
			identifyEdgeCells();
			
			System.out.println("Restored " + newWaterCells.size() + " original water cells");
		}
	}
	
	// === RECONNECT WATER NETWORK METHOD ===
	private void reconnectWaterNetwork(IScope scope, List<GridCell> newWaterCells, IField waterField) {
		// Find connected water regions and equalize water levels
		Set<GridCell> processedCells = new HashSet<>();
		
		for (GridCell newWaterCell : newWaterCells) {
			if (!processedCells.contains(newWaterCell)) {
				// Find all connected water cells using BFS
				Set<GridCell> connectedRegion = findConnectedWaterRegion(newWaterCell);
				processedCells.addAll(connectedRegion);
				
				// Calculate average water level for this region
				double totalWaterLevel = 0.0;
				int count = 0;
				
				for (GridCell cell : connectedRegion) {
					totalWaterLevel += cell.waterElevation;
					count++;
				}
				
				if (count > 0) {
					double averageLevel = totalWaterLevel / count;
					
					// Apply average level to all cells in this region
					for (GridCell cell : connectedRegion) {
						cell.waterElevation = averageLevel;
						if (waterField != null) {
							waterField.set(scope, cell.x, cell.y, cell.waterElevation);
						}
					}
				}
			}
		}
	}
	
	// === FIND CONNECTED WATER REGION METHOD ===
	private Set<GridCell> findConnectedWaterRegion(GridCell startCell) {
		Set<GridCell> region = new HashSet<>();
		Queue<GridCell> queue = new LinkedList<>();
		
		queue.add(startCell);
		region.add(startCell);
		
		while (!queue.isEmpty()) {
			GridCell current = queue.poll();
			
			for (GridCell neighbor : current.neighbors) {
				if (neighbor.isWater && !region.contains(neighbor)) {
					region.add(neighbor);
					queue.add(neighbor);
				}
			}
		}
		
		return region;
	}
	
	// === GET SPECIFIC OBSTACLE AT LOCATION ===
	@action(name = "get_obstacle_area_size", args = {
			@arg(name = "click_point", type = IType.POINT, doc = @doc("Point to analyze obstacle area size"))
	}, doc = @doc("Returns the size of the top obstacle at the clicked point"))
	public Integer getObstacleAreaSize(final IScope scope) throws GamaRuntimeException {
		GamaPoint clickPoint = (GamaPoint) scope.getArg("click_point", IType.POINT);
		int width = getIntAttribute(getCurrentAgent(scope), GRID_WIDTH);
		int height = getIntAttribute(getCurrentAgent(scope), GRID_HEIGHT);
		
		int[] coords = worldToGrid(clickPoint, scope, width, height);
		int x = coords[0], y = coords[1];
		
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return 0;
		}
		
		GridCell clickedCell = internalGrid[x][y];
		if (!obstacleGridCells.contains(clickedCell)) {
			return 0;
		}
		
		// Find the most recent active obstacle at this location
		Set<Integer> obstaclesAtCell = cellToObstacleIds.get(clickedCell);
		if (obstaclesAtCell == null || obstaclesAtCell.isEmpty()) {
			return 0;
		}
		
		int topObstacleId = -1;
		for (int obstacleId : obstaclesAtCell) {
			ObstacleStructure obstacle = individualObstacles.get(obstacleId);
			if (obstacle != null && !obstacle.isDestroyed) {
				topObstacleId = Math.max(topObstacleId, obstacleId);
			}
		}
		
		if (topObstacleId == -1) {
			return 0;
		}
		
		ObstacleStructure topObstacle = individualObstacles.get(topObstacleId);
		return topObstacle != null ? topObstacle.getSize() : 0;
	}
	
	// === DEBUGGING AND INFO ACTIONS ===
	@action(name = "get_obstacle_info_at_point", args = {
			@arg(name = "click_point", type = IType.POINT, doc = @doc("Point to get obstacle information"))
	}, doc = @doc("Returns detailed information about obstacles at the clicked point"))
	public String getObstacleInfoAtPoint(final IScope scope) throws GamaRuntimeException {
		GamaPoint clickPoint = (GamaPoint) scope.getArg("click_point", IType.POINT);
		int width = getIntAttribute(getCurrentAgent(scope), GRID_WIDTH);
		int height = getIntAttribute(getCurrentAgent(scope), GRID_HEIGHT);
		
		int[] coords = worldToGrid(clickPoint, scope, width, height);
		int x = coords[0], y = coords[1];
		
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return "Out of bounds";
		}
		
		GridCell clickedCell = internalGrid[x][y];
		if (!obstacleGridCells.contains(clickedCell)) {
			return "No obstacle at this location";
		}
		
		Set<Integer> obstaclesAtCell = cellToObstacleIds.get(clickedCell);
		if (obstaclesAtCell == null || obstaclesAtCell.isEmpty()) {
			return "Obstacle cell found but no individual obstacle records";
		}
		
		StringBuilder info = new StringBuilder();
		info.append("Obstacles at this location: ");
		for (int obstacleId : obstaclesAtCell) {
			ObstacleStructure obstacle = individualObstacles.get(obstacleId);
			if (obstacle != null) {
				info.append("ID=").append(obstacleId);
				info.append(" Type=").append(obstacle.type.name);
				info.append("(").append(obstacle.isDestroyed ? "destroyed" : "active").append(") ");
			}
		}
		
		return info.toString();
	}
	
	// === ENHANCED WATER FLOW AFTER OBSTACLE REMOVAL ===
	private void handleWaterFlowAfterObstacleRemoval(IScope scope, Set<GridCell> removedObstacles, IField waterField) {
		// Check if any removed obstacle cells are adjacent to water
		Set<GridCell> potentialFlowCells = new HashSet<>();
		
		for (GridCell removedObstacle : removedObstacles) {
			// Skip if this was already restored as original water
			if (removedObstacle.isWater) {
				continue;
			}
			
			// Check if this cell should become a water cell
			for (GridCell neighbor : removedObstacle.neighbors) {
				if (neighbor.isWater) {
					// There's water adjacent to this removed obstacle
					// The removed obstacle cell might become flooded
					double waterLevel = neighbor.waterElevation;
					if (waterLevel > removedObstacle.terrainElevation + getFloatAttribute(getCurrentAgent(scope), FLOW_THRESHOLD)) {
						potentialFlowCells.add(removedObstacle);
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
	
	// === OBSTACLE BUILDING HELPER METHODS ===
	
	// Helper method: Calculate adaptive obstacle height
	private double calculateAdaptiveObstacleHeight(GridCell cell, double standardObstacleHeight, ObstacleType type) {
		// 1. Local terrain average (prevents floating walls)
		double localAvgTerrain = getLocalTerrainAverage(cell, 3);
		
		// 2. Terrain-based height
		double terrainBasedHeight;
		if (type.followTerrain) {
			terrainBasedHeight = localAvgTerrain + standardObstacleHeight;
		} else {
			terrainBasedHeight = standardObstacleHeight; // Absolute elevation
		}
		
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
	
	// Helper method: Smooth obstacle elevations (like water initialization)
	private void smoothObstacleElevations(Map<GridCell, Double> elevations, List<GridCell> obstacleList) {
		// Apply smoothing iterations
		for (int iteration = 0; iteration < 3; iteration++) {
			Map<GridCell, Double> smoothedElevations = new HashMap<>();
			
			for (GridCell cell : obstacleList) {
				double sum = elevations.get(cell);
				int count = 1;
				
				// Average with neighboring obstacle cells
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
	
	// Helper method: Calculate neighbor obstacle height
	private double calculateNeighborObstacleHeight(GridCell neighbor, double mainHeight, double standardObstacleHeight, ObstacleType type) {
		// Supporting cells are 70% of main obstacle height
		double reductionFactor = 0.7;
		double neighborLocalTerrain = getLocalTerrainAverage(neighbor, 2);
		
		// Ensure neighbor is still effective but not as tall
		double minHeight;
		if (type.followTerrain) {
			minHeight = neighborLocalTerrain + (standardObstacleHeight * reductionFactor);
		} else {
			minHeight = standardObstacleHeight * reductionFactor;
		}
		double adaptiveHeight = mainHeight * reductionFactor;
		
		return Math.max(minHeight, adaptiveHeight);
	}
	
	// Helper method: Displace water from cell - ENHANCED WITH TRACKING
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
					.filter(n -> n.isWater && !obstacleGridCells.contains(n))
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
	
	// === ENHANCED OBSTACLE STATUS ACTIONS ===
	@action(name = "get_active_obstacle_count", doc = @doc("Returns the number of active individual obstacles"))
	public Integer getActiveObstacleCount(final IScope scope) {
		return (int) individualObstacles.values().stream().filter(o -> !o.isDestroyed).count();
	}
	
	@action(name = "get_total_obstacle_cells", doc = @doc("Returns the total number of obstacle cells (including overlaps)"))
	public Integer getTotalObstacleCells(final IScope scope) {
		return obstacleGridCells.size();
	}
	
	@action(name = "get_obstacles_under_attack", doc = @doc("Returns the number of obstacles currently under water attack"))
	public Integer getObstaclesUnderAttack(final IScope scope) {
		return (int) activeObstacles.stream().filter(o -> !o.isDestroyed && o.isUnderWaterAttack).count();
	}
	
	@action(name = "clear_all_obstacles", doc = @doc("Removes all obstacles from the simulation"))
	public Boolean clearAllObstacles(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		final IField obstacleField = (IField) agent.getAttribute(OBSTACLE_FIELD);
		final IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		final int width = getIntAttribute(agent, GRID_WIDTH);
		final int height = getIntAttribute(agent, GRID_HEIGHT);
		
		// Track cells that should be restored as water
		Set<GridCell> waterCellsToRestore = new HashSet<>();
		
		// Restore terrain elevations for all obstacle cells
		for (GridCell cell : obstacleGridCells) {
			cell.terrainElevation = cell.originalTerrainElevation;
			if (obstacleField != null) {
				obstacleField.set(scope, cell.x, cell.y, 0.0);
			}
			
			// Check if this cell should be restored as water
			if (cell.wasOriginalWater) {
				waterCellsToRestore.add(cell);
			}
		}
		
		// Clear all data structures
		individualObstacles.clear();
		cellToObstacleIds.clear();
		activeObstacles.clear();
		obstacleGridCells.clear();
		nextObstacleId = 1; // Reset ID counter
		
		// Restore original water cells
		if (!waterCellsToRestore.isEmpty()) {
			restoreOriginalWaterCells(scope, waterCellsToRestore, waterField);
			System.out.println("All obstacles cleared and " + waterCellsToRestore.size() + " original water cells restored");
		} else {
			System.out.println("All individual obstacles cleared");
		}
		
		return true;
	}
	
	// === EMERGENCY CLEAR ALL OBSTACLES METHOD ===
	@action(name = "emergency_clear_all_obstacles", doc = @doc("Emergency removal of all obstacles with immediate water restoration and normalization"))
	public Boolean emergencyClearAllObstacles(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		final IField obstacleField = (IField) agent.getAttribute(OBSTACLE_FIELD);
		final IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		final int width = getIntAttribute(agent, GRID_WIDTH);
		final int height = getIntAttribute(agent, GRID_HEIGHT);
		
		System.out.println("EMERGENCY: Clearing all obstacles and restoring original water topology...");
		
		// Stop simulation during emergency clear
		boolean wasActive = getBoolAttribute(agent, SIMULATION_ACTIVE);
		setBoolAttribute(agent, SIMULATION_ACTIVE, false);
		
		// Track cells that should be restored as water
		Set<GridCell> waterCellsToRestore = new HashSet<>();
		
		// Restore terrain elevations for all obstacle cells
		for (GridCell cell : obstacleGridCells) {
			cell.terrainElevation = cell.originalTerrainElevation;
			if (obstacleField != null) {
				obstacleField.set(scope, cell.x, cell.y, 0.0);
			}
			
			// Check if this cell should be restored as water
			if (cell.wasOriginalWater) {
				waterCellsToRestore.add(cell);
			}
		}
		
		// Clear all data structures
		individualObstacles.clear();
		cellToObstacleIds.clear();
		activeObstacles.clear();
		obstacleGridCells.clear();
		nextObstacleId = 1; // Reset ID counter
		
		// Restore original water cells
		if (!waterCellsToRestore.isEmpty()) {
			restoreOriginalWaterCells(scope, waterCellsToRestore, waterField);
			
			// EMERGENCY: Additional water level normalization
			normalizeAllWaterLevels(scope, waterField);
			
			System.out.println("EMERGENCY CLEAR COMPLETE: " + waterCellsToRestore.size() + " water cells restored, levels normalized");
		} else {
			System.out.println("EMERGENCY CLEAR COMPLETE: All obstacles removed, no water restoration needed");
		}
		
		// Restart simulation if it was active
		if (wasActive) {
			setBoolAttribute(agent, SIMULATION_ACTIVE, true);
		}
		
		return true;
	}
	
	// === NORMALIZE ALL WATER LEVELS METHOD ===
	private void normalizeAllWaterLevels(IScope scope, IField waterField) {
		if (activeWaterCells.isEmpty()) {
			return;
		}
		
		// Find all connected water regions and normalize each separately
		Set<GridCell> processedCells = new HashSet<>();
		int regionsNormalized = 0;
		
		for (GridCell cell : activeWaterCells) {
			if (!processedCells.contains(cell)) {
				// Find connected region
				Set<GridCell> region = findConnectedWaterRegion(cell);
				processedCells.addAll(region);
				
				if (region.size() > 1) {
					// Calculate average water level for the region
					double totalLevel = 0.0;
					for (GridCell regionCell : region) {
						totalLevel += regionCell.waterElevation;
					}
					double averageLevel = totalLevel / region.size();
					
					// Apply normalized level to all cells in region
					for (GridCell regionCell : region) {
						regionCell.waterElevation = averageLevel;
						if (waterField != null) {
							waterField.set(scope, regionCell.x, regionCell.y, regionCell.waterElevation);
						}
					}
					
					regionsNormalized++;
				}
			}
		}
		
		// Recalculate edge cells after normalization
		identifyEdgeCells();
		
		System.out.println("Water normalization complete: " + regionsNormalized + " regions normalized");
	}
	
	// === SIMULATION CONTROL ACTIONS ===
	@action(name = "start_spreading_simulation", doc = @doc("Starts the spreading simulation"))
	public Boolean startSpreadingSimulation(final IScope scope){
		final IAgent agent = getCurrentAgent(scope);
		setBoolAttribute(agent, SIMULATION_ACTIVE, true);
		System.out.println("Simulation started with " + activeWaterCells.size() + " water cells and individual obstacle tracking");
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
		
		// Clear obstacles during reset (using enhanced method)
		clearAllObstacles(scope);
		setBoolAttribute(agent, OBSTACLE_BUILDING_MODE, false);
		setBoolAttribute(agent, OBSTACLE_REMOVAL_MODE, false);
		
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
					// Restore original water tracking
					cell.wasOriginalWater = true;
					activeWaterCells.add(cell);
					totalWaterTerrainElev += cell.terrainElevation;
					waterCellCount++;
				} else {
					cell.isWater = false;
					cell.waterElevation = 0.0;
					cell.isEdgeCell = false;
					// Clear original water tracking if not water
					cell.wasOriginalWater = false;
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
			// Store original water elevation
			cell.originalWaterElevation = uniformWaterLevel;
			if (waterField != null) {
				waterField.set(scope, cell.x, cell.y, cell.waterElevation);
			}
		}
		
		// Rebuild initial edge list
		identifyEdgeCells();
		System.out.println("Reset complete with enhanced individual obstacle tracking system: " + activeWaterCells.size() + " water cells restored");
		
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