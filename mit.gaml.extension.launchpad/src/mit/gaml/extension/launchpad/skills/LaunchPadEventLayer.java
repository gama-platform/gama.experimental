/*********************************************************************************************
 *
 * 'EventLayer.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package mit.gaml.extension.launchpad.skills;

import java.io.IOException;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IEventLayerDelegate;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.common.interfaces.ILayer;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.outputs.layers.AbstractLayer;
import msi.gama.outputs.layers.EventLayerStatement;
import msi.gama.outputs.layers.ILayerData;
import msi.gama.outputs.layers.ILayerStatement;
import msi.gama.outputs.layers.LayerData;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.descriptions.StatementDescription;
import msi.gaml.statements.IExecutable;
import net.thecodersbreakfast.lp4j.api.BackBufferOperation;
import net.thecodersbreakfast.lp4j.api.Button;
import net.thecodersbreakfast.lp4j.api.Color;
import net.thecodersbreakfast.lp4j.api.Launchpad;
import net.thecodersbreakfast.lp4j.api.LaunchpadClient;
import net.thecodersbreakfast.lp4j.api.LaunchpadListenerAdapter;
import net.thecodersbreakfast.lp4j.api.Pad;
import net.thecodersbreakfast.lp4j.midi.MidiDeviceConfiguration;
import net.thecodersbreakfast.lp4j.midi.MidiLaunchpad;

/**
 * Written by Arnaud Grignard & Huynh Quang Nghi (CC2017)
 */

public class LaunchPadEventLayer implements ILayer, IEventLayerDelegate {

	MyLPListener myListener=null;
	IScope executionScope=null;
	
	public LaunchPadEventLayer() { 
		definition=null;
	}

	protected ILayerStatement definition;
	private String name;
	boolean hasBeenDrawnOnce=false;
	private ILayerData data=null;

	public LaunchPadEventLayer(final ILayerStatement layer) {
		definition = layer;
		if (definition != null) {
			setName(definition.getName());
		}
		data = createData();
	}

	@Override
	public ILayerStatement getDefinition() {
		return definition;
	}

	@Override
	public ILayerData getData() {
		return data;
	}

	protected ILayerData createData() {
		return new LayerData(definition);
	}

	@Override
	public void forceRedrawingOnce() {
		hasBeenDrawnOnce = false;
	}

	@Override
	public void draw(final IScope scope, final IGraphics g) throws GamaRuntimeException {
		if (!g.is2D() && !getData().isDynamic() && hasBeenDrawnOnce) { return; }
		if (g.isNotReadyToUpdate() && hasBeenDrawnOnce) { return; }
		getData().compute(scope, g);
		g.setOpacity(getData().getTransparency(scope));
		g.beginDrawingLayer(this); 
		g.endDrawingLayer(this);
		hasBeenDrawnOnce = true;
	}
 

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final void setName(final String name) {
		this.name = name;
	} 

	@Override
	public void disableOn(final IDisplaySurface surface) { 
	}

	public static boolean launch = false;
	Launchpad launchpad;
	public static LaunchpadClient client;

	@Override
	public void firstLaunchOn(final IDisplaySurface surface) { 
		launch = true;
		try {
			System.out.println("LaunchPad launched");
			launchpad = new MidiLaunchpad(MidiDeviceConfiguration.autodetect());
			client = launchpad.getClient();
			myListener = new MyLPListener(client);
			launchpad.setListener(myListener);
			client.reset();
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() { 
		try {
			client.reset();
			launchpad.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getType() {
		return "Event layer";
	}

	private void executeEvent() {
		final IAgent agent = ((EventLayerStatement) definition).executesInSimulation() ? executionScope.getSimulation()
				: executionScope.getExperiment();
		final String actionName = ((EventLayerStatement) definition).getFacet("action").toString();
		final IExecutable executer = agent == null ? null : agent.getSpecies().getAction(actionName);
		if (executer == null) { return; }
		executionScope.execute(executer, agent, null);
		// GAMA.getExperiment().refreshAllOutputs();
	}

	public static Pad pressedPad;
	public static Button pressedButton;

	public class MyLPListener extends LaunchpadListenerAdapter {

		private final LaunchpadClient client;

		public MyLPListener(final LaunchpadClient client) {
			this.client = client;
		}

		@Override
		public void onPadPressed(final Pad pad, final long timestamp) {
			client.setPadLight(pad, Color.YELLOW, BackBufferOperation.NONE);
			pressedPad = pad;
		}

		@Override
		public void onPadReleased(final Pad pad, final long timestamp) {
			client.setPadLight(pad, Color.BLACK, BackBufferOperation.NONE);
			pressedPad = pad;
			executeEvent();
		}

		@Override
		public void onButtonPressed(final Button button, final long timestamp) {
			client.setButtonLight(button, Color.YELLOW, BackBufferOperation.NONE);
			pressedButton = button;
		}

		@Override
		public void onButtonReleased(final Button button, final long timestamp) {
			client.setButtonLight(button, Color.BLACK, BackBufferOperation.NONE);
			pressedButton = button;
		}
	}
  

	@Override
	public boolean acceptSource(final IScope scope, final Object source) {
		// TODO Auto-generated method stub
		if (source.equals("launchpad")) { return true; }
		return false;
	}

	@Override
	public boolean createFrom(final IScope scope, final Object source, final EventLayerStatement statement) {
		// TODO Auto-generated method stub
		executionScope = scope;
		System.out.println("LAUNCHPAD event layer delegate " + statement.getFacetValue(scope, "action"));
		if (!launch) {
			definition = statement;
			firstLaunchOn(null);
		}
		return false;
	}

}
