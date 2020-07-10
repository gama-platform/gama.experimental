package ummisco.gaml.extensions.hecras.skill;

import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.skill;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang3.ArrayUtils;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;

import com.sun.jna.platform.win32.WinUser;

import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gaml.types.IType;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.IList;
import msi.gaml.skills.Skill;

@skill(name = "hecrasSkill", concept = { IConcept.STATISTIC, IConcept.SKILL })
@doc("read hecras data")
public class HecRasSkill extends Skill {

	public HecRasEngine hrc;

	@action(name = "load_hecras", doc = @doc(value = "instantiate hecras engine", returns = "running hecras engine", examples = {
			@example("load_hecras") }))

	public Object load_hecras(final IScope scope) {
		int res = 0;
		Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
		hrc = new HecRasEngine();
		scope.getSimulation().postDisposeAction(scope1 -> {
			try {
				if (hrc != null) {
					hrc.QuitRas();
					hrc.release();
					Ole32.INSTANCE.CoUninitialize();
					hrc = null;
//					HWND hwnd = User32.INSTANCE.FindWindow
//							("Ras", null); // class name
//					
//					User32.INSTANCE.PostMessage(hwnd, WinUser.WM_CLOSE, null, null);
				}
			} catch (Exception ex) {
				scope.getGui().getConsole().informConsole(ex.getMessage(), null);
				ex.printStackTrace();
			}
			return null;
		});
		return res;
	}

	@action(name = "Generate_RasMap", args = {
			@arg(name = "filePath", type = IType.STRING, optional = false, doc = @doc("filePath")),
			@arg(name = "planName", type = IType.STRING, optional = false, doc = @doc("planName")),
			@arg(name = "simDate", type = IType.STRING, optional = false, doc = @doc("simDate")),
			@arg(name = "startHour", type = IType.INT, optional = false, doc = @doc("startHour")),
			@arg(name = "endHour", type = IType.INT, optional = false, doc = @doc("endHour")),
			@arg(name = "startMin", type = IType.INT, optional = false, doc = @doc("startMin")),
			@arg(name = "endMin", type = IType.INT, optional = false, doc = @doc("endMin")), }, doc = @doc(value = "Generate_RasMap hecras", returns = "vrf files", examples = {
					@example("generateTiff(\r\n"
							+ "				\"C:\\\\git\\\\gama.experimental\\\\ummisco.gama.extension.hecras\\\\models\\\\GAMA to hecras\\\\HWC\\\\HWC2.rasmap\",\r\n"
							+ "				\"Plan 04\", \"25JUL2019\", 0, 24, 0, 60)") }))
	public Object primGenerate_RasMap(final IScope scope) throws GamaRuntimeException {
		int res = 0;
		try {
			String filePath = scope.getStringArg("filePath");
			String planName = scope.getStringArg("planName");
			String simDate = scope.getStringArg("simDate");
			int startHour = scope.getIntArg("startHour");
			int endHour = scope.getIntArg("endHour");
			int startMin = scope.getIntArg("startMin");
			int endMin = scope.getIntArg("endMin");
			res = hrc.generateTiff(filePath, planName, simDate, startHour, endHour, startMin, endMin);

		} catch (Exception ex) {
			scope.getGui().getConsole().informConsole(ex.getMessage(), null);
			ex.printStackTrace();
		}
		return res;
	}

	@action(name = "Project_Open", args = {
			@arg(name = "file", type = IType.FILE, optional = false, doc = @doc("project path")) }, doc = @doc(value = "open hecras project", returns = "opened hecras project", examples = {
					@example("Project_Open(\"E:\\Downloads\\HWC\\HelloWorldCoupling.prj\")") }))
	public Object primProject_Open(final IScope scope) throws GamaRuntimeException {
		int res = 0;
		try {
			String a = scope.getStringArg("file");
			res = hrc.Project_Open(a);
		} catch (Exception ex) {
			scope.getGui().getConsole().informConsole(ex.getMessage(), null);
			ex.printStackTrace();
		}
		return res;
	}

	@action(name = "Compute_CurrentPlan", doc = @doc(value = "Compute CurrentPlan", returns = "Computed CurrentPlan", examples = {
			@example("Compute_CurrentPlan()") }))
	public Object primCompute_CurrentPlan(final IScope scope) throws GamaRuntimeException {
		int res = 0;
		try {
			res = hrc.Compute_CurrentPlan();
		} catch (Exception ex) {
			scope.getGui().getConsole().informConsole(ex.getMessage(), null);
			ex.printStackTrace();
		}
		return res;
	}

