package cict.gaml.extensions.webcam;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import msi.gama.metamodel.shape.ILocation;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.IScope;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gama.util.matrix.IMatrix;

public class WebcamOperators {

	private static IMatrix matrixValueFromImage(final IScope scope, final BufferedImage image, final ILocation preferredSize) {
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


	@operator(value = "cam_shot", can_be_const = false, category = IOperatorCategory.LIST)
	@doc(value = "get a photoshot from webcam")
	public static IMatrix cam_shot(final IScope scope, final String varName) {
		Webcam wcam;
		if(scope.getSimulation().getExperiment().getAttribute("webcam") == null) {

			Dimension size = WebcamResolution.QQVGA.getSize();//.QVGA.getSize();

			wcam = Webcam.getDefault();
			wcam.setViewSize(size);
			scope.getSimulation().getExperiment().setAttribute("webcam",wcam);
		}
		wcam=(Webcam) scope.getSimulation().getExperiment().getAttribute("webcam");
		if(!wcam.isOpen()) {


			scope.getSimulation().postDisposeAction(scope1 -> {
				Webcam wc=(Webcam) scope.getSimulation().getExperiment().getAttribute("webcam");
				wc.close();
				
				return null;
			});
			wcam.open();
		}
		return  matrixValueFromImage(scope, wcam.getImage(), null);
//		return wcam.getImage();
		
	}
	
}
