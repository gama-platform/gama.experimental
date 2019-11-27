package thredds.crawlabledataset;

import java.io.File;

/**
 * An object that can be asked whether it can return a java.io.File representation of itself
 * and the method by which to request such a representation.
 *
 * @author edavis
 * @since 4.2
 */
public interface CheckableForFileRepresentation
{
  boolean canRepresentAsFile();
  File getFile();
}
