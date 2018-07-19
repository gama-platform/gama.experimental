/*********************************************************************************************
 *
 * 'NetworkSkill.java, in plugin ummisco.gama.network, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/

package ummisco.gama.unity.skills;

import java.util.List;
import java.util.Map;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.extensions.messaging.GamaMessage;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.IList;
import msi.gaml.types.IType;
import ummisco.gama.network.common.IConnector;
import ummisco.gama.network.skills.INetworkSkill;
import ummisco.gama.network.skills.NetworkSkill;
import ummisco.gama.unity.messages.GamaUnityMessage;



/**
 * @author 

@symbol(name = "unity", kind = ISymbolKind.SINGLE_STATEMENT, with_sequence = false, concept = {
		IConcept.SYSTEM })
@inside(kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@doc(value = "The statement allow to move the ball.", usages = {
		@usage(value = "Move a Ball in unity scene", examples = { @example("StartUnity;") }) })
 */
@skill(name = "Unity", concept = { IConcept.NETWORK, IConcept.COMMUNICATION, IConcept.SKILL })
public class UnitySkillOld extends NetworkSkill{
	
	public static final String SKILL_NAME = "unity";
	
	
	
	@action(name = "getGameObjectList", args = {
			@arg(name = "objectType", type = IType.STRING, doc = @doc("The game object type"))}, doc = @doc(value = "", returns = "", examples = {@example("") })
			)
	public String getGameObjectList(final IScope scope) {
		final IAgent agent = scope.getAgent();
		final String commandToExecute = (String) scope.getArg("command", IType.STRING);

		return " This is the liste";
	}
		
    @operator(value = "build_Msg_Unity", doc = {
			@doc("build the message content in order to send it to unity") }, category = {
					IOperatorCategory.STRING }) 
    public static String buildMessagetoUnity(final IScope scope, String action, String unityObject, String unityAttribute, String valueType, String value, String messageText) {
 
    	final String newMessage = "\n<action>"+action+"</action>\n"
    			+ "<object>"+unityObject+"</object>\n"
    			+ "<attribute>"+unityAttribute+"</attribute>\n"
    			+ "<valueType>"+valueType+"</valueType>\n"
    			+ "<value>"+value+"</value>\n"
    			+ "<messageText>"+messageText+"</contentText>\n";
   // 	System.out.println("The message to send is --> "+newMessage);
    	
    	return newMessage;
    }
    
    
    
    //-------------------------
    
	@action(name = "sendUnity", args = {
			@arg(name = IKeyword.TO, type = IType.NONE, optional = true, doc = @doc("The agent, or server, to which this message will be sent to")),
			@arg(name = GamaUnityMessage.CONTENTS, type = IType.NONE, optional = false, doc = @doc("The contents of the message, an arbitrary object")), 
			@arg(name = GamaUnityMessage.ACTION, type = IType.NONE, optional = false, doc = @doc("The action of the message")) })
	public GamaUnityMessage primSendMessage2(final IScope scope) throws GamaRuntimeException {
		final IAgent sender = scope.getAgent();
		Object receiver = scope.getArg("to", IType.NONE);
		Object unityAction = scope.getArg(GamaUnityMessage.ACTION, IType.NONE);
		if (unityAction == null) {
			return null;
		}
		if (receiver == null)
			receiver = sender;
		final Object contents = effectiveContents(scope, scope.getArg(GamaUnityMessage.CONTENTS, IType.NONE));
		if (contents == null) {
			return null;
		}
		final GamaUnityMessage message = createNewUnityMessage(scope, sender, receiver, unityAction, contents);
		//final GamaMessage message = createNewMessage(scope, sender, receiver, contents);
		effectiveSend(scope, message, receiver); 
		System.out.println("From primSendMessage2  to rename " + message.toString());
		return message;
	}
	
	
	
	
	
	@action(name = "send", args = {
			@arg(name = IKeyword.TO, type = IType.NONE, optional = true, doc = @doc("The agent, or server, to which this message will be sent to")),
			@arg(name = GamaMessage.CONTENTS, type = IType.NONE, optional = false, doc = @doc("The contents of the message, an arbitrary object")) 
			})
	public GamaMessage primSendMessage(final IScope scope) throws GamaRuntimeException {
		final IAgent sender = scope.getAgent();
		Object receiver = scope.getArg("to", IType.NONE);
		//Object unityAction = scope.getArg(GamaUnityMessage.ACTION, IType.NONE);
		//if (unityAction == null) {
		//	return null;
		//}
		if (receiver == null)
			receiver = sender;
		final Object contents = effectiveContents(scope, scope.getArg(GamaUnityMessage.CONTENTS, IType.NONE));
		if (contents == null) {
			return null;
		}
		final GamaMessage message = createNewMessage(scope, sender, receiver, contents);
		effectiveSend(scope, message, receiver);
		return message;
	}
	
	
	// -----------------------------------
	
	
	protected GamaUnityMessage createNewUnityMessage(final IScope scope, final Object sender, final Object receivers, final Object unityAction,
			final Object contents) {
		return new GamaUnityMessage(scope, sender, receivers, unityAction, contents);
	}
	
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	//@Override
	protected void effectiveSend(final IScope scope, final GamaUnityMessage message, final Object receiver) {
		if (receiver instanceof IList) {
			for (final Object o : ((IList) receiver).iterable(scope)) {
				effectiveSend(scope, message.copy(scope), o);
			}
		}
		String destName = receiver.toString();
		if (receiver instanceof IAgent && getRegisteredAgents(scope).contains(receiver)) {
			final IAgent mReceiver = (IAgent) receiver;
			destName = (String) mReceiver.getAttribute(INetworkSkill.NET_AGENT_SERVER);
		}

		final IAgent agent = scope.getAgent();
		final List<String> serverNames = (List<String>) agent.getAttribute(INetworkSkill.NET_AGENT_SERVER);
		final Map<String, IConnector> connections = getRegisteredServers(scope);
		for (final String servName : serverNames) {
			System.out.println("______________>> " + message.getAction());
		
			connections.get(servName).send(agent, destName, message);
		}
		
		System.out.println("Message sent from UnitySkill "+message.stringValue(scope));
	}
	
	//----------------------------------
	
	
	
	
	

	

}
