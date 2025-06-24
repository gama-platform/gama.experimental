/*******************************************************************************************************
 *
 * Predicate.java, in gama.extension.bdi, is part of the source code of the GAMA modeling and simulation
 * platform .
 *
 * (c) 2007-2024 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.experimental.types;

import java.time.Duration;

import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class Predicate.
 */
public class MCPTransport implements IValue {

	
	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(),"transport", transport);
	}

	McpTransport transport = null;
	/**
	 * Instantiates a new predicate.
	 */
	
	
	
	public MCPTransport(String urlToAdd, Integer timeout) {
		super();
		transport = new HttpMcpTransport.Builder().sseUrl(urlToAdd).timeout(Duration.ofSeconds(timeout))
				.logRequests(true).logResponses(true).build();

	}
	
		
	public MCPTransport(MCPTransport p) {
		
		
		
	}
	



	public McpTransport getTransport() {
		return transport;
	}


	@Override
	public String toString() { 
		return "transport(" + transport.toString() +")";
	}

	@Override
	public String serializeToGaml(final boolean includingBuiltIn) {
		return toString();
	}

	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return toString();
	}

	@Override
	public MCPTransport copy(final IScope scope) throws GamaRuntimeException {
		return new MCPTransport(this);
	}

	

	@Override
	public int hashCode() {
		return transport.hashCode();
	} 

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final MCPTransport other = (MCPTransport) obj;
		return transport.equals(other.transport); 
	}

	/**
	 * Method getType()
	 *
	 * @see gama.core.common.interfaces.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() { return Types.get(MCPTransportType.id); }

}
