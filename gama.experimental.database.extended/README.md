[//]: # (startConcept|use_database)
[//]: # (keyword|concept_database)
# Using Database Access

Database features of GAMA provide a set of actions on Database Management Systems (DBMS) and Multi-Dimensional Database for agents in GAMA. Database features are implemented in the irit.gaml.extensions.database plug-in with these features:

* Agents can execute SQL queries (create, Insert, select, update, drop, delete) to various kinds of DBMS.
* Agents can execute MDX (Multidimensional Expressions) queries to select multidimensional objects, such as cubes, and return multidimensional cellsets that contain the cube's data.

These features are implemented in two kinds of component: _skills_ (SQLSKILL, MDXSKILL) and agent (AgentDB)

SQLSKILL and AgentDB provide almost the same features (a same set of actions on DBMS) but with certain slight differences:

* An agent of species AgentDB will maintain a unique connection to the database during the whole simulation. The connection is thus initialized when the agent is created.
* In contrast, an agent of a species with the SQLSKILL skill will open a connection each time he wants to execute a query. This means that each action will be composed of three running steps:
  * Make a database connection.
  * Execute SQL statement.
  * Close database connection.

> An agent with the SQLSKILL spends lot of time to create/close the connection each time it needs to send a query; it saves the database connection (DBMS often limit the number of simultaneous connections). In contrast, an AgentDB agent only needs to establish one database connection and it can be used for any actions. Because it does not need to create and close database connection for each action: therefore, actions of AgentDB agents are executed faster than actions of SQLSKILL ones but we must pay a connection for each agent.

* With an inheritance agent of species AgentDB  or an agent of a species using SQLSKILL, we can query data from relational database for creating species, defining environment or analyzing or storing simulation results into RDBMS. On the other hand, an agent of species with MDXKILL supports the OLAP technology to query data from data marts (multidimensional database).
The database features help us to have more flexibility in management of simulation models and analysis of simulation results.








## Description

* **Plug-in**: _irit.gaml.extensions.database_
* **Author**: TRUONG Minh Thai, Frederic AMBLARD, Benoit GAUDOU, Christophe SIBERTIN-BLANC




## Supported DBMS

The following DBMS are currently supported:

* SQLite
* MySQL Server
* PostgreSQL Server
* SQL Server
* Mondrian OLAP Server
* SQL Server Analysis Services

Note that, other DBMSs require a dedicated server to work while SQLite only needs a file to be accessed.
All the actions can be used independently from the chosen DBMS. Only the connection parameters are DBMS-dependent.




## SQLSKILL
### Define a species that uses the SQLSKILL skill

Example of declaration:

```
species toto skills: [SQLSKILL] {
	//insert your descriptions here
}
```

Agents with such a skill can use additional actions (defined in the skill)

### Map of connection parameters for SQL

In the actions defined in the SQLSkill, a parameter containing the connection parameters is required. It is a map with the following _key::value_ pairs:

| **Key** | **Optional** | **Description** |
|:-------|:--------|:--------------------------------------------------------|
| _dbtype_ | No | DBMS type value. Its value is a string. We must use "mysql" when we want to connect to a MySQL. That is the same for "postgres", "sqlite" or "sqlserver" (ignore case sensitive) |
| _host_  | Yes | Host name or IP address of data server. It is absent when we work with SQlite. |
| _port_  | Yes | Port of connection. It is not required when we work with SQLite.|
| _database_ | No | Name of database. It is the file name including the path when we work with SQLite. |
| _user_  | Yes | Username. It is not required when we work with SQLite. |
| _passwd_ | Yes | Password. It is not required when we work with SQLite. |
| srid    | Yes | srid (Spatial Reference Identifier)  corresponds to a spatial reference system. This value is specified when GAMA connects to spatial database. If it is absent then GAMA uses spatial reference system defined in _Preferences->External_ configuration. |

**Table 1**: Connection parameter description



**Example**: Definitions of connection parameter

```
// POSTGRES connection parameter
map <string, string>  POSTGRES <- [
     'host'::'localhost',
     'dbtype'::'postgres',
     'database'::'BPH',
     'port'::'5433',
     'user'::'postgres',
     'passwd'::'abc'];

//SQLite
map <string, string>  SQLITE <- [
    'dbtype'::'sqlite',
    'database'::'../includes/meteo.db'];

// SQLSERVER connection parameter
map <string, string> SQLSERVER <- [
    'host'::'localhost',
    'dbtype'::'sqlserver',
    'database'::'BPH',
    'port'::'1433',
    'user'::'sa',
    'passwd'::'abc'];

// MySQL connection parameter
map <string, string>  MySQL <- [
    'host'::'localhost',
    'dbtype'::'MySQL',
    'database'::'', // it may be a null string
    'port'::'3306',
    'user'::'root',
    'passwd'::'abc'];
```

### Test a connection to database
**Syntax**:
> _**testConnection** (params: connection`_`parameter)_
The action tests the connection to a given database.

* **Return**: boolean. It is:
  * _true_: the agent can connect to the DBMS (to the given Database with given name and password)
  * _false_: the agent cannot connect
* **Arguments**:
  * _params_: (type = map) map of connection parameters
* **Exceptions**: _GamaRuntimeException_

**Example**: Check a connection to MySQL

```
if (self testConnection(params:MySQL)){
	write "Connection is OK" ;
}else{
	write "Connection is false" ;
}	

```

### Select data from database
**Syntax**:
> _**select** (param: connection`_`parameter, select: selection`_`string,values: value`_`list)_
The action creates a connection to a DBMS and executes the select statement. If the connection or selection fails then it throws a GamaRuntimeException.

* **Return**: list < list >. If the selection succeeds, it returns a list with three elements:
  * The first element is a list of column name.
  * The second element is a list of column type.
  * The third element is a data set.
* **Arguments**:
  * _params_: (type = map) map containing the connection parameters
  * _select_: (type = string) select string. The selection string can contain question marks.
  * _values_: List of values that are used to replace question marks in appropriate. This is an optional parameter.
* **Exceptions**: _GamaRuntimeException_

**Example**: select data from table points

```
map <string, string>   PARAMS <- ['dbtype'::'sqlite', 'database'::'../includes/meteo.db'];
list<list> t <- list<list> (self select(params:PARAMS, 
		                 select:"SELECT * FROM points ;"));
```

**Example**: select data from table point with question marks from table points

```
map <string, string>   PARAMS <- ['dbtype'::'sqlite', 'database'::'../includes/meteo.db'];
list<list> t <- list<list> (self select(params: PARAMS, 
                                           select: "SELECT temp_min FROM points where (day>? and day<?);"
                                           values: [10,20] ));
```

### Insert data into database
**Syntax**:

> _**insert** (param: connection`_`parameter,  into:  table`_`name, columns: column`_`list, values: value`_list)_The action creates a connection to a DBMS and executes the insert statement. If the connection or insertion fails then it throws a_GamaRuntimeException_.

* **Return**: int

> If the insertion succeeds, it returns a number of records inserted by the insert.

* **Arguments**:
  *_params_: (type = map) map containing the connection parameters.
  *_into_: (type = string) table name.
  *_columns_: (type=list)  list of column names of table. It is an optional argument. If it is not applicable then all columns of table are selected.
  *_values_: (type=list) list of values that are used to insert into table corresponding to columns. Hence the columns and values must have same size.
* **Exceptions**:_GamaRuntimeException

**Example**: Insert data into table registration

```
map<string, string> PARAMS <- ['dbtype'::'sqlite', 'database'::'../../includes/Student.db'];

do insert (params: PARAMS, 
               into: "registration", 
               values: [102, 'Mahnaz', 'Fatma', 25]);

do insert (params: PARAMS, 
                into: "registration", 
                columns: ["id", "first", "last"], 
                values: [103, 'Zaid tim', 'Kha']);

int n <- insert (params: PARAMS, 
                        into: "registration", 
                       columns: ["id", "first", "last"], 
                       values: [104, 'Bill', 'Clark']);
```

### Execution update commands
**Syntax**:

> _**executeUpdate** (param: connection`_`parameter,  updateComm:  table`_`name, values: value`_`list)_
The action executeUpdate executes an update command (create/insert/delete/drop) by using the current database connection of the agent. If the database connection does not exist or the update command fails then it throws a GamaRuntimeException. Otherwise, it returns an integer value.

* **Return**: int. If the insertion succeeds, it returns a number of records inserted by the insert.
* **Arguments**:
  * _params_: (type = map) map containing the connection parameters
  * _updateComm_: (type = string) SQL command string. It may be commands: _create_, _update_, _delete_ and _drop_ with or without question marks.
  * _columns_: (type=list)  list of column names of table.
  * _values_: (type=list) list of values that are used to replace question marks if appropriate. This is an optional parameter.
* **Exceptions**: _GamaRuntimeException_

**Examples**: Using action executeUpdate do sql commands (create, insert, update, delete and drop).

```
map<string, string> PARAMS <- ['dbtype'::'sqlite',  'database'::'../../includes/Student.db'];
// Create table
do executeUpdate (params: PARAMS, 
                              updateComm: "CREATE TABLE registration" 
                                             + "(id INTEGER PRIMARY KEY, " 
                                             + " first TEXT NOT NULL, " + " last TEXT NOT NULL, " 
                                             + " age INTEGER);");

// Insert into 
do executeUpdate (params: PARAMS ,  
                             updateComm: "INSERT INTO registration " + "VALUES(100, 'Zara', 'Ali', 18);");
do insert (params: PARAMS, into: "registration", 
               columns: ["id", "first", "last"], 
               values: [103, 'Zaid tim', 'Kha']);

// executeUpdate with question marks
do executeUpdate (params: PARAMS,
                             updateComm: "INSERT INTO registration " + "VALUES(?, ?, ?, ?);" ,  
                             values: [101, 'Mr', 'Mme', 45]);

//update 
int n <-  executeUpdate (params: PARAMS, 
                                       updateComm: "UPDATE registration SET age = 30 WHERE id IN (100, 101)" );

// delete
int n <- executeUpdate (params: PARAMS, 
                                      updateComm: "DELETE FROM registration where id=? ",  
                                      values: [101] );

// Drop table
do executeUpdate (params: PARAMS, updateComm: "DROP TABLE registration");
```


## MDXSKILL
MDXSKILL plays the role of an OLAP tool using select to query data from OLAP server to GAMA environment and then species can use the queried data for any analysis purposes.

### Define a species that uses the MDXSKILL skill
Example of declaration:

``` 
	species olap skills: [MDXSKILL]
	 {  
		//insert your descriptions here
		
	 } 
      ...
```

Agents with such a skill can use additional actions (defined in the skill)

### Map of connection parameters for MDX
In the actions defined in the SQLSkill, a parameter containing the connection parameters is required. It is a map with following key::value pairs:

| **Key** | **Optional** | **Description** |
|:-------|:--------|:--------------------------------------------------------|
| _olaptype_ | No | OLAP Server type value. Its value is a string. We must use "SSAS/XMLA" when we want to connect to an SQL Server Analysis Services by using XML for Analysis. That is the same for "MONDRIAN/XML" or "MONDRIAN" (ignore case sensitive) |
| _dbtype_ | No | DBMS type value. Its value is a string. We must use "mysql" when we want to connect to a MySQL. That is the same for "postgres" or "sqlserver" (ignore case sensitive) |
| _host_ | No | Host name or IP address of data server. |
| _port_ | No | Port of connection. It is no required when we work with SQLite. |
| _database_ | No | Name of database. It is file name include path when we work with SQLite. |
| _catalog_ | Yes | Name of catalog. It is an optional parameter. We do not need to use it when we connect to SSAS via XMLA and its file name includes the path when we connect a ROLAP database directly by using Mondrian API (see Example as below) |
| _user_  | No | Username. |
| _passwd_ | No | Password. |

**Table 2**: OLAP Connection parameter description

**Example**: Definitions of OLAP connection parameter

```
//Connect SQL Server Analysis Services via XMLA
	map<string,string> SSAS <- [
				'olaptype'::'SSAS/XMLA',
				'dbtype'::'sqlserver',
				'host'::'172.17.88.166',
				'port'::'80',
				'database'::'olap',
				'user'::'test',
				'passwd'::'abc'];

//Connect Mondriam server via XMLA
	map<string,string>  MONDRIANXMLA <- [
				'olaptype'::"MONDRIAN/XMLA",
				'dbtype'::'postgres',
				'host'::'localhost',
				'port'::'8080',
				'database'::'MondrianFoodMart',
				'catalog'::'FoodMart',
				'user'::'test',
				'passwd'::'abc'];

//Connect a ROLAP server using Mondriam API	
	map<string,string>  MONDRIAN <- [
				'olaptype'::'MONDRIAN',
				'dbtype'::'postgres',
				'host'::'localhost',
				'port'::'5433',
				'database'::'foodmart',
				'catalog'::'../includes/FoodMart.xml',
				'user'::'test',
                                'passwd'::'abc'];
```

### Test a connection to OLAP database

**Syntax**:

> _**testConnection** (params: connection`_`parameter)_
The action tests the connection to a given OLAP database.

* **Return**: boolean.  It is:
  * _true_: the agent can connect to the DBMS (to the given Database with given name and password)
  * _false_: the agent cannot connect
* **Arguments**:
  * _params_: (type = map) map of connection parameters
* **Exceptions**: _GamaRuntimeException_

**Example**: Check a connection to MySQL

```
if (self testConnection(params:MONDIRANXMLA)){
	write "Connection is OK";
}else{
	write "Connection is false";
}	
```

### Select data from OLAP database

**Syntax**:

> _**select** (param: connection`_`parameter, onColumns: column`_`string, onRows: row`_`string from: cube`_`string, where: condition`_`string, values: value`_`list)_
The action creates a connection to an OLAP database and executes the select statement. If the connection or selection fails then it throws a _GamaRuntimeException_.

* **Return**: list < list >.  If the selection succeeds, it returns a list with three elements:
  * The first element is a list of column name.
  * The second element is a list of column type.
  * The third element is a data set.
* **Arguments**:
  * _params_: (type = map) map containing the connection parameters
  * _onColumns_: (type = string) declare  the select string on columns. The selection string can contain question marks.
  * _onRows_: (type = string) declare the selection string on rows. The selection string can contain question marks.
  * _from_: (type = string) specify cube where data is selected. The cube\_string can contain question marks.
  * where_: (type = string) specify the selection conditions. The condiction\_string can contains question marks. This is an optional parameter.
  *_values_: List of values that are used to replace question marks if appropriate. This is an optional parameter.
* **Exceptions**:_GamaRuntimeException

**Example**: select data from SQL Server Analysis Service via XMLA

```
if (self testConnection[ params::SSAS]){
	list l1  <- list(self select (params: SSAS ,
		onColumns: " { [Measures].[Quantity], [Measures].[Price] }",
		onRows:" { { { [Time].[Year].[All].CHILDREN } * "
		+ " { [Product].[Product Category].[All].CHILDREN } * "
		+"{ [Customer].[Company Name].&[Alfreds Futterkiste], " 
		+"[Customer].[Company Name].&[Ana Trujillo Emparedadosy helados], " 
		+ "[Customer].[Company Name].&[Antonio Moreno Taquería] } } } " ,
		from : "FROM [Northwind Star] "));
	write "result1:"+ l1;
}else {
	write "Connect error";
}
```

**Example**: select data from Mondrian via XMLA with question marks in selection

```
if (self testConnection(params:MONDRIANXMLA)){
	list<list> l2  <- list<list> (self select(params: MONDRIANXMLA, 
	onColumns:" {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Store Sales]} ",
	onRows:"  Hierarchize(Union(Union(Union({([Promotion Media].[All Media],"
 	+" [Product].[All Products])}, "
	+" Crossjoin([Promotion Media].[All Media].Children, "
	+" {[Product].[All Products]})), "
	+" Crossjoin({[Promotion Media].[Daily Paper, Radio, TV]}, "
	+" [Product].[All Products].Children)), "
	+" Crossjoin({[Promotion Media].[Street Handout]}, " 
	+" [Product].[All Products].Children)))  ",
	from:" from [?] " ,
	where :" where [Time].[?] " ,
	values:["Sales",1997]));
	write "result2:"+ l2;
}else {
	write "Connect error";
}
```




## AgentDB

AgentBD is a built-in species, which supports behaviors that look like actions in SQLSKILL but differs slightly with SQLSKILL in that it uses  only one connection for several actions. It means that AgentDB makes a connection to DBMS and keeps that connection for its later operations with DBMS.

### Define a species that is an inheritance of agentDB
Example of declaration:

```
species agentDB parent: AgentDB {  
	//insert your descriptions here
} 
```

### Connect to database

**Syntax**:

> _**Connect** (param: connection`_`parameter)_
This action makes a connection to DBMS. If a connection is established then it will assign the connection object into a built-in attribute of species (conn) otherwise it throws a GamaRuntimeException.

* **Return**: connection
* **Arguments**:
  * _params_: (type = map) map containing the connection parameters
* **Exceptions**: GamaRuntimeException

**Example**: Connect to PostgreSQL

```
// POSTGRES connection parameter
map <string, string>  POSTGRES <- [
                                        'host'::'localhost',
                                        'dbtype'::'postgres',
                                        'database'::'BPH',
                                        'port'::'5433',
                                        'user'::'postgres',
                                        'passwd'::'abc'];
ask agentDB {
      do connect (params: POSTGRES);
}
```

### Check agent connected a database or not

**Syntax**:

> _**isConnected** (param: connection`_`parameter)_
This action checks if an agent is connecting to database or not.

* **Return**: Boolean.   If agent is connecting to a database then isConnected returns true; otherwise it returns false.
* **Arguments**:
  * _params_: (type = map) map containing the connection parameters

**Example**: Using action executeUpdate do sql commands (create, insert, update, delete and drop).

```
ask agentDB {
	if (self isConnected){
              write "It already has a connection";
	}else{
              do connect (params: POSTGRES);
        } 
}
```

### Close the current connection

**Syntax**:

> _**close**_
This action closes the current database connection of species. If species does not has a database connection then it throws a GamaRuntimeException.

* **Return**: null

If the current connection of species is close then the action return null value; otherwise it throws a GamaRuntimeException.

**Example**:

```
ask agentDB {
	if (self isConnected){
	      do close;
	}
}
```

### Get connection parameter

**Syntax**:

> _**getParameter**_
This action returns the connection parameter of species.

* **Return**: map < string, string >

**Example**:

```
ask agentDB {
	if (self isConnected){
		write "the connection parameter: " +(self getParameter);
        }
}
```

### Set connection parameter

**Syntax**:

> _**setParameter** (param: connection`_`parameter)_
This action sets the new values for connection parameter and closes the current connection of species. If it can not close the current connection then it will throw GamaRuntimeException. If the species wants to make the connection to database with the new values then action connect must be called.

* **Return**: null
* **Arguments**:
  * _params_: (type = map) map containing the connection parameters
* **Exceptions**: _GamaRuntimeException_

**Example**:

```
ask agentDB {
	if (self isConnected){
             do setParameter(params: MySQL);
             do connect(params: (self getParameter));
        }
}
```

### Retrieve data from database by using AgentDB
Because AgentDB's connection to database is kept alive, it can execute several SQL queries using only the `connect` action once. Hence AgentDB can do actions such as **select**, **insert**, **executeUpdate** with the same parameters as those of SQLSKILL _except for the **params** parameter which is always absent_.

**Examples**:

```
map<string, string> PARAMS <- ['dbtype'::'sqlite', 'database'::'../../includes/Student.db'];
ask agentDB {
   do connect (params: PARAMS);
   // Create table
   do executeUpdate (updateComm: "CREATE TABLE registration" 
	+ "(id INTEGER PRIMARY KEY, " 
        + " first TEXT NOT NULL, " + " last TEXT NOT NULL, " 
        + " age INTEGER);");
   // Insert into 
   do executeUpdate ( updateComm: "INSERT INTO registration " 
        + "VALUES(100, 'Zara', 'Ali', 18);");
   do insert (into: "registration", 
	 columns: ["id", "first", "last"], 
	 values: [103, 'Zaid tim', 'Kha']);
   // executeUpdate with question marks
   do executeUpdate (updateComm: "INSERT INTO registration VALUES(?, ?, ?, ?);",  
	 values: [101, 'Mr', 'Mme', 45]);
   //select
    list<list> t <- list<list> (self select( 
	 select:"SELECT * FROM registration;"));
    //update 
    int n <-  executeUpdate (updateComm: "UPDATE registration SET age = 30 WHERE id IN (100, 101)");
     // delete
     int n <- executeUpdate ( updateComm: "DELETE FROM registration where id=? ",  values: [101] );
     // Drop table
      do executeUpdate (updateComm: "DROP TABLE registration");
}
```

## Using database features to define environment or create species

In Gama, we can use results of select action of SQLSKILL or AgentDB to create species or define boundary of environment in the same way we do with shape files. Further more, we can also save simulation data that are generated by simulation including geometry data to database.

### Define the boundary of the environment from database

* **Step 1**: specify select query by declaration a map object with keys as below:

| **Key** | **Optional** | **Description** |
|:-------|:--------|:--------------------------------------------------------|
| _dbtype_ | No | DBMS type value. Its value is a string. We must use "mysql" when we want to connect to a MySQL. That is the same for "postgres", "sqlite" or "sqlserver" (ignore case sensitive) |
| _host_  | Yes | Host name or IP address of data server. It is absent when we work with SQlite. |
| _port_  | Yes | Port of connection. It is not required when we work with SQLite. |
| _database_ | No | Name of database. It is the file name including the path when we work with SQLite. |
| _user_  | Yes | Username. It is  not required when we work with SQLite. |
| _passwd_ | Yes | Password. It is  not required when we work with SQLite. |
| _srid_  | Yes | srid (Spatial Reference Identifier)  corresponds to a spatial reference system. This value is specified when GAMA connects to spatial database. If it is absent then GAMA uses spatial reference system defined in Preferences->External configuration. |
| _select_ | No |Selection string |

**Table 3**: Select boundary parameter description

**Example**:

```
map<string,string> BOUNDS <- [	
	//'srid'::'32648',
	'host'::'localhost',								
        'dbtype'::'postgres',
	'database'::'spatial_DB',
	'port'::'5433',								
        'user'::'postgres',
	'passwd'::'tmt',
	'select'::'SELECT ST_AsBinary(geom) as geom FROM bounds;' ];
```

* **Step 2**: define boundary of environment by using the map object in first step.

```
geometry shape <- envelope(BOUNDS);
```

Note: We can do the same way if we work with MySQL, SQLite, or SQLServer and we must convert Geometry format in GIS database to binary format.

### Create agents from the result of a select action

If we are familiar with how to create agents from a shapefile then it becomes very simple to create agents from select result. We can do as below:

* **Step 1**: Define a species with SQLSKILL or AgentDB

```
species toto skills: SQLSKILL {
	//insert your descriptions here	
}	
```

* **Step 2**: Define a connection and selection parameters

```
global {
	map<string,string>  PARAMS <- ['dbtype'::'sqlite','database'::'../includes/bph.sqlite'];
	string location <- 'select ID_4, Name_4, ST_AsBinary(geometry) as geom from vnm_adm4 
                                      where id_2=38253 or id_2=38254;';
	...
}      
```

* **Step 3**: Create species by using selected results

```
init {
   create toto { 
	  create locations from: list(self select (params: PARAMS, 
		                                   select: LOCATIONS)) 
                                                   with:[ id:: "id_4", custom_name:: "name_4", shape::"geom"];
	}
   ...
}
```

### Save Geometry data to database
If we are familiar with how to create agents from a shapefile then it becomes very simple to create agents from select result. We can do as below:

* **Step 1**: Define a species with SQLSKILL or AgentDB

```
species toto skills: SQLSKILL {  
	//insert your descriptions here
} 
```

* **Step 2**: Define a connection and create GIS database and tables

```
global {
	map<string,string> PARAMS <-  ['host'::'localhost', 'dbtype'::'Postgres', 'database'::'', 
                                                            'port'::'5433', 'user'::'postgres', 'passwd'::'tmt'];

	init {
		create toto ;
		ask toto {
			if (self testConnection[ params::PARAMS]){
			    // create GIS database	
 			    do executeUpdate(params:PARAMS, 
		                updateComm: "CREATE DATABASE spatial_db with TEMPLATE = template_postgis;"); 
 			    	remove key: "database" from: PARAMS;
				put "spatial_db" key:"database" in: PARAMS;
				//create table
                            do executeUpdate params: PARAMS 
				  updateComm : "CREATE TABLE buildings "+
				  "( "  +
                   	               " name character varying(255), " + 
                                       " type character varying(255), " + 
                                       " geom GEOMETRY " + 
                                   ")";
			}else {
 				write "Connection to MySQL can not be established ";
 			}	
		}
	}
}
```

* **Step 3**: Insert geometry data to GIS database

```
ask building {
   ask DB_Accessor {
	do insert(params: PARAMS, 
                  into: "buildings",
		  columns: ["name", "type","geom"],
		  values: [myself.name,myself.type,myself.shape];
   }
}
```

[//]: # (endConcept|use_database)
