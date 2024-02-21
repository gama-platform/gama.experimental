/*******************************************************************************************************
 *
 * MSSQLConnection.java, in irit.gaml.extensions.database, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.8.2).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package irit.gama.extensions.database.extended.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import gama.extension.database.utils.sql.SqlConnection;
import gama.extension.database.utils.sql.SqlUtils;
import gama.core.metamodel.topology.projection.IProjection;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.dev.DEBUG;

/**
 * The Class MSSQLConnection.
 */
/*
 * @Author TRUONG Minh Thai Fredric AMBLARD Benoit GAUDOU Christophe Sibertin-BLANC Created date: 19-Apr-2013 Modified:
 * * 26-Apr-2013: Remove driver msi.gama.ext/sqljdbc4.jar add driver msi.gama.ext/jtds-1.2.6.jar Change driver name for
 * MSSQL from com.microsoft.sqlserver.jdbc.SQLServerDriver to net.sourceforge.jtds.jdbc.Driver Edit ConnectDB for new
 * driver Add new condition for geometry type 2004 (it look like postgres) 15-Jan-2014 Fix null error of getInsertString
 * methods Fix date/time error of getInsertString methods
 *
 * Last Modified: 15-Jan-2014
 */
public class MSSQLConnection extends SqlConnection {

	/** The Constant MSSQL. */
	public static final String MSSQL = "sqlserver";
	
	/** The Constant MSSQLDriver. */
	public static final String MSSQLDriver = "net.sourceforge.jtds.jdbc.Driver";	
	
	/** The Constant WKT2GEO. */
	private static final String WKT2GEO = "geometry::STGeomFromText";
	
	/** The Constant SRID. */
	private static final String SRID = "0"; // must solve later
	
	/** The Constant PREFIX_TIMESTAMP. */
	private static final String PREFIX_TIMESTAMP = "cast('";
	
	/** The Constant MID_TIMESTAMP. */
	private static final String MID_TIMESTAMP = "' as ";
	
	/** The Constant SUPFIX_TIMESTAMP. */
	private static final String SUPFIX_TIMESTAMP = ")";

	/**
	 * Instantiates a new MSSQL connection.
	 *
	 * @param venderName the vender name
	 * @param url the url
	 * @param port the port
	 * @param dbName the db name
	 * @param userName the user name
	 * @param password the password
	 * @param transformed the transformed
	 */
	MSSQLConnection(final String venderName, final String url, final String port, final String dbName,
			final String userName, final String password, final Boolean transformed) {
		super(venderName, url, port, dbName, userName, password, transformed);
	}

	@Override
	public Connection connectDB()
			throws ClassNotFoundException, InstantiationException, SQLException, IllegalAccessException {
		Connection conn = null;
		try {
			if (vender.equalsIgnoreCase(MSSQL)) {
				// Class.forName(MSSQLDriver).newInstance();
				// conn =
				// DriverManager.getConnection("jdbc:sqlserver://" + url + ":" +
				// port + ";databaseName=" + dbName +
				// ";user=" + userName + ";password=" + password + ";");
				Class.forName(MSSQLDriver).newInstance();
				conn = DriverManager.getConnection("jdbc:jtds:sqlserver://" + url + ":" + port + "/" + dbName, userName,
						password);
			} else {
				throw new ClassNotFoundException("MSSQLConnection.connectDB: The " + vender + " is not supported!");
			}
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
			throw new ClassNotFoundException(e.toString());
		} catch (final InstantiationException e) {
			e.printStackTrace();
			throw new InstantiationException(e.toString());
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
			throw new IllegalAccessException(e.toString());
		} catch (final SQLException e) {
			e.printStackTrace();
			throw new SQLException(e.toString());
		}
		return conn;

	}

	@Override
	protected IList<IList<Object>> resultSet2GamaList(final ResultSetMetaData rsmd, final ResultSet rs) {
		// convert Geometry in SQL to Geometry type in GeoTool
		final IList<IList<Object>> repRequest = GamaListFactory.create(gama.gaml.types.Types.LIST);
		try {
			final List<Integer> geoColumn = getGeometryColumns(rsmd);
			final int nbCol = rsmd.getColumnCount();
			while (rs.next()) {

				final IList<Object> rowList = GamaListFactory.create();
				for (int j = 1; j <= nbCol; j++) {
					if (geoColumn.contains(j)) {
						rowList.add(SqlUtils.read(rs.getBytes(j)));
					} else {
						rowList.add(rs.getObject(j));
					}
				}
				repRequest.add(rowList);
				// i++;
			}
		} catch (final Exception e) {

		}
		return repRequest;

	}

