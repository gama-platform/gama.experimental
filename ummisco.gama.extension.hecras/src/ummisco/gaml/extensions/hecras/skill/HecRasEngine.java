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

	public HecRasEngine(boolean visible) throws COMException {
		this();
	}

	public void Compute_ShowComputationWindow() throws COMException {

		this.invokeNoReply("Compute_ShowComputationWindow");
	}

	public int Compute_HideComputationWindow() throws COMException {

		this.invokeNoReply("Compute_HideComputationWindow");
		return 0;
		
	}
	public int QuitRas() throws COMException {

		this.invokeNoReply("QuitRas");
		return 0;
	}

	public int Project_Open(String filename) throws COMException {

		this.invokeNoReply("Project_Open", new VARIANT(filename));
		return 0;
	}

	public int Compute_CurrentPlan() throws COMException {

		VARIANT rrr = new VARIANT(new LONG(0));
		VARIANT.ByReference rr = new ByReference(rrr.getPointer());

		SAFEARRAY vaa = SAFEARRAY.createSafeArray(new VARTYPE(Variant.VT_BSTR), 8);
		VARIANT vava = new VARIANT(vaa);
		VARIANT.ByReference pVal = new VARIANT.ByReference(vava.getPointer());

		this.invoke("Compute_CurrentPlan", rr, pVal);
		return 0;
	}
}
