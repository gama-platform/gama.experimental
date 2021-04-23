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
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ummisco.gama.ui.views.displays.SWTDisplayView;

public class WebDisplayView extends SWTDisplayView {

	public static String ID = "msi.gama.application.view.WebDisplayView"; 

	@Override
	protected Composite createSurfaceComposite(Composite parent) {

		surfaceComposite = new Browser(parent, SWT.NONE);
	    GridData layoutData = new GridData(GridData.FILL_BOTH);
	    layoutData.horizontalSpan = 2;
	    layoutData.verticalSpan = 2;
	    surfaceComposite.setLayoutData(layoutData);
	    ((Browser) surfaceComposite).setText("<!-- message form -->\r\n"
	    		+ "<form name=\"publish\">\r\n"
	    		+ "  <input type=\"text\" name=\"message\">\r\n"
	    		+ "  <input type=\"submit\" value=\"Send\">\r\n"
	    		+ "</form>\r\n"
	    		+ "\r\n"
	    		+ "<!-- div with messages -->\r\n"
	    		+ "<div id=\"messages\"></div>\r\n"
	    		+ "<script>\r\n"
	    		+ "let socket = new WebSocket(\"ws://localhost:8887\");\r\n"
	    		+ "\r\n"
	    		+ "\r\n"
	    		+ "// send message from the form\r\n"
	    		+ "document.forms.publish.onsubmit = function() {\r\n"
	    		+ "  let outgoingMessage = this.message.value;\r\n"
	    		+ "\r\n"
	    		+ "  socket.send(outgoingMessage);\r\n"
	    		+ "  return false;\r\n"
	    		+ "};\r\n"
	    		+ "\r\n"
	    		+ "// message received - show the message in div#messages\r\n"
	    		+ "socket.onmessage = function(event) {\r\n"
	    		+ "  let message = event.data;\r\n"
	    		+ "\r\n"
	    		+ "  let messageElem = document.createElement('div');\r\n"
	    		+ "  messageElem.textContent = message;\r\n"
	    		+ "  document.getElementById('messages').prepend(messageElem);\r\n"
	    		+ "}\r\n"
	    		+ "</script>", true);
  
		return surfaceComposite;
	}

}