package hardSyncModeComm;

public enum RequestType 
{
	hasAttribute,
	dead,
	getDirectVarValue,
	setDirectVarValue,
	getAttribute,
	setAttribute,
	copy,
	dispose,
	init,
	_init_,
	initSubPopulations,
	get,
	getOrCreateAttributes,
	getAgent,
	getName, 
	setName,
	getLocation, // getAttribute
	setLocation, // setAttributes
	getGeometry,
	getGeometryScope,
	setGeometry,
	schedule,
	getSpecies,
	getScope,
	getHost,
	getPopulation,
	getModel,
	primDie,
	setPeers,
	getPeers,
	setHost,
	getMacroAgents,
	getPopulationFor,
	compareTo,
	getFromIndicesList,
	getAttributes,
	setAgent,
	
	poison // stop request
	
}
