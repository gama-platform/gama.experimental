package gama.experimental.argumentation.species;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.setter;
import gama.annotations.precompiler.GamlAnnotations.species;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.core.metamodel.agent.GamlAgent;
import gama.core.metamodel.agent.IAgent;
import gama.core.metamodel.population.IPopulation;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaListFactory;
import gama.core.util.GamaMapFactory;
import gama.core.util.GamaPair;
import gama.core.util.IList;
import gama.core.util.IMap;
import gama.core.util.graph.GamaGraph;
import gama.core.util.graph.IGraph;
import gama.experimental.argumentation.skills.ArgumentingSkill;
import gama.experimental.argumentation.types.GamaArgument;
import gama.experimental.argumentation.types.GamaArgumentType;
import gama.gaml.descriptions.ConstantExpressionDescription;
import gama.gaml.operators.Random;
import gama.gaml.statements.Arguments;
import gama.gaml.statements.IStatement.WithArgs;
import gama.gaml.types.IType;
import gama.gaml.types.Types;


@species (
		name = "abstract_adopter",
		skills = { "argumenting" })
@vars ({
	 @variable(name = "intention", type = IType.FLOAT),
		@variable(name = "attitude", type = IType.FLOAT),
		@variable(name = "social_norm", type = IType.FLOAT),
		@variable(name = "pbc", type = IType.FLOAT),
		@variable(name = "weight_attitude", type = IType.FLOAT, init = "1.0"),
		@variable(name = "weight_social_norm", type = IType.FLOAT, init = "1.0"),
		@variable(name = "weight_pbc", type = IType.FLOAT, init = "1.0"),
		@variable(name = "adoption_state", type = IType.STRING, init = "'knowledge'"),
		@variable(name = "persuasion_threshold", type = IType.FLOAT, init = "0.1"),
		@variable(name = "decision_threshold", type = IType.INT, init = "5"),
		@variable(name = "adoption_threshold", type = IType.FLOAT, init = "0.5"),
		@variable(name = "confirmation_time", type = IType.FLOAT, init = "200.0"),
		@variable(name = "addoption_time", type = IType.FLOAT, init = "0.0"),
		@variable(name = "social_network", type = IType.LIST, of = IType.AGENT),
		@variable(name = "proba_communication_channel", type = IType.MAP),
		@variable(name = "influence_factor", type = IType.FLOAT),
		@variable(name = "known_arguments", type = IType.MAP),
		@variable(name = "intention_uncertainty", type = IType.FLOAT),
		@variable(name = "convergence_speed", type = IType.FLOAT),
		@variable(name = "sigmoid_coeff", type = IType.FLOAT, init="1.0"),
		@variable(name = "probability_exchange", type = IType.FLOAT, init="1.0"),
		@variable(name = "argument_lifespan", type = IType.FLOAT, init="100.0"),
		@variable(name = "global_argumentation_graph", type = IType.GRAPH)
				
})
public class MIDAO extends GamlAgent {
	static final String INTENTION = "intention";
	static final String ATTITUDE = "attitude";
	static final String SOCIAL_NORM = "social_norm";
	static final String PBC = "pbc";
	
	static final String WEIGHT_ATTITUDE = "weight_attitude";
	static final String WEIGHT_SOCIAL_NORM = "weight_social_norm";
	static final String WEIGHT_PBC = "weight_pbc";
	
	static final String KNOWLEDGE = "knowledge";
	static final String PERSUASION = "persuasion";
	static final String IMPLEMENTATION = "implementation";
	static final String DECISION = "decision";
	static final String CONFIRMATION = "confirmation";

	static final String PERSUASION_THRESHOLD = "persuasion_threshold";
	static final String DECISION_THRESHOLD = "decision_threshold";
	static final String ADOPTION_THRESHOLD = "adoption_threshold";

	static final String ADOPTION_STATE = "adoption_state";
	
	static final String CONFIRMATION_TIME = "confirmation_time";
	static final String ADOPTION_TIME = "addoption_time";

	
	static final String SOCIAL_NETWORK = "social_network";
	static final String PROBA_COMMUNICATION_CHANNEL = "proba_communication_channel";

	static final String INFLUENCE_FACTOR = "influence_factor";
	
	static final String KNWON_ARGUMENTS = "known_arguments";
	static final String INTENTION_UNCERTAINTY = "intention_uncertainty";
	

	static final String CONVERGENCE_SPEED = "convergence_speed";
	
	static final String SIGMOID_COEFF = "sigmoid_coeff";
	
	static final String PROBABILITY_EXCHANGE = "probability_exchange";
	
	static final String ARGUMENT_LIFESPAN = "argument_lifespan";
	
