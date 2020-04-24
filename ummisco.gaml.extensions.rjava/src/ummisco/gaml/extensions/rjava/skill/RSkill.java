/*********************************************************************************************
 *
 * 'RSkill.java, in plugin ummisco.gaml.extensions.rjava, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang.SystemUtils;
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

@skill (
		name = "RSkill",
		concept = { IConcept.STATISTIC, IConcept.SKILL })
public class RSkill extends Skill {

	class TextConsole implements RMainLoopCallbacks {
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
			if (fd.getDirectory() != null) {
				res = fd.getDirectory();
			}
			if (fd.getFile() != null) {
				res = res == null ? fd.getFile() : res + fd.getFile();
			}
			return res;
		}

		@Override
		public void rFlushConsole(final Rengine re) {}

		@Override
		public void rLoadHistory(final Rengine re, final String filename) {}

		@Override
		public void rSaveHistory(final Rengine re, final String filename) {}

		public long rExecJCommand(final Rengine re, final String commandId, final long argsExpr, final int options) {
			System.out.println("rExecJCommand \"" + commandId + "\"");
			return 0;
		}

		public void rProcessJEvents(final Rengine re) {}

	}

	private final String[] args = new String[] { "--vanilla" };
	private Rengine re = null;
	private IList<?> loadedLib = null;
	private String env;

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
		// re = new Rengine(args, false, new TextConsole());

		final String cmd[] =
				((String) scope.getArg("command", IType.STRING)).split(System.getProperty("line.separator"));
		int i = 0;
		for (i = 0; i < cmd.length; i++) {
			if (!cmd[i].equals("\r\n")) {
				Reval(scope, cmd[i].trim());

				// System.out.println(cmd[i].trim()+" "+xx);
			}
		}

		final REXP x = Reval(scope, cmd[i - 1].trim());
		// System.out.println(" ");
		// System.out.println(x);
		// System.out.println("type "+x.getType());
		// System.out.println("rtype "+x.rtype);
		// System.out.println("contentclass"+x.getContent().getClass());
		// System.out.println("xp "+x.xp);
		return dataConvert_R2G(x);
	}

	@action (
			name = "startR",
			doc = @doc (
					value = "evaluate the R command",
					returns = "value in Gama data type",
					examples = { @example ("startR") }))

	public String startR(final IScope scope) {
		initEnv(scope);

		re = Rengine.getMainEngine();

		if (re == null) {

			re = new Rengine(args, false, new TextConsole());

			if (loadedLib == null) {
				loadedLib = (IList<?>) dataConvert_R2G(re.eval("search()"));
			}
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
				if (values.size() > i) {
					vl = values.get(i);
				}
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
			if (v.size() > 0) {
				df = df.substring(0, df.length() - 1);
			}
			df += "),";
			// System.out.println("");
		}
		if (values.size() > 0) {
			df = df.substring(0, df.length() - 1);
		}
		df += ")";
		// System.out.println(df);

		return df;
	}

	public static Object dataConvert_G2R(final Object o) {
		Object res = "\"" + o.toString() + "\"";
		if (o instanceof Integer || o instanceof Double) {
			res = o.toString();
		}
		if (o instanceof Boolean) {
			res = (Boolean) o ? "TRUE" : "FALSE";
		}
		if (o instanceof GamaColor) {
			res = "\"" + ((GamaColor) o).stringValue(null) + "\"";
		}
		if (o instanceof GamaImageFile) {
			res = "\"" + ((GamaImageFile) o).getPath(null) + "\"";
		}

		// if(o instanceof IAgent) {
		// res="\""+o+"\"";
		// }
		// if(o instanceof String) {
		// res="\""+o+"\"";
		// }
		if (o instanceof GamaPoint) {
			res = "\"" + ((GamaPoint) o).x + "," + ((GamaPoint) o).y + "\"";
		}

		if (o instanceof GamaShape) {
			res = "\"" + ((GamaShape) o).getLocation().x + "," + ((GamaShape) o).getLocation().y + "\"";
		}

		if (o instanceof IList) {
			res = "c(";
			for (final Object obj : (IList<?>) o) {
				res += "" + obj + ",";
			}
			if (((String) res).length() > 2) {
				res = ((String) res).substring(0, ((String) res).length() - 1);
				res += ")";
			} else {
				res = "\"\"";
			}
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> void setenv(final String key, final String value) {
		try {
			/// we obtain the actual environment
			final Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
			final Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
			final boolean environmentAccessibility = theEnvironmentField.isAccessible();
			theEnvironmentField.setAccessible(true);

			final Map<K, V> env = (Map<K, V>) theEnvironmentField.get(null);

			if (SystemUtils.IS_OS_WINDOWS) {
				// This is all that is needed on windows running java jdk 1.8.0_92
				if (value == null) {
					env.remove(key);
				} else {
					env.put((K) key, (V) value);
				}
			} else {
				// This is triggered to work on openjdk 1.8.0_91
				// The ProcessEnvironment$Variable is the key of the map
				final Class<K> variableClass = (Class<K>) Class.forName("java.lang.ProcessEnvironment$Variable");
				final Method convertToVariable = variableClass.getMethod("valueOf", String.class);
				final boolean conversionVariableAccessibility = convertToVariable.isAccessible();
				convertToVariable.setAccessible(true);

				// The ProcessEnvironment$Value is the value fo the map
				final Class<V> valueClass = (Class<V>) Class.forName("java.lang.ProcessEnvironment$Value");
				final Method convertToValue = valueClass.getMethod("valueOf", String.class);
				final boolean conversionValueAccessibility = convertToValue.isAccessible();
				convertToValue.setAccessible(true);

				if (value == null) {
					env.remove(convertToVariable.invoke(null, key));
				} else {
					// we place the new value inside the map after conversion so as to
					// avoid class cast exceptions when rerunning this code
					env.put((K) convertToVariable.invoke(null, key), (V) convertToValue.invoke(null, value));

					// reset accessibility to what they were
					convertToValue.setAccessible(conversionValueAccessibility);
					convertToVariable.setAccessible(conversionVariableAccessibility);
				}
			}
			// reset environment accessibility
			theEnvironmentField.setAccessible(environmentAccessibility);

			// we apply the same to the case insensitive environment
			final Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
					.getDeclaredField("theCaseInsensitiveEnvironment");
			final boolean insensitiveAccessibility = theCaseInsensitiveEnvironmentField.isAccessible();
			theCaseInsensitiveEnvironmentField.setAccessible(true);
			// Not entirely sure if this needs to be casted to ProcessEnvironment$Variable
			// and $Value as well
			final Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
			if (value == null) {
				// remove if null
				cienv.remove(key);
			} else {
				cienv.put(key, value);
			}
			theCaseInsensitiveEnvironmentField.setAccessible(insensitiveAccessibility);
		} catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException
				| InvocationTargetException e) {
			throw new IllegalStateException("Failed setting environment variable <" + key + "> to <" + value + ">", e);
		} catch (final NoSuchFieldException e) {
			// we could not find theEnvironment
			final Map<String, String> env = System.getenv();
			Stream.of(Collections.class.getDeclaredClasses())
					// obtain the declared classes of type $UnmodifiableMap
					.filter(c1 -> "java.util.Collections$UnmodifiableMap".equals(c1.getName())).map(c1 -> {
						try {
							return c1.getDeclaredField("m");
						} catch (final NoSuchFieldException e1) {
							throw new IllegalStateException("Failed setting environment variable <" + key + "> to <"
									+ value + "> when locating in-class memory map of environment", e1);
						}
					}).forEach(field -> {
						try {
							final boolean fieldAccessibility = field.isAccessible();
							field.setAccessible(true);
							// we obtain the environment
							final Map<String, String> map = (Map<String, String>) field.get(env);
							if (value == null) {
								// remove if null
								map.remove(key);
							} else {
								map.put(key, value);
							}
							// reset accessibility
							field.setAccessible(fieldAccessibility);
						} catch (final ConcurrentModificationException e1) {
							// This may happen if we keep backups of the environment before calling this
							// method
							// as the map that we kept as a backup may be picked up inside this block.
							// So we simply skip this attempt and continue adjusting the other maps
							// To avoid this one should always keep individual keys/value backups not the
							// entire map
							System.out.println("Attempted to modify source map: " + field.getDeclaringClass() + "#"
									+ field.getName() + e1);
						} catch (final IllegalAccessException e1) {
							throw new IllegalStateException("Failed setting environment variable <" + key + "> to <"
									+ value + ">. Unable to access field!", e1);
						}
					});
		}
		System.out.println(
				"Set environment variable <" + key + "> to <" + value + ">. Sanity Check: " + System.getenv(key));
	}
	public void initEnv(final IScope scope) {
		final String RPath = GamaPreferences.External.LIB_R.value(scope).getPath(scope).replace("libjri.jnilib", "")
			.replace("libjri.so", "").replace("jri.dll", "");
	
		String rhome=RPath.substring(0,RPath.indexOf("library"));
//		setenv("R_HOME", rhome);
		setenv("R_HOME", "/Library/Frameworks/R.framework/Resources/");
		env = System.getProperty("java.library.path");
		if (!env.contains("jri")) {
			if (System.getProperty("os.name").startsWith("Windows")) {
				System.setProperty("java.library.path", RPath + ";" + env);
			} else {
				System.setProperty("java.library.path", RPath + ":" + env);
			}
			try {
				final java.lang.reflect.Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
				fieldSysPath.setAccessible(true);
				fieldSysPath.set(null, null);

			} catch (final Exception ex) {
				scope.getGui().getConsole().informConsole(ex.getMessage(), null);
				ex.printStackTrace();
			}
			// System.out.println(System.getProperty("java.library.path"));
		}
		System.loadLibrary("jri");

		if (System.getenv("R_HOME") == null) {
			throw GamaRuntimeException.error("The R_HOME environment variable is not set. R cannot be run.", scope);
		}
	}

	public REXP Reval(final IScope scope, final String cmd) {
		try {
			re = Rengine.getMainEngine();
			if (re == null) { return null; }
		} catch (final Exception ex) {
			throw GamaRuntimeException.error("R cannot be found ...", scope);
		}

		return re.eval(cmd);

	}

	public Object dataConvert_R2G(final Object o) {
		REXP x;
		if (o instanceof REXP) {
			x = (REXP) o;
		} else {
			return o;
		}

		if (x.getType() == REXP.XT_ARRAY_STR) {
			final String[] s = x.asStringArray();

			final IList<Object> a = GamaListFactory.create();
			for (final String element : s) {
				a.add(dataConvert_R2G(element));
			}
			return a;
		}

		if (x.getType() == REXP.DOTSXP) {
			final RList s = x.asList();

			final IList<Object> a = GamaListFactory.create();
			for (int i = 0; i < s.keys().length; i++) {
				a.add(dataConvert_R2G(s.at(0)));
			}
			return a;
		}

		if (x.getType() == REXP.XT_ARRAY_BOOL_INT) {
			final int[] s = x.asIntArray();

			final IList<Object> a = GamaListFactory.create();
			for (final int element : s) {
				a.add(element == 0 ? false : true);
			}
			return a;
		}
		if (x.getType() == REXP.XT_ARRAY_DOUBLE) {
			final double[] s = x.asDoubleArray();

			final IList<Object> a = GamaListFactory.create();
			for (final double element : s) {
				a.add(element);
			}
			return a;
		}

		if (x.getType() == REXP.XT_ARRAY_INT) {
			final int[] s = x.asIntArray();

			final IList<Object> a = GamaListFactory.create();
			for (final int element : s) {
				a.add(element);
			}
			return a;
		}

		if (x.getType() == REXP.XT_STR) { return x.getContent(); }
		if (x.getType() == REXP.XT_FACTOR) {
			final RFactor f = x.asFactor();
			final IList<Object> a = GamaListFactory.create();
			for (int i = 0; i < f.size(); i++) {
				a.add(dataConvert_R2G(f.at(i)));
			}
			return a;
		}
		if (x.getType() == REXP.XT_VECTOR) {
			final RVector f = x.asVector();
			final IList<Object> a = GamaListFactory.create();
			for (int i = 0; i < f.size(); i++) {
				a.add(dataConvert_R2G(f.at(i)));
			}
			return a;
		}
		return x;
	}
}
