/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package across.gaml.extensions.imageanalysis.boofcv;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.List;

import org.ejml.data.DMatrixRMaj;
import org.ejml.ops.ConvertMatrixData;

import boofcv.abst.distort.FDistort;
import boofcv.abst.geo.Estimate1ofEpipolar;
import boofcv.alg.enhance.GEnhanceImageOps;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.interpolate.InterpolationType;
import boofcv.alg.misc.ImageStatistics;
import boofcv.factory.geo.FactoryMultiView;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.ConfigLength;
import boofcv.struct.ConnectRule;
import boofcv.struct.geo.AssociatedPair;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.homography.Homography2D_F32;
import georegression.struct.point.Point2D_F64;

/**
 * Class which simplifies the removal of perspective distortion from a region inside an image. Given the ordered
 * corners of a quadrilateral in the input image it applies a homography transform which reprojects the region
 * into the input image into a rectangular output image.
 *
 * @author Peter Abeles
 */
public class RemovePerspectiveDistortion<T extends ImageBase<T>> {
	FDistort distort;

	// computes the homography
	Estimate1ofEpipolar computeHomography = FactoryMultiView.homographyDLT(true);

//	RefineEpipolar refineHomography = FactoryMultiView.refineHomography(1e-8,20, EpipolarError.SIMPLE);

	// storage for computed homography
	DMatrixRMaj H = new DMatrixRMaj(3, 3);
	Homography2D_F32 H32 = new Homography2D_F32();
	//	DMatrixRMaj Hrefined = new DMatrixRMaj(3,3);
	// transform which applies the homography
	PointTransformHomography_F32 transform = new PointTransformHomography_F32();

	// storage for associated points between the two images
	ArrayList<AssociatedPair> associatedPairs = new ArrayList<>();

	// input and output images
	T output;

	/**
	 * Constructor which specifies the characteristics of the undistorted image
	 *
	 * @param width Width of undistorted image
	 * @param height Height of undistorted image
	 * @param imageType Type of undistorted image
	 */
	public RemovePerspectiveDistortion( int width, int height, ImageType<T> imageType ) {
		this(width, height);
		output = imageType.createImage(width, height);
		distort = new FDistort(imageType);
		distort.output(output);
		distort.interp(InterpolationType.BILINEAR).transform(transform);
	}

	/**
	 * Creates the variables for computing the transform but not rendering the image
	 *
	 * @param width Width of undistorted image
	 * @param height Height of undistorted image
	 */
	public RemovePerspectiveDistortion( int width, int height ) {
		for (int i = 0; i < 4; i++) {
			associatedPairs.add(new AssociatedPair());
		}

		associatedPairs.get(0).p1.setTo(0, 0);
		associatedPairs.get(1).p1.setTo(width, 0);
		associatedPairs.get(2).p1.setTo(width, height);
		associatedPairs.get(3).p1.setTo(0, height);
	}