	static final String GLOBAL_ARGUMENTATION_GRAPH = "global_argumentation_graph";
	

	
	public MIDAO(IPopulation<? extends IAgent> s, int index) {
		super(s, index);
	}


	@getter(INTENTION)
	public Double getIntention(final IAgent agent) {
		return (Double) agent.getAttribute(INTENTION);
	}

	@setter(INTENTION)
	public void setIntention(final IAgent agent, final Double v) {
		agent.setAttribute(INTENTION, v);
	}
	
	@getter(ATTITUDE)
	public Double getAttitude(final IAgent agent) {
		return (Double) agent.getAttribute(ATTITUDE);
	}

	@setter(ATTITUDE)
	public void setAttitude(final IAgent agent, final Double v) {
		agent.setAttribute(ATTITUDE, v);
	}
	
	@getter(SOCIAL_NORM)
	public Double getSocialNorm(final IAgent agent) {
		return (Double) agent.getAttribute(SOCIAL_NORM);
	}

	@setter(SOCIAL_NORM)
	public void setSocialNorm(final IAgent agent, final Double v) {
		agent.setAttribute(SOCIAL_NORM, v);
	}
	
	@getter(PBC)
	public Double getPBC(final IAgent agent) {
		return (Double) agent.getAttribute(PBC);
	}

	@setter(PBC)
	public void setPBC(final IAgent agent, final Double v) {
		agent.setAttribute(PBC, v);
	}
	
	@getter(WEIGHT_ATTITUDE)
	public Double getWeightAttitude(final IAgent agent) {
		return (Double) agent.getAttribute(WEIGHT_ATTITUDE);
	}

	@setter(WEIGHT_ATTITUDE)
	public void setWeightAttitude(final IAgent agent, final Double v) {
		agent.setAttribute(WEIGHT_ATTITUDE, v);
	}
	
	@getter(WEIGHT_SOCIAL_NORM)
	public Double getWeightSocialNorm(final IAgent agent) {
		return (Double) agent.getAttribute(WEIGHT_SOCIAL_NORM);
	}

	@setter(WEIGHT_SOCIAL_NORM)
	public void setWeightSocialNorm(final IAgent agent, final Double v) {
		agent.setAttribute(WEIGHT_SOCIAL_NORM, v);
	}
	
	@getter(WEIGHT_PBC)
	public Double getWeightPBC(final IAgent agent) {
		return (Double) agent.getAttribute(WEIGHT_PBC);
	}

	@setter(WEIGHT_PBC)
	public void setWeightPBC(final IAgent agent, final Double v) {
		agent.setAttribute(WEIGHT_PBC, v);
	}
	
	@getter(PERSUASION_THRESHOLD)
	public Double getPersuasionThreshold(final IAgent agent) {
		return (Double) agent.getAttribute(PERSUASION_THRESHOLD);
	}

	@setter(PERSUASION_THRESHOLD)
	public void setPersuasionThreshold(final IAgent agent, final Double v) {
		agent.setAttribute(PERSUASION_THRESHOLD, v);
	}
	
	@getter(DECISION_THRESHOLD)
	public Integer getDecisionThreshold(final IAgent agent) {
		return (Integer) agent.getAttribute(DECISION_THRESHOLD);
	}

	@setter(DECISION_THRESHOLD)
	public void setDecisionThreshold(final IAgent agent, final Integer v) {
		agent.setAttribute(DECISION_THRESHOLD, v);
	}
	
	@getter(ADOPTION_THRESHOLD)
	public Double getAdoptionThreshold(final IAgent agent) {
		return (Double) agent.getAttribute(ADOPTION_THRESHOLD);
	}

	@setter(ADOPTION_THRESHOLD)
	public void setAdoptionThreshold(final IAgent agent, final Double v) {
		agent.setAttribute(ADOPTION_THRESHOLD, v);
	}
	
	
	@getter(ADOPTION_STATE)
	public String getAdoptionState(final IAgent agent) {
		return (String) agent.getAttribute(ADOPTION_STATE);
	}

	@setter(ADOPTION_STATE)
	public void setAdoptionState(final IAgent agent, final String v) {
		agent.setAttribute(ADOPTION_STATE, v);
	}

	
	@getter(CONFIRMATION_TIME)
	public Double getConfirmationTime(final IAgent agent) {
		return (Double) agent.getAttribute(CONFIRMATION_TIME);
	}

	@setter(CONFIRMATION_TIME)
	public void setConfirmationTime(final IAgent agent, final Double v) {
		agent.setAttribute(CONFIRMATION_TIME, v);
	}
	

