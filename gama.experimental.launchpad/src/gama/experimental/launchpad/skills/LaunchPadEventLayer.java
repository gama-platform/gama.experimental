/*********************************************************************************************
 *
 * 'EventLayer.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package gama.experimental.launchpad.skills;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import gama.core.common.interfaces.IDisplaySurface;
import gama.core.common.interfaces.IEventLayerDelegate;
import gama.core.common.interfaces.IGraphics;
import gama.core.common.interfaces.ILayer;
import gama.core.metamodel.agent.IAgent;
import gama.core.outputs.layers.EventLayerStatement;
import gama.core.outputs.layers.ILayerData;
import gama.core.outputs.layers.ILayerStatement;
import gama.core.outputs.layers.LayerData;
import gama.core.runtime.IScope;
import gama.core.runtime.IScope.IGraphicsScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.gaml.statements.IExecutable;
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
	public static final Set<String> EVENTS =
			new HashSet<>(Arrays.asList("pad_down")); 

	public static HashMap<String, Color> colorMap = new HashMap<String, Color>() {{
	    put("black",Color.BLACK);
	    put("red",Color.RED);
	    put("darkred",Color.DARKRED);
	    put("green",Color.GREEN);
	    put("darkgreen",Color.DARKGREEN);
	    put("orange",Color.ORANGE);
	    put("brown",Color.BROWN);
	    put("yellow",Color.YELLOW);
	    put("lightyellow",Color.LIGHTYELLOW);
	}};
	
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
		if (executer == null) {
			return;
		}
		executionScope.execute(executer, agent, null);
		// GAMA.getExperiment().refreshAllOutputs();
	}

	public static Pad pressedPad;
	public static Button pressedButton;
    public class MyLPListener extends LaunchpadListenerAdapter {

        private final LaunchpadClient client;

        public MyLPListener(LaunchpadClient client) {
            this.client = client;
        }

        @Override
        public void onPadPressed(Pad pad, long timestamp) {
            client.setPadLight(pad, Color.YELLOW, BackBufferOperation.NONE);
            pressedPad=pad;
        }

        @Override
        public void onPadReleased(Pad pad, long timestamp) {
            client.setPadLight(pad, Color.BLACK, BackBufferOperation.NONE);
            pressedPad=pad;
            executeEvent();
        }
        
        @Override
        public void onButtonPressed(Button button, long timestamp) {
            //client.setButtonLight(button, Color.YELLOW, BackBufferOperation.NONE);
            pressedButton=button;
        }
        @Override
        public void onButtonReleased(Button button, long timestamp) {
            //client.setButtonLight(button, Color.BLACK, BackBufferOperation.NONE);
            pressedButton=button;
        }
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
	public void draw(final IGraphicsScope scope, final IGraphics g) throws GamaRuntimeException {
		if (!g.is2D() && !getData().isDynamic() && hasBeenDrawnOnce) { return; }
		if (g.isNotReadyToUpdate() && hasBeenDrawnOnce) { return; }
		getData().compute(scope, g);
		g.setAlpha(getData().getTransparency(scope));
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
	public boolean acceptSource(final IScope scope, final Object source) {
		// TODO Auto-generated method stub
		if(source.equals("launchpad")) {
			return true;
		}
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

	@Override
	public Set<String> getEvents() {
		// TODO Auto-generated method stub
		return EVENTS;
	}

}
