package ummisco.gama.unity.wox.serial;

/**
 * The <code>Serial</code> interface defines the constants used in the
 * XML representation of objects. It also provides arrays of classes
 * used for mapping primitive types, and primitive arrays.
 *
 * @author Simon M. Lucas <br />
 *         Carlos R. Jaimez Gonzalez
 * @version Serial.java - 1.0
 */
public interface Serial {
    // use string constants to enforce consistency
    // between readers and writers
    /**This is the OBJECT tag used in the XML representation of an object*/
    public static final String OBJECT = "object";
    /**This is the FIELD tag used in the XML representation of an object's field*/
    public static final String FIELD = "field";
    /**This is the NAME attribute used in the XML representation of an object*/
    public static final String NAME = "name";
    /**This is the TYPE attribute used in the XML representation of an object*/
    public static final String TYPE = "type";
    /**This is the VALUE attribute used in the XML representation of an object*/
    public static final String VALUE = "value";
    /**This is the ARRAY attribute used in the XML representation of an array*/
    public static final String ARRAY = "array";
    /**This is the ARRAYLIST attribute used in the XML representation of a list*/
    public static final String ARRAYLIST = "list";
    /**This is the ELEMENT_TYPE attribute used in the XML representation of an array*/
    public static final String ELEMENT_TYPE = "elementType";
    /**This is the MAP attribute used in the XML representation of a map*/
    public static final String MAP = "map";
    /**This is the KEY_TYPE attribute used in the XML representation of a map*/
    public static final String KEY_TYPE = "keyType";
    /**This is the VALUE_TYPE attribute used in the XML representation of a map*/
    public static final String VALUE_TYPE = "valueType";
    /**This is the ENTRY attribute used in the XML representation of a map*/
    public static final String ENTRY = "entry";
    /**This is the KEY attribute used in the XML representation of a map*/
    public static final String KEY = "key";
    /**This is the LENGTH attribute used in the XML representation of an array*/
    public static final String LENGTH = "length";
    /**This is the ID attribute used in the XML representation of an object*/
    public static final String ID = "id";
    /**This is the ID attribute used in the XML representation of an object*/
    public static final String IDREF = "idref";

    // next is used to disambiguate shadowed fields
    /**This is the DECLARED attribute used in the XML representation of an object*/
    public static final String DECLARED = "declaredClass";


    /**Array of classes that represent the classes of primitive arrays.*/
    public static final Class[] primitiveArrays =
            new Class[]{
                int[].class,
                boolean[].class,
                byte[].class,
                short[].class,
                long[].class,
                char[].class,
                float[].class,
                double[].class,
                //added Nov 2007 for wrappers
                Integer[].class,
                Boolean[].class,
                Byte[].class,
                Short[].class,
                Long[].class,
                Character[].class,
                Float[].class,
                Double[].class,
                //added Nov 2007 for Class.class
                Class[].class
            };

    /**Array of classes that represent the WOX primitive arrays.*/
    public static final String[] primitiveArraysWOX =
            new String[]{
                "int",
                "boolean",
                "byte",
                "short",
                "long",
                "char",
                "float",
                "double",
                //added Nov 2007 for wrappers
                "intWrapper",
                "booleanWrapper",
                "byteWrapper",
                "shortWrapper",
                "longWrapper",
                "charWrapper",
                "floatWrapper",
                "doubleWrapper",
                //added Nov 2007 for Class.class
                "class"
            };

    // now declare the wrapper classes for each primitive object type
    // note that this order must correspond to the order in primitiveArrays

    // there may be a better way of doing this that does not involve
    // wrapper objects (e.g. Integer is the wrapper of int), but I've
    // yet to find it
    // note that the creation of wrapper objects is a significant
    // overhead
    // example: reading an array of 1 million int (all zeros) takes
    // about 900ms using reflection, versus 350ms hard-coded
    /**Array of classes that represent the wrapper classes for primitives.*/
    public static final Class[] primitiveWrappers =
            new Class[]{
                Integer.class,
                Boolean.class,
                Byte.class,
                Short.class,
                Long.class,
                Character.class,
                Float.class,
                Double.class
            };

    /**Array of classes that represent the classes of primitives.*/
    public static final Class[] primitives =
            new Class[]{
                int.class,
                boolean.class,
                byte.class,
                short.class,
                long.class,
                char.class,
                float.class,
                double.class
            };
}
