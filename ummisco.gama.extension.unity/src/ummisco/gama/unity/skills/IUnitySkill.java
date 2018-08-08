/*********************************************************************************************
 *
 * 'INetworkSkill.java, in plugin ummisco.gama.network, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2018 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/

package ummisco.gama.unity.skills;

public interface IUnitySkill {

	// Topics hierarchy

	public final static String TOPIC_ASK = "ask";
	public final static String TOPIC_SET = "set";
	public final static String TOPIC_GET = "get";

	// publish topics
	
	//subscribe topics
	public static final String TOPIC_MAIN = "Unity";
	public static final String TOPIC_MONO_FREE = "monoFree";
	public static final String TOPIC_POSITION = "position";
	public static final String TOPIC_COLOR = "color";
	public final static String TOPIC_REPLAY = "replay";
	public final static String TOPIC_NOTIFICATION = "notification";
	
	public static final String TOPIC_GAMA = "Gama";

}
