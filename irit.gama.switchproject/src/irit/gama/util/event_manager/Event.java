/*******************************************************************************************************
 *
 * Event.java, in plugin irit.gama.switchproject, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.util.event_manager;

import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.UUID;

import irit.gama.common.interfaces.IKeywordIrit;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaDate;
import msi.gama.util.GamaMap;
import msi.gama.util.IList;
import msi.gaml.descriptions.ActionDescription;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.IStatement;
import msi.gaml.types.IType;

/**
 * Event used by the event manager
 * 
 * @author Jean-François Erdelyi
 */
public class Event {

	// ############################################
	// Comparator

	/**
	 * Event comparator -> priority queue
	 */
	public static class EventComparator implements Comparator<Event> {
		// Current scope
		IScope scope;

		/**
		 * Random used if equals
		 */
		EventComparator(IScope scope) {
			this.scope = scope;
		}

		/*
		@Override
		public int compare(Event x, Event y) {
			if (x.getDate().isSmallerThan(y.getDate(), true)) {
				return -1;
			}
			if (x.getDate().isGreaterThan(y.getDate(), true)) {
				return 1;
			}
			// In order to randomize if the event A and B are equals
			return ((int) scope.getRandom().between(0, 1) < 1) ? -1 : 1;
		}
		*/
		
		public int compare(Event x, Event y) {
			if (x.milli < y.milli) {
				return -1;
			}
			if (x.milli > y.milli) {
				return 1;
			}
			// In order to randomize if the event A and B are equals
			return ((int) scope.getRandom().between(0, 1) < 1) ? -1 : 1;
		}
	}

	// ############################################
	// Attributes
	
	/**
	 * Message ID
	 */
	UUID id = null;

	/**
	 * The execution date
	 */
	private GamaDate date;
	
	/**
	 * The execution date (milli)
	 */
	private long milli;
	
	/**
	 * Simulation scope
	 */
	private IScope scope;

	/**
	 * Species
	 */
	private String species;

	/**
	 * Executer
	 */
	private IStatement.WithArgs action;

	/**
	 * Arguments
	 */
	private Arguments arguments;

	/**
	 * Referred agent
	 */
	private IAgent referredAgent;

	/**
	 * Caller agent
	 */
	private IAgent agent;
	
	/**
	 * Is alive
	 */
	private boolean isAlive;

	// ############################################
	// Constructor

	/**
	 * Create a new event with action and Arguments as map
	 */
	public Event(IScope scope, IAgent caller, ActionDescription action, final GamaMap<String, Object> args,
			GamaDate date, IAgent referredAgent) {

		this.scope = scope.copy("Later");
		this.species = caller.getSpeciesName();
		this.agent = caller;
		this.referredAgent = referredAgent;
		this.isAlive = true;
		this.id = UUID.randomUUID();

		// Get arguments
		arguments = action.createCompiledArgs().resolveAgainst(scope);

		// Convert arguments and insert it in the current scope
		IList<String> keys = args.getKeys();
		for (String key : keys) {
			IType<?> type = this.scope.getType(key);
			final Object val = type.cast(this.scope, args.get(key), null, true);
			this.scope.addVarWithValue(key, val);
		}

		// Set action by name
		setAction(action.getName());
		setDate(date);
	}

	// ############################################
	// Methods

	/**
	 * Set date
	 */
	private void setDate(GamaDate date) throws GamaRuntimeException {
		if (date != null) {
			this.date = date.copy(scope);
			this.milli = date.getLocalDateTime().atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
		} else {
			this.date = null;
			this.milli = 0;
		}
	}

	/**
	 * Set Action
	 */
	private void setAction(String actionName) throws GamaRuntimeException {
		// Get target species
		final ISpecies species = getSpecies();
		if (species == null) {
			throw GamaRuntimeException.error("Impossible to find a species to execute " + actionName, scope);
		}

		// Get action
		action = species.getAction(actionName);
		if (action == null) {
			throw GamaRuntimeException.error("Impossible to find action " + actionName + " in " + species, scope);
		}
	}

	/**
	 * Get arguments
	 */
	private Arguments getRuntimeArgs() throws GamaRuntimeException {
		if (arguments == null) {
			return null;
		}
		return arguments.resolveAgainst(scope);
	}

	/**
	 * Get Species
	 */
	private ISpecies getSpecies() throws GamaRuntimeException {
		return species != null ? scope.getModel().getSpecies(species) : scope.getAgent().getSpecies();
	}

	/**
	 * Get date
	 */
	public GamaDate getDate() {
		return date;
	}

	/**
	 * Get date
	 */
	public IScope getScope() {
		return scope;
	}
	
	/**
	 * Get date
	 */
	public long getMilli() {
		return milli;
	}

	/**
	 * Get caller
	 */
	public IAgent getAgent() {
		return agent;
	}
	
	/**
	 * Get id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Is greater than the event e
	 */
	public boolean isGreaterThan(Event e) {
		return milli > e.milli;
	}

	/**
	 * True if the time of the next event is reached
	 */
	public boolean isTimeReached() {
		return scope.getClock().getCurrentDate().isGreaterThan(date, false);
	}

	/**
	 * Execute the action
	 */
	public boolean execute() {
		if (!this.isAlive) {
			return false;
		}
		/*DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS");

		LocalDateTime dateTime = LocalDateTime.now();
		System.out.println("EXEC START " + dateTime.format(formatter));*/
		if (date == null) {
			date = scope.getClock().getCurrentDate();
		}
		scope.getAgent().setAttribute(IKeywordIrit.EVENT_DATE, date);
		scope.getAgent().setAttribute(IKeywordIrit.REFER_TO, referredAgent);
		/*Object res = scope.execute(action, getRuntimeArgs()).getValue();
		dateTime = LocalDateTime.now();
		System.out.println("EXEC END " + dateTime.format(formatter));
		
		return res;*/
//		scope.execute(action, getRuntimeArgs()).getValue();
		if(referredAgent == null) {
			scope.execute(action, getRuntimeArgs()).getValue();
		} else {
			scope.execute(action, referredAgent, true, getRuntimeArgs()).getValue();
		}
		
		return true;
	}
	
	/**
	 * Kill event
	 */
	public void kill() {
		this.isAlive = false;
	}

	/**
	 * To string
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(action.getName());
		sb.append(" of ");
		sb.append(getAgent());
		sb.append(" with ");
		// Bug in GAMA core -> Arguments toString() does not return the toString of each
		// facet but the hash of the list
		sb.append(arguments.toString());
		sb.append(" at ");
		sb.append(date);

		return sb.toString();
	}
}
