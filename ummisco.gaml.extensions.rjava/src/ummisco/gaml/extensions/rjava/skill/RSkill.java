/*******************************************************************************************************
 *
 * RSkill.java, in ummisco.gaml.extensions.rjava, is part of the source code of the GAMA modeling and simulation
 * platform (v.2.0.0).
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package ummisco.gaml.extensions.rjava.skill;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

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
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gama.util.file.GamaImageFile;
import msi.gaml.skills.Skill;
import msi.gaml.species.ISpecies;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

/**
 * The Class RSkill.
 */
@skill (
		name = "RSkill",
		concept = { IConcept.STATISTIC, IConcept.SKILL })
public class RSkill extends Skill {

	/**
	 * The Class TextConsole.
	 */
	static class TextConsole implements RMainLoopCallbacks {
		@Override
		public void rWriteConsole(final Rengine re, final String text, final int oType) {
			// System.out.print("xxxx"+text);
			GAMA.getGui().getConsole().informConsole("R>" + text, null);
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
			if (fd.getDirectory() != null) { res = fd.getDirectory(); }
			if (fd.getFile() != null) { res = res == null ? fd.getFile() : res + fd.getFile(); }
			return res;
		}

		@Override
		public void rFlushConsole(final Rengine re) {}

		@Override
		public void rLoadHistory(final Rengine re, final String filename) {}

		@Override
		public void rSaveHistory(final Rengine re, final String filename) {}

		/**
		 * R exec J command.
		 *
		 * @param re
		 *            the re
		 * @param commandId
		 *            the command id
		 * @param argsExpr
		 *            the args expr
		 * @param options
		 *            the options
		 * @return the long
		 */
		public long rExecJCommand(final Rengine re, final String commandId, final long argsExpr, final int options) {
			System.out.println("rExecJCommand \"" + commandId + "\"");
			return 0;
		}

		/**
		 * R process J events.
		 *
		 * @param re
		 *            the re
		 */
		public void rProcessJEvents(final Rengine re) {}

	}

	/** The args. */
	private final String[] args = { "--vanilla", "--slave" };

	/** The re. */
	private Rengine re = null;

	/** The loaded lib. */
	private IList<?> loadedLib = null;

	/** The env. */
	private String env;

	/**
	 * Prim R eval.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@action (
			name = "R_eval",
			args = { @arg (
					name = "command",
					type = IType.STRING,
					optional = true,
					doc = @doc ("R command to be evalutated")) },
			doc = @doc (
					value = "evaluate the R command",
					returns = "value in Gama data type",
					examples = { @example (" R_eval(\"data(iris)\")") }))
	public Object primREval(final IScope scope) throws GamaRuntimeException {
		// New line or ';', see #3038
		Pattern p = Pattern.compile(System.lineSeparator() + "|;");
		final String cmd[] = p.split(scope.getStringArg("command"));
		REXP result = null;
		for (int i = 0; i < cmd.length; i++) {
			String command = cmd[i].trim();
			if (!command.isBlank()) { result = Reval(scope, command); }
		}

		return dataConvert_R2G(result);
	}

	/**
	 * Start R.
	 *
	 * @param scope
	 *            the scope
	 * @return the string
	 */
	@action (
			name = "startR",
			doc = @doc (
					value = "evaluate the R command"))