	@Override
	protected List<Integer> getGeometryColumns(final ResultSetMetaData rsmd) throws SQLException {
		final int numberOfColumns = rsmd.getColumnCount();
		final List<Integer> geoColumn = new ArrayList<>();
		for (int i = 1; i <= numberOfColumns; i++) {
			if (vender.equalsIgnoreCase(MSSQL) && rsmd.getColumnType(i) == 2004) {
				geoColumn.add(i);
			}
		}
		return geoColumn;

	}

	@Override
	protected IList<Object> getColumnTypeName(final ResultSetMetaData rsmd) throws SQLException {
		final int numberOfColumns = rsmd.getColumnCount();
		final IList<Object> columnType = GamaListFactory.create();
		for (int i = 1; i <= numberOfColumns; i++) {
			if (vender.equalsIgnoreCase(MSSQL) && rsmd.getColumnType(i) == 2004) {
				columnType.add(GEOMETRYTYPE);
			} else {
				columnType.add(rsmd.getColumnTypeName(i).toUpperCase());
			}
		}
		return columnType;

	}

	@Override
	protected String getInsertString(final IScope scope, final Connection conn, final String table_name,
			final IList<Object> cols, final IList<Object> values) throws GamaRuntimeException {
		final int col_no = cols.size();
		String insertStr = "INSERT INTO ";
		String selectStr = "SELECT ";
		String colStr = "";
		String valueStr = "";
		// Check size of parameters
		if (values.size() != col_no) {
			throw new IndexOutOfBoundsException("Size of columns list and values list are not equal");
		}
		// Get column name
		for (int i = 0; i < col_no; i++) {
			if (i == col_no - 1) {
				colStr = colStr + (String) cols.get(i);
			} else {
				colStr = colStr + (String) cols.get(i) + ",";
			}
		}
		// create SELECT statement string
		selectStr = selectStr + " TOP 1 " + colStr + " FROM " + table_name + " ;";

		try {
			// get column type;
			final Statement st = conn.createStatement();
			final ResultSet rs = st.executeQuery(selectStr);
			final ResultSetMetaData rsmd = rs.getMetaData();
			final IList<Object> col_Names = getColumnName(rsmd);
			final IList<Object> col_Types = getColumnTypeName(rsmd);

			if (DEBUG.IS_ON()) {
				DEBUG.OUT("list of column Name:" + col_Names);
				DEBUG.OUT("list of column type:" + col_Types);
			}
			// Insert command
			// set parameter value
			valueStr = "";
			final IProjection saveGis = getSavingGisProjection(scope);
			for (int i = 0; i < col_no; i++) {
				// Value list begin-------------------------------------------
				if (values.get(i) == null) {
					valueStr = valueStr + NULLVALUE;
				} else if (((String) col_Types.get(i)).equalsIgnoreCase(GEOMETRYTYPE)) {

					// 23/Jul/2013 - Transform GAMA GIS TO NORMAL
					final WKTReader wkt = new WKTReader();
					Geometry geo = wkt.read(values.get(i).toString());
					// DEBUG.LOG(geo.toString());
					if (transformed) {
						geo = saveGis.inverseTransform(geo);
					}
					// DEBUG.LOG(geo.toString());
					valueStr = valueStr + WKT2GEO + "('" + geo.toString() + "', " + SRID + ")";

				} else if (((String) col_Types.get(i)).equalsIgnoreCase(CHAR)
						|| ((String) col_Types.get(i)).equalsIgnoreCase(VARCHAR)
						|| ((String) col_Types.get(i)).equalsIgnoreCase(NVARCHAR)
						|| ((String) col_Types.get(i)).equalsIgnoreCase(TEXT)) { // for
																					// String
																					// type
					// Correct error string
					String temp = values.get(i).toString();
					temp = temp.replaceAll("'", "''");
					// Add to value:
					valueStr = valueStr + "'" + temp + "'";
				} else if (((String) col_Types.get(i)).equalsIgnoreCase(TIMESTAMP)) { // For
																						// timestamp
					valueStr = valueStr + PREFIX_TIMESTAMP + values.get(i).toString() + MID_TIMESTAMP + TIMESTAMP
							+ SUPFIX_TIMESTAMP;
				} else if (((String) col_Types.get(i)).equalsIgnoreCase(DATETIME)) { // For
																						// datetime
					valueStr = valueStr + PREFIX_TIMESTAMP + values.get(i).toString() + MID_TIMESTAMP + DATETIME
							+ SUPFIX_TIMESTAMP;
				} else if (((String) col_Types.get(i)).equalsIgnoreCase(DATE)) { // For
																					// datetime
					valueStr = valueStr + PREFIX_TIMESTAMP + values.get(i).toString() + MID_TIMESTAMP + DATE
							+ SUPFIX_TIMESTAMP;
				} else { // For other type
					valueStr = valueStr + values.get(i).toString();
				}
				if (i != col_no - 1) { // Add delimiter of each value
					valueStr = valueStr + ",";
				}
				// Value list
				// end--------------------------------------------------------

			}
			insertStr = insertStr + table_name + "(" + colStr + ") " + "VALUES(" + valueStr + ")";

			if (DEBUG.IS_ON()) {
				DEBUG.OUT("MSSQLConnection.getInsertString:" + insertStr);
			}

		} catch (final SQLException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("MSSQLConnection.getInsertString " + e.toString(), scope);
		} catch (final ParseException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("MSSQLConnection.getInsertString " + e.toString(), scope);
		}

		return insertStr;
	}

