/*********************************************************************************************
 *
 * 'Java2DDisplaySurface.java, in plugin ummisco.gama.java2d, is part of the source code of the GAMA modeling and
 * simulation platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package cict.gama.extensions.websocket;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Collection;

import org.locationtech.jts.geom.Envelope;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.interfaces.ILayer;
import msi.gama.common.interfaces.ILayerManager;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.metamodel.shape.IShape;
import msi.gama.outputs.LayeredDisplayData;
import msi.gama.outputs.LayeredDisplayData.Changes;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.outputs.display.LayerManager;
import msi.gama.outputs.layers.IEventLayerListener;
import msi.gama.precompiler.GamlAnnotations.display;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;
import msi.gaml.expressions.IExpression;
import ummisco.gama.ui.views.displays.DisplaySurfaceMenu;

@display("web")
@doc("Display that uses the websocket technology to draw the layers in a web view")
public class WebDisplaySurface implements IDisplaySurface {

	final LayeredDisplayOutput output;

	private IScope scope;
	protected final ILayerManager layerManager;
	protected IGraphics iGraphics;

	protected DisplaySurfaceMenu menuManager;
	protected IExpression temp_focus;

	public WebDisplaySurface(final Object... args) {
		output = (LayeredDisplayOutput) args[0];
		output.setSurface(this);
		setDisplayScope(output.getScope().copy("in java2D display"));
		output.getData().addListener(this);
		temp_focus = output.getFacet(IKeyword.FOCUS);
		layerManager = new LayerManager(this, output);
		int port = 8887; // 843 flash policy port

		try {
			final ChatServer s = new ChatServer(port);
			s.start();
			System.out.println("ChatServer started on port: " + s.getPort());
			scope.getSimulation().postDisposeAction(scope1 -> {
				try {
					s.stop();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			});
//			BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
//			while (true) {
//				String in = sysin.readLine();
//				s.broadcast(in);
//				if (in.equals("exit")) {
//					s.stop(1000);
//					break;
//				}
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void setDisplayScope(final IScope scope) {
		if (this.scope != null) {
			GAMA.releaseScope(this.scope);
		}
		this.scope = scope;
	}

	@Override
	public void changed(Changes property, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public IScope getScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedImage getImage(int width, int height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateDisplay(boolean force) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMenuManager(Object displaySurfaceMenu) {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoomIn() {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoomOut() {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoomFit() {
		// TODO Auto-generated method stub

	}

	@Override
	public ILayerManager getManager() {
		// TODO Auto-generated method stub
		return layerManager;
	}

	@Override
	public void focusOn(IShape geometry) {
		// TODO Auto-generated method stub

	}

	@Override
	public void runAndUpdate(Runnable r) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void outputReloaded() {
		// TODO Auto-generated method stub

	}

	@Override
	public double getEnvWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getEnvHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getDisplayWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getDisplayHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ILocation getModelCoordinates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILocation getModelCoordinatesFrom(int xOnScreen, int yOnScreen, Point sizeInPixels, Point positionInPixels) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IAgent> selectAgent(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void followAgent(IAgent a) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getZoomLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSize(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public LayeredDisplayOutput getOutput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LayeredDisplayData getData() {
		return output.getData();
	}

	@Override
	public void layersChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(IEventLayerListener e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeListener(IEventLayerListener e) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<IEventLayerListener> getLayerListeners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Envelope getVisibleRegionForLayer(ILayer currentLayer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getFPS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isRealized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRendered() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDisposed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void getModelCoordinatesInfo(StringBuilder receiver) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispatchKeyEvent(char character) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispatchMouseEvent(int swtEventType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMousePosition(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draggedTo(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectAgentsAroundMouse() {
		// TODO Auto-generated method stub

	}
}