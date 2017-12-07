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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RFactor;
import org.rosuda.JRI.RList;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

import msi.gama.common.preferences.GamaPreferences;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.precompiler.ITypeProvider;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaColor;
import msi.gama.util.GamaList;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gama.util.file.GamaImageFile;
import msi.gaml.skills.Skill;
import msi.gaml.species.ISpecies;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

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



		final String cmd[]=((String) scope.getArg("command", IType.STRING)).split(System.getProperty("line.separator"));
		int i=0;
		for(i=0; i<cmd.length; i++){
			if(!cmd[i].equals("\r\n")) {				
				Reval(scope,cmd[i].trim());
				
//				System.out.println(cmd[i].trim()+" "+xx);
			}
		}
		
		REXP x =Reval(scope,cmd[i-1].trim());
//		System.out.println(" ");
//		System.out.println(x);
//		System.out.println("type "+x.getType());
//		System.out.println("rtype "+x.rtype);
//		System.out.println("contentclass"+x.getContent().getClass());
//		System.out.println("xp "+x.xp);
		return dataConvert_R2G(x);
	}

	@operator (
			value = "startR",
			content_type = IType.CONTAINER,
			index_type = ITypeProvider.FIRST_CONTENT_TYPE,
			category = { IOperatorCategory.GRAPH },
			concept = { IConcept.STATISTIC, IConcept.CAST })
	@doc (
			value = "to_R_dataframe(speciesname)",
			masterDoc = true,
			comment = "convert agent attributes to dataframe of R",
			examples = @example (
					value = "to_R_dataframe(people)",
					isExecutable = false),
			see = { "R_eval" })
	public String startR(final IScope scope) {
		re = Rengine.getMainEngine();

		if (re == null) {

			re = new Rengine(args, false, new TextConsole());

			if (loadedLib == null) {
				loadedLib = (GamaList) dataConvert_R2G(re.eval("search()"));
			}
		} else {
			scope.getSimulation().postDisposeAction(scope1 -> {
				GamaList l = (GamaList) dataConvert_R2G(re.eval("search()"));
				for (int i = 0; i < l.size(); i++) {
					if (((String) l.get(i)).contains("package:") && loadedLib != null
							&& !loadedLib.contains(l.get(i))) {
						// System.out.println(l.get(i));
						re.eval("detach(\"" + l.get(i) + "\")");
					}
				}
				re.idleEval("rm(list=ls(all=TRUE))");
				re.idleEval("gc()");
				return null;
			});
		}

		return "R started";
	}
	@operator (
			value = "to_R_dataframe",
			content_type = IType.CONTAINER,
			index_type = ITypeProvider.FIRST_CONTENT_TYPE,
			category = { IOperatorCategory.GRAPH },
			concept = { IConcept.STATISTIC, IConcept.CAST })
	@doc (
			value = "to_R_dataframe(speciesname)",
			masterDoc = true,
			comment = "convert agent attributes to dataframe of R",
			examples = @example (
					value = "to_R_dataframe(people)",
					isExecutable = false),
			see = { "R_eval" })
	public static String toRDataFrame(final IScope scope,final ISpecies species) {

		List<String> names = new ArrayList(species.getAttributeNames(scope));
		names.remove("host");
		names.remove("peers");
		names.remove("shape");
		Collections.sort(names);
//		for (final String name : names) {
//			System.out.println(name);
//		}
		IList<? extends IAgent> a = species.getAgents(scope).listValue(scope, Types.AGENT, false);
		List<List> values=new ArrayList();
		for(IAgent aa:a) {			
			int i=0;
			for (final String name : names) {
				Object v=aa.getDirectVarValue(scope, name);
				List vl=null;
				if(values.size()>i) {					
					vl=values.get(i);
				}
				if(vl==null) {
					vl=new ArrayList<>();
					vl.add(name);
					values.add(vl);
				}
				vl.add(v);
//				System.out.println(v.getClass());
				i++;
			}
		}
		
		String df="data.frame(";
		for(List v:values) {
			df=df+v.get(0)+"=c(";
			v.remove(0);
			for(Object o:v) {
				df+=""+dataConvert_G2R(o)+",";
//				System.out.print(o+"           "+dataConvert_G2R(o));
			}
			if(v.size()>0)			
				df=df.substring(0, df.length()-1);
			df+="),";
//			System.out.println("");
		}
		if(values.size()>0)
			df=df.substring(0, df.length()-1);
		df+=")";
//		System.out.println(df);
		
		return df;
	}

	public static Object dataConvert_G2R(Object o) {
		Object res=o.toString();
		if(o instanceof GamaColor) {
			res="\""+((GamaColor)o).stringValue(null)+"\"";
		}
		if(o instanceof GamaImageFile) {
			res="\""+((GamaImageFile)o).getPath(null)+"\"";
		}
		
		if(o instanceof IAgent) {
			res="\""+o+"\"";
		}
		if(o instanceof String) {
			res="\""+o+"\"";
		}
		if(o instanceof GamaPoint) {
			res="\""+((GamaPoint)o).x+","+((GamaPoint)o).y+"\"";
		}
		
		if(o instanceof GamaShape) {
			res="\""+((GamaShape)o).getLocation().x+","+((GamaShape)o).getLocation().y+"\"";
		}
		
		if(o instanceof GamaList) {
			res="c(";
			for(Object obj:((GamaList)o)) {
				res+=""+(obj)+",";
			}
			if(((String)res).length()>2) {				
				res=((String)res).substring(0, ((String)res).length()-1);
				res+=")";
			}else {
				res="\"\"";
			}
		}
		return res;
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
			return null;
		}



		return re.eval(cmd);
		
	}
	
	
	public Object dataConvert_R2G(Object o) {
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
				a.add(dataConvert_R2G(s[i]));
			}
			return a;
		}
		
		if(x.getType()==REXP.DOTSXP) {
			RList s=x.asList();

			GamaList a=(GamaList) GamaListFactory.create();
			for(int i=0; i<s.keys().length;i++) {
				a.add(dataConvert_R2G(s.at(0)));
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

		if(x.getType()==REXP.XT_ARRAY_INT) {
			int[] s=x.asIntArray();

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
				a.add(dataConvert_R2G(f.at(i)));
			}
			return a;
		}
		if(x.getType()==REXP.XT_VECTOR) {
			RVector f=x.asVector();
			GamaList a=(GamaList) GamaListFactory.create();
			for(int i=0; i<f.size(); i++) {
				a.add(dataConvert_R2G(f.at(i)));
			}
			return a;
		}
		return x;
	}
}
