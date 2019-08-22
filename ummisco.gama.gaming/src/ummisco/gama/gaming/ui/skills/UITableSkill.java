package ummisco.gama.gaming.ui.skills;

import java.util.ArrayList;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
import msi.gaml.types.IType;

@vars ({ @variable (
		name = IUILocatedSkill.AGENT_LOCATION,
		type = IType.POINT,
		doc = @doc ("locked location")),
		@variable (
				name = IUILocatedSkill.AGENT_LOCKED_WIDTH,
				type = IType.FLOAT,
				doc = @doc ("locked width")),
		@variable (
				name = IUILocatedSkill.AGENT_LOCKED_HEIGHT,
				type = IType.FLOAT,
				doc = @doc ("locked height")),
		@variable (
				name = IUILocatedSkill.AGENT_UI_WIDTH,
				type = IType.FLOAT,
				doc = @doc ("resized width")),
		@variable (
				name = IUILocatedSkill.AGENT_UI_HEIGHT,
				type = IType.FLOAT,
				doc = @doc ("resized height")),
		@variable (
				name = IUITableSkill.NUMBER_OF_LINES,
				type = IType.INT,
				doc = @doc ("number of displayed lines")),
		@variable (
				name = IUITableSkill.NUMBER_OF_COLUMNS,
				type = IType.INT,
				doc = @doc ("number of columns")),
		@variable (
				name = IUITableSkill.MATRIX_DATA,
				type = IType.CONTAINER,
				doc = @doc ("resized height")),
		@variable (
				name = IUILocatedSkill.AGENT_DISPLAY,
				type = IType.STRING,
				doc = @doc ("map of location")) })

@skill (
		name = IUITableSkill.SKILL_NAME,
		concept = { IConcept.GUI, IConcept.COMMUNICATION, IConcept.SKILL })
public class UITableSkill extends UILocatedSkill {

	@Override
	@action (
			name = IUILocatedSkill.UI_AGENT_LOCATION_SET,
			args = { @arg (
					name = IUILocatedSkill.UI_AGENT_LOCATION,
					type = IType.POINT,
					optional = false,
					doc = @doc ("name of the display")),
					@arg (
							name = IUILocatedSkill.UI_NAME,
							type = IType.STRING,
							optional = false,
							doc = @doc ("name of the display")),
					@arg (
							name = IUITableSkill.NUMBER_OF_LINES,
							type = IType.INT,
							optional = false,
							doc = @doc ("number of displayed lines")),
					@arg (
							name = IUITableSkill.NUMBER_OF_COLUMNS,
							type = IType.INT,
							optional = false,
							doc = @doc ("number of columns")),
					@arg (
							name = IUILocatedSkill.UI_HEIGHT,
							type = IType.FLOAT,
							optional = false,
							doc = @doc ("width of the object in %")),
					@arg (
							name = IUILocatedSkill.UI_WIDTH,
							type = IType.FLOAT,
							optional = false,
							doc = @doc ("height of the object in %")) },
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void setAgentLocationInUI(final IScope scope) {
		super.setAgentLocationInUI(scope);
		final IAgent agt = scope.getAgent();
		final int numberOfColumn = ((Integer) scope.getArg(IUITableSkill.NUMBER_OF_COLUMNS, IType.INT)).intValue();
		final int cellHeight = ((Integer) scope.getArg(IUITableSkill.NUMBER_OF_LINES, IType.INT)).intValue();
		agt.setAttribute(IUITableSkill.NUMBER_OF_LINES, cellHeight);
		agt.setAttribute(IUITableSkill.NUMBER_OF_COLUMNS, numberOfColumn);
		final ArrayList<IAgent[]> data = new ArrayList<>();
		agt.setAttribute(IUITableSkill.MATRIX_DATA, data);
	}

	@action (
			name = IUITableSkill.ADD_LINE,
			args = { @arg (
					name = IUITableSkill.ELEMENTS_TO_ADD,
					type = IType.CONTAINER,
					optional = false,
					doc = @doc ("list of agent to display")) },
			doc = @doc (
					value = "",
					returns = "",
					examples = { @example ("") }))
	public void addLine(final IScope scope) {
		final IAgent agt = scope.getAgent();
		@SuppressWarnings ("unchecked") final IList<IAgent> elements =
				(IList<IAgent>) scope.getArg(IUITableSkill.ELEMENTS_TO_ADD, IType.CONTAINER);
		@SuppressWarnings ("unchecked") final ArrayList<IAgent[]> data =
				(ArrayList<IAgent[]>) agt.getAttribute(IUITableSkill.MATRIX_DATA);
		final float ui_width = (float) agt.getAttribute(IUILocatedSkill.AGENT_LOCKED_WIDTH);
		final float ui_height = (float) agt.getAttribute(IUILocatedSkill.AGENT_LOCKED_HEIGHT);
		final int nbColumns = (int) agt.getAttribute(IUITableSkill.NUMBER_OF_COLUMNS);
		final int nbLines = (int) agt.getAttribute(IUITableSkill.NUMBER_OF_LINES);
		final String display = (String) agt.getAttribute(IUILocatedSkill.AGENT_DISPLAY);

		final IAgent[] nLine = new IAgent[elements.size()];
		int j = 0;
		for (final IAgent el : elements) {
			nLine[j] = el;
			j++;
		}
		final int i = data.size();
		data.add(nLine);

		j = 0;
		for (final IAgent el : elements) {
			final GamaPoint p = getCellCoordinate(agt, i, j);
			el.setAttribute(IUILocatedSkill.AGENT_LOCATION, p);
			el.setAttribute(IUILocatedSkill.AGENT_LOCKED_WIDTH, ui_width / nbColumns);
			el.setAttribute(IUILocatedSkill.AGENT_LOCKED_HEIGHT, ui_height / nbLines);
			el.setAttribute(IUILocatedSkill.AGENT_DISPLAY, display);
			el.setAttribute(IUITableCell.I_COODINATE, i);
			el.setAttribute(IUITableCell.J_COODINATE, j);
			j++;
		}
	}

	public GamaPoint getCellCoordinate(final IAgent agt, final int i, final int j) {
		final GamaPoint pt = (GamaPoint) agt.getAttribute(IUILocatedSkill.AGENT_LOCATION);
		final float ui_width = (float) agt.getAttribute(IUILocatedSkill.AGENT_LOCKED_WIDTH);
		final float ui_height = (float) agt.getAttribute(IUILocatedSkill.AGENT_LOCKED_HEIGHT);
		final int nbColumns = ((Integer) agt.getAttribute(IUITableSkill.NUMBER_OF_COLUMNS)).intValue();
		final int nbLines = ((Integer) agt.getAttribute(IUITableSkill.NUMBER_OF_LINES)).intValue();
		final double x = pt.x + j * (ui_width / nbColumns);
		final double y = pt.y + i * (ui_height / nbLines);
		final GamaPoint res = new GamaPoint(x, y, 0);
		return res;
	}

	@Override
	public void initialize(final IScope scope) {
		super.initialize(scope);
	}

}
