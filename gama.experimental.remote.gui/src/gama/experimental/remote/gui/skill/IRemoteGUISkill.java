package gama.experimental.remote.gui.skill;

public interface IRemoteGUISkill {
	public static String SKILL_NAME = "remoteGUI";
	//Agent Data
	public final static String NET_AGENT_NAME = "network_name";
	public final static String NET_AGENT_GROUPS = "network_groups";
	public final static String NET_AGENT_SERVER = "network_connection";
	public final static String EXPOSED_VAR_LIST = "network_connection";
	
	public final static String CONFIGURE_TOPIC = "connect";
	public final static String SERVER_URL = "to";
	public final static String LOGIN = "login";
	public final static String PASSWORD = "password";
	public final static String WITHNAME = "with_name";
	
	public final static String EXPOSE_VAR = "expose";
	public final static String LISTEN_VAR = "listen";
	public final static String VAR_NAME = "variables";
	public final static String STORE_NAME = "store_to";
	public final static String EXPOSED_NAME = "with_name";
	
}
