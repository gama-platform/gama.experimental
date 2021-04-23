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
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.locationtech.jts.geom.Envelope;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.ILayer;
import msi.gama.common.interfaces.ILayerManager;
import msi.gama.common.preferences.GamaPreferences;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.metamodel.shape.IShape;
import msi.gama.outputs.LayeredDisplayData;
import msi.gama.outputs.LayeredDisplayData.Changes;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.outputs.layers.IEventLayerListener;
import msi.gama.precompiler.GamlAnnotations.display;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.runtime.IScope;
import ummisco.gama.dev.utils.DEBUG; 

@display ("web")
@doc ("Display that uses the websocket technology to draw the layers in a web view")
public class WebDisplaySurface extends WebSocketServer implements IDisplaySurface {
 
	public WebDisplaySurface(final Object... args) { 
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
		return null;
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
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(WebSocket arg0, Exception arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(WebSocket arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOpen(WebSocket arg0, ClientHandshake arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		
	}
 
 

}