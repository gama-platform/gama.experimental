package gama.experimental.database.extended;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import gama.experimental.database.extended.sql.MSSQLConnection;
import gama.experimental.database.extended.sql.MSSQLConnector;
import gama.extension.database.utils.sql.SqlUtils; 

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
