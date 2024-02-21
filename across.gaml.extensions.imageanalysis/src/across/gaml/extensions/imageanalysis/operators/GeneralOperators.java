package across.gaml.extensions.imageanalysis.operators;

import java.awt.image.BufferedImage;
import java.util.List;

import boofcv.alg.feature.detect.edge.CannyEdge;
import boofcv.alg.feature.detect.edge.EdgeContour;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import georegression.struct.point.Point2D_I32;
import gama.core.metamodel.shape.GamaPoint;
import gama.core.metamodel.shape.IShape;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.core.runtime.IScope;
import gama.core.util.GamaListFactory;
import gama.core.util.IList;
import gama.core.util.matrix.GamaIntMatrix;
import gama.core.util.matrix.IMatrix;
import gama.experimental.webcam.operators.WebcamOperators;
import gama.gaml.operators.Spatial.Creation;
import gama.gaml.types.IType;

public class GeneralOperators {

	   @operator (
				value = "remove_perspective",
				can_be_const = false,
				category = IOperatorCategory.LIST)
		@doc (
				value = "remove the perspective from an image using 4 reference points (top-left, top-right, bottom-right, bottom-left)")
		public static IMatrix removePerspective(final IScope scope, final IMatrix inputImage, IList<GamaPoint> map_corners)  {
	    	return WebcamOperators.matrixValueFromImage(scope, PatternMatching.removeDistortion(scope, map_corners,GamaIntMatrix.constructBufferedImageFromMatrix(scope, inputImage)));
	   }
	   
	   @operator (
				value = "to_binary_image",
				can_be_const = false,
				category = IOperatorCategory.LIST)
		@doc (
				value = "remove the perspective from an image using 4 reference points (top-left, top-right, bottom-right, bottom-left)")
		public static IMatrix removePerspective(final IScope scope,  final IMatrix inputImage, final double threshold)  {
	    	BufferedImage buf = GamaIntMatrix.constructBufferedImageFromMatrix(scope, inputImage);
	    	GrayF32 input = ConvertBufferedImage.convertFromSingle(buf, null, GrayF32.class);
			var binary = new GrayU8(input.width, input.height);
			
			
			ThresholdImageOps.threshold(input, binary, (float)threshold, true);
			GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
			filtered = BinaryImageOps.dilate8(filtered, 1, null);
			return WebcamOperators.matrixValueFromImage(scope, VisualizeBinaryData.renderBinary(filtered, false, null));
	   }
	   
	   @operator (
				value = "huang_threshold",
				can_be_const = false,
				category = IOperatorCategory.LIST)
		@doc (
				value = "Compute the Huang Threshold from the image")
		public static Double computeHuang(final IScope scope,  final IMatrix inputImage)  {
	    	BufferedImage buf = GamaIntMatrix.constructBufferedImageFromMatrix(scope, inputImage);
	    	GrayF32 input = ConvertBufferedImage.convertFromSingle(buf, null, GrayF32.class);
			return GThresholdImageOps.computeHuang(input, 0, 255) ;
	   }
	   
	   @operator (
				value = "entropy_threshold",
				can_be_const = false,
				category = IOperatorCategory.LIST)
		@doc (
				value = "Compute the Entropy Threshold from the image")
		public static Double computeEntropy(final IScope scope,  final IMatrix inputImage)  {
	    	BufferedImage buf = GamaIntMatrix.constructBufferedImageFromMatrix(scope, inputImage);
	    	GrayF32 input = ConvertBufferedImage.convertFromSingle(buf, null, GrayF32.class);
			return GThresholdImageOps.computeEntropy(input, 0, 255) ;
	   }
	   
	   @operator (
				value = "otsu_threshold",
				can_be_const = false,
				category = IOperatorCategory.LIST)
		@doc (
				value = "Compute the Otsu Threshold from the image")
		public static Double computeOtsu(final IScope scope,  final IMatrix inputImage)  {
	    	BufferedImage buf = GamaIntMatrix.constructBufferedImageFromMatrix(scope, inputImage);
	    	GrayF32 input = ConvertBufferedImage.convertFromSingle(buf, null, GrayF32.class);
			return GThresholdImageOps.computeOtsu(input, 0, 255) ;
	   }
	   
	 
	   @operator (
				value = "li_threshold",
				can_be_const = false,
				category = IOperatorCategory.LIST)
		@doc (
				value = "Compute the Li Threshold from the image")
		public static Double computeLi(final IScope scope,  final IMatrix inputImage)  {
	    	BufferedImage buf = GamaIntMatrix.constructBufferedImageFromMatrix(scope, inputImage);
	    	GrayF32 input = ConvertBufferedImage.convertFromSingle(buf, null, GrayF32.class);
			return GThresholdImageOps.computeLi(input, 0, 255) ;
	   }
	   
