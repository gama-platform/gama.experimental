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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.eclipse.swt.SWT;

import org.locationtech.jts.geom.Envelope;

import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.interfaces.ILayer;
import msi.gama.common.interfaces.ILayerManager;
import msi.gama.common.preferences.GamaPreferences;
import msi.gama.common.util.ImageUtils;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.metamodel.shape.IShape;
import msi.gama.outputs.LayeredDisplayData;
import msi.gama.outputs.LayeredDisplayData.Changes;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.outputs.display.AWTDisplayGraphics;
import msi.gama.outputs.display.LayerManager;
import msi.gama.outputs.layers.IEventLayerListener;
import msi.gama.outputs.layers.OverlayLayer;
import msi.gama.precompiler.GamlAnnotations.display;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.PlatformHelper;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import ummisco.gama.dev.utils.DEBUG;
import ummisco.gama.ui.utils.WorkbenchHelper;
import ummisco.gama.ui.views.displays.DisplaySurfaceMenu;

@display ("web")
@doc("Display that uses the websocket technology to draw the layers in a web view")
public class WebDisplaySurface extends JPanel implements IDisplaySurface {


	final LayeredDisplayOutput output;
	protected final Rectangle viewPort = new Rectangle();
	// protected final AffineTransform translation = new AffineTransform();
	protected final ILayerManager layerManager;
	protected IGraphics iGraphics;

	protected DisplaySurfaceMenu menuManager;
	protected IExpression temp_focus;

	protected Dimension previousPanelSize;
	protected double zoomIncrement = 0.1;
	protected boolean zoomFit = true;
	protected volatile boolean disposed;

	private IScope scope;
	int frames;
	private volatile boolean realized = false;
	private volatile boolean rendered = false;
	Set<IEventLayerListener> listeners = new HashSet<>();
	Point mousePosition;

