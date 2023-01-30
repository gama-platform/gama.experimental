package across.gaml.extensions.webcam.operators;

 
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import across.gaml.extensions.webcam.types.GamaWebcam;
import msi.gama.common.util.FileUtils;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaPair;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.operators.Cast;

public class WebcamOperators {
	
	
	public static BufferedImage mirrorImage(BufferedImage img, boolean vertical, boolean horizontal) {
	    if (!vertical && !horizontal) return img;  
		//Getting the height and with of the read image.
	      int height = img.getHeight();
	      int width = img.getWidth();
	      //Creating Buffered Image to store the output
	      BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	      if (horizontal && !vertical) {
		      for(int j = 0; j < height; j++){
		         for(int i = 0, w = width - 1; i < width; i++, w--){
		           res.setRGB(w, j, img.getRGB(i, j));
		         }
		      }
	      }else if (!horizontal && vertical) {
	    	  for(int j = 0, h = height - 1; j < height; j++, h--){
				    for(int i = 0;i < width; i++){
				    	res.setRGB(i, h, img.getRGB(i, j));
			      }
		      }
		  } else {
			  for(int j = 0, h = height - 1; j < height; j++, h--){
				  for(int i = 0, w = width - 1; i < width; i++, w--){
			            res.setRGB(w, h, img.getRGB(i, j));
			         }
			      }
		  }
	      return res;
	}
	


