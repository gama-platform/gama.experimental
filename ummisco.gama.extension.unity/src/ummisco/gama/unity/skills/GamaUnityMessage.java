	/*********************************************************************************************
	 *
	 * 'GamaMessage.java, in plugin msi.gama.core, is part of the source code of the
	 * GAMA modeling and simulation platform.
	 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
	 *
	 * Visit https://github.com/gama-platform/gama for license information and developers contact.
	 * 
	 *
	 **********************************************************************************************/
	package ummisco.gama.unity.skills;

	import msi.gama.common.interfaces.IKeyword;
	import msi.gama.common.interfaces.IValue;
	import msi.gama.common.util.StringUtils;
import msi.gama.extensions.messaging.GamaMessage;
import msi.gama.precompiler.GamlAnnotations.doc;
	import msi.gama.precompiler.GamlAnnotations.getter;
	import msi.gama.precompiler.GamlAnnotations.setter;
	import msi.gama.precompiler.GamlAnnotations.var;
	import msi.gama.precompiler.GamlAnnotations.vars;
	import msi.gama.runtime.IScope;
	import msi.gama.runtime.exceptions.GamaRuntimeException;
	import msi.gaml.types.IType;
	import msi.gaml.types.Types;

	/**
	 * The Class GamaMessageProxy.
	 *
	 * @author drogoul
	 */

	@vars({ @var(name = GamaUnityMessage.SENDER, type = IType.NONE, doc = {
			@doc("Returns the sender that has sent this message") }),
			@var(name = GamaUnityMessage.CONTENTS, type = IType.NONE, doc = {
					@doc("Returns the contents of this message, as a list of arbitrary objects") }),
			@var(name = GamaUnityMessage.UNREAD, type = IType.BOOL, init = IKeyword.TRUE, doc = {
					@doc("Returns whether this message is unread or not") }),
			@var(name = GamaUnityMessage.ACTION, type = IType.NONE, doc = {
					@doc("Returns the action of this message") }),
			@var(name = GamaUnityMessage.RECEPTION_TIMESTAMP, type = IType.INT, doc = {
					@doc("Returns the reception time stamp of this message (I.e. at what cycle it has been received)") }),
			@var(name = GamaUnityMessage.EMISSION_TIMESTAMP, type = IType.INT, doc = {
					@doc("Returns the emission time stamp of this message (I.e. at what cycle it has been emitted)") }) })
	public class GamaUnityMessage extends GamaMessage {

		public final static String CONTENTS = "contents";
		public final static String UNREAD = "unread";
		public final static String EMISSION_TIMESTAMP = "emission_timestamp";
		public final static String RECEPTION_TIMESTAMP = "recention_timestamp";
		public final static String SENDER = "sender";
		public final static String RECEIVERS = "receivers";
		public final static String ACTION = "unityAction";

		
		protected Object unityAction;



		public GamaUnityMessage(final IScope scope, final Object sender, final Object receivers, final Object unityAction, final Object content)
				throws GamaRuntimeException {
			super(scope, sender, receivers, content);
			setAction(unityAction);
		}

		
		/*
		 * (non-Javadoc)
		 *
		 * @see msi.gama.extensions.fipa.IGamaMessage#getSender()
		 */
		@getter(GamaUnityMessage.ACTION)
		public Object getAction() {
			return unityAction;
		}

		/**
		 * Sets the receivers.
		 *
		 * @param sender
		 *            the receivers
		 */
		@setter(GamaUnityMessage.ACTION)
		public void setAction(final Object unityAction) {
			this.unityAction = unityAction;
		}

	

		@Override
		public String serialize(final boolean includingBuiltIn) {
			return StringUtils.toGaml(contents, includingBuiltIn);
		}

		@Override
		public String stringValue(final IScope scope) throws GamaRuntimeException {
			return "message[sender: " + getSender() + "; Action: "+getAction() + "; content: " + getContents(scope) + "; content" + "]";
		}

		@Override
		public GamaUnityMessage copy(final IScope scope) throws GamaRuntimeException {
			return new GamaUnityMessage(scope, getSender(), getReceivers(), getAction(), getContents(scope));
		}

		/**
		 * Method getType()
		 * 
		 * @see msi.gama.common.interfaces.ITyped#getType()
		 */
		@Override
		public IType<?> getType() {
			return Types.get(IType.MESSAGE);
		}

		public void hasBeenReceived(final IScope scope) {
			// receptionTimeStamp = scope.getClock().getCycle();

		}

	}

