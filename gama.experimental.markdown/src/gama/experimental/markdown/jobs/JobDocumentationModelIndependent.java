package gama.experimental.markdown.jobs;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;

import gama.experimental.markdown.markdownSyntactic.IParser;
import gama.experimental.markdown.markdownSyntactic.LightModel;
import gama.experimental.markdown.markdownSyntactic.MarkdownModelDocumentor;
import gama.ui.navigator.view.contents.ResourceManager;
import gama.ui.navigator.view.contents.WrappedGamaFile;
import gaml.compiler.gaml.indexer.GamlResourceIndexer;
/**
 * 
 * @author damienphilippon
 * Date : 19 Dec 2017
 * Class used to do a Documentation job for an Independent Model (build the model, its index and its imports documentation)
 */
public class JobDocumentationModelIndependent extends JobDocumentation {

	/**
	 * The file of the model
	 */
	public WrappedGamaFile file;

	/**
	 * Constructor using a given model file and a given output directory
	 * @param aFile {@code WrappedGamaFile} the file containing the model
	 * @param directory {@code String} the string representing the path for the outputs
	 */
	public JobDocumentationModelIndependent(WrappedGamaFile aFile, String directory) {
		super(directory);
		this.file = aFile;
	}
	
	/**
	 * Method to run the job in background
	 */
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		generateForWrappedGamaFile(this.file);
		generateIndex();
		return Status.OK_STATUS;
	}
	/**
	 * Overriding the method to build the documentatioon of the imported models also
	 */
	public void generateForWrappedGamaFile(WrappedGamaFile aFile)
	{ 
		//Build the documentation of the model first
		IPath pathProject = aFile.getProject().getResource().getLocation();
		IPath pathResource = aFile.getResource().getLocation();
		String pathResourceToProject = FilenameUtils.removeExtension(pathResource.makeRelativeTo(pathProject).toOSString());
		(new File(directory+File.separator+pathResourceToProject+".md")).getParentFile().mkdirs();
		MarkdownModelDocumentor modelDoc = new MarkdownModelDocumentor(aFile,directory+File.separator+pathResourceToProject+".md");
		modelDoc.generateMarkdown();
		modelDoc.saveMarkdown();
		
		
		//Add the links to the different species and experiments contained in the model
		LightModel model = new LightModel(URI.createURI(aFile.getResource().getLocationURI().toString()));
		if(modelsDone.contains(aFile.getResource().getLocationURI().toString())==false)
		{
			modelsDone.add(aFile.getResource().getLocationURI().toString());
			IPath importedModel = aFile.getResource().getLocation();
			IPath relativisedPath = importedModel.makeRelativeTo(new Path(indexPath));
			for(String aSpecies : model.speciesLink.keySet())
			{
				String linkToSpecies = model.speciesLink.get(aSpecies);
				linkToSpecies=FilenameUtils.removeExtension(relativisedPath.toString().replaceFirst(".."+IParser.SPLITTER, "").replaceFirst("../","").replaceFirst(".."+IParser.SPLITTER, "").replaceFirst("../",""))+".md#"+linkToSpecies;
				this.speciesLink.put(aSpecies, linkToSpecies);
			}
			for(String anExperiment : model.experimentsLink.keySet())
			{
				String linkToExperiment = model.experimentsLink.get(anExperiment);
				linkToExperiment=FilenameUtils.removeExtension(relativisedPath.toString().replaceFirst(".."+IParser.SPLITTER,"").replaceFirst("../","").replaceFirst(".."+IParser.SPLITTER, "").replaceFirst("../",""))+".md#"+linkToExperiment;
				this.experimentsLink.put(anExperiment, linkToExperiment);
			}
		}
		
		//Generate the documentation for the imported models
		
		Map<URI, String> importedUris = GamlResourceIndexer.allImportsOf(URI.createURI(aFile.getResource().getLocationURI().toString()));
		for(final URI tmpUri : importedUris.keySet()){
			//Compute the relative path
			IPath importedModelPath = new Path(tmpUri.toFileString().replace("file:", ""));
			IPath relativisedPath = importedModelPath.makeRelativeTo(new Path(indexPath));
			IFile tmpfile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(importedModelPath); 
			WrappedGamaFile importedModel = (WrappedGamaFile) ResourceManager.getInstance().findWrappedInstanceOf(tmpfile);
			
			LightModel lightModelImported = new LightModel(tmpUri);
			
			//If the model has not been documented yet by the job, add the links to its species and experiments
			if(modelsDone.contains(importedModel.getResource().getLocationURI().toString())==false)
			{
				modelsDone.add(importedModel.getResource().getLocationURI().toString());
				IPath importedLightModelPath = importedModel.getResource().getLocation();
				IPath relativisedLightModelPath = importedLightModelPath.makeRelativeTo(new Path(indexPath));
				for(String aSpecies : lightModelImported.speciesLink.keySet())
				{
					String linkToSpecies = model.speciesLink.get(aSpecies);
					linkToSpecies=FilenameUtils.removeExtension(relativisedLightModelPath.toString().replaceFirst(".."+IParser.SPLITTER, "").replaceFirst("../","").replaceFirst(".."+IParser.SPLITTER, "").replaceFirst("../",""))+".md#"+linkToSpecies;
					this.speciesLink.put(aSpecies, linkToSpecies);
				}
				for(String anExperiment : lightModelImported.experimentsLink.keySet())
				{
					String linkToExperiment = model.experimentsLink.get(anExperiment);
					linkToExperiment=FilenameUtils.removeExtension(relativisedLightModelPath.toString().replaceFirst(".."+IParser.SPLITTER,"").replaceFirst("../","").replaceFirst(".."+IParser.SPLITTER, "").replaceFirst("../",""))+".md#"+linkToExperiment;
					this.experimentsLink.put(anExperiment, linkToExperiment);
				}
			}
			String pathResourceModelToProject = FilenameUtils.removeExtension(relativisedPath.toOSString().replaceFirst(".."+IParser.SPLITTER, "").replaceFirst("../","").replaceFirst(".."+IParser.SPLITTER, "").replaceFirst("../",""));
			(new File(directory+File.separator+pathResourceModelToProject+".md")).getParentFile().mkdirs();
			
			//Generate the documentation of the imported models
			MarkdownModelDocumentor importedModelDoc = new MarkdownModelDocumentor(importedModel,directory+File.separator+pathResourceModelToProject+".md");
			importedModelDoc.generateMarkdown();
			importedModelDoc.saveMarkdown();
			
		}
		//Release the memory used by the variables
		importedUris=null;
		pathProject=null;
		pathResource=null;
		pathResourceToProject=null;
	}
}
