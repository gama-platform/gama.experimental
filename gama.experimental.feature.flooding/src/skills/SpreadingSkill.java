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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		@variable(name = "obstacle_destruction_time", type = IType.FLOAT, init = "10.0", doc = @doc("Time in cycles before obstacle cell under water attack is destroyed")),
		// C++ ENGINE VARIABLES
		@variable(name = "use_cpp_engine", type = IType.BOOL, init = "false", doc = @doc("Whether to use C++ engine for spreading simulation")),
		@variable(name = "cpp_engine_path", type = IType.STRING, init = "", doc = @doc("Path to the C++ spreading engine binary"))
})
@skill(name = "spreading", concept = { "spreading", "simulation", "water", "flood", "rain", "obstacles", "buildings", "cpp", "acceleration" }, 
       doc = @doc("A skill for managing spreading simulations with optimized water flow mechanics, rain system, generic obstacle management, and optional C++ acceleration"))
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
	// C++ ENGINE CONSTANTS
	public static final String USE_CPP_ENGINE = "use_cpp_engine";
	public static final String CPP_ENGINE_PATH = "cpp_engine_path";
	
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
		public final double waterResistance;
		
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
			return DYKE;
		}
	}
	
	// === INTERNAL GRID CELL CLASS ===
	public static class GridCell {
		public int x, y;
		public boolean isWater;
		public double waterElevation;
		public double terrainElevation;
		public double originalTerrainElevation;
		public boolean isEdgeCell;
		public List<GridCell> neighbors;
		public IShape shape;
		private Map<String, Object> attributes;
		
		public boolean wasOriginalWater;
		public double originalWaterElevation;
		
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
		public Map<GridCell, Double> cellElevations;
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
			this.displacedWaterCells = new HashSet<>();
			this.isDestroyable = destroyable;
			this.customDestructionTime = destructionTime;
		}
		
		public void addCell(GridCell cell, double elevation) {
			obstacleCells.add(cell);
			cellElevations.put(cell, elevation);
		}
		
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
	
	// === ENHANCED OBSTACLE CELL CLASS ===
	public static class ObstacleCell {
		public GridCell gridCell;
		public int obstacleId;
		public ObstacleType type;
		public double creationTime;
		public double waterAttackStartTime;
		public boolean isUnderWaterAttack;
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
	
	// === C++ ENGINE INTERFACE ===
	private static class CppEngineInterface {
		private Process process;
		private BufferedReader reader;
		private BufferedWriter writer;
		private String enginePath;
		
		public CppEngineInterface(String path) throws IOException {
			this.enginePath = path;
			startProcess();
		}
		
		private void startProcess() throws IOException {
			ProcessBuilder pb = new ProcessBuilder(enginePath);
			pb.redirectErrorStream(false);
			process = pb.start();
			
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			
			System.out.println("C++ engine process started (PID: " + process.pid() + ")");
		}
		
		public synchronized String executeCommand(String command, String... args) throws IOException {
			if (process == null || !process.isAlive()) {
				System.err.println("C++ process died, restarting...");
				startProcess();
			}
			
			StringBuilder cmdLine = new StringBuilder(command);
			for (String arg : args) {
				cmdLine.append(" ").append(arg);
			}
			
			writer.write(cmdLine.toString());
			writer.newLine();
			writer.flush();
			
			String response = reader.readLine();
			
			if (response == null) {
				throw new IOException("C++ process terminated unexpectedly");
			}
			
			return response.trim();
		}
		
		public void shutdown() {
			try {
				if (writer != null) {
					executeCommand("quit");
					writer.close();
				}
				if (reader != null) {
					reader.close();
				}
				if (process != null && process.isAlive()) {
					process.destroy();
					process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
					if (process.isAlive()) {
						process.destroyForcibly();
					}
				}
			} catch (Exception e) {
				System.err.println("Error shutting down C++ engine: " + e.getMessage());
			}
		}
	}
	
	// === INTERNAL DATA STRUCTURES ===
	private GridCell[][] internalGrid;
	private List<GridCell> activeWaterCells;
	private HashSet<GridCell> edgeWaterCells;
	private List<ObstacleCell> activeObstacles;
	private HashSet<GridCell> obstacleGridCells;
	private Map<IAgent, SpreadingSkill> skillInstances = new ConcurrentHashMap<>();
	
	private int nextObstacleId = 1;
	private Map<Integer, ObstacleStructure> individualObstacles;
	private Map<GridCell, Set<Integer>> cellToObstacleIds;
	
	private CppEngineInterface cppEngine;
	
	// === ENHANCED CONSTRUCTOR ===
	public SpreadingSkill() {
		super();
		this.activeWaterCells = new ArrayList<>();
		this.edgeWaterCells = new HashSet<>();
		this.activeObstacles = new ArrayList<>();
		this.obstacleGridCells = new HashSet<>();
		this.individualObstacles = new HashMap<>();
		this.cellToObstacleIds = new HashMap<>();
		
		// Add shutdown hook for C++ engine cleanup
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (cppEngine != null) {
				try {
					System.out.println("Shutdown hook: Cleaning up C++ engine...");
					cppEngine.shutdown();
				} catch (Exception e) {
					System.err.println("Error in shutdown hook: " + e.getMessage());
				}
			}
		}));
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
	
	// === GRID INITIALIZATION WITH OBSTACLE FIELD ===
	@action(name = "initialize_spreading_grid_with_obstacle_field", args = {
			@arg(name = "dem_field", type = IType.MATRIX, doc = @doc("Digital elevation model field")),
			@arg(name = "obstacle_field", type = IType.MATRIX, doc = @doc("Pre-created obstacle field with correct coordinate system")),
			@arg(name = "water_geometries", type = IType.LIST, doc = @doc("List of water polygon geometries")),
			@arg(name = "initial_water_depth", type = IType.FLOAT, optional = true, doc = @doc("Initial water depth (default: 1.5m)")),
			@arg(name = "flow_threshold", type = IType.FLOAT, optional = true, doc = @doc("Flow threshold parameter")),
			@arg(name = "rising_rate", type = IType.FLOAT, optional = true, doc = @doc("Rising rate parameter")),
			@arg(name = "min_flow_diff", type = IType.FLOAT, optional = true, doc = @doc("Minimum flow difference parameter")),
			@arg(name = "equalization_threshold", type = IType.FLOAT, optional = true, doc = @doc("Equalization threshold parameter")) }, 
			doc = @doc("Initializes the spreading grid with DEM and pre-created obstacle field"))
	public Boolean initializeSpreadingGridWithObstacleField(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		final IField demField = (IField) scope.getArg("dem_field", IType.FIELD);
		final IField obstacleField = (IField) scope.getArg("obstacle_field", IType.FIELD);
		final IList<IShape> waterGeometries = scope.getListArg("water_geometries");
		final Double initialWaterDepth = scope.hasArg("initial_water_depth")
				? scope.getFloatArg("initial_water_depth")
				: 1.5;
		
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
		
		int gridWidth = demField.getCols(scope);
		int gridHeight = demField.getRows(scope);
		setIntAttribute(agent, GRID_WIDTH, gridWidth);
		setIntAttribute(agent, GRID_HEIGHT, gridHeight);
		
		internalGrid = new GridCell[gridWidth][gridHeight];
		activeWaterCells.clear();
		edgeWaterCells.clear();
		activeObstacles.clear();
		obstacleGridCells.clear();
		individualObstacles.clear();
		cellToObstacleIds.clear();
		nextObstacleId = 1;
		
		IField waterField = GamaFieldType.withObject(scope, 0.0, gridWidth, gridHeight, Types.FLOAT);
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				waterField.set(scope, i, j, 0.0);
			}
		}
		
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				obstacleField.set(scope, i, j, 0.0);
			}
		}
		
		initializeGridCells(scope, demField, waterGeometries, initialWaterDepth, waterField);
		
		agent.setAttribute(WATER_FIELD, waterField);
		agent.setAttribute(OBSTACLE_FIELD, obstacleField);
		
		System.out.println("Grid initialized successfully with coordinate-aligned obstacle field and individual obstacle tracking:");
		System.out.println("  Grid dimensions: " + gridWidth + "x" + gridHeight);
		System.out.println("  Water cells: " + activeWaterCells.size());
		System.out.println("  Individual obstacle management system ready");
		
		return true;
	}
	
	// === COMBINED INITIALIZATION WITH C++ ===
	@action(name = "initialize_spreading_with_cpp", args = {
			@arg(name = "dem_field", type = IType.MATRIX, doc = @doc("Digital elevation model field")),
			@arg(name = "obstacle_field", type = IType.MATRIX, doc = @doc("Pre-created obstacle field with correct coordinate system")),
			@arg(name = "water_geometries", type = IType.LIST, doc = @doc("List of water polygon geometries")),
			@arg(name = "initial_water_depth", type = IType.FLOAT, optional = true, doc = @doc("Initial water depth (default: 1.5m)")),
			@arg(name = "use_cpp", type = IType.BOOL, optional = true, doc = @doc("Whether to use C++ engine (default: true)")),
			@arg(name = "cpp_engine_path", type = IType.STRING, optional = true, doc = @doc("Path to C++ engine binary (default: ./spreading_engine)")),
			@arg(name = "flow_threshold", type = IType.FLOAT, optional = true, doc = @doc("Flow threshold parameter")),
			@arg(name = "rising_rate", type = IType.FLOAT, optional = true, doc = @doc("Rising rate parameter")),
			@arg(name = "min_flow_diff", type = IType.FLOAT, optional = true, doc = @doc("Minimum flow difference parameter")),
			@arg(name = "equalization_threshold", type = IType.FLOAT, optional = true, doc = @doc("Equalization threshold parameter"))
	}, doc = @doc("Initializes the spreading grid and optionally the C++ engine in one call"))
	public Boolean initializeSpreadingWithCpp(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		System.out.println("=== INITIALIZATION START ===");
		
		System.out.println("Step 1/3: Initializing Java grid...");
		
		final IField demField = (IField) scope.getArg("dem_field", IType.FIELD);
		final IField obstacleField = (IField) scope.getArg("obstacle_field", IType.FIELD);
		final IList<IShape> waterGeometries = scope.getListArg("water_geometries");
		final Double initialWaterDepth = scope.hasArg("initial_water_depth")
				? scope.getFloatArg("initial_water_depth")
				: 1.5;
		
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
		
		int gridWidth = demField.getCols(scope);
		int gridHeight = demField.getRows(scope);
		setIntAttribute(agent, GRID_WIDTH, gridWidth);
		setIntAttribute(agent, GRID_HEIGHT, gridHeight);
		
		internalGrid = new GridCell[gridWidth][gridHeight];
		activeWaterCells.clear();
		edgeWaterCells.clear();
		activeObstacles.clear();
		obstacleGridCells.clear();
		individualObstacles.clear();
		cellToObstacleIds.clear();
		nextObstacleId = 1;
		
		IField waterField = GamaFieldType.withObject(scope, 0.0, gridWidth, gridHeight, Types.FLOAT);
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				waterField.set(scope, i, j, 0.0);
			}
		}
		
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				obstacleField.set(scope, i, j, 0.0);
			}
		}
		
		initializeGridCells(scope, demField, waterGeometries, initialWaterDepth, waterField);
		
		agent.setAttribute(WATER_FIELD, waterField);
		agent.setAttribute(OBSTACLE_FIELD, obstacleField);
		
		System.out.println("  Grid initialized: " + gridWidth + "x" + gridHeight);
		System.out.println("  Water cells: " + activeWaterCells.size());
		System.out.println("  Edge cells: " + edgeWaterCells.size());
		
		boolean useCpp = scope.hasArg("use_cpp") ? scope.getBoolArg("use_cpp") : true;
		
		if (useCpp) {
			System.out.println("Step 2/3: Initializing C++ engine...");
			
			String enginePath = scope.hasArg("cpp_engine_path") 
				? scope.getStringArg("cpp_engine_path")
				: "./spreading_engine";
			
			Path path = Paths.get(enginePath);
			if (!Files.exists(path)) {
				System.err.println("  WARNING: C++ engine binary not found at: " + enginePath);
				System.err.println("  Falling back to Java implementation");
				setBoolAttribute(agent, USE_CPP_ENGINE, false);
			} else {
				try {
					if (cppEngine != null) {
						cppEngine.shutdown();
					}
					
					cppEngine = new CppEngineInterface(enginePath);
					
					String result = cppEngine.executeCommand("init", 
						String.valueOf(gridWidth), String.valueOf(gridHeight));
					
					if (!result.startsWith("OK")) {
						System.err.println("  Failed to initialize C++ engine: " + result);
						System.err.println("  Falling back to Java implementation");
						setBoolAttribute(agent, USE_CPP_ENGINE, false);
					} else {
						System.out.println("  Syncing terrain to C++...");
						syncTerrainToCpp();
						
						System.out.println("  Syncing water to C++...");
						syncWaterToCpp();
						
						System.out.println("  Syncing obstacles to C++...");
						syncObstaclesToCpp();
						
						System.out.println("  Finalizing C++ engine...");
						result = cppEngine.executeCommand("finalize");
						
						if (!result.startsWith("OK")) {
							System.err.println("  Failed to finalize C++ engine: " + result);
							System.err.println("  Falling back to Java implementation");
							setBoolAttribute(agent, USE_CPP_ENGINE, false);
						} else {
							agent.setAttribute(CPP_ENGINE_PATH, enginePath);
							setBoolAttribute(agent, USE_CPP_ENGINE, true);
							
							System.out.println("  C++ engine initialized successfully!");
							System.out.println("  Engine path: " + enginePath);
						}
					}
					
				} catch (Exception e) {
					System.err.println("  Error initializing C++ engine: " + e.getMessage());
					System.err.println("  Falling back to Java implementation");
					e.printStackTrace();
					setBoolAttribute(agent, USE_CPP_ENGINE, false);
				}
			}
		} else {
			System.out.println("Step 2/3: C++ engine disabled, using Java implementation");
			setBoolAttribute(agent, USE_CPP_ENGINE, false);
		}
		
		System.out.println("Step 3/3: Initialization complete");
		boolean usingCpp = getBoolAttribute(agent, USE_CPP_ENGINE);
		System.out.println("  Engine mode: " + (usingCpp ? "C++" : "Java"));
		System.out.println("=== INITIALIZATION COMPLETE ===");
		
		return true;
	}
	
	// === C++ ENGINE INITIALIZATION ===
	@action(name = "initialize_cpp_engine", args = {
		@arg(name = "engine_path", type = IType.STRING, optional = true, 
			 doc = @doc("Path to C++ engine binary (default: ./spreading_engine)"))
	}, doc = @doc("Initializes the C++ spreading engine"))
	public Boolean initializeCppEngine(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		if (internalGrid == null) {
			System.err.println("ERROR: Grid not initialized! Call initialize_spreading_grid first.");
			System.err.println("You must call initialize_spreading_grid() or initialize_spreading_grid_with_obstacle_field() before initialize_cpp_engine()");
			return false;
		}
		
		String enginePath = scope.hasArg("engine_path") 
			? scope.getStringArg("engine_path")
			: "./spreading_engine";
		
		Path path = Paths.get(enginePath);
		if (!Files.exists(path)) {
			System.err.println("C++ engine binary not found at: " + enginePath);
			System.err.println("Please build the C++ engine first using build.sh");
			return false;
		}
		
		try {
			if (cppEngine != null) {
				cppEngine.shutdown();
			}
			
			cppEngine = new CppEngineInterface(enginePath);
			
			int width = getIntAttribute(agent, GRID_WIDTH);
			int height = getIntAttribute(agent, GRID_HEIGHT);
			
			if (width <= 0 || height <= 0) {
				System.err.println("ERROR: Invalid grid dimensions: " + width + "x" + height);
				return false;
			}
			
			String result = cppEngine.executeCommand("init", 
				String.valueOf(width), String.valueOf(height));
			
			if (!result.startsWith("OK")) {
				System.err.println("Failed to initialize C++ engine: " + result);
				return false;
			}
			
			System.out.println("Syncing terrain to C++...");
			syncTerrainToCpp();
			
			System.out.println("Syncing water to C++...");
			syncWaterToCpp();
			
			System.out.println("Syncing obstacles to C++...");
			syncObstaclesToCpp();
			
			System.out.println("Finalizing C++ engine...");
			result = cppEngine.executeCommand("finalize");
			
			if (!result.startsWith("OK")) {
				System.err.println("Failed to finalize C++ engine: " + result);
				return false;
			}
			
			agent.setAttribute(CPP_ENGINE_PATH, enginePath);
			setBoolAttribute(agent, USE_CPP_ENGINE, true);
			
			System.out.println("C++ spreading engine initialized successfully!");
			System.out.println("  Engine path: " + enginePath);
			System.out.println("  Grid: " + width + "x" + height);
			System.out.println("  Water cells: " + activeWaterCells.size());
			
			return true;
			
		} catch (Exception e) {
			System.err.println("Error initializing C++ engine: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	// === SYNC METHODS ===
	private void syncTerrainToCpp() throws IOException, InterruptedException {
		if (internalGrid == null) {
			throw new IOException("Internal grid is not initialized");
		}
		
		int width = internalGrid.length;
		int height = internalGrid[0].length;
		
		StringBuilder terrainData = new StringBuilder();
		int cellCount = 0;
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				GridCell cell = internalGrid[i][j];
				if (cell != null && !cell.isWater) {
					terrainData.append(i).append(",").append(j).append(",")
						.append(cell.terrainElevation).append(";");
					cellCount++;
				}
			}
		}
		
		System.out.println("  Syncing " + cellCount + " terrain cells...");
		
		if (terrainData.length() > 0) {
			cppEngine.executeCommand("set_terrain", terrainData.toString());
		}
	}
	
	private void syncWaterToCpp() throws IOException, InterruptedException {
		if (activeWaterCells == null || activeWaterCells.isEmpty()) {
			System.out.println("  No water cells to sync");
			return;
		}
		
		StringBuilder waterData = new StringBuilder();
		
		for (GridCell cell : activeWaterCells) {
			waterData.append(cell.x).append(",").append(cell.y).append(",")
				.append(cell.waterElevation).append(",")
				.append(cell.terrainElevation).append(";");
		}
		
		System.out.println("  Syncing " + activeWaterCells.size() + " water cells...");
		
		if (waterData.length() > 0) {
			cppEngine.executeCommand("set_water", waterData.toString());
		}
	}
	
	private void syncObstaclesToCpp() throws IOException, InterruptedException {
		if (obstacleGridCells == null || obstacleGridCells.isEmpty()) {
			System.out.println("  No obstacle cells to sync");
			return;
		}
		
		StringBuilder obstacleData = new StringBuilder();
		
		for (GridCell cell : obstacleGridCells) {
			obstacleData.append(cell.x).append(",").append(cell.y).append(";");
		}
		
		System.out.println("  Syncing " + obstacleGridCells.size() + " obstacle cells...");
		
		if (obstacleData.length() > 0) {
			cppEngine.executeCommand("set_obstacles", obstacleData.toString());
		}
	}
	
	private void syncObstaclesToCppDynamic() throws IOException {
		StringBuilder obstacleData = new StringBuilder();
		
		for (GridCell cell : obstacleGridCells) {
			obstacleData.append(cell.x).append(",").append(cell.y).append(";");
		}
		
		cppEngine.executeCommand("set_obstacles", obstacleData.toString());
	}
	
	// === UPDATE WATER DATA FROM C++ ===
	private void updateWaterDataFromCpp(IScope scope, String waterData) {
		final IAgent agent = getCurrentAgent(scope);
		IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		
		if (waterData == null || waterData.isEmpty()) {
			return;
		}
		
		java.util.Set<String> cppWaterPositions = new java.util.HashSet<>();
		java.util.Map<String, Double> cppWaterLevels = new java.util.HashMap<>();
		
		String[] cells = waterData.split(";");
		
		for (String cellData : cells) {
			if (cellData.isEmpty()) continue;
			
			String[] parts = cellData.split(",");
			if (parts.length == 3) {
				try {
					int x = Integer.parseInt(parts[0]);
					int y = Integer.parseInt(parts[1]);
					double waterElev = Double.parseDouble(parts[2]);
					
					String key = x + "," + y;
					cppWaterPositions.add(key);
					cppWaterLevels.put(key, waterElev);
				} catch (NumberFormatException e) {
					System.err.println("Error parsing water data: " + cellData);
				}
			}
		}
		
		activeWaterCells.removeIf(cell -> {
			String key = cell.x + "," + cell.y;
			if (!cppWaterPositions.contains(key)) {
				cell.isWater = false;
				cell.waterElevation = 0.0;
				cell.isEdgeCell = false;
				if (waterField != null) {
					waterField.set(scope, cell.x, cell.y, 0.0);
				}
				return true;
			}
			return false;
		});
		
		for (String key : cppWaterPositions) {
			String[] coords = key.split(",");
			int x = Integer.parseInt(coords[0]);
			int y = Integer.parseInt(coords[1]);
			double waterElev = cppWaterLevels.get(key);
			
			GridCell cell = internalGrid[x][y];
			
			if (!cell.isWater) {
				cell.isWater = true;
				activeWaterCells.add(cell);
			}
			
			cell.waterElevation = waterElev;
			
			if (waterField != null) {
				waterField.set(scope, x, y, waterElev);
			}
		}
		
		identifyEdgeCells();
	}
	
	// === C++ ENGINE CONTROL ===
	@action(name = "shutdown_cpp_engine", doc = @doc("Shuts down the C++ engine process"))
	public Boolean shutdownCppEngine(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		
		if (cppEngine != null) {
			try {
				cppEngine.shutdown();
				System.out.println("C++ engine shut down successfully");
			} catch (Exception e) {
				System.err.println("Error shutting down C++ engine: " + e.getMessage());
			} finally {
				cppEngine = null;
				setBoolAttribute(agent, USE_CPP_ENGINE, false);
			}
		}
		
		return true;
	}
	
	@action(name = "disable_cpp_engine", doc = @doc("Disables C++ engine and switches back to Java implementation"))
	public Boolean disableCppEngine(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		setBoolAttribute(agent, USE_CPP_ENGINE, false);
		System.out.println("Switched to Java spreading engine");
		return true;
	}
	
	@action(name = "get_engine_status", doc = @doc("Returns current engine status (Java or C++)"))
	public String getEngineStatus(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		boolean useCpp = getBoolAttribute(agent, USE_CPP_ENGINE);
		return useCpp ? "C++" : "Java";
	}
	
	// === SHARED GRID CELL INITIALIZATION METHOD ===
	private void initializeGridCells(IScope scope, IField demField, IList<IShape> waterGeometries,
									 Double initialWaterDepth, IField waterField) throws GamaRuntimeException {
		int gridWidth = demField.getCols(scope);
		int gridHeight = demField.getRows(scope);
		
		int waterCellCount = 0;
		double totalWaterTerrainElev = 0.0;
		int waterCellsFound = 0;
		
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				double terrainElev = ((Number) demField.get(scope, i, j)).doubleValue();
				
				IShape cellShape = demField.getCellShapeAt(scope, i, j);
				
				GridCell cell = new GridCell(i, j, terrainElev, cellShape);
				internalGrid[i][j] = cell;
				
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
					cell.wasOriginalWater = true;
					activeWaterCells.add(cell);
					waterCellCount++;
				}
			}
		}
		
		double uniformWaterLevel = 0.0;
		if (waterCellsFound > 0) {
			double avgWaterTerrainElev = totalWaterTerrainElev / waterCellsFound;
			uniformWaterLevel = avgWaterTerrainElev + initialWaterDepth;
		}
		
		final double finalWaterLevel = uniformWaterLevel;
		for (GridCell cell : activeWaterCells) {
			cell.waterElevation = finalWaterLevel;
			cell.originalWaterElevation = finalWaterLevel;
			waterField.set(scope, cell.x, cell.y, cell.waterElevation);
		}
		
		buildNeighborRelationships(gridWidth, gridHeight);
		
		identifyEdgeCells();
		
		System.out.println("Grid cells initialized: " + waterCellCount + " water cells");
		System.out.println("Initial water level: " + (uniformWaterLevel > 0 ? String.format("%.2f", uniformWaterLevel) : "0.0") + "m");
	}
	
	private void buildNeighborRelationships(int width, int height) {
		final int[] dx = { -1, -1, -1, 0, 0, 1, 1, 1 };
		final int[] dy = { -1, 0, 1, -1, 1, -1, 0, 1 };
		
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
	
	// === SIMULATION STEP - ROUTER ===
	@action(name = "simulate_spreading_step", doc = @doc("Executes one step of the spreading simulation"))
	public Boolean simulateSpreadingStep(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		if (!getBoolAttribute(agent, SIMULATION_ACTIVE)) {
			return false;
		}
		
		boolean useCppEngine = getBoolAttribute(agent, USE_CPP_ENGINE);
		
		if (useCppEngine) {
			return simulateSpreadingStepCpp(scope);
		} else {
			return simulateSpreadingStepJava(scope);
		}
	}
	
	// === C++ SIMULATION STEP ===
	private Boolean simulateSpreadingStepCpp(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		int currentStep = getIntAttribute(agent, SIMULATION_STEP) + 1;
		setIntAttribute(agent, SIMULATION_STEP, currentStep);
		
		double flowThreshold = getFloatAttribute(agent, FLOW_THRESHOLD);
		double risingRate = getFloatAttribute(agent, RISING_RATE);
		double minFlowDiff = getFloatAttribute(agent, MIN_FLOW_DIFF);
		double equalizationThreshold = getFloatAttribute(agent, EQUALIZATION_THRESHOLD);
		
		boolean rainActive = getBoolAttribute(agent, RAIN_ACTIVE);
		double rainRate = getFloatAttribute(agent, RAIN_RATE);
		double rainIntensity = getFloatAttribute(agent, RAIN_INTENSITY);
		
		double effectiveRisingRate = risingRate;
		double effectiveFlowThreshold = flowThreshold;
		double effectiveMinFlowDiff = minFlowDiff;
		
		if (rainActive && rainRate > 0.0) {
			effectiveRisingRate += rainRate;
			effectiveFlowThreshold = flowThreshold * (1.0 / rainIntensity);
			effectiveMinFlowDiff = minFlowDiff * (1.0 / rainIntensity);
		}
		
		try {
			syncObstaclesToCppDynamic();
			
			String params = String.format("%.6f,%.6f,%.6f,%.6f,%.6f,%d",
				effectiveFlowThreshold, effectiveRisingRate, effectiveMinFlowDiff,
				equalizationThreshold, rainRate, rainActive ? 1 : 0);
			
			String result = cppEngine.executeCommand("step", params);
			
			if (!result.startsWith("OK")) {
				System.err.println("C++ engine step failed: " + result);
				return false;
			}
			
			String waterData = cppEngine.executeCommand("get_water");
			updateWaterDataFromCpp(scope, waterData);
			
			IField waterField = (IField) agent.getAttribute(WATER_FIELD);
			updateObstacleStatus(scope, waterField);
			
			return true;
			
		} catch (Exception e) {
			System.err.println("Error communicating with C++ engine: " + e.getMessage());
			e.printStackTrace();
			setBoolAttribute(agent, USE_CPP_ENGINE, false);
			System.err.println("Falling back to Java implementation");
			return simulateSpreadingStepJava(scope);
		}
	}
	
	// === JAVA SIMULATION STEP (Original Implementation) ===
	private Boolean simulateSpreadingStepJava(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		int currentStep = getIntAttribute(agent, SIMULATION_STEP) + 1;
		setIntAttribute(agent, SIMULATION_STEP, currentStep);
		
		double flowThreshold = getFloatAttribute(agent, FLOW_THRESHOLD);
		double risingRate = getFloatAttribute(agent, RISING_RATE);
		double minFlowDiff = getFloatAttribute(agent, MIN_FLOW_DIFF);
		double equalizationThreshold = getFloatAttribute(agent, EQUALIZATION_THRESHOLD);
		
		boolean rainActive = getBoolAttribute(agent, RAIN_ACTIVE);
		double rainRate = getFloatAttribute(agent, RAIN_RATE);
		double rainIntensity = getFloatAttribute(agent, RAIN_INTENSITY);
		
		double effectiveRisingRate = risingRate;
		double effectiveFlowThreshold = flowThreshold;
		double effectiveMinFlowDiff = minFlowDiff;
		
		if (rainActive && rainRate > 0.0) {
			effectiveRisingRate += rainRate;
			effectiveFlowThreshold = flowThreshold * (1.0 / rainIntensity);
			effectiveMinFlowDiff = minFlowDiff * (1.0 / rainIntensity);
		}
		
		IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		
		if (rainActive) {
			applyRainfall(scope, waterField);
		}
		
		updateObstacleStatus(scope, waterField);
		
		List<GridCell> newWaterCells = new ArrayList<>();
		HashSet<GridCell> affectedNeighbors = new HashSet<>();
		
		final double finalEffectiveFlowThreshold = effectiveFlowThreshold;
		final double finalEffectiveRisingRate = effectiveRisingRate;
		
		double minSpreadingLevel = edgeWaterCells.parallelStream()
			.filter(cell -> cell.waterElevation > cell.terrainElevation + finalEffectiveFlowThreshold)
			.mapToDouble(cell -> cell.waterElevation)
			.min()
			.orElse(Double.MAX_VALUE);
		
		for (GridCell edgeCell : edgeWaterCells) {
			if (edgeCell.waterElevation > edgeCell.terrainElevation + effectiveFlowThreshold) {
				for (GridCell neighbor : edgeCell.neighbors) {
					if (!neighbor.isWater && canWaterFlowToCell(neighbor, edgeCell.waterElevation, effectiveMinFlowDiff)) {
						neighbor.isWater = true;
						
						double baseWaterLevel = Math.max(
							neighbor.terrainElevation + effectiveFlowThreshold,
							minSpreadingLevel - 0.01
						);
						
						if (rainActive && rainRate > 0.0) {
							baseWaterLevel += rainRate * rainIntensity * 0.5;
						}
						
						neighbor.waterElevation = baseWaterLevel;
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
		
		if (!newWaterCells.isEmpty()) {
			activeWaterCells.addAll(newWaterCells);
			updateEdgeCells(newWaterCells, affectedNeighbors);
			smoothWaterSurface(scope, waterField, 0.5);
		}
		
		if (newWaterCells.isEmpty()) {
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
				
				Map<GridCell, Double> newElevations = new ConcurrentHashMap<>();
				activeWaterCells.parallelStream().forEach(cell -> {
					if (cell.waterElevation < targetLevel) {
						newElevations.put(cell, Math.min(cell.waterElevation + finalEffectiveRisingRate, targetLevel));
					}
				});
				
				for (Map.Entry<GridCell, Double> entry : newElevations.entrySet()) {
					GridCell cell = entry.getKey();
					cell.waterElevation = entry.getValue();
					if (waterField != null) {
						waterField.set(scope, cell.x, cell.y, cell.waterElevation);
					}
				}
				
				smoothWaterSurface(scope, waterField, 0.3);
				
			} else {
				final double uniformRise = effectiveRisingRate;
				
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
	
	// === WATER SMOOTHING METHOD ===
	private void smoothWaterSurface(IScope scope, IField waterField, double smoothingFactor) {
		int width = getIntAttribute(getCurrentAgent(scope), GRID_WIDTH);
		int height = getIntAttribute(getCurrentAgent(scope), GRID_HEIGHT);
		double[][] smoothedValues = new double[width][height];
		
		activeWaterCells.parallelStream().forEach(cell -> {
			double sum = cell.waterElevation * (1.0 - smoothingFactor);
			double weight = 1.0 - smoothingFactor;
			int waterNeighborCount = 0;
			
			for (GridCell neighbor : cell.neighbors) {
				if (neighbor.isWater) {
					sum += neighbor.waterElevation * (smoothingFactor / 8.0);
					weight += smoothingFactor / 8.0;
					waterNeighborCount++;
				}
			}
			
			if (waterNeighborCount > 0) {
				smoothedValues[cell.x][cell.y] = sum / weight;
			} else {
				smoothedValues[cell.x][cell.y] = cell.waterElevation;
			}
		});
		
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
			for (GridCell cell : activeWaterCells) {
				cell.waterElevation += rainRate;
				if (waterField != null) {
					waterField.set(scope, cell.x, cell.y, cell.waterElevation);
				}
			}
		}
	}
	
	// === OBSTACLE STATUS UPDATE ===
	private void updateObstacleStatus(IScope scope, IField waterField) {
		final IAgent agent = getCurrentAgent(scope);
		final double destructionTime = getFloatAttribute(agent, OBSTACLE_DESTRUCTION_TIME);
		final double currentTime = scope.getSimulation().getClock().getCycle();
		final IField obstacleField = (IField) agent.getAttribute(OBSTACLE_FIELD);
		
		List<ObstacleCell> obstaclesToRemove = new ArrayList<>();
		
		for (ObstacleCell obstacleCell : activeObstacles) {
			if (obstacleCell.isDestroyed || !obstacleCell.isDestroyable) continue;
			
			GridCell cell = obstacleCell.gridCell;
			
			boolean currentlyUnderAttack = false;
			for (GridCell neighbor : cell.neighbors) {
				if (neighbor.isWater) {
					currentlyUnderAttack = true;
					break;
				}
			}
			
			if (currentlyUnderAttack && !obstacleCell.isUnderWaterAttack) {
				obstacleCell.isUnderWaterAttack = true;
				obstacleCell.waterAttackStartTime = currentTime;
			} else if (!currentlyUnderAttack && obstacleCell.isUnderWaterAttack) {
				obstacleCell.isUnderWaterAttack = false;
				obstacleCell.waterAttackStartTime = -1;
			}
			
			if (obstacleCell.isUnderWaterAttack) {
				ObstacleStructure obstacle = individualObstacles.get(obstacleCell.obstacleId);
				double destructionTimeToUse = obstacle != null ? obstacle.getDestructionTime() : destructionTime;
				
				if ((currentTime - obstacleCell.waterAttackStartTime) >= destructionTimeToUse) {
					obstacleCell.isDestroyed = true;
					obstaclesToRemove.add(obstacleCell);
					
					handleObstacleCellDestruction(scope, obstacleCell, obstacleField);
				}
			}
		}
		
		activeObstacles.removeAll(obstaclesToRemove);
	}
	
	// === HANDLE OBSTACLE CELL DESTRUCTION ===
	private void handleObstacleCellDestruction(IScope scope, ObstacleCell destroyedObstacleCell, IField obstacleField) {
		GridCell cell = destroyedObstacleCell.gridCell;
		int obstacleId = destroyedObstacleCell.obstacleId;
		
		Set<Integer> cellObstacles = cellToObstacleIds.get(cell);
		if (cellObstacles != null) {
			cellObstacles.remove(obstacleId);
			
			if (cellObstacles.isEmpty()) {
				cellToObstacleIds.remove(cell);
				obstacleGridCells.remove(cell);
				
				cell.terrainElevation = cell.originalTerrainElevation;
				obstacleField.set(scope, cell.x, cell.y, 0.0);
				
			} else {
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
		
		ObstacleStructure obstacle = individualObstacles.get(obstacleId);
		if (obstacle != null) {
			obstacle.obstacleCells.remove(cell);
			obstacle.cellElevations.remove(cell);
		}
	}
	
	// === WATER FLOW CHECK ===
	private boolean canWaterFlowToCell(GridCell targetCell, double sourceWaterLevel, double minFlowDiff) {
		if (obstacleGridCells.contains(targetCell)) {
			Set<Integer> obstacleIds = cellToObstacleIds.get(targetCell);
			if (obstacleIds != null && !obstacleIds.isEmpty()) {
				for (int obstacleId : obstacleIds) {
					ObstacleStructure obstacle = individualObstacles.get(obstacleId);
					if (obstacle != null && !obstacle.isDestroyed) {
						if (!obstacle.isDestroyable) {
							if (sourceWaterLevel <= targetCell.terrainElevation + minFlowDiff) {
								return false;
							}
						}
					}
				}
			}
		}
		
		return (sourceWaterLevel - targetCell.terrainElevation) > minFlowDiff;
	}
	
	private void updateEdgeCells(List<GridCell> newWaterCells, HashSet<GridCell> affectedNeighbors) {
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
			@arg(name = "rain_intensity", type = IType.FLOAT, optional = true, doc = @doc("Intensity multiplier for rain effects (default: 1.0)")) }, 
			doc = @doc("Starts rainfall with specified rate"))
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
	
	// === SIMULATION CONTROL ACTIONS ===
	@action(name = "start_spreading_simulation", doc = @doc("Starts the spreading simulation"))
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
	
	// === RESET SIMULATION ===
	@action(name = "reset_spreading_simulation", args = {
			@arg(name = "water_geometries", type = IType.LIST, doc = @doc("List of water polygon geometries")),
			@arg(name = "initial_water_depth", type = IType.FLOAT, optional = true, doc = @doc("Initial water depth (default: 1.5m)")) }, 
			doc = @doc("Resets the spreading simulation to initial state"))
	public Boolean resetSpreadingSimulation(final IScope scope) throws GamaRuntimeException {
		final IAgent agent = getCurrentAgent(scope);
		
		boolean wasUsingCpp = getBoolAttribute(agent, USE_CPP_ENGINE);
		String cppPath = null;
		
		if (wasUsingCpp && cppEngine != null) {
			cppPath = (String) agent.getAttribute(CPP_ENGINE_PATH);
			shutdownCppEngine(scope);
		}
		
		setBoolAttribute(agent, SIMULATION_ACTIVE, false);
		setIntAttribute(agent, SIMULATION_STEP, 0);
		
		setBoolAttribute(agent, RAIN_ACTIVE, false);
		setFloatAttribute(agent, RAIN_RATE, 0.0);
		setFloatAttribute(agent, RAIN_INTENSITY, 1.0);
		
		clearAllObstacles(scope);
		setBoolAttribute(agent, OBSTACLE_BUILDING_MODE, false);
		setBoolAttribute(agent, OBSTACLE_REMOVAL_MODE, false);
		
		final IList<IShape> waterGeometries = scope.getListArg("water_geometries");
		final Double initialWaterDepth = scope.hasArg("initial_water_depth") ? scope.getFloatArg("initial_water_depth") : 1.5;
		
		activeWaterCells.clear();
		edgeWaterCells.clear();
		
		int width = getIntAttribute(agent, GRID_WIDTH);
		int height = getIntAttribute(agent, GRID_HEIGHT);
		
		IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		
		if (waterField != null) {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					waterField.set(scope, i, j, 0.0);
				}
			}
		}
		
		int waterCellCount = 0;
		double totalWaterTerrainElev = 0.0;
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				GridCell cell = internalGrid[i][j];
				
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
					cell.wasOriginalWater = true;
					activeWaterCells.add(cell);
					totalWaterTerrainElev += cell.terrainElevation;
					waterCellCount++;
				} else {
					cell.isWater = false;
					cell.waterElevation = 0.0;
					cell.isEdgeCell = false;
					cell.wasOriginalWater = false;
				}
			}
		}
		
		double uniformWaterLevel = 0.0;
		if (waterCellCount > 0) {
			double avgWaterTerrainElev = totalWaterTerrainElev / waterCellCount;
			uniformWaterLevel = avgWaterTerrainElev + initialWaterDepth;
		}
		
		for (GridCell cell : activeWaterCells) {
			cell.waterElevation = uniformWaterLevel;
			cell.originalWaterElevation = uniformWaterLevel;
			if (waterField != null) {
				waterField.set(scope, cell.x, cell.y, cell.waterElevation);
			}
		}
		
		identifyEdgeCells();
		System.out.println("Reset complete: " + activeWaterCells.size() + " water cells restored");
		
		if (wasUsingCpp && cppPath != null) {
			System.out.println("Reinitializing C++ engine after reset...");
			try {
				Thread.sleep(100);
				
				cppEngine = new CppEngineInterface(cppPath);
				
				String result = cppEngine.executeCommand("init", 
					String.valueOf(width), String.valueOf(height));
				
				if (result.startsWith("OK")) {
					syncTerrainToCpp();
					syncWaterToCpp();
					syncObstaclesToCpp();
					cppEngine.executeCommand("finalize");
					
					setBoolAttribute(agent, USE_CPP_ENGINE, true);
					System.out.println("C++ engine reinitialized successfully");
				}
			} catch (Exception e) {
				System.err.println("Failed to reinitialize C++ engine: " + e.getMessage());
				setBoolAttribute(agent, USE_CPP_ENGINE, false);
			}
		}
		
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
	
	// Note: Obstacle-related actions (build_obstacle, remove_obstacle_area, etc.) 
	// have been omitted for brevity but remain unchanged from the original code.
	// Include them from your original file if needed.
	
	@action(name = "clear_all_obstacles", doc = @doc("Removes all obstacles from the simulation"))
	public Boolean clearAllObstacles(final IScope scope) {
		final IAgent agent = getCurrentAgent(scope);
		final IField obstacleField = (IField) agent.getAttribute(OBSTACLE_FIELD);
		final IField waterField = (IField) agent.getAttribute(WATER_FIELD);
		
		Set<GridCell> waterCellsToRestore = new HashSet<>();
		
		for (GridCell cell : obstacleGridCells) {
			cell.terrainElevation = cell.originalTerrainElevation;
			if (obstacleField != null) {
				obstacleField.set(scope, cell.x, cell.y, 0.0);
			}
			
			if (cell.wasOriginalWater) {
				waterCellsToRestore.add(cell);
			}
		}
		
		individualObstacles.clear();
		cellToObstacleIds.clear();
		activeObstacles.clear();
		obstacleGridCells.clear();
		nextObstacleId = 1;
		
		if (!waterCellsToRestore.isEmpty()) {
			restoreOriginalWaterCells(scope, waterCellsToRestore, waterField);
			System.out.println("All obstacles cleared and " + waterCellsToRestore.size() + " original water cells restored");
		} else {
			System.out.println("All individual obstacles cleared");
		}
		
		return true;
	}
	
	private void restoreOriginalWaterCells(IScope scope, Set<GridCell> cellsToRestore, IField waterField) {
		List<GridCell> newWaterCells = new ArrayList<>();
		
		for (GridCell cell : cellsToRestore) {
			if (!cell.isWater && cell.wasOriginalWater) {
				cell.isWater = true;
				cell.waterElevation = cell.originalWaterElevation;
				activeWaterCells.add(cell);
				newWaterCells.add(cell);
				
				if (waterField != null) {
					waterField.set(scope, cell.x, cell.y, cell.waterElevation);
				}
			}
		}
		
		if (!newWaterCells.isEmpty()) {
			reconnectWaterNetwork(scope, newWaterCells, waterField);
			identifyEdgeCells();
		}
	}
	
	private void reconnectWaterNetwork(IScope scope, List<GridCell> newWaterCells, IField waterField) {
		Set<GridCell> processedCells = new HashSet<>();
		
		for (GridCell newWaterCell : newWaterCells) {
			if (!processedCells.contains(newWaterCell)) {
				Set<GridCell> connectedRegion = findConnectedWaterRegion(newWaterCell);
				processedCells.addAll(connectedRegion);
				
				double totalWaterLevel = 0.0;
				int count = 0;
				
				for (GridCell cell : connectedRegion) {
					totalWaterLevel += cell.waterElevation;
					count++;
				}
				
				if (count > 0) {
					double averageLevel = totalWaterLevel / count;
					
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
}