import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IParser {
	public static String GAMA_KEYWORD_ACTION="action";
	public static String GAMA_KEYWORD_ARG="arg";
	public static String GAMA_KEYWORD_ASPECT = "aspect";
	public static String GAMA_KEYWORD_ATTRIBUTE = "Attribute";
	public static String GAMA_KEYWORD_CONTROL = "control";
	public static String GAMA_KEYWORD_GRID = "grid";
	public static String GAMA_KEYWORD_INIT = "init";
	public static String GAMA_KEYWORD_MODEL = "model";
	public static String GAMA_KEYWORD_NAME = "name";
	public static String GAMA_KEYWORD_OUTPUT = "output";
	public static String GAMA_KEYWORD_PARAMETER = "parameter";
	public static String GAMA_KEYWORD_PARENT = "parent";
	public static String GAMA_KEYWORD_REFLEX = "reflex";
	public static String GAMA_KEYWORD_RETURN = "return";
	public static String GAMA_KEYWORD_SKILLS = "skills";
	public static String GAMA_KEYWORD_SPECIES = "species";
	public static String GAMA_KEYWORD_STATE = "state";
	public static String GAMA_KEYWORD_TYPE = "type";
	public static String GAMA_KEYWORD_UNKNOWN = "unknown";
	public static String GAMA_KEYWORD_VALUE = "value";
	public static String GAMA_KEYWORD_WHEN = "when";
	public static String JOB_NAME_TO_UML = "Converting to UML";
	public static String XML_KEYWORD_ASSOCIATION = "uml:Association";
	public static String XML_KEYWORD_CLASS = "uml:Class";
	public static String XML_KEYWORD_ID = "xmi:id";
	public static String XML_TAG_ASSOCIATION = "packagedElement";
	public static String XML_TAG_ATTRIBUTE = "ownedAttribute";
	public static String XML_TAG_CLASS = "packagedElement";
	public static String XML_TAG_END = "ownedEnd";
	public static String XML_TAG_GENERALIZATION = "generalization";
	public static String XML_TAG_OPERATION = "ownedOperation";
	public static String XML_TAG_PARAMETER = "ownedParameter";
	public static String XML_TAG_TYPE = "type";
	public static Map<String,String> MAP_BUILT_IN_TYPES;
	static {
        Map<String, String> aMap = new HashMap<String,String>();
        aMap.put("string", "pathmap://UML_LIBRARIES/UMLPrimitiveTypes.library.uml#String");
        aMap.put("int", "pathmap://UML_LIBRARIES/UMLPrimitiveTypes.library.uml#Integer");
        aMap.put("float", "pathmap://UML_LIBRARIES/UMLPrimitiveTypes.library.uml#Real");
        aMap.put("bool", "pathmap://UML_LIBRARIES/UMLPrimitiveTypes.library.uml#Boolean");
        MAP_BUILT_IN_TYPES = Collections.unmodifiableMap(aMap);
    }
}
