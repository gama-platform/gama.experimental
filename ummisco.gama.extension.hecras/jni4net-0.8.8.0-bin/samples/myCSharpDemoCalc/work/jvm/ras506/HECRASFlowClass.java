// ------------------------------------------------------------------------------
//  <autogenerated>
//      This code was generated by jni4net. See http://jni4net.sourceforge.net/ 
// 
//      Changes to this file may cause incorrect behavior and will be lost if 
//      the code is regenerated.
//  </autogenerated>
// ------------------------------------------------------------------------------

package ras506;

@net.sf.jni4net.attributes.ClrType
public class HECRASFlowClass extends system.MarshalByRefObject implements ras506._HECRASFlow, ras506.HECRASFlow {
    
    //<generated-proxy>
    private static system.Type staticType;
    
    protected HECRASFlowClass(net.sf.jni4net.inj.INJEnv __env, long __handle) {
            super(__env, __handle);
    }
    
    @net.sf.jni4net.attributes.ClrConstructor("()V")
    public HECRASFlowClass() {
            super(((net.sf.jni4net.inj.INJEnv)(null)), 0);
        ras506.HECRASFlowClass.__ctorHECRASFlowClass0(this);
    }
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    private native static void __ctorHECRASFlowClass0(net.sf.jni4net.inj.IClrProxy thiz);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;LSystem/String;LSystem/String;)I")
    public native int UnsteadyBoundaryIndex(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<java.lang.String> StorageArea, net.sf.jni4net.Ref<java.lang.String> Connection);
    
    public static system.Type typeof() {
        return ras506.HECRASFlowClass.staticType;
    }
    
    private static void InitJNI(net.sf.jni4net.inj.INJEnv env, system.Type staticType) {
        ras506.HECRASFlowClass.staticType = staticType;
    }
    //</generated-proxy>
}
