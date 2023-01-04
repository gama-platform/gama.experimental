package across.gaml.extensions.webcam.types;

import java.util.Iterator;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamEventType;
import com.github.sarxos.webcam.WebcamListener;

public class WebcamCustom extends Webcam {

	
	public WebcamCustom(WebcamDevice device) {
		super(device);
	}
	
	public void dispose() {

		WebcamEvent we = new WebcamEvent(WebcamEventType.CLOSED, this);
		
		for (WebcamListener l : getWebcamListeners()) {
			try {
				l.webcamClosed(we);
				//l.webcamDisposed(we);
			} catch (Exception e) {}
		}
	}

}
