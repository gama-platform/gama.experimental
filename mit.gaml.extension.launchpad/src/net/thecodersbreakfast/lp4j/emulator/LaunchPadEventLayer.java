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

import javax.sound.midi.MidiUnavailableException;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IEventLayerDelegate;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.outputs.layers.AbstractLayer;
import msi.gama.outputs.layers.EventLayerStatement;
import msi.gama.outputs.layers.IDisplayLayerBox;
import msi.gama.outputs.layers.IEventLayerListener;
import msi.gama.outputs.layers.ILayerStatement;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.CreateStatement;
import msi.gaml.statements.IExecutable;
import msi.gaml.types.IType;
import net.thecodersbreakfast.lp4j.api.BackBufferOperation;
import net.thecodersbreakfast.lp4j.api.Button;
import net.thecodersbreakfast.lp4j.api.Color;
import net.thecodersbreakfast.lp4j.api.Launchpad;
import net.thecodersbreakfast.lp4j.api.LaunchpadClient;
import net.thecodersbreakfast.lp4j.api.LaunchpadListener;
import net.thecodersbreakfast.lp4j.emulator.Example.MyListener;
import net.thecodersbreakfast.lp4j.midi.MidiDeviceConfiguration;
import net.thecodersbreakfast.lp4j.midi.MidiLaunchpad;

/**
 * Written by marilleau
 */

public class LaunchPadEventLayer extends AbstractLayer implements IEventLayerDelegate  {

	@Override
	protected void setPositionAndSize(final IDisplayLayerBox box, final IGraphics g) {
		super.setPositionAndSize(box, g);
	}

//	EventListener listener;
	MyListener myListener;
	IScope executionScope;
	
	public LaunchPadEventLayer() {		
	}
	
	public LaunchPadEventLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	public void enableOn(final IDisplaySurface surface) {
//		surface.addListener(listener);
	}

	@Override
	public void disableOn(final IDisplaySurface surface) {
		super.disableOn(surface);
//		surface.removeListener(listener);
	}

    private static CountDownLatch stop = new CountDownLatch(1);
    
    
    public static boolean launch=false;

