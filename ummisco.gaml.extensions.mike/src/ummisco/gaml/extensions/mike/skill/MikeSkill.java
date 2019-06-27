package ummisco.gaml.extensions.mike.skill;

import java.io.File;
import java.io.IOException;

import mike_gama_coupling.Mike_Data;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;
import net.sf.jni4net.Bridge;

@skill(name = "MikeSkill", concept = { IConcept.STATISTIC, IConcept.SKILL })
@doc("read mike data")
public class MikeSkill extends Skill {
	private String env;

	public static void main(String arsg[]) throws IOException {
		Bridge.setVerbose(true);
		Bridge.init();
		Bridge.LoadAndRegisterAssemblyFrom(new File("Mike_Gama.j4n.dll"));

		Mike_Data calc = new Mike_Data();
		final String result = calc.Dfs0File_Read_Data("C:\\git\\HydraulicTools\\RESULT2015.res11", "KIM_SON");

		System.out.printf("Answer to the Ultimate Question is : " + result);
	}

	@action(name = "Dfs0File_Read_Data",args = { @arg (
			name = "file",
			type = IType.STRING,
			optional = false,
			doc = @doc ("dsf0 path")),
			@arg (
					name = "gate",
					type = IType.STRING,
					optional = false,
					doc = @doc ("gate name"))}, doc = @doc(value = "evaluate the R command", returns = "value in Gama data type", examples = {
					@example(" Dfs0File_Read_Data(\"C:\\\\git\\\\HydraulicTools\\\\RESULT2015.res11\", \"KIM_SON\")") }))
	public Object primDfs0Read(final IScope scope) throws GamaRuntimeException {
		Mike_Data calc = new Mike_Data();
//		return calc.Dfs0File_Read_Data("C:\\git\\HydraulicTools\\RESULT2015.res11", "KIM_SON");
		String a=scope.getStringArg("file");
		String b=scope.getStringArg("gate");
				
		return calc.Dfs0File_Read_Data(a,b);

	}

	@action(name = "load_Mike", doc = @doc(value = "evaluate the R command", returns = "value in Gama data type", examples = {
			@example("startR") }))

	public void load_Mike(final IScope scope) {
		initEnv(scope);
		Bridge.setVerbose(true);
		final String RPath = "C:\\git\\BacHungHai_Irrigation\\AAA\\jni4net-0.8.8.0-bin\\samples\\myCSharpDemoCalc\\work";
		try {		

			Bridge.init(new File(RPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//        Bridge.init(new File(System.getProperty("user.dir")));
//        Bridge.LoadAndRegisterAssemblyFrom(new File("C:\\git\\BacHungHai_Irrigation\\AAA\\jni4net-0.8.8.0-bin\\samples\\myCSharpDemoCalc\\work\\DHI.Generic.MikeZero.DFS.dll"));
//
//        Bridge.LoadAndRegisterAssemblyFrom(new File("C:\\git\\BacHungHai_Irrigation\\AAA\\jni4net-0.8.8.0-bin\\samples\\myCSharpDemoCalc\\work\\DHI.DHIfl.dll"));
//
//        Bridge.LoadAndRegisterAssemblyFrom(new File("C:\\git\\BacHungHai_Irrigation\\AAA\\jni4net-0.8.8.0-bin\\samples\\myCSharpDemoCalc\\work\\DHI.Generic.MikeZero.EUM.dll"));
//
//        Bridge.LoadAndRegisterAssemblyFrom(new File("C:\\git\\BacHungHai_Irrigation\\AAA\\jni4net-0.8.8.0-bin\\samples\\myCSharpDemoCalc\\work\\DHI.PFS.dll"));
//
//        Bridge.LoadAndRegisterAssemblyFrom(new File("C:\\git\\BacHungHai_Irrigation\\AAA\\jni4net-0.8.8.0-bin\\samples\\myCSharpDemoCalc\\work\\jni4net.n.w64.v40-0.8.8.0.dll"));
//        Bridge.LoadAndRegisterAssemblyFrom(new File("C:\\git\\BacHungHai_Irrigation\\AAA\\jni4net-0.8.8.0-bin\\samples\\myCSharpDemoCalc\\work\\jni4net.n-0.8.8.0.dll"));
//        Bridge.LoadAndRegisterAssemblyFrom(new File("C:\\git\\BacHungHai_Irrigation\\AAA\\jni4net-0.8.8.0-bin\\samples\\myCSharpDemoCalc\\work\\MyCSharpDemoCalc.dll"));
//        Bridge.LoadAndRegisterAssemblyFrom(new File("C:\\git\\BacHungHai_Irrigation\\AAA\\jni4net-0.8.8.0-bin\\samples\\myCSharpDemoCalc\\work\\MyCSharpDemoCalc.j4n.dll"));
		Bridge.LoadAndRegisterAssemblyFrom(new File(RPath+"\\Mike_Gama.j4n.dll"));

//		return "Mike loaded";
	}

	public void initEnv(final IScope scope) {
		env = System.getProperty("java.library.path");
//		if (!env.contains("jri")) {
//			final String RPath = GamaPreferences.External.LIB_R.value(scope).getPath(scope).replace("libjri.jnilib", "")
//					.replace("libjri.so", "").replace("jri.dll", "");
		final String RPath = "C:\\git\\BacHungHai_Irrigation\\AAA\\jni4net-0.8.8.0-bin\\samples\\myCSharpDemoCalc\\work";
		if (System.getProperty("os.name").startsWith("Windows")) {
			System.setProperty("java.library.path", RPath + ";" + env);
		} else {
			System.setProperty("java.library.path", RPath + ":" + env);
		}
		try {
			final java.lang.reflect.Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);

		} catch (final Exception ex) {
			scope.getGui().getConsole(scope).informConsole(ex.getMessage(), null);
			ex.printStackTrace();
		}
		// System.out.println(System.getProperty("java.library.path"));
//		}
//		System.loadLibrary("jri");

//		if (System.getenv("R_HOME") == null) {
//			throw GamaRuntimeException.error("The R_HOME environment variable is not set. R cannot be run.", scope);
//		}
	}
}
