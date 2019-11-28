package cict.gaml.extensions.netcdf.file;

import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Formatter;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ucar.ma2.Array;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.image.ImageArrayAdapter;
import ucar.nc2.ui.image.ImageViewPanel;

public class TestNetCDF extends JPanel {

//	String netCDF_File = "E:/ROMS/SENEGAL_Y1980M1.nc";
	String netCDF_File = "E:/tos_O1_2001-2002.nc";
//	String netCDF_File = "E:/test_echam_spectral.nc";
//	String netCDF_File = "E:/cami_0000-09-01_64x128_L26_c030918.nc";
//	String netCDF_File = "C:\\git\\gama.experimental\\cict.gaml.extensions.netcdf\\models\\NetCDF\\includes\\tos_O1_2001-2002.nc";
//	String netCDF_File = "E:/sresa1b_ncar_ccsm3-example.nc";
	private static TestNetCDF tester;
	private static JFrame frame;
	private static ImageViewPanel imgpan;
	NetcdfDataset ds = null;
	private GridDataset gridDataset;
	private int nbgrid = 0;
	private int time = 0;
	private int ntimes = 1;
	boolean forward = true;

	public static void main(final String args[]) throws Exception {
		frame = new JFrame("NetCDF test");
		imgpan = new ImageViewPanel(frame);
		tester = new TestNetCDF();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		frame.getContentPane().add(imgpan);
		frame.pack();
		frame.setBounds(new Rectangle(50, 50, 800, 450));
		frame.setVisible(true);

		tester.process();
		new Thread() {

			@Override
			public void run() {
				BufferedImage img = null;
				do {

					if (tester.forward) {
						tester.time++;
						if (tester.time >= tester.ntimes) {
							tester.nbgrid++;
							tester.time = 0;
						}
					} else {
						tester.time--;
						if (tester.time < 0)
							tester.time = tester.ntimes - 1;
					}

					Array data;
					try {
						data = tester.getGrid().readDataSlice(tester.time, 0, -1, -1);
						img = ImageArrayAdapter.makeGrayscaleImage(data, null);
					} catch (IOException e) {
						e.printStackTrace();
					}

					imgpan.setImage(img);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} while (img != null);
			}

		}.start();
	}

	boolean process() {
		boolean err = false;

		try {
			this.ds = NetcdfDataset.openDataset(netCDF_File, true, null);
			if (this.ds == null) {
				JOptionPane.showMessageDialog(null, "NetcdfDataset.open cant open " + netCDF_File);
				return false;
			}

			this.gridDataset = new ucar.nc2.dt.grid.GridDataset(this.ds, new Formatter());

		} catch (FileNotFoundException ioe) {
			JOptionPane.showMessageDialog(null,
					"NetcdfDataset.open cant open " + netCDF_File + "\n" + ioe.getMessage());
			ioe.printStackTrace();
			err = true;

		} catch (Throwable ioe) {
			ioe.printStackTrace();
			err = true;
		}

		if (ds != null) {
			GridDatatype grid = getGrid();
			if (grid == null)
				return false;
			this.time = 0;
			GridCoordSystem gcsys = grid.getCoordinateSystem();
			if (gcsys.getTimeAxis() != null)
				ntimes = (int) gcsys.getTimeAxis().getSize();

			Array data;
			try {
				data = grid.readDataSlice(this.time, 0, -1, -1);
				imgpan.setImage(ImageArrayAdapter.makeGrayscaleImage(data, null));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return !err;

	}

	public GridDatatype getGrid() {
//	    GeogridBean vb = (GeogridBean) varTable.getSelectedBean();
//	    if (vb == null) {
		List<?> grids = gridDataset.getGrids();
//		System.out.println(grids.size());
		if (nbgrid >= grids.size()) {
			nbgrid = 0;
		}
		if (grids.size() > 0)
			return (GridDatatype) grids.get(nbgrid);// TODO number of the map
		else
			return null;
//	    }
//	    return gridDataset.findGridDatatype( vb.getName());
	}

//	void setDataset(NetcdfDataset newds) {
//		if (newds == null)
//			return;
//		try {
//			if (ds != null)
//				ds.close();
//		} catch (IOException ioe) {
//		}
//
//		Formatter parseInfo = new Formatter();
//		this.ds = newds;
//		try {
//
//			this.gridDataset = new ucar.nc2.dt.grid.GridDataset(ds, parseInfo);
//		} catch (IOException e) {
//			e.printStackTrace();
//			return;
//		}
//	}
}
