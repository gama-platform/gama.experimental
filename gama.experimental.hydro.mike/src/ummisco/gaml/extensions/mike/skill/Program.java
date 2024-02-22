package ummisco.gaml.extensions.mike.skill;

import java.io.File;
import java.io.IOException;

import net.sf.jni4net.Bridge;
import system.Console;

/**
 * @author Pavel Savara (original)
 */
public class Program {
	public native void CreateDfs0File(String string, boolean calendarAxis);
	public static void main(String[] args) throws IOException {
		// create bridge, with default setup
		// it will lookup jni4net.n.dll next to jni4net.j.jar 
		Bridge.setVerbose(true);
		Bridge.init();
		Bridge.LoadAndRegisterAssemblyFrom(new File("D:\\AAa\\DHI.Generic.MikeZero.DFS.dll"));
		
		// here you go!
		Console.WriteLine("Hello .NET world!\n");
//		Program p=new Program();
//		p.CreateDfs0File("", true);
		// OK, simple hello is boring, let's play with System.Environment
		// they are Hashtable realy
//		final IDictionary variables = system.Environment.GetEnvironmentVariables();
//		// let's enumerate all keys
//		final IEnumerator keys = variables.getKeys().GetEnumerator();
//		while (keys.MoveNext()) {
//			// there hash table is not generic and returns system.Object
//			// but we know is should be system.String, so we could cast
//			final system.String key = (system.String) keys.getCurrent();
//			Console.Write(key);
//
//			// this is automatic conversion of JVM string to system.String
//			Console.Write(" : ");
//
//			// we use the hashtable
//			Object value = variables.getItem(key);
//
//			// and this is JVM toString() redirected to CLR ToString() method
//			String valueToString = value.toString();
//			Console.WriteLine(valueToString);
//		}
//
//		// Console output is really TextWriter on stream
//		final TextWriter writer = Console.getOut();
//		writer.Flush();
	}
}