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
public class HECRASGeometryClass extends system.MarshalByRefObject implements ras506._HECRASGeometry, ras506.HECRASGeometry {
    
    //<generated-proxy>
    private static system.Type staticType;
    
    protected HECRASGeometryClass(net.sf.jni4net.inj.INJEnv __env, long __handle) {
            super(__env, __handle);
    }
    
    @net.sf.jni4net.attributes.ClrConstructor("()V")
    public HECRASGeometryClass() {
            super(((net.sf.jni4net.inj.INJEnv)(null)), 0);
        ras506.HECRASGeometryClass.__ctorHECRASGeometryClass0(this);
    }
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    private native static void __ctorHECRASGeometryClass0(net.sf.jni4net.inj.IClrProxy thiz);
    
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
    
    public static system.Type typeof() {
        return ras506.HECRASGeometryClass.staticType;
    }
    
    private static void InitJNI(net.sf.jni4net.inj.INJEnv env, system.Type staticType) {
        ras506.HECRASGeometryClass.staticType = staticType;
    }
    //</generated-proxy>
}