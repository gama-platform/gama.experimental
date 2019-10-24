import net.sf.jni4net.Bridge;

import java.io.IOException;

import HecRas_Gama_Coupling.HecRas_Data;

public class MyCalcUsageInJava {
    public static void main(String arsg[]) throws IOException {
        Bridge.init();
        Bridge.LoadAndRegisterAssemblyFrom(new java.io.File("HecRas_Data.j4n.dll"));

        HecRas_Data calc = new HecRas_Data();
        final String result = calc.init_hecras();

        System.out.printf("Answer to the Ultimate Question is : " + result);
    }
}
