// ------------------------------------------------------------------------------
//  <autogenerated>
//      This code was generated by jni4net. See http://jni4net.sourceforge.net/ 
// 
//      Changes to this file may cause incorrect behavior and will be lost if 
//      the code is regenerated.
//  </autogenerated>
// ------------------------------------------------------------------------------

package ras506;

@net.sf.jni4net.attributes.ClrTypeInfo
public final class HECRASFlow_ {
    
    //<generated-static>
    private static system.Type staticType;
    
    public static system.Type typeof() {
        return ras506.HECRASFlow_.staticType;
    }
    
    private static void InitJNI(net.sf.jni4net.inj.INJEnv env, system.Type staticType) {
        ras506.HECRASFlow_.staticType = staticType;
    }
    //</generated-static>
}

//<generated-proxy>
@net.sf.jni4net.attributes.ClrProxy
class __HECRASFlow extends system.Object implements ras506.HECRASFlow {
    
    protected __HECRASFlow(net.sf.jni4net.inj.INJEnv __env, long __handle) {
            super(__env, __handle);
    }
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;LSystem/String;LSystem/String;)I")
    public native int UnsteadyBoundaryIndex(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<java.lang.String> StorageArea, net.sf.jni4net.Ref<java.lang.String> Connection);
}
//</generated-proxy>
