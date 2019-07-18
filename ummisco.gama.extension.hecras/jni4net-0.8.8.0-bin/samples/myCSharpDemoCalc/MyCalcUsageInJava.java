import net.sf.jni4net.Bridge;

import java.io.IOException;

import mike_gama_coupling.hecras_Data;

public class MyCalcUsageInJava {
    public static void main(String arsg[]) throws IOException {
        Bridge.init();
        Bridge.LoadAndRegisterAssemblyFrom(new java.io.File("MyCSharpDemoCalc.j4n.dll"));

        Mike_Data calc = new Mike_Data();
        final String result = calc.Dfs0File_Read_Data("C:\\git\\HydraulicTools\\RESULT2015.res11", "KIM_SON");

        System.out.printf("Answer to the Ultimate Question is : " + result);
    }
}
