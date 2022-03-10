package ummisco.gama.unity.wox.serial;

import org.jdom2.Element;

/**
 * The <code>ObjectReader</code> interface must be implemented by object readers
 * (de-serializers). It defines the <code>read</code> method, which takes a
 * JDOM element, and returns the live object.
 *
 * @author Simon M. Lucas <br />
 *         Carlos R. Jaimez Gonzalez
 * @version ObjectReader.java - 1.0
 */
public interface ObjectReader extends Serial {
    /**
     * This method reads a JDOM element, and returns a live object.
     * @param xob The JDOM element that represents the object.
     * @return A live object.
     */
    public Object read(Element xob);
}