	@getter(ADOPTION_TIME)
	public Double getAdoptionTime(final IAgent agent) {
		return (Double) agent.getAttribute(ADOPTION_TIME);
	}

	@setter(ADOPTION_TIME)
	public void setAdoptionTime(final IAgent agent, final Double v) {
		agent.setAttribute(ADOPTION_TIME, v);
	}
	
	@getter(SOCIAL_NETWORK)
	public IList<IAgent> getSocialNetwork(final IAgent agent) {
		return (IList<IAgent>) agent.getAttribute(SOCIAL_NETWORK);
	}

	@setter(SOCIAL_NETWORK)
	public void setSocialNetwork(final IAgent agent, final IList<IAgent> v) {
		agent.setAttribute(SOCIAL_NETWORK, v);
	}
	
	@getter(PROBA_COMMUNICATION_CHANNEL)
	public IMap<IAgent, Double> getProbaCommunicationChannel(final IAgent agent) {
		return (IMap<IAgent, Double>) agent.getAttribute(PROBA_COMMUNICATION_CHANNEL);
	}

	@setter(PROBA_COMMUNICATION_CHANNEL)
	public void setProbaCommunicationChannel(final IAgent agent, final IMap<IAgent, Double> v) {
		agent.setAttribute(PROBA_COMMUNICATION_CHANNEL, v);
	}
	
	@getter(KNWON_ARGUMENTS)
	public IMap<GamaArgument, GamaPair<Double,Double>> getKnownArguments(final IAgent agent) {
		return (IMap<GamaArgument, GamaPair<Double,Double>>) agent.getAttribute(KNWON_ARGUMENTS);
	}

	@setter(KNWON_ARGUMENTS)
	public void setKnownArguments(final IAgent agent, final IMap<GamaArgument, GamaPair<Double,Double>>  v) {
		agent.setAttribute(KNWON_ARGUMENTS, v);
	}
	
	
	@getter(INFLUENCE_FACTOR)
	public Double getInfluenceFactor(final IAgent agent) {
		return (Double) agent.getAttribute(INFLUENCE_FACTOR);
	}

	@setter(INFLUENCE_FACTOR)
	public void setInfluenceFactor(final IAgent agent, final Double v) {
		agent.setAttribute(INFLUENCE_FACTOR, v);
	}
	
	@getter(INTENTION_UNCERTAINTY)
	public Double getIntentionUncertainty(final IAgent agent) {
		return (Double) agent.getAttribute(INTENTION_UNCERTAINTY);
	}

	@setter(INTENTION_UNCERTAINTY)
	public void setIntentionUncertainty(final IAgent agent, final Double v) {
		agent.setAttribute(INTENTION_UNCERTAINTY, v);
	}
	
	@getter(CONVERGENCE_SPEED)
	public Double getConvergenceSpeed(final IAgent agent) {
		return (Double) agent.getAttribute(CONVERGENCE_SPEED);
	}

	@setter(CONVERGENCE_SPEED)
	public void setConvergenceSpeed(final IAgent agent, final Double v) {
		agent.setAttribute(CONVERGENCE_SPEED, v);
	}
	
	@getter(SIGMOID_COEFF)
	public Double getSigmoidCoeff(final IAgent agent) {
		return (Double) agent.getAttribute(SIGMOID_COEFF);
	}

	@setter(SIGMOID_COEFF)
	public void setSigmoidCoeff(final IAgent agent, final Double v) {
		agent.setAttribute(SIGMOID_COEFF, v);
	}
	
	
	@getter(PROBABILITY_EXCHANGE)
	public Double getProbaExchange(final IAgent agent) {
		return (Double) agent.getAttribute(PROBABILITY_EXCHANGE);
	}

	@setter(PROBABILITY_EXCHANGE)
	public void setProbaExchange(final IAgent agent, final Double v) {
		agent.setAttribute(PROBABILITY_EXCHANGE, v);
	}
	
	@getter(ARGUMENT_LIFESPAN)
	public Double getArgumentLifespan(final IAgent agent) {
		return (Double) agent.getAttribute(ARGUMENT_LIFESPAN);
	}

	@setter(ARGUMENT_LIFESPAN)
	public void setArgumentLifespan(final IAgent agent, final Double v) {
		agent.setAttribute(ARGUMENT_LIFESPAN, v);
	}
	
	@getter(GLOBAL_ARGUMENTATION_GRAPH)
	static public  IGraph<GamaArgument, Object> getGlobalArgGraph(final IAgent agent) {
		return ( IGraph<GamaArgument, Object>) agent.getAttribute(GLOBAL_ARGUMENTATION_GRAPH);
	}

