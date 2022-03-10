package ummisco.gama.unity.client.app;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttException;

import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import ummisco.gama.unity.client.data.Student;
import ummisco.gama.unity.client.messages.UIActionMessage;
import ummisco.gama.unity.client.wox.serial.WoxSerializer;


public class localApp {

	public static WoxSerializer serializer = new WoxSerializer();
	private static int separator = 5;
	private static int separatorSpace = 50;

	private static MqttConnector connector;
	private static String topic = "serialization";

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		connector = new MqttConnector();
		connector.connectClient();
		connector.subscribeToTopic(topic);

		System.out.println("-- Welcome to the sereializer App -- ");

		Scanner choose = new Scanner(System.in);
		String choice = null;
		int j = 0;

		while (!"end".equals(choice)) {
			printChoiceListMessage();
			choice = choose.nextLine();
			if ("1".equals(choice)) {
				// GAMA to GAMA messaging : Local serialisation and deserialization
				System.out.println("GAMA to GAMA messaging :  Local serialisation and deserialization");

				// GAMA <-> GAMA
				String content = "<object type=\"Student\" dotnettype=\"Student, Assembly-CSharp, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\" id=\"0\"><field name=\"name\" type=\"string\" value=\"Carlos Jaimez\" /><field name=\"registrationNumber\" type=\"int\" value=\"76453\" /><field name=\"courses\"><object type=\"array\" elementType=\"Course\" length=\"3\" id=\"1\"><object type=\"Course\" dotnettype=\"Course, Assembly-CSharp, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\" id=\"2\"><field name=\"code\" type=\"int\" value=\"6756\" /><field name=\"name\" type=\"string\" value=\"XML and Related Technologies\" /><field name=\"term\" type=\"int\" value=\"2\" /></object><object type=\"Course\" dotnettype=\"Course, Assembly-CSharp, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\" id=\"3\"><field name=\"code\" type=\"int\" value=\"9865\" /><field name=\"name\" type=\"string\" value=\"Object Oriented Programming\" /><field name=\"term\" type=\"int\" value=\"2\" /></object><object type=\"Course\" dotnettype=\"Course, Assembly-CSharp, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\" id=\"4\"><field name=\"code\" type=\"int\" value=\"1134\" /><field name=\"name\" type=\"string\" value=\"E-Commerce Programming\" /><field name=\"term\" type=\"int\" value=\"3\" /></object></object></field><field name=\"mapCourse\"><object type=\"map\" id=\"5\"><object type=\"entry\"><object type=\"int\" value=\"4598\" id=\"6\" /><object type=\"Course\" dotnettype=\"Course, Assembly-CSharp, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\" id=\"7\"><field name=\"code\" type=\"int\" value=\"4598\" /><field name=\"name\" type=\"string\" value=\"Enterprise Component Architecture\" /><field name=\"term\" type=\"int\" value=\"3\" /></object></object><object type=\"entry\"><object type=\"int\" value=\"9865\" id=\"8\" /><object type=\"Course\" dotnettype=\"Course, Assembly-CSharp, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\" id=\"9\"><field name=\"code\" type=\"int\" value=\"9865\" /><field name=\"name\" type=\"string\" value=\"Object Oriented Programming\" /><field name=\"term\" type=\"int\" value=\"2\" /></object></object><object type=\"entry\"><object type=\"int\" value=\"6756\" id=\"10\" /><object type=\"Course\" dotnettype=\"Course, Assembly-CSharp, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\" id=\"11\"><field name=\"code\" type=\"int\" value=\"6756\" /><field name=\"name\" type=\"string\" value=\"XML and Related Technologies\" /><field name=\"term\" type=\"int\" value=\"3\" /></object></object><object type=\"entry\"><object type=\"int\" value=\"1134\" id=\"12\" /><object type=\"Course\" dotnettype=\"Course, Assembly-CSharp, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\" id=\"13\"><field name=\"code\" type=\"int\" value=\"1134\" /><field name=\"name\" type=\"string\" value=\"E-Commerce Programming\" /><field name=\"term\" type=\"int\" value=\"2\" /></object></object></object></field></object>";
				HashMap<String, String> mappingDic = new HashMap<String, String>();
				mappingDic.put("\"Student\"", "\"ummisco.gama.unity.client.data.Student\"");
				mappingDic.put("\"Course\"", "\"ummisco.gama.unity.client.data.Course\"");

				Student st = (Student) loadFromString(content, mappingDic);
				System.out.println(st.printStudent());

				printSeparatorSpace(separator);
				printGoBackToMenuMessage(separator);
				choice = choose.nextLine();
				printSeparatorSpace(separatorSpace);
				choice = null;
			}
			if ("2".equals(choice)) {
				// GAMA to UNITY messaging : Java Object serialization then publish via MQTT
				// brocker
				System.out.println("GAMA to UNITY messaging : Java Object serialization then publish via MQTT brocker");

				// GAMA -> Unity

				String topic = "serialization_java";
				Student st = Student.getNewStudent();

				sendToUnity(topic, st);

				printGoBackToMenuMessage(separator);
				choice = choose.nextLine();
				printSeparatorSpace(separatorSpace);
				choice = null;
			}
			if ("3".equals(choice)) {
				// UNITY to GAMA messaging : Wait for mqtt message then C# Object
				// deserialization
				System.out.println("UNITY to GAMA messaging : Wait for mqtt message then C# Object deserialization");
				// Unity -> GAMA

				HashMap<String, String> mappingDic = new HashMap<String, String>();
				mappingDic.put("\"Student\"", "\"ummisco.gama.unity.client.data.Student\"");
				mappingDic.put("\"Course\"", "\"ummisco.gama.unity.client.data.Course\"");

				checkFromMqtt(10, mappingDic);

				printGoBackToMenuMessage(separator);
				choice = choose.nextLine();
				printSeparatorSpace(separatorSpace);
				choice = null;
			}
			
			if ("4".equals(choice)) {
				
				System.out.println("Local Test");
				
				HashMap<String, String> mappingDic = new HashMap<String, String>();
				mappingDic.put("\"MaterialUI.UIActionMessage\"", "\"ummisco.gama.unity.client.messages.UIActionMessage\"");

				//String code = "<object type=\"MaterialUI.UIActionMessage\" dotnettype=\"MaterialUI.UIActionMessage, Assembly-CSharp, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\" id=\"0\"><field name=\"topic\" type=\"string\" value=\"UITopic\" /><field name=\"messageTime\" type=\"long\" value=\"1646750887\" /><field name=\"elementId\" type=\"string\" value=\"button1\" /><field name=\"actionCode\"><object type=\"int\" value=\"11\" id=\"1\" /></field><field name=\"content\" type=\"string\" value=\" \" /></object>";
				String code = "<object type=\"MaterialUI.UIActionMessage\" dotnettype=\"MaterialUI.UIActionMessage, Assembly-CSharp, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null\" id=\"0\"><field name=\"topic\" type=\"string\" value=\"UITopic\" /><field name=\"messageTime\" type=\"long\" value=\"1646828347\" /><field name=\"messageNumber\" type=\"int\" value=\"123\" /><field name=\"elementId\" type=\"string\" value=\"button1\" /><field name=\"actionCode\"><object type=\"int\" value=\"11\" id=\"1\" /></field><field name=\"content\" type=\"string\" value=\"  Content Texte \" /></object>";
				localTest(mappingDic, code);

				printGoBackToMenuMessage(separator);
				choice = choose.nextLine();
				printSeparatorSpace(separatorSpace);
				choice = null;
			}
		}

