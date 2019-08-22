package ummisco.gaml.extensions.hecras.skill;

/**
 * @author Pavel Savara (original)
 */
public class Program {
	/*
	public native void CreateDfs0File(String string, boolean calendarAxis);

    public interface HECRASController extends Library {
        @SuppressWarnings("deprecation")
		HECRASController INSTANCE = (HECRASController) Native.loadLibrary(
            (Platform.isWindows() ? "RAS506" : "AS506LinuxPort"), HECRASController.class);
        // it's possible to check the platform on which program runs, for example purposes we assume that there's a linux port of the library (it's not attached to the downloadable project)
//        byte giveVoidPtrGetChar(Pointer param); // char giveVoidPtrGetChar(void* param);
//        int giveVoidPtrGetInt(Pointer param);   //int giveVoidPtrGetInt(void* param);
        void Compute_ShowComputationWindow(String a);               // int giveIntGetInt(int a);
    }
	public static void main(String[] args)  {

		String env = System.getProperty("java.library.path");
//		if (!env.contains("jri")) {
//			final String RPath = GamaPreferences.External.LIB_R.value(scope).getPath(scope).replace("libjri.jnilib", "")
//					.replace("libjri.so", "").replace("jri.dll", "");
		final String RPath = "E:\\git\\gama.experimental\\ummisco.gama.extension.hecras\\lib";
//		if (System.getProperty("os.name").startsWith("Windows")) {
			System.setProperty("java.library.path", RPath + ";" + env);
//		}
		System.loadLibrary("RAS506");
		Field fieldSysPath;
		try {
			fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		fieldSysPath.setAccessible( true ); 
		fieldSysPath.set( null, null );
		}   catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 HECRASController sdll = HECRASController.INSTANCE;


	        int a = 3; 
	        sdll.Compute_ShowComputationWindow("");  // calling function with int parameter&result 

//		try {
//
//			JISystem.getLogger().setLevel(Level.FINEST);
//			JISession session = JISession.createSession("localhost", "hqnghi88@hotmail.com", "ngan2105");
//			session.useSessionSecurity(true);
//			JIProgId pid = JIProgId.valueOf("RAS506.HECRASController");
//			pid.setAutoRegistration(true);
//			JIComServer comStub = null;
//			try {
//				comStub = new JIComServer(pid, "localhost", session);
//			} catch (UnknownHostException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			IJIComObject unknown = comStub.createInstance();
//			IJIDispatch dispatch = (IJIDispatch) JIObjectFactory.narrowObject(unknown.queryInterface(IJIDispatch.IID));
//			// File filePath = new
//			// File("F:\\Archivio\\Clienti\\CFD\\Aggiornamento\\xSalari\\Modelli_HEC_RT\\RAS\\Tevere\\Tevere.prj");
//			File filePath = new File(
//					"C:\\HEC Data\\HEC-RAS\\Example Data\\2D Unsteady Flow Hydraulics\\Muncie\\Muncie.prj");
//			if (!filePath.exists()) {
//				System.out.println(filePath + " file does not exist");
//				return;
//			}
//			JIString JIfilePath = new JIString(filePath.getAbsolutePath());
//			dispatch.callMethod("Project_Open", new JIString[] { JIfilePath });
//			// Integer[] a = new Integer[1];
//			JIVariant nmsg = new JIVariant(0, true);
//			JIString[] strs = new JIString[1];
//			Arrays.fill(strs, new JIString("1"));
//			JIVariant msg = new JIVariant(new JIArray(strs, true), true);
//			dispatch.callMethod("Compute_CurrentPlan", new JIVariant[] { nmsg, msg });
//		} catch (JIException ex) {
//			ex.printStackTrace();
//		}
		// create bridge, with default setup
		// it will lookup jni4net.n.dll next to jni4net.j.jar
//		Bridge.setVerbose(true);
//		Bridge.init();
//		Bridge.LoadAndRegisterAssemblyFrom(new File("D:\\AAa\\DHI.Generic.hecrasZero.DFS.dll"));
//		
//		// here you go!
//		Console.WriteLine("Hello .NET world!\n");
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
	*/
}