	@setter(GLOBAL_ARGUMENTATION_GRAPH)
	static public void setGlobalArgGraph(final IAgent agent, final  IGraph<GamaArgument, Object> s) {
		agent.setAttribute(GLOBAL_ARGUMENTATION_GRAPH, s);
	}
	
	@Override
	public boolean doStep(final IScope scope) {
		if (super.doStep(scope)) {
			IAgent ag = getAgent();
			updateArgumentLifeSpan(scope);
			String state = getAdoptionState(ag);
			if (KNOWLEDGE.equals(state)) {
				if (getIntention(ag) >= getPersuasionThreshold(ag)) {
					setAdoptionState(ag, PERSUASION);
				}
			} else if (PERSUASION.equals(state)) {
				doActionNoArg(scope, "search_information");
				if (getKnownArguments(ag).size() >= getDecisionThreshold(ag)) {
					setAdoptionState(ag,DECISION);
				}
			} else if (DECISION.equals(state)) {
				doActionNoArg(scope, "search_information");
				if (getIntention(ag) >= getAdoptionThreshold(ag)) {
					setAdoptionState(ag,IMPLEMENTATION);
					setAdoptionTime(ag, 0.0);
				}
			} else if (IMPLEMENTATION.equals(state)) {
				double adoptTime = getAdoptionTime(ag);
				adoptTime += scope.getSimulation().getTimeStep(scope);
				setAdoptionTime(ag, adoptTime);
				if (getIntention(ag) >= getAdoptionThreshold(ag) && adoptTime >= getConfirmationTime(ag) ) {
					setAdoptionState(ag,CONFIRMATION);
				}
			} 
			return true;
		}
		return false;
	}
	
	
	public void updateArgumentLifeSpan(IScope scope) {
		IAgent agent = scope.getAgent();
		IMap<GamaArgument, GamaPair<Double,Double>> knownArguments = getKnownArguments(agent);
		double step = scope.getSimulation().getTimeStep(scope);
		List<GamaArgument> args = new ArrayList<>(knownArguments.keySet());
		boolean updateArgs = false;
		for(GamaArgument arg :args) {
			GamaPair<Double, Double> v = knownArguments.get(arg);
			v.value -= step;
			if (v.value <= 0.0) {
				ArgumentingSkill.getArgGraph(agent).removeVertex(arg);
				knownArguments.remove(arg);
				updateArgs = true;
			} 
		}
		if (updateArgs = true) {
			doAction1Arg(scope, "change_in_known_arguments", "agent", agent);
		}
	}
	
	@action(name = "search_information", doc = @doc(value = "search for new information", examples = {
			@example("do search_information;") }))
	public void  primSearchForInformation(final IScope scope) throws GamaRuntimeException {
		IAgent ag = scope.getAgent();
		if (!getSocialNetwork(ag).isEmpty() && Random.opFlip(scope, getProbaExchange(ag))) {
			IAgent other = getSocialNetwork(ag).anyValue(scope);
			doAction1Arg(scope, "interaction_with_other", "other", other);
		}
		IMap<IAgent,Double> probaCC = getProbaCommunicationChannel(ag);
		if (probaCC != null) {
			for (IAgent cc : probaCC.keySet()) {
				if (Random.opFlip(scope, probaCC.get(cc))) {
					IList<GamaArgument> arguments = CommunicationChannel.getArguments(cc);
					if (!arguments.isEmpty()) {
						GamaArgument arg = arguments.anyValue(scope);
						addArguments(scope,ag,arg,getGlobalArgGraph(ag));
					}
				}
			}
		}

		
		
	}
	
	@action(name = "compute_intention",
			args = {@arg(name = "agent", type = IType.FLOAT, optional = true, doc = @doc("the agent of which to compute the attitude")) },
			
			doc = @doc(value = "compute the intention from the attitude, social norm and the PBC", examples = {
			@example("do compute_intention;") }))
	public Double  primComputeIntention(final IScope scope) throws GamaRuntimeException {
		IAgent agent = scope.getAgent();
		Double wA = getWeightAttitude(agent);
		Double wSN = getWeightSocialNorm(agent);
		Double wPBC = getWeightPBC(agent);
		
		Double sumW = wA + wSN + wPBC;
		Double intention = 0.0;
		if (sumW == 0) {
			 intention = (getAttitude(agent) + getSocialNorm(agent) + getPBC(agent))/3.0;
		} else {
			intention = (wA*getAttitude(agent) + wSN * getSocialNorm(agent) + wPBC * getPBC(agent))/sumW;
		}		
		setIntention(agent,intention);
		return intention;
	}
	
	
	@action(name = "change_in_known_arguments", 
			args = {@arg(name = "agent", type = IType.FLOAT, optional = true, doc = @doc("the agent of which to compute the attitude")) },
			doc = @doc(value = "compute the attitude from the arguments", examples = {
			@example("do compute_attitude;") }))
	public void  primKnownArgumentsModification(final IScope scope) throws GamaRuntimeException {
		IAgent agent = scope.hasArg("agent") ? (IAgent) scope.getArg("agent", IType.AGENT) : null;
		if (agent == null) agent = scope.getAgent();
		doAction1Arg(scope, "compute_attitude", "agent", agent);
		doAction1Arg(scope, "compute_uncertainty","agent", agent);
		doAction1Arg(scope, "compute_intention","agent", agent);
		
	}
	
