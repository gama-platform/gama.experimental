package cict.gaml.extensions.netcdf.file;

import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Formatter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ui.GeoGridTable;
import ucar.nc2.ui.ToolsUI;
import ucar.nc2.ui.image.ImageViewPanel;

public class TestNetCDF extends JPanel {

	private static TestNetCDF ui;
	private static JFrame frame;
	private static ImageViewPanel imgpan;
	NetcdfDataset ds = null;
	GeoGridTable dsTable;

	public static void main(final String args[]) throws Exception {
		frame = new JFrame("NetCDF (4.2) Tools");
		imgpan = new ImageViewPanel(frame);
		ui = new TestNetCDF();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		frame.getContentPane().add(imgpan);
		frame.pack(); 
	    frame.setBounds(new Rectangle(50, 50, 800, 450));
		frame.setVisible(true);

		ui.process();
		new Thread() {

			@Override
			public void run() {
				BufferedImage img = imgpan.imageDatasetFactory.getNextImage(true);
				while (img != null) {
					imgpan.setImage(img);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					img = imgpan.imageDatasetFactory.getNextImage(true);
				}
			}

		}.start();
	}

	boolean process() {

		dsTable = new GeoGridTable(true);
//		String command = "E:/test_echam_spectral.nc";
//		String command = "E:/cami_0000-09-01_64x128_L26_c030918.nc";
		String command = "E:/tos_O1_2001-2002.nc";
//		String command = "E:/sresa1b_ncar_ccsm3-example.nc";
		boolean err = false;

		NetcdfDataset newds;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(10000);
		try {
			newds = NetcdfDataset.openDataset(command, true, null);
			if (newds == null) {
				JOptionPane.showMessageDialog(null, "NetcdfDataset.open cant open " + command);
				return false;
			}
			setDataset(newds);

		} catch (FileNotFoundException ioe) {
			JOptionPane.showMessageDialog(null, "NetcdfDataset.open cant open " + command + "\n" + ioe.getMessage());
			// ioe.printStackTrace();
			err = true;

		} catch (Throwable ioe) {
			ioe.printStackTrace();
			ioe.printStackTrace(new PrintStream(bos));
			err = true;
		}

		if (ds != null) {
			GridDatatype grid = dsTable.getGrid();
			if (grid == null)
				return false;
			imgpan.setImageFromGrid(grid);
		}
		return !err;

	}

	void setDataset(NetcdfDataset newds) {
		if (newds == null)
			return;
		try {
			if (ds != null)
				ds.close();
		} catch (IOException ioe) {
		}

		Formatter parseInfo = new Formatter();
		this.ds = newds;
		try {
			dsTable.setDataset(newds, parseInfo);
		} catch (IOException e) {
			String info = parseInfo.toString();
//			if (info.length() > 0) {
//				detailTA.setText(info);
//				detailWindow.show();
//			}
			e.printStackTrace();
			return;
		}
//		setSelectedItem(newds.getLocation());
	}
}
