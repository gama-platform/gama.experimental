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
public final class HECRASGeometry_ {
    
    //<generated-static>
    private static system.Type staticType;
    
    public static system.Type typeof() {
        return ras506.HECRASGeometry_.staticType;
    }
    
    private static void InitJNI(net.sf.jni4net.inj.INJEnv env, system.Type staticType) {
        ras506.HECRASGeometry_.staticType = staticType;
    }
    //</generated-static>
}

//<generated-proxy>
@net.sf.jni4net.attributes.ClrProxy
class __HECRASGeometry extends system.Object implements ras506.HECRASGeometry {
    
    protected __HECRASGeometry(net.sf.jni4net.inj.INJEnv __env, long __handle) {
            super(__env, __handle);
    }
    
    @net.sf.jni4net.attributes.ClrMethod("()I")
    public native int nRiver();
    
    @net.sf.jni4net.attributes.ClrMethod("(I)LSystem/String;")
    public native java.lang.String RiverName(net.sf.jni4net.Ref<java.lang.Integer> riv);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)I")
    public native int RiverIndex(net.sf.jni4net.Ref<java.lang.String> RiverName);
    
    @net.sf.jni4net.attributes.ClrMethod("(I)I")
    public native int nReach(net.sf.jni4net.Ref<java.lang.Integer> riv);
    
    @net.sf.jni4net.attributes.ClrMethod("(II)LSystem/String;")
    public native java.lang.String ReachName(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch);
    
    @net.sf.jni4net.attributes.ClrMethod("(ILSystem/String;)I")
    public native int ReachIndex(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.String> ReachName);
    
    @net.sf.jni4net.attributes.ClrMethod("(II)I")
    public native int ReachInvert_nPoints(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch);
    
    @net.sf.jni4net.attributes.ClrMethod("(IILSystem/Array;LSystem/Array;)V")
    public native void ReachInvert_Points(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<system.Array> PointX, net.sf.jni4net.Ref<system.Array> PointY);
    
    @net.sf.jni4net.attributes.ClrMethod("(II)I")
    public native int nNode(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch);
    
    @net.sf.jni4net.attributes.ClrMethod("(III)LSystem/String;")
    public native java.lang.String NodeRS(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.Integer> n);
    
    @net.sf.jni4net.attributes.ClrMethod("(IILSystem/String;)I")
    public native int NodeIndex(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.String> Rs);
    
    @net.sf.jni4net.attributes.ClrMethod("(III)I")
    public native int NodeType(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.Integer> n);
    
    @net.sf.jni4net.attributes.ClrMethod("(III)LSystem/String;")
    public native java.lang.String NodeCType(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.Integer> n);
    
    @net.sf.jni4net.attributes.ClrMethod("(III)I")
    public native int NodeCutLine_nPoints(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.Integer> n);
    
    @net.sf.jni4net.attributes.ClrMethod("(IIILSystem/Array;LSystem/Array;)V")
    public native void NodeCutLine_Points(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.Integer> n, net.sf.jni4net.Ref<system.Array> PointX, net.sf.jni4net.Ref<system.Array> PointY);
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Save();
}
//</generated-proxy>