	Launchpad launchpad ;
	LaunchpadClient client;
	@Override
	public void firstLaunchOn(final IDisplaySurface surface) {
		super.firstLaunchOn(surface);
		launch=true;
//		final IExpression eventType = definition.getFacet(IKeyword.NAME);
//		final IExpression actionName = definition.getFacet(IKeyword.ACTION);
//		executionScope = surface.getScope().copy("of EventLayer");
		try {
			launchpad = new MidiLaunchpad(MidiDeviceConfiguration.autodetect());
			client = launchpad.getClient();
			myListener = new MyListener(client);
			launchpad.setListener((LaunchpadListener) myListener);

			client.reset();
			client.setButtonLight(Button.STOP, Color.RED, BackBufferOperation.NONE);

//	        stop.await();
//	        client.reset();
//	        launchpad.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// Evaluated in the display surface scope to gather variables defined in
		// there
//		final String currentEvent = Cast.asString(surface.getScope(), eventType.value(surface.getScope()));
//		final String currentAction = Cast.asString(surface.getScope(), actionName.value(surface.getScope()));

//		listener = new EventListener(surface, currentEvent, currentAction);
//		surface.addListener(listener);
	}

	@Override
	public void dispose() {
		super.dispose();
//		listener.dispose();

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

	// We explicitly translate by the origin of the surface
	@Override
	public ILocation getModelCoordinatesFrom(final int xOnScreen, final int yOnScreen, final IDisplaySurface g) {
		if (xOnScreen == -1 && yOnScreen == -1)
			return new GamaPoint(0, 0);
		return g.getModelCoordinates();
	}

	// AD: Fix for Issue #1511
	@Override
	public boolean containsScreenPoint(final int x, final int y) {
		return false;
	}

	private class EventListener implements IEventLayerListener {

		private final static int MOUSE_PRESS = 0;
		private final static int MOUSE_RELEASED = 1;
		private final static int MOUSE_CLICKED = 2;
		private final static int MOUSE_MOVED = 4;
		private final static int MOUSE_ENTERED = 5;
		private final static int MOUSE_EXITED = 6;
		private final static int KEY_PRESSED = 3;

		private final int listenedEvent;
		private final IDisplaySurface surface;
		private final String event, actionName;

		public EventListener(final IDisplaySurface display, final String event, final String action) {
			actionName = action;
			this.event = event;
			listenedEvent = getListeningEvent(event);
			surface = display;
		}

		public void dispose() {
			surface.removeListener(this);
		}

		public int getListeningEvent(final String eventTypeName) {
			if (eventTypeName.equals(IKeyword.MOUSE_DOWN)) {
				return MOUSE_PRESS;
			}
			if (eventTypeName.equals(IKeyword.MOUSE_UP)) {
				return MOUSE_RELEASED;
			}
			if (eventTypeName.equals(IKeyword.MOUSE_CLICKED)) {
				return MOUSE_CLICKED;
			}
			if (eventTypeName.equals(IKeyword.MOUSE_MOVED)) {
				return MOUSE_MOVED;
			}
			if (eventTypeName.equals(IKeyword.MOUSE_ENTERED)) {
				return MOUSE_ENTERED;
			}
			if (eventTypeName.equals(IKeyword.MOUSE_EXITED)) {
				return MOUSE_EXITED;
			}
			return KEY_PRESSED;
		}

		@Override
		public void mouseClicked(final int x, final int y, final int button) {
			if (MOUSE_CLICKED == listenedEvent && button == 1) {
				executeEvent(x, y);
			}
		}

		@Override
		public void mouseDown(final int x, final int y, final int button) {
			if (MOUSE_PRESS == listenedEvent && button == 1) {
				executeEvent(x, y);
			}
		}

		@Override
		public void mouseUp(final int x, final int y, final int button) {
			if (MOUSE_RELEASED == listenedEvent && button == 1) {
				executeEvent(x, y);
			}
		}

		@Override
		public void mouseMove(final int x, final int y) {
			if (MOUSE_MOVED == listenedEvent) {
				executeEvent(x, y);
			}
		}

		@Override
		public void mouseEnter(final int x, final int y) {
			if (MOUSE_ENTERED == listenedEvent) {
				executeEvent(x, y);
			}
		}

		@Override
		public void mouseExit(final int x, final int y) {
			if (MOUSE_EXITED == listenedEvent) {
				executeEvent(x, y);
			}
		}

		private void executeEvent(final int x, final int y) {
			final IAgent agent = ((EventLayerStatement) definition).executesInSimulation()
					? executionScope.getSimulation() : executionScope.getExperiment();
			final IExecutable executer = agent == null ? null : agent.getSpecies().getAction(actionName);
			if (executer == null) {
				return;
			}
			final ILocation pp = getModelCoordinatesFrom(x, y, surface);
			if (pp == null) {
				return;
			}
			if (pp.getX() < 0 || pp.getY() < 0 || pp.getX() >= surface.getEnvWidth()
					|| pp.getY() >= surface.getEnvHeight()) {
				if (MOUSE_EXITED != listenedEvent) {
					return;
				}
			}
			GAMA.runAndUpdateAll(() -> executionScope.execute(executer, agent, null));

		}

		/**
		 * Method keyPressed()
		 * 
		 * @see msi.gama.outputs.layers.IEventLayerListener#keyPressed(java.lang.Character)
		 */
		@Override
		public void keyPressed(final String c) {
			if (c.equals(event)) {
				executeEvent(-1, -1);
			}
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
	public boolean createFrom(IScope scope, List<Map<String, Object>> inits, Integer max, Object source, Arguments init,
			EventLayerStatement statement) {
		// TODO Auto-generated method stub
//		System.out.println("LAUNCHPAD event layer delegate");
		if(!launch) {
			firstLaunchOn(null);
		}
		return false;
	}

	@Override
	public IType<?> fromFacetType() {
		// TODO Auto-generated method stub
		return null;
	}

}