	@action(name = "compute_attitude", 
			 args = {@arg(name = "agent", type = IType.FLOAT, optional = true, doc = @doc("the agent of which to compute the attitude")) },
				
			doc = @doc(value = "compute the attitude from the arguments", examples = {
			@example("do compute_attitude;") }))
	public Double  primComputeAttitude(final IScope scope) throws GamaRuntimeException {
		IAgent agent = scope.hasArg("agent") ? (IAgent) scope.getArg("agent", IType.AGENT) : null;
		if (agent == null) agent = scope.getAgent();
		Double attitude = 0.0;
		IMap<GamaArgument, GamaPair<Double,Double>> knownArguments = getKnownArguments(agent);
		if (!knownArguments.isEmpty()) {
			IMap<GamaArgument, Double> argumentAcceptability = (IMap<GamaArgument, Double>) doAction1Arg(scope, "get_arguments_acceptabilities", "agent", agent);
			int nb_pos = 0;
			int nb_neg = 0;
			for(GamaArgument arg : argumentAcceptability.keySet()) {
				double acc = argumentAcceptability.get(arg);
				knownArguments.get(arg).key = acc;
				
				if (arg.getConclusion().equals("+")) {
					attitude += acc;
					nb_pos++;
				} else if (arg.getConclusion().equals("-")) {
					attitude += (- 1)* acc;
					nb_neg++;
				} 
			}
			attitude = (Double) doAction2Arg(scope, "normalize_attitude","agent", agent, "attitude", attitude);
		}
		
		setAttitude(agent,attitude);
		return attitude;
	}
	
	
	
	
	
	@action(name = "normalize_attitude", 
			 args = {@arg(name = "agent", type = IType.FLOAT, optional = true, doc = @doc("the agent of which to compute the attitude")),
					 @arg(name = "attitude", type = IType.FLOAT, optional = false, doc = @doc("the value of attitude before normalization")) },
				
			doc = @doc(value = "compute the normalize value of the attitude", examples = {
			@example("do normalize_attitude;") }))
	public Double  primNormalizeAttitude(final IScope scope) throws GamaRuntimeException {
		IAgent agent = scope.hasArg("agent") ? (IAgent) scope.getArg("agent", IType.AGENT) : null;
		if (agent == null) agent = scope.getAgent();
		
		Double attitude = scope.getFloatArg("attitude");
		Double alpha = getSigmoidCoeff(agent);
		Double v = Math.exp(-1 * alpha * attitude);
		return (1 -  v)/(1+v);
	}
	
	
	@action(name = "compute_uncertainty", 
			 args = {@arg(name = "agent", type = IType.FLOAT, optional = true, doc = @doc("the agent of which to compute the attitude")) },
			doc = @doc(value = "compute the uncertainty of the agent", examples = {
			@example("do compute_uncertainty;") }))
	public Double  primUpdateUncertainty(final IScope scope) throws GamaRuntimeException {
		IAgent agent = scope.hasArg("agent") ? (IAgent) scope.getArg("agent", IType.AGENT) : null;
		if (agent == null) agent = scope.getAgent();
		Double uncertainty = 1.0;
		Double v = 0.0;
		IMap<String, Double> conf = ArgumentingSkill.getSourceConf(agent);
		for (GamaArgument arg : getKnownArguments(agent).keySet()) {
			v += conf.get(arg.getSourceType());
		}
		uncertainty = 1 - v/getDecisionThreshold(agent);
		setIntentionUncertainty(agent, uncertainty);
		return uncertainty;
	}
	
	

