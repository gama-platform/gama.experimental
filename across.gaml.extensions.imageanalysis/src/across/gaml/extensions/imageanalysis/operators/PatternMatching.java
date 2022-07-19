package across.gaml.extensions.imageanalysis.operators;

 
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.common.io.Files;

import across.gaml.extensions.imageanalysis.boofcv.RemovePerspectiveDistortion;
import boofcv.alg.enhance.GEnhanceImageOps;
import boofcv.alg.template.TemplateMatching;
import boofcv.factory.template.FactoryTemplateMatching;
import boofcv.factory.template.TemplateScoreType;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.Match;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.point.Point2D_F64;
import msi.gama.common.geometry.Envelope3D;
import msi.gama.common.util.FileUtils;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.metamodel.shape.IShape;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.IOperatorCategory;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gaml.operators.Spatial.Creation;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

public class PatternMatching {
	// hdtrung - save 4 selected points of map: top-left, top-right, bottom-right, bottom-left
	private static List<GamaPoint> map_corners;
	
	public static void print2DArray(int[][] arr){
//        System.out.println(arr[0][0] + " " + arr[1][0] + " | ");
//        System.out.println(arr[0][1] + " " + arr[1][1] + " | ");
    }
	
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
        
        
//        File file = new File(resultPath);
//        System.out.println("check2");
//        if(file.exists()) {
//        	System.out.println("check3");
//	        for (int i = 0; i < 2*cols - 1; i = i + 2){
//	            for (int j = 0; j < 2*rows - 1; j = j + 2){
//	                BufferedImage dest = srcImg.getSubimage(i*width, j*height, width, height);
//	                int numberFiles = file.list().length + 1;
//	                File pathFile = new File(resultPath + numberFiles + ".PNG");
//	                ImageIO.write(dest,"PNG", pathFile);
//	            }
//	        }
//        }else {
//        	System.out.println("check4");
//        	new File(resultPath).mkdir();
//        	System.out.println("make directory: " + file.isDirectory());
//        	for (int i = 0; i < 2*cols - 1; i = i + 2){
//	            for (int j = 0; j < 2*rows - 1; j = j + 2){
//	            	System.out.println("check5");
//	                BufferedImage dest = srcImg.getSubimage(i*width, j*height, width, height);
//	                int numberFiles = file.list().length + 1;
//	                File pathFile = new File(resultPath + numberFiles + ".PNG");
//	                ImageIO.write(dest,"PNG", pathFile);
//	                System.out.println("check6");
//	            }
//	        }
//        }
        return null;
    }
	
	private static String cropImage(final BufferedImage srcImg, final int cols, final int rows) throws IOException {
        int width = srcImg.getWidth()/cols;
        int height = srcImg.getHeight()/rows;
        
        String basePath = new File("results").getAbsolutePath();
        if (!new File(basePath).exists()) {
        	new File(basePath).mkdir();
        }

        for (int i = 0; i < cols; i++){
            for (int j = 0; j < rows; j++){
                BufferedImage dest = srcImg.getSubimage(i*width, j*height, width, height);
                File pathFile = new File(basePath + "/" + i + j + ".PNG");
                ImageIO.write(dest,"PNG", pathFile);
            }
        }
        return null;
    }
	
    private static List<Match> findMatches(GrayF32 image, GrayF32 template, GrayF32 mask,
                                           int expectedMatches ) {
        // create template matcher.
    
        TemplateMatching<GrayF32> matcher =
                FactoryTemplateMatching.createMatcher(TemplateScoreType.SUM_SQUARE_ERROR, GrayF32.class);

        // Find the points which match the template the best
        matcher.setImage(image);
        matcher.setTemplate(template, mask, expectedMatches);
        matcher.process();

        return matcher.getResults().toList();  
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
			value = "gray_sharpened_4",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "Grayscale the image and apply sharpen-4")
	public static String shapened4(final IScope scope, final String outputPath, final String  image_path)  {
    	BufferedImage tmpBfrImage = getBufferedImage(scope,image_path );
    	BufferedImage result = null;
    	Planar<GrayU8> color = ConvertBufferedImage.convertFrom(tmpBfrImage, true, ImageType.PL_U8);
    	Planar<GrayU8> adjusted = color.createSameShape();
        GEnhanceImageOps.sharpen4(color, adjusted);
        result = ConvertBufferedImage.convertTo_U8(adjusted, null, true);
    	File outputfile = new File(FileUtils.constructAbsoluteFilePath(scope, outputPath, false));
		try {

			String ext = Files.getFileExtension(outputfile.getAbsolutePath());
			ImageIO.write(result, ext, outputfile);
		} catch (IOException e) {
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when writting file " + outputfile +": " + e, scope), true);
			
		}
		if (outputfile.exists())
			return outputfile.getAbsolutePath();
	    return null; 
       
    }
    
    @operator (
			value = "gray_sharpened_8",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "Grayscale the image and apply sharpen-8")
	public static String shapened8(final IScope scope, final String outputPath, final String  image_path)  {
    	BufferedImage tmpBfrImage = getBufferedImage(scope,image_path );
    	BufferedImage result = null;
    	Planar<GrayU8> color = ConvertBufferedImage.convertFrom(tmpBfrImage, true, ImageType.PL_U8);
    	Planar<GrayU8> adjusted = color.createSameShape();
        GEnhanceImageOps.sharpen8(color, adjusted);
        result = ConvertBufferedImage.convertTo_U8(adjusted, null, true);
    	File outputfile = new File(FileUtils.constructAbsoluteFilePath(scope, outputPath, false));
		try {

			String ext = Files.getFileExtension(outputfile.getAbsolutePath());
			ImageIO.write(result, ext, outputfile);
		} catch (IOException e) {
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when writting file " + outputfile +": " + e, scope), true);
			
		}
		if (outputfile.exists())
			return outputfile.getAbsolutePath();
	    return null; 
       
    }
    
    
    @operator (
			value = "remove_perspective",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "remove the perspective from an image using 4 reference points (top-left, top-right, bottom-right, bottom-left)")
	public static String removePerspective(final IScope scope, final String outputPath, final List<GamaPoint> points, final String  image_path, int resWidth, int resHeight)  {
    	BufferedImage flat = removeDistortion(scope, points,image_path,resWidth,resHeight);
		File outputfile = new File(FileUtils.constructAbsoluteFilePath(scope, outputPath, false));
		try {

			String ext = Files.getFileExtension(outputfile.getAbsolutePath());
			ImageIO.write(flat, ext, outputfile);
			map_corners = points;
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
			value = "image_matching",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "detect pattern images from an image")
	public static Map<GamaShape, Map<String, Double>> patternMatching(final IScope scope, final String image_path, final Map<String, String> patterns_path, final Integer expectedMatches )  {
//    public static Map<GamaShape, Map<String, Double>> patternMatching(final IScope scope, final String image_path, final Integer cols, final Integer rows )  {
    	File whatFile = new File(FileUtils.constructAbsoluteFilePath(scope, image_path, true));
    	
		// check the required parameters 
		if (whatFile == null || whatFile.getName().trim().isEmpty())
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + image_path, scope), true);
		Map result =  GamaMapFactory.create();
        // Original image
        GrayF32 image = UtilImageIO.loadImage(whatFile.getParentFile().getAbsolutePath(), whatFile.getName(), GrayF32.class);
        int numMatches = expectedMatches == null ?  Integer.MAX_VALUE : expectedMatches;
        for(String pattern : patterns_path.keySet()) {
        	File pi = new File(FileUtils.constructAbsoluteFilePath(scope, pattern, true));
    		
        	 GrayF32 patim = UtilImageIO.loadImage(pi.getParentFile().getAbsolutePath(), pi.getName(), GrayF32.class);
        	 var output = new BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_BGR);
             ConvertBufferedImage.convertTo(image, output);
             List<Match> found = findMatches(image, patim, null, numMatches);
             for (Match m : found) {
            	Map info =  GamaMapFactory.create();
            	 info.put("TYPE", patterns_path.get(pattern));
            	 info.put("SCORE", m.score);
            	
                 IShape shape = Creation.rectangle(scope, patim.width , patim.height);
                 shape.setLocation(new GamaPoint(m.x + patim.width/2.0,m.y + patim.height/2.0));
                 result.put(shape, info);
             }
             
        }
