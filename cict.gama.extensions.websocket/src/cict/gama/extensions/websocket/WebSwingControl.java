/*********************************************************************************************
 *
 * 'SwingControl.java, in plugin ummisco.gama.java2d, is part of the source code of the GAMA modeling and simulation
 * platform. (v. 1.8.1)
 *
 * (c) 2007-2020 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package cict.gama.extensions.websocket;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame; 

import msi.gama.runtime.PlatformHelper;
import javax.swing.JApplet;
import javax.swing.LayoutFocusTraversalPolicy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ummisco.gama.dev.utils.DEBUG; 
import ummisco.gama.ui.utils.WorkbenchHelper;

public abstract class WebSwingControl extends Composite {

	static {
		DEBUG.ON();
	}

	JApplet applet;
	Frame frame;
	boolean populated = false;

	public WebSwingControl(final Composite parent, final int style) {
		super(parent, style | ((style & SWT.BORDER) == 0 ? SWT.EMBEDDED : 0) | SWT.NO_BACKGROUND);
 
		GridLayout layout = new GridLayout(2, true); 
		setLayout(layout);
		addListener(SWT.Dispose, event -> EventQueue.invokeLater(() -> {
			try {
				frame.remove(applet);
			} catch (final Exception e) {}

		}));

//		Browser b = new Browser(parent, SWT.NONE);
//	    GridData layoutData = new GridData(GridData.FILL_BOTH);
//	    layoutData.horizontalSpan = 2;
//	    layoutData.verticalSpan = 2;
//	    b.setLayoutData(layoutData);
//	    b.setJavascriptEnabled(true);
//	    b.setUrl("C:\\git\\gama.experimental\\cict.gama.extensions.websocket\\lib\\a.html");
//	    b.setText("<!-- message form -->\r\n"
//	    		+ "<form name=\"publish\">\r\n"
//	    		+ "  <input type=\"text\" name=\"message\">\r\n"
//	    		+ "  <input type=\"submit\" value=\"Send\">\r\n"
//	    		+ "</form>\r\n"
//	    		+ "\r\n"
//	    		+ "<!-- div with messages -->\r\n"
//	    		+ "<div id=\"messages\"></div>\r\n"
//	    		+ "<script>\r\n"
//	    		+ "let socket = new WebSocket(\"ws://localhost:8887\");\r\n"
//	    		+ "\r\n"
//	    		+ "\r\n"
//	    		+ "// send message from the form\r\n"
//	    		+ "document.forms.publish.onsubmit = function() {\r\n"
//	    		+ "  let outgoingMessage = this.message.value;\r\n"
//	    		+ "\r\n"
//	    		+ "  socket.send(outgoingMessage);\r\n"
//	    		+ "  return false;\r\n"
//	    		+ "};\r\n"
//	    		+ "\r\n"
//	    		+ "// message received - show the message in div#messages\r\n"
//	    		+ "socket.onmessage = function(event) {\r\n"
//	    		+ "  let message = event.data;\r\n"
//	    		+ "\r\n"
//	    		+ "  let messageElem = document.createElement('div');\r\n"
//	    		+ "  messageElem.textContent = message;\r\n"
//	    		+ "  document.getElementById('messages').prepend(messageElem);\r\n"
//	    		+ "}\r\n"
//	    		+ "</script>", true);
  
	}

	@Override
	public void checkWidget() {}

	@Override
	public boolean isFocusControl() {
		boolean result = false;
		try {
			result = super.isFocusControl();
		} catch (final Exception e) {
			// Nothing. Eliminates annoying exceptions when closing Java2D displays.
		}
		return result;
	}

	protected void populate() {
		if (isDisposed()) { return; }
		if (!populated) {
			populated = true;
			frame = SWT_AWT.new_Frame(this);
			EventQueue.invokeLater(() -> {
				applet = new JApplet();
				if (PlatformHelper.isWindows()) {
					applet.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
				}
				frame.add(applet);
				final WebDisplaySurface surface = createSwingComponent();
				applet.getRootPane().getContentPane().add(surface);
				WorkbenchHelper.asyncRun(() -> WebSwingControl.this.getParent().layout(true, true));
			});
		}
	}

	/**
	 * Creates the embedded Swing component. This method is called from the AWT event thread.
	 *
	 * @return a non-null Swing component
	 */
	protected abstract WebDisplaySurface createSwingComponent();

	@Override
	public void setBounds(final Rectangle rect) {
		setBounds(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Overridden to propagate the size to the embedded Swing component.
	 */
	@Override
	public void setBounds(final int x, final int y, final int width, final int height) {
		DEBUG.OUT("-- Surface bounds set to " + x + "  " + y + " | " + width + " " + height);
		populate();
		super.setBounds(x, y, width, height);
	}

	public JApplet getTopLevelContainer() {
		return applet;
	}

}
