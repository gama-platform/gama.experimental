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
public class HECRASControllerClass extends system.MarshalByRefObject implements ras506._HECRASController, ras506.HECRASController, ras506.__HECRASController_Event {
    
    //<generated-proxy>
    private static system.Type staticType;
    
    protected HECRASControllerClass(net.sf.jni4net.inj.INJEnv __env, long __handle) {
            super(__env, __handle);
    }
    
    @net.sf.jni4net.attributes.ClrConstructor("()V")
    public HECRASControllerClass() {
            super(((net.sf.jni4net.inj.INJEnv)(null)), 0);
        ras506.HECRASControllerClass.__ctorHECRASControllerClass0(this);
    }
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    private native static void __ctorHECRASControllerClass0(net.sf.jni4net.inj.IClrProxy thiz);
    
    @net.sf.jni4net.attributes.ClrMethod("()Z")
    public native boolean getComputeStartedFromController();
    
    @net.sf.jni4net.attributes.ClrMethod("(Z)V")
    public native void setComputeStartedFromController(boolean ComputeStartedFromController);
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void QuitRas();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void ShowRas();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void ShowRasMapper();
    
    @net.sf.jni4net.attributes.ClrMethod("()Z")
    public native boolean Compute_Cancel();
    
    @net.sf.jni4net.attributes.ClrMethod("()Z")
    public native boolean Compute_Complete();
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;)LSystem/String;")
    public native java.lang.String Create_WATPlanName(net.sf.jni4net.Ref<java.lang.String> HECBasePlanName, net.sf.jni4net.Ref<java.lang.String> SimulationName);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;LSystem/String;LSystem/String;LSystem/String;LSystem/String;LSystem/String;LSystem/String;LSystem/String;Z)Z")
    public native boolean Compute_WATPlan(net.sf.jni4net.Ref<java.lang.String> RasBasePlanTitle, net.sf.jni4net.Ref<java.lang.String> SimluationName, net.sf.jni4net.Ref<java.lang.String> newFPart, net.sf.jni4net.Ref<java.lang.String> DestinationDirectory, net.sf.jni4net.Ref<java.lang.String> InputDSSFile, net.sf.jni4net.Ref<java.lang.String> OutputDSSFile, net.sf.jni4net.Ref<java.lang.String> StartDate, net.sf.jni4net.Ref<java.lang.String> StartTime, net.sf.jni4net.Ref<java.lang.String> EndDate, net.sf.jni4net.Ref<java.lang.String> EndTime, net.sf.jni4net.Ref<java.lang.Boolean> ShowMessageList);
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Compute_ShowComputationWindow();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Compute_HideComputationWindow();
    
    @net.sf.jni4net.attributes.ClrMethod("(ILSystem/Array;Z)Z")
    public native boolean Compute_CurrentPlan(net.sf.jni4net.Ref<java.lang.Integer> nmsg, net.sf.jni4net.Ref<system.Array> Msg, net.sf.jni4net.Ref<java.lang.Boolean> BlockingMode);
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String ProjectionSRSFilename();
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;ILSystem/Array;LSystem/Array;)V")
    public native void Schematic_StorageAreaPolygon(net.sf.jni4net.Ref<java.lang.String> Name, net.sf.jni4net.Ref<java.lang.Integer> count, net.sf.jni4net.Ref<system.Array> x, net.sf.jni4net.Ref<system.Array> Y);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;ILSystem/Array;LSystem/Array;)LSystem/Object;")
    public native system.Object Schematic_D2FlowAreaPolygon(net.sf.jni4net.Ref<java.lang.String> Name, net.sf.jni4net.Ref<java.lang.Integer> count, net.sf.jni4net.Ref<system.Array> x, net.sf.jni4net.Ref<system.Array> Y);
    
    @net.sf.jni4net.attributes.ClrMethod("()I")
    public native int Schematic_ReachCount();
    
    @net.sf.jni4net.attributes.ClrMethod("()I")
    public native int Schematic_ReachPointCount();
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/Array;LSystem/Array;LSystem/Array;LSystem/Array;LSystem/Array;LSystem/Array;)V")
    public native void Schematic_ReachPoints(net.sf.jni4net.Ref<system.Array> RiverName_0, net.sf.jni4net.Ref<system.Array> ReachName_0, net.sf.jni4net.Ref<system.Array> ReachStartIndex_0, net.sf.jni4net.Ref<system.Array> ReachPointCount_0, net.sf.jni4net.Ref<system.Array> ReachPointX_0, net.sf.jni4net.Ref<system.Array> ReachPointY_0);
    
    @net.sf.jni4net.attributes.ClrMethod("()I")
    public native int Schematic_XSCount();
    
    @net.sf.jni4net.attributes.ClrMethod("()I")
    public native int Schematic_XSPointCount();
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/Array;LSystem/Array;LSystem/Array;LSystem/Array;LSystem/Array;LSystem/Array;)V")
    public native void Schematic_XSPoints(net.sf.jni4net.Ref<system.Array> RSName_0, net.sf.jni4net.Ref<system.Array> ReachIndex_0, net.sf.jni4net.Ref<system.Array> XSStartIndex_0, net.sf.jni4net.Ref<system.Array> XSPointCount_0, net.sf.jni4net.Ref<system.Array> XSPointX_0, net.sf.jni4net.Ref<system.Array> XSPointY_0);
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void ExportGIS();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void SteadyFlow_ClearFlowData();
    
    @net.sf.jni4net.attributes.ClrMethod("(I)V")
    public native void setSteadyFlownProfile(int );
    
    @net.sf.jni4net.attributes.ClrMethod("()I")
    public native int getSteadyFlownProfile();
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;LSystem/Array;)V")
    public native void SteadyFlow_SetFlow(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<system.Array> Flow);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;ZLSystem/Array;)Z")
    public native boolean SteadyFlow_FixedWSBoundary(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.Boolean> Downstream, net.sf.jni4net.Ref<system.Array> WSElev);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;LSystem/String;FLSystem/String;)V")
    public native void UnsteadyFlow_SetGateOpening_Constant(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<java.lang.String> gateName, net.sf.jni4net.Ref<java.lang.Float> OpenHeight, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;)V")
    public native void Edit_XS(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;)V")
    public native void Edit_BC(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;)V")
    public native void Edit_IW(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;)V")
    public native void Edit_LW(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;LSystem/String;)V")
    public native void Edit_AddXS(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;LSystem/String;)V")
    public native void Edit_AddBC(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;LSystem/String;)V")
    public native void Edit_AddIW(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;LSystem/String;)V")
    public native void Edit_AddLW(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Edit_GeometricData();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Edit_SteadyFlowData();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Edit_QuasiUnsteadyFlowData();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Edit_UnsteadyFlowData();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Edit_SedimentData();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Edit_WaterQualityData();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Edit_PlanData();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Edit_MultipleRun();
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;)V")
    public native void PlotXS(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;)V")
    public native void PlotRatingCurve(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;)V")
    public native void PlotPF(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;)V")
    public native void PlotPFGeneral(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;)V")
    public native void PlotXYZ(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;)V")
    public native void PlotStageFlow(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)V")
    public native void PlotStageFlow_SA(net.sf.jni4net.Ref<java.lang.String> saName);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;)V")
    public native void PlotHydraulicTables(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;)V")
    public native void TableXS(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;)V")
    public native void TablePF(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach);
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String CurrentProjectFile();
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String CurrentProjectTitle();
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String CurrentPlanFile();
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String CurrentGeomFile();
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String CurrentGeomHDFFile();
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String CurrentSteadyFile();
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String CurrentUnSteadyFile();
    
    @net.sf.jni4net.attributes.ClrMethod("(ILSystem/Array;LSystem/Array;)V")
    public native void Output_Variables(net.sf.jni4net.Ref<java.lang.Integer> nVar, net.sf.jni4net.Ref<system.Array> VarName, net.sf.jni4net.Ref<system.Array> VarDesc);
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Output_Initialize();
    
    @net.sf.jni4net.attributes.ClrMethod("(ILSystem/Array;)V")
    public native void Output_GetProfiles(net.sf.jni4net.Ref<java.lang.Integer> nProfile, net.sf.jni4net.Ref<system.Array> ProfileName);
    
    @net.sf.jni4net.attributes.ClrMethod("(ILSystem/Array;)V")
    public native void Output_GetRivers(net.sf.jni4net.Ref<java.lang.Integer> nRiver, net.sf.jni4net.Ref<system.Array> River);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)I")
    public native int Output_GetRiver(net.sf.jni4net.Ref<java.lang.String> River);
    
    @net.sf.jni4net.attributes.ClrMethod("(ILSystem/String;)I")
    public native int Output_GetReach(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.String> Reach);
    
    @net.sf.jni4net.attributes.ClrMethod("(IILSystem/Array;)V")
    public native void Output_GetReaches(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> nReach, net.sf.jni4net.Ref<system.Array> Reach);
    
    @net.sf.jni4net.attributes.ClrMethod("(IIILSystem/Array;LSystem/Array;)V")
    public native void Output_GetNodes(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.Integer> nRS, net.sf.jni4net.Ref<system.Array> Rs, net.sf.jni4net.Ref<system.Array> NodeType);
    
    @net.sf.jni4net.attributes.ClrMethod("(IILSystem/String;)I")
    public native int Output_GetNode(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.String> Rs);
    
    @net.sf.jni4net.attributes.ClrMethod("(IIIIII)F")
    public native float Output_NodeOutput(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.Integer> n, net.sf.jni4net.Ref<java.lang.Integer> updn, net.sf.jni4net.Ref<java.lang.Integer> prof, net.sf.jni4net.Ref<java.lang.Integer> nVar);
    
    @net.sf.jni4net.attributes.ClrMethod("(IIIIILSystem/Array;LSystem/Array;LSystem/Array;)V")
    public native void Output_ReachOutput(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.Integer> prof, net.sf.jni4net.Ref<java.lang.Integer> nVar, net.sf.jni4net.Ref<java.lang.Integer> nRS, net.sf.jni4net.Ref<system.Array> Rs, net.sf.jni4net.Ref<system.Array> ChannelDist, net.sf.jni4net.Ref<system.Array> value);
    
    @net.sf.jni4net.attributes.ClrMethod("(IIIIIILSystem/Array;LSystem/Array;LSystem/Array;LSystem/Array;LSystem/Array;LSystem/Array;LSystem/Array;LSystem/Array;)V")
    public native void Output_VelDist(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.Integer> n, net.sf.jni4net.Ref<java.lang.Integer> updn, net.sf.jni4net.Ref<java.lang.Integer> prof, net.sf.jni4net.Ref<java.lang.Integer> nv, net.sf.jni4net.Ref<system.Array> LeftSta, net.sf.jni4net.Ref<system.Array> RightSta, net.sf.jni4net.Ref<system.Array> ConvPerc, net.sf.jni4net.Ref<system.Array> Area, net.sf.jni4net.Ref<system.Array> WP, net.sf.jni4net.Ref<system.Array> Flow, net.sf.jni4net.Ref<system.Array> HydrDepth, net.sf.jni4net.Ref<system.Array> Velocity);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;ZZZZ)LSystem/Object;")
    public native system.Object Output_ComputationLevel_Export(net.sf.jni4net.Ref<java.lang.String> filename, net.sf.jni4net.Ref<java.lang.String> errmsg, net.sf.jni4net.Ref<java.lang.Boolean> WriteFlow, net.sf.jni4net.Ref<java.lang.Boolean> WriteStage, net.sf.jni4net.Ref<java.lang.Boolean> WriteArea, net.sf.jni4net.Ref<java.lang.Boolean> WriteTopWidth);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;ILSystem/Array;LSystem/Array;LSystem/Array;LSystem/String;)Z")
    public native boolean OutputDSS_GetStageFlowSA(net.sf.jni4net.Ref<java.lang.String> StorageArea, net.sf.jni4net.Ref<java.lang.Integer> nvalue, net.sf.jni4net.Ref<system.Array> ValueDateTime, net.sf.jni4net.Ref<system.Array> Stage, net.sf.jni4net.Ref<system.Array> Flow, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;ILSystem/Array;LSystem/Array;LSystem/Array;LSystem/String;)Z")
    public native boolean OutputDSS_GetStageFlow(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<java.lang.Integer> nvalue, net.sf.jni4net.Ref<system.Array> ValueDateTime, net.sf.jni4net.Ref<system.Array> Stage, net.sf.jni4net.Ref<system.Array> Flow, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;)V")
    public native void Project_New(net.sf.jni4net.Ref<java.lang.String> title, net.sf.jni4net.Ref<java.lang.String> filename);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)V")
    public native void Project_Open(java.lang.String projectFilename);
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String Project_Current();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Project_Save();
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)V")
    public native void Project_SaveAs(net.sf.jni4net.Ref<java.lang.String> newProjectName);
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void Project_Close();
    
    @net.sf.jni4net.attributes.ClrMethod("(ILSystem/Array;Z)V")
    public native void Plan_Names(net.sf.jni4net.Ref<java.lang.Integer> PlanCount, net.sf.jni4net.Ref<system.Array> PlanNames, net.sf.jni4net.Ref<java.lang.Boolean> IncludeOnlyPlansInBaseDirectory);
    
    @net.sf.jni4net.attributes.ClrMethod("(ILSystem/Array;)V")
    public native void Plan_Reports(net.sf.jni4net.Ref<java.lang.Integer> ReportCount, net.sf.jni4net.Ref<system.Array> ReportNames);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)LSystem/String;")
    public native java.lang.String Plan_InformationXML(net.sf.jni4net.Ref<java.lang.String> requestXML);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)LSystem/String;")
    public native java.lang.String Plan_GetFilename(net.sf.jni4net.Ref<java.lang.String> planName);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)Z")
    public native boolean Plan_SetCurrent(net.sf.jni4net.Ref<java.lang.String> PlanTitleToSet);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;ZLSystem/String;)Z")
    public native boolean PlanOutput_IsCurrent(net.sf.jni4net.Ref<java.lang.String> PlanTitleToCheck, net.sf.jni4net.Ref<java.lang.Boolean> ShowMessageList, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)Z")
    public native boolean PlanOutput_SetCurrent(net.sf.jni4net.Ref<java.lang.String> PlanTitleToSet);
    
    @net.sf.jni4net.attributes.ClrMethod("(ILSystem/Array;Z)I")
    public native int PlanOutput_SetMultiple(net.sf.jni4net.Ref<java.lang.Integer> nPlanTitleToSet, net.sf.jni4net.Ref<system.Array> PlanTitleToSet_0, net.sf.jni4net.Ref<java.lang.Boolean> ShowMessageList);
    
    @net.sf.jni4net.attributes.ClrMethod("(ILSystem/Array;)V")
    public native void Geometry_GetRivers(net.sf.jni4net.Ref<java.lang.Integer> nRiver, net.sf.jni4net.Ref<system.Array> River);
    
    @net.sf.jni4net.attributes.ClrMethod("()LRAS506/HECRASGeometry;")
    public native ras506.HECRASGeometry Geometry();
    
    @net.sf.jni4net.attributes.ClrMethod("(IILSystem/Array;)V")
    public native void Geometry_GetReaches(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> nReach, net.sf.jni4net.Ref<system.Array> Reach);
    
    @net.sf.jni4net.attributes.ClrMethod("(IIILSystem/Array;LSystem/Array;)V")
    public native void Geometry_GetNodes(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.Integer> nRS, net.sf.jni4net.Ref<system.Array> Rs, net.sf.jni4net.Ref<system.Array> NodeType);
    
    @net.sf.jni4net.attributes.ClrMethod("(IILSystem/String;)I")
    public native int Geometry_GetNode(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rch, net.sf.jni4net.Ref<java.lang.String> Rs);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;ILSystem/Array;LSystem/String;)V")
    public native void Geometry_GetGateNames(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<java.lang.Integer> ngate, net.sf.jni4net.Ref<system.Array> gateName, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(ILSystem/Array;)V")
    public native void Geometry_GetStorageAreas(net.sf.jni4net.Ref<java.lang.Integer> count, net.sf.jni4net.Ref<system.Array> SAnames);
    
    @net.sf.jni4net.attributes.ClrMethod("(ILSystem/Array;)V")
    public native void Geometry_Get2DFlowAreas(net.sf.jni4net.Ref<java.lang.Integer> count, net.sf.jni4net.Ref<system.Array> D2Names);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;)V")
    public native void Geometery_GISImport(net.sf.jni4net.Ref<java.lang.String> title, net.sf.jni4net.Ref<java.lang.String> filename);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)V")
    public native void Geometry_BreachParamSetXML(net.sf.jni4net.Ref<java.lang.String> xmlText);
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String Geometry_BreachParamGetXML();
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;FLSystem/String;)Z")
    public native boolean Geometry_SetSAArea(net.sf.jni4net.Ref<java.lang.String> saName, net.sf.jni4net.Ref<java.lang.Float> Area, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(IIIIIFLSystem/String;)V")
    public native void Geometry_RatioMann(net.sf.jni4net.Ref<java.lang.Integer> riv, net.sf.jni4net.Ref<java.lang.Integer> rchUp, net.sf.jni4net.Ref<java.lang.Integer> nup, net.sf.jni4net.Ref<java.lang.Integer> rchDn, net.sf.jni4net.Ref<java.lang.Integer> ndn, net.sf.jni4net.Ref<java.lang.Float> ratio, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;ILSystem/Array;LSystem/Array;LSystem/String;)Z")
    public native boolean Geometry_GetMann(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<java.lang.Integer> nMann, net.sf.jni4net.Ref<system.Array> Mann_n, net.sf.jni4net.Ref<system.Array> station, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;ILSystem/Array;LSystem/Array;LSystem/String;)Z")
    public native boolean Geometry_SetMann(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<java.lang.Integer> nMann, net.sf.jni4net.Ref<system.Array> Mann_n, net.sf.jni4net.Ref<system.Array> station, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;FFFLSystem/String;)Z")
    public native boolean Geometry_SetMann_LChR(net.sf.jni4net.Ref<java.lang.String> River, net.sf.jni4net.Ref<java.lang.String> Reach, net.sf.jni4net.Ref<java.lang.String> Rs, net.sf.jni4net.Ref<java.lang.Float> MannLOB, net.sf.jni4net.Ref<java.lang.Float> MannChan, net.sf.jni4net.Ref<java.lang.Float> MannROB, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)LSystem/String;")
    public native java.lang.String Geometry_GetGML(java.lang.String geomfilename);
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String HECRASVersion();
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/Array;LSystem/Array;LSystem/String;)V")
    public native void GetDataLocations_Output(net.sf.jni4net.Ref<java.lang.String> planTitle, net.sf.jni4net.Ref<system.Array> DSSFiles, net.sf.jni4net.Ref<system.Array> DSSPathnames, net.sf.jni4net.Ref<java.lang.String> errmsg);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;)LSystem/String;")
    public native java.lang.String wcf_InputDataLocations_Get(net.sf.jni4net.Ref<java.lang.String> projectfile, net.sf.jni4net.Ref<java.lang.String> planTitle);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;)LSystem/String;")
    public native java.lang.String wcf_InputDataLocations_Set(net.sf.jni4net.Ref<java.lang.String> projectfile, net.sf.jni4net.Ref<java.lang.String> planTitle, net.sf.jni4net.Ref<java.lang.String> xmlText);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;LSystem/String;LSystem/String;)LSystem/String;")
    public native java.lang.String wcf_OutputDataLocations(net.sf.jni4net.Ref<java.lang.String> projectfile, net.sf.jni4net.Ref<java.lang.String> PlanFilename, net.sf.jni4net.Ref<java.lang.String> planTitle, net.sf.jni4net.Ref<java.lang.String> planShortID);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)LSystem/String;")
    public native java.lang.String wcf_CreateNewPlan(net.sf.jni4net.Ref<java.lang.String> xmlText);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)LSystem/String;")
    public native java.lang.String wcf_ComputePlan(net.sf.jni4net.Ref<java.lang.String> xmlText);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;LSystem/String;)Z")
    public native boolean wcf_SetOutputPlans(net.sf.jni4net.Ref<java.lang.String> xmlText, net.sf.jni4net.Ref<java.lang.String> errMessage);
    
    @net.sf.jni4net.attributes.ClrMethod("()LSystem/String;")
    public native java.lang.String Plan_GetParameterUncertaintyXML();
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)V")
    public native void Plan_SetParameterUncertaintyXML(net.sf.jni4net.Ref<java.lang.String> xmlText);
    
    @net.sf.jni4net.attributes.ClrMethod("(LRAS506/__HECRASController_ComputeProgressEventEventHandler;)V")
    public native void add_ComputeProgressEvent(ras506.__HECRASController_ComputeProgressEventEventHandler );
    
    @net.sf.jni4net.attributes.ClrMethod("(LRAS506/__HECRASController_ComputeProgressEventEventHandler;)V")
    public native void remove_ComputeProgressEvent(ras506.__HECRASController_ComputeProgressEventEventHandler );
    
    @net.sf.jni4net.attributes.ClrMethod("(LRAS506/__HECRASController_ComputeMessageEventEventHandler;)V")
    public native void add_ComputeMessageEvent(ras506.__HECRASController_ComputeMessageEventEventHandler );
    
    @net.sf.jni4net.attributes.ClrMethod("(LRAS506/__HECRASController_ComputeMessageEventEventHandler;)V")
    public native void remove_ComputeMessageEvent(ras506.__HECRASController_ComputeMessageEventEventHandler );
    
    @net.sf.jni4net.attributes.ClrMethod("(LRAS506/__HECRASController_ComputeCompleteEventHandler;)V")
    public native void add_ComputeComplete(ras506.__HECRASController_ComputeCompleteEventHandler );
    
    @net.sf.jni4net.attributes.ClrMethod("(LRAS506/__HECRASController_ComputeCompleteEventHandler;)V")
    public native void remove_ComputeComplete(ras506.__HECRASController_ComputeCompleteEventHandler );
    
    public static system.Type typeof() {
        return ras506.HECRASControllerClass.staticType;
    }
    
    private static void InitJNI(net.sf.jni4net.inj.INJEnv env, system.Type staticType) {
        ras506.HECRASControllerClass.staticType = staticType;
    }
    //</generated-proxy>
}