	@action(name = "interaction_with_other", 
			 args = {@arg(name = "other", type = IType.AGENT, optional = false, doc = @doc("the other agent with which interact")) },
				
			doc = @doc(value = "Interact with another agent - exchange arguments and update the social norm",
					
			examples = {
			@example("do interaction_with_other;") }))
	public void  primInteractionWithOther(final IScope scope) throws GamaRuntimeException {
		IAgent agent = scope.getAgent();
		IAgent other = (IAgent) scope.getArg("other", IType.AGENT);
		doAction1Arg(scope, "update_social_norm", "other", other); 
		if (!getKnownArguments(agent).isEmpty())
			doAction1Arg(scope, "exchange_arguments", "other", other); 
	}
	
	
	@action(name = "exchange_arguments", 
			 args = {@arg(name = "other", type = IType.AGENT, optional = false, doc = @doc("the other agent with which interact")),
			@arg(name = "graph", type = IType.GRAPH, optional = false, doc = @doc("the global argumentation graph with all the arguments and attacks"))},
			doc = @doc(value = "exchane arguments with another agent, return true if at least one argument was exchanged",
			examples = {@example("do exchange_arguments(graph);") }))
	public void  primExchangeArgument(final IScope scope) throws GamaRuntimeException {
		IAgent agent = scope.getAgent();
		IAgent other = (IAgent) scope.getArg("other", IType.AGENT);
		final IGraph<GamaArgument, Object> refGraph = scope.hasArg("graph") ? (IGraph) scope.getArg("graph", IType.GRAPH) : null;
		IMap<GamaArgument, Double> args = GamaMapFactory.create();
		IMap<GamaArgument,GamaPair<Double, Double>> kas = getKnownArguments(agent);
		for(GamaArgument a : kas.keySet()) {
			args.put(a, kas.get(a).key);
		}
		GamaArgument argProposed = Random.opRndCoice(scope, args);
		
		doAction5Arg(scope, "argument_react", "agent_proposing", agent, "agent_receiving", other, "arguments_exchanged", GamaListFactory.create(), "argument_proposed", argProposed, "graph", refGraph);
		
	}
	

	@action(name = "update_social_norm", 
			 args = {@arg(name = "other", type = IType.AGENT, optional = false, doc = @doc("the other agent with which interact")) },
				
			doc = @doc(value = "update the social norm according to the other agent",
					
			examples = {
			@example("do update_social_norm;") }))
	public void  primUpdateSocialNorm(final IScope scope) throws GamaRuntimeException {
		IAgent agent = scope.getAgent();
		IAgent other = (IAgent) scope.getArg("other", IType.AGENT);
		double socialNorm = getSocialNorm(agent);
		
		socialNorm += getConvergenceSpeed(agent) * (1 - getIntentionUncertainty(other)) * (getAttitude(other) - socialNorm) ;
		setSocialNorm(agent, socialNorm);
		doAction1Arg(scope, "compute_intention","agent", agent);
	}
	
	
	@action(name = "new_argument", 
			args = {@arg(name = "argument", type = GamaArgumentType.id, optional = false, doc = @doc("the new argument to add")) },
			doc = @doc(value = "add a new argument",
					examples = {
							@example("do new_argument(arg1);") }))
	public void  primNewArgument(final IScope scope) throws GamaRuntimeException {
		IAgent agent = scope.getAgent();
		GamaArgument arg = (GamaArgument) scope.getArg("argument", GamaArgumentType.id);
		addArguments(scope,agent,arg, getGlobalArgGraph(agent) );
	}
	
	
	private void addArguments(IScope scope, IAgent agentRec, GamaArgument arg, IGraph<GamaArgument, Object> refGraph) {
		IMap<GamaArgument, GamaPair<Double,Double>> kArgs = getKnownArguments(agentRec);
		if (kArgs.keySet().contains(arg)) {
			GamaPair<Double, Double> v = kArgs.get(arg);
			v.setValue(getArgumentLifespan(agentRec));
			kArgs.put(arg, v);
		} else {
			
			GamaPair<Double, Double> v = new GamaPair<Double, Double>(0.0, getArgumentLifespan(agentRec), Types.FLOAT, Types.FLOAT);
			kArgs.put(arg, v);
			setKnownArguments(agentRec, kArgs);
			
			doAction3Arg(scope, "add_argument","agent", agentRec, "argument", arg, "graph", refGraph);
			doAction1Arg(scope, "change_in_known_arguments", "agent", agentRec);
		}
	}
	
