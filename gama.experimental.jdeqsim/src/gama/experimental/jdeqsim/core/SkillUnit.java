package gama.experimental.jdeqsim.core;

import gama.core.metamodel.agent.IAgent;
import gama.core.runtime.IScope;

/**
 * Unit using as core definition in skills
 * 
 * @author Jean-François Erdelyi
 */
public abstract class SkillUnit implements INamable {
	public static int skillUnitCount = 0;
	private String name = null;

	protected IScope scope = null;
	protected IAgent relativeAgent = null;

	public SkillUnit(IScope scope, IAgent relativeAgent) {
		this.scope = scope;
		this.relativeAgent = relativeAgent;
	}

	public IScope getScope() {
		return scope;
	}

	public void setScope(IScope scope) {
		this.scope = scope;
	}

	public IAgent getRelativeAgent() {
		return relativeAgent;
	}

	public void setRelativeAgent(IAgent relativeAgent) {
		this.relativeAgent = relativeAgent;
	}

	@Override
	public String getName() {
		if (relativeAgent != null) {
			return relativeAgent.getName();
		} else if (name == null) {
			name = getClass().getSimpleName() + skillUnitCount++;
		}
		return name;
	}
}