	public String startR(final IScope scope) {
		initEnv(scope);

		re = Rengine.getMainEngine();

		if (re == null) {

			re = new Rengine(args, false, new TextConsole());

			if (loadedLib == null) { loadedLib = (IList<?>) dataConvert_R2G(re.eval("search()")); }
		} else {
			scope.getSimulation().postDisposeAction(scope1 -> {
				final IList<?> l = (IList<?>) dataConvert_R2G(re.eval("search()"));
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

	/**
	 * To R data.
	 *
	 * @param scope
	 *            the scope
	 * @param o
	 *            the o
	 * @return the object
	 */
	@operator (
			value = "to_R_data",
			content_type = IType.CONTAINER,
			index_type = ITypeProvider.FIRST_CONTENT_TYPE,
			category = { IOperatorCategory.GRAPH },
			concept = { IConcept.STATISTIC, IConcept.CAST })
	@doc (
			value = "to_R_data(speciesname)",
			masterDoc = true,
			comment = "convert agent attributes to data type of R",
			examples = @example (
					value = "to_R_data(people)",
					isExecutable = false),
			see = { "R_eval" })
	public static Object toRData(final IScope scope, final Object o) {
		return dataConvert_G2R(o);
	}

	/**
	 * To R data frame.
	 *
	 * @param scope
	 *            the scope
	 * @param species
	 *            the species
	 * @return the string
	 */
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
	public static String toRDataFrame(final IScope scope, final ISpecies species) {

		final List<String> names = new ArrayList<>(species.getAttributeNames(scope));
		names.remove("host");
		names.remove("peers");
		names.remove("shape");
		Collections.sort(names);
		// for (final String name : names) {
		// System.out.println(name);
		// }
		final IList<? extends IAgent> a = species.getAgents(scope).listValue(scope, Types.AGENT, false);
		final List<List> values = new ArrayList<>();
		for (final IAgent aa : a) {
			int i = 0;
			for (final String name : names) {
				final Object v = aa.getDirectVarValue(scope, name);
				List<Object> vl = null;
				if (values.size() > i) { vl = values.get(i); }
				if (vl == null) {
					vl = new ArrayList<>();
					vl.add(name);
					values.add(vl);
				}
				vl.add(v);
				// System.out.println(v.getClass());
				i++;
			}
		}

		String df = "data.frame(";
		for (final List<?> v : values) {
			df = df + v.get(0) + "=c(";
			v.remove(0);
			for (final Object o : v) {
				df += "" + dataConvert_G2R(o) + ",";
				// System.out.print(o+" "+dataConvert_G2R(o));
			}
			if (v.size() > 0) { df = df.substring(0, df.length() - 1); }
			df += "),";
			// System.out.println("");
		}
		if (values.size() > 0) { df = df.substring(0, df.length() - 1); }
		df += ")";
		// System.out.println(df);

		return df;
	}

	/**
	 * Data convert G 2 R.
	 *
	 * @param o
	 *            the o
	 * @return the object
	 */
	public static Object dataConvert_G2R(final Object o) {
		Object res = "\"" + o.toString() + "\"";
		if (o instanceof Integer || o instanceof Double) { res = o.toString(); }
		if (o instanceof Boolean) { res = (Boolean) o ? "TRUE" : "FALSE"; }
		if (o instanceof GamaColor) { res = "\"" + ((GamaColor) o).stringValue(null) + "\""; }
		if (o instanceof GamaImageFile) { res = "\"" + ((GamaImageFile) o).getPath(null) + "\""; }

		// if(o instanceof IAgent) {
		// res="\""+o+"\"";
		// }
		// if(o instanceof String) {
		// res="\""+o+"\"";
		// }
		if (o instanceof GamaPoint) { res = "\"" + ((GamaPoint) o).x + "," + ((GamaPoint) o).y + "\""; }

		if (o instanceof GamaShape) {
			res = "\"" + ((GamaShape) o).getLocation().x + "," + ((GamaShape) o).getLocation().y + "\"";
		}

		if (o instanceof IList) {
			res = "c(";
			for (final Object obj : (IList<?>) o) { res += "" + obj + ","; }
			if (((String) res).length() > 2) {
				res = ((String) res).substring(0, ((String) res).length() - 1);
				res += ")";
			} else {
				res = "\"\"";
			}
		}
		return res;
	}

	/**
	 * Inits the env.
	 *
	 * @param scope
	 *            the scope
	 */
	public void initEnv(final IScope scope) {
		env = System.getProperty("java.library.path");
		if(!env.contains("jri")) {
			throw GamaRuntimeException.error("The path to the JRI is not set. Add the option -Djava.library.path=a_path to  your  GAMA.ini file (see the documentation for more details).", scope);			
		}

// This does not work anymore with JAVA 17+.  An issue is that REngine load locally the lib...
// This prevents us to allow the moeler to set the R path in the GUI interface....
//		if (!env.contains("jri")) {
//			final String RPath = GamaPreferences.External.LIB_R.value(scope).getPath(scope).replace("libjri.jnilib", "")
//					.replace("libjri.so", "").replace("jri.dll", "");
//			if (System.getProperty("os.name").startsWith("Windows")) {
//				System.setProperty("java.library.path", RPath + ";" + env);
//			} else {
//				System.setProperty("java.library.path", RPath + ":" + env);
//			}
//			try {
//				final java.lang.reflect.Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
//				fieldSysPath.setAccessible(true);
//				fieldSysPath.set(null, null);
//
//			} catch (final Exception ex) {
//				scope.getGui().getConsole().informConsole(ex.getMessage(), null);
//				ex.printStackTrace();
//			}
//			// System.out.println(System.getProperty("java.library.path"));
//		}
//		System.loadLibrary("jri");

		if (System.getenv("R_HOME") == null)
			throw GamaRuntimeException.error("The R_HOME environment variable is not set. R cannot be run.", scope);
	}

	/**
	 * Reval.
	 *
	 * @param scope
	 *            the scope
	 * @param cmd
	 *            the cmd
	 * @return the rexp
	 */
	public REXP Reval(final IScope scope, final String cmd) {
		try {
			re = Rengine.getMainEngine();
			if (re == null) return null;
		} catch (final Exception ex) {
			throw GamaRuntimeException.error("R cannot be found ...", scope);
		}

		return re.eval(cmd);

	}

	/**
	 * Data convert R 2 G.
	 *
	 * @param o
	 *            the o
	 * @return the object
	 */
	public Object dataConvert_R2G(final Object o) {
		REXP x;
		if (!(o instanceof REXP)) return o;
		x = (REXP) o;

		if (x.getType() == REXP.XT_ARRAY_STR) {
			final String[] s = x.asStringArray();

			final IList<Object> a = GamaListFactory.create();
			for (final String element : s) { a.add(dataConvert_R2G(element)); }
			return a;
		}

		if (x.getType() == REXP.DOTSXP) {
			final RList s = x.asList();

			final IList<Object> a = GamaListFactory.create();
			for (int i = 0; i < s.keys().length; i++) { a.add(dataConvert_R2G(s.at(0))); }
			return a;
		}

		if (x.getType() == REXP.XT_ARRAY_BOOL_INT) {
			final int[] s = x.asIntArray();

			final IList<Object> a = GamaListFactory.create();
			for (final int element : s) { a.add(element != 0); }
			return a;
		}
		if (x.getType() == REXP.XT_ARRAY_DOUBLE) {
			final double[] s = x.asDoubleArray();

			final IList<Object> a = GamaListFactory.create();
			for (final double element : s) { a.add(element); }
			return a;
		}

		if (x.getType() == REXP.XT_ARRAY_INT) {
			final int[] s = x.asIntArray();

			final IList<Object> a = GamaListFactory.create();
			for (final int element : s) { a.add(element); }
			return a;
		}

		if (x.getType() == REXP.XT_STR) return x.getContent();
		if (x.getType() == REXP.XT_FACTOR) {
			final RFactor f = x.asFactor();
			final IList<Object> a = GamaListFactory.create();
			for (int i = 0; i < f.size(); i++) { a.add(dataConvert_R2G(f.at(i))); }
			return a;
		}
		if (x.getType() == REXP.XT_VECTOR) {
			final RVector f = x.asVector();
			final IList<Object> a = GamaListFactory.create();
			for (int i = 0; i < f.size(); i++) { a.add(dataConvert_R2G(f.at(i))); }
			return a;
		}
		return x;
	}
}