	@action(name = "argument_react", 
			args = {@arg(name = "agent_proposing", type = IType.AGENT, optional = false, doc = @doc("the agent proposing the argument")), 
					@arg(name = "agent_receiving", type = IType.AGENT, optional = false, doc = @doc("the agent receiving the argument")),
					@arg(name = "arguments_exchanged", type = IType.LIST, optional = false, doc = @doc("the argument already exchanged by the two agents")),
					@arg(name = "argument_proposed", type = GamaArgumentType.id, optional = false, doc = @doc("the argument proposed by the other agent")),
					@arg(name = "graph", type = IType.GRAPH, optional = false, doc = @doc("the global argumentation graph with all the arguments and attacks")) },
			doc = @doc(value = "behavior of an agent receiving a argument from another agent",
					
			examples = {
			@example("do argument_react(ap,ar,arguments, arg, global_graph);") }))
	public void primArgumentReact(final IScope scope) throws GamaRuntimeException {
		IAgent agentRec = (IAgent) scope.getArg("agent_receiving", IType.AGENT);
		IAgent agentPop = (IAgent) scope.getArg("agent_proposing", IType.AGENT);
		GamaArgument arg = (GamaArgument) scope.getArg("argument_proposed", GamaArgumentType.id);
		
		IMap<GamaArgument, GamaPair<Double,Double>> kArgs = getKnownArguments(agentPop);
		GamaPair<Double, Double> v = kArgs.get(arg);
		v.setValue(getArgumentLifespan(agentPop));
		kArgs.put(arg, v);
		
		IList<GamaArgument> arguments = (IList<GamaArgument>) scope.getArg("arguments_exchanged",IType.LIST);
		final IGraph<GamaArgument, Object> refGraph = scope.hasArg("graph") ? (IGraph) scope.getArg("graph", IType.GRAPH) : null;
		
		GamaArgument argRect = (GamaArgument) doAction3Arg(scope, "argument_attacking", "agent_receiving", agentRec, "argument_proposed", arg, "arguments_exchanged", arguments);
		if (argRect != null && (Boolean) doAction3Arg(scope, "argument_consistency", "agent_proposing", agentRec, "agent_receiving", agentPop, "argument_proposed", argRect)) {
			if (!arguments.contains(argRect)) {
				arguments.add(argRect);
				addArguments(scope,agentRec,arg,refGraph);
				doAction5Arg(scope, "argument_react", "agent_proposing", agentRec, "agent_receiving", agentPop, "arguments_exchanged", arguments, "argument_proposed", argRect, "graph", refGraph);
			} 
		} else {
			if ((Boolean) doAction2Arg(scope, "trust_other", "agent_proposing", agentPop, "agent_receiving", agentRec)) {
				addArguments(scope,agentRec,arg,refGraph);
			}
		}
	
	}
	
	
	
	@action(name = "argument_consistency", 
			args = {@arg(name = "agent_proposing", type = IType.AGENT, optional = false, doc = @doc("the agent proposing the argument")), 
					@arg(name = "agent_receiving", type = IType.AGENT, optional = false, doc = @doc("the agent receiving the argument")),
					@arg(name = "argument_proposed", type = GamaArgumentType.id, optional = false, doc = @doc("the argument proposed by the other agent"))},
			doc = @doc(value = "return true if the agent proposing the argument considers its consistent in the discussion with the other agent",
					
			examples = {
			@example("do argument_consistence(ap,ar,arg);") }))
	public Boolean primArgumentConsistency(final IScope scope) throws GamaRuntimeException {
		IAgent agentRec = (IAgent) scope.getArg("agent_receiving", IType.AGENT);
		IAgent agentPop = (IAgent) scope.getArg("agent_proposing", IType.AGENT);
		GamaArgument arg = (GamaArgument) scope.getArg("argument_proposed", GamaArgumentType.id);
		
		Double intentionRec = getIntention(agentRec);
		Double intentionProp = getIntention(agentPop);
		
		if (intentionRec == intentionProp) 
			return true;
		if (intentionProp > intentionRec ) 
			return arg.getConclusion().equals("+");
		return arg.getConclusion().equals("-");
	}
	
	@action(name = "argument_attacking", 
			args = {@arg(name = "agent_receiving", type = IType.AGENT, optional = false, doc = @doc("the agent receiving the argument")),
					@arg(name = "argument_proposed", type = GamaArgumentType.id, optional = false, doc = @doc("the argument proposed by the other agent")),
					@arg(name = "arguments_exchanged", type = IType.LIST, optional = false, doc = @doc("the argument already exchanged by the two agents"))
							
	},		
			doc = @doc(value = "return an argument known by the agent receiving that attacks the argument proposed, nil otherwise",
					
			examples = {
			@example("do argument_attacking(ar,ap,args);") }))
	public GamaArgument  primArgumentAttacking(final IScope scope) throws GamaRuntimeException {
		IAgent agent = (IAgent) scope.getArg("agent_receiving", IType.AGENT);
		GamaArgument arg = (GamaArgument) scope.getArg("argument_proposed", GamaArgumentType.id);
		IMap<GamaArgument, GamaPair<Double,Double>> knownArg = getKnownArguments(agent);
		final IGraph<GamaArgument, Object> graph = (IGraph<GamaArgument, Object>) ArgumentingSkill.getArgGraph(agent);
		IList<GamaArgument> arguments = (IList<GamaArgument>) scope.getArg("arguments_exchanged",IType.LIST);
		GamaArgument argSelected = null;
		Double acc = -1 * Double.MAX_VALUE;
		Set edges = (Set) graph.incomingEdgesOf(arg);
		for (Object e : edges) {
			GamaArgument s = graph.getEdgeSource(e);
			if (!arguments.contains(s)) {
				Double v = knownArg.get(s).key;
				if (v > acc) {
					acc = v;
					argSelected = s;
				}
			}
			
		}
		return argSelected;
	
		
	}
	
