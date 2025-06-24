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

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
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
public class Memory implements IValue {

	
	
	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(),"memory", memory);
	}

	ChatMemory memory = null;
	/**
	 * Instantiates a new predicate.
	 */
	
	
	
	public Memory(ChatModel model, Integer maxTokens) {
		super();
		memory= TokenWindowChatMemory.withMaxTokens(((maxTokens != null) && (maxTokens > 0)) ? maxTokens : 1000, new OpenAiTokenCountEstimator(GPT_4_O_MINI));
		model.setMemory(this);
	}
	
	public ChatMemory getMemory() {
		return memory;
	}

	public void addToMemory(String msgToAdd) {
		if (msgToAdd == null) return;
		SystemMessage systemMessage = SystemMessage.from(msgToAdd);
		memory.add(systemMessage); 
	}
	
	public Memory(Memory p) {
		p.memory.messages();
		
		
	}
	



	@Override
	public String toString() { 
		return "memory(" + memory.messages().toString() +")";
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
	public Memory copy(final IScope scope) throws GamaRuntimeException {
		return new Memory(this);
	}

	

	@Override
	public int hashCode() {
		return memory.hashCode();
	} 

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final Memory other = (Memory) obj;
		return memory.equals(other.memory); 
	}

	/**
	 * Method getType()
	 *
	 * @see gama.core.common.interfaces.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() { return Types.get(MemoryType.id); }

}
