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
import ucar.nc2.Dimension;
import ucar.unidata.geoloc.vertical.OceanSigma;
import ucar.unidata.util.Parameter;

/**
 * Create a ocean_sigma_coordinate Vertical Transform from the information in the Coordinate Transform Variable.
 *
 * @author caron
 */
public class VOceanSigma extends AbstractCoordTransBuilder {
  private String sigma,eta,depth;

  public String getTransformName() {
    return "ocean_sigma_coordinate";
  }

  public TransformType getTransformType() {
    return TransformType.Vertical;
  }

  public CoordinateTransform makeCoordinateTransform(NetcdfDataset ds, Variable ctv) {
    String formula_terms = getFormula(ds, ctv);
    if (null == formula_terms) return null;

    String[] values = parseFormula(formula_terms, "sigma eta depth");
    if (values == null) return null;
    
    sigma = values[0];
    eta = values[1];
    depth = values[2];

    CoordinateTransform rs = new VerticalCT("OceanSigma_Transform_"+ctv.getShortName(), getTransformName(), VerticalCT.Type.OceanSigma, this);
    rs.addParameter(new Parameter("standard_name", getTransformName()));
    rs.addParameter(new Parameter("formula_terms", formula_terms));
    rs.addParameter((new Parameter("height_formula", "height(x,y,z) = eta(n,j,i) + sigma(k)*(depth(j,i)+eta(n,j,i))")));

    if (!addParameter(rs, OceanSigma.SIGMA, ds, sigma)) return null;
    if (!addParameter(rs, OceanSigma.ETA, ds, eta)) return null;
    if (!addParameter(rs, OceanSigma.DEPTH, ds, depth)) return null;

    return rs;
  }


  public String toString() {
    return "OceanS:" + " sigma:"+ sigma + " eta:"+eta + " depth:"+depth;
  }

  public ucar.unidata.geoloc.vertical.VerticalTransform makeMathTransform(NetcdfDataset ds, Dimension timeDim, VerticalCT vCT) {
    return new OceanSigma(ds, timeDim, vCT.getParameters());
  }
}