//        System.out.println(image_path);
////        cropGrid(final BufferedImage srcImg, final int cols, final int rows)
//        try {
//            cropGrid(whatFile, cols, rows);
//            String basePath = new File("results").getAbsolutePath();
//            File dir = new File(basePath);
//            File[] directoryListing = dir.listFiles();
//            
//            if (directoryListing != null) {
//                sortByNumber(directoryListing);
//                for (int i = 0; i < directoryListing.length; i++){
//                	System.out.println(okilaaaaa)
//                    System.out.println(directoryListing[i].getName());
////                    print2DArray(classifyCode(directoryListing[i]));
//                    System.out.println("------------------------------------------------");
//                }
//            }
//        }catch (Exception e){
//        	e.printStackTrace();
//        }
        return result;
       
    }

    @operator (
			value = "code_detect",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "detect lego code from an image")
    public static List<Lego> codeDetecting(final IScope scope, final String image_path, final Integer cols, final Integer rows )  {
    	List<Lego> results = GamaListFactory.create();
    	File whatFile = new File(FileUtils.constructAbsoluteFilePath(scope, image_path, true));
		if (whatFile == null || whatFile.getName().trim().isEmpty())
			GAMA.reportError(scope, GamaRuntimeException.error("Problem when reading file " + image_path, scope), true);
		try {
            cropGrid(whatFile, cols, rows);
            String basePath = new File("results").getAbsolutePath();
            File dir = new File(basePath);
            File[] directoryListing = dir.listFiles();
            
            if (directoryListing != null) {
                sortByNumber(directoryListing);
                for (int i = 0; i < directoryListing.length; i++){
                	Lego l = new Lego(2, 2);
                	l.setVal(classifyCode(scope, directoryListing[i]));
                	results.add(l);
                	System.out.println("Element: ");
                    System.out.println(l.getVal());
                    System.out.println("------------------------------------------------");
                }
            }
        }catch (Exception e){
        	e.printStackTrace();
        }
		
        return results;
    }
}