	@operator (
			value = "cam_shot",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "get a photoshot with the given resolution <pair(width, height) in pixels> from the given webcam, and optionnaly mirror the image, and save it to the file - autoclose or not the webcam after")
	public static IMatrix cam_shot(final IScope scope,final GamaWebcam webcam,  final GamaPair<Integer, Integer> resolution,final boolean mirrorHorizontal, final boolean mirrorVertical, final String filepath, final boolean autoclose) {
		BufferedImage im = CamShotAct(scope, webcam, resolution, mirrorHorizontal,mirrorVertical, autoclose);
		if (filepath != null && !filepath.isBlank()) {
			String path_gen = FileUtils.constructAbsoluteFilePath(scope, filepath, false);
			File outputfile = new File(path_gen);
	    	try {
	    		if (!Files.exists(Paths.get(path_gen).getParent())) 
	    			Files.createDirectories(Paths.get(path_gen).getParent());
	    		ImageIO.write(im, com.google.common.io.Files.getFileExtension(path_gen), outputfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return  matrixValueFromImage(scope, im); 
	}
	
	
	@operator (
			value = "close_webcam",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "close a webcam")
	public static boolean closeWebcam(final IScope scope, final GamaWebcam webcam) {
		webcam.getWebcam().getLock().disable();
			
		return webcam.getWebcam().close();
	}

	@operator (
			value = "open_webcam",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "open a webcam")
	public static boolean openWebcam(final IScope scope, final GamaWebcam webcam) {
		webcam.getWebcam().getLock().disable();
		return webcam.getWebcam().open(false);
	}

	
	
	@operator (
			value = "cam_shot",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "get a photoshot with the default resolution from the given webcam, and optionnaly mirror the image, and save it to the file")
	public static IMatrix cam_shot(final IScope scope, final GamaWebcam webcam, final boolean mirrorHorizontal, final boolean mirrorVertical,final String filepath, final boolean autoclose) {
		return cam_shot(scope,webcam, null,mirrorHorizontal, mirrorVertical, filepath, autoclose ) ;
	}
	
	@operator (
			value = "cam_shot",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "get a photoshot with the default resolution  from the given webcam and save it to the file")
	public static IMatrix cam_shot(final IScope scope, final GamaWebcam webcam, final String filepath, final boolean autoclose) {
		return cam_shot(scope,webcam, null,false, false, filepath, autoclose ) ;
	}
	
	@operator (
			value = "cam_shot",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "get a photoshot with the given resolution <pair(width, height) in pixels> from the given webcam and save it to the file")
	public static IMatrix cam_shot(final IScope scope, final GamaWebcam webcam, final GamaPair<Integer, Integer> resolution,final String filepath, final boolean autoclose) {
		return cam_shot(scope,webcam, null,false, false, filepath, autoclose ) ;
	}
	
	@operator (
			value = "cam_shot",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "get a photoshot with the default resolution from the given webcam, and optionnaly mirror the image")
	public static IMatrix cam_shot(final IScope scope, final GamaWebcam webcam,final GamaPair<Integer, Integer> resolution, final boolean mirrorHorizontal, final boolean mirrorVertical, final boolean autoclose) {
		return cam_shot(scope,webcam, resolution,mirrorHorizontal, mirrorVertical, null, autoclose ) ;
	}
	
	@operator (
			value = "cam_shot",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "get a photoshot with the default resolution from the given webcam, and optionnaly mirror the image")
	public static IMatrix cam_shot(final IScope scope, final GamaWebcam webcam, final boolean mirrorHorizontal, final boolean mirrorVertical, final boolean autoclose) {
		return cam_shot(scope,webcam, null,mirrorHorizontal, mirrorVertical, null, autoclose ) ;
	}
	
	@operator (
			value = "cam_shot",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "get a photoshot with the default resolution  from the given webcam")
	public static IMatrix cam_shot(final IScope scope, final GamaWebcam webcam, final boolean autoclose) {
		return cam_shot(scope,webcam, null,false, false, null, autoclose ) ;
	}
	
	@operator (
			value = "cam_shot",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "get a photoshot with the given resolution <pair(width, height) in pixels> from the given webcam")
	public static IMatrix cam_shot(final IScope scope, final GamaWebcam webcam, final GamaPair<Integer, Integer> resolution, final boolean autoclose) {
		return cam_shot(scope,webcam, null,false, false, null, autoclose ) ;
	}
	
	

	
	
	public static BufferedImage CamShotAct(final IScope scope, GamaWebcam webcam, final GamaPair<Integer, Integer> resolution,final boolean mirrorHorizontal, final boolean mirrorVertical, final boolean autoclose) {
		if (webcam == null || webcam.getWebcam() == null) {
			GAMA.reportError(scope, GamaRuntimeException.error("No webcam detected", scope), false);
			return null;
		}
		
		if (!webcam.getWebcam().isOpen()) {
			
			openWebcam(scope, webcam);
		}
		
		if (resolution != null )  {
			final int width = Cast.asInt(scope, resolution.key);
			final int height = Cast.asInt(scope, resolution.value);
			Dimension dim = new Dimension(width,height );
			
			boolean nonStandard = true;
			int max_width = 0; int max_width_corresponding_height = 0; // we need to save those in pairs to preserve ratios
			int max_height = 0;int max_height_corresponding_width = 0;
			
			for (Dimension avail_dim : webcam.getWebcam().getViewSizes()) {
				
				if (max_width < avail_dim.width) {
					max_width = avail_dim.width;
					max_width_corresponding_height = avail_dim.height;
				}
				
				if (max_height < avail_dim.height) {
					max_height = avail_dim.height;
					max_height_corresponding_width = avail_dim.width;
				}
				
				if (avail_dim.equals(dim)) {
					nonStandard = false;
					break;
				}
			}
			
			/*if(width > max_width) {
				dim.width = max_width;
				dim.height = max_width_corresponding_height;
				nonStandard = false;
			}
			if(height > max_height) {
				dim.height = max_height;
				dim.width = max_height_corresponding_width;
				nonStandard = false;
			}
			*/
			if (!webcam.getWebcam().getViewSize().equals(dim)) {
				webcam.getWebcam().close();
					
				
				if (nonStandard ) {
					Dimension[] nonStandardResolutions = new Dimension[] {dim};
					webcam.getWebcam().setCustomViewSizes(nonStandardResolutions);
				}
				if (!webcam.getWebcam().isOpen()) 
					webcam.getWebcam().setViewSize(dim);
			}
			if (webcam.getWebcam().getLock().isLocked()) {
				webcam.getWebcam().getLock().unlock();
			}
			openWebcam(scope, webcam);
		}
		BufferedImage im = mirrorImage((BufferedImage) webcam.getWebcam().getImage(), mirrorHorizontal,mirrorVertical); 
		
		if (autoclose) 
			webcam.getWebcam().close();
		return im;
			
	}
	
	public static IMatrix matrixValueFromImage(final IScope scope, final BufferedImage image) {
		if (image == null) {
			return null;
		}
		int xSize, ySize;
		BufferedImage resultingImage = image;
		xSize = image.getWidth();
		ySize = image.getHeight();
		final IMatrix<Integer> matrix = new GamaIntMatrix(xSize, ySize);
		for (int i = 0; i < xSize; i++) {
			for (int j = 0; j < ySize; j++) {
				matrix.set(scope, i, j, resultingImage.getRGB(i, j));
			}
		}
		return matrix;
	}
	
}
