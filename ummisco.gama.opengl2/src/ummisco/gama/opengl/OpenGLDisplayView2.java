/*********************************************************************************************
 *
 * 'OpenGLDisplayView.java, in plugin ummisco.gama.opengl, is part of the source code of the GAMA modeling and
 * simulation platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gama.opengl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import msi.gama.common.interfaces.IGui;
import ummisco.gama.ui.views.displays.LayeredDisplayView;

/**
 * Class OpenGLLayeredDisplayView.
 *
 * @author drogoul
 * @since 25 mars 2015
 *
 */
public class OpenGLDisplayView2 extends LayeredDisplayView {

	public static String ID = IGui.GL_LAYER_VIEW_ID2;//"msi.gama.application.view.OpenGLDisplayView";

	@Override
	public SWTOpenGLDisplaySurface getDisplaySurface() {
		return (SWTOpenGLDisplaySurface) super.getDisplaySurface();
	}

	@Override
	protected Composite createSurfaceComposite(final Composite parent) {
		final SWTOpenGLDisplaySurface surface = new SWTOpenGLDisplaySurface(parent, getOutput());
		surfaceComposite = surface.renderer.getCanvas();
		surface.outputReloaded();
		return surfaceComposite;
	}

	@Override
	public boolean forceOverlayVisibility() {
		final SWTOpenGLDisplaySurface surface = getDisplaySurface();
		return surface != null && surface.getROIDimensions() != null;
	}

	@Override
	public List<String> getCameraNames() {
		return new ArrayList<String>(getDisplaySurface().renderer.camera.PRESETS.keySet());
	}

	@Override
	public void hideToolbar() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showToolbar() {
		// TODO Auto-generated method stub
		
	}
}
