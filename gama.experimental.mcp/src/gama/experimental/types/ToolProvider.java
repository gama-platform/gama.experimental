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
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.service.tool.ToolExecutor;
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
import gama.gaml.descriptions.ActionDescription;
import gama.gaml.descriptions.ConstantExpressionDescription;
import gama.gaml.species.ISpecies;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.IStatement;
import gama.gaml.types.IType;
import gama.gaml.types.Types;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * The Class Predicate.
 */
@vars({})
public class ToolProvider implements IValue {

	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(), "parameters", parameters, "toolProvider", toolProvider);
	}

	private dev.langchain4j.service.tool.ToolProvider toolProvider = null;
	private MCPClient client = null;

	private IMap<ToolSpecification, ToolExecutor> parameters = null;

	/**
	 * Instantiates a new predicate.
	 */

	private static Map<String, Object> toMap(String arguments) {
		try {
			return new ObjectMapper().readValue(arguments, new TypeReference<>() {
			});
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void addToolExecutor(IScope scope, String name, String description, ActionDescription executor) {
		ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
			String aname = executor.getName();

			if (scope.getModel() != null && scope.getModel().getAction(aname) != null) {

				String message = toolExecutionRequest.arguments();// "";
				Map<String, Object> arguments = toMap(toolExecutionRequest.arguments());
				if (arguments.get("msg") != null) {
					message = arguments.get("msg").toString();
				} else {

					message = arguments.get("description").toString();
				}

				final ISpecies context = scope.getModel();
				final IStatement.WithArgs actionTNR = context.getAction(aname);
				final Arguments argsTNR = new Arguments();
				argsTNR.put("msg", ConstantExpressionDescription.create(message));
				actionTNR.setRuntimeArgs(scope, argsTNR);

				return actionTNR.executeOn(scope).toString();
//				return scope.getModel().getAction(aname).executeOn(scope).toString();
			}
			return toolExecutionRequest.arguments();
		};
		ToolSpecification toolSpecification = ToolSpecification.builder().name(name).description(description)
				.parameters(JsonObjectSchema.builder()
						.addStringProperty("msg", "The message give to the tool as input, it can be any")
//		        .required("msg") // the required properties should be specified explicitly
						.build())
				.build(); 
		addTool(toolSpecification, toolExecutor);

	}

	public void addToolExecutor(IScope scope, String json, ActionDescription executor) {
		ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> {
			String aname = executor.getName();

			if (scope.getModel() != null && scope.getModel().getAction(aname) != null) {

				String message = toolExecutionRequest.arguments();// "";
				Map<String, Object> arguments = toMap(toolExecutionRequest.arguments());
//				if (arguments.get("msg") != null) {
//					message = arguments.get("msg").toString();
//				} else {
//
//					message = arguments.get("description").toString();
//				}

				final ISpecies context = scope.getModel();
				final IStatement.WithArgs actionTNR = context.getAction(aname);
				final Arguments argsTNR = new Arguments();
				arguments.forEach((key, value) -> {
				    // Example: Print the key and value
//				    System.out.println(key + " = " + value);
					argsTNR.put(key, ConstantExpressionDescription.create(value));

				});
				actionTNR.setRuntimeArgs(scope, argsTNR);

				return actionTNR.executeOn(scope).toString();
//				return scope.getModel().getAction(aname).executeOn(scope).toString();
			}
			return toolExecutionRequest.arguments();
		}; 
		try {
			ToolSpecification toolSpec = parseToolSpecification(json);
			System.out.println("ToolSpecification created successfully:");
			System.out.println("Name: " + toolSpec.name());
			System.out.println("Description: " + toolSpec.description());
			System.out.println("Parameters: " + toolSpec.parameters());

			addTool(toolSpec, toolExecutor);
		} catch (JsonProcessingException e) {
			System.err.println("Error parsing JSON string: " + e.getMessage());
		}

	}

	public static ToolSpecification parseToolSpecification(String json) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> toolMap = mapper.readValue(json, Map.class);

		// Extract the top-level fields
		String toolName = (String) toolMap.get("name");
		String toolDescription = (String) toolMap.get("description");

		// Access the nested 'parameters' map
		@SuppressWarnings("unchecked")
		Map<String, Object> parametersMap = (Map<String, Object>) toolMap.get("parameters");
		if (parametersMap == null) {
			throw new IllegalArgumentException("JSON must contain a 'parameters' object.");
		}

		@SuppressWarnings("unchecked")
		Map<String, Map<String, Object>> properties = (Map<String, Map<String, Object>>) parametersMap
				.get("properties");

		@SuppressWarnings("unchecked")
		List<String> requiredProperties = (List<String>) parametersMap.get("required");

		// Build the JsonObjectSchema for the parameters
		JsonObjectSchema.Builder schemaBuilder = JsonObjectSchema.builder();
		if (properties != null) {
			properties.forEach((name, props) -> {
				String type = (String) props.get("type");
				String description = (String) props.get("description");

				if ("string".equals(type)) {
					schemaBuilder.addStringProperty(name, description);
				} else if ("integer".equals(type)) {
					schemaBuilder.addIntegerProperty(name, description);
				}
			});
		}

		if (requiredProperties != null) {
			requiredProperties.forEach(schemaBuilder::required);
		}
		JsonObjectSchema jsonObjectSchema = schemaBuilder.build();

		// Build and return the ToolSpecification
		return ToolSpecification.builder().name(toolName).description(toolDescription).parameters(jsonObjectSchema)
				.build();
	}

	public ToolProvider() {
		super();
	}

	public ToolProvider(MCPClient client) {
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

	public ToolProvider(ToolProvider p) {
		parameters = GamaMapFactory.concurrentMap();
		parameters.putAll(p.parameters);
		init();
	}

	public dev.langchain4j.service.tool.ToolProvider getToolProvider() {
		return toolProvider;
	}

	public IMap<ToolSpecification, ToolExecutor> getParameters() {
		return parameters;
	}

	public void init() {
		if (client != null) {
			toolProvider = McpToolProvider.builder().mcpClients(List.of(client.client)).build();

		} else {
			toolProvider = (toolProviderRequest) -> {
				Builder tb = ToolProviderResult.builder();
				parameters.getPairs().stream().forEach(
						(c) -> tb.add((ToolSpecification) ((GamaPair) c).key, (ToolExecutor) ((GamaPair) c).value));
				return tb.build();

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
		return "provider(" + toolProvider.toString() + ")";
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
	public ToolProvider copy(final IScope scope) throws GamaRuntimeException {
		return new ToolProvider(this);
	}

	@Override
	public int hashCode() {
		return toolProvider.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		final ToolProvider other = (ToolProvider) obj;
		return toolProvider.equals(other.toolProvider);
	}

	/**
	 * Method getType()
	 *
	 * @see gama.core.common.interfaces.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() {
		return Types.get(ToolProviderType.id);
	}

}
