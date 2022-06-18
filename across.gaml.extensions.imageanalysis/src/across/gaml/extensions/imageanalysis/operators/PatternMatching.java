package across.gaml.extensions.imageanalysis.operators;

 
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.common.io.Files;

import boofcv.alg.template.TemplateMatching;
import boofcv.factory.template.FactoryTemplateMatching;
import boofcv.factory.template.TemplateScoreType;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.Match;
import boofcv.struct.image.GrayF32;
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
import msi.gama.util.GamaMapFactory;
import msi.gaml.operators.Spatial.Creation;

public class PatternMatching {
	
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
    @operator (
			value = "image_matching",
			can_be_const = false,
			category = IOperatorCategory.LIST)
	@doc (
			value = "detect pattern images from an image")
	public static Map<GamaShape, Map<String, Double>> patternMatching(final IScope scope, final String image_path, final Map<String, String> patterns_path, final Integer expectedMatches )  {
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
        return result;
       
    }

  
}
