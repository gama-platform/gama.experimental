// $Id: UnitSymbol.java 64 2006-07-12 22:30:50Z edavis $
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
package ucar.units;

/**
 * Provides support for symbols for units.
 * 
 * @author Steven R. Emmerson
 * @version $Id: UnitSymbol.java 64 2006-07-12 22:30:50Z edavis $
 */
public final class UnitSymbol extends UnitID {
	private static final long	serialVersionUID	= 1L;
	/**
	 * The symbol for the unit.
	 * 
	 * @serial
	 */
	private final String		symbol;

	/**
	 * Constructs from a symbol.
	 * 
	 * @param symbol
	 *            The symbol for the unit. Shall not be <code>
     *				null</code>.
	 */
	public UnitSymbol(final String symbol) throws NameException {
		if (symbol == null) {
			throw new NameException("Symbol can't be null");
		}
		this.symbol = symbol;
	}

	/**
	 * Returns the name of the unit. Always returns <code>null</code>.
	 * 
	 * @return <code>null</code>.
	 */
	@Override
	public String getName() {
		return null;
	}

	/**
	 * Returns the plural form of the name of the unit. Always returns
	 * <code>null</code>.
	 * 
	 * @return <code>null</code>.
	 */
	@Override
	public String getPlural() {
		return null;
	}

	/**
	 * Returns the symbol for the unit.
	 * 
	 * @return The symbol for the unit. Never <code>null
     *				</code>.
	 */
	@Override
	public String getSymbol() {
		return symbol;
	}

	/**
	 * Returns the string representation of this identifier.
	 * 
	 * @return The string representation of this identifier.
	 */
	@Override
	public String toString() {
		return getSymbol();
	}
}