	@Override
	protected String getInsertString(final IScope scope, final Connection conn, final String table_name,
			final IList<Object> values) throws GamaRuntimeException {
		// TODO Auto-generated method stub
		String insertStr = "INSERT INTO ";
		String selectStr = "SELECT ";
		String colStr = "";
		String valueStr = "";

		// Get column name
		// create SELECT statement string
		selectStr = selectStr + " TOP 1 * " + " FROM " + table_name + " ;";
		try {
			// get column type;
			final Statement st = conn.createStatement();
			final ResultSet rs = st.executeQuery(selectStr);
			final ResultSetMetaData rsmd = rs.getMetaData();
			final IList<Object> col_Names = getColumnName(rsmd);
			final IList<Object> col_Types = getColumnTypeName(rsmd);
			final int col_no = col_Names.size();
			// Check size of parameters
			if (values.size() != col_Names.size()) {
				throw new IndexOutOfBoundsException("Size of columns list and values list are not equal");
			}

			// Insert command
			// set parameter value
			colStr = "";
			valueStr = "";
			for (int i = 0; i < col_no; i++) {
				// Value list begin-------------------------------------------
				if (values.get(i) == null) {
					valueStr = valueStr + NULLVALUE;
				} else if (((String) col_Types.get(i)).equalsIgnoreCase(GEOMETRYTYPE)) {
					// 23/Jul/2013 - Transform GAMA GIS TO NORMAL
					final WKTReader wkt = new WKTReader();
					Geometry geo = wkt.read(values.get(i).toString());
					// DEBUG.LOG(geo.toString());

					if (transformed) {
						geo = getSavingGisProjection(scope).inverseTransform(geo);
					}
					// DEBUG.LOG(geo.toString());
					valueStr = valueStr + WKT2GEO + "('" + geo.toString() + "', " + SRID + ")";

				} else if (((String) col_Types.get(i)).equalsIgnoreCase(CHAR)
						|| ((String) col_Types.get(i)).equalsIgnoreCase(VARCHAR)
						|| ((String) col_Types.get(i)).equalsIgnoreCase(NVARCHAR)
						|| ((String) col_Types.get(i)).equalsIgnoreCase(TEXT)) { // for
																					// String
																					// type
																					// Correct
																					// error
																					// string
					String temp = values.get(i).toString();
					temp = temp.replaceAll("'", "''");
					// Add to value:
					valueStr = valueStr + "'" + temp + "'";
				} else if (((String) col_Types.get(i)).equalsIgnoreCase(TIMESTAMP)) { // For
																						// timestamp
					valueStr = valueStr + PREFIX_TIMESTAMP + values.get(i).toString() + MID_TIMESTAMP + TIMESTAMP
							+ SUPFIX_TIMESTAMP;
				} else if (((String) col_Types.get(i)).equalsIgnoreCase(DATETIME)) { // For
																						// datetime
					valueStr = valueStr + PREFIX_TIMESTAMP + values.get(i).toString() + MID_TIMESTAMP + DATETIME
							+ SUPFIX_TIMESTAMP;
				} else if (((String) col_Types.get(i)).equalsIgnoreCase(DATE)) { // For
																					// datetime
					valueStr = valueStr + PREFIX_TIMESTAMP + values.get(i).toString() + MID_TIMESTAMP + DATE
							+ SUPFIX_TIMESTAMP;
				} else { // For other type
					valueStr = valueStr + values.get(i).toString();
				}
				// Value list
				// end--------------------------------------------------------
				// column list
				colStr = colStr + col_Names.get(i).toString();

				if (i != col_no - 1) { // Add delimiter of each value
					colStr = colStr + ",";
					valueStr = valueStr + ",";
				}
			}

			insertStr = insertStr + table_name + "(" + colStr + ") " + "VALUES(" + valueStr + ")";

			if (DEBUG.IS_ON()) {
				DEBUG.OUT("SqlConection.getInsertString:" + insertStr);
			}

		} catch (final SQLException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("MSSQLConnection.insertBD " + e.toString(), scope);
		} catch (final ParseException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("MSSQLConnection.insertBD " + e.toString(), scope);
		}

		return insertStr;
	}
	
	@Override
	public void close() throws Exception {
		throw new NotImplementedException("Close not implemented");
	}
	
	
}
