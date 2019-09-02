package cict.gaml.extensions.webcam;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;

import msi.gama.metamodel.shape.ILocation;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.IScope;
import msi.gama.util.file.GamaImageFile;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gama.util.matrix.IMatrix;

public class WebcamOperators {

	static VideoCapture webSource = null;
	static Mat frame;
	static MatOfByte mem;
	static CascadeClassifier faceDetector;// = new
											// CascadeClassifier(WebcamOperators.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1));
	static MatOfRect faceDetections;

	private static IMatrix matrixValueFromImage(final IScope scope, final BufferedImage image,
			final ILocation preferredSize) {
		int xSize, ySize;
		BufferedImage resultingImage = image;
		if (preferredSize == null) {
			xSize = image.getWidth();
			ySize = image.getHeight();
		} else {
			xSize = (int) preferredSize.getX();
			ySize = (int) preferredSize.getY();
			resultingImage = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
			final Graphics2D g = resultingImage.createGraphics();
			g.drawImage(image, 0, 0, xSize, ySize, null);
			g.dispose();
			// image = resultingImage;
		}
		final IMatrix matrix = new GamaIntMatrix(xSize, ySize);
		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				matrix.set(scope, i, j, resultingImage.getRGB(i, j));
			}
		}
		return matrix;

	}

	private static String env;

	public static void initEnv(final IScope scope) {
		env = System.getProperty("java.library.path");
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
				scope.getGui().getConsole().informConsole(ex.getMessage(), null);
				ex.printStackTrace();
			}
			// System.out.println(System.getProperty("java.library.path"));
		}
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		// if(System.getenv("R_HOME")==null) {
		// return "missing R_HOME";
		// }
	}

	@operator (
			value = "cam_shot",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "get a photoshot from webcam")
	public static GamaImageFile cam_shot(final IScope scope, final String varName) {
		// Webcam wcam;
		if (scope.getSimulation().getExperiment().getAttribute("webcam") == null) {

			// Dimension size = WebcamResolution.QQVGA.getSize();//.QVGA.getSize();
			//
			// wcam = Webcam.getDefault();
			// wcam.setViewSize(size);
			initEnv(scope);
			webSource = new VideoCapture();
			webSource.open(0);
			scope.getSimulation().getExperiment().setAttribute("webcam", webSource);
		}
		// wcam=(Webcam) scope.getSimulation().getExperiment().getAttribute("webcam");
		webSource = (VideoCapture) scope.getSimulation().getExperiment().getAttribute("webcam");
		// if(!wcam.isOpen()) {

		if (webSource.grab()) {

			scope.getSimulation().postDisposeAction(scope1 -> {
				// Webcam wc=(Webcam) scope.getSimulation().getExperiment().getAttribute("webcam");
				// wc.close();

				final VideoCapture webSource =
						(VideoCapture) scope.getSimulation().getExperiment().getAttribute("webcam");
				webSource.release();
				return null;
			});
			// wcam.open();
		}
		frame = new Mat();
		mem = new MatOfByte();
		faceDetections = new MatOfRect();
		webSource.retrieve(frame);
		// String path="D:\\haarcascade_frontalface_alt.xml";//
		final String path = WebcamOperators.class.getResource("haarcascade_frontalface_alt.xml").getPath().substring(1);
		faceDetector = new CascadeClassifier(path);
		faceDetector.detectMultiScale(frame, faceDetections);

		Highgui.imencode(".bmp", frame, mem);
		Image im;
		try {
			im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
			final BufferedImage buff = (BufferedImage) im;
			for (final Rect rect : faceDetections.toArray()) {
				// System.out.println("ttt");
				Core.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
						new Scalar(0, 255, 0));
				System.out.println(rect.x + " " + rect.width + " " + rect.y + " " + rect.height);
				buff.getGraphics().drawRect(rect.x, rect.y, rect.width, rect.height);
			}
			final GamaImageFile gif = new GamaImageFile(scope, varName, matrixValueFromImage(scope, buff, null));
			return gif;
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return wcam.getImage();
		return null;

	}

}
