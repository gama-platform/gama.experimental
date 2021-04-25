/*********************************************************************************************
 *
 * 'AWTDisplayView.java, in plugin ummisco.gama.java2d, is part of the source code of the GAMA modeling and simulation
 * platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package cict.gama.extensions.websocket;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ummisco.gama.ui.views.displays.SWTDisplayView;

public class WebDisplayView extends SWTDisplayView {

	public static String ID = "msi.gama.application.view.WebDisplayView"; 

	@Override
	protected Composite createSurfaceComposite(Composite parent) {
		if (getOutput() == null) { return null; }

		surfaceComposite = new WebSwingControl(parent, SWT.NO_FOCUS) {

			@Override
			protected WebDisplaySurface createSwingComponent() {
				return (WebDisplaySurface) getDisplaySurface();
			}

		};
		surfaceComposite.setEnabled(false);
		return surfaceComposite;
	}

}