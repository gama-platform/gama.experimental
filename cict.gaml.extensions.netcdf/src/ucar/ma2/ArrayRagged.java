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
package ucar.ma2;

/**
 * DO NOT USE
 * @author caron
 * @since Nov 15, 2008
 */
public class ArrayRagged extends Array {

  protected ArrayRagged(int[] shape) {
    super(shape);
  }


  public Class getElementType() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   * DO NOT USE, throws UnsupportedOperationException
   */
  public Array createView(Index index) {
    if (index.getSize() == getSize()) return this;
    throw new UnsupportedOperationException();
  }

  public Object getStorage() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }// used to create Array from java array

  void copyFrom1DJavaArray(IndexIterator iter, Object javaArray) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  void copyTo1DJavaArray(IndexIterator iter, Object javaArray) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * DO NOT USE, throws UnsupportedOperationException
   */
  public Array copy() {
    throw new UnsupportedOperationException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public double getDouble(Index i) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public void setDouble(Index i, double value) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public float getFloat(Index i) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public void setFloat(Index i, float value) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public long getLong(Index i) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public void setLong(Index i, long value) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public int getInt(Index i) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public void setInt(Index i, int value) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public short getShort(Index i) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public void setShort(Index i, short value) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public byte getByte(Index i) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public void setByte(Index i, byte value) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public boolean getBoolean(Index i) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public void setBoolean(Index i, boolean value) {
    throw new ForbiddenConversionException();
  }

  public Object getObject(Index ima) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void setObject(Index ima, Object value) {
//To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public char getChar(Index i) {
    throw new ForbiddenConversionException();
  }

  /**
   * DO NOT USE, throw ForbiddenConversionException
   */
  public void setChar(Index i, char value) {
    throw new ForbiddenConversionException();
  }

  // trusted, assumes that individual dimension lengths have been checked
  // package private : mostly for iterators
  public double getDouble(int index) {
    throw new ForbiddenConversionException();
  }

  public void setDouble(int index, double value) {
    throw new ForbiddenConversionException();
  }

  public float getFloat(int index) {
    throw new ForbiddenConversionException();
  }

  public void setFloat(int index, float value) {
    throw new ForbiddenConversionException();
  }

  public long getLong(int index) {
    throw new ForbiddenConversionException();
  }

  public void setLong(int index, long value) {
    throw new ForbiddenConversionException();
  }

  public int getInt(int index) {
    throw new ForbiddenConversionException();
  }

  public void setInt(int index, int value) {
    throw new ForbiddenConversionException();
  }

  public short getShort(int index) {
    throw new ForbiddenConversionException();
  }

  public void setShort(int index, short value) {
    throw new ForbiddenConversionException();
  }

  public byte getByte(int index) {
    throw new ForbiddenConversionException();
  }

  public void setByte(int index, byte value) {
    throw new ForbiddenConversionException();
  }

  public char getChar(int index) {
    throw new ForbiddenConversionException();
  }

  public void setChar(int index, char value) {
    throw new ForbiddenConversionException();
  }

  public boolean getBoolean(int index) {
    throw new ForbiddenConversionException();
  }

  public void setBoolean(int index, boolean value) {
    throw new ForbiddenConversionException();
  }

  public Object getObject(int elem) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void setObject(int elem, Object value) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

}
