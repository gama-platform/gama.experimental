package irit.gama.extensions.database.extended.sql;

import java.util.HashMap;
import java.util.Map;

import msi.gama.database.sql.ISqlConnector;
import msi.gama.database.sql.SqlConnection;

public class MSSQLConnector implements ISqlConnector {

	@Override
	public SqlConnection connection(String venderName, String url, String port, String dbName, String userName,
			String password, Boolean transformed) {
		return new MSSQLConnection(venderName, url, port, dbName, userName, password, transformed);
	}

	@Override
	public Map<String, Object> getConnectionParameters(String host, String dbtype, String port, String database,
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
