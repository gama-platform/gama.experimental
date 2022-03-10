package ummisco.gama.unity.client.wox.serial;

import java.util.HashMap;

/**
 * The <code>TypeMapping</code> class provides the type mappings
 * from Java to WOX and viceversa. This class has an important role
 * in the standard XML representation of WOX. For more information about
 * the XML representation please visit: http://woxserializer.sourceforge.net/
 *
 * @author Carlos R. Jaimez Gonzalez <br />
 *         Simon M. Lucas
 * @version TypeMapping.java - 1.0
 */
public class TypeMapping {

    //**********************************************************
    //09 October 2007
    /**Map from Java types to WOX types*/
    public static final HashMap mapJavaToWOX = new HashMap();
    /**Map from WOX types to Java types*/
    public static final HashMap mapWOXToJava = new HashMap();

    /**Map from Java array types to WOX array types*/
    public static final HashMap mapArrayJavaToWOX = new HashMap();
    /**Map from WOX array types to Java array types*/
    public static final HashMap mapArrayWOXToJava = new HashMap();

    static{
        //initialise map of Java data types (primitive and Wrapper) to WOX data types
        mapJavaToWOX.put(byte.class, "byte");
        mapJavaToWOX.put(short.class, "short");
        mapJavaToWOX.put(int.class, "int");
        mapJavaToWOX.put(long.class, "long");
        mapJavaToWOX.put(float.class, "float");
        mapJavaToWOX.put(double.class, "double");
        mapJavaToWOX.put(char.class, "char");
        mapJavaToWOX.put(boolean.class, "boolean");
        mapJavaToWOX.put(Byte.class, "byte");
        mapJavaToWOX.put(Short.class, "short");
        mapJavaToWOX.put(Integer.class, "int");
        mapJavaToWOX.put(Long.class, "long");
        mapJavaToWOX.put(Float.class, "float");
        mapJavaToWOX.put(Double.class, "double");
        mapJavaToWOX.put(Character.class, "char");
        mapJavaToWOX.put(Boolean.class, "boolean");
        //initialise map of Java data types (String, Class and Array) to WOX data types
        mapJavaToWOX.put(String.class, "string");
        mapJavaToWOX.put(Class.class, "class");
        //mapJavaToWOX.put(Array.class, "array");
        //initialise map of Java primitive arrays to WOX arrays
        mapArrayJavaToWOX.put(byte[].class, "byte[]");
        mapArrayJavaToWOX.put(byte[][].class, "byte[][]");
        mapArrayJavaToWOX.put(byte[][][].class, "byte[][][]");
        mapArrayJavaToWOX.put(short[].class, "short[]");
        mapArrayJavaToWOX.put(short[][].class, "short[][]");
        mapArrayJavaToWOX.put(short[][][].class, "short[][][]");
        mapArrayJavaToWOX.put(int[].class, "int[]");
        mapArrayJavaToWOX.put(int[][].class, "int[][]");
        mapArrayJavaToWOX.put(int[][][].class, "int[][][]");
        mapArrayJavaToWOX.put(long[].class, "long[]");
        mapArrayJavaToWOX.put(long[][].class, "long[][]");
        mapArrayJavaToWOX.put(long[][][].class, "long[][][]");
        mapArrayJavaToWOX.put(float[].class, "float[]");
        mapArrayJavaToWOX.put(float[][].class, "float[][]");
        mapArrayJavaToWOX.put(float[][][].class, "float[][][]");
        mapArrayJavaToWOX.put(double[].class, "double[]");
        mapArrayJavaToWOX.put(double[][].class, "double[][]");
        mapArrayJavaToWOX.put(double[][][].class, "double[][][]");
        mapArrayJavaToWOX.put(char[].class, "char[]");
        mapArrayJavaToWOX.put(char[][].class, "char[][]");
        mapArrayJavaToWOX.put(char[][][].class, "char[][][]");
        mapArrayJavaToWOX.put(boolean[].class, "boolean[]");
        mapArrayJavaToWOX.put(boolean[][][].class, "boolean[][]");
        mapArrayJavaToWOX.put(boolean[][][].class, "boolean[][][]");
        //initialise map of Java wrapper arrays to WOX arrays
        mapArrayJavaToWOX.put(Byte[].class, "byteWrapper[]");
        mapArrayJavaToWOX.put(Byte[][].class, "byteWrapper[][]");
        mapArrayJavaToWOX.put(Byte[][][].class, "byteWrapper[][][]");
        mapArrayJavaToWOX.put(Short[].class, "shortWrapper[]");
        mapArrayJavaToWOX.put(Short[][].class, "shortWrapper[][]");
        mapArrayJavaToWOX.put(Short[][][].class, "shortWrapper[][][]");
        mapArrayJavaToWOX.put(Integer[].class, "intWrapper[]");
        mapArrayJavaToWOX.put(Integer[][].class, "intWrapper[][]");
        mapArrayJavaToWOX.put(Integer[][][].class, "intWrapper[][][]");
        mapArrayJavaToWOX.put(Long[].class, "longWrapper[]");
        mapArrayJavaToWOX.put(Long[][].class, "longWrapper[][]");
        mapArrayJavaToWOX.put(Long[][][].class, "longWrapper[][][]");
        mapArrayJavaToWOX.put(Float[].class, "floatWrapper[]");
        mapArrayJavaToWOX.put(Float[][].class, "floatWrapper[][]");
        mapArrayJavaToWOX.put(Float[][][].class, "floatWrapper[][][]");
        mapArrayJavaToWOX.put(Double[].class, "doubleWrapper[]");
        mapArrayJavaToWOX.put(Double[][].class, "doubleWrapper[][]");
        mapArrayJavaToWOX.put(Double[][][].class, "doubleWrapper[][][]");
        mapArrayJavaToWOX.put(Character[].class, "charWrapper[]");
        mapArrayJavaToWOX.put(Character[][].class, "charWrapper[][]");
        mapArrayJavaToWOX.put(Character[][][].class, "charWrapper[][][]");
        mapArrayJavaToWOX.put(Boolean[].class, "booleanWrapper[]");
        mapArrayJavaToWOX.put(Boolean[][].class, "booleanWrapper[][]");
        mapArrayJavaToWOX.put(Boolean[][][].class, "booleanWrapper[][][]");
        //initialise map of Java arrays of Class and String to WOX arrays
        mapArrayJavaToWOX.put(Class[].class, "class[]");
        mapArrayJavaToWOX.put(Class[][].class, "class[][]");
        mapArrayJavaToWOX.put(Class[][][].class, "class[][][]");
        mapArrayJavaToWOX.put(String[].class, "string[]");
        mapArrayJavaToWOX.put(String[][].class, "string[][]");
        mapArrayJavaToWOX.put(String[][][].class, "string[][][]");


        //************************************************************************************
        //initialise map of WOX data types to Java data types
        mapWOXToJava.put("byte", byte.class);
        mapWOXToJava.put("short", short.class);
        mapWOXToJava.put("int", int.class);
        mapWOXToJava.put("long", long.class);
        mapWOXToJava.put("float", float.class);
        mapWOXToJava.put("double", double.class);
        mapWOXToJava.put("char", char.class);
        mapWOXToJava.put("boolean", boolean.class);
        mapWOXToJava.put("byteWrapper", Byte.class);
        mapWOXToJava.put("shortWrapper", Short.class);
        mapWOXToJava.put("intWrapper", Integer.class);
        mapWOXToJava.put("longWrapper", Long.class);
        mapWOXToJava.put("floatWrapper", Float.class);
        mapWOXToJava.put("doubleWrapper", Double.class);
        mapWOXToJava.put("charWrapper", Character.class);
        mapWOXToJava.put("booleanWrapper", Boolean.class);
        //initialise map of WOX data types (String, Class and Array) to Java data types
        mapWOXToJava.put("string", String.class);
        mapWOXToJava.put("class", Class.class);
        //mapWOXToJava.put("array", Array.class);
        //initialise map of WOX primitive arrays to Java arrays
        mapArrayWOXToJava.put("byte[]", byte[].class);
        mapArrayWOXToJava.put("byte[][]", byte[][].class);
        mapArrayWOXToJava.put("byte[][][]", byte[][][].class);
        mapArrayWOXToJava.put("short[]", short[].class);
        mapArrayWOXToJava.put("short[][]", short[][].class);
        mapArrayWOXToJava.put("short[][][]", short[][][].class);
        mapArrayWOXToJava.put("int[]", int[].class);
        mapArrayWOXToJava.put("int[][]", int[][].class);
        mapArrayWOXToJava.put("int[][][]", int[][][].class);
        mapArrayWOXToJava.put("long[]", long[].class);
        mapArrayWOXToJava.put("long[][]", long[][].class);
        mapArrayWOXToJava.put("long[][][]", long[][][].class);
        mapArrayWOXToJava.put("float[]", float[].class);
        mapArrayWOXToJava.put("float[][]", float[][].class);
        mapArrayWOXToJava.put("float[][][]", float[][][].class);
        mapArrayWOXToJava.put("double[]", double[].class);
        mapArrayWOXToJava.put("double[][]", double[][].class);
        mapArrayWOXToJava.put("double[][][]", double[][][].class);
        mapArrayWOXToJava.put("char[]", char[].class);
        mapArrayWOXToJava.put("char[][]", char[][].class);
        mapArrayWOXToJava.put("char[][][]", char[][][].class);
        mapArrayWOXToJava.put("boolean[]", boolean[].class);
        mapArrayWOXToJava.put("boolean[][]", boolean[][].class);
        mapArrayWOXToJava.put("boolean[][][]", boolean[][][].class);
        //initialise map of WOX wrapper arrays to Java wrapper arrays
        mapArrayWOXToJava.put("byteWrapper[]", Byte[].class);
        mapArrayWOXToJava.put("byteWrapper[][]", Byte[][].class);
        mapArrayWOXToJava.put("byteWrapper[][][]", Byte[][][].class);
        mapArrayWOXToJava.put("shortWrapper[]", Short[].class);
        mapArrayWOXToJava.put("shortWrapper[][]", Short[][].class);
        mapArrayWOXToJava.put("shortWrapper[][][]", Short[][][].class);
        mapArrayWOXToJava.put("intWrapper[]", Integer[].class);
        mapArrayWOXToJava.put("intWrapper[][]", Integer[][].class);
        mapArrayWOXToJava.put("intWrapper[][][]", Integer[][][].class);
        mapArrayWOXToJava.put("longWrapper[]", Long[].class);
        mapArrayWOXToJava.put("longWrapper[][]", Long[][].class);
        mapArrayWOXToJava.put("longWrapper[][][]", Long[][][].class);
        mapArrayWOXToJava.put("floatWrapper[]", Float[].class);
        mapArrayWOXToJava.put("floatWrapper[][]", Float[][].class);
        mapArrayWOXToJava.put("floatWrapper[][][]", Float[][][].class);
        mapArrayWOXToJava.put("doubleWrapper[]", Double[].class);
        mapArrayWOXToJava.put("doubleWrapper[][]", Double[][].class);
        mapArrayWOXToJava.put("doubleWrapper[][][]", Double[][][].class);
        mapArrayWOXToJava.put("charWrapper[]", Character[].class);
        mapArrayWOXToJava.put("charWrapper[][]", Character[][].class);
        mapArrayWOXToJava.put("charWrapper[][][]", Character[][][].class);
        mapArrayWOXToJava.put("booleanWrapper[]", Boolean[].class);
        mapArrayWOXToJava.put("booleanWrapper[][]", Boolean[][].class);
        mapArrayWOXToJava.put("booleanWrapper[][][]", Boolean[][][].class);
        //initialise map of WOX arrays of Class and String to Java arrays
        mapArrayWOXToJava.put("class[]", Class[].class);
        mapArrayWOXToJava.put("class[][]", Class[][].class);
        mapArrayWOXToJava.put("class[][][]", Class[][][].class);
        mapArrayWOXToJava.put("string[]", String[].class);
        mapArrayWOXToJava.put("string[][]", String[][].class);
        mapArrayWOXToJava.put("string[][][]", String[][][].class);

    }


}
