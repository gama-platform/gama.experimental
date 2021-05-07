/*******************************************************************************************************
*
* LoggerSkill.java, in plugin irit.gama.switchproject,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skills.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jfree.data.json.impl.JSONArray;
import org.jfree.data.json.impl.JSONObject;

import irit.gama.common.interfaces.IKeywordIrit;
import msi.gama.common.util.FileUtils;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;

/**
 * Logger skill, add the capability to log data and write it into files
 * 
 * @author Jean-François Erdelyi
 */
@SuppressWarnings("unchecked")
@vars({ @variable(name = IKeywordIrit.LOG_DATA, type = IType.NONE, doc = { @doc("Logbook data") }), })
@skill(name = IKeywordIrit.LOGGING, concept = { IKeywordIrit.LOGGING, IConcept.SKILL }, internal = true)
public class LoggingSkill extends Skill {

	// ############################################
	// Getter and setter

	@getter(IKeywordIrit.LOG_DATA)
	public Object getLogData(final IAgent agent) {
		if (agent == null) {
			return null;
		}
		return (JSONObject) agent.getAttribute(IKeywordIrit.LOG_DATA);
	}

	// ############################################
	// Actions

	@action(name = "log_plot_1d", args = {
			@arg(name = IKeywordIrit.SECTION, type = IType.STRING, optional = false, doc = @doc("The name of the section")),
			@arg(name = IKeywordIrit.ENTRY, type = IType.STRING, optional = false, doc = @doc("The name of the entry")),
			@arg(name = IKeywordIrit.X, type = IType.STRING, optional = false, doc = @doc("X data")) }, doc = @doc(examples = {
					@example("do log_plot_2d section: name data: \"Mean speed\" x: my_date") }, value = "Write new line in logbook."))
	public Object logPlot1d(final IScope scope) throws GamaRuntimeException {
		// Get data
		String sectionName = (String) scope.getArg(IKeywordIrit.SECTION, IType.STRING);
		String entryName = (String) scope.getArg(IKeywordIrit.ENTRY, IType.STRING);
		String x = (String) scope.getArg(IKeywordIrit.X, IType.STRING);

		// Get date
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		Date date = new Date(System.currentTimeMillis());

		// Put data
		JSONObject data = new JSONObject();
		data.put("name", entryName);
		data.put("type", "1d");
		data.put("date", formatter.format(date));
		data.put("x", x);

		// Write log
		return log(scope, sectionName, data);
	}

	@action(name = "log_plot_2d", args = {
			@arg(name = IKeywordIrit.SECTION, type = IType.STRING, optional = false, doc = @doc("The name of the section")),
			@arg(name = IKeywordIrit.ENTRY, type = IType.STRING, optional = false, doc = @doc("The name of the entry")),
			@arg(name = IKeywordIrit.X, type = IType.STRING, optional = false, doc = @doc("X data")),
			@arg(name = IKeywordIrit.Y, type = IType.STRING, optional = false, doc = @doc("Y data")) }, doc = @doc(examples = {
					@example("do log_plot_2d section: name data: \"Mean speed\" x: my_date y: 30.2") }, value = "Write new line in logbook."))
	public Object logPlot2d(final IScope scope) throws GamaRuntimeException {
		// Get data
		String sectionName = (String) scope.getArg(IKeywordIrit.SECTION, IType.STRING);
		String entryName = (String) scope.getArg(IKeywordIrit.ENTRY, IType.STRING);
		String x = (String) scope.getArg(IKeywordIrit.X, IType.STRING);
		String y = (String) scope.getArg(IKeywordIrit.Y, IType.STRING);

		// Get date
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		Date date = new Date(System.currentTimeMillis());

		// Put data
		JSONObject data = new JSONObject();
		data.put("name", entryName);
		data.put("type", "2d");
		data.put("date", formatter.format(date));
		data.put("x", x);
		data.put("y", y);

		// Write log
		return log(scope, sectionName, data);
	}

	@action(name = "write", args = {
			@arg(name = IKeywordIrit.FILE_NAME, type = IType.STRING, optional = false, doc = @doc("File name")),
			@arg(name = IKeywordIrit.FLUSH, type = IType.BOOL, optional = true, doc = @doc("Flush data if true")) }, doc = @doc(examples = {
					@example("do write file_name: \"log.txt\" flush: true;") }, value = "Write data in file."))
	public Object write(final IScope scope) throws GamaRuntimeException {
		// Get data from the scope
		String fileName = (String) scope.getArg(IKeywordIrit.FILE_NAME, IType.STRING);
		Boolean flush = (Boolean) scope.getArg(IKeywordIrit.FLUSH, IType.BOOL);
		FileWriter fw = null;
		IAgent agent = scope.getAgent();
		JSONObject jsonData = (JSONObject) agent.getAttribute(IKeywordIrit.LOG_DATA);

		// Not ok
		if (jsonData == null) {
			return false;
		}

		// Write data
		String path = FileUtils.constructAbsoluteFilePath(scope, fileName, false);
		try {
			fw = new FileWriter(path);
			fw.write(jsonData.toJSONString());
		} catch (IOException e) {
			throw GamaRuntimeException.error(e.getMessage(), scope);
		} finally {
			try {
				if (fw != null) {
					fw.flush();
					fw.close();
				}
			} catch (IOException e) {
				throw GamaRuntimeException.error(e.getMessage(), scope);
			}
		}

		// If "flush" then clear data
		if (flush) {
			jsonData.clear();
		}

		// Ok
		return true;
	}

	@action(name = "flush", doc = @doc(examples = { @example("do flush;") }, value = "Flush data."))
	public Object flush(final IScope scope) throws GamaRuntimeException {
		IAgent agent = scope.getAgent();
		JSONObject jsonData = (JSONObject) agent.getAttribute(IKeywordIrit.LOG_DATA);

		// Clear data
		jsonData.clear();

		// Always true
		return true;
	}

	// ############################################
	// Internal behavior

	public Object log(final IScope scope, final String dataName, final JSONObject data) throws GamaRuntimeException {
		IAgent agent = scope.getAgent();
		JSONObject jsonData = (JSONObject) agent.getAttribute(IKeywordIrit.LOG_DATA);
		if (jsonData == null) {
			jsonData = new JSONObject();
			agent.setAttribute(IKeywordIrit.LOG_DATA, jsonData);
		}

		// Get map
		JSONArray values = (JSONArray) jsonData.get(dataName);

		// If map is null then create a new one
		if (values == null) {
			values = new JSONArray();
			jsonData.put(dataName, values);
		}

		return values.add(data);
	}

}