	   @operator (
				value = "to_binary_image",
				can_be_const = false,
				category = IOperatorCategory.LIST)
		@doc (
				value = "return a binary image using the given threshold")
		public static IMatrix toBinaryImage(final IScope scope,  final IMatrix inputImage, Double threshold)  {
	    	BufferedImage buf = GamaIntMatrix.constructBufferedImageFromMatrix(scope, inputImage);
	    	GrayF32 input = ConvertBufferedImage.convertFromSingle(buf, null, GrayF32.class);
			var binary = new GrayU8(input.width, input.height);
			ThresholdImageOps.threshold(input, binary, threshold.floatValue(), true);
			GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
			filtered = BinaryImageOps.dilate8(filtered, 1, null);
			return WebcamOperators.matrixValueFromImage(scope, VisualizeBinaryData.renderBinary(filtered, false, null));
	   }
	   
	   
	   @operator (
				value = "detect_contours",
				can_be_const = false,
				category = IOperatorCategory.LIST)
		@doc (
				value = "returns vector lines from the image")
	   public static IMatrix tes1(final IScope scope,  final IMatrix inputImage)  {
		   BufferedImage buf = GamaIntMatrix.constructBufferedImageFromMatrix(scope, inputImage);
		   GrayF32 input = ConvertBufferedImage.convertFromSingle(buf, null, GrayF32.class);
			var binary = new GrayU8(input.width, input.height);
			var label = new GrayS32(input.width, input.height);

			// Select a global threshold using Otsu's method.
			double threshold = GThresholdImageOps.computeOtsu(input, 0, 255);

			// Apply the threshold to create a binary image
			ThresholdImageOps.threshold(input, binary, (float)threshold, true);

			// remove small blobs through erosion and dilation
			// The null in the input indicates that it should internally declare the work image it needs
			// this is less efficient, but easier to code.
			GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
			filtered = BinaryImageOps.dilate8(filtered, 1, null);

			// Detect blobs inside the image using an 8-connect rule
			List<Contour> contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT, label);

			// colors of contours
			int colorExternal = 0xFFFFFF;
			int colorInternal = 0xFF2020;

			// display the results
			BufferedImage visualBinary = VisualizeBinaryData.renderBinary(binary, false, null);
			BufferedImage visualFiltered = VisualizeBinaryData.renderBinary(filtered, false, null);
			BufferedImage visualLabel = VisualizeBinaryData.renderLabeledBG(label, contours.size(), null);
			BufferedImage visualContour = VisualizeBinaryData.renderContours(contours, colorExternal, colorInternal,
					input.width, input.height, null);

			return WebcamOperators.matrixValueFromImage(scope, visualContour);
	   }	
	   
	   
	   @operator (
				value = "vectorize",
				can_be_const = false,
				content_type = IType.GEOMETRY,
				category = IOperatorCategory.LIST)
		@doc (
				value = "returns vector lines from the image")
		public static IList<IShape> vectorizeImage(final IScope scope,  final IMatrix inputImage)  {
		   IList<IShape> lines = GamaListFactory.create();
		   BufferedImage buf = GamaIntMatrix.constructBufferedImageFromMatrix(scope, inputImage);
			   GrayF32 input = ConvertBufferedImage.convertFromSingle(buf, null, GrayF32.class);
			 GrayU8 binary = new GrayU8(input.width, input.height);
			BufferedImage polygon = new BufferedImage(input.width, input.height, BufferedImage.TYPE_INT_RGB);

			// the mean pixel value is often a reasonable threshold when creating a binary image
			double mean = ImageStatistics.mean(input);

			// create a binary image by thresholding
			ThresholdImageOps.threshold(input, binary, (float)mean, true);

			// reduce noise with some filtering
			GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
			filtered = BinaryImageOps.dilate8(filtered, 1, null);

			// Find internal and external contour around each shape
			List<Contour> contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT, null);

			double coeffX = scope.getSimulation().getWidth() / inputImage.getCols(scope);
			double coeffY = scope.getSimulation().getHeight() / inputImage.getRows(scope);
			for (Contour c : contours) {
				IList<IShape> pts = GamaListFactory.create();
				for (Point2D_I32 pt : c.external) {
					pts.add(new GamaPoint(pt.x * coeffX, pt.y * coeffY) );
				}
				if (! pts.isEmpty()) {
					lines.add(Creation.line(scope, pts));
				}
				
				for (List<Point2D_I32> ppt : c.internal) {
					IList<IShape> pts2= GamaListFactory.create();
					for (Point2D_I32 pt :ppt) {
						pts2.add(new GamaPoint(pt.x * coeffX, pt.y * coeffY) );
					}
					if (! pts2.isEmpty()) 
						lines.add(Creation.line(scope, pts2));
				}
				
			}
		   return lines;
	   }
	  
}
