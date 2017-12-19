package jobs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;

import markdownSyntactic.IParser;
import markdownSyntactic.LightModel;
import markdownSyntactic.MarkdownModelDocumentor;
import markdownSyntactic.MarkdownTools;
import msi.gama.lang.gaml.indexer.GamlResourceIndexer;
import ummisco.gama.ui.navigator.contents.WrappedFolder;
import ummisco.gama.ui.navigator.contents.WrappedGamaFile;
import ummisco.gama.ui.navigator.contents.WrappedProject;
import ummisco.gama.ui.navigator.contents.WrappedResource;
/**
 * 
 * @author damienphilippon
 * Date : 19 Dec 2017
 * Abstract class used to do a Documentation job (will be derived by Project, ModelProject and ModelIndependent)
 */
public abstract class JobDocumentation extends WorkspaceJob {
	/**
	 * The directory that will be used to save the file(s)
	 */
	public String directory;
	
	/**
	 * Variable representing all the species and the link to the documentation files that will present them
	 */
	Map<String, String> speciesLink = new HashMap<String,String>();
	/**
	 * Variable representing all the experiments and the link to the documentation files that will present them
	 */
	Map<String, String> experimentsLink  = new HashMap<String,String>();
	
	/**
	 * The list of all the models already done by the process
	 */
	ArrayList<String> modelsDone = new ArrayList<String>();
	
	/**
	 * The path to the index file
	 */
	public String indexPath;
	
	
	/**
	 * Constructor of JobDocumentation that will use the given string as an output directory
	 * @param directory {@code String} the path (string) of the output directory
	 */
	public JobDocumentation(String directory) {
		super(IParser.JOB_NAME);
		this.directory=directory;
		
		//The index is defined at the base of the directory file
		this.indexPath=directory+File.separator+IParser.MARKDOWN_LABEL_INDEX+".md";
	}
	
