package ummisco.gama.unity.client.wox.serial;

import org.jdom2.Element;
import java.lang.reflect.*;
import java.util.*;

/**
 * This is a simple XML to object de-serializer. The <code>SimpleReader</code> class
 * implements <code>ObjectReader</code>. It reads an object from a JDOM element
 * and puts it back to a live Java object. The XML representation of the
 * object is a standard WOX representation. For more information about
 * the XML representation please visit: http://woxserializer.sourceforge.net/
 *
 * @author Simon M. Lucas <br />
 *         Carlos R. Jaimez Gonzalez
 * @version SimpleReader.java - 1.0
 */
public class SimpleReader extends TypeMapping implements ObjectReader {

    /**A map to store object references*/
    HashMap map;
    /**holds the mapping between primitives and their corresponding classes*/
    static HashMap primitivesMap;

    //this section was introduced to solve the problem of loading
    //a primitive class: double.class, int.class, etc. Oct 2006
    static{
        //maps the primitives to their corresponding classes
        primitivesMap = new HashMap();
        primitivesMap.put("float", float.class);
        primitivesMap.put("double", double.class);
        primitivesMap.put("boolean", boolean.class);
        primitivesMap.put("char", char.class);
        primitivesMap.put("int", int.class);
        primitivesMap.put("short", short.class);
        primitivesMap.put("long", long.class);
        primitivesMap.put("byte", byte.class);
    }


    /**
     * Default constructor. Initializes the map for object references.
     */
    public SimpleReader() {
        map = new HashMap();
    }

    /* public void printMap(){
    Set set = map.entrySet();
    Iterator it = set.iterator();
    while(it.hasNext()){
    Map.Entry entry = (Map.Entry)it.next();
    //System.out.println("key: " + entry.getKey() + ", value: " + entry.getValue());
    }
    }*/


    /**
     * This is the only public method of <code>SimpleReader</code>.
     * This method reads a JDOM element (which represents the object),
     * and returns a live java object. The JDOM element is the object
     * in a standard XML representation defined by WOX.
     * This is the low level entry point of the WOX de-serializer.
     * @param xob The JDOM element that represents the object.
     * @return A live java object.
     */
    public Object read(Element xob) {
        // there are several possibilities - see how we handle them
        if (empty(xob)) {
            return null;
        }
        else if (reference(xob)) {
            //System.out.println("it is a reference: " + xob.getAttributeValue(IDREF) );
            //printMap();
            return map.get(xob.getAttributeValue(IDREF));
        }
        // at this point we must be reading an actual Object
        // so we need to store it in
        // there are two ways we can handle objects referred to
        // by idrefs
        // the  simplest is to put all objects in an ArrayList or
        // HashMap, and then get retrieve the objects from the collection
        Object ob = null;
        String id = xob.getAttributeValue(ID);
        //System.out.println("id: " + id);
        /*if(xob.getAttributeValue(TYPE).equals("wox.request.WOXReference")){
            ob = readWOXReference(xob, id);
        }
        else*/
        if (isPrimitiveArray(xob)) {
            //System.out.println("readPrimitiveArray: " + xob.getAttributeValue(TYPE));
            ob = readPrimitiveArray(xob, id);
        }
        else if (isArray(xob)) {
            //System.out.println("readObjectArray: " + xob.getAttributeValue(TYPE));
            ob = readObjectArray(xob, id);
        }
        else if (isArrayList(xob)) {
            //System.out.println("readArrayList: " + xob.getAttributeValue(TYPE));
            ob = readArrayList(xob, id);
        }
        else if (isHashMap(xob)) {
            //System.out.println("readHashMap: " + xob.getAttributeValue(TYPE));
            ob = readHashMap(xob, id);
        }
        else if (Util.stringable(xob.getAttributeValue(TYPE))) {
            //System.out.println("readStringObject: " + xob.getAttributeValue(TYPE));
            ob = readStringObject(xob, id);
        }
        else { // assume we have a normal object with some fields to set
            //System.out.println("readObject: " + xob.getAttributeValue(TYPE));
            ob = readObject(xob, id);
        }
        // now place the object in a collection for later reference
        //System.out.println("ob: " + ob + ", id: " + id);
        return ob;
    }


    /**
     * It checks if the element is empty (no children).
     * @param xob A JDOM Element.
     * @return True if it is an empty element. False otherwise.
     */
    private boolean empty(Element xob) {
        // empty only if it has no attributes and no content
        // System.out.println("Empty test on: " + xob);
        return !xob.getAttributes().iterator().hasNext() &&
                !xob.getContent().iterator().hasNext();
    }