		connector.clearAllMessages();
		connector.client.disconnect();
		connector.client.close();
		choose.close();
	}

	public static Object loadFromString(String content, HashMap<String, String> mappingDic) {

		System.out.println(" -- content : " + content);
		Object st = serializer.deserializeFromString(content, mappingDic);

		return st;
	}

	public static void checkFromMqtt(int cmp, HashMap<String, String> mappingDic)
			throws MqttException, InterruptedException, NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		int i = 0;

		while (i < cmp) {
			TimeUnit.SECONDS.sleep(1);
			if (!connector.hasNextMessage()) {
				System.out.println("No. There is no message!");
			} else {
				System.out.println("Yes, there is a message in the mailbox");
				String msg = connector.getNextMessage();
				Object st = serializer.deserializeFromString(msg, mappingDic);
				System.out.println(((Student) st).printStudent());
			}
			i++;
		}
		connector.clearAllMessages();

	}
	
	
	public static void localTest(HashMap<String, String> mappingDic, String msg)
			throws MqttException, InterruptedException, NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

			System.out.println("Local Test");
			Object obj = WoxSerializer.deserializeFromString(msg, mappingDic);
			UIActionMessage UIMsg = (UIActionMessage) obj;
			System.out.println(" topic --> " + UIMsg.topic);
			System.out.println(" messageTime --> " + UIMsg.messageTime);
			System.out.println(" messageNumber --> " + UIMsg.messageNumber);
			System.out.println(" elementId --> " + UIMsg.elementId);
			System.out.println(" actionCode --> " + UIMsg.actionCode);
			System.out.println(" content --> " + UIMsg.content);
			/*
			GamaMap<String, String> MapMsg = (GamaMap<String, String>) GamaMapFactory.create() ;
			
			MapMsg = UIMsg.ToHashMap();
			System.out.println(" elementId --> " + MapMsg.get("elementId"));
			*/
	}

	public static void sendToUnity(String topic, Object obj) throws Exception {
		String serializedContent = serializer.getSerializedToString(obj);
		connector.publish(topic, serializedContent);
	}

	public static void serialiseObject(Object obj, String filename) {
		serializer.save(obj, filename);
	}

	public static void printGoBackToMenuMessage(int sp) throws InterruptedException, IOException {
		for (int i = 0; i < sp; i++)
			System.out.println();
		System.out.println("Press enter to getback to the main menu");
	}

	public static void printChoiceListMessage() {
		System.out.println("\nType : \n 1 for GAMA to GAMA messaging :  Local serialisation and deserialization,"
				+ " \n 2 for GAMA to UNITY messaging : Java Object serialization then publish via MQTT brocker, "
				+ "\n 3 for UNITY to GAMA messaging : Wait for mqtt message then C# Object deserialization,"
				+ "\n 4 for local Testing,"
				+ "\n To end the program type \"end\".");
	}

	public static void printSeparatorSpace(int sp) throws InterruptedException, IOException {
		for (int i = 0; i < sp; i++)
			System.out.println();
	}

}
