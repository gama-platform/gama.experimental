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
import msi.gaml.operators.Cast;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;

@skill(name = "RSkill", concept = { IConcept.STATISTIC, IConcept.SKILL })
public class RSkill extends Skill {

	class TextConsole implements RMainLoopCallbacks {
		@Override
		public void rWriteConsole(final Rengine re, final String text, final int oType) {
//			System.out.print("xxxx"+text);
			GAMA.getGui().getConsole(null).informConsole(text, null);
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

	private String[] args=new String[] {" --no-save" };
	private Rengine re = null;

	@action(name = "R_eval", args = {
			@arg(name = "command", type = IType.STRING, optional = true, doc = @doc("R command to be evalutated")) }, doc = @doc(value = "evaluate the R command", returns = "object in R.", examples = {
					@example(" R_eval(\"data(iris)\")") }))
	public Object primREval(final IScope scope) throws GamaRuntimeException {
		// re = new Rengine(args, false, new TextConsole());
		String env = System.getProperty("java.library.path");
		if(!env.contains("jri")) {			
			String RPath = GamaPreferences.External.LIB_R.value(scope).getPath(scope).replace("libjri.jnilib", "").replace("libjri.so", "").replace("jri.dll", "");
			System.setProperty("java.library.path", RPath+ ";" + env);
			try {
				java.lang.reflect.Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
				fieldSysPath.setAccessible( true );
				fieldSysPath.set( null, null );
			}catch(Exception ex) {
				ex.printStackTrace();
			}
//			System.out.println(System.getProperty("java.library.path"));
		}
//		System.loadLibrary("jri");
		re=Rengine.getMainEngine();
		if(re==null) {			
			re = new Rengine(args, false, new TextConsole());
			
		}
		final REXP x = re.eval((String) scope.getArg("command", IType.STRING));
		
		System.out.println(" ");
		System.out.println(x.getType());
		System.out.println(x.getContent());
		return dataConvert(x);
	}

	public Object dataConvert(Object o) {
		REXP x;
		if(o instanceof REXP) {
			x=(REXP)o;
		}else {
			return null;
		}
		
		
		if(x.getType()==REXP.XT_ARRAY_STR) {
			String[] s=x.asStringArray();

			GamaList a=(GamaList) GamaListFactory.create();
			for(int i=0; i<s.length;i++) {
				a.add(s[i]);
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
				a.add(Cast.asFloat(null, f.at(i)));
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
