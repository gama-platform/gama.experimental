package ummisco.gama.unity.wox.serial;

import sun.reflect.ReflectionFactory;
import java.security.AccessController;
import java.lang.reflect.Constructor;

/**
 * The <code>Util</code> class provides static methods that are used by
 * <code>SimpleReader</code> and <code>SimpleWriter</code>. The methods of
 * this class are used by the serialization and de-serialization processes.
 *
 * @author Carlos R. Jaimez Gonzalez <br />
 *         Simon M. Lucas
 * @version Util.java - 1.0
 */
public class Util implements Serial {

    /** reflection factory for forcing default constructors */
    private static final ReflectionFactory reflFactory = (ReflectionFactory)
            AccessController.doPrivileged(
                    new ReflectionFactory.GetReflectionFactoryAction());

    /**
     * This method returns a default constructor for the specified class.
     * Despite appearences it can be used to construct objects
     * of the specified type! of first non-serializable.
     * @param cl The class to be used to get its constructor.
     * @return The default constructor for the specified class.
     * @throws Exception If there is a problem.
     */
    public static Constructor forceDefaultConstructor(Class cl) throws Exception {
        Constructor cons = Object.class.getDeclaredConstructor(new Class[0]);
        cons = reflFactory.newConstructorForSerialization(cl, cons);
        cons.setAccessible(true);
        // System.out.println("Cons: " + cons);
        return cons;
    }

    //version commented on 08June2007
    /*public static boolean stringable(Object o) {
    // assume the following types go easily to strings...
    boolean val =  (o instanceof Number) ||
    (o instanceof Boolean) ||
    (o instanceof Class) ||
    (o instanceof String);
    // System.out.println("Stringable: " + o + " : " + val + " : " + o.getClass());
    return val;
    }*/

    /**
     * Returns true if the class of the object passed as parameter is <i>stringable</i>.
     * In other words, returns true if the object can go easily to a string representation.
     * Examples: Integer, Long, Character, Double, Class, String, etc. New version: June 2007.
     * @param o The object to test.
     * @return True if the object is stringable. False otherwise.
     */
    public static boolean stringable(Object o) {
        // assume the following types go easily to strings...
        boolean val = (o instanceof Byte) ||
                (o instanceof Short) ||
                (o instanceof Integer) ||
                (o instanceof Long) ||
                (o instanceof Float) ||
                (o instanceof Double) ||
                (o instanceof Character) ||
                (o instanceof Boolean) ||
                (o instanceof Class) ||
                (o instanceof String);
        // System.out.println("Stringable: " + o + " : " + val + " : " + o.getClass());
        return val;
    }

    /**
     * Returns true if the class passed as parameter is <i>stringable</i>. In other words,
     * returns true if objects of the class can go easily to a string representation.
     * Examples: Integer, Long, Character, Double, Class, String, etc. New version: June 2007.
     * @param type The class to test.
     * @return True if the class is stringable. False otherwise.
     */
    public static boolean stringable(Class type) {
        // assume the following types go easily to strings...
        boolean val = (Byte.class.isAssignableFrom(type) ) ||
                (Double.class.isAssignableFrom(type) ) ||
                (Float.class.isAssignableFrom(type) ) ||
                (Integer.class.isAssignableFrom(type) ) ||
                (Long.class.isAssignableFrom(type) ) ||
                (Short.class.isAssignableFrom(type) ) ||
                (Boolean.class.isAssignableFrom(type)) ||
                (Character.class.isAssignableFrom(type) ) ||
                (Class.class.equals(type)) ||
                (String.class.equals(type));
        // System.out.println("Stringable: " + type + " : " + val);
        return val;
    }

    //version commented on 08June2007
    /*
    public static boolean stringable(Class type) {
    // assume the following types go easily to strings...
    boolean val =  (Number.class.isAssignableFrom(type) ) ||
    (Boolean.class.isAssignableFrom(type)) ||
    (String.class.equals(type)) ||
    (Class.class.equals(type));
    // System.out.println("Stringable: " + type + " : " + val);
    return val;
    }*/

    /**
     * Returns true if the class which name is passed as parameter is <i>stringable</i>.
     * In other words, returns true if objects of the class can go easily to a string
     * representation.
     * @param name The name of the class to test.
     * @return True if the class is stringable. False otherwise.
     */
    public static boolean stringable(String name) {
        // assume the following types go easily to strings...
        // System.out.println("Called (String) version");
        try {
            //Class type = Class.forName(name);
            //Class type = Class.forName(name);
            //return stringable(type);
            //first we need to get the real data type
            Class realDataType = (Class)TypeMapping.mapWOXToJava.get(name);
            //if the data type was found in the mapWOXToJava then it is "stringable"
            if (realDataType!=null){
                return true;
            }
            else{
                return false;
            }
            //return stringable(realDataType);
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * This method returns true if the class passed as parameter is one of the
     * valid primitive arrays classes supported by WOX.
     * @param type The classs to test
     * @return True if the class is in the array of primitives. False otherwise.
     */
    public static boolean primitive(Class type) {
        for (int i=0; i<primitives.length; i++) {
            if (primitives[i].equals(type)) {
                return true;
            }
        }
        return false;
    }

}
