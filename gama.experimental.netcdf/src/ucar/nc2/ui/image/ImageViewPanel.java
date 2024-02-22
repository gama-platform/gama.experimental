/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package ucar.nc2.ui.image;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * *
 * 
 * @author caron
 */
public class ImageViewPanel extends JPanel {

	private PicturePane pixPane;

	public ImageViewPanel(Container buttPanel) {
		pixPane = new PicturePane();
		setLayout(new BorderLayout());
		add(pixPane, BorderLayout.CENTER);

		if (buttPanel == null) {
			buttPanel = new JPanel();
			add(buttPanel, BorderLayout.NORTH);
		}

	}

	public void setImage(BufferedImage im) {
		if (im == null)
			return; 
		pixPane.setBufferedImage(im, "setImage");
		pixPane.repaint();
	}

	static public void main(String[] args) {
		String[] readers = javax.imageio.ImageIO.getReaderFormatNames();
		for (int i = 0; i < readers.length; i++) {
			System.out.println(" reader = " + readers[i]);
		}
		System.out.println("-------------------------------------------");
		String[] readerMimes = javax.imageio.ImageIO.getReaderMIMETypes();
		for (int i = 0; i < readerMimes.length; i++) {
			System.out.println(" readerMimes = " + readerMimes[i]);
		}
		System.out.println("-------------------------------------------");
		String[] writers = javax.imageio.ImageIO.getWriterFormatNames();
		for (int i = 0; i < writers.length; i++) {
			System.out.println(" writers = " + writers[i]);
		}
		System.out.println("-------------------------------------------");
		String[] writerMimes = javax.imageio.ImageIO.getWriterMIMETypes();
		for (int i = 0; i < writerMimes.length; i++) {
			System.out.println(" writerMimes = " + writerMimes[i]);
		}
		System.out.println("-------------------------------------------");
	}

}
