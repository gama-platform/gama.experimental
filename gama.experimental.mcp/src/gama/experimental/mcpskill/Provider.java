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
package gama.experimental.mcpskill;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.common.interfaces.IKeyword;
import gama.core.common.interfaces.IValue;
import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaMap;
import gama.core.util.IMap;
import gama.core.util.file.json.Json;
import gama.core.util.file.json.JsonValue;
import dev.langchain4j.model.chat.ChatModel;
import gama.gaml.types.IType;
import gama.gaml.types.Types;

/**
 * The Class Predicate.
 */
@vars ({ @variable (
		name = "name",
		type = IType.STRING,
		doc = @doc ("the name of the predicate")),
		@variable (
				name = "is_true",
				type = IType.BOOL,
				doc = @doc ("the truth value of the predicate")),
		@variable (
				name = IKeyword.VALUES,
				type = IType.MAP,
				doc = @doc ("the values attached to the predicate")),
		@variable (
				name = "date",
				type = IType.FLOAT,
				doc = @doc ("the date of the predicate")) })
public class Provider implements IValue {

	@Override
	public JsonValue serializeToJson(final Json json) {
		return json.typedObject(getGamlType(), "name", name, "is_true", is_true, "values", values, "date", date);
	}

	/** The name. */
	String name;

	/** The values. */
	IMap<String, Object> values;

	ChatModel chatModel;

	/** The date. */
	Double date;
 
	/** The agent cause. */
	IAgent agentCause;

	/** The is true. */
	boolean is_true = true;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@getter ("name")
	public String getName() { return name; }

	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	@getter ("values")
	public IMap<String, Object> getValues() { return values; }

	/**
	 * Gets the checks if is true.
	 *
	 * @return the checks if is true
	 */
	@getter ("is_true")
	public Boolean getIs_True() { return is_true; }

	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	@getter ("date")
	public Double getDate() { return date; } 
 
	/**
	 * Sets the values.
	 *
	 * @param values
	 *            the values
	 */
	public void setValues(final IMap<String, Object> values) {
		this.values = values;
	}

	/**
	 * Sets the checks if is true.
	 *
	 * @param ist
	 *            the new checks if is true
	 */
	public void setIs_True(final Boolean ist) { this.is_true = ist; }

	/**
	 * Sets the date.
	 *
	 * @param date
	 *            the new date
	 */
	public void setDate(final Double date) { this.date = date; }
  

	/**
	 * Instantiates a new predicate.
	 */
	public Provider() {
		this.name = "";
		this.agentCause = null;
	}

	/**
	 * Instantiates a new predicate.
	 *
	 * @param name
	 *            the name
	 */
	public Provider(final String name) {
		this.name = name;
		this.agentCause = null;
	}

	/**
	 * Instantiates a new predicate.
	 *
	 * @param name
	 *            the name
	 * @param ist
	 *            the ist
	 */
	public Provider(final String name, final ChatModel cm) {
		this.name = name; 
		this.chatModel = cm;
	}
 
	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	public void setName(final String name) {
		this.name = name;

	}

	@Override
	public String toString() {
		return "predicate(" + name + (values == null ? "" : "," + values) + (agentCause == null ? "" : "," + agentCause) + "," + is_true + ")";
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
		return new Provider(name, chatModel == null ? null :  chatModel);
	}

	/**
	 * Copy.
	 *
	 * @return the predicate
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	public Provider copy() throws GamaRuntimeException { 
		if (chatModel != null) {
			return new Provider(name, chatModel);
		}
		return new Provider(name);
	}


	@Override
	public int hashCode() {
		return Objects.hash(name, values);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final Provider other = (Provider) obj;
		if (!Objects.equals(name, other.name) || is_true != other.is_true) return false; 

		if (chatModel != null && other.chatModel != null && !chatModel.equals(other.chatModel)) return false;
		return true;
	}

	/**
	 * Equals intention plan.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	public boolean equalsIntentionPlan(final Object obj) {
		// Only test case where the parameter is not null
		return equals(obj);//TODO doing this for now because they are exactly the same, but should investigate if there's a need for a different equality operator
	}

	/**
	 * Equals but not truth.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	public boolean equalsButNotTruth(final Object obj) {
		// return true if the predicates are equals but one is true and not the
		// other
		// Doesn't check the lifetime value
		// Used in emotions
		if (obj == this) return false; // is_true must be different
		if (!(obj instanceof Provider)) return false;
		
		Provider other = (Provider)obj;
		
		if (!Objects.equals(name, other.name) || is_true == other.is_true) return false;
		if (agentCause == null || other.agentCause == null) return true; //TODO: this is a weird condition for equality
		if (values == null || other.values == null) return true;
		
		final Set<String> keys = values.keySet();
		keys.retainAll(other.values.keySet());
		for (final String k : keys) {
			if (values.get(k) == null && other.values.get(k) != null || !values.get(k).equals(other.values.get(k))) {
				return false;					
			}
		}
		
		//TODO: why don't we compare agentCause ?
		return true;
	}

	/**
	 * Equals emotions.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	public boolean equalsEmotions(final Object obj) {
		// Ne teste pas l'agent cause.
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		final Provider other = (Provider) obj;
		if (!Objects.equals(name, other.name) || is_true != other.is_true) return false;
		if (values != null && other.values != null) {
			final Set<String> keys = values.keySet();
			keys.retainAll(other.values.keySet());
			for (final String k : keys) {
				if (	values.get(k) == null && other.values.get(k) != null
					|| !values.get(k).equals(other.values.get(k)))
					return false;
			}
		}

		return true;
	}

	/**
	 * Method getType()
	 *
	 * @see gama.core.common.interfaces.ITyped#getGamlType()
	 */
	@Override
	public IType<?> getGamlType() { return Types.get(ProviderType.id); }

}
