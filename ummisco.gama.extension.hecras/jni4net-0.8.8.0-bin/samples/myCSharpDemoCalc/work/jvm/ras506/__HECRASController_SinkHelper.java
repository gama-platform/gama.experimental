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
public class __HECRASController_SinkHelper extends system.Object implements ras506.__HECRASController {
    
    //<generated-proxy>
    private static system.Type staticType;
    
    protected __HECRASController_SinkHelper(net.sf.jni4net.inj.INJEnv __env, long __handle) {
            super(__env, __handle);
    }
    
    protected __HECRASController_SinkHelper() {
            super(((net.sf.jni4net.inj.INJEnv)(null)), 0);
    }
    
    @net.sf.jni4net.attributes.ClrMethod("(F)V")
    public native void ComputeProgressEvent(float Progress);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)V")
    public native void ComputeMessageEvent(java.lang.String eventMessage);
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void ComputeComplete();
    
    public static system.Type typeof() {
        return ras506.__HECRASController_SinkHelper.staticType;
    }
    
    private static void InitJNI(net.sf.jni4net.inj.INJEnv env, system.Type staticType) {
        ras506.__HECRASController_SinkHelper.staticType = staticType;
    }
    //</generated-proxy>
}
