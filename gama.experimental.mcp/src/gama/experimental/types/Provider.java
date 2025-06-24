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

import java.util.List;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderResult;
import dev.langchain4j.service.tool.ToolProviderResult.Builder;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IValue;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMapFactory;
import gama.core.util.GamaPair;
import gama.core.util.IMap;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class Predicate.
 */
@vars ({  })
public class Provider implements IValue {

	
	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(),"parameters", parameters, "toolProvider", toolProvider);
	}

	private ToolProvider toolProvider = null;
	private MCPClient client = null;
	
	private IMap<ToolSpecification,ToolExecutor> parameters = null;
	/**
	 * Instantiates a new predicate.
	 */
	
	
	
	
	public Provider() {
		super();
	}

	public Provider(MCPClient client) {
		super();
		this.client = client;
	}
	
	
	public MCPClient getClient() {
		return client;
	}


	public void setClient(MCPClient client) {
		this.client = client;
		init();
	}


	public Provider(Provider p) {
		parameters = GamaMapFactory.concurrentMap();
		parameters.putAll(p.parameters);
		init();
	}
	

	
	public ToolProvider getToolProvider() {
		return toolProvider;
	}


	public IMap<ToolSpecification, ToolExecutor> getParameters() {
		return parameters;
	}


	public void init() {
		if (client != null) {
			toolProvider = McpToolProvider.builder()
		            .mcpClients(List.of(client.client))
		            .build();

		} else {
			toolProvider= (toolProviderRequest) -> {
				Builder tb = ToolProviderResult.builder();
				parameters.getPairs().stream().forEach(
						(c) -> tb.add((ToolSpecification) ((GamaPair) c).key, (ToolExecutor) ((GamaPair) c).value));
				return tb
						.build();
	
			};
		}
	}
	
	public void addTool(ToolSpecification spec, ToolExecutor ex) {
		if (parameters == null) {
			parameters = GamaMapFactory.create();
		}
		parameters.put(spec, ex);
		init();
	}
	
	
	


	@Override
	public String toString() {
		return "provider(" + toolProvider.toString() +")";
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
	public Provider copy(final IScope scope) throws GamaRuntimeException {
		return new Provider(this);
	}

	

	@Override
	public int hashCode() {
		return toolProvider.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final Provider other = (Provider) obj;
		return toolProvider.equals(other.toolProvider);
	}

	/**
	 * Method getType()
	 *
	 * @see gama.core.common.interfaces.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() { return Types.get(ProviderType.id); }

}
