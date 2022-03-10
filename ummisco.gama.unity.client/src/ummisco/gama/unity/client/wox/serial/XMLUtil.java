package ummisco.gama.unity.client.wox.serial;

import java.io.*;
import org.jdom2.output.XMLOutputter;
import org.jdom2.Element;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import java.util.*;

/**
 * The <code>XMLUtil</code> class provides static methods that are used by
 * <code>SimpleReader</code> and <code>SimpleWriter</code>. The methods of this
 * class are used by the serialization and de-serialization processes.
 *
 * @author Carlos R. Jaimez Gonzalez <br />
 *         Simon M. Lucas
 * @version XMLUtil.java - 1.0
 */
public class XMLUtil implements Serial {

	/**
	 * This method displays the XML representation of the object passed as
	 * parameter. The XML representation is based on the WOX standard.
	 * 
	 * @param object The object to be displayed.
	 * @throws Exception If there is a problem displaying the object.
	 */
	public static void disElement(Object object) throws Exception {
		// XMLOutputter out = new XMLOutputter(" ", true);
		XMLOutputter out = new XMLOutputter();
		Element xCons = new SimpleWriter().write(object);
		out.output(xCons, System.out);
	}

	// -------------------------------------------------------------------------------

	/**
	 * This method displays the XML representation of the JDOM Element (object)
	 * passed as parameter. The XML representation is based on the WOX standard.
	 * 
	 * @param element The JDOM element (object) to be displayed
	 * @throws Exception If there is a problem displaying the object.
	 */
	public static void disElement(Element element) throws Exception {
		// XMLOutputter out = new XMLOutputter(" ", true);
		XMLOutputter out = new XMLOutputter();
		out.output(element, System.out);
	}

	// -------------------------------------------------------------------------------

	/**
	 * This method takes a JDOM Element object and returns its string
	 * representation.
	 * 
	 * @param element The JDOM object.
	 * @throws Exception If there is a problem. Author: Carlos Roberto Jaimez
	 *                   Gonzalez 3rd May 2005
	 */
	public static String element2String(Element element) throws Exception {
		// XMLOutputter out = new XMLOutputter(" ", true);
		XMLOutputter out = new XMLOutputter();
		return out.outputString(element);
	}

	// -------------------------------------------------------------------------------

	/**
	 * This method takes a JDOM Element and returns its string representation in XML
	 * ready to be presented in a browser The method replaces the characters '<' and
	 * '>' by '&lt' and '&gt' respectively.
	 * 
	 * @param element The JDOM Element to be displayed.
	 * @return The string representation in XML.
	 * @throws Exception If there is a problem.
	 */
	public static String element2BrowserString(Element element) throws Exception {
		// XMLOutputter out = new XMLOutputter(" ", true);
		XMLOutputter out = new XMLOutputter();
		String myString = out.outputString(element);
		// System.out.println("original:\n " + myString);
		myString = myString.replaceAll("<", "&lt;");
		myString = myString.replaceAll(">", "&gt;");
		// System.out.println("original without '<' and '>':\n " + myString);
		return myString;
	}

	// --------------------------------------------------------------------------------

	/**
	 * This method converts an XML file into a JDOM Element. The path of the XML
	 * file must be provided.
	 * 
	 * @param xmlPath The path of the XML file to be converted.
	 * @return A JDOM element.
	 * @throws Exception If there is a problem.
	 */
	public static Element xmlToElement(String xmlPath) throws Exception {
		try {
			// String Filename = pathXMLObject + xmlPath + ".xml";
			org.jdom2.input.SAXBuilder builder = new org.jdom2.input.SAXBuilder();
			Document document = builder.build(new File(xmlPath));
			Element element = document.getRootElement();
			return element;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// --------------------------------------------------------------------------------

	/**
	 * This method creates an XML file, and puts the JDOM Element as its contents.
	 * 
	 * @param element  The JDOM Element to be stored.
	 * @param filename The file to be created.
	 * @return True if the creation was successfull. False otherwise.
	 */
	public static boolean createFile(Element element, File filename) {
		try {
			FileWriter fileWrite = new FileWriter(filename);
			XMLOutputter fmt = new XMLOutputter();
			fmt.output(element, fileWrite);
			fileWrite.flush();
			fileWrite.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// --------------------------------------------------------------------------------

	/**
	 * It generates a random number and returns it as string. This is normally used
	 * to be concatenated with the XML filename.
	 * 
	 * @return A random number.
	 */
	public static String generateRandomNo() {
		int id = 0;
		int range = Integer.MAX_VALUE / 3 * 2;
		Random rand = new Random();
		for (int i = 0; i < 1000; i++) {
			id = rand.nextInt(range);
		}
		return Integer.toString(id);
	}

	// ---------------------------------------------------------------------------------

	/**
	 * This method reads an XML file and returns its contents as String.
	 * 
	 * @param xmlFileName The name (path) of the XML file.
	 * @return The string (XML) representation of the XML file.
	 * @throws Exception If there is a problem.
	 */
	public static String convertXmlToString(String xmlFileName) throws Exception {
		File file = new File(xmlFileName);
		FileInputStream insr = new FileInputStream(file);
		byte[] fileBuffer = new byte[(int) file.length()];
		insr.read(fileBuffer);
		insr.close();
		return new String(fileBuffer);
	}

	/**
	 * This method converts a JDOM Element to a String in XML.
	 * 
	 * @param xmlElement The JDOM Element to be converted.
	 * @return The string representation of the JDOM Element (in XML).
	 */
	public static String getXmlElementAsString(Element xmlElement) {
		XMLOutputter fmt = new XMLOutputter();
		return fmt.outputString(xmlElement);
	}

}
