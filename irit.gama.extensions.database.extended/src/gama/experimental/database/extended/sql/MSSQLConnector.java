package gama.experimental.database.extended.sql;

import java.util.HashMap;
import java.util.Map;

import gama.extension.database.utils.sql.ISqlConnector;
import gama.extension.database.utils.sql.SqlConnection;
import gama.core.runtime.IScope;

public class MSSQLConnector implements ISqlConnector {

	@Override
	public SqlConnection connection(final IScope scope, String venderName, String url, String port, String dbName, String userName,
			String password, Boolean transformed) {
		return new MSSQLConnection(venderName, url, port, dbName, userName, password, transformed);
	}

	@Override
	public Map<String, Object> getConnectionParameters(final IScope scope, String host, String dbtype, String port, String database,
			String user, String passwd) {

		Map<String, Object> connectionParameters = new HashMap<>();

		connectionParameters.put("host", host);
		connectionParameters.put("dbtype", dbtype);
		connectionParameters.put("port", port);
		connectionParameters.put("database", database);
		connectionParameters.put("user", user);
		connectionParameters.put("passwd", passwd);

		return connectionParameters;
	}

}
