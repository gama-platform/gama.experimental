package miat.gama.extension.qrcode.operators;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;

import across.gaml.extensions.webcam.operators.WebcamOperators;
import across.gaml.extensions.webcam.types.GamaWebcam;
import msi.gama.common.util.FileUtils;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.GamaPair;
import msi.gama.util.IList;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gama.util.matrix.GamaMatrix;
import msi.gama.util.matrix.GamaObjectMatrix;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.operators.Spatial.Creation;
import msi.gaml.types.Types;

public class Operators {
	
	
	
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
			value = "encodeQR",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "encode a given message into a QR code with the given resolution (width, height) as matrix of bool")
	public static GamaMatrix encodeQRcode(final IScope scope, final String message, final int width, final int height)  {
		com.google.zxing.Writer writer = new MultiFormatWriter();
		BitMatrix matrix = null;
	      try {
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType,String>(2);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            matrix = writer.encode(message,
            com.google.zxing.BarcodeFormat.QR_CODE, width, height, hints);
        } catch (com.google.zxing.WriterException e) {
        	GAMA.reportError(scope, GamaRuntimeException.error("Problem when encoding the message \""+ message + "\": " + e.getMessage(), scope), true);
        }

		 GamaObjectMatrix matrixR = new GamaObjectMatrix(width, height, Types.BOOL);
		 
		 for (int i = 0; i < width; i++) {
			 for (int j = 0; j < width; j++) {
				 matrixR.set(scope, i, j, matrix.get(i, j)); 
			 } 
		 }
		 return matrixR;	 
	}
	
	@operator (
			value = "decodeQR",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "decode a QR code from a photoshot with the given resolution (width, height) in pixels from the given webcam")
	public static String decodeQRcode(final IScope scope,  final GamaWebcam webcam, final GamaPair<Integer,Integer> resolutions)  {
		final BufferedImage tmpBfrImage = WebcamOperators.CamShotAct(scope, webcam,resolutions, false,false);
		if (tmpBfrImage == null)
			GAMA.reportError(scope, GamaRuntimeException.error("Could not decode the image", scope), true);
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
		}catch (NotFoundException tmpExcpt) { 
			return null;
		} catch (Exception tmpExcpt) {
			
			GAMA.reportError(scope, GamaRuntimeException.error("BarCodeUtil.decode Excpt err - " + tmpExcpt.toString() + " - " + tmpExcpt.getMessage(), scope), true);
		}
		return tmpFinalResult;
	}
	
	@operator (
			value = "decodeQR",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "decode a QR code from a photoshot from the given webcam")
	public static String decodeQRcode(final IScope scope, final GamaWebcam webcam )  {
		return decodeQRcode(scope,webcam, null);
	}
	
	
	
	
	@operator (
			value = "decodeQRFile",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "decode the QR code from an image file")
	public static String decodeQRcodeFile(final IScope scope, final String file_path)  {
		return decodeQRcodeFile(scope,file_path, false).keySet().stream().findFirst().get();
	}

		
	@operator (
			value = "decodeMultiQRFile",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "decode multiple QR code from an image file")
	public static Map<String, GamaShape> decodeMultiQRcodeFile(final IScope scope, final String file_path)  {
		return decodeQRcodeFile(scope,file_path, true);
	}
	
	
	public static GamaShape toShape(IScope scope, Result result) {
		ResultPoint[] pts = result.getResultPoints();
		IList<GamaPoint> ptsGama = GamaListFactory.create();
		for (ResultPoint pt : pts) {
			ptsGama.add(new GamaPoint(pt.getX(), pt.getY()));
		}
		GamaShape gs = (GamaShape) Creation.polygon(scope, ptsGama);
		return gs;
	}
	
	public static Map<String, GamaShape> decodeQRcodeFile(final IScope scope, final String file_path, boolean multiple)  {
		String path_gen = FileUtils.constructAbsoluteFilePath(scope, file_path, true);
		File whatFile = new File(path_gen);
		// check the required parameters 
		if (whatFile == null || whatFile.getName().trim().isEmpty())
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + file_path, scope), true);
			
		BufferedImage tmpBfrImage = null;
		try {
			tmpBfrImage = ImageIO.read(whatFile);
		} catch (IOException tmpIoe) {
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + file_path, scope), true);
		}
		if (tmpBfrImage == null)
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + file_path, scope), true);
		LuminanceSource tmpSource = new BufferedImageLuminanceSource(tmpBfrImage);
		BinaryBitmap tmpBitmap = new BinaryBitmap(new HybridBinarizer(tmpSource));
	    MultiFormatReader tmpBarcodeReader = new MultiFormatReader();
			
	    Result tmpResult;   
		Map<String, GamaShape> tmpFinalResult = GamaMapFactory.create();
		if (multiple) {
	        MultipleBarcodeReader bcReader = new GenericMultipleBarcodeReader(tmpBarcodeReader);
	        try {
				for (Result result : bcReader.decodeMultiple(tmpBitmap)) {
					tmpFinalResult.put(result.getText(), toShape(scope,result));;
					
				}
			} catch (NotFoundException e) {
				return null;
			} catch (Error e) {
				GAMA.reportError(scope, GamaRuntimeException.error(
						"BarCodeUtil.decode Excpt err - " + e.toString() + " - " + e.getMessage(), scope), true);
			}

		    
	    } else {
	    	try {
				tmpResult = tmpBarcodeReader.decode(tmpBitmap);
				tmpFinalResult.put(tmpResult.getText(), toShape(scope,tmpResult));
				
			} catch (NotFoundException e) {
				return null;
			} catch (Error e) {
				GAMA.reportError(scope, GamaRuntimeException.error(
						"BarCodeUtil.decode Excpt err - " + e.toString() + " - " + e.getMessage(), scope), true);
			}
	    }
		
		return tmpFinalResult;
	}

	
}


