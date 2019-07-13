package cict.gaml.extensions.webcam;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

public class Gesture extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final JFrame frame = new JFrame("Hand track");
	private final JLabel lab = new JLabel();
	private static String stringa = "Attendo azione";

	private static Point last = new Point();
	private static boolean close = false;
	private static boolean act = false;
	private static long current = 0;
	private static long prev = 0;
	private static boolean start = false;

	/**
	 * Create the panel.
	 */
	public Gesture() {

	}

	public void setframe(final VideoCapture webcam) {
		frame.setSize(1024, 768);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.getContentPane().add(lab);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				System.out.println("Closed");
				close = true;
				webcam.release();
				e.getWindow().dispose();
			}
		});
	}

	public void frametolabel(final Mat matframe) {
		final MatOfByte cc = new MatOfByte();
		Highgui.imencode(".JPG", matframe, cc);
		final byte[] chupa = cc.toArray();
		final InputStream ss = new ByteArrayInputStream(chupa);
		try {
			final BufferedImage aa = ImageIO.read(ss);
			lab.setIcon(new ImageIcon(aa));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public double calcoladistanza(final Point P1, final Point P2) {
		final double distanza = Math.sqrt((P1.x - P2.x) * (P1.x - P2.x) + (P1.y - P2.y) * (P1.y - P2.y));

		return distanza;
	}

	public double calcolaangolo(final Point P1, final Point P2, final Point P3) {
		double angolo = 0;
		final Point v1 = new Point();
		final Point v2 = new Point();
		v1.x = P3.x - P1.x;
		v1.y = P3.y - P1.y;
		v2.x = P3.x - P2.x;
		v2.y = P3.y - P2.y;
		final double dotproduct = v1.x * v2.x + v1.y * v2.y;
		final double length1 = Math.sqrt(v1.x * v1.x + v1.y * v1.y);
		final double length2 = Math.sqrt(v2.x * v2.x + v2.y * v2.y);
		final double angle = Math.acos(dotproduct / (length1 * length2));
		angolo = angle * 180 / Math.PI;

		return angolo;
	}

	public Mat filtrocolorergb(final int b, final int g, final int r, final int b1, final int g1, final int r1,
			final Mat immagine) {
		final Mat modifica = new Mat();
		if (immagine != null) {
			Core.inRange(immagine, new Scalar(b, g, r), new Scalar(b1, g1, r1), modifica);
		} else {
			System.out.println("Errore immagine");
		}
		return modifica;
	}

	public Mat filtrocolorehsv(final int h, final int s, final int v, final int h1, final int s1, final int v1,
			final Mat immagine) {
		final Mat modifica = new Mat();
		if (immagine != null) {
			Core.inRange(immagine, new Scalar(h, s, v), new Scalar(h1, s1, v1), modifica);
		} else {
			System.out.println("Errore immagine");
		}
		return modifica;
	}

	public Mat skindetction(final Mat orig) {
		final Mat maschera = new Mat();
		final Mat risultato = new Mat();
		Core.inRange(orig, new Scalar(0, 0, 0), new Scalar(30, 30, 30), risultato);
		Imgproc.cvtColor(orig, maschera, Imgproc.COLOR_BGR2HSV);
		for (int i = 0; i < maschera.size().height; i++) {
			for (int j = 0; j < maschera.size().width; j++) {
				if (maschera.get(i, j)[0] < 19
						|| maschera.get(i, j)[0] > 150 && maschera.get(i, j)[1] > 25 && maschera.get(i, j)[1] < 220) {

					risultato.put(i, j, 255, 255, 255);

				} else {
					risultato.put(i, j, 0, 0, 0);
				}
			}

		}

		return risultato;

	}

	public Mat filtromorfologico(final int kd, final int ke, final Mat immagine) {
		final Mat modifica = new Mat();
		Imgproc.erode(immagine, modifica, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(ke, ke)));
		// Imgproc.erode(modifica, modifica, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(ke,ke)));
		Imgproc.dilate(modifica, modifica, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(kd, kd)));
		return modifica;

	}

	public List<MatOfPoint> cercacontorno(final Mat originale, final Mat immagine, final boolean disegna,
			final boolean disegnatutto, final int filtropixel) {
		final List<MatOfPoint> contours = new LinkedList<>();
		final List<MatOfPoint> contoursbig = new LinkedList<>();
		final Mat hierarchy = new Mat();

		Imgproc.findContours(immagine, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE,
				new Point(0, 0));

		for (int i = 0; i < contours.size(); i++) {
			if (contours.get(i).size().height > filtropixel) {
				contoursbig.add(contours.get(i));
				if (disegna && !disegnatutto) {
					Imgproc.drawContours(originale, contours, i, new Scalar(0, 255, 0), 2, 8, hierarchy, 0,
							new Point());
				}
			}

			if (disegnatutto && !disegna) {
				Imgproc.drawContours(originale, contours, i, new Scalar(0, 255, 255), 2, 8, hierarchy, 0, new Point());
			}

		}
		return contoursbig;
	}

	public List<Point> listacontorno(final Mat immagine, final int filtropixel) {
		final List<MatOfPoint> contours = new LinkedList<>();
		final List<MatOfPoint> contoursbig = new LinkedList<>();
		List<Point> listapunti = new LinkedList<>();
		final Mat hierarchy = new Mat();

		Imgproc.findContours(immagine, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE,
				new Point(0, 0));

		for (int i = 0; i < contours.size(); i++) {
			// System.out.println("Dimensione contorni"+contours.get(i).size().height);
			if (contours.get(i).size().height > filtropixel) {
				contoursbig.add(contours.get(i));
			}

		}
		if (contoursbig.size() > 0) {

			listapunti = contoursbig.get(0).toList();

		}
		return listapunti;
	}

	public List<Point> inviluppodifetti(final Mat immagine, final List<MatOfPoint> contours, final boolean disegna,
			final int sogliaprofondita) {
		final List<Point> defects = new LinkedList<>();

		for (int i = 0; i < contours.size(); i++) {
			final MatOfInt hull_ = new MatOfInt();
			final MatOfInt4 convexityDefects = new MatOfInt4();

			@SuppressWarnings ("unused") List<Point> punticontorno = new LinkedList<>();
			punticontorno = contours.get(i).toList();

			Imgproc.convexHull(contours.get(i), hull_);

			if (hull_.size().height >= 4) {

				Imgproc.convexityDefects(contours.get(i), hull_, convexityDefects);

				final List<Point> pts = new ArrayList<>();
				final MatOfPoint2f pr = new MatOfPoint2f();
				Converters.Mat_to_vector_Point(contours.get(i), pts);
				// rettangolo
				pr.create(pts.size(), 1, CvType.CV_32S);
				pr.fromList(pts);
				if (pr.height() > 10) {
					final RotatedRect r = Imgproc.minAreaRect(pr);
					final Point[] rect = new Point[4];
					r.points(rect);

					Core.line(immagine, rect[0], rect[1], new Scalar(0, 100, 0), 2);
					Core.line(immagine, rect[0], rect[3], new Scalar(0, 100, 0), 2);
					Core.line(immagine, rect[1], rect[2], new Scalar(0, 100, 0), 2);
					Core.line(immagine, rect[2], rect[3], new Scalar(0, 100, 0), 2);
					Core.rectangle(immagine, r.boundingRect().tl(), r.boundingRect().br(), new Scalar(50, 50, 50));
				}
				// fine rettangolo

				final int[] buff = new int[4];
				final int[] zx = new int[1];
				final int[] zxx = new int[1];
				for (int i1 = 0; i1 < hull_.size().height; i1++) {
					if (i1 < hull_.size().height - 1) {
						hull_.get(i1, 0, zx);
						hull_.get(i1 + 1, 0, zxx);
					} else {
						hull_.get(i1, 0, zx);
						hull_.get(0, 0, zxx);
					}
					if (disegna) {
						Core.line(immagine, pts.get(zx[0]), pts.get(zxx[0]), new Scalar(140, 140, 140), 2);
					}
				}

				for (int i1 = 0; i1 < convexityDefects.size().height; i1++) {
					convexityDefects.get(i1, 0, buff);
					if (buff[3] / 256 > sogliaprofondita) {
						if (pts.get(buff[2]).x > 0 && pts.get(buff[2]).x < 1024 && pts.get(buff[2]).y > 0
								&& pts.get(buff[2]).y < 768) {
							defects.add(pts.get(buff[2]));
							Core.circle(immagine, pts.get(buff[2]), 6, new Scalar(0, 255, 0));
							if (disegna) {
								Core.circle(immagine, pts.get(buff[2]), 6, new Scalar(0, 255, 0));
							}

						}
					}
				}
				if (defects.size() < 3) {
					final int dim = pts.size();
					Core.circle(immagine, pts.get(0), 3, new Scalar(0, 255, 0), 2);
					Core.circle(immagine, pts.get(0 + dim / 4), 3, new Scalar(0, 255, 0), 2);
					defects.add(pts.get(0));
					defects.add(pts.get(0 + dim / 4));

				}
			}
		}
		return defects;
	}

	public Point centropalmo(final Mat immagine, final List<Point> difetti) {
		final MatOfPoint2f pr = new MatOfPoint2f();
		final Point center = new Point();
		final float[] radius = new float[1];
		pr.create(difetti.size(), 1, CvType.CV_32S);
		pr.fromList(difetti);

		if (pr.size().height > 0) {
			start = true;
			Imgproc.minEnclosingCircle(pr, center, radius);

			// Core.circle(immagine, center,(int) radius[0], new Scalar(255,0,0));
			// Core.circle(immagine, center, 3, new Scalar(0,0,255),4);
		} else {
			start = false;
		}
		return center;

	}

	public List<Point> dita(final Mat immagine, final List<Point> punticontorno, final Point center) {
		final List<Point> puntidita = new LinkedList<>();
		final List<Point> dita = new LinkedList<>();
		final int intervallo = 55;
		for (int j = 0; j < punticontorno.size(); j++) {
			Point prec = new Point();
			Point vertice = new Point();
			Point next = new Point();
			vertice = punticontorno.get(j);
			if (j - intervallo > 0) {

				prec = punticontorno.get(j - intervallo);
			} else {
				final int a = intervallo - j;
				prec = punticontorno.get(punticontorno.size() - a - 1);
			}
			if (j + intervallo < punticontorno.size()) {
				next = punticontorno.get(j + intervallo);
			} else {
				final int a = j + intervallo - punticontorno.size();
				next = punticontorno.get(a);
			}

			final Point v1 = new Point();
			final Point v2 = new Point();
			v1.x = vertice.x - next.x;
			v1.y = vertice.y - next.y;
			v2.x = vertice.x - prec.x;
			v2.y = vertice.y - prec.y;
			final double dotproduct = v1.x * v2.x + v1.y * v2.y;
			final double length1 = Math.sqrt(v1.x * v1.x + v1.y * v1.y);
			final double length2 = Math.sqrt(v2.x * v2.x + v2.y * v2.y);
			double angle = Math.acos(dotproduct / (length1 * length2));
			angle = angle * 180 / Math.PI;
			if (angle < 60) {
				final double centroprec = Math
						.sqrt((prec.x - center.x) * (prec.x - center.x) + (prec.y - center.y) * (prec.y - center.y));
				final double centrovert = Math.sqrt((vertice.x - center.x) * (vertice.x - center.x)
						+ (vertice.y - center.y) * (vertice.y - center.y));
				final double centronext = Math
						.sqrt((next.x - center.x) * (next.x - center.x) + (next.y - center.y) * (next.y - center.y));
				if (centroprec < centrovert && centronext < centrovert) {

					puntidita.add(vertice);
					// Core.circle(immagine, vertice, 2, new Scalar(200,0,230));

					// Core.line(immagine, vertice, center, new Scalar(0,255,255));
				}
			}
		}

		final Point media = new Point();
		media.x = 0;
		media.y = 0;
		int med = 0;
		boolean t = false;
		if (puntidita.size() > 0) {
			final double dif = Math.sqrt((puntidita.get(0).x - puntidita.get(puntidita.size() - 1).x)
					* (puntidita.get(0).x - puntidita.get(puntidita.size() - 1).x)
					+ (puntidita.get(0).y - puntidita.get(puntidita.size() - 1).y)
							* (puntidita.get(0).y - puntidita.get(puntidita.size() - 1).y));
			if (dif <= 20) {
				t = true;
			}
		}
		for (int i = 0; i < puntidita.size() - 1; i++) {

			final double d = Math.sqrt((puntidita.get(i).x - puntidita.get(i + 1).x)
					* (puntidita.get(i).x - puntidita.get(i + 1).x)
					+ (puntidita.get(i).y - puntidita.get(i + 1).y) * (puntidita.get(i).y - puntidita.get(i + 1).y));

			if (d > 20 || i + 1 == puntidita.size() - 1) {
				final Point p = new Point();

				p.x = (int) (media.x / med);
				p.y = (int) (media.y / med);

				// if(p.x>0 && p.x<1024 && p.y<768 && p.y>0){

				dita.add(p);
				// }

				if (t && i + 1 == puntidita.size() - 1) {
					final Point ult = new Point();
					if (dita.size() > 1) {
						ult.x = (dita.get(0).x + dita.get(dita.size() - 1).x) / 2;
						ult.y = (dita.get(0).y + dita.get(dita.size() - 1).y) / 2;
						dita.set(0, ult);
						dita.remove(dita.size() - 1);
					}
				}
				med = 0;
				media.x = 0;
				media.y = 0;
			} else {

				media.x = media.x + puntidita.get(i).x;
				media.y = media.y + puntidita.get(i).y;
				med++;

			}
		}

		return dita;
	}

	public void disegnaditacentropalmo(final Mat immagine, final Point center, final Point dito,
			final List<Point> dita) {

		Core.line(immagine, new Point(150, 50), new Point(730, 50), new Scalar(255, 0, 0), 2);
		Core.line(immagine, new Point(150, 380), new Point(730, 380), new Scalar(255, 0, 0), 2);
		Core.line(immagine, new Point(150, 50), new Point(150, 380), new Scalar(255, 0, 0), 2);
		Core.line(immagine, new Point(730, 50), new Point(730, 380), new Scalar(255, 0, 0), 2);
		if (dita.size() == 1) {
			Core.line(immagine, center, dito, new Scalar(0, 255, 255), 4);
			Core.circle(immagine, dito, 3, new Scalar(255, 0, 255), 3);
			// Core.putText(immagine, dito.toString(), dito, Core.FONT_HERSHEY_COMPLEX, 1, new Scalar(0,200,255));

		} else {
			for (int i = 0; i < dita.size(); i++) {
				Core.line(immagine, center, dita.get(i), new Scalar(0, 255, 255), 4);
				Core.circle(immagine, dita.get(i), 3, new Scalar(255, 0, 255), 3);
			}
		}
		Core.circle(immagine, center, 3, new Scalar(0, 0, 255), 3);
		// Core.putText(immagine, center.toString(), center, Core.FONT_HERSHEY_COMPLEX, 1, new Scalar(0,200,255));

	}

	public void mousetrack(final List<Point> dita, final Point dito, final Point centro, final Robot r,
			final boolean on, final Mat immagine, final long temp) throws InterruptedException {

		if (on && centro.x > 10 && centro.y > 10 && dito.x > 10 && centro.y > 10 && start) {
			current = temp;
			switch (dita.size()) {
				case 0:
					if (act && current - prev > 500) {
						stringa = "Drag & drop";
						r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
						act = false;
					} else {
						if (current - prev > 500) {
							final Point p = new Point();
							final Point np = new Point();
							np.x = centro.x - last.x;
							np.y = centro.y - last.y;
							p.x = (int) (-1 * (np.x - 730)) * 1366 / 580;
							p.y = (int) (np.y - 50) * 768 / 330;
							if (p.x > 0 && p.x > 0 && p.x < 1367 && p.y < 769) {
								r.mouseMove((int) p.x, (int) p.y);
							}

						}
					}
					break;
				case 1:

					if (act && current - prev > 500) {
						stringa = "Click";
						System.out.println("click");
						r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
						r.mousePress(InputEvent.BUTTON1_DOWN_MASK);

						r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

						r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
						System.out.println("rilascio");

						act = false;
					} else {
						if (current - prev > 500) {
							stringa = "Puntatore";

							final Point p1 = new Point();
							p1.x = (int) (-1 * (dito.x - 730)) * 1366 / 580;
							p1.y = (int) (dito.y - 50) * 768 / 330;
							if (p1.x > 0 && p1.x > 0 && p1.x < 1367 && p1.y < 769) {
								r.mouseMove((int) p1.x, (int) p1.y);
							}
							last.x = centro.x - dito.x;
							last.y = centro.y - dito.y;
						}
					}
					break;
				case 2:
					final double angolo = calcolaangolo(dita.get(0), dita.get(1), centro);
					r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
					r.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
					if (act && current - prev > 500) {
						act = false;
						if ((int) angolo < 30) {
							stringa = "Doppio click";
							System.out.println("doppio click");
							r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							r.delay(100);
							r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
						} else {
							stringa = "Tasto destro";
							r.mousePress(InputEvent.BUTTON3_DOWN_MASK);
							r.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
						}

					}
					break;
				case 3:
					stringa = "Annulla";
					act = false;
					break;
				case 4:
					stringa = "Blocco puntatore: attendo azione!";
					r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

					prev = temp;
					act = true;

					break;

				case 5:
					stringa = "Blocco puntatore: attendo azione!";
					if (!act) {
						r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

						prev = temp;
						act = true;
					}
					break;
				default:
					stringa = "Attendo azione!";

					break;
			}

		} else {
			r.mouseRelease(InputEvent.BUTTON1_MASK);
		}
		Core.putText(immagine, stringa, new Point(50, 40), Core.FONT_HERSHEY_COMPLEX, 1, new Scalar(200, 0, 0));

	}

	public Point filtromediamobile(final List<Point> buffer, final Point attuale) {
		final Point media = new Point();
		media.x = 0;
		media.y = 0;
		for (int i = buffer.size() - 1; i > 0; i--) {
			buffer.set(i, buffer.get(i - 1));
			media.x = media.x + buffer.get(i).x;
			media.y = media.y + buffer.get(i).y;
		}
		buffer.set(0, attuale);
		media.x = (media.x + buffer.get(0).x) / buffer.size();
		media.y = (media.y + buffer.get(0).y) / buffer.size();
		return media;
	}

	public static void main(final String[] args) throws InterruptedException, AWTException {

		final String env = System.getProperty("java.library.path");
		if (!env.contains("opencv_java2413")) {
			final String opencv_path = "C:\\git\\gama.experimental\\cict.gaml.extensions.VR\\lib\\x64";// \\opencv_java2413.dll
			// String opencv_path = "E:\\Downloads\\Programs\\ocv\\opencv\\build\\java\\x86";//\\opencv_java2413.dll
			if (System.getProperty("os.name").startsWith("Windows")) {
				System.setProperty("java.library.path", opencv_path + ";" + env);
			} else {
				System.setProperty("java.library.path", opencv_path + ":" + env);
			}
			try {
				final java.lang.reflect.Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
				fieldSysPath.setAccessible(true);
				fieldSysPath.set(null, null);
				// System.loadLibrary("jri");

			} catch (final Exception ex) {
				ex.printStackTrace();
			}
			// System.out.println(System.getProperty("java.library.path"));
		}
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		final Gesture v = new Gesture();
		final VideoCapture webcam = new VideoCapture(0);
		webcam.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 768);
		webcam.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 1024);
		v.setframe(webcam);
		final Robot r = new Robot();
		final Mat mimm = new Mat();
		Mat modifica = new Mat();
		Point centro = new Point();
		Point dito = new Point();
		final List<Point> buffer = new LinkedList<>();
		final List<Point> bufferdita = new LinkedList<>();
		List<Point> dita = new LinkedList<>();
		long temp = 0;

		while (true && !close) {

			if (!webcam.isOpened() && !close) {
				System.out.println("Camera Error");
			} else {
				List<Point> difetti = new LinkedList<>();
				if (!close) {
					temp = System.currentTimeMillis();
					webcam.retrieve(mimm);
					// modifica = v.filtromorfologico(2, 7, v.filtrocolorergb(0, 0, 0, 40, 40, 40, mimm));
					modifica = v.filtromorfologico(2, 7, v.filtrocolorehsv(0, 0, 0, 180, 255, 40, mimm));

					difetti = v.inviluppodifetti(mimm, v.cercacontorno(mimm, modifica, false, false, 450), false, 5);

					if (buffer.size() < 7) {
						buffer.add(v.centropalmo(mimm, difetti));
					} else {
						centro = v.filtromediamobile(buffer, v.centropalmo(mimm, difetti));
						// System.out.println((int)centro.x+" "+(int)centro.y+" "+(int)v.centropalmo(mimm,difetti).x+"
						// "+(int)v.centropalmo(mimm,difetti).y);
					}

					dita = v.dita(mimm, v.listacontorno(modifica, 200), centro);

					if (dita.size() == 1 && bufferdita.size() < 5) {
						bufferdita.add(dita.get(0));
						dito = dita.get(0);
					} else {
						if (dita.size() == 1) {
							dito = v.filtromediamobile(bufferdita, dita.get(0));
							// System.out.println((int)dito.x +" "+(int)dito.y+" "+(int)dita.get(0).x+"
							// "+(int)dita.get(0).y);
						}
					}

					v.disegnaditacentropalmo(mimm, centro, dito, dita);

					v.mousetrack(dita, dito, centro, r, true, mimm, temp);

					v.frametolabel(mimm);

				}
			}

		}

	}
}
