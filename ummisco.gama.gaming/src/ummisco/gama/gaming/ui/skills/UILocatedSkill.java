package ummisco.gama.gaming.ui.skills;

import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;

import java.util.ArrayList;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.outputs.IOutput;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.var;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;

import com.vividsolutions.jts.geom.Envelope;

@vars({ @var(name = IUILocatedSkill.AGENT_LOCATION, type = IType.POINT, doc = @doc("locked location")),
	@var(name = IUILocatedSkill.AGENT_LOCKED_WIDTH, type = IType.FLOAT, doc = @doc("locked width")),
	@var(name = IUILocatedSkill.AGENT_LOCKED_HEIGHT, type = IType.FLOAT, doc = @doc("locked height")),
	@var(name = IUILocatedSkill.AGENT_UI_WIDTH, type = IType.FLOAT, doc = @doc("locked height")),
	@var(name = IUILocatedSkill.AGENT_UI_HEIGHT, type = IType.POINT, doc = @doc("locked height")),
	@var(name = IUILocatedSkill.AGENT_DISPLAY, type = IType.STRING, doc = @doc("map of location"))})

@skill(name=IUILocatedSkill.SKILL_NAME, concept = { IConcept.GUI, IConcept.COMMUNICATION, IConcept.SKILL })
public class UILocatedSkill extends Skill {
	private ArrayList<IAgent> followedAgent = null;
	
	
	@action(name = IUILocatedSkill.UI_AGENT_LOCATION, args = {
			@arg(name = IUILocatedSkill.UI_NAME, type = IType.STRING, optional = true, doc = @doc("name of the display"))},
			doc = @doc(value = "", returns = "", examples = { @example("") }))
	public GamaPoint getUILocation(IScope scope)
	{
		if(this.followedAgent == null)
			this.initialize(scope);
		IAgent agt = scope.getAgent();
		String outputName = (String) scope.getArg(IUILocatedSkill.UI_NAME, IType.STRING);
		IOutput out = scope.getSimulation().getOutputManager().getOutputWithName(outputName);
		//output.get
		System.out.println("output " + out.getClass().getCanonicalName());
		if(!(out instanceof LayeredDisplayOutput))
			return null;
		LayeredDisplayOutput output = (LayeredDisplayOutput)out;
		
		
		
		//output.getSurface().getDisplayWidth()
		//output.getSurface().getModelCoordinatesFrom(xOnScreen, yOnScreen, sizeInPixels, positionInPixels)
		
		return null;
	}
	
	@action(name=IUILocatedSkill.UI_AGENT_LOCATION_SET,args={
			@arg(name = IUILocatedSkill.UI_AGENT_LOCATION, type = IType.POINT, optional = false, doc = @doc("name of the display")),
			@arg(name = IUILocatedSkill.UI_NAME, type = IType.STRING, optional = false, doc = @doc("name of the display")),
			@arg(name = IUILocatedSkill.UI_HEIGHT, type = IType.FLOAT, optional = false, doc = @doc("width of the object in %")),
			@arg(name = IUILocatedSkill.UI_WIDTH, type = IType.FLOAT, optional = false, doc = @doc("height of the object in %"))},
			doc = @doc(value = "", returns = "", examples = { @example("")}))
	public void setAgentLocationInUI(IScope scope)
	{
		if(this.followedAgent == null)
			this.initialize(scope);
		IAgent agt = scope.getAgent();
		String outputName = (String) scope.getArg(IUILocatedSkill.UI_NAME, IType.STRING);
		GamaPoint pt = (GamaPoint) scope.getArg(IUILocatedSkill.UI_AGENT_LOCATION, IType.POINT);
		float wd = ((Double)scope.getArg(IUILocatedSkill.UI_WIDTH, IType.FLOAT)).floatValue();
		float hg = ((Double)scope.getArg(IUILocatedSkill.UI_HEIGHT, IType.FLOAT)).floatValue();
		this.followedAgent.add(agt);
		agt.setAttribute(IUILocatedSkill.AGENT_DISPLAY, outputName);
		agt.setAttribute(IUILocatedSkill.AGENT_LOCATION, pt);
		agt.setAttribute(IUILocatedSkill.AGENT_LOCKED_WIDTH, wd);
		agt.setAttribute(IUILocatedSkill.AGENT_LOCKED_HEIGHT, hg);
		moveAgentUI(agt);
				
	}
	
	
	public void lockAgent(IAgent agt)
	{
		this.followedAgent.add(agt);
	}
	public void unlockAgent(IAgent agt)
	{
		this.followedAgent.remove(agt);
	}
	
	private void moveAgentUI(IAgent agt)
	{
		IScope scope = agt.getScope();
		String outputName = (String) agt.getAttribute(IUILocatedSkill.AGENT_DISPLAY);
		GamaPoint pt = (GamaPoint) agt.getAttribute(IUILocatedSkill.AGENT_LOCATION);
		float ui_width = (float) agt.getAttribute(IUILocatedSkill.AGENT_LOCKED_WIDTH);
		float ui_height = (float) agt.getAttribute(IUILocatedSkill.AGENT_LOCKED_HEIGHT);
		IOutput out = scope.getSimulation().getOutputManager().getOutputWithName(outputName);
		if(!(out instanceof LayeredDisplayOutput))
			return;
		LayeredDisplayOutput output = (LayeredDisplayOutput)out;
		if(output.getSurface() == null)
			return;
		Envelope e = output.getSurface().getVisibleRegionForLayer(output.getSurface().getManager().getItems().get(0));
		double xmin=Math.max(0,e.getMinX());
		double ymin=Math.max(0,e.getMinY());
		double xmax = Math.min(e.getMaxX(),output.getSurface().getEnvWidth());
		double ymax=Math.min(e.getMaxY(),output.getSurface().getEnvHeight());
		double xx = xmin + pt.x*(xmax - xmin) ;
		double yy = ymin + pt.y*(ymax - ymin);
		
		
		
		float tui_width = (float)((xmax - xmin) * ui_width);
		float tui_height = (float) ((ymax - ymin) * ui_height);
		ILocation loc = new GamaPoint(xx,yy);
		agt.setAttribute(IUILocatedSkill.AGENT_UI_WIDTH, tui_width);
		agt.setAttribute(IUILocatedSkill.AGENT_UI_HEIGHT, tui_height);
		agt.setLocation(loc);
	}

	private void moveAllAgent()
	{
		for(IAgent a:followedAgent)
		{
			moveAgentUI(a);
		}
	}
	
	private void removeLockedAgent(IScope scope)
	{
		this.followedAgent.clear();
	}
	
	private void initialize(final IScope scope)
	{
		this.followedAgent = new ArrayList<>();
		registerSimulationEvent(scope);
	}
	
	private void registerSimulationEvent(final IScope scope) {
		scope.getSimulation().postEndAction(scope1 -> { moveAllAgent();
			return null;
		});

		scope.getSimulation().postDisposeAction(scope1 -> {
			removeLockedAgent(scope1);
			return null;
		});
	}

}
