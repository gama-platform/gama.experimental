package ummisco.gaml.extensions.hecras.skill;

import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.skill;

import java.io.File;
import java.io.IOException;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
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
		int res=0;
		Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
		hrc = new HecRasEngine();
		scope.getSimulation().postDisposeAction(scope1 -> {
			try {
				if(hrc!=null) {					
					hrc.QuitRas();
					Ole32.INSTANCE.CoUninitialize();
					hrc=null;
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

	@action(name = "Project_Open", args = {
			@arg(name = "file", type = IType.STRING, optional = false, doc = @doc("project path")) }, doc = @doc(value = "open hecras project", returns = "opened hecras project", examples = {
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
	
	@action(name = "QuitRas", doc = @doc(value = "QuitRas", returns = "QuitRas", examples = {
			@example("QuitRas()") }))
	public Object primQuitRas(final IScope scope) throws GamaRuntimeException {
		int res = 0;
		try {
			res = hrc.QuitRas();
			Ole32.INSTANCE.CoUninitialize();
			hrc=null;
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