	@action(name = "trust_other", 
			args = {@arg(name = "agent_proposing", type = IType.AGENT, optional = false, doc = @doc("the agent proposing the argument")), 
					@arg(name = "agent_receiving", type = IType.AGENT, optional = false, doc = @doc("the agent receiving the argument"))},
					
			doc = @doc(value = "return true if the agent tu the social norm according to the other agent",
					
			examples = {
			@example("do trust_other(ap,ar);") }))
	public Boolean  primTrustOther(final IScope scope) throws GamaRuntimeException {
		IAgent agent = (IAgent) scope.getArg("agent_receiving", IType.AGENT);
		IAgent other = (IAgent) scope.getArg("agent_proposing", IType.AGENT);
		

		Double intention = getIntention(agent);
		Double influence = getInfluenceFactor(agent);
		Double uncertainty = getIntentionUncertainty(agent);
		
		Double intentionOther = getIntention(other);
		Double influenceOther = getInfluenceFactor(other);
		
		return ((intention + uncertainty) >= (intentionOther - influenceOther + influence)) &&
				 ((intention - uncertainty) <= (intentionOther + influenceOther - influence));
	}
	
	
	private Object doActionNoArg(final IScope scope, final String actionName) {
		WithArgs act = getAgent().getSpecies().getAction(actionName);
		return act.executeOn(scope);
	}


	private Object doAction1Arg(final IScope scope, final String actionName, final String argName,
			final Object ArgVal) {
		Arguments args = new Arguments();
		args.put(argName, ConstantExpressionDescription.createNoCache(ArgVal));
		WithArgs act = getAgent().getSpecies().getAction(actionName);
		act.setRuntimeArgs(scope, args);
		return act.executeOn(scope);
	}

	private Object doAction2Arg(final IScope scope, final String actionName, final String argName1,
			final Object ArgVal1, final String argName2, final Object ArgVal2) {
		Arguments args = new Arguments();
		args.put(argName1, ConstantExpressionDescription.createNoCache(ArgVal1));
		args.put(argName2, ConstantExpressionDescription.createNoCache(ArgVal2));
		WithArgs act = getAgent().getSpecies().getAction(actionName);
		act.setRuntimeArgs(scope, args);
		return act.executeOn(scope);
	}
		
	
	private Object doAction3Arg(final IScope scope, final String actionName, final String argName1,
			final Object ArgVal1, final String argName2, final Object ArgVal2, final String argName3,
			final Object ArgVal3) {
		Arguments args = new Arguments();
		
		
		args.put(argName1, ConstantExpressionDescription.createNoCache(ArgVal1));
		args.put(argName2, ConstantExpressionDescription.createNoCache(ArgVal2));
		args.put(argName3, ConstantExpressionDescription.createNoCache(ArgVal3));
		WithArgs act = getAgent().getSpecies().getAction(actionName);
		act.setRuntimeArgs(scope, args);
		return act.executeOn(scope);
	}
	private Object doAction5Arg(final IScope scope, final String actionName, final String argName1,
			final Object ArgVal1, final String argName2, final Object ArgVal2, final String argName3,
			final Object ArgVal3, final String argName4, final Object ArgVal4,final String argName5, final Object ArgVal5) {
		Arguments args = new Arguments();
		args.put(argName1, 
				ConstantExpressionDescription.createNoCache(ArgVal1));
		args.put(argName2, ConstantExpressionDescription.createNoCache(ArgVal2));
		args.put(argName3, ConstantExpressionDescription.createNoCache(ArgVal3));
		args.put(argName4, ConstantExpressionDescription.createNoCache(ArgVal4));
		args.put(argName5, ConstantExpressionDescription.createNoCache(ArgVal5));
		WithArgs act = getAgent().getSpecies().getAction(actionName);
		act.setRuntimeArgs(scope, args);
		return act.executeOn(scope);
	}

}
