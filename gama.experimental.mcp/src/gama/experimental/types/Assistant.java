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


import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.service.AiServices;
import gama.annotations.precompiler.GamlAnnotations.vars;
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
@vars ({  })
public class Assistant implements IValue {

	 interface AIAssistant {

         String chat(String message);
     }

	
	 AIAssistant assistant;
	
	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(),"assistant", assistant);
	}

	Memory memory = null;
	ChatModel chatModel = null;
	/**
	 * Instantiates a new predicate.
	 */
	
	public Assistant() {
		super();
	}
	
	
	public Assistant(Assistant p) {
		super(); 
	}
	
	public Assistant(ChatModel model, ToolProvider providerArg, ContentRetriever contentRetriever, Memory memoryArg) {
		super();
		this.chatModel = model;
		AiServices<AIAssistant> assistantBD = AiServices.builder(AIAssistant.class).chatModel(model.getModel());
		if (providerArg != null) {
			assistantBD = assistantBD.toolProvider(providerArg.getToolProvider());
			assistantBD.hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage
					.from(toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()));
		}
		if (contentRetriever != null) {
			assistantBD = assistantBD.contentRetriever(contentRetriever.contentRetriever);
		}
		if (memoryArg != null) {
			this.memory = memoryArg;
			assistantBD = assistantBD.chatMemory(memory.getMemory()); 
		}
		assistant = assistantBD.build();
	}
	
	
	public String askQuestion(String prompt) {
		return assistant.chat(prompt);
	}	
	
	
	


	public AIAssistant getAssistant() {
		return assistant;
	}


	public Memory getMemory() {
		return memory;
	}


	public ChatModel getChatModel() {
		return chatModel;
	}


	@Override
	public String toString() {
		return "assistant(" + assistant.toString() +")";
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
	public Assistant copy(final IScope scope) throws GamaRuntimeException {
		return new Assistant(this);
	}

	

	@Override
	public int hashCode() {
		return assistant.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final Assistant other = (Assistant) obj;
		return assistant.equals(other.assistant);
	}

	/** 
	 * Method getType()
	 *
	 * @see gama.core.common.interfaces.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() { return Types.get(AssistantType.id); }

}
