package src;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import msi.gama.kernel.model.IModel;
import msi.gama.lang.gaml.validation.GamlModelBuilder;
import msi.gaml.compilation.GamlCompilationError;
import msi.gaml.descriptions.ExperimentDescription;
import msi.gaml.species.ISpecies;
import ummisco.gama.ui.navigator.contents.WrappedFolder;
import ummisco.gama.ui.navigator.contents.WrappedGamaFile;
import ummisco.gama.ui.navigator.contents.WrappedProject;
import ummisco.gama.ui.navigator.contents.WrappedResource;

public class GenerateMDHandler extends AbstractHandler {
	boolean save_file_after_each_method = true;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		
		if (selection != null & selection instanceof IStructuredSelection) {
            IStructuredSelection strucSelection = (IStructuredSelection) selection;
            for(Object o: strucSelection.toList())
            {
        		WrappedResource file_to_convert = (WrappedResource) o;
        		generateForWrappedResource(file_to_convert);
	        }
           MessageDialog.openInformation(HandlerUtil.getActiveShell(event),"GAML Documentation", "Documentation generated.");
 	       
        }
		return null;
	}
	public void generateForWrappedGamaFile(WrappedGamaFile file_to_convert)
	{
        File inputFile = file_to_convert.getResource().getRawLocation().toFile();
		String header_file_save = inputFile.getParentFile()+File.separator+"doc_"+file_to_convert.getName().replace(' ','_').replace(".gaml", "")+File.separator;
		Path pathDocFolder = new File(header_file_save).toPath();
		Path pathExperimentsFolder = new File(header_file_save+IParser.MARKDOWN_EXPERIMENT_FOLDER+File.separator).toPath();
		Path pathSpeciesFolder = new File(header_file_save+IParser.MARKDOWN_SPECIES_FOLDER+File.separator).toPath();
		if(Files.exists(pathDocFolder)==false)
		{
			try {
				Files.createDirectory(pathDocFolder);
				if(Files.exists(pathExperimentsFolder)==false)
	    		{
					Files.createDirectory(pathExperimentsFolder);
	    		}

				if(Files.exists(pathSpeciesFolder)==false)
	    		{
					Files.createDirectory(pathSpeciesFolder);
	    		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
       List<GamlCompilationError> errors=new ArrayList<GamlCompilationError>();
        IModel model = GamlModelBuilder.compile(file_to_convert.getResource().getRawLocationURI(),errors);
        if(model!=null)
        {
        	MarkdownIndexDocFile indexFile = new MarkdownIndexDocFile(pathDocFolder.toString()+"/index.md");
        	indexFile.generateIndex(model);
        	indexFile.saveFile();
            for(String aKeySpecies : model.getAllSpecies().keySet())
            {
                ISpecies aSpecies = model.getSpecies(aKeySpecies);
            	MarkdownSpeciesDocFile file = new MarkdownSpeciesDocFile(pathSpeciesFolder.toString()+"/"+AbstractMarkdownDocFile.getSpeciesName(aSpecies)+".md");
            	file.generateSpecies(aSpecies);
            	
            	file.saveFile();
            }
            
            for(ExperimentDescription anExperiment : model.getDescription().getModelDescription().getExperiments())
            {
                MarkdownExperimentDocFile file = new MarkdownExperimentDocFile(pathExperimentsFolder.toString()+"/"+anExperiment.getName()+".md");
            	file.generateExperiments(anExperiment);
            	file.saveFile();
            	
            }
        }
        
	}
	public void generateForWrappedResource(final WrappedResource theResource)
	{
		if((theResource!=null)&((theResource instanceof WrappedFolder)||(theResource instanceof WrappedProject)))
		{
			for(Object aResource : theResource.getNavigatorChildren())
			{
				if(aResource instanceof WrappedResource)
				{
					generateForWrappedResource((WrappedResource) aResource);
				}
			}
		}
		else
		{
			if((theResource!=null )&(theResource instanceof WrappedGamaFile))
			{
				WrappedGamaFile gamaFile = (WrappedGamaFile) theResource;
				
				generateForWrappedGamaFile(gamaFile);
			}
		}
	}
}
