package ummisco.gaml.extensions.hecras.skill;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

//import com.jacob.activeX.*;
//import com.jacob.com.*;
/**
 * This class uses the the Jacob tech
 * to use and interact with a Com cmponent 
 * in a java application
 */
public class ReadDLL {   
//	 public static void main(String[] args) {
//	        /**
//	         * `System.getProperty("os.arch")`
//	         * It'll tell us on which platform Java Program is executing. Based on that we'll load respective DLL file.
//	         * Placed under same folder of program file(.java/.class).
//	         */
////	        String libFile = System.getProperty("os.arch").equals("amd64") ? "jacob-1.17-x64.dll" : "jacob-1.17-x86.dll";
//		 	String libFile="E:\\git\\gama.experimental\\ummisco.gama.extension.hecras\\lib\\jacob-1.19\\jacob-1.19-x64.dll";
//	        try {
//	            /* Read DLL file*/
////	            InputStream inputStream = ReadDLL.class.getResourceAsStream(libFile);
////	            /**
////	             *  Step 1: Create temporary file under <%user.home%>\AppData\Local\Temp\jacob.dll 
////	             *  Step 2: Write contents of `inputStream` to that temporary file.
////	             */
////	            File temporaryDll = File.createTempFile("jacob", ".dll");
////	            FileOutputStream outputStream = new FileOutputStream(temporaryDll);
////	            byte[] array = new byte[8192];
////	            for (int i = inputStream.read(array); i != -1; i = inputStream.read(array)) {
////	                outputStream.write(array, 0, i);
////	            }
////	            outputStream.close();
//	            /**
//	             * `System.setProperty(LibraryLoader.JACOB_DLL_PATH, temporaryDll.getAbsolutePath());`
//	             * Set System property same like setting java home path in system.
//	             * 
//	             * `LibraryLoader.loadJacobLibrary();`
//	             * Load JACOB library in current System.
//	             */
//	            System.setProperty(LibraryLoader.JACOB_DLL_PATH, libFile);
//	            LibraryLoader.loadJacobLibrary();
//
//	            /**
//	             * Create ActiveXComponent using CLSID. You can also use program id here.
//	             * Next line(commented line/compProgramID) shows you how you can create ActiveXComponent using ProgramID.
//	             */
////	            ActiveXComponent compCLSID = new ActiveXComponent("clsid:{00024500-0000-0000-C000-000000000046}");
//	            /*ActiveXComponent compProgramID = new ActiveXComponent("Excel.Application");*/
//	            ActiveXComponent compCLSID = new ActiveXComponent("RAS506.HECRASController");
//
//	            System.out.println("The Library been loaded, and an activeX component been created");
//	            
//	            /**
//	             * This is function/method of Microsoft Excel to use it with COM bridge.
//	             * Excel methods and its use can be found on
//	             * http://msdn.microsoft.com/en-us/library/bb179167(v=office.12).aspx
//	             * 
//	             * Understand code:
//	             * 1. Make Excel visible
//	             * 2. Get workbook of excel object.
//	             * 3. Open 1test.xls1 file in excel
//	             */
//	            
////	            for(int i=0; i<100; i++) {
////	            	System.out.println(i);
////	            	System.out.println();
//	            	try {	            	
//	            		Dispatch.call(compCLSID, "Project_Open", new Variant("E:\\Downloads\\HWC\\HelloWorldCoupling.prj"));
//	            		SafeArray sa = new SafeArray(Variant.VariantString, 1);
////	            	    sa.setString(1, "null");  
//	            	    SafeArray sa1 = new SafeArray(Variant.VariantLongInt, 1);
//	            	    sa1.setLong(1, 0);  
////	            	    sa.setString(0, "Unsteady Flow 01");  
//	                    Variant pData= new Variant(Variant.VariantPointer,true); 
//	                    pData.putLong(1);
//	                    Variant vn = new Variant(Variant.VariantArray,true);
//	                    vn.putSafeArrayRef(sa);
////	            		Variant vsvs=new Variant(new String[] {});
//	                    
//	                    Variant res=Dispatch.call(compCLSID, "Plan_GetFilename", new Variant("Plan 04"));
//	                    System.out.println(res);
//	                    Variant optional = new Variant();
//	        			
//	            		Dispatch.call(compCLSID, "Compute_CurrentPlan",pData, vn);
//	            	}catch(Exception ex){ex.printStackTrace();}
//	            	finally {	            		            	
//	            		Dispatch.call(compCLSID, "QuitRas");
//	            		System.out.println("closed");
//	            	}
//	            	
////	            }
//
//	            
////	            Dispatch.put(compCLSID, "Visible", new Variant(true));
////	            Dispatch workbook = compCLSID.getProperty("Workbooks").toDispatch();
////	            Dispatch.call(workbook, "Open", new Variant("D:\\test\\test.xls"));
//
//	            /* Temporary file will be removed after terminating-closing-ending the application-program */
////	            temporaryDll.deleteOnExit();
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	        }
//	    }
	
//	public static void main(String[] args){
//		
//		//Loading the library:
//		ActiveXComponent comp=new ActiveXComponent("Com.Calculation");		
//		System.out.println("The Library been loaded, and an activeX component been created");
//		
//		int arg1=100;
//		int arg2=50;
//		//using the functions from the library:		
//		int summation=Dispatch.call(comp, "sum",arg1,arg2).toInt();
//		System.out.println("Summation= "+ summation);
//		
//		int subtraction= Dispatch.call(comp,"subtract",arg1,arg2).toInt();
//		System.out.println("Subtraction= "+ subtraction);
//		
//		int multiplication=Dispatch.call(comp,"multi",arg1,arg2).toInt();
//		System.out.println("Multiplication= "+ multiplication);
//		
//		double division=Dispatch.call(comp,"div",arg1,arg2).toDouble();
//		System.out.println("Division= "+ division);
//		
//		/**The following code is abstract of using the get,
//		 * when the library contains a function that return
//		 * some kind of a struct, and then get its properties and values
//		 **/
////		Dispatch disp=Dispatch.call(comp,"sum",100,10).toDispatch();
////		DataType Var=Dispatch.get(disp,"Property Name");
//	}
}
