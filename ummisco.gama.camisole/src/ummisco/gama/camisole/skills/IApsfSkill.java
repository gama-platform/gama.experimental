package ummisco.gama.camisole.skills;

public interface IApsfSkill {
	public final static String SKILL_NAME = "camisole";
	
	public final static String DEVELOPPED_TEMPLATE = "DEVELOPPED_TEMPLATE";
	public final static String APSF_SOIL = "apsf";
	public final static String APSF_SOIL_FACTORY = "apsf_factory";
	public final static String GRANULOMETRIC_DATA_OM = "granulometry_om";
	public final static String GRANULOMETRIC_DATA_MM = "granulometry_mm";
	public final static String COUPLED_MODEL = "sub_model";
	public final static String SOIL_DIVIDING = "dividing_factor";
	public final static String SOIL_SIZE = "size";
	public final static String BULK_DENSITY = "bulk_density";
	public final static String NUMBER_OF_TRY = "number_of_try";
	public final static String DEFAULT_SPECIES = "default_species";
	public final static String DEFAULT_SPECIES_VAR = "particle_species";
	
	
	public final static String ASSOCIATE_PROCESS_TO_TEMPLATE_COMMAND = "associate";
	public final static String PARTICLE_NAME = "particle";
	public final static String TEMPLATE_NAME = "from_canvas";
	public final static String AT_SCALE = "at_scale";
	public final static String PROCESS_NAME = "with_process";
	
	public final static String GRANULOMETRIC_DEFINE_COMMAND = "granulometry";
	public final static String SOIL_TEMPLATE_LIST_COMMAND = "list_template";
	public final static String SOIL_DEFINE_COMMAND = "initialize_soil";
	public final static String MIN_BOUNDARY = "min";
	public final static String MAX_BOUNDARY = "max";
	public final static String BOUNDARY = "boundary";
	public final static String ORGANIC_MATTER = "organic_matter";
	public final static String MINERAL_MATTER = "mineral_matter";
		
}