    /**
     * It checks if the element is an object reference
     * @param xob A JDOM Element
     * @return True if it is an object reference. False otherwise.
     */
    private boolean reference(Element xob) {
        boolean ret = xob.getAttribute(IDREF) != null;
        // System.out.println("Reference? : " + ret);
        return ret;
    }

    /**
     * It checks if the element is a primitive array
     * @param xob A JDOM Element
     * @return True if it is a primitive array. False otherwise.
     */
    private boolean isPrimitiveArray(Element xob) {
        //if (!xob.getName().equals(ARRAY)) {
        if (!xob.getAttributeValue(TYPE).equals(ARRAY)) {
            return false;
        }
        // at this point we must have an array - but is it
        // primitive?  - iterate through all the primitive array types to see
        //String arrayType = xob.getAttributeValue(TYPE);
        String arrayType = xob.getAttributeValue(ELEMENT_TYPE);
        for (int i = 0; i < primitiveArraysWOX.length; i++) {
            if (primitiveArraysWOX[i].equals(arrayType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * It checks if the element is an array
     * @param xob A JDOM Element
     * @return True if it is an array. False otherwise.
     */
    private boolean isArray(Element xob) {
        // this actually returns true for any array
        //return xob.getName().equals(ARRAY);
        return xob.getAttributeValue(TYPE).equals(ARRAY);
    }

    /**
     * It checks if the element is an ArrayList.
     * @param xob A JDOM Element
     * @return True if it is an ArrayList. False otherwise.
     */
    private boolean isArrayList(Element xob) {
        // this actually returns true for any arrayList
        return xob.getAttributeValue(TYPE).equals(ARRAYLIST);
    }

    /**
     * It checks if the element is an HashMap.
     * @param xob A JDOM Element
     * @return True if it is a HashMap. False otherwise.
     */
    private boolean isHashMap(Element xob) {
        // this actually returns true for any HashMap
        return xob.getAttributeValue(TYPE).equals(MAP);
    }


    /**
     * It reads a Primitive Array.
     * @param xob A JDOM element.
     * @param id The id for the object.
     * @return A live object.
     */
    private Object readPrimitiveArray(Element xob, Object id) {
        try {
            //Class type = getPrimitiveType(xob.getAttributeValue(TYPE));
            //get the Java type that corresponds to the WOX type
            //Class type = (Class)mapWOXToJava.get(xob.getAttributeValue(TYPE));
            Class type = (Class)mapWOXToJava.get(xob.getAttributeValue(ELEMENT_TYPE));
            //get the wrapper type to be able to construct the elements of the array
            Class wrapperType = getWrapperType(type);
            //System.out.println("type: " + type + ", wrapperType: " + wrapperType);

            Constructor cons = null;
            if ((!type.equals(char.class)) && (!type.equals(Character.class)) &&
                    (!type.equals(Class.class))){
                //get the constructor for the wrapper class - this will always take a
                //String argument - except Character data type, which takes char value!
                // System.out.println(type + " : " + wrapperType);
                cons = wrapperType.getDeclaredConstructor(new Class[]{String.class});
            }

            Object[] args = new Object[1];
            int len = Integer.parseInt(xob.getAttributeValue(LENGTH));
            //System.out.println("len: " + len);
            Object array = Array.newInstance(type, len);
            //System.out.println("array type: " + array.getClass().getComponentType().getName());

            // Array.   //why int primitive array must be standalone
            /*if (type.equals(int.class)) {
            Object intArray = readIntArray((int[]) array, xob);
            return intArray;
            }*/
            /*if (type.equals(Integer.class)) {
            Object intArray = readIntArray((Integer[]) array, xob);
            return intArray;
            }*/

            //code added by Carlos Jaimez (29th April 2005)
            if ((type.equals(byte.class)) || (type.equals(Byte.class))) {
                Object byteArray = readByteArray(xob);
                //if it is a Byte array, we have to copy the byte array into it
                if (type.equals(Byte.class)) {
                    byte[] arrayPrimitiveByte = (byte[])byteArray;
                    Byte[] arrayWrapperByte = new Byte[arrayPrimitiveByte.length];
                    for(int k=0; k<arrayPrimitiveByte.length; k++){
                        arrayWrapperByte[k] = new Byte(arrayPrimitiveByte[k]);
                    }
                    map.put(id, arrayWrapperByte);
                    return arrayWrapperByte;
                }
                //if it is a byte array
                else{
                    map.put(id, byteArray);
                    return byteArray;
                }
            }

            //only for char arrays (june 2007)
            if (type.equals(char.class) ) {
                Object charArray = readCharArray((char[]) array, xob);
                map.put(id, charArray);
                return charArray;
            }

            if (type.equals(Character.class)) {
                Object charArray = readCharArray((Character[]) array, xob);
                map.put(id, charArray);
                return charArray;
            }

            if (type.equals(Class.class)) {
                Object classArray = readClassArray((Class[]) array, xob);
                map.put(id, classArray);
                return classArray;
            }



            //----------------------------------

            StringTokenizer st = new StringTokenizer(xob.getText());
            int index = 0;
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                // will this be tedious?  need to get the right
                // type from this now
                //System.out.println("s: " + s);

                //if the value of this element is "null", we set is as null
                if (s.equals("null")){
                    Array.set(array, index++, null);
                }
                //if it is a value other than null
                else{
                    args[0] = s;
                    Object value = cons.newInstance(args);
                    Array.set(array, index++, value);
                }


                //System.out.println("s: " + s);
                //------------------------------------------------------------
                //code added to work with Character data types (C.J.)
                //27-Feb-2006 (the previous code is below commented)
                /*Character c[] = new Character[]{new Character('a')};
                Object value = null;
                if (wrapperType.getName().equals("java.lang.Character")){
                c[0] = new Character(s.charAt(0));
                //System.out.println("using constructor with c: " + c);
                value = cons.newInstance(c);
                }
                else{
                //System.out.println("using constructor with args: " + args);
                value = cons.newInstance(args);
                }*/
                //--------------------------------------------------------------
                //Previous code
                //Object value = cons.newInstance(args);
                //--------------------------------------------------------------

                // System.out.println(index + " : " + value);
                //Array.set(array, index++, value);
                // Array.set
            }
            map.put(id, array);
            return array;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets the primitive type given its name.
     * @param name Name of the primitive type
     * @return The primitive type
     */
    private Class getPrimitiveType(String name) {
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i].getName().equals(name)) {
                // System.out.println("Found primitive type: " + primitiveArrays[i]);
                return primitives[i];
            }
        }
        return null;
    }

    /**
     * Gets the wrapper type give the primitive type.
     * @param type The primitive type.
     * @return The wrapper type that corresponds to the primitive type.
     */
    private Class getWrapperType(Class type) {
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i].equals(type)) {
                // System.out.println("Found primitive type: " + primitiveArrays[i]);
                return primitiveWrappers[i];
            }
        }
        //if the data type was not found in the array, then it returns the
        //same data type passed as parameter, which is actually a wrapper
        //or the Class data type
        return type;
    }

    /**
     * Gets the wrapper type give the primitive type.
     * @param type The primitive type.
     * @return The wrapper type that corresponds to the primitive type.
     */
    private Class getWrapperType(String type) {
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i].getName().equals(type)) {
                // System.out.println("Found primitive type: " + primitiveArrays[i]);
                return primitiveWrappers[i];
            }
        }
        return null;
    }


    /**
     * Reads an array of int.
     * @param a The array of int.
     * @param xob A JDOM element.
     * @return A live object.
     */
    private Object readIntArray(int[] a, Element xob) {
        StringTokenizer st = new StringTokenizer(xob.getText());
        int index = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            a[index++] = Integer.parseInt(s);
        }
        return a;
    }

    /**
     * Reads an array of Integer.
     * @param a The array of Integer.
     * @param xob A JDOM element.
     * @return A live object.
     */
    private Object readIntArray(Integer[] a, Element xob) {
        StringTokenizer st = new StringTokenizer(xob.getText());
        int index = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            //check for null values because this is a wrapper
            if (s.equals("null")){
                a[index++] = null;
            }
            else{
                a[index++] = new Integer(s);
            }
        }
        return a;
    }


    /**
     * Reads an array of byte.
     * @param xob A JDOM element.
     * @return A live object.
     */
    private Object readByteArray(Element xob) {
        //get the encoded base64 text from the XML
        String strByte = xob.getText();
        //get the bytes from the string
        byte[] encodedArray = strByte.getBytes();
        //System.out.println("encoded.length: " + encodedArray.length);
        //decode the source byte[] array
        byte[] decodedArray = EncodeBase64.decode(encodedArray);
        //System.out.println("decoded.length: " + decodedArray.length);
        //return the real decoded array of byte
        return decodedArray;
    }


    /**
     * Reads an array of char.
     * @param a The array of char.
     * @param xob A JDOM element.
     * @return A live object.
     */
    private Object readCharArray(char[] a, Element xob) {
        StringTokenizer st = new StringTokenizer(xob.getText());
        int index = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            //the token represents the unicode value in the form "\\u0004"
            int decimalValue = getDecimalValue(s);
            a[index++] = (char)decimalValue;
        }
        // System.out.println("Read int array: " + index);
        return a;
    }

    /**
     * Reads an array of Character.
     * @param a The array of Character.
     * @param xob A JDOM element.
     * @return A live object.
     */
    private Object readCharArray(Character[] a, Element xob) {
        StringTokenizer st = new StringTokenizer(xob.getText());
        int index = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if(s.equals("null")){
                a[index++] = null;
            }
            else{
                //the token represents the unicode value in the form "\\u0004"
                int decimalValue = getDecimalValue(s);
                a[index++] = new Character((char)decimalValue);
            }
        }
        // System.out.println("Read int array: " + index);
        return a;
    }


    /**
     * Reads an array of classes.
     * @param a The array of classes.
     * @param xob A JDOM element.
     * @return A live object.
     */
    private Object readClassArray(Class[] a, Element xob) {
        StringTokenizer st = new StringTokenizer(xob.getText());
        int index = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if(s.equals("null")){
                a[index++] = null;
            }
            else{
                Class javaClass = (Class)mapWOXToJava.get(s);
                //if the data type was NOT found in the map
                if(javaClass==null){
                    javaClass = (Class)mapArrayWOXToJava.get(s);
                    //if the data type was NOT found in the array map
                    if(javaClass==null){
                        try{
                            //System.out.println("class NOT found in any of the maps: " + s);
                            a[index++] = Class.forName(s);
                        }
                        catch(java.lang.ClassNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                    else{
                        //System.out.println("WOX type: " + s + ", Java type (array): " + javaClass);
                        a[index++] = javaClass;
                    }
                }
                else{
                    //System.out.println("WOX type: " + s + ", Java type: " + javaClass);
                    a[index++] = javaClass;
                }

            }
        }
        // System.out.println("Read int array: " + index);
        return a;
    }


    /**
     * Reads a HashMap.
     * @param xob A JDOM element.
     * @param id The id for the object.
     * @return A live object.
     */
    private Object readHashMap(Element xob, Object id) {
        //System.out.println("Reading a HashMap...");
        HashMap newHashMap = new HashMap();
        //get the Entry objects (children)
        List children = xob.getChildren();
        int index = 0;
        //for every Entry we have to get the KEY and the VALUE
        for (Iterator i = children.iterator(); i.hasNext();) {
            //get the next entry
            Element entryElement = (Element)i.next();
            //get its children (key and value)
            List entryChildren = entryElement.getChildren();
            //get the key - read it
            Object key = read((Element)entryChildren.get(0));
            //get the value - read it
            Object value = read((Element)entryChildren.get(1));
            newHashMap.put(key, value);
        }
        map.put(id, newHashMap);
        return newHashMap;
    }

    /**
     * Reads an ArrayList.
     * @param xob A JDOM element.
     * @param id The id for the object.
     * @return A live object.
     */
    private Object readArrayList(Element xob, Object id) {

        //System.out.println("Reading an ArrayList...");
        Object array = readObjectArrayGeneric(xob,id);
        ArrayList list = new ArrayList();
        //populate the ArrayList with the array elements
        for (int i=0; i<Array.getLength(array); i++){
            list.add(Array.get(array, i));
        }
        map.put(id, list);
        return list;

        // to read an object array we first determine the
        // class of the array - leave this to a separate method
        // since there seems to be no automatic way to get the
        // type of the array

        //System.out.println("--------------------READ OBJECT ARRAY");
        /*try {
        //String arrayTypeName = xob.getAttributeValue(TYPE);
        String arrayTypeName = xob.getAttributeValue(ELEMENT_TYPE);
        int len = Integer.parseInt(xob.getAttributeValue(LENGTH));
        Class componentType = getObjectArrayComponentType(arrayTypeName);
        Object array = Array.newInstance(componentType, len);
        //map.put(id, array);
        // now fill in the array
        List children = xob.getChildren();
        int index = 0;
        for (Iterator i = children.iterator(); i.hasNext();) {
        //System.out.println("before reading...");
        Object childArray = read((Element) i.next());
        //System.out.println(index + " child: " + childArray);
        Array.set(array, index++, childArray);
        }
        ArrayList list = new ArrayList();
        //populate the ArrayList with the array elements
        for (int i=0; i<Array.getLength(array); i++){
        list.add(Array.get(array, i));
        }

        map.put(id, array);
        return array;
        } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
        } */
    }


    /**
     * Reads an Array of objects.
     * @param xob A JDOM element.
     * @param id The id for the object.
     * @return A live object.
     */
    private Object readObjectArray(Element xob, Object id) {

        Object array = readObjectArrayGeneric(xob,id);
        map.put(id, array);
        return array;

        // to read an object array we first determine the
        // class of the array - leave this to a separate method
        // since there seems to be no automatic way to get the
        // type of the array

        //System.out.println("--------------------READ OBJECT ARRAY");
        /* try {
        //String arrayTypeName = xob.getAttributeValue(TYPE);
        String arrayTypeName = xob.getAttributeValue(ELEMENT_TYPE);
        int len = Integer.parseInt(xob.getAttributeValue(LENGTH));
        Class componentType = getObjectArrayComponentType(arrayTypeName);
        Object array = Array.newInstance(componentType, len);
        map.put(id, array);
        // now fill in the array
        List children = xob.getChildren();
        int index = 0;
        for (Iterator i = children.iterator(); i.hasNext();) {
        //System.out.println("before reading...");
        Object childArray = read((Element) i.next());
        //System.out.println(index + " child: " + childArray);
        Array.set(array, index++, childArray);
        }
        return array;
        } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
        } */
    }


    /**
     * Reads an Array of objects.
     * @param xob A JDOM element.
     * @param id The id for the object.
     * @return A live object.
     */
    private Object readObjectArrayGeneric(Element xob, Object id) {
        // to read an object array we first determine the
        // class of the array - leave this to a separate method
        // since there seems to be no automatic way to get the
        // type of the array

        //System.out.println("--------------------READ OBJECT ARRAY");
        try {
            //String arrayTypeName = xob.getAttributeValue(TYPE);
            String arrayTypeName = xob.getAttributeValue(ELEMENT_TYPE);
            int len = Integer.parseInt(xob.getAttributeValue(LENGTH));
            Class componentType = getObjectArrayComponentType(arrayTypeName);
            Object array = Array.newInstance(componentType, len);
            //map.put(id, array);
            // now fill in the array
            List children = xob.getChildren();
            int index = 0;
            for (Iterator i = children.iterator(); i.hasNext();) {
                //System.out.println("before reading...");
                Object childArray = read((Element) i.next());
                //System.out.println(index + " child: " + childArray);
                Array.set(array, index++, childArray);
            }
            return array;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets the component type of the array as a class.
     * @param arrayTypeName The type of the array as string.
     * @return The class of the array component.
     */
    private Class getObjectArrayComponentType(String arrayTypeName) throws Exception {
        // System.out.println("Getting class for: " + arrayTypeName);

        //we first look for the Java type in the map
        Class javaClass = (Class)mapWOXToJava.get(arrayTypeName);
        //if the type was not found, we now look for it in the array map
        if(javaClass==null){
            javaClass = (Class)mapArrayWOXToJava.get(arrayTypeName);
            //if the type is not in the array map
            if(javaClass==null){
                //System.out.println("WOX type not found in any of the maps...");
                if (arrayTypeName.equals("Object")){
                    //System.out.println("It is an array of Object...");
                    arrayTypeName = "java.lang.Object";
                }
                return Class.forName(arrayTypeName);
            }
            else{
                return javaClass;
            }
        }
        else{
            return javaClass;
        }


//        String componentTypeName = arrayTypeName.substring(1);
//        System.out.println("Component type name: " + componentTypeName);
//        Class componentType = Class.forName(componentTypeName);
//        System.out.println("Component type: " + componentType);
//        return componentType;
    }

    /**
     * Reads an object to a string.
     * @param xob A JDOM element.
     * @param id The id for the object.
     * @return A live object.
     */
    private Object readStringObject(Element xob, Object id) {
        try {
            //get the Java type that corresponds to the WOX type in the XML
            Class type = (Class)TypeMapping.mapWOXToJava.get(xob.getAttributeValue(TYPE));
            //Class type = Class.forName(xob.getAttributeValue(TYPE));
            // System.out.println("Declared: ");
            // print(type.getDeclaredConstructors());
            // System.out.println("All?: ");
            // print(type.getConstructors());
            // System.out.println("Type: " + type);
            // System.out.println("Text: " + xob.getText());
            // AccessController.doPrivileged(null);
            // PrivilegedAction action

            // handle class objects differently
            if (type.equals(Class.class)) {
                //look for the Java class that corresponds to the WOX type
                Class javaClass = (Class)mapWOXToJava.get(xob.getAttributeValue(VALUE));
                //if not found, look for it in the array map
                if (javaClass == null){
                    javaClass = (Class)mapArrayWOXToJava.get(xob.getAttributeValue(VALUE));
                    //if not found, load it
                    if (javaClass == null){
                        //System.out.println("NOT found in any of the arrays: " + xob.getAttributeValue(VALUE));
                        Object obClass = Class.forName(xob.getAttributeValue(VALUE));
                        map.put(id, obClass);  //added Oct 2006
                        return obClass;
                    }
                    //if found in the array map
                    else{
                        //System.out.println("Found in the Array Map: " + javaClass);
                        map.put(id, javaClass);  //added Oct 2006
                        return javaClass;
                    }
                }
                //if found in the first map
                else{
                    //System.out.println("Found in the First Map: " + javaClass);
                    map.put(id, javaClass);  //added Oct 2006
                    return javaClass;
                }



                //System.out.println("type: " + type + ", text: " + xob.getText());
                //if it was a primitive class (i.e. double, boolean, etc.), then get it from the map
                /*System.out.println("xob.getText()" + xob.getAttributeValue(VALUE));
                Object primitiveClass = primitivesMap.get(xob.getAttributeValue(VALUE));
                if (primitiveClass != null){
                map.put(id, primitiveClass);  //added Oct 2006
                return ((Class)primitiveClass);
                }
                //otherwise load the appropriate class and return it
                //Object obClass = Class.forName(xob.getText());
                Object obClass = Class.forName(xob.getAttributeValue(VALUE));
                map.put(id, obClass);  //added Oct 2006
                return obClass;*/
            }
            /******************************************/
            /*else if (type.equals(java.util.concurrent.atomic.AtomicLong.class)){
            //System.out.println("it is atomic long...");
            Class[] st = {long.class};
            Constructor cons = type.getDeclaredConstructor(st);
            // System.out.println("String Constructor: " + cons);
            Object ob = makeObject(cons, new Object[]{new Long(xob.getText())}, id);
            return ob;

            } */
            /********************************************/
            else {
                //if it is a Character object - special case because Character has no constructor
                //that takes a String. It only has a constructor that takes a char value
                if (type.equals(char.class)){
                    //int decimalValue = getDecimalValue(xob.getText());
                    int decimalValue = getDecimalValue(xob.getAttributeValue(VALUE));
                    Character charObject = new Character((char)decimalValue);
                    //System.out.println("decimalvalue: " + decimalValue + ", charObject: " + charObject);
                    map.put(id, charObject);
                    return charObject;
                    /*System.out.println("it is CHAR!!!");
                    st = new Class[]{char.class};
                    System.out.println("charText: " + charText + ", decimalValue: " + );*/
                }
                //for the rest of the Wrapper objects - they have constructors that take "String"
                else{
                    Object ob = this.makeWrapper(type, xob.getAttributeValue(VALUE));
                    map.put(id, ob);
                    return ob;
                    //commented on 16 April 2008 to solve the wrapper problem
                    /*Class[] st = {String.class};
                    Constructor cons = type.getDeclaredConstructor(st);
                    //Object ob = makeObject(cons, new String[]{xob.getText()}, id);
                    Object ob = makeObject(cons, new String[]{xob.getAttributeValue(VALUE)}, id);
                    return ob;*/

                }

            }
        } catch (Exception e) {

            e.printStackTrace();
            // System.out.println("While trying: " type );
            return null;
            // throw new RuntimeException(e);
        }

    }

    /**
     * Gets the decimal value given the unicode value.
     * @param unicodeValue The value to be used.
     * @return The decimal value that corresponds to the unicode value.
     */
    private static int getDecimalValue(String unicodeValue){
        //first remove the "\\u" part of the unicode value
        //System.out.println("unicodeValue: " + unicodeValue);
        String unicodeModified = unicodeValue.substring(2, unicodeValue.length());
        //System.out.println("unicodeModified: " + unicodeModified);
        int decimalValue = Integer.parseInt(unicodeModified, 16);
        //System.out.println("decimalValue: " + decimalValue);
        return decimalValue;
    }

    /**
     * Reads an object.
     * @param xob A JDMO element.
     * @param id The id for the object.
     * @return A live object.
     */
    private Object readObject(Element xob, Object id) {
        // to read in an object we iterate over all the field elements
        // setting the corresponding field in the Object
        // first we construct an object of the correct class
        // this class may not have a public default constructor,
        // but will have a private default constructor - so we get
        // this back
        try {
            //System.out.println("Type: " + xob.getAttributeValue(TYPE));
            //System.out.println("Element: " + xob.getName());

            Class type = Class.forName(xob.getAttributeValue(TYPE));
            //System.out.println("type: " + type + ", TYPE: " + xob.getAttributeValue(TYPE));
            // System.out.println("Declared: ");
            // print(type.getDeclaredConstructors());
            // System.out.println("All?: ");
            // print(type.getConstructors());
            // AccessController.doPrivileged(null);
            // PrivilegedAction action

            // put the forced call in here!!!
            // Constructor cons = type.getDeclaredConstructor(new Class[0]);
            Constructor cons = Util.forceDefaultConstructor(type);
            cons.setAccessible(true);
            //System.out.println("Default Constructor: " + cons + ", id is: " + id);
            //this.printMap();
            Object ob = makeObject(cons, new Object[0], id);
            //System.out.println("ob before instanceof: " + ob);
            boolean bbb = ob instanceof Method;
            //System.out.println("ob instanceof Method: " +  bbb + ", ob is: " + ob);
            // now go through setting all the fields
            setFields(ob, xob);
            //System.out.println("after setFields(). ob is: " +  ob);

            /************************************************************/
            //if the TYPE is "java.lang.reflect.Method", this is a special case. We will construct
            //the method by invoking the getMethod method
            //System.out.println("/*********************************************************************/");
            /*           if (xob.getAttributeValue(TYPE).equals("java.lang.reflect.Method")){
            //we get the info about the method
            String methodName = "";
            String className = "";
            Class[] methodParameters = null;
            for (Iterator i = xob.getChildren().iterator(); i.hasNext();) {
            Element fe = (Element) i.next();
            String name = fe.getAttributeValue(NAME);
            if (name.equals("clazz"))
            className = fe.getChild(OBJECT).getText();
            else if (name.equals("name"))
            methodName = fe.getChild(OBJECT).getText();
            else if (name.equals("parameterTypes")){
            System.out.println("getting the parameter types...");
            Element child = (Element) fe.getChildren().iterator().next();
            methodParameters = (Class[]) read(child);
            }
            //System.out.println("parameterTypes");
            //System.out.println("fe: " + fe);
            }
            System.out.println("CLASS: " + className + ", METHOD: " + methodName);
            System.out.println("methodParameters.length: " + methodParameters.length);
            for(int g=0; g<methodParameters.length; g++){
            System.out.println("m[" + g + "]: " + methodParameters[g]);
            }

            Class myClass = Class.forName(className);
            Method method = myClass.getMethod(methodName, methodParameters);
            System.out.println("method is: "  + method);

            //Element className = xob.getChild("");
            //System.out.println("xxx: " + xob.getParentElement());
            }                                                                   */
            //System.out.println("/*********************************************************************/");


            /********************************************************************/



            return ob;
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            return null;
            // throw new RuntimeException(e);
        }
    }

    /**
     * Sets the fields of an object.
     * @param ob The object to set the fields.
     * @param xob A JDOM element.
     */
    private void setFields(Object ob, Element xob) {
        // iterate over the set of fields
        Class type = ob.getClass();
        for (Iterator i = xob.getChildren().iterator(); i.hasNext();) {
            Element fe = (Element) i.next();
            String name = fe.getAttributeValue(NAME);
            //System.out.println("name: " + name);
            // ignore shadowing for now...
            String declaredType = fe.getAttributeValue(DECLARED);
            try {
                Class declaringType;
                if (declaredType != null) {
                    declaringType = Class.forName(declaredType);
                } else {
                    declaringType = type;
                }
                //System.out.println("Field name: " + name + " belonging to: " + declaringType);
                Field field = getField(declaringType, name);
                field.setAccessible(true);
                Object value = null;
                if (Util.primitive(field.getType())) {
                    // System.out.println("Primitive");
                    //System.out.println("setfield... primitive...type: " + fe.getAttributeValue(TYPE));
                    if (fe.getAttributeValue(TYPE).equals("char")){
                        int decimalValue = getDecimalValue(fe.getAttributeValue(VALUE));
                        Character charObject = new Character((char)decimalValue);
                        //System.out.println("decimalvalue: " + decimalValue + ", charObject: " + charObject);
                        value = charObject;
                    }
                    else{
                        value = makeWrapper(field.getType(), fe.getAttributeValue(VALUE));
                    }
                }
                //it means that the datatype is a Wrapper or a String
                else if (mapWOXToJava.get(fe.getAttributeValue(TYPE))!= null){
                    Class typeWrapper = (Class)TypeMapping.mapWOXToJava.get(fe.getAttributeValue(TYPE));
                    //if it is a Character object - special case because Character has no constructor
                    //that takes a String. It only has a constructor that takes a char value
                    if (typeWrapper.equals(Character.class)){
                        //int decimalValue = getDecimalValue(xob.getText());
                        int decimalValue = getDecimalValue(fe.getAttributeValue(VALUE));
                        Character charObject = new Character((char)decimalValue);
                        //System.out.println("decimalvalue: " + decimalValue + ", charObject: " + charObject);
                        value = charObject;
                        //return charObject;
                        /*System.out.println("it is CHAR!!!");
                        st = new Class[]{char.class};
                        System.out.println("charText: " + charText + ", decimalValue: " + );*/
                    }
                    //for the rest of the Wrapper objects - they have constructors that take "String"
                    else{
                        Class[] st = {String.class};
                        Constructor cons = typeWrapper.getDeclaredConstructor(st);
                        cons.setAccessible(true);
                        //System.out.println("STEP 2");
                        value = cons.newInstance(new String[]{fe.getAttributeValue(VALUE)});
                        //Object ob = makeObject(cons, new String[]{xob.getText()}, id);
                        //Object ob2 = makeObject(cons, new String[]{fe.getAttributeValue(VALUE)});
                        ///value = ob2;
                        //return ob;
                    }

                }
                else {
                    // must be an object with only one child
                    //System.out.println("Object");
                    Element child = (Element) fe.getChildren().iterator().next();
                    value = read(child);
                }
                //System.out.println("  Setting: " + field);
                // System.out.println("  of: " + ob);
                //System.out.println("  to: " + value );
                field.set(ob, value);
                // still need to retrieve the value of this object!!!
                // how to do that?
                // well - either the Object is stringable (e.g. String or
                // so at this stagw we either determine the value of the
                // field directly, or otherwise
            } catch (Exception e) {
                // e.printStackTrace();
                // throw new RuntimeException(e);
                //System.out.println(name + " : " + e);

            }
        }

    }

    // this method not only makes the object, but also places
    // it in the HashMap of object references
    private Object makeObject(Constructor cons, Object[] args, Object key) throws Exception {
        //System.out.println("STEP 1");
        cons.setAccessible(true);
        //System.out.println("STEP 2");
        Object value = cons.newInstance(args);
        //System.out.println("value is: " + value);
        map.put(key, value);
        return value;
    }

    private Object makeWrapper(Class type, String value) throws Exception {
        Class wrapperType = getWrapperType(type);
        // System.out.println("wrapperType: " + wrapperType + " : " + type + " : " + (type == int.class));
        Constructor cons = wrapperType.getDeclaredConstructor(new Class[]{String.class});
        return cons.newInstance(new Object[]{value});
    }

    private Field getField(Class type, String name) throws Exception {
        // System.out.println(type + " :::::: " + name);
        if (type == null) {
            return null;
        }
        try {
            // throws an exception if there's no such field
            return type.getDeclaredField(name);
        } catch (Exception e) {
            // try the superclass instead
            return getField(type.getSuperclass(), name);
        }
    }

//    public Constructor getConstructor(Class type) {
//        Constructor[] cons = type.getDeclaredConstructors();
//        return null;
//    }

    private void print(Constructor[] cons) {
        for (int i = 0; i < cons.length; i++) {
            //System.out.println(i + " : " + cons[i]);
        }
    }


    private Class getComponentType(String type) {
        for (int i = 0; i < primitiveArrays.length; i++) {
            if (primitiveArrays[i].getName().equals(type)) {
                // System.out.println("Found primitive type: " + primitiveArrays[i]);
                return primitives[i];
            }
        }
        return null;
    }

    private Class getArrayType(String type) {
        for (int i = 0; i < primitiveArrays.length; i++) {
            if (primitiveArrays[i].getName().equals(type)) {
                // System.out.println("Found primitive type: " + primitiveArrays[i]);
                return primitiveArrays[i];
            }
        }
        return null;
    }
}
