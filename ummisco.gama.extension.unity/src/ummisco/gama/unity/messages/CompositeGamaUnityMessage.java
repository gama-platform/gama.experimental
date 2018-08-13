

/*********************************************************************************************
*
* 'CompositeGamaMessage.java, in plugin ummisco.gama.network, is part of the source code of the
* GAMA modeling and simulation platform.
* (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
*
* Visit https://github.com/gama-platform/gama for license information and developers contact.
* 
*
**********************************************************************************************/
package ummisco.gama.unity.messages;

import java.util.Map;

import msi.gama.extensions.messaging.GamaMessage;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import ummisco.gama.network.common.CompositeGamaMessage;
import ummisco.gama.serializer.factory.StreamConverter;

public class CompositeGamaUnityMessage extends GamaUnityMessage {
	protected Object deserializeContent;
	
	/*
	public CompositeGamaUnityMessage(IScope scope, Object sender, Object receivers, Object content)
			throws GamaRuntimeException {
		super(scope, sender, receivers, content);
		this.setUnread(true);
		deserializeContent=null;
	}
	*/
	
	public CompositeGamaUnityMessage(IScope scope, GamaUnityMessage message)
	{
		super(scope,message.getSender(),message.getReceivers(), message.getUnityAction(), message.getUnityObject(), (Map<?, ?>) message.getUnityAttribute(), message.getUnityTopic(), message.getContents(scope));
		this.contents = StreamConverter.convertNetworkObjectToStream(scope, (message.getContents(scope)));
		this.emissionTimeStamp = message.getEmissionTimestamp();
		this.setUnread(true);
		deserializeContent=null;
	}
	
	/*
	private CompositeGamaUnityMessage(IScope scope, Object sender, Object receivers, Object content,Object deserializeContent,int timeStamp) {
		super(scope, sender, receivers, content);
		this.emissionTimeStamp = timeStamp;
		this.setUnread(true);
		this.deserializeContent = deserializeContent;
	}
	*/
	
	@Override
	public Object getContents(IScope scope) {
		this.setUnread(false);
		if(deserializeContent == null)
			deserializeContent = StreamConverter.convertNetworkStreamToObject(scope, (String)contents);//StreamConverter.convertStreamToObject(scope, (String)(super.getContents(scope)));
		return deserializeContent; 
	}
}

