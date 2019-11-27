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
package ucar.nc2.ft;

import ucar.nc2.units.DateUnit;
import ucar.unidata.geoloc.EarthLocation;

import java.util.Date;

/**
 * A collection of observations at one time and location.
 * @author caron
 * @since Feb 29, 2008
 */
public interface PointFeature {

  /**
   * Location of this observation
   * @return the location of this observation
   */
  public EarthLocation getLocation();

 /**
   * Actual time of this observation.
   * Convert to Date with getTimeUnit().makeDate()
   * @return actual time of this observation.
   */
  public double getObservationTime();

  /**
   * Actual time of this observation, as a Date.
   * @return actual time of this observation, as a Date.
   */
  public Date getObservationTimeAsDate();

  /**
   * Nominal time of this observation.
   * Convert to Date with getTimeUnit().makeDate().
   * When the nominal time is not given in the data, it is usually set to the observational time.
   * @return Nominal time of this observation.
   */
  public double getNominalTime();

  /**
   * Nominal time of this observation, as a Date.
   * @return Nominal time of this observation, as a Date.
   */
  public Date getNominalTimeAsDate();

  /**
   * Get the time unit of the time coordinate.
   * @return time unit of the time coordinate
   */
  public DateUnit getTimeUnit();

  /**
   * The actual data of this observation.
   * @return the actual data of this observation.
   * @throws java.io.IOException on i/o error
   */
  public ucar.ma2.StructureData getData() throws java.io.IOException;

}
