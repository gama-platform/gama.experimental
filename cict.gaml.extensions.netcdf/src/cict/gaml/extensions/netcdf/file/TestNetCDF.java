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
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ui.image.ImageViewPanel;

public class TestNetCDF extends JPanel {

	private static TestNetCDF ui;
	private static JFrame frame;
	private static ImageViewPanel imgpan;
	NetcdfDataset ds = null;
	private GridDataset gridDataset;

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

//		String command = "E:/ROMS/SENEGAL_Y1980M1.nc";
		String command = "E:/tos_O1_2001-2002.nc";
//		String command = "E:/test_echam_spectral.nc";
//		String command = "E:/cami_0000-09-01_64x128_L26_c030918.nc";
//		String command = "C:\\git\\gama.experimental\\cict.gaml.extensions.netcdf\\models\\NetCDF\\includes\\tos_O1_2001-2002.nc";
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
			ioe.printStackTrace();
			err = true;

		} catch (Throwable ioe) {
			ioe.printStackTrace();
			ioe.printStackTrace(new PrintStream(bos));
			err = true;
		}

		if (ds != null) {
			GridDatatype grid = getGrid();
			if (grid == null)
				return false;
			imgpan.setImageFromGrid(grid);
		}
		return !err;

	}

	public GridDatatype getGrid() {
//	    GeogridBean vb = (GeogridBean) varTable.getSelectedBean();
//	    if (vb == null) {
		List<?> grids = gridDataset.getGrids();
		if (grids.size() > 0)
			return (GridDatatype) grids.get(0);
		else
			return null;
//	    }
//	    return gridDataset.findGridDatatype( vb.getName());
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

			this.gridDataset = new ucar.nc2.dt.grid.GridDataset(ds, parseInfo);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
