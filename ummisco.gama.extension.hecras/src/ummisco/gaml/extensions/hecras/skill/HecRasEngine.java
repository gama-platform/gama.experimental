package ummisco.gaml.extensions.hecras.skill;
/*
 * The contents of this file is dual-licensed under 2
 * alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to
 * the project.
 *
 * You may obtain a copy of the LGPL License at:
 *
 * http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */

import java.lang.reflect.Array;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.OaIdl.SAFEARRAY;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.Variant.VARIANT;
import com.sun.jna.platform.win32.Variant.VARIANT.ByReference;
import com.sun.jna.platform.win32.WTypes.VARTYPE;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LONG;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMLateBindingObject;

public class HecRasEngine extends COMLateBindingObject {
	public static void main(String args[]) {
		Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
		HecRasEngine p = new HecRasEngine();
		try {
			p.Project_Open("E:\\Downloads\\HWC\\HelloWorldCoupling.prj");
			p.Compute_HideComputationWindow();
			p.Compute_CurrentPlan();
		} finally {
			p.QuitRas();
			Ole32.INSTANCE.CoUninitialize();

//			HWND hwnd = User32.INSTANCE.FindWindow
//		             ("Ras", null); // class name
//		        
//		        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_CLOSE, null, null);
		}
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HecRasEngine() throws COMException {
		super("RAS506.HECRASController", false);
	}

	public HecRasEngine(Boolean visible) throws COMException {
		this();
	}

	public int Project_Open(String filename) throws COMException {
		this.invokeNoReply("Project_Open", new VARIANT(filename));
		return 0;
	}

//	Boolean Compute_CurrentPlan(int nmsg, Array Msg, Boolean BlockingMode) throws COMException { 
// this.invokeNoReply("QuitRas"); 
// return 0; }
	public int Compute_CurrentPlan() throws COMException {

		VARIANT rrr = new VARIANT(new LONG(0));
		VARIANT.ByReference rr = new ByReference(rrr.getPointer());

		SAFEARRAY vaa = SAFEARRAY.createSafeArray(new VARTYPE(Variant.VT_BSTR), 8);
		VARIANT vava = new VARIANT(vaa);
		VARIANT.ByReference pVal = new VARIANT.ByReference(vava.getPointer());

		this.invoke("Compute_CurrentPlan", rr, pVal);
		return 0;
	}

	public int ShowRas() throws COMException {
		this.invokeNoReply("ShowRas");
		return 0;
	}

	public int QuitRas() throws COMException {
		this.invokeNoReply("QuitRas");
		return 0;
	}

	public int ShowRasMapper() throws COMException {
		this.invokeNoReply("ShowRasMapper");
		return 0;
	}

	Boolean Compute_Cancel() throws COMException {
		VARIANT v = this.invoke("Compute_Cancel");
		return v.booleanValue();
	}

	Boolean Compute_Complete() throws COMException {
		VARIANT v = this.invoke("Compute_Complete");
		return v.booleanValue();
	}

	String Create_WATPlanName(String HECBasePlanName, String SimulationName) throws COMException {
		VARIANT v = this.invoke("Create_WATPlanName", new VARIANT(HECBasePlanName), new VARIANT(SimulationName));
		return v.stringValue();
	}

	Boolean Compute_WATPlan(String RasBasePlanTitle, String SimluationName, String newFPart,
			String DestinationDirectory, String InputDSSFile, String OutputDSSFile, String StartDate, String StartTime,
			String EndDate, String EndTime, Boolean ShowMessageList) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(RasBasePlanTitle), new VARIANT(SimluationName), new VARIANT(newFPart),
				new VARIANT(DestinationDirectory), new VARIANT(InputDSSFile), new VARIANT(OutputDSSFile),
				new VARIANT(StartDate), new VARIANT(StartTime), new VARIANT(EndDate), new VARIANT(EndTime),
				new VARIANT(ShowMessageList) };
		VARIANT r = this.invoke("Compute_WATPlan", v);
		return r.booleanValue();
	}

	public int Compute_ShowComputationWindow() throws COMException {
		this.invokeNoReply("Compute_ShowComputationWindow");
		return 0;
	}

	public int Compute_HideComputationWindow() throws COMException {
		this.invokeNoReply("Compute_HideComputationWindow");
		return 0;

	}

	String ProjectionSRSFilename() throws COMException {
		VARIANT r = this.invoke("ProjectionSRSFilename");
		return r.stringValue();
	}

	public int Schematic_StorageAreaPolygon(String Name, int count, Array x, Array Y) throws COMException {
		this.invokeNoReply("Schematic_StorageAreaPolygon");
		return 0;
	}

	public int Schematic_D2FlowAreaPolygon(String Name, int count, Array x, Array Y) throws COMException {
		this.invokeNoReply("Schematic_D2FlowAreaPolygon");
		return 0;
	}

	int Schematic_ReachCount() throws COMException {
		this.invokeNoReply("Schematic_ReachCount");
		return 0;
	}

	int Schematic_ReachPointCount() throws COMException {
		this.invokeNoReply("Schematic_ReachPointCount");
		return 0;
	}

	public int Schematic_ReachPoints(Array RiverName_0, Array ReachName_0, Array ReachStartIndex_0,
			Array ReachPointCount_0, Array ReachPointX_0, Array ReachPointY_0) throws COMException {
		this.invokeNoReply("Schematic_ReachPoints");
		return 0;
	}

	int Schematic_XSCount() throws COMException {
		this.invokeNoReply("Schematic_XSCount");
		return 0;
	}

	int Schematic_XSPointCount() throws COMException {
		this.invokeNoReply("Schematic_XSPointCount");
		return 0;
	}

	public int Schematic_XSPoints(Array RSName_0, Array ReachIndex_0, Array XSStartIndex_0, Array XSPointCount_0,
			Array XSPointX_0, Array XSPointY_0) throws COMException {
		this.invokeNoReply("Schematic_XSPoints");
		return 0;
	}

	public int ExportGIS() throws COMException {
		this.invokeNoReply("ExportGIS");
		return 0;
	}

	public int SteadyFlow_ClearFlowData() throws COMException {
		this.invokeNoReply("SteadyFlow_ClearFlowData");
		return 0;
	}

	public int SteadyFlow_SetFlow(String River, String Reach, String Rs, Pointer Flow) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs), new VARIANT(Flow) };
		this.invokeNoReply("SteadyFlow_SetFlow", v);
		return 0;
	}

	Boolean SteadyFlow_FixedWSBoundary(String River, String Reach, Boolean Downstream, Pointer WSElev)
			throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Downstream),
				new VARIANT(WSElev) };
		VARIANT r = this.invoke("SteadyFlow_FixedWSBoundary", v);
		return r.booleanValue();
	}

	public int UnsteadyFlow_SetGateOpening_Constant(String River, String Reach, String Rs, String gateName,
			float OpenHeight, String errmsg) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs), new VARIANT(gateName),
				new VARIANT(OpenHeight), new VARIANT(errmsg) };

		this.invokeNoReply("UnsteadyFlow_SetGateOpening_Constant", v);
		return 0;
	}

	public int Edit_XS(String River, String Reach, String Rs) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs) };
		this.invokeNoReply("Edit_XS", v);
		return 0;
	}

	public int Edit_BC(String River, String Reach, String Rs) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs) };

		this.invokeNoReply("Edit_BC", v);
		return 0;
	}

	public int Edit_IW(String River, String Reach, String Rs) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs) };
		this.invokeNoReply("Edit_IW", v);
		return 0;
	}

	public int Edit_LW(String River, String Reach, String Rs) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs) };
		this.invokeNoReply("Edit_LW", v);
		return 0;
	}

	public int Edit_AddXS(String River, String Reach, String Rs, String errmsg) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs), new VARIANT(errmsg) };
		this.invokeNoReply("Edit_AddXS", v);
		return 0;
	}

	public int Edit_AddBC(String River, String Reach, String Rs, String errmsg) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs), new VARIANT(errmsg) };
		this.invokeNoReply("Edit_AddBC", v);
		return 0;
	}

	public int Edit_AddIW(String River, String Reach, String Rs, String errmsg) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs), new VARIANT(errmsg) };
		this.invokeNoReply("Edit_AddIW", v);
		return 0;
	}

	public int Edit_AddLW(String River, String Reach, String Rs, String errmsg) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs), new VARIANT(errmsg) };
		this.invokeNoReply("Edit_AddLW", v);
		return 0;
	}

	public int Edit_GeometricData() throws COMException {
		this.invokeNoReply("Edit_GeometricData");
		return 0;
	}

	public int Edit_SteadyFlowData() throws COMException {
		this.invokeNoReply("Edit_SteadyFlowData");
		return 0;
	}

	public int Edit_QuasiUnsteadyFlowData() throws COMException {
		this.invokeNoReply("Edit_QuasiUnsteadyFlowData");
		return 0;
	}

	public int Edit_UnsteadyFlowData() throws COMException {
		this.invokeNoReply("Edit_UnsteadyFlowData");
		return 0;
	}

	public int Edit_SedimentData() throws COMException {
		this.invokeNoReply("Edit_SedimentData");
		return 0;
	}

	public int Edit_WaterQualityData() throws COMException {
		this.invokeNoReply("Edit_WaterQualityData");
		return 0;
	}

	public int Edit_PlanData() throws COMException {
		this.invokeNoReply("Edit_PlanData");
		return 0;
	}

	public int Edit_MultipleRun() throws COMException {
		this.invokeNoReply("Edit_MultipleRun");
		return 0;
	}

	public int PlotXS(String River, String Reach, String Rs) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs) };
		this.invokeNoReply("PlotXS", v);
		return 0;
	}

	public int PlotRatingCurve(String River, String Reach, String Rs) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs) };
		this.invokeNoReply("PlotRatingCurve", v);
		return 0;
	}

	public int PlotPF(String River, String Reach) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach) };
		this.invokeNoReply("PlotPF", v);
		return 0;
	}

	public int PlotPFGeneral(String River, String Reach) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach) };
		this.invokeNoReply("PlotPFGeneral", v);
		return 0;
	}

	public int PlotXYZ(String River, String Reach) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach) };
		this.invokeNoReply("PlotXYZ", v);
		return 0;
	}

	public int PlotStageFlow(String River, String Reach, String Rs) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs) };
		this.invokeNoReply("PlotStageFlow", v);
		return 0;
	}

	public int PlotStageFlow_SA(String saName) throws COMException {
		this.invokeNoReply("PlotStageFlow_SA", new VARIANT(saName));
		return 0;
	}

	public int PlotHydraulicTables(String River, String Reach, String Rs) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs) };
		this.invokeNoReply("PlotHydraulicTables", v);
		return 0;
	}

	public int TableXS(String River, String Reach, String Rs) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs) };
		this.invokeNoReply("TableXS", v);
		return 0;
	}

	public int TablePF(String River, String Reach) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach) };
		this.invokeNoReply("TablePF", v);
		return 0;
	}

	String CurrentProjectFile() throws COMException {
		VARIANT r = this.invoke("CurrentProjectFile");
		return r.stringValue();
	}

	String CurrentProjectTitle() throws COMException {
		VARIANT r = this.invoke("CurrentProjectTitle");
		return r.stringValue();
	}

	String CurrentPlanFile() throws COMException {
		VARIANT r = this.invoke("CurrentPlanFile");
		return r.stringValue();
	}

	String CurrentGeomFile() throws COMException {
		VARIANT r = this.invoke("CurrentGeomFile");
		return r.stringValue();
	}

	String CurrentGeomHDFFile() throws COMException {
		VARIANT r = this.invoke("CurrentGeomHDFFile");
		return r.stringValue();
	}

	String CurrentSteadyFile() throws COMException {
		VARIANT r = this.invoke("CurrentSteadyFile");
		return r.stringValue();
	}

	String CurrentUnSteadyFile() throws COMException {
		VARIANT r = this.invoke("CurrentUnSteadyFile");
		return r.stringValue();
	}

	public int Output_Variables(int nVar, Pointer VarName, Pointer VarDesc) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(nVar), new VARIANT(VarName), new VARIANT(VarDesc) };
		this.invokeNoReply("Output_Variables", v);
		return 0;
	}

	public int Output_Initialize() throws COMException {
		this.invokeNoReply("Output_Initialize");
		return 0;
	}

	public int Output_GetProfiles(int nProfile, Pointer ProfileName) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(nProfile), new VARIANT(ProfileName) };
		this.invokeNoReply("Output_GetProfiles", v);
		return 0;
	}

	public int Output_GetRivers(int nRiver, Pointer River) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(nRiver), new VARIANT(River) };
		this.invokeNoReply("Output_GetRivers", v);
		return 0;
	}

	int Output_GetRiver(String River) throws COMException {
		this.invokeNoReply("Output_GetRiver", new VARIANT(River));
		return 0;
	}

	int Output_GetReach(int riv, String Reach) throws COMException {
		this.invokeNoReply("Output_GetReach", new VARIANT(riv), new VARIANT(Reach));
		return 0;
	}

	public int Output_GetReaches(int riv, int nReach, Pointer Reach) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(riv), new VARIANT(nReach), new VARIANT(Reach) };
		this.invokeNoReply("Output_GetReaches", v);
		return 0;
	}

	public int Output_GetNodes(int riv, int rch, int nRS, Pointer Rs, Pointer NodeType) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(riv), new VARIANT(rch), new VARIANT(nRS), new VARIANT(Rs),
				new VARIANT(NodeType) };
		this.invokeNoReply("Output_GetNodes", v);
		return 0;
	}

	int Output_GetNode(int riv, int rch, String Rs) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(riv), new VARIANT(rch), new VARIANT(Rs) };
		this.invokeNoReply("Output_GetNode", v);
		return 0;
	}

	float Output_NodeOutput(int riv, int rch, int n, int updn, int prof, int nVar) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(riv), new VARIANT(rch), new VARIANT(n), new VARIANT(updn),
				new VARIANT(prof), new VARIANT(nVar) };
		this.invokeNoReply("Output_NodeOutput", v);
		return 0;
	}

	public int Output_ReachOutput(int riv, int rch, int prof, int nVar, int nRS, Pointer Rs, Pointer ChannelDist,
			Pointer value) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(riv), new VARIANT(rch), new VARIANT(prof), new VARIANT(nVar),
				new VARIANT(nRS), new VARIANT(Rs), new VARIANT(ChannelDist), new VARIANT(value) };
		this.invokeNoReply("Output_ReachOutput", v);
		return 0;
	}

	public int Output_VelDist(int riv, int rch, int n, int updn, int prof, int nv, Pointer LeftSta, Pointer RightSta,
			Pointer ConvPerc, Pointer Area, Pointer WP, Pointer Flow, Pointer HydrDepth, Pointer Velocity)
			throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(riv), new VARIANT(rch), new VARIANT(n), new VARIANT(updn),
				new VARIANT(prof), new VARIANT(nv), new VARIANT(LeftSta), new VARIANT(RightSta), new VARIANT(ConvPerc),
				new VARIANT(Area), new VARIANT(Area), new VARIANT(Flow), new VARIANT(HydrDepth),
				new VARIANT(Velocity) };
		this.invokeNoReply("Output_VelDist", v);
		return 0;
	}

	public int Output_ComputationLevel_Export(String filename, String errmsg, Boolean WriteFlow, Boolean WriteStage,
			Boolean WriteArea, Boolean WriteTopWidth) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(filename), new VARIANT(errmsg), new VARIANT(WriteFlow),
				new VARIANT(WriteStage), new VARIANT(WriteArea), new VARIANT(WriteTopWidth) };
		this.invokeNoReply("Output_ComputationLevel_Export", v);
		return 0;
	}

	Boolean OutputDSS_GetStageFlowSA(String StorageArea, int nvalue, Pointer ValueDateTime, Pointer Stage, Pointer Flow,
			String errmsg) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(StorageArea), new VARIANT(nvalue), new VARIANT(ValueDateTime),
				new VARIANT(Stage), new VARIANT(Flow), new VARIANT(errmsg) };
		VARIANT r = this.invoke("OutputDSS_GetStageFlowSA", v);
		return r.booleanValue();
	}

	Boolean OutputDSS_GetStageFlow(String River, String Reach, String Rs, int nvalue, Pointer ValueDateTime,
			Pointer Stage, Pointer Flow, String errmsg) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs), new VARIANT(nvalue),
				new VARIANT(ValueDateTime), new VARIANT(Stage), new VARIANT(Flow), new VARIANT(errmsg) };
		VARIANT r = this.invoke("OutputDSS_GetStageFlow", v);
		return r.booleanValue();
	}

	public int Project_New(String title, String filename) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(title), new VARIANT(filename) };
		this.invokeNoReply("Project_New", v);
		return 0;
	}

	String Project_Current() throws COMException {
		VARIANT r = this.invoke("Project_Current");
		return r.stringValue();
	}

	public int Project_Save() throws COMException {
		this.invokeNoReply("Project_Save");
		return 0;
	}

	public int Project_SaveAs(String newProjectName) throws COMException {
		this.invokeNoReply("Project_SaveAs");
		return 0;
	}

	public int Project_Close() throws COMException {
		this.invokeNoReply("Project_Close");
		return 0;
	}

	public int Plan_Names(int PlanCount, Pointer PlanNames, Boolean IncludeOnlyPlansInBaseDirectory)
			throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(PlanCount), new VARIANT(PlanNames),
				new VARIANT(IncludeOnlyPlansInBaseDirectory) };
		this.invokeNoReply("Plan_Names", v);
		return 0;
	}

	public int Plan_Reports(int ReportCount, Pointer ReportNames) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(ReportCount), new VARIANT(ReportNames) };
		this.invokeNoReply("Plan_Reports", v);
		return 0;
	}

	String Plan_InformationXML(String requestXML) throws COMException {
		VARIANT r = this.invoke("Plan_InformationXML", new VARIANT(requestXML));
		return r.stringValue();
	}

	String Plan_GetFilename(String planName) throws COMException {
		VARIANT r = this.invoke("Plan_GetFilename", new VARIANT(planName));
		return r.stringValue();
	}

	Boolean Plan_SetCurrent(String PlanTitleToSet) throws COMException {
		VARIANT r = this.invoke("Plan_SetCurrent", new VARIANT(PlanTitleToSet));
		return r.booleanValue();
	}

	Boolean PlanOutput_IsCurrent(String PlanTitleToCheck, Boolean ShowMessageList, String errmsg) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(PlanTitleToCheck), new VARIANT(ShowMessageList),
				new VARIANT(errmsg) };
		VARIANT r = this.invoke("PlanOutput_IsCurrent", v);
		return r.booleanValue();
	}

	Boolean PlanOutput_SetCurrent(String PlanTitleToSet) throws COMException {
		VARIANT r = this.invoke("PlanOutput_SetCurrent", new VARIANT(PlanTitleToSet));
		return r.booleanValue();
	}

	int PlanOutput_SetMultiple(int nPlanTitleToSet, Pointer PlanTitleToSet_0, Boolean ShowMessageList)
			throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(nPlanTitleToSet), new VARIANT(PlanTitleToSet_0),
				new VARIANT(ShowMessageList) };
		this.invokeNoReply("PlanOutput_SetMultiple", v);
		return 0;
	}

	public int Geometry_GetRivers(int nRiver, Pointer River) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(nRiver), new VARIANT(River) };
		this.invokeNoReply("Geometry_GetRivers", v);
		return 0;
	}

	String Geometry() throws COMException {// HECRASGeometry
		VARIANT r = this.invoke("Geometry");
		return r.stringValue();
	}

	public int Geometry_GetReaches(int riv, int nReach, Pointer Reach) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(riv), new VARIANT(nReach), new VARIANT(Reach) };
		this.invokeNoReply("Geometry_GetReaches", v);
		return 0;
	}

	public int Geometry_GetNodes(int riv, int rch, int nRS, Pointer Rs, Pointer NodeType) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(riv), new VARIANT(rch), new VARIANT(nRS), new VARIANT(Rs),
				new VARIANT(NodeType) };
		this.invokeNoReply("Geometry_GetNodes", v);
		return 0;
	}

	int Geometry_GetNode(int riv, int rch, String Rs) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(riv), new VARIANT(rch), new VARIANT(Rs) };
		this.invokeNoReply("Geometry_GetNode", v);
		return 0;
	}

	public int Geometry_GetGateNames(String River, String Reach, String Rs, int ngate, Pointer gateName, String errmsg)
			throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs), new VARIANT(ngate),
				new VARIANT(gateName), new VARIANT(errmsg) };
		this.invokeNoReply("Geometry_GetGateNames", v);
		return 0;
	}

	public int Geometry_GetStorageAreas(int count, Pointer SAnames) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(count), new VARIANT(SAnames) };
		this.invokeNoReply("Geometry_GetStorageAreas", v);
		return 0;
	}

	public int Geometry_Get2DFlowAreas(int count, Pointer D2Names) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(count), new VARIANT(D2Names) };
		this.invokeNoReply("Geometry_Get2DFlowAreas", v);
		return 0;
	}

	public int Geometery_GISImport(String title, String filename) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(title), new VARIANT(filename) };
		this.invokeNoReply("Geometery_GISImport", v);
		return 0;
	}

	public int Geometry_BreachParamSetXML(String xmlText) throws COMException {
		this.invokeNoReply("Geometry_BreachParamSetXML", new VARIANT(xmlText));
		return 0;
	}

	String Geometry_BreachParamGetXML() throws COMException {
		VARIANT r = this.invoke("Geometry_BreachParamGetXML");
		return r.stringValue();
	}

	Boolean Geometry_SetSAArea(String saName, float Area, String errmsg) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(saName), new VARIANT(Area), new VARIANT(errmsg) };
		VARIANT r = this.invoke("Geometry_SetSAArea", v);
		return r.booleanValue();
	}

	public int Geometry_RatioMann(int riv, int rchUp, int nup, int rchDn, int ndn, float ratio, String errmsg)
			throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(riv), new VARIANT(rchDn), new VARIANT(nup), new VARIANT(rchDn),
				new VARIANT(ndn), new VARIANT(ratio), new VARIANT(errmsg) };
		this.invokeNoReply("Geometry_RatioMann", v);
		return 0;
	}

	Boolean Geometry_GetMann(String River, String Reach, String Rs, int nMann, Pointer Mann_n, Pointer station,
			String errmsg) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs), new VARIANT(nMann),
				new VARIANT(Mann_n), new VARIANT(station), new VARIANT(errmsg) };
		VARIANT r = this.invoke("Geometry_GetMann", v);
		return r.booleanValue();
	}

	Boolean Geometry_SetMann(String River, String Reach, String Rs, int nMann, Pointer Mann_n, Pointer station,
			String errmsg) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs), new VARIANT(nMann),
				new VARIANT(Mann_n), new VARIANT(station), new VARIANT(errmsg) };
		VARIANT r = this.invoke("Geometry_SetMann", v);
		return r.booleanValue();
	}

	Boolean Geometry_SetMann_LChR(String River, String Reach, String Rs, float MannLOB, float MannChan, float MannROB,
			String errmsg) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(River), new VARIANT(Reach), new VARIANT(Rs), new VARIANT(MannLOB),
				new VARIANT(MannChan), new VARIANT(MannROB), new VARIANT(errmsg) };
		VARIANT r = this.invoke("Geometry_SetMann_LChR", v);
		return r.booleanValue();
	}

	String Geometry_GetGML(String geomfilename) throws COMException {
		VARIANT r = this.invoke("Geometry_GetGML", new VARIANT(geomfilename));
		return r.stringValue();
	}

	String HECRASVersion() throws COMException {
		VARIANT r = this.invoke("HECRASVersion");
		return r.stringValue();
	}

	public int GetDataLocations_Output(String planTitle, Pointer DSSFiles, Pointer DSSPathnames, String errmsg)
			throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(planTitle), new VARIANT(DSSFiles), new VARIANT(DSSPathnames),
				new VARIANT(errmsg) };
		this.invokeNoReply("GetDataLocations_Output", v);
		return 0;
	}

	String wcf_InputDataLocations_Get(String projectfile, String planTitle) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(projectfile), new VARIANT(planTitle) };
		VARIANT r = this.invoke("wcf_InputDataLocations_Get", v);
		return r.stringValue();
	}

	String wcf_InputDataLocations_Set(String projectfile, String planTitle, String xmlText) throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(projectfile), new VARIANT(planTitle), new VARIANT(xmlText) };
		VARIANT r = this.invoke("wcf_InputDataLocations_Set", v);
		return r.stringValue();
	}

	String wcf_OutputDataLocations(String projectfile, String PlanFilename, String planTitle, String planShortID)
			throws COMException {
		VARIANT v[] = new VARIANT[] { new VARIANT(projectfile), new VARIANT(PlanFilename), new VARIANT(planTitle),
				new VARIANT(planShortID) };
		VARIANT r = this.invoke("wcf_OutputDataLocations", v);
		return r.stringValue();
	}

	String wcf_CreateNewPlan(String xmlText) throws COMException {
		VARIANT r = this.invoke("wcf_CreateNewPlan", new VARIANT(xmlText));
		return r.stringValue();
	}

	String wcf_ComputePlan(String xmlText) throws COMException {
		VARIANT r = this.invoke("wcf_ComputePlan", new VARIANT(xmlText));
		return r.stringValue();
	}

	Boolean wcf_SetOutputPlans(String xmlText, String errMessage) throws COMException {
		VARIANT r = this.invoke("wcf_SetOutputPlans", new VARIANT(xmlText), new VARIANT(errMessage));
		return r.booleanValue();
	}

	String Plan_GetParameterUncertaintyXML() throws COMException {
		VARIANT r = this.invoke("Plan_GetParameterUncertaintyXML");
		return r.stringValue();
	}

	public int Plan_SetParameterUncertaintyXML(String xmlText) throws COMException {
		this.invokeNoReply("Plan_SetParameterUncertaintyXML", new VARIANT(xmlText));
		return 0;
	}

//	Boolean ComputeStartedFromController{get;set;}
//
//	int SteadyFlow_nProfile
//	{
//		get;set;
//	}
}
