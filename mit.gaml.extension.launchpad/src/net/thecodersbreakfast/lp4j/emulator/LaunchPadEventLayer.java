/*********************************************************************************************
 *
 * 'EventLayer.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package net.thecodersbreakfast.lp4j.emulator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IEventLayerDelegate;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.outputs.layers.AbstractLayer;
import msi.gama.outputs.layers.EventLayerStatement;
import msi.gama.outputs.layers.IDisplayLayerBox;
import msi.gama.outputs.layers.ILayerStatement;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.IExecutable;
import msi.gaml.types.IType;
import net.thecodersbreakfast.lp4j.api.BackBufferOperation;
import net.thecodersbreakfast.lp4j.api.Button;
import net.thecodersbreakfast.lp4j.api.Color;
import net.thecodersbreakfast.lp4j.api.Launchpad;
import net.thecodersbreakfast.lp4j.api.LaunchpadClient;
import net.thecodersbreakfast.lp4j.api.LaunchpadListener;
import net.thecodersbreakfast.lp4j.api.LaunchpadListenerAdapter;
import net.thecodersbreakfast.lp4j.api.Pad;
import net.thecodersbreakfast.lp4j.midi.MidiDeviceConfiguration;
import net.thecodersbreakfast.lp4j.midi.MidiLaunchpad;

/**
 * Written by Arnaud Grignard & Huynh Quang Nghi (CC2017)
 */

public class LaunchPadEventLayer extends AbstractLayer implements IEventLayerDelegate  {

	@Override
	protected void setPositionAndSize(final IDisplayLayerBox box, final IGraphics g) {
		super.setPositionAndSize(box, g);
	}

	MyLPListener myListener;
	IScope executionScope;
	
	public LaunchPadEventLayer() {		
	}
	
	public LaunchPadEventLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	public void disableOn(final IDisplaySurface surface) {
		super.disableOn(surface);
	}

    public static boolean launch=false;
	Launchpad launchpad ;
	LaunchpadClient client;
	@Override
	public void firstLaunchOn(final IDisplaySurface surface) {
		super.firstLaunchOn(surface);
		launch=true;
		try {
			System.out.println("LaunchPad launched");
			launchpad = new MidiLaunchpad(MidiDeviceConfiguration.autodetect());
			client = launchpad.getClient();
			myListener = new MyLPListener(client);
			launchpad.setListener((LaunchpadListener) myListener);
			client.reset();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
        try {
        	client.reset();
			launchpad.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getType() {
		return "Event layer";
	}


	@operator (
			value = "getPad",
			can_be_const = false,
			category = { IOperatorCategory.USER_CONTROL },
			concept = { IConcept.LAYER })
	@doc (
			value = "get pad pressed pad position",
			examples = @example (
					value = "getPad()",
					test = false))
	public static ILocation getPad(Integer idc) throws GamaRuntimeException {
		 GamaPoint p=new GamaPoint(pressedPad.getX(),pressedPad.getY());
		 return p;
	}
	
	

	@operator (
			value = "getButton",
			can_be_const = false,
			category = { IOperatorCategory.USER_CONTROL },
			concept = { IConcept.LAYER })
	@doc (
			value = "get button pressed name",
			examples = @example (
					value = "getButton()",
					test = false))
	public static String getButton(Integer idx) throws GamaRuntimeException {		
		String name;
		if(pressedButton!=null){
			name =  pressedButton.name();	
		}else{
			name="NULL";
		}
		
		return name;
	}


	private void executeEvent() {
		final IAgent agent = ((EventLayerStatement) definition).executesInSimulation()
				? executionScope.getSimulation() : executionScope.getExperiment();
				String actionName= ((EventLayerStatement) definition).getFacet("action").toString();
		final IExecutable executer = agent == null ? null : agent.getSpecies().getAction(actionName);
		if (executer == null) {
			return;
		}
		executionScope.execute(executer, agent, null);

	}

	static Pad pressedPad;
	static Button pressedButton;
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
            client.setButtonLight(button, Color.YELLOW, BackBufferOperation.NONE);
            pressedButton=button;
        }
        @Override
        public void onButtonReleased(Button button, long timestamp) {
            client.setButtonLight(button, Color.BLACK, BackBufferOperation.NONE);
            pressedButton=button;
        }
    }

	@Override
	protected void privateDrawDisplay(final IScope scope, final IGraphics g) throws GamaRuntimeException {
	}

	@Override
	public void drawDisplay(final IScope scope, final IGraphics g) throws GamaRuntimeException {
		if (definition != null) {
			definition.getBox().compute(scope);
			setPositionAndSize(definition.getBox(), g);
		}
	}

	@Override
	public boolean acceptSource(IScope scope, Object source) {
		// TODO Auto-generated method stub
		if(source.equals("launchpad")) {
			return true;
		}
		return false;
	}

	@Override
	public boolean createFrom(IScope scope, Object source, EventLayerStatement statement) {
		// TODO Auto-generated method stub
		executionScope = scope;
		System.out.println("LAUNCHPAD event layer delegate "+statement.getFacetValue(scope, "action"));
		if(!launch) {			
			definition=statement;
			firstLaunchOn(null);
		}
		return false;
	}

}