	/**
	 * Applies distortion removal to the specified region in the input image. The undistorted image is returned.
	 *
	 * @param input Input image
	 * @param corner0 Top left corner
	 * @param corner1 Top right corner
	 * @param corner2 Bottom right corner
	 * @param corner3 Bottom left corner
	 * @return true if successful or false if it failed
	 */
	public boolean apply( T input,
						  Point2D_F64 corner0, Point2D_F64 corner1,
						  Point2D_F64 corner2, Point2D_F64 corner3 ) {
		if (createTransform(corner0, corner1, corner2, corner3)) {
			distort.input(input).apply();

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Compues the distortion removal transform
	 *
	 * @param tl Top left corner
	 * @param tr Top right corner
	 * @param br Bottom right corner
	 * @param bl Bottom left corner
	 * @return true if successful or false if it failed
	 */
	public boolean createTransform( Point2D_F64 tl, Point2D_F64 tr,
									Point2D_F64 br, Point2D_F64 bl ) {
		associatedPairs.get(0).p2.setTo(tl);
		associatedPairs.get(1).p2.setTo(tr);
		associatedPairs.get(2).p2.setTo(br);
		associatedPairs.get(3).p2.setTo(bl);

		if (!computeHomography.process(associatedPairs, H))
			return false;

//		if( !refineHomography.fitModel(associatedPairs,H,Hrefined) ) {
//			return false;
//		}
//		homography.set(Hrefined);

		ConvertMatrixData.convert(H, H32);
		transform.set(H32);

		return true;
	}

	public DMatrixRMaj getH() {
		return H;
	}

	public PointTransformHomography_F32 getTransform() {
		return transform;
	}

	/**
	 * Returns the undistorted output image
	 */
	public T getOutput() {
		return output;
	}
	
	
	public static void main(String[] args) {
		
		
		BufferedImage orig = UtilImageIO.loadImageNotNull("C:\\Users\\admin_ptaillandie\\Documents\\GitHub\\gama.experimental\\across.gaml.extensions.imageanalysis\\src\\across\\gaml\\extensions\\imageanalysis\\operators\\real_map_sharpened.jpg");
		/*Planar<GrayF32> input = ConvertBufferedImage.convertFromPlanar(orig, null, true, GrayF32.class);

		RemovePerspectiveDistortion<Planar<GrayF32>> removePerspective =
				new RemovePerspectiveDistortion<>(500, 500, ImageType.pl(3, GrayF32.class));

		// Specify the corners in the input image of the region.
		// Order matters! top-left, top-right, bottom-right, bottom-left
		if (!removePerspective.apply(input,
				new Point2D_F64(97, 439), new Point2D_F64(1082, 419),
				new Point2D_F64(1111, 1400), new Point2D_F64(126, 1426))) {
			throw new RuntimeException("Failed!?!?");
		}

		
		BufferedImage flat = ConvertBufferedImage.convertTo_F32(output, null, true);
		GrayF32 input_ = ConvertBufferedImage.convertFromSingle(flat, null, GrayF32.class);
		
		var binary = new GrayU8(output.width, output.height);

		Planar<GrayU8> color = ConvertBufferedImage.convertFrom(flat, true, ImageType.PL_U8);
		Planar<GrayU8> adjusted = color.createSameShape();

		
		ShowImages.showWindow(orig, "Original Image", true);
		ShowImages.showWindow(flat, "Without Perspective Distortion", true);*/
		GrayF32 input_ = ConvertBufferedImage.convertFromSingle(orig, null, GrayF32.class);
		var binary = new GrayU8(input_.width, input_.height);

		
		
		
		// Display multiple images in the same window
				var gui = new ListDisplayPanel();

		// Global Methods
		GThresholdImageOps.threshold(input_, binary, ImageStatistics.mean(input_), true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Global: Mean");
		GThresholdImageOps.threshold(input_, binary, GThresholdImageOps.computeOtsu(input_, 0, 255), true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Global: Otsu");
		GThresholdImageOps.threshold(input_, binary, GThresholdImageOps.computeEntropy(input_, 0, 255), true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Global: Entropy");

		// Local method
		GThresholdImageOps.localMean(input_, binary, ConfigLength.fixed(57), 1.0, true, null, null, null);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: Mean");
		GThresholdImageOps.localGaussian(input_, binary, ConfigLength.fixed(85), 1.0, true, null, null);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: Gaussian");
		GThresholdImageOps.localNiblack(input_, binary, ConfigLength.fixed(11), 0.30f, true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: Niblack");
		GThresholdImageOps.localSauvola(input_, binary, ConfigLength.fixed(11), 0.30f, true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: Sauvola");
		GThresholdImageOps.localWolf(input_, binary, ConfigLength.fixed(11), 0.30f, true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: Wolf");
		GThresholdImageOps.localNick(input_, binary, ConfigLength.fixed(11), -0.2f, true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: NICK");
		GThresholdImageOps.blockMinMax(input_, binary, ConfigLength.fixed(21), 1.0, true, 15);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Block: Min-Max");
		GThresholdImageOps.blockMean(input_, binary, ConfigLength.fixed(21), 1.0, true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Block: Mean");
		GThresholdImageOps.blockOtsu(input_, binary, false, ConfigLength.fixed(21), 0.5, 1.0, true);
		gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Block: Otsu");

		// Sauvola is tuned for text image. Change radius to make it run better in others.

		// Show the image image for reference
		gui.addImage(ConvertBufferedImage.convertTo(input_, null), "Input Image");
		
		

		
		String imageName = "ttt";
		String fileName = imageName.substring(imageName.lastIndexOf('/') + 1);
		
		
		
		// convert into a usable format
				GrayF32 input = ConvertBufferedImage.convertFromSingle(orig, null, GrayF32.class);
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

				gui.addImage(visualBinary, "Binary Original");
				gui.addImage(visualFiltered, "Binary Filtered");
				gui.addImage(visualLabel, "Labeled Blobs");
				gui.addImage(visualContour, "Contours");
			
		
		
		ShowImages.showWindow(gui, fileName);
		
		
		
	}

}


