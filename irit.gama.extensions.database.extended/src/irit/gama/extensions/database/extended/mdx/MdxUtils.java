/*******************************************************************************************************
 *
 * MdxUtils.java, in irit.gaml.extensions.database, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.8.2).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package irit.gama.extensions.database.extended.mdx;

import java.util.Map;

import gama.core.common.util.FileUtils;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.dev.DEBUG;

/**
 * The Class MdxUtils.
 */
public class MdxUtils {

	/**
	 * Creates the connection object.
	 *
	 * @param scope the scope
	 * @param params the params
	 * @return the mdx connection
	 * @throws GamaRuntimeException the gama runtime exception
	 */
	public static MdxConnection createConnectionObject(final IScope scope, final Map<String, Object> params)
			throws GamaRuntimeException {
		final String olaptype = (String) params.get("olaptype");
		final String dbtype = (String) params.get("dbtype");
		final String host = (String) params.get("host");
		final String port = (String) params.get("port");
		final String database = (String) params.get("database");
		String catalog = (String) params.get("catalog");
		final String user = (String) params.get("user");
		final String passwd = (String) params.get("passwd");
		if (DEBUG.IS_ON()) {
			DEBUG.OUT("MdxlUtils.createConnectionObject:" + olaptype + " - " + dbtype + " - " + host + " - " + port
					+ " - " + database + " - " + catalog + " - " + user + " - " + passwd);
		}
		MdxConnection mdxConn;
		// create connection
		if (olaptype.equalsIgnoreCase(MdxConnection.MSAS)) {

			mdxConn = new MSASConnection(olaptype, host, port, database, user, passwd);
		} else if (olaptype.equalsIgnoreCase(MdxConnection.MONDRIAN)) {
			final String fullPath = FileUtils.constructAbsoluteFilePath(scope, catalog, true);
			// catalog=fullPath.replace('\\', '/');
			catalog = fullPath;
			if (DEBUG.IS_ON()) {
				DEBUG.OUT("MdxlUtils.createConnectionObject- full path:" + fullPath);
				DEBUG.OUT("MdxlUtils.createConnectionObject- Catalog path:" + catalog);
			}

			if (DEBUG.IS_ON()) {
				DEBUG.OUT("MdxlUtils.createConnectionObject.catalog.Mondrian:" + olaptype + " - " + dbtype + " - "
						+ " - " + host + " - " + port + " - " + database + " - " + catalog + " - " + user + " - "
						+ passwd + " - ");
			}

			mdxConn = new MondrianConnection(olaptype, dbtype, host, port, database, catalog, user, passwd);
			if (DEBUG.IS_ON()) {
				DEBUG.OUT("MdxlUtils.createConnectionObject.connectionObject.Mondrian.Object:" + mdxConn.toString());
			}

		} else if (olaptype.equalsIgnoreCase(MdxConnection.MONDRIANXMLA)) {
			if (DEBUG.IS_ON()) {
				DEBUG.OUT("MdxlUtils.createConnectionObject.catalog.MondrianXMLA:" + olaptype + " - " + dbtype + " - "
						+ " - " + host + " - " + port + " - " + database + " - " + catalog + " - " + user + " - "
						+ passwd + " - ");
			}

			mdxConn = new MondrianXmlaConnection(olaptype, dbtype, host, port, database, catalog, user, passwd);
			if (DEBUG.IS_ON()) {
				DEBUG.OUT(
						"MdxlUtils.createConnectionObject.connectionObject.MondrianXMLA.Object:" + mdxConn.toString());
			}

		} else {
			throw GamaRuntimeException.error("GAMA does not support: " + olaptype, scope);
		}
		if (DEBUG.IS_ON()) {
			DEBUG.OUT("MdxUtils.createConnection:" + mdxConn.toString());
		}
		return mdxConn;
	}

}
