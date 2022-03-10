package ummisco.gama.unity.client.wox.serial;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 * The <code>Easy</code> class is used to serialize/de-serialize objects to/from XML.
 * It has two static methods. The <code>save</code> method serializes an object to XML
 * and stores it in an XML file; and the <code>load</code> method de-serializes an object
 * from an XML file.
 *
 * @author Simon M. Lucas <br />
 *         Carlos R. Jaimez Gonzalez
 * @version Easy.java - 1.0
 */
public class WoxSerializer {

    /**
     * This method saves an object to the specified XML file. Example: <br /><br />
     * <code>
     * ArrayList list = new ArrayList();  <br />
     * list.add(new Product("Beans", 500)); <br />
     * list.add(new Product("Bread", 200)); <br />
     * Easy.save(list, "list.xml");
     * </code>
     * @param ob Any object.
     * @param filename This is the XML file where the object will be stored.
     */
    public static void save(Object ob, String filename) {
        try {
            ObjectWriter writer = new SimpleWriter();
            Element el = writer.write(ob);
            XMLOutputter out = new XMLOutputter(); // ("  ", true);
            FileWriter file = new FileWriter(filename);
            out.output(el, file);
            file.close();
            System.out.println("Saved object to " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String getSerializedToString(Object ob) {
    	String serializedMsg = "";
        try {
            ObjectWriter writer = new SimpleWriter();
            Element el = writer.write(ob);
            XMLOutputter out = new XMLOutputter(); // ("  ", true);
            serializedMsg = out.outputString(el);
            
            System.out.println("Saved object to " + serializedMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return serializedMsg;
    }

    /**
     * This method loads an object from the specified XML file. Example: <br /><br />
     * <code>
     * ArrayList list = (ArrayList)Easy.load("list.xml");
     * </code>
     * @param filename The XML file where the object is stored.
     * @return Object The live object.
     */
    public static Object load(String filename) {
        try {
            SAXBuilder builder = new SAXBuilder();
            InputStream is = new FileInputStream(filename);
            Document doc = builder.build(is);
            Element el = doc.getRootElement();
            ObjectReader reader = new SimpleReader();
            return reader.read(el);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Object deserialise(String content) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(content);
            Element el = doc.getRootElement();
            ObjectReader reader = new SimpleReader();
            return reader.read(el);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    

    public static Object loadWithDic(String filename, HashMap<String, String> dic) 
    {
        	
        try {
            SAXBuilder builder = new SAXBuilder();
            InputStream is = new FileInputStream(filename);
            Document doc = builder.build(is);
            Element el = doc.getRootElement();
            
            // Traitement du cas de la racine.
            if((el.getAttribute("type").isSpecified() ) && ("object".equals(el.getName()) )) {
            	String type = el.getAttribute("type").getValue();
            	if ("Student".equals(type)) {
                    el.setAttribute("type", "data.Student");
                    System.out.println("	Type (root element): " + el.getAttribute("type").getValue());
                }
    			if ("Course".equals(type)) {
                    el.setAttribute("type", "data.Course");
                }
            }
            
            System.out.println("No of child el: " + el.getDescendants());
            
            // Traitement des descendants de la racine.
            Iterator<Content> it = el.getDescendants();
            while(it.hasNext()) {
                Element i = (Element) it.next();
                if(i.hasAttributes()) {
                	try {
                		if(( i.getAttribute("type").isSpecified() ) && ("object".equals(i.getName()) )) {
                			String type = i.getAttribute("type").getValue();
                			System.out.print("		Name : " + i.getName());
                			System.out.print("	Type : " + i.getAttribute("type").getValue());

                			if ("Student".equals(type)) {
                                i.setAttribute("type", "data.Student");
                                System.out.print("	Type (new): " + i.getAttribute("type").getValue());
                            }
                			if ("Course".equals(type)) {
                                i.setAttribute("type", "data.Course");
                                System.out.print("	Type (new): " + i.getAttribute("type").getValue());
                            }
                			System.out.println("");
                		}
                		if(( i.getAttribute("elementType").isSpecified() ) && ("object".equals(i.getName()) )) {
                			String elementType = i.getAttribute("elementType").getValue();
                			System.out.print("	elementType : " + i.getAttribute("elementType").getValue());

                			if ("Student".equals(elementType)) {
                                i.setAttribute("elementType", "data.Student");
                                System.out.print("	elementType (new): " + i.getAttribute("elementType").getValue());
                            }
                			if ("Course".equals(elementType)) {
                                i.setAttribute("elementType", "data.Course");
                                System.out.print("	elementType (new): " + i.getAttribute("elementType").getValue());
                            }
                			System.out.println("");
                		}
                	}
                	catch(Exception e) {
                	  //  Block of code to handle errors
                	}
               }
            } 
            
            System.out.println(" --------------------------------");
                       
            it = el.getDescendants();
            while(it.hasNext()) {
                Element i = (Element) it.next();
                if(i.hasAttributes())
                
                	try {
                		if(( i.getAttribute("type").isSpecified() ) && ("object".equals(i.getName()) )) {
                			String type = i.getAttribute("type").getValue();
                			System.out.print("		Name : " + i.getName());
                			System.out.println("	Type : " + i.getAttribute("type").getValue());
                		}
                	}
                	catch(Exception e) {
                	  //  Block of code to handle errors
                	}
              }

            System.out.println("--- END ---");
                        
            
           
            ObjectReader reader = new SimpleReader();
            return reader.read(el);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    

    public static Object deserializeFromString(String content, HashMap<String, String> dic) 
    {
        	
        try {
            SAXBuilder builder = new SAXBuilder();
            
            //content = content.replace("Student", "data.Student");
    		//content = content.replace("Course", "data.Course");
            System.out.println((" --> " + content));
        	for (String str : dic.keySet()) {
        		content = content.replace(str, dic.get(str));
        		System.out.println((" --> " + content));
        	}
        	Document doc = builder.build(new StringReader(content));
            Element el = doc.getRootElement();
            ObjectReader reader = new SimpleReader();
            return reader.read(el);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    
}
