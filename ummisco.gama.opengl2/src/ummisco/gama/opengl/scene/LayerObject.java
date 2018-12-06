/*********************************************************************************************
 *
 * 'LayerObject.java, in plugin ummisco.gama.opengl, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gama.opengl.scene;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.jogamp.opengl.GL2;
import com.vividsolutions.jts.geom.Geometry;

import msi.gama.common.geometry.Scaling3D;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.interfaces.ILayer; 
import msi.gama.metamodel.agent.AgentIdentifier;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.IShape; 
import msi.gama.outputs.layers.OverlayLayer;
import msi.gama.runtime.IScope;
import msi.gama.util.file.GamaGeometryFile;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.statements.draw.DrawingAttributes;
import msi.gaml.statements.draw.FieldDrawingAttributes;
import msi.gaml.types.GamaGeometryType;
import ummisco.gama.modernOpenGL.DrawingEntity;
import ummisco.gama.opengl.Abstract3DRenderer;
import ummisco.gama.opengl.ModernRenderer;
//import ummisco.gama.opengl.ModernRenderer;
import ummisco.gama.opengl.scene.GeometryObject.GeometryObjectWithAnimation;

/**
 * Class LayerObject.
 *
 * @author drogoul
 * @since 3 mars 2014
 *
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class LayerObject {

	final static GamaPoint NULL_OFFSET = new GamaPoint();
	final static GamaPoint NULL_SCALE = new GamaPoint(1, 1, 1);

	private boolean sceneIsInitialized = false;
	protected boolean constantRedrawnLayer = false;

	GamaPoint offset = new GamaPoint(NULL_OFFSET);
	GamaPoint scale = new GamaPoint(NULL_SCALE);
	Double alpha = 1d;
	final ILayer layer;
	volatile boolean isInvalid;
	final boolean overlay;
	volatile boolean locked;
	boolean isAnimated;
	final Abstract3DRenderer renderer;
	final LinkedList<List<AbstractObject>> objects;
	List<AbstractObject> currentList;
	Integer openGLListIndex;
	boolean isFading;

	public LayerObject(final Abstract3DRenderer renderer, final ILayer layer) {
		this.renderer = renderer;
		this.layer = layer;
		this.overlay = computeOverlay();
		currentList = newCurrentList();
		if (layer != null && layer.getData().getTrace() != null ) { //|| renderer instanceof ModernRenderer
			objects = new LinkedList();
			objects.add(currentList);
		} else
			objects = null;
	}

	protected boolean computeOverlay() {
		return layer != null && layer.isOverlay();
	}

	public boolean isLightInteraction() {
		return true;
	}

	private List newCurrentList() {
		return /* Collections.synchronizedList( */new ArrayList()/* ) */;
	}

	protected boolean isPickable() {
		return layer == null ? false : layer.getData().isSelectable();
	}

	public void draw(final OpenGL gl) {
		if (isInvalid()) { return; }
//		if (renderer.useShader()) {
			drawWithShader(gl.getGL());
//		} else {
//			drawWithoutShader(gl);
//		}
	}

	private void drawWithShader(final GL2 gl) {
		final ModernRenderer renderer = (ModernRenderer) this.renderer;

		if (isOverlay()) {
			gl.glDisable(GL2.GL_DEPTH_TEST);
		} else {
			gl.glEnable(GL2.GL_DEPTH_TEST);
		}

		if (!sceneIsInitialized || constantRedrawnLayer) {
			renderer.getDrawer().prepareMapForLayer(this);
			double alpha = 0d;
			final double originalAlpha = this.alpha;
			final int size = currentList.size();
			final double delta = size == 0 ? 0 : 1d / size;
			for (final AbstractObject list : currentList) {
				alpha = isFading ? originalAlpha * (alpha + delta) : originalAlpha;
				final AgentIdentifier id = list.attributes.getAgentIdentifier();
//				if (id != null)
//					System.out.println(id.getAgent(GAMAHelper.getRuntimeScope()));
				
				synchronized (list) {
//					for (final AbstractObject object : list) {
						final double alpha1 = alpha;
						renderer.getOpenGLHelper().setCurrentObjectAlpha(alpha1);
						final DrawingEntity[] drawingEntity = renderer.getDrawingEntityGenerator()
								.generateDrawingEntities(renderer.getSurface().getScope(), list, this, gl);
						if (overlay) {
							for (final DrawingEntity de : drawingEntity) {
								de.enableOverlay(true);
							}
						}
						if (drawingEntity != null) {

//							for (final DrawingEntity de : drawingEntity) {
//								System.out.println(de);
//								}							
							renderer.getDrawer().addDrawingEntities(drawingEntity);
						}
//					}
				}
			}
			renderer.getDrawer().redraw();
			renderer.getDrawer().refresh(this);
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			sceneIsInitialized = true;
		} else {
			renderer.getDrawer().refresh(this);
		}
	}

	private void drawWithoutShader(final OpenGL gl) {
		final GamaPoint scale = getScale();

		if (overlay) {
			gl.getGL().glDisable(GL2.GL_DEPTH_TEST);
			// Addition to fix #2228 and #2222
			gl.suspendZTranslation();
			//
			final double viewHeight = gl.getViewHeight();
			final double viewWidth = gl.getViewWidth();
			final double viewRatio = viewWidth / (viewHeight == 0 ? 1 : viewHeight);
			final double worldHeight = gl.getWorldHeight();
			final double worldWidth = gl.getWorldWidth();
			final double maxDim = worldHeight > worldWidth ? worldHeight : worldWidth;

			gl.pushIdentity(GL2.GL_PROJECTION);
			if (viewRatio >= 1.0) {
				gl.getGL().glOrtho(0, maxDim * viewRatio, -maxDim, 0, -1, 1);
			} else {
				gl.getGL().glOrtho(0, maxDim, -maxDim / viewRatio, 0, -1, 1);
			}

			gl.pushIdentity(GL2.GL_MODELVIEW);
		} else {
			gl.getGL().glEnable(GL2.GL_DEPTH_TEST);
		}
		try {
			gl.pushMatrix();
			final GamaPoint offset = getOffset();
			gl.translateBy(offset.x, -offset.y, overlay ? 0 : offset.z);
			gl.scaleBy(scale.x, scale.y, scale.z);

			final boolean picking = renderer.getPickingState().isPicking() && isPickable();
			if (picking) {
				if (!overlay)
					gl.runWithNames(() -> drawAllObjects(gl, true));
			} else {
				if (isAnimated || overlay) {
					drawAllObjects(gl, false);
				} else {
					if (openGLListIndex == null) {
						openGLListIndex = gl.compileAsList(() -> drawAllObjects(gl, false));
					}
					gl.drawList(openGLListIndex);
				}
			}
		} finally {
			gl.popMatrix();
			if (overlay) {
				// Addition to fix #2228 and #2222
				gl.resumeZTranslation();
				gl.pop(GL2.GL_MODELVIEW);
				gl.pop(GL2.GL_PROJECTION);
			}
		}

	}

	private void addFrame(final OpenGL gl) {
		GamaPoint scale = new GamaPoint(renderer.getEnvWidth(), renderer.getEnvHeight());
		final IScope scope = renderer.getSurface().getScope();
		final IExpression expr = layer.getDefinition().getFacet(IKeyword.SIZE);
		if (expr != null) {
			scale = (GamaPoint) Cast.asPoint(scope, expr.value(scope));
			if (scale.x <= 1) {
				scale.x *= renderer.getEnvWidth();
			}
			if (scale.y <= 1) {
				scale.y *= renderer.getEnvHeight();
			}
		}
		gl.pushMatrix();
		gl.translateBy(0, -scale.y, 0);
		gl.scaleBy(scale.x, scale.y, 1);
		gl.setCurrentColor(((OverlayLayer) layer).getData().getBackgroundColor(scope));
		gl.setCurrentObjectAlpha(((OverlayLayer) layer).getData().getTransparency(scope));
		gl.drawCachedGeometry(IShape.Type.ROUNDED, null);
		gl.popMatrix(); 
	}

	protected void drawAllObjects(final OpenGL gl, final boolean picking) {
		if (overlay) {
			addFrame(gl);
		}
		if (objects != null) {
			double delta = 0;
			if (isFading) {
				final int size = objects.size();
				delta = size == 0 ? 0 : 1d / size;
			}
			double alpha = 0d;
			for (final List<AbstractObject> list : objects) {
				alpha = delta == 0d ? this.alpha : this.alpha * (alpha + delta);
				drawObjects(gl, list, alpha, picking);
			}
		} else
			drawObjects(gl, currentList, alpha, picking);
	}

	protected void drawObjects(final OpenGL gl, final List<AbstractObject> list, final double alpha,
			final boolean picking) {
		final ImmutableList<AbstractObject> l = ImmutableList.copyOf(list);
		gl.setCurrentObjectAlpha(alpha);
		for (final AbstractObject object : l) {
			object.draw(gl, renderer.getDrawerFor(object.getDrawerType()), picking);
		}
	}

	public boolean isStatic() {
		if (layer == null) { return true; }
		return !layer.getData().isDynamic();
	}

	public void setAlpha(final Double a) {
		alpha = a;
	}

	public GamaPoint getOffset() {
		return offset == null ? NULL_OFFSET : offset;
	}

	public void setOffset(final GamaPoint offset) {
		this.offset = offset;
	}

	public GamaPoint getScale() {
		return scale == null ? NULL_SCALE : scale;
	}

	public Double getAlpha() {
		return alpha;
	}

	public void setScale(final GamaPoint scale) {
		this.scale.setLocation(scale);
	}

	public StringObject addString(final String string, final DrawingAttributes attributes) {
		final StringObject object = new StringObject(string, attributes);
		currentList.add(object);
		return object;
	}

	public ResourceObject addFile(final GamaGeometryFile file, final DrawingAttributes attributes) {
		final ResourceObject resource = new ResourceObject(file, attributes);
		currentList.add(resource);
		return resource;
	}

	public GeometryObject addImage(final Object o, final DrawingAttributes attributes) {
		// If no dimensions have been defined, then the image is considered as wide and tall as the environment
		Scaling3D size = attributes.getSize();
		if (size == null) {
			size = Scaling3D.of(renderer.getWorldsDimensions());
			attributes.setSize(size);
		}
		final GamaPoint loc = attributes.getLocation();
		final Scaling3D inc = attributes.getSize().dividedBy(2);
		final GamaPoint newLoc = loc == null ? inc.toGamaPoint() : loc.plus(inc.getX(), inc.getY(), inc.getZ());
		// We build a rectangle that will serve as a "support" for the image (which will become its texture)
		final Geometry geometry = GamaGeometryType.buildRectangle(size.getX(), size.getY(), newLoc).getInnerGeometry();

		attributes.setLocation(newLoc);
		attributes.setTexture(o);
		return addGeometry(geometry, attributes);
	}

	public FieldObject addField(final double[] fieldValues, final FieldDrawingAttributes attributes) {
		final FieldObject field = new FieldObject(fieldValues, attributes);
		currentList.add(field);
		return field;
	}

	public GeometryObject addGeometry(final Geometry geometry, final DrawingAttributes attributes) {
		final GeometryObject geom;
		if (attributes.isAnimated()) {
			isAnimated = true;
			geom = new GeometryObjectWithAnimation(geometry, attributes);
		} else {
			geom = new GeometryObject(geometry, attributes);
		}
		currentList.add(geom);
		return geom;
	}

	private int getTrace() {
		if (layer == null) { return 0; }
		final Integer trace = layer.getData().getTrace();
		return trace == null ? 0 : trace;
	}

	private boolean getFading() {
		if (layer == null) { return false; }
		final Boolean fading = layer.getData().getFading();
		return fading == null ? false : fading;
	}

	public void clear(final OpenGL gl) {

		if (objects != null) {
			final int sizeLimit = getTrace();
			isFading = getFading();
			final int size = objects.size();
			for (int i = 0, n = size - sizeLimit; i < n; i++) {
				final List<AbstractObject> list = objects.poll();
			}
			currentList = newCurrentList();
			objects.offer(currentList);
		} else
			currentList.clear();
		final Integer index = openGLListIndex;
		if (index != null) {
			gl.deleteList(index);
			openGLListIndex = null;
		}

		sceneIsInitialized = false;

	}

	public boolean isInvalid() {
		return isInvalid;
	}

	public void invalidate() {
		isInvalid = true;
	}

	public boolean hasTrace() {
		return getTrace() > 0;
	}

	public boolean isLocked() {
		return locked;
	}

	public void lock() {
		locked = true;
	}

	public void unlock() {
		locked = false;
	}

	public boolean isOverlay() {
		return overlay;
	}

	// public SimpleLayer toSimpleLayer() {
	//
	// final List<DrawingEntity> drawingEntityList = new ArrayList<DrawingEntity>();
	// // we don't send the "constantRedrawnLayer" (like the rotation helper)
	// if (!constantRedrawnLayer) {
	// for (final List<AbstractObject> list : objects) {
	// for (final AbstractObject object : list) {
	// final DrawingEntity[] drawingEntities = renderer.getDrawingEntityGenerator()
	// .generateDrawingEntities(renderer.getSurface().getScope(), object, false, this, null);
	// // explicitly passes null for the OpenGL context
	// if (drawingEntities != null) {
	// for (final DrawingEntity drawingEntity : drawingEntities) {
	// drawingEntityList.add(drawingEntity);
	// }
	// }
	// }
	// }
	// }
	// return new SimpleLayer(getOffset(), getScale(), alpha, drawingEntityList);
	// }

	public int numberOfTraces() {
		return objects == null ? 1 : objects.size();
	}

}