	ChatServer myserver ;

//	public WebDisplaySurface(final Object... args) {
//		output = (LayeredDisplayOutput) args[0];
//		output.setSurface(this);
//		setDisplayScope(output.getScope().copy("in java2D display"));
//		output.getData().addListener(this);
//		temp_focus = output.getFacet(IKeyword.FOCUS);
//		layerManager = new LayerManager(this, output);
//		int port = 8887; // 843 flash policy port
//
//		try {
//			final ChatServer s = new ChatServer(port);
//			s.start();
//			System.out.println("ChatServer started on port: " + s.getPort());
//			scope.getSimulation().postDisposeAction(sc -> {
//				try {
//					s.stop();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				return null;
//			});
////			BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
////			while (true) {
////				String in = sysin.readLine();
////				s.broadcast(in);
////				if (in.equals("exit")) {
////					s.stop(1000);
////					break;
////				}
////			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	public WebDisplaySurface(final Object... args) {
		output = (LayeredDisplayOutput) args[0];
		output.setSurface(this);
		setDisplayScope(output.getScope().copy("in java2D display"));
		output.getData().addListener(this);
		temp_focus = output.getFacet(IKeyword.FOCUS);
		setDoubleBuffered(true);
		setIgnoreRepaint(true);
		setLayout(new BorderLayout());
		setBackground(output.getData().getBackgroundColor());
		setName(output.getName());
		layerManager = new LayerManager(this, output);
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(final ComponentEvent e) {
				if (zoomFit) {
					zoomFit();
				} else {
					if (isFullImageInPanel()) {
						centerImage();
					} else if (isImageEdgeInPanel()) {
						scaleOrigin();
					}
					updateDisplay(true);
				}
				final double newZoom = Math.min(getWidth() / getDisplayWidth(), getHeight() / getDisplayHeight());
				newZoomLevel(1 / newZoom);
				previousPanelSize = getSize();
			}
		});
		int port = 8887; // 843 flash policy port

		try {
			myserver = new ChatServer(port,this);
			myserver.start();
			System.out.println("ChatServer started on port: " + myserver.getPort());
//			scope.getSimulation().postDisposeAction(sc -> {
//				try {
//					s.stop();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				return null;
//			});
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

	@Override
	public void setMenuManager(final Object menuManager) {
		this.menuManager = (DisplaySurfaceMenu) menuManager;
	}

	@Override
	public int getFPS() {
		final int result = frames;
		frames = 0;
		return result;
	}

	@Override
	public void dispatchKeyEvent(final char e) {
		for (final IEventLayerListener gl : listeners) {
			gl.keyPressed(String.valueOf(e));
		}
	}

//	@Override
//	public void setMousePosition(final int xm, final int ym) {
//		final int x = PlatformHelper.autoScaleUp(xm);
//		final int y = PlatformHelper.autoScaleUp(ym);
//		if (mousePosition == null) {
//			mousePosition = new Point(x, y);
//		} else {
//			mousePosition.setLocation(x, y);
//		}
//	}
//
//	@Override
//	public void draggedTo(final int x, final int y) {
//		final Point origin = getOrigin();
//		setOrigin(origin.x + PlatformHelper.autoScaleUp(x) - getMousePosition().x, origin.y + PlatformHelper.autoScaleUp(y) - getMousePosition().y);
//		DEBUG.OUT("Translation on X : " + (PlatformHelper.autoScaleUp(x) - getMousePosition().x) + " | on Y : "
//				+ (PlatformHelper.autoScaleUp(y) - getMousePosition().y));
//		DEBUG.OUT("Old Origin = " + origin + " | New Origin = " + getOrigin());
//		setMousePosition(x, y);
//		updateDisplay(true);
//	}

	public void setMousePosition(final Point point) {
		mousePosition = point;

	}

	@Override
	public Point getMousePosition() {
		return mousePosition;
	}

	@Override
	public void dispatchMouseEvent(final int swtMouseEvent) {
		final int x = mousePosition.x;
		final int y = mousePosition.y;
		for (final IEventLayerListener gl : listeners) {
			switch (swtMouseEvent) {
				case SWT.MouseDown:
					gl.mouseDown(x, y, 1);
					break;
				case SWT.MouseUp:
					gl.mouseUp(x, y, 1);
					break;
				case SWT.MouseMove:
					gl.mouseMove(x, y);
					break;
				case SWT.MouseEnter:
					gl.mouseEnter(x, y);
					break;
				case SWT.MouseExit:
					gl.mouseExit(x, y);
					break;
				case SWT.MenuDetect:
					gl.mouseMenu(x, y);
					break;
			}
		}
	}

	@Override
	public void outputReloaded() {
		// We first copy the scope
		setDisplayScope(output.getScope().copy("in java2D display "));
		// We disable error reporting
		if (!GamaPreferences.Runtime.ERRORS_IN_DISPLAYS.getValue()) {
			getScope().disableErrorReporting();
		}

		layerManager.outputChanged();

		resizeImage(getWidth(), getHeight(), true);
		if (zoomFit) {
			zoomFit();
		}
		updateDisplay(true);
	}

	@Override
	public IScope getScope() {
		return scope;
	}

	@Override
	public ILayerManager getManager() {
		return layerManager;
	}

	Point getOrigin() {
		return viewPort.getLocation();
	}

	@Override
	public void setFont(final Font f) {
		// super.setFont(null);
	}

	@Override
	public BufferedImage getImage(final int w, final int h) {
		final int previousWidth = getWidth();
		final int previousHeight = getHeight();
		final int width = w == -1 ? previousWidth : w;
		final int height = h == -1 ? previousHeight : h;
		final boolean sameSize = width == previousWidth && height == previousHeight;
		final BufferedImage newImage = ImageUtils.createCompatibleImage(width, height, false);
		final Graphics g = newImage.getGraphics();

		while (!rendered) {
			try {
				Thread.sleep(10);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			EventQueue.invokeAndWait(() -> {
				final Rectangle old = new Rectangle(viewPort);
				if (!sameSize) {
					viewPort.x = viewPort.y = 0;
					final int[] point = computeBoundsFrom(width, height);
					viewPort.width = point[0];
					viewPort.height = point[1];
					// resizeImage(width, height, false);

				}
				print(g);
				if (!sameSize) {
					// resizeImage(previousWidth, previousHeight, false);
					viewPort.setBounds(old);
				}

			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
		g.dispose();
		return newImage;
	}

	protected void scaleOrigin() {
		final Point origin = getOrigin();
		setOrigin((int) Math.round((double) origin.x * getWidth() / previousPanelSize.width),
				(int) Math.round((double) origin.y * getHeight() / previousPanelSize.height));
		updateDisplay(true);
	}

	protected void centerImage() {
		setOrigin((int) Math.round((getWidth() - getDisplayWidth()) / 2d),
				(int) Math.round((getHeight() - getDisplayHeight()) / 2d));
	}

	protected int getOriginX() {
		return getOrigin().x;
	}

	protected int getOriginY() {
		return getOrigin().y;
	}

	void setOrigin(final int x, final int y) {
		// Temporarily reverts the changes introduced for #2367
		final int inset = 0;
		viewPort.setLocation(x - inset, y - inset);
	}

	@Override
	public void updateDisplay(final boolean force) {
		if (disposed) { return; }
		rendered = false;
		EventQueue.invokeLater(() -> repaint());
	}

	@Override
	public void focusOn(final IShape geometry) {
		final Rectangle2D r = this.getManager().focusOn(geometry, this);
		if (r == null) { return; }
		final double xScale = getWidth() / r.getWidth();
		final double yScale = getHeight() / r.getHeight();
		double zoomFactor = Math.min(xScale, yScale);
		final Point center = new Point((int) Math.round(r.getCenterX()), (int) Math.round(r.getCenterY()));

		zoomFactor = applyZoom(zoomFactor);
		center.setLocation(center.x * zoomFactor, center.y * zoomFactor);
		centerOnDisplayCoordinates(center);

		updateDisplay(true);
	}

	@Override
	public void validate() {}

	@Override
	public void doLayout() {}

	private void zoom(final boolean in) {
		final Point origin = getOrigin();
		final Point p = getMousePosition();
		int x = p.x;
		int y = p.y;
		if (x == -1 && y == -1) {
			x = getWidth() / 2;
			y = getHeight() / 2;
		}
		final double zoomFactor = applyZoom(1.0 + (in ? 1 : -1) * zoomIncrement);
		final double newx = Math.round(zoomFactor * (x - origin.x) - x + getWidth() / 2d);
		final double newy = Math.round(zoomFactor * (y - origin.y) - y + getHeight() / 2d);
		centerOnDisplayCoordinates(new Point((int) newx, (int) newy));
		updateDisplay(true);
	}

	@Override
	public void zoomIn() {
		zoom(true);
	}

	@Override
	public void zoomOut() {
		zoom(false);
	}

	// Used when the image is resized.
	public boolean isImageEdgeInPanel() {
		if (previousPanelSize == null) { return false; }
		final Point origin = getOrigin();
		return origin.x > 0 && origin.x < previousPanelSize.width
				|| origin.y > 0 && origin.y < previousPanelSize.height;
	}

	// Tests whether the image is displayed in its entirety in the panel.
	public boolean isFullImageInPanel() {
		final Point origin = getOrigin();
		return origin.x >= 0 && origin.x + getDisplayWidth() < getWidth() && origin.y >= 0
				&& origin.y + getDisplayHeight() < getHeight();
	}

	public boolean resizeImage(final int x, final int y, final boolean force) {
		if (!force && x == getDisplayWidth() && y == getDisplayHeight()) { return true; }
		if (x < 10 || y < 10) { return false; }
		if (getWidth() <= 0 && getHeight() <= 0) { return false; }
		// DEBUG.OUT("Resize display : " + x + " " + y);
		final int[] point = computeBoundsFrom(x, y);
		final int imageWidth = Math.max(1, point[0]);
		final int imageHeight = Math.max(1, point[1]);
		setDisplayHeight(imageHeight);
		setDisplayWidth(imageWidth);
		iGraphics = new AWTDisplayGraphics((Graphics2D) this.getGraphics());
		iGraphics.setDisplaySurface(this);
		return true;

	}

	@Override
	public void paintComponent(final Graphics g) {

		realized = true;
		final AWTDisplayGraphics gg = getIGraphics();
		if (gg == null) { return; }
		DEBUG.OUT("-- Surface effectively painting on Java2D context");
		super.paintComponent(g);
		final Graphics2D g2d = (Graphics2D) g.create(getOrigin().x, getOrigin().y, (int) Math.round(getDisplayWidth()),
				(int) Math.round(getDisplayHeight()));
		gg.setGraphics2D(g2d);
		gg.setUntranslatedGraphics2D((Graphics2D) g);
		layerManager.drawLayersOn(gg);
		if (temp_focus != null) {
			final IShape geometry = Cast.asGeometry(getScope(), temp_focus.value(getScope()), false);
			temp_focus = null;
			focusOn(geometry);
			rendered = true;
			return;
		}

		// TODO Verify that the following expressions should not be also included in the "focus" block
		g2d.dispose();
		frames++;
		rendered = true;
	}

	AWTDisplayGraphics getIGraphics() {
		return (AWTDisplayGraphics) iGraphics;
	}

	@Override
	public GamaPoint getModelCoordinates() {
		final Point origin = getOrigin();
		final Point mouse = getMousePosition();
		if (mouse == null) { return null; }
		final int xc = mouse.x - origin.x;
		final int yc = mouse.y - origin.y;
		final List<ILayer> layers = layerManager.getLayersIntersecting(xc, yc);
		for (final ILayer layer : layers) {
			if (layer.isProvidingWorldCoordinates()) { return layer.getModelCoordinatesFrom(xc, yc, this); }
		}
		// See Issue #2783: we dont return null but 0,0.
		// return null;
		return new GamaPoint();
	}

	@Override
	public void getModelCoordinatesInfo(final StringBuilder sb) {
		final Point origin = getOrigin();
		final Point mouse = getMousePosition();
		if (mouse == null) { return; }
		final int xc = mouse.x - origin.x;
		final int yc = mouse.y - origin.y;
		final List<ILayer> layers = layerManager.getLayersIntersecting(xc, yc);
		for (final ILayer layer : layers) {
			if (layer.isProvidingCoordinates()) {
				layer.getModelCoordinatesInfo(xc, yc, this, sb);
				return;
			}
		}
		sb.append("No world coordinates");
	}

	@Override
	public double getEnvWidth() {
		return output.getData().getEnvWidth();
	}

	@Override
	public double getEnvHeight() {
		return output.getData().getEnvHeight();
	}

	@Override
	public double getDisplayWidth() {
		return viewPort.width;
	}

	protected void setDisplayWidth(final int displayWidth) {
		viewPort.width = displayWidth - 2;
	}

	@Override
	public LayeredDisplayData getData() {
		return output.getData();
	}

	@Override
	public double getDisplayHeight() {
		return viewPort.height;
	}

	protected void setDisplayHeight(final int displayHeight) {
		viewPort.height = displayHeight - 2;
	}

	@Override
	public LayeredDisplayOutput getOutput() {
		return output;
	}

	public void newZoomLevel(final double newZoomLevel) {
		getData().setZoomLevel(newZoomLevel, true, false);
	}

	@Override
	public double getZoomLevel() {
		if (getData().getZoomLevel() == null) {
			getData().setZoomLevel(1.0, true, false);
		}
		return getData().getZoomLevel();
	}

	@Override
	public void zoomFit() {
		final int w = getWidth();
		final int h = getHeight();
		setMousePosition(new Point((int) Math.round((double) w / 2), (int) Math.round((double) h / 2)));
		if (resizeImage(w, h, false)) {
			newZoomLevel(1d);
			zoomFit = true;
			centerImage();
			updateDisplay(true);
		}
	}

	private int[] computeBoundsFrom(final int vwidth, final int vheight) {
		if (!layerManager.stayProportional()) { return new int[] { vwidth, vheight }; }
		final int[] dim = new int[2];
		final double widthHeightConstraint = getEnvHeight() / getEnvWidth();
		if (widthHeightConstraint < 1) {
			dim[1] = Math.min(vheight, (int) Math.round(vwidth * widthHeightConstraint));
			dim[0] = Math.min(vwidth, (int) Math.round(dim[1] / widthHeightConstraint));
		} else {
			dim[0] = Math.min(vwidth, (int) Math.round(vheight / widthHeightConstraint));
			dim[1] = Math.min(vheight, (int) Math.round(dim[0] * widthHeightConstraint));
		}
		return dim;
	}

	@Override
	public GamaPoint getModelCoordinatesFrom(final int xOnScreen, final int yOnScreen, final Point sizeInPixels,
			final Point positionInPixels) {
		final double xScale = sizeInPixels.x / getEnvWidth();
		final double yScale = sizeInPixels.y / getEnvHeight();
		final int xInDisplay = xOnScreen - positionInPixels.x;
		final int yInDisplay = yOnScreen - positionInPixels.y;
		final double xInModel = xInDisplay / xScale;
		final double yInModel = yInDisplay / yScale;
		return new GamaPoint(xInModel, yInModel);
	}

	@Override
	public Envelope getVisibleRegionForLayer(final ILayer currentLayer) {
		if (currentLayer instanceof OverlayLayer) { return getScope().getSimulation().getEnvelope(); }
		final Envelope e = new Envelope();
		final Point origin = getOrigin();
		int xc = -origin.x;
		int yc = -origin.y;
		e.expandToInclude((GamaPoint) currentLayer.getModelCoordinatesFrom(xc, yc, this));
		xc = xc + getIGraphics().getViewWidth();
		yc = yc + getIGraphics().getViewHeight();
		e.expandToInclude((GamaPoint) currentLayer.getModelCoordinatesFrom(xc, yc, this));
		return e;
	}

	protected void setDisplayScope(final IScope scope) {
		if (this.scope != null) {
			GAMA.releaseScope(this.scope);
		}
		this.scope = scope;
	}

	@Override
	public void runAndUpdate(final Runnable r) {
		new Thread(() -> {
			r.run();
			if (output.isPaused() || getScope().isPaused()) {
				updateDisplay(true);
			}
		}).start();
	}

	@Override
	public void dispose() {
		try {
			myserver.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getData().removeListener(this);
		if (disposed) { return; }
		setRealized(false);
		disposed = true;
		if (layerManager != null) {
			layerManager.dispose();
		}

		GAMA.releaseScope(getScope());
		setDisplayScope(null);
	}

	@Override
	public void addListener(final IEventLayerListener ell) {
		listeners.add(ell);
	}

	@Override
	public void removeListener(final IEventLayerListener ell) {
		listeners.remove(ell);
	}

	@Override
	public Collection<IEventLayerListener> getLayerListeners() {
		return listeners;
	}

	/**
	 * Method followAgent()
	 *
	 * @see msi.gama.common.interfaces.IDisplaySurface#followAgent(msi.gama.metamodel.agent.IAgent)
	 */
	@Override
	public void followAgent(final IAgent a) {}

	@Override
	public void setBounds(final int arg0, final int arg1, final int arg2, final int arg3) {
		// DEBUG.OUT("-- Java2D surface set bounds to " + arg0 + " " + arg1 + " | " + arg2 + " " + arg3);
		if (arg2 == 0 && arg3 == 0) { return; }
		super.setBounds(arg0, arg1, arg2, arg3);
	}
	//
	// @Override
	// public void setBounds(final Rectangle r) {
	// DEBUG.OUT("-- Java2D surface set bounds to " + r);
	// if (r.width < 1 && r.height < 1) { return; }
	// super.setBounds(r);
	// }

	double applyZoom(final double factor) {
		double real_factor = Math.min(factor, 10 / getZoomLevel());
		real_factor = Math.max(MIN_ZOOM_FACTOR, real_factor);
		real_factor = Math.min(MAX_ZOOM_FACTOR, real_factor);
		final boolean success = resizeImage(Math.max(1, (int) Math.round(getDisplayWidth() * real_factor)),
				Math.max(1, (int) Math.round(getDisplayHeight() * real_factor)), false);

		if (success) {
			zoomFit = false;
			final double widthHeightConstraint = getEnvHeight() / getEnvWidth();

			if (widthHeightConstraint < 1) {
				newZoomLevel(getDisplayWidth() / getWidth());
			} else {
				newZoomLevel(getDisplayHeight() / getHeight());
			}
		}
		return real_factor;
	}

	private void centerOnViewCoordinates(final Point p) {
		final Point origin = getOrigin();
		final int translationX = p.x - Math.round(getWidth() / (float) 2);
		final int translationY = p.y - Math.round(getHeight() / (float) 2);
		setOrigin(origin.x - translationX, origin.y - translationY);

	}

	void centerOnDisplayCoordinates(final Point p) {
		final Point origin = getOrigin();
		centerOnViewCoordinates(new Point(p.x + origin.x, p.y + origin.y));
	}

	@Override
	public void selectAgentsAroundMouse() {
		final int mousex = getMousePosition().x;
		final int mousey = getMousePosition().y;
		final Point origin = getOrigin();
		final int xc = mousex - origin.x;
		final int yc = mousey - origin.y;
		final List<ILayer> layers = layerManager.getLayersIntersecting(xc, yc);
		if (layers.isEmpty()) { return; }
		WorkbenchHelper.run(() -> menuManager.buildMenu(mousex, mousey, xc, yc, layers));
	}

	@Override
	public Collection<IAgent> selectAgent(final int x, final int y) {
		final int xc = x - getOriginX();
		final int yc = y - getOriginY();
		final List<IAgent> result = new ArrayList<>();
		final List<ILayer> layers = getManager().getLayersIntersecting(xc, yc);
		for (final ILayer layer : layers) {
			final Set<IAgent> agents = layer.collectAgentsAt(xc, yc, this);
			if (!agents.isEmpty()) {
				result.addAll(agents);
			}
		}
		return result;
	}

	@Override
	public void layersChanged() {}

	/**
	 * Method changed()
	 *
	 * @see msi.gama.outputs.LayeredDisplayData.DisplayDataListener#changed(int, boolean)
	 */
	@Override
	public void changed(final Changes property, final Object value) {

		switch (property) {
			case BACKGROUND:
				setBackground((Color) value);
				break;
			default:
				;
		}

	};
 
	public void setRealized(final boolean b) {
		realized = b;
	}

	@Override
	public boolean isRendered() {
		return rendered;
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

//	@Override
//	public Font computeFont(final Font f) {
//		if (f == null) { return null; }
//		if (PlatformHelper.isWindows() && PlatformHelper.isHiDPI()) {
//			return f.deriveFont((float) PlatformHelper.autoScaleUp(f.getSize2D()));
//		}
//		return f;
//
//	}

	@Override
	public void setMousePosition(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draggedTo(int x, int y) {
		// TODO Auto-generated method stub
		
	}

}