package miat.gama.extension.qrcode.operators;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import msi.gama.precompiler.IOperatorCategory;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.file.GamaImageFile;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gama.util.matrix.IMatrix;

public class Operators {
	
	static Webcam webcam;
	static Integer webcam_id;
	
	@operator (
			value = "cam_shot",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "get a photoshot from the default webcam")
	public static GamaImageFile cam_shot(final IScope scope, final String filepath) {
		return cam_shot(scope, filepath, null, null,null);
	}
	
	@operator (
			value = "cam_shot",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "get a photoshot from the default webcam, with the given resolution (width, height) in pixels")
	public static GamaImageFile cam_shot(final IScope scope, final String filepath, final Integer width, final Integer height) {
		return cam_shot(scope, filepath, width, height, null);
	}
	
	@operator (
			value = "cam_shot",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "get a photoshot with the given resolution (width, height) in pixels from the given webcam")
	public static GamaImageFile cam_shot(final IScope scope, final String filepath, final Integer width, final Integer height, Integer webcamid) {
		BufferedImage im = CamShotAct(scope, width, height, webcamid);
		return new GamaImageFile(scope, filepath, matrixValueFromImage(scope, im)); 
	}
	
	private static BufferedImage CamShotAct(final IScope scope,final Integer width, final Integer height, Integer webcamId) {
		long mt = System.currentTimeMillis();
		Webcam.setAutoOpenMode(true);
		if ((webcamId != null) && (webcamId >= Webcam.getWebcams().size())) {
			webcamId = 0;
		} 
		if ((webcam_id != null) && (webcam_id != webcamId)){
			webcam_id = webcamId;
			
			if (webcam != null) {
				//webcam.close();
				webcam = null;
			}
		}
		if (webcam == null) {
			if ((webcamId != null) ) {
				webcam = Webcam.getWebcams().get(webcamId);
				webcam_id = webcamId;
			} else {
				webcam = Webcam.getDefault();
				webcam_id = 0;
			}
			//webcam.open();
			webcam.getLock().disable();
		}
		if (webcam == null) {
			GamaRuntimeException.error("No webcam detected", scope);
		}
		System.out.println("lalal2: " + (System.currentTimeMillis() - mt));
		mt = System.currentTimeMillis();
		if (width != null && height != null)  {
			Dimension dim = new Dimension(width, height);
			if (!webcam.getViewSize().equals(dim)) {
				//webcam.close();
				boolean nonStandard = true;
				for (int i = 0; i < webcam.getViewSizes().length; i++) {
					if (webcam.getViewSizes()[i].equals(dim)) {
						nonStandard = false;
						break;
					}
				}
				if (nonStandard) {
					Dimension[] nonStandardResolutions = new Dimension[] {dim};
					webcam.setCustomViewSizes(nonStandardResolutions);
				}
				webcam.setViewSize(dim);
				
				//webcam.open();
			}

		}
		BufferedImage bim = (BufferedImage) webcam.getImage(); 
		return bim;
	}
	
	private static IMatrix matrixValueFromImage(final IScope scope, final BufferedImage image) {
		int xSize, ySize;
		BufferedImage resultingImage = image;
		xSize = image.getWidth();
		ySize = image.getHeight();
		final IMatrix matrix = new GamaIntMatrix(xSize, ySize);
		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				matrix.set(scope, i, j, resultingImage.getRGB(i, j));
			}
		}
		return matrix;
	}
	
	
	
	@operator (
			value = "decodeQR",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "decode a QR code from a webcam a photoshot from a photoshot with the given resolution (width, height) in pixels from the given webcam")
	public static String decodeQRcode(final IScope scope, final Integer width, final Integer height,final int idWebcam )  {
		final BufferedImage tmpBfrImage = CamShotAct(scope, width, height, idWebcam);
		if (tmpBfrImage == null)
			GamaRuntimeException.error("Could not decode the image", scope);
		LuminanceSource tmpSource = new BufferedImageLuminanceSource(tmpBfrImage);
		BinaryBitmap tmpBitmap = new BinaryBitmap(new HybridBinarizer(tmpSource));
		MultiFormatReader tmpBarcodeReader = new MultiFormatReader();
		Result tmpResult;   
		String tmpFinalResult = "";
		try {
			Map<DecodeHintType,Object> tmpHintsMap = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
			tmpHintsMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
			tmpHintsMap.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
			tmpHintsMap.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
			
			tmpResult = tmpBarcodeReader.decode(tmpBitmap);
			tmpFinalResult = String.valueOf(tmpResult.getText());
		} catch (Exception tmpExcpt) {
			GamaRuntimeException.error("BarCodeUtil.decode Excpt err - " + tmpExcpt.toString() + " - " + tmpExcpt.getMessage(), scope);
		}
		return tmpFinalResult;
	}
	
		
	@operator (
			value = "decodeQRFile",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "decode the QR code from an image file")
	public static String decodeQRcodeFile(final IScope scope, final String file_path)  {
		File whatFile = new File(file_path);
		// check the required parameters 
		if (whatFile == null || whatFile.getName().trim().isEmpty())
			GamaRuntimeException.error("Problem when reading file " + file_path, scope);
			
		BufferedImage tmpBfrImage = null;
		try {
			tmpBfrImage = ImageIO.read(whatFile);
		} catch (IOException tmpIoe) {
			GamaRuntimeException.error("Problem when reading file " + file_path, scope);
		}
		if (tmpBfrImage == null)
			GamaRuntimeException.error("Problem when reading file " + file_path, scope);
		LuminanceSource tmpSource = new BufferedImageLuminanceSource(tmpBfrImage);
		BinaryBitmap tmpBitmap = new BinaryBitmap(new HybridBinarizer(tmpSource));
		MultiFormatReader tmpBarcodeReader = new MultiFormatReader();
		Result tmpResult;   
		String tmpFinalResult = "";
		try {
			tmpResult = tmpBarcodeReader.decode(tmpBitmap);
			tmpFinalResult = String.valueOf(tmpResult.getText());
		} catch (Exception tmpExcpt) {
			GamaRuntimeException.error(
					"BarCodeUtil.decode Excpt err - " + tmpExcpt.toString() + " - " + tmpExcpt.getMessage(), scope);
		}
		return tmpFinalResult;
	}

	
}


