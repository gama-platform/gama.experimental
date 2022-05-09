package irit.gama.extensions.database.extended;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import irit.gama.extensions.database.extended.sql.MSSQLConnection;
import irit.gama.extensions.database.extended.sql.MSSQLConnector;
import msi.gama.database.sql.SqlUtils;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		SqlUtils.externalConnectors.put(MSSQLConnection.MSSQL,new MSSQLConnector());
	}

// 		} else if (dbtype.equalsIgnoreCase(SqlConnection.MSSQL)) {
// 	sqlConn = new MSSQLConnection(dbtype, host, port, database, user, passwd, transform);
	
	
	@Override
	public void stop(BundleContext context) throws Exception {}
}
