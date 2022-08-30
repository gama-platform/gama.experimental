package across.gaml.extensions.imageanalysis.operators;

 
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.google.common.io.Files;

import across.gaml.extensions.imageanalysis.boofcv.RemovePerspectiveDistortion;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_F64;
import msi.gama.common.geometry.Envelope3D;
import msi.gama.common.util.FileUtils;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.IShape;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gaml.types.IType;

public class PatternMatching {
	// hdtrung - save 4 selected points of map: top-left, top-right, bottom-right, bottom-left
	private static List<GamaPoint> map_corners;
	private static List<List<GamaIntMatrix>> current_matrix_map;
	private static List<List<Integer>> current_int_map;
	
	public static double getBlackIntensity(final BufferedImage img){
        int intensity = 0;
        int total = img.getHeight() * img.getWidth() - 20;

        for (int i = 0; i < img.getHeight()-5; i++) {
            for (int j = 0; j < img.getWidth()-5; j++) {
                int p  = img.getRGB(i,j) & 0xFF;
                if(p <= 60){
                    intensity++;
                }
            }
        }
        return ((double)intensity/(double)total) * 100;
    }
	
	private static String cropGrid(final File srcImg, final int cols, final int rows) throws IOException {
		BufferedImage bf = ImageIO.read(srcImg);
        int width = bf.getWidth()/(2 * cols - 1);
        int height = bf.getHeight()/(2 * rows - 1);
        int files = 0;
        
        String basePath = new File("results").getAbsolutePath();
        if (!new File(basePath).exists()) {
        	new File(basePath).mkdir();
        }
        
        for (int i = 0; i < 2*cols - 1; i = i + 2){
            for (int j = 0; j < 2*rows - 1; j = j + 2){
                BufferedImage dest = bf.getSubimage(i*width, j*height, width, height);
                files++;
                File pathFile = new File(basePath + "/" + files + ".PNG");
                ImageIO.write(dest,"PNG", pathFile);
            }
        }
        return null;
    }
	
	public static BufferedImage mirrorImage(BufferedImage img) throws IOException {
	      //Getting the height and with of the read image.
	      int height = img.getHeight();
	      int width = img.getWidth();
	      //Creating Buffered Image to store the output
	      BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	      for(int j = 0; j < height; j++){
	         for(int i = 0, w = width - 1; i < width; i++, w--){
	            int p = img.getRGB(i, j);
	            //set mirror image pixel value - both left and right
	            res.setRGB(w, j, p);
	         }
	      }
	      return res;
	}
    
