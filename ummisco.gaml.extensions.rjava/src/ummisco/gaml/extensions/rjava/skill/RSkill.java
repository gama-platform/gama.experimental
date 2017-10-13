/*********************************************************************************************
 *
 * 'RSkill.java, in plugin ummisco.gaml.extensions.rjava, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gaml.extensions.rjava.skill;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RFactor;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

import msi.gama.common.preferences.GamaPreferences;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;
import msi.gama.util.GamaListFactory;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;

@skill(name = "RSkill", concept = { IConcept.STATISTIC, IConcept.SKILL })
public class RSkill extends Skill {

	class TextConsole implements RMainLoopCallbacks {
		@Override
		public void rWriteConsole(final Rengine re, final String text, final int oType) {
//			System.out.print("xxxx"+text);
			GAMA.getGui().getConsole(null).informConsole("R>"+text, null);
		}

		@Override
		public void rBusy(final Rengine re, final int which) {
			System.out.println("rBusy(" + which + ")");
		}

		@Override
		public String rReadConsole(final Rengine re, final String prompt, final int addToHistory) {
			System.out.print(prompt);
			try {
				final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				final String s = br.readLine();
				return s == null || s.length() == 0 ? s : s + "\n";
			} catch (final Exception e) {
				System.out.println("jriReadConsole exception: " + e.getMessage());
			}
			return null;
		}

		@Override
		public void rShowMessage(final Rengine re, final String message) {
			System.out.println("rShowMessage \"" + message + "\"");
		}

		@Override
		public String rChooseFile(final Rengine re, final int newFile) {
			final FileDialog fd = new FileDialog(new Frame(), newFile == 0 ? "Select a file" : "Select a new file",
					newFile == 0 ? FileDialog.LOAD : FileDialog.SAVE);
			fd.setVisible(true);
			String res = null;
			if (fd.getDirectory() != null)
				res = fd.getDirectory();
			if (fd.getFile() != null)
				res = res == null ? fd.getFile() : res + fd.getFile();
			return res;
		}

		@Override
		public void rFlushConsole(final Rengine re) {
		}

		@Override
		public void rLoadHistory(final Rengine re, final String filename) {
		}

		@Override
		public void rSaveHistory(final Rengine re, final String filename) {
		}

		public long rExecJCommand(final Rengine re, final String commandId, final long argsExpr, final int options) {
			System.out.println("rExecJCommand \"" + commandId + "\"");
			return 0;
		}

		public void rProcessJEvents(final Rengine re) {
		}

	}

	private String[] args=new String[] {"--vanilla" };
	private Rengine re = null;
	private GamaList loadedLib=null;
	private String env;
	@action(name = "R_eval", args = {
			@arg(name = "command", type = IType.STRING, optional = true, doc = @doc("R command to be evalutated")) }, doc = @doc(value = "evaluate the R command", returns = "value in Gama data type", examples = {
					@example(" R_eval(\"data(iris)\")") }))
	public Object primREval(final IScope scope) throws GamaRuntimeException {
		// re = new Rengine(args, false, new TextConsole());
		initEnv(scope);



		final String cmd[]=((String) scope.getArg("command", IType.STRING)).split("\r\n");
		int i=0;
		for(i=0; i<cmd.length; i++){
			if(!cmd[i].equals("\r\n")) {				
				Reval(scope,cmd[i].trim());
				
//				System.out.println(cmd[i].trim()+" "+x);
			}
		}
		
		REXP x =Reval(scope,cmd[i-1].trim());
//		System.out.println(" ");
//		System.out.println(x);
//		System.out.println("type "+x.getType());
//		System.out.println("rtype "+x.rtype);
//		System.out.println("contentclass"+x.getContent().getClass());
//		System.out.println("xp "+x.xp);
		return dataConvert(x);
	}

	public void initEnv(final IScope scope) {
		env = System.getProperty("java.library.path");
		if(!env.contains("jri")) {			
			String RPath = GamaPreferences.External.LIB_R.value(scope).getPath(scope).replace("libjri.jnilib", "").replace("libjri.so", "").replace("jri.dll", "");
			if(System.getProperty("os.name").startsWith("Windows")) {				
				System.setProperty("java.library.path", RPath+ ";" + env);
			}else {
				System.setProperty("java.library.path", RPath+ ":" + env);
			}
			try {
				java.lang.reflect.Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
				fieldSysPath.setAccessible( true );
				fieldSysPath.set( null, null );
//				System.loadLibrary("jri");
				
			}catch(Exception ex) {
				scope.getGui().getConsole(scope).informConsole(ex.getMessage(), null);
				ex.printStackTrace();
			}
//			System.out.println(System.getProperty("java.library.path"));
		}
//		if(System.getenv("R_HOME")==null) {
//			return "missing R_HOME";
//		}
	}
	public REXP Reval(final IScope scope, final String cmd) {
		re=Rengine.getMainEngine();
		
		if(re==null) {			
			
			re = new Rengine(args, false, new TextConsole());

			if(loadedLib==null) {
				loadedLib=(GamaList) dataConvert(re.eval("search()"));
			}
		}else {
			scope.getSimulation().postDisposeAction(scope1 -> {
				GamaList l=(GamaList) dataConvert(re.eval("search()"));
				for(int i=0;i<l.size();i++) {
					if(((String) l.get(i)).contains("package:") && !loadedLib.contains(l.get(i))) {
//						System.out.println(l.get(i));
						re.eval("detach(\""+l.get(i)+"\")");
					}
				}
				re.idleEval("rm(list=ls(all=TRUE))");
				re.idleEval("gc()");
				return null;
			});
		}



		return re.eval(cmd);
		
	}
	
	
	public Object dataConvert(Object o) {
		REXP x;
		if(o instanceof REXP) {
			x=(REXP)o;
		}else {
			return o;
		}
		
		
		if(x.getType()==REXP.XT_ARRAY_STR) {
			String[] s=x.asStringArray();

			GamaList a=(GamaList) GamaListFactory.create();
			for(int i=0; i<s.length;i++) {
				a.add(dataConvert(s[i]));
			}
			return a;
		}
		
		if(x.getType()==REXP.DOTSXP) {
			RList s=x.asList();

			GamaList a=(GamaList) GamaListFactory.create();
			for(int i=0; i<s.keys().length;i++) {
				a.add(dataConvert(s.at(0)));
			}
			return a;
		}
		
		if(x.getType()==REXP.XT_ARRAY_BOOL_INT) {
			int[] s=x.asIntArray();

			GamaList a=(GamaList) GamaListFactory.create();
			for(int i=0; i<s.length;i++) {
				a.add(s[i]==0?false:true);
			}
			return a;
		}
		if(x.getType()==REXP.XT_ARRAY_DOUBLE) {
			double[] s=x.asDoubleArray();

			GamaList a=(GamaList) GamaListFactory.create();
			for(int i=0; i<s.length;i++) {
				a.add(s[i]);
			}
			return a;
		}

		if(x.getType()==REXP.XT_STR) {
			return (String)x.getContent();
		}
		if(x.getType()==REXP.XT_FACTOR) {
			RFactor f=x.asFactor();
			GamaList a=(GamaList) GamaListFactory.create();
			for(int i=0; i<f.size(); i++) {
				a.add(dataConvert(f.at(i)));
			}
			return a;
		}
		if(x.getType()==REXP.XT_VECTOR) {
			RVector f=x.asVector();
			GamaList a=(GamaList) GamaListFactory.create();
			for(int i=0; i<f.size(); i++) {
				a.add(dataConvert(f.at(i)));
			}
			return a;
		}
		return x;
	}
}
