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
 
import java.util.ArrayList;

import org.jinterop.dcom.impls.automation.SafeArrayBounds;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.OaIdl;
import com.sun.jna.platform.win32.OaIdl.SAFEARRAY;
import com.sun.jna.platform.win32.OaIdl.SAFEARRAYBOUND;
import com.sun.jna.platform.win32.OaIdl.SAFEARRAYByReference;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.OleAuto;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.Variant.VARIANT;
import com.sun.jna.platform.win32.Variant.VARIANT.ByReference;
import com.sun.jna.platform.win32.WTypes.VARTYPE;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.LONG;
import com.sun.jna.platform.win32.WinDef.UINT;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMLateBindingObject;
import com.sun.jna.platform.win32.COM.Dispatch;
import com.sun.jna.platform.win32.COM.IDispatch;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

public class Program2 extends COMLateBindingObject {
	public static void main(String args[]) {
		Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
		Program2 p=new Program2(); 
        try {
    		p.Project_Open("E:\\Downloads\\Hello World Coupling\\HelloWorldCoupling.prj"); 
    		p.Compute_CurrentPlan( );
        } finally {
        	p.QuitRas();
            Ole32.INSTANCE.CoUninitialize();
        }
	}
    public Program2() throws COMException {
        super("RAS506.HECRASController", false);
    }

    public Program2(boolean visible) throws COMException {
        this();
//        this.setVisible(visible);
    }
//
//    public void setVisible(boolean bVisible) throws COMException {
//        this.setProperty("Visible", bVisible);
//    }
//
//    public String getVersion() throws COMException {
//        return this.getStringProperty("Version");
//    }
//
//    public void newExcelBook() throws COMException {
//        this.invokeNoReply("Add", getWorkbooks());
//    }
//
    public void QuitRas () throws COMException {
    	//Project_Open             hrc.Project_Open(@"C:\Users\hqngh\OneDrive\Documents\Hello World Coupling\HelloWorldCoupling.prj");

        this.invokeNoReply("QuitRas");
    }
//
    public void Project_Open (String filename) throws COMException {
    	//Project_Open             hrc.Project_Open(@"C:\Users\hqngh\OneDrive\Documents\Hello World Coupling\HelloWorldCoupling.prj");

        this.invokeNoReply("Project_Open",  new VARIANT(filename));
    }
    
    public SAFEARRAY.ByReference createVarArray(int size) {
    	  SAFEARRAY.ByReference psa;
    	  SAFEARRAYBOUND[] rgsabound = new SAFEARRAYBOUND[1];
    	  rgsabound[0] = new SAFEARRAYBOUND(size, 0);
    	  psa = OleAuto.INSTANCE.SafeArrayCreate(
    	      new VARTYPE(Variant.VT_VARIANT), new UINT(1), rgsabound);
    	  return psa;
    	}

public class StringByReference extends ByReference {
    public StringByReference() {
        this(0);
    }

    public StringByReference(int size) {
        super();
        getPointer().clear(size < 4 ? 4 : size);
    }

    public StringByReference(String str) {
        super();
        setValue(str);
    }

    private void setValue(String str) {
        getPointer().setString(0, str);
    }

    public String getValue() {
        return getPointer().getString(0);
    }
}
    public void Compute_CurrentPlan( ) throws COMException {
    	//Project_Open             hrc.Project_Open(@"C:\Users\hqngh\OneDrive\Documents\Hello World Coupling\HelloWorldCoupling.prj");
//         Pointer p = new Memory(8*Native.getNativeSize(Byte.TYPE));      
//////         ArrayList aa = null;
//        for (int i = 0; i < 8; i++) {
//            p.setByte(i*Native.getNativeSize(Byte.TYPE),(byte) i);
//        }
    	WString[] tabs=new WString[] {new WString("0"),new WString("1"),new WString("2"),new WString("3"),new WString("4"),new WString("4"),new WString("4"),new WString("4")};
    	String[] array = new String[8];
    	//    	Byte[] bb=new Byte[] {}; 
//    	Pointer aarray=new Pointer(8);
    	
    	
//    	PointerByReference parray=new PointerByReference();
//    	parray.setPointer(aarray);
//    	SAFEARRAYByReference va=new SAFEARRAYByReference(parray.getPointer());
//    	va.setAutoRead(true);
//    	va.setAutoWrite(true);
        VARIANT rrr = new VARIANT(new LONG(0));
        VARIANT.ByReference rr=new ByReference(rrr.getPointer());
//        VARIANT vv = new VARIANT(new StringByReference(4).getPointer());
//        VARIANT.ByReference vv = new VARIANT.ByReference(new StringArray(tabs));
        
        SAFEARRAY vaa=SAFEARRAY.createSafeArray( new VARTYPE(Variant.VT_BSTR), 8);

        VARIANT vava=new VARIANT(vaa);
        //        vava.setValue(vaa);
        

//        long nmsg = -999;
//        boolean BlockingMode = true;
//        boolean fbyref=true;
////        SAFEARRAY abc = new SAFEARRAY(8);
//        VARIANT v[]=new VARIANT[3];
//        v[0]=new VARIANT(nmsg);
//        v[1]=new VARIANT(vaa);
//        v[2]=new VARIANT(BlockingMode);
//        Variant retval;
    	VARIANT V=this.invoke("Compute_CurrentPlan", rr,vava);
    	System.out.println(V);
    }
//
//    public void closeActiveWorkbook(boolean bSave) throws COMException {
//        this.invokeNoReply("Close", getActiveWorkbook(), new VARIANT(bSave));
//    }
//
//    public void quit() throws COMException {
//        this.invokeNoReply("Quit");
//    }
//
//    public void insertValue(String range, String value) throws COMException {
//        Range pRange = new Range(this.getAutomationProperty("Range",
//                this.getActiveSheet(), new VARIANT(range)));
//        this.setProperty("Value", pRange, new VARIANT(value));
//    }
//
//    public Application getApplication() {
//        return new Application(this.getAutomationProperty("Application"));
//    }
//
//    public ActiveWorkbook getActiveWorkbook() {
//        return new ActiveWorkbook(this.getAutomationProperty("ActiveWorkbook"));
//    }
//
//    public Workbooks getWorkbooks() {
//        return new Workbooks(this.getAutomationProperty("WorkBooks"));
//    }
//
//    public ActiveSheet getActiveSheet() {
//        return new ActiveSheet(this.getAutomationProperty("ActiveSheet"));
//    }
//
//    public class Application extends COMLateBindingObject {
//
//        public Application(IDispatch iDispatch) throws COMException {
//            super(iDispatch);
//        }
//    }
//
//    public class Workbooks extends COMLateBindingObject {
//        public Workbooks(IDispatch iDispatch) throws COMException {
//            super(iDispatch);
//        }
//    }
//
//    public class ActiveWorkbook extends COMLateBindingObject {
//        public ActiveWorkbook(IDispatch iDispatch) throws COMException {
//            super(iDispatch);
//        }
//    }
//
//    public class ActiveSheet extends COMLateBindingObject {
//        public ActiveSheet(IDispatch iDispatch) throws COMException {
//            super(iDispatch);
//        }
//    }
//
//    public class Range extends COMLateBindingObject {
//        public Range(IDispatch iDispatch) throws COMException {
//            super(iDispatch);
//        }
//    }
}