    public static GamaIntMatrix classifyCode(final IScope scope, final File f){
        int[][] r = new int[2][2];
        GamaIntMatrix l = new GamaIntMatrix(2, 2);
        try{
            BufferedImage img = ImageIO.read(f);
            int width = img.getWidth()/2;
            int height = img.getHeight()/2;
            int files = 0;
            String basePath = new File("prcImg").getAbsolutePath();
            new File(basePath + files).mkdir();

            for (int i = 0; i < 2; i++){
                for (int j = 0; j < 2; j++){
                    BufferedImage dest = img.getSubimage(i*width, j*height, width, height);
                    File pathFile = new File(basePath + files + "/" + + i + j + ".png");
                    ImageIO.write(dest,"png", pathFile);
                    if(getBlackIntensity(ImageIO.read(pathFile)) > 30){
                        r[i][j] = 1;
                        l.set(scope, i, j, 1);
                    }else {
                        r[i][j] = 0;
                        l.set(scope, i, j, 0);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return l;
    }
    
    public static void sortByNumber(final File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                return n1 - n2;
            }

            private int extractNumber(String name) {
                int i = 0;
                try {
                    int e = name.lastIndexOf('.');
                    String number = name.substring(0, e);
                    i = Integer.parseInt(number);
                } catch(Exception e) {
                    i = 0;
                }
                return i;
            }
        });
    }

    @operator (
			value = "crop_image",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "crop an image")
	public static String imageCropping(final IScope scope, final String pattern_path, final IShape geometry, final String  image_path, final IShape bounds)  {
    	File imageFile = new File(FileUtils.constructAbsoluteFilePath(scope, image_path, true));
		// check the required parameters 
		if (imageFile == null || imageFile.getName().trim().isEmpty())
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + image_path, scope), true);
		BufferedImage tmpBfrImage = null;
		try {
			tmpBfrImage = ImageIO.read(imageFile);
		} catch (IOException tmpIoe) {
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + image_path, scope), true);
		}
		if (tmpBfrImage == null)
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + image_path, scope), true);
		Envelope3D envbounds = bounds == null ?scope.getSimulation().getGeometry().getEnvelope() : bounds.getEnvelope();
		double coeffX = tmpBfrImage.getWidth() / envbounds.getWidth();
		double coeffY= tmpBfrImage.getHeight()/ envbounds.getHeight();
		Envelope3D env = geometry.getEnvelope();
		BufferedImage dest = tmpBfrImage.getSubimage((int) Math.round((env.getMinX() - envbounds.getMinX()) * coeffX), (int) Math.round((env.getMinY()  - envbounds.getMinY())* coeffY), (int) Math.round(env.getWidth() * coeffX), (int) Math.round( env.getHeight() * coeffY));
		File outputfile = new File(FileUtils.constructAbsoluteFilePath(scope, pattern_path, false));
		try {

			String ext = Files.getFileExtension(outputfile.getAbsolutePath());
			
			ImageIO.write(dest, ext, outputfile);
		} catch (IOException e) {
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when writting file " + outputfile +": " + e, scope), true);
			
		}
		if (outputfile.exists())
			return outputfile.getAbsolutePath();
	    return null; 
		
    }
    
    private static Point2D_F64 toPoint2D(GamaPoint pt, double coeffX, double coeffY) {
    	 return new Point2D_F64(pt.x * coeffX,pt.y * coeffY);
    }
    
    @operator (
			value = "remove_perspective",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "remove the perspective from an image using 4 reference points (top-left, top-right, bottom-right, bottom-left)")
	public static String removePerspective(final IScope scope, final String outputPath, final String  image_path, int resWidth, int resHeight)  {
    	BufferedImage flat = removeDistortion(scope, map_corners,image_path,resWidth,resHeight);
		File outputfile = new File(FileUtils.constructAbsoluteFilePath(scope, outputPath, false));
		try {
			BufferedImage flat1 = mirrorImage(flat);
			String ext = Files.getFileExtension(outputfile.getAbsolutePath());
			ImageIO.write(flat, ext, outputfile);
		} catch (IOException e) {
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when writting file " + outputfile +": " + e, scope), true);
			
		}
		if (outputfile.exists())
			return outputfile.getAbsolutePath();
	    return null; 
       
    }
    
    public static BufferedImage getBufferedImage(final IScope scope, final String  image_path) {
    	File imageFile = new File(FileUtils.constructAbsoluteFilePath(scope, image_path, true));
		if (imageFile == null || imageFile.getName().trim().isEmpty())
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + image_path, scope), true);
		BufferedImage tmpBfrImage = null;
		try {
			tmpBfrImage = ImageIO.read(imageFile);
		} catch (IOException tmpIoe) {
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + image_path, scope), true);
		}
		if (tmpBfrImage == null)
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + image_path, scope), true);
		return tmpBfrImage;
    }
    
    public static BufferedImage removeDistortion(final IScope scope, final List<GamaPoint> points, final String  image_path, int resWidth, int resHeight) {
    	if (points.size() != 4) {
    		GAMA.reportError(scope, GamaRuntimeException.error("4 points have to be defined (top-left, top-right, bottom-right, bottom-left)", scope), true);
    	}
    	BufferedImage tmpBfrImage = getBufferedImage(scope,image_path );
		Envelope3D envbounds = scope.getSimulation().getGeometry().getEnvelope() ;
		double coeffX = tmpBfrImage.getWidth() / envbounds.getWidth();
		double coeffY= tmpBfrImage.getHeight()/ envbounds.getHeight();
		
		Planar<GrayF32> input = ConvertBufferedImage.convertFromPlanar(tmpBfrImage, null, true, GrayF32.class);
		Point2D_F64 PTL = toPoint2D(points.get(0), coeffX,coeffY);
		Point2D_F64 PTR = toPoint2D(points.get(1), coeffX,coeffY);
		Point2D_F64 PBR = toPoint2D(points.get(2), coeffX,coeffY);
		Point2D_F64 PBL = toPoint2D(points.get(3), coeffX,coeffY);
		
		RemovePerspectiveDistortion<Planar<GrayF32>> removePerspective =
				new RemovePerspectiveDistortion<>(resWidth, resHeight, ImageType.pl(3, GrayF32.class));

		// Specify the corners in the input image of the region.
		// Order matters! top-left, top-right, bottom-right, bottom-left
		if (!removePerspective.apply(input,
				PTL, PTR,PBR,PBL)) {
			GAMA.reportError(scope, GamaRuntimeException.error("Problem with distortion computation", scope), true);
		}

		Planar<GrayF32> output = removePerspective.getOutput();

		return ConvertBufferedImage.convertTo_F32(output, null, true);
    }

    
    @operator (
			value = "code_detect",
			can_be_const = false,
			type = IType.LIST,
			content_type = IType.MATRIX,
			category = IOperatorCategory.LIST)
	@doc (
			value = "detect lego code from an image - result is matrix type")
    public static List<List<GamaIntMatrix>> codeDetectMatrix(final IScope scope, final String image_path, final Integer cols, final Integer rows )  {
    	List<List<GamaIntMatrix>> results = GamaListFactory.create();
    	List<List<GamaIntMatrix>> reverse_results = GamaListFactory.create();
    	File whatFile = new File(FileUtils.constructAbsoluteFilePath(scope, image_path, true));
		if (whatFile == null || whatFile.getName().trim().isEmpty())
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + image_path, scope), true);
		try {
            cropGrid(whatFile, cols, rows);
            String basePath = new File("results").getAbsolutePath();
            File dir = new File(basePath);
            File[] directoryListing = dir.listFiles();
            
            int current_index = 0;
            
            if (directoryListing != null) {
                sortByNumber(directoryListing);
                for (int i = 0; i < cols; i++) {
                	List<GamaIntMatrix> r = GamaListFactory.create();
                	List<Integer> r2 = GamaListFactory.create();
                	for (int j = 0; j < rows; j++) {
                		GamaIntMatrix l = new GamaIntMatrix(2, 2);
                    	l = classifyCode(scope, directoryListing[current_index]);
                    	r.add(l); 	
                    	current_index++;
                	}
                	results.add(r);
                	System.out.println();
                }
                Collections.reverse(results);
            }
            for (File file: dir.listFiles())
                if (!file.isDirectory())
                    file.delete();
            if(current_matrix_map != null) {
	           	for (int i = 0; i < results.size(); i++) {
	           		if (current_matrix_map.get(i) != results.get(i)) {
	           			current_matrix_map = results;
	           			Collections.reverse(results);
	           			return results;
	           		}
	           	}
            } else {
            	current_matrix_map = results;
   			 	return results;
            }
        }catch (Exception e){
        	e.printStackTrace();
        }
		return current_matrix_map;
    }

    @operator (
			value = "code_detect",
			can_be_const = false,
			type = IType.LIST,
			content_type = IType.MATRIX,
			category = IOperatorCategory.LIST)
	@doc (
			value = "detect lego code from an image - result is int type")
    public static List<List<Integer>> codeDetectInt(final IScope scope, final String image_path, final Integer cols, final Integer rows )  {
    	List<List<GamaIntMatrix>> results = GamaListFactory.create();
    	List<List<Integer>> int_results = GamaListFactory.create();
    	File whatFile = new File(FileUtils.constructAbsoluteFilePath(scope, image_path, true));
    	
		if (whatFile == null || whatFile.getName().trim().isEmpty())
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + image_path, scope), true);
		try {
            cropGrid(whatFile, cols, rows);
            String basePath = new File("results").getAbsolutePath();
            File dir = new File(basePath);
            File[] directoryListing = dir.listFiles();
            
            int current_index = 0;
            
            if (directoryListing != null) {
                sortByNumber(directoryListing);
                for (int i  = 0; i < cols; i++) {
                	List<GamaIntMatrix> r = GamaListFactory.create();
                	List<Integer> r2 = GamaListFactory.create();
                	for (int j = 0; j < rows; j++) {
                		GamaIntMatrix l = new GamaIntMatrix(2, 2);
                    	l = classifyCode(scope, directoryListing[current_index]);
                    	int tmp = convertCode(l);
                    	r.add(l);
                    	r2.add(tmp);     	
                    	current_index++;
                	}
                	results.add(r);
                	int_results.add(r2);
                }
            }
            Collections.reverse(int_results);
            for (File file: dir.listFiles())
                if (!file.isDirectory())
                    file.delete();
            if(current_int_map != null) {
	           	for (int i = 0; i < results.size(); i++) {
	           		if (current_int_map.get(i) != int_results.get(i)) {
	           			current_int_map = int_results;
	           			return int_results;
	           		}
	           	}
            } else {
            	current_int_map = int_results;
   			 	return int_results;
            }
        }catch (Exception e){
        	e.printStackTrace();
        }
		return current_int_map;
    }
    
    public static int convertCode(GamaIntMatrix m) {
    	int result = 0;
    	int sum = m.getMatrix()[0] + m.getMatrix()[1] + m.getMatrix()[2]+ m.getMatrix()[3];
        switch (sum){
            case 1:
                if (m.getMatrix()[0] == 1 || m.getMatrix()[3] == 1){
                	result = 10;
                }else {
                	result = 11;
                }
                break;
            case 2:
                if (m.getMatrix()[0] == m.getMatrix()[3]){
                    if (m.getMatrix()[0] == 1){
                    	result = 20;
                    }else {
                    	result = 21;
                    }
                }else {
                    if (m.getMatrix()[0] == m.getMatrix()[1]){
                    	result = 30;
                    }else {
                    	result = 31;
                    }
                }
                break;
            case 3:
                if (m.getMatrix()[0] == 0 || m.getMatrix()[3] == 0){
                	result = 40;
                }else {
                	result = 41;
                }
                break;
            default: result = 0;
        }
    	return result;
    }
    
    @operator (
			value = "map_define",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "define 4 corners of map from an image using 4 reference points (top-left, top-right, bottom-right, bottom-left)")
	public static String mapDefine(final IScope scope, final List<GamaPoint> points)  {
    	captureImg(scope, "../includes/difine_map.png", 0);
    	map_corners = points;
	    return null; 
    }
    
    @operator (
			value = "capture_image",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "capture an image by webcam.")
	public static String captureImg (final IScope scope, final String name, final Integer camera) {
		Webcam webcam = Webcam.getWebcamByName(Webcam.getWebcams().get(camera).getName());
//		for(Webcam w : Webcam.getWebcams()) {
//			System.out.println(w.getName());
//		}
		webcam.open();
		try {
			BufferedImage img = webcam.getImage();
			ImageIO.write(img, "PNG", new File(FileUtils.constructAbsoluteFilePath(scope, name, false)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return name;
	}
}
