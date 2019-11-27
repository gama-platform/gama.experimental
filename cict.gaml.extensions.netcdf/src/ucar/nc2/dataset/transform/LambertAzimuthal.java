/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package ucar.nc2.dataset.transform;

import ucar.nc2.dataset.*;
import ucar.nc2.Variable;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.Earth;

/**
 * Create a LambertAzimuthal Projection from the information in the Coordinate Transform Variable.
 *
 * @author caron
 */
public class LambertAzimuthal extends AbstractCoordTransBuilder {

  public String getTransformName() {
    return "lambert_azimuthal_equal_area";
  }

  public TransformType getTransformType() {
    return TransformType.Projection;
  }

  public CoordinateTransform makeCoordinateTransform(NetcdfDataset ds, Variable ctv) {

    double lon0 = readAttributeDouble(ctv, "longitude_of_projection_origin", Double.NaN);
    double lat0 = readAttributeDouble(ctv, "latitude_of_projection_origin", Double.NaN);
    double false_easting = readAttributeDouble(ctv, "false_easting", 0.0);
    double false_northing = readAttributeDouble(ctv, "false_northing", 0.0);

    if ((false_easting != 0.0) || (false_northing != 0.0)) {
      double scalef = getFalseEastingScaleFactor(ds, ctv);
      false_easting *= scalef;
      false_northing *= scalef;
    }

    double earth_radius = getEarthRadius(ctv);

    ucar.unidata.geoloc.projection.LambertAzimuthalEqualArea proj =
            new ucar.unidata.geoloc.projection.LambertAzimuthalEqualArea(lat0, lon0, false_easting, false_northing,
                    earth_radius);

    return new ProjectionCT(ctv.getShortName(), "FGDC", proj);
  }
}

