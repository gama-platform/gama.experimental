//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by jni4net. See http://jni4net.sourceforge.net/ 
//     Runtime Version:4.0.30319.42000
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace RAS506 {
    
    
    #region Component Designer generated code 
    public partial class HECRASFlowClass_ {
        
        public static global::java.lang.Class _class {
            get {
                return global::RAS506.@__HECRASFlowClass.staticClass;
            }
        }
    }
    #endregion
    
    #region Component Designer generated code 
    [global::net.sf.jni4net.attributes.JavaProxyAttribute(typeof(global::RAS506.HECRASFlowClass), typeof(global::RAS506.HECRASFlowClass_))]
    [global::net.sf.jni4net.attributes.ClrWrapperAttribute(typeof(global::RAS506.HECRASFlowClass), typeof(global::RAS506.HECRASFlowClass_))]
    internal sealed partial class @__HECRASFlowClass : global::java.lang.Object {
        
        internal new static global::java.lang.Class staticClass;
        
        private @__HECRASFlowClass(global::net.sf.jni4net.jni.JNIEnv @__env) : 
                base(@__env) {
        }
        
        private static void InitJNI(global::net.sf.jni4net.jni.JNIEnv @__env, java.lang.Class @__class) {
            global::RAS506.@__HECRASFlowClass.staticClass = @__class;
        }
        
        private static global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod> @__Init(global::net.sf.jni4net.jni.JNIEnv @__env, global::java.lang.Class @__class) {
            global::System.Type @__type = typeof(__HECRASFlowClass);
            global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod> methods = new global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod>();
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "UnsteadyBoundaryIndex", "UnsteadyBoundaryIndex0", "(Lnet/sf/jni4net/Ref;Lnet/sf/jni4net/Ref;Lnet/sf/jni4net/Ref;Lnet/sf/jni4net/Ref;" +
                        "Lnet/sf/jni4net/Ref;)I"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "__ctorHECRASFlowClass0", "__ctorHECRASFlowClass0", "(Lnet/sf/jni4net/inj/IClrProxy;)V"));
            return methods;
        }
        
        private static int UnsteadyBoundaryIndex0(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle River, global::net.sf.jni4net.utils.JniLocalHandle Reach, global::net.sf.jni4net.utils.JniLocalHandle Rs, global::net.sf.jni4net.utils.JniLocalHandle StorageArea, global::net.sf.jni4net.utils.JniLocalHandle Connection) {
            // (Lnet/sf/jni4net/Ref;Lnet/sf/jni4net/Ref;Lnet/sf/jni4net/Ref;Lnet/sf/jni4net/Ref;Lnet/sf/jni4net/Ref;)I
            // (LSystem/String;LSystem/String;LSystem/String;LSystem/String;LSystem/String;)I
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            int @__return = default(int);
            try {
            string @__ref_River = net.sf.jni4net.Ref.GetValue<string>(@__env, River);
            string @__ref_Reach = net.sf.jni4net.Ref.GetValue<string>(@__env, Reach);
            string @__ref_Rs = net.sf.jni4net.Ref.GetValue<string>(@__env, Rs);
            string @__ref_StorageArea = net.sf.jni4net.Ref.GetValue<string>(@__env, StorageArea);
            string @__ref_Connection = net.sf.jni4net.Ref.GetValue<string>(@__env, Connection);
            global::RAS506.HECRASFlowClass @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::RAS506.HECRASFlowClass>(@__env, @__obj);
            @__return = ((int)(((global::RAS506._HECRASFlow)(@__real)).UnsteadyBoundaryIndex(ref __ref_River, ref __ref_Reach, ref __ref_Rs, ref __ref_StorageArea, ref __ref_Connection)));
            net.sf.jni4net.Ref.SetValue<string>(@__env, River, @__ref_River);
            net.sf.jni4net.Ref.SetValue<string>(@__env, Reach, @__ref_Reach);
            net.sf.jni4net.Ref.SetValue<string>(@__env, Rs, @__ref_Rs);
            net.sf.jni4net.Ref.SetValue<string>(@__env, StorageArea, @__ref_StorageArea);
            net.sf.jni4net.Ref.SetValue<string>(@__env, Connection, @__ref_Connection);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static void @__ctorHECRASFlowClass0(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__class, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::RAS506.HECRASFlowClass @__real = new global::RAS506.HECRASFlowClass();
            global::net.sf.jni4net.utils.Convertor.InitProxy(@__env, @__obj, @__real);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        new internal sealed class ContructionHelper : global::net.sf.jni4net.utils.IConstructionHelper {
            
            public global::net.sf.jni4net.jni.IJvmProxy CreateProxy(global::net.sf.jni4net.jni.JNIEnv @__env) {
                return new global::RAS506.@__HECRASFlowClass(@__env);
            }
        }
    }
    #endregion
}