	/**
	 * Method to run the job in background
	 */
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		return Status.OK_STATUS;
	}
	
	/**
	 * Recursive method to generate the models (will loop trough the folders)
	 * @param theResource {@code WrappedResource} the wrapped resource to loop on (project or folder)
	 */
	public void generateForWrappedResource(final WrappedResource theResource)
	{
		IPath pathProject = theResource.getProject().getResource().getLocation();
		IPath pathResource = theResource.getResource().getLocation();
		IPath pathResourceToProject = pathResource.makeRelativeTo(pathProject);
		
		if((theResource!=null)&((theResource instanceof WrappedFolder)||(theResource instanceof WrappedProject)))
		{
			File newDirectory = new File(directory+File.separator+pathResourceToProject);
			if(Files.exists(newDirectory.toPath())==false)
			{
				try {
					Files.createDirectory(newDirectory.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Object[] childrenResources = theResource.getNavigatorChildren().clone();
			for(Object aResource : childrenResources)
			{
				if(aResource instanceof WrappedResource)
				{
					if(((WrappedResource) aResource).countModels()>0)
					{
						generateForWrappedResource((WrappedResource) aResource);
					}
				}
			}
			childrenResources=null;
		}
		else
		{
			if((theResource!=null )&(theResource instanceof WrappedGamaFile))
			{
				WrappedGamaFile gamaFile = (WrappedGamaFile) theResource;
				
				generateForWrappedGamaFile(gamaFile);
				gamaFile=null;
			}
		}
	}
	/**
	 * Method to release the memory used by the variables of the job
	 */
	public void dispose()
	{
		this.experimentsLink=null;
		this.speciesLink=null;
		this.modelsDone=null;
	}

	/**
	 * Method to generate the index of the job (can be for the whole project or a model)
	 */
	public void generateIndex()
	{
		StringBuilder mDText =new StringBuilder();
		mDText.append(MarkdownTools.addHeader(IParser.MARKDOWN_LABEL_INDEX));
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.addLine());
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.addSubHeader(IParser.MARKDOWN_LABEL_SPECIES));
		
		//Sorts the species alphabetically
		List<String> species = new ArrayList( Arrays.asList(speciesLink.keySet().toArray()));
		Collections.sort(species,String.CASE_INSENSITIVE_ORDER);
		for(String aSpecies : species)
		{
			mDText.append(MarkdownTools.goBeginLine());
			mDText.append(MarkdownTools.addLink(aSpecies, speciesLink.get(aSpecies)));
		}
		species = null;
		
		
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.addLine());
		mDText.append(MarkdownTools.goBeginLine());
		
		mDText.append(MarkdownTools.addSubHeader(IParser.MARKDOWN_LABEL_EXPERIMENTS));
		

		//Sorts the experiments alphabetically
		List<String> experiments = new ArrayList( Arrays.asList(experimentsLink.keySet().toArray()));
		Collections.sort(experiments,String.CASE_INSENSITIVE_ORDER);
		for(String anExperiment : experiments)
		{
			mDText.append(MarkdownTools.goBeginLine());
			mDText.append(MarkdownTools.addLink(anExperiment, experimentsLink.get(anExperiment)));
		}
		experiments = null;
		

		//Save the markdown text to the file and set the markdown text to null
		try {
			FileWriter fw = new FileWriter(indexPath);
			fw.write(mDText.toString());
			fw.flush();
			fw.close();
			mDText=null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to generate the documentation of a given file (can be the documentation of a whole model)
	 * @param aFile
	 */
	public void generateForWrappedGamaFile(WrappedGamaFile aFile)
	{ 
		
		//Loop trough the imported models of the file and build the links between their species and the documentation
		Iterator<URI> importedUris = GamlResourceIndexer.allImportsOf(URI.createURI(aFile.getResource().getLocationURI().toString()));
		while(importedUris.hasNext())
		{
			URI tmpUri = importedUris.next();
			if(modelsDone.contains(tmpUri.toString())==false)
			{
				modelsDone.add(tmpUri.toString());
				LightModel model = new LightModel(tmpUri);

				IPath importedModel = new Path(tmpUri.toFileString().replace("file:", ""));
				IPath relativisedPath = importedModel.makeRelativeTo(new Path(indexPath));
				for(String aSpecies : model.speciesLink.keySet())
				{
					String linkToSpecies = model.speciesLink.get(aSpecies);
					linkToSpecies=FilenameUtils.removeExtension(relativisedPath.toString().replaceFirst(".."+File.separator, "").replaceFirst(".."+File.separator, ""))+".md#"+linkToSpecies;
					this.speciesLink.put(aSpecies, linkToSpecies);
				}
				for(String anExperiment : model.experimentsLink.keySet())
				{
					String linkToExperiment = model.experimentsLink.get(anExperiment);
					linkToExperiment=FilenameUtils.removeExtension(relativisedPath.toString().replaceFirst(".."+File.separator,"").replaceFirst(".."+File.separator, ""))+".md#"+linkToExperiment;
					this.experimentsLink.put(anExperiment, linkToExperiment);
				}
				
				model.dispose();
				model=null;
			}
		}
		importedUris=null;
		
		//Add the species and experiments of the model
		LightModel model = new LightModel(URI.createURI(aFile.getResource().getLocationURI().toString()));
		if(modelsDone.contains(aFile.getResource().getRawLocationURI().toString())==false)
		{
			modelsDone.add(aFile.getResource().getRawLocationURI().toString());
			IPath importedModel = aFile.getResource().getLocation();
			IPath relativisedPath = importedModel.makeRelativeTo(new Path(indexPath));
			for(String aSpecies : model.speciesLink.keySet())
			{
				String linkToSpecies = model.speciesLink.get(aSpecies);
				linkToSpecies=FilenameUtils.removeExtension(relativisedPath.toString().replaceFirst(".."+File.separator, "").replaceFirst(".."+File.separator, ""))+".md#"+linkToSpecies;
				this.speciesLink.put(aSpecies, linkToSpecies);
			}
			for(String anExperiment : model.experimentsLink.keySet())
			{
				String linkToExperiment = model.experimentsLink.get(anExperiment);
				linkToExperiment=FilenameUtils.removeExtension(relativisedPath.toString().replaceFirst(".."+File.separator,"").replaceFirst(".."+File.separator, ""))+".md#"+linkToExperiment;
				this.experimentsLink.put(anExperiment, linkToExperiment);
			}
		}
		
		//Build the folders tree 
		IPath pathProject = aFile.getProject().getResource().getLocation();
		IPath pathResource = aFile.getResource().getLocation();
		String pathResourceToProject = FilenameUtils.removeExtension(pathResource.makeRelativeTo(pathProject).toOSString());
		(new File(directory+File.separator+pathResourceToProject+".md")).getParentFile().mkdirs();
		
		//Generate the documentation of the model
		MarkdownModelDocumentor modelDoc = new MarkdownModelDocumentor(aFile,directory+File.separator+pathResourceToProject+".md");
		modelDoc.generateMarkdown();
		modelDoc.saveMarkdown();
		
		//Empty the variable to release memory
		pathProject=null;
		pathResource=null;
		pathResourceToProject=null;
	}
	
	

}