	@action(name = "Compute_HideComputationWindow", doc = @doc(value = "Compute_HideComputationWindow", returns = "Compute_HideComputationWindow", examples = {
			@example("Compute_HideComputationWindow()") }))
	public Object primCompute_HideComputationWindow(final IScope scope) throws GamaRuntimeException {
		int res = 0;
		try {
			res = hrc.Compute_HideComputationWindow();
		} catch (Exception ex) {
			scope.getGui().getConsole().informConsole(ex.getMessage(), null);
			ex.printStackTrace();
		}
		return res;
	}

	@action(name = "Project_Close", doc = @doc(value = "Project_Close", returns = "Project_Close", examples = {
			@example("Project_Close()") }))
	public Object primProject_Close(final IScope scope) throws GamaRuntimeException {
		int res = 0;
		try {
			res = hrc.Project_Close();
		} catch (Exception ex) {
			scope.getGui().getConsole().informConsole(ex.getMessage(), null);
			ex.printStackTrace();
		}
		return res;
	}

	@action(name = "ExportGIS", doc = @doc(value = "ExportGIS", returns = "ExportGIS", examples = {
			@example("ExportGIS()") }))
	public Object primExportGIS(final IScope scope) throws GamaRuntimeException {
		int res = 0;
		try {
			res = hrc.ExportGIS();
		} catch (Exception ex) {
			scope.getGui().getConsole().informConsole(ex.getMessage(), null);
			ex.printStackTrace();
		}
		return res;
	}

	@action(name = "Update_Data", args = {
			@arg(name = "num", type = IType.INT, optional = false, doc = @doc("number step")) }, doc = @doc(value = "update data", returns = "updated data", examples = {
					@example("Update_Data(100)") }))
	public Object primUpdate_Data(final IScope scope) throws GamaRuntimeException {
		int res = 0;
		try {

			Integer num = scope.getIntArg("num");
			File file = new File("E:\\Downloads\\HWC\\HelloWorldCoupling.p04.hdf");

			try (HdfFile hdfFile = new HdfFile(file)) {
				Dataset dataset = hdfFile.getDatasetByPath(
						"/Results/Unsteady/Output/Output Blocks/Base Output/Unsteady Time Series/2D Flow Areas/Hello 2D Area/Depth");
				// data will be a java array of the dimensions of the HDF5 dataset
				float[][] data = (float[][]) dataset.getData();
				try (PrintWriter p = new PrintWriter(
						new FileOutputStream("E:\\git\\hecras_gama_coupling\\includes\\Depth.csv", false))) {
					int x = dataset.getDimensions()[0];
					int y = dataset.getDimensions()[1];
//					p.println("ncols 40");//"+x);1118216
//					p.println("nrows 20");//"+y);
//					p.println("xllcorner     0.0");
//					p.println("yllcorner     0.0");
//					p.println("cellsize      2.0");

					float[] oneDArray = new float[(int) dataset.getSize()];
					for (int i = 0; i < x; i++) {
						for (int s = 0; s < y; s++) {
							oneDArray[(i * y) + s] = data[i][s];
						}
					}

					float[][][] frame = new float[2000][][];
					int f = 0;
					int fi = 0;
					while (fi < oneDArray.length - 800) {

						frame[f] = new float[20][40];
						for (int i = 0; i < 20; i++) {
							for (int s = 0; s < 40; s++) {
								frame[f][i][s] = oneDArray[fi];
								fi++;
							}
						}
						f++;
					}

					p.println(ArrayUtils.toString(frame[num]).replace("},{", "\n").replace("{{", "").replace("}}", ""));// .replace(",",
																														// "
																														// ")
					p.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				// System.out.println(ArrayUtils.toString(data));
			}

		} catch (Exception ex) {
			scope.getGui().getConsole().informConsole(ex.getMessage(), null);
			ex.printStackTrace();
		}
		return res;
	}

	@action(name = "QuitRas", doc = @doc(value = "QuitRas", returns = "QuitRas", examples = { @example("QuitRas()") }))
	public Object primQuitRas(final IScope scope) throws GamaRuntimeException {
		int res = 0;
		try {
			res = hrc.QuitRas();
			hrc.release();
			Ole32.INSTANCE.CoUninitialize();
			hrc = null;
//			HWND hwnd = User32.INSTANCE.FindWindow
//		             ("Ras", null); // class name
//		        
//		        User32.INSTANCE.PostMessage(hwnd, WinUser.WM_CLOSE, null, null);
		} catch (Exception ex) {
			scope.getGui().getConsole().informConsole(ex.getMessage(), null);
			ex.printStackTrace();
		}
		return res;
	}
}
