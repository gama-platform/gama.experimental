package ummisco.gama.unity.wox.serial;

import org.jdom2.Element;

/**
 * The <code>ObjectWriter</code> interface must be implemented by object writers
 * (serializers). It defines the <code>write</code> method, which takes a Java object
 * and converts it into a JDOM element.
 *
 * @author Simon M. Lucas <br />
 *         Carlos R. Jaimez Gonzalez 
 * @version ObjectWriter.java - 1.0
 */
public interface ObjectWriter extends Serial {
    /**
     * This method takes a Java object and converts it to a JDOM element.
     * @param o The java object to be written.
     * @return A JDOM Element representing the java object.
     */
    public Element write(Object o);
}
