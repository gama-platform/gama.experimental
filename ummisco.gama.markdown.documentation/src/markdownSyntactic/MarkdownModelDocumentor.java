package markdownSyntactic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;

import msi.gama.lang.gaml.indexer.GamlResourceIndexer;
import msi.gaml.compilation.GAML;
import msi.gaml.compilation.ast.ISyntacticElement;
import msi.gaml.compilation.ast.ISyntacticElement.SyntacticVisitor;
import ummisco.gama.ui.navigator.contents.WrappedGamaFile;
import visitors.VisitorExperiments;
import visitors.VisitorModel;
import visitors.VisitorSpecies;
/**
 * 
 * @author damienphilippon
 * Date : 19 Dec 2017
 * Class used to generate a markdown file corresponding to a model file 
 */
public class MarkdownModelDocumentor {
	/**
	 * Variable that will contain the markdown text of the model
	 */
	StringBuilder mDText;
	/**
	 * Variable that will contain the path to save the model
	 */
	String pathToSave;
	/**
	 * Variable that will contain the gama file
	 */
	WrappedGamaFile modelFile;
	/**
	 * Variable that will contain the model element
	 */
	ISyntacticElement modelElement;

	/**
	 * Variable that will contain the list of all the species of the model
	 */
	ArrayList<ISyntacticElement> species = new ArrayList<ISyntacticElement>();

	/**
	 * Variable that will contain the list of all the experiments of the model
	 */
	ArrayList<ISyntacticElement> experiments = new ArrayList<ISyntacticElement>();

	/**
	 * Visitor of all the species of the model
	 */
	VisitorSpecies visitorSpecies;
	
	/**
	 * Visitor of all the experiments of the model
	 */
	VisitorExperiments visitorExperiments;
	/**
	 * Visitor of the world section
	 */
	VisitorModel visitorModel;

	
	/**
	 * Variable representing all the species and the link to the documentation files that will present them
	 */
	Map<String, String> speciesLink = new HashMap<String,String>();
	/**
	 * Variable representing all the experiments and the link to the documentation files that will present them
	 */
	Map<String, String> experimentsLink = new HashMap<String,String>();
	
	
	/**
	 * Visitor of the species to build the index
	 */
	SyntacticVisitor visitorSpeciesForIndex = new SyntacticVisitor()
	{

		public void visit(ISyntacticElement element) {
			if(element.isSpecies())
			{
				mDText.append(MarkdownTools.goBeginLine());
				speciesLink.put(element.getName(), "#"+element.getKeyword()+"-"+element.getName());
				mDText.append(IParser.MARKDOWN_KEYWORD_LIST+IParser.MARKDOWN_KEYWORD_SPACE+MarkdownTools.addLink(element.getName(),"#"+element.getKeyword()+"-"+element.getName())+ IParser.MARKDOWN_KEYWORD_SPACE+"("+element.getKeyword()+")");
			}
		}
	};
	
	/**
	 * Visitor of the experiments to build the index
	 */
	SyntacticVisitor visitorExperimentsForIndex = new SyntacticVisitor()
	{
		public void visit(ISyntacticElement element) {
			mDText.append(MarkdownTools.goBeginLine());
			mDText.append(IParser.MARKDOWN_KEYWORD_LIST+IParser.MARKDOWN_KEYWORD_SPACE+MarkdownTools.addLink(element.getName(),"#"+element.getKeyword()+"-"+element.getName()+"-"+element.getExpressionAt(IParser.GAMA_KEYWORD_TYPE)+"-"));
			experimentsLink.put(element.getName(), "#"+element.getKeyword()+"-"+element.getName()+"-"+element.getExpressionAt(IParser.GAMA_KEYWORD_TYPE)+"-");
			if(element.hasFacet(IParser.GAMA_KEYWORD_TYPE))
			{
				mDText.append(IParser.MARKDOWN_KEYWORD_SPACE+"("+element.getExpressionAt(IParser.GAMA_KEYWORD_TYPE)+")");
			}
		}
	};
	
	/**
	 * Visitor of to build the list of species and experiments
	 */
	SyntacticVisitor visitorForLoading = new SyntacticVisitor()
	{

		@Override
		public void visit(ISyntacticElement element) {
			if((element.isSpecies())||(element.getKeyword().equals(IParser.GAMA_KEYWORD_GRID)))
			{
				species.add(element);
				element.visitSpecies(visitorForLoading);		
			}
			if(element.isExperiment())
			{
				experiments.add(element);
			}
		}
	};
	
	/**
	 * Constructor of the MarkdownModelDocumentor, using the WrappedGamaFile of the Model and the path to save the markdown resulting
	 * @param aFile {@code WrappedGamaFile} the file containing the model
	 * @param pathToSave {@code String} the path that will be used to save the markdown generated
	 */
	public MarkdownModelDocumentor(WrappedGamaFile aFile, String pathToSave)
	{
		this.modelFile=aFile;
		this.modelElement=GAML.getContents(URI.createURI(aFile.getResource().getLocationURI().toString()));
		this.mDText=new StringBuilder();
		
		//load the imports, species and experiments to give them to the visitors
		loadImports();
		loadSpeciesAndExperiments();
		
		
		this.visitorModel = new VisitorModel(this.speciesLink,this.experimentsLink);
		this.visitorSpecies = new VisitorSpecies(this.speciesLink,this.experimentsLink);
		this.visitorExperiments = new VisitorExperiments(this.speciesLink,this.experimentsLink);
		this.pathToSave=pathToSave;
	}
	
	/**
	 * Method to load the different files imported by a model in order to generate the links to the
	 * species and experiments
	 */
	public void loadImports()
	{
		IPath pathModel = modelFile.getResource().getRawLocation();
		Iterator<URI> importedUris = GamlResourceIndexer.allImportsOf(URI.createURI(modelFile.getResource().getRawLocationURI().toString()));

		while(importedUris.hasNext())
		{
			URI tmpUri = importedUris.next();
			LightModel model = new LightModel(tmpUri);

			IPath importedModel = new Path(tmpUri.toFileString().replace("file:", ""));
			IPath relativisedPath = importedModel.makeRelativeTo(pathModel);
			for(String aSpecies : model.speciesLink.keySet())
			{
				String linkToSpecies = model.speciesLink.get(aSpecies);
				linkToSpecies=FilenameUtils.removeExtension(relativisedPath.toString().replaceFirst(".."+IParser.SPLITTER, ""))+".md#"+linkToSpecies;
				this.speciesLink.put(aSpecies, linkToSpecies);
			}
			for(String anExperiment : model.experimentsLink.keySet())
			{
				String linkToExperiment = model.experimentsLink.get(anExperiment);
				linkToExperiment=FilenameUtils.removeExtension(relativisedPath.toString().replaceFirst(".."+IParser.SPLITTER,""))+".md#"+linkToExperiment;
				this.speciesLink.put(anExperiment, linkToExperiment);
			}
			
			model.dispose();
			model=null;
		}
		importedUris=null;
	}
	
	/**
	 * Method to generate the markdown of the file, defining the order in which the sections are implemented
	 */
	public void generateMarkdown()
	{
		mDText.append(MarkdownTools.addHeader("File "+modelFile.getName()));
		generateIndex();
		generateSpecies();
		generateExperiments();
	}

	/**
	 * Method to save the markdown file (will create the missing directories if any)
	 * and release the different variables (dispose is used)
	 */
	public void saveMarkdown()
	{
		try {
			File tmpFile = new File(pathToSave);
			tmpFile.getParentFile().mkdirs();
			FileWriter fw = new FileWriter(pathToSave);
			fw.write(mDText.toString());
			fw.flush();
			fw.close();
			dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Method to dispose / release the memory used by the different variables of the object
	 */
	public void dispose()
	{
		speciesLink=null;
		experimentsLink=null;
		this.modelElement.dispose();
		this.modelFile=null;
		this.modelElement=null;
		this.species=null;
		this.experiments=null;
		visitorModel.dispose();
		visitorSpecies.dispose();
		visitorExperiments.dispose();
		visitorModel=null;
		visitorSpecies=null;
		visitorExperiments=null;
		mDText=null;
	}
	

	/**
	 * Method to generate the markdown code for all the species
	 */
	public void generateSpecies()
	{
		//add a line and then the label of the species
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.addLine());
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.addSubHeader(IParser.MARKDOWN_LABEL_SPECIES));
		
		//Add the documentation of the world bloc
		visitorModel.setText(mDText);
		this.visitorModel.visit(modelElement);
		mDText=visitorModel.getText();
		
		//Add the documentation of the species
		this.visitorSpecies.setText(mDText);
		for(ISyntacticElement anElement : species)
		{
			visitorSpecies.visit(anElement);
		}
		mDText=visitorSpecies.getText();
	}
	
	/**
	 * Method to generate the markdown code for all the experiments
	 */
	public void generateExperiments()
	{
		//add a line and then the label of the experiments
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.addLine());
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.addSubHeader(IParser.MARKDOWN_LABEL_EXPERIMENTS));
		
		//Add the documentation of the experiments
		this.visitorExperiments.setText(mDText);
		this.modelElement.visitExperiments(visitorExperiments);
		mDText=visitorExperiments.getText();
	}
	
	/**
	 * Method to generate the markdown code for the index
	 */
	public void generateIndex()
	{
		//add a line and then the label of the index
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.addLine());
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.addSubHeader(IParser.MARKDOWN_LABEL_INDEX));

		//add the label of the species section and the number of species found
		mDText.append(MarkdownTools.addSubSubHeader(IParser.MARKDOWN_LABEL_SPECIES+" ("+species.size()+")"));
		
		//sort the species alphabetically 
		species.sort(new Comparator<ISyntacticElement>()
		{
			public int compare(ISyntacticElement element1, ISyntacticElement element2) 
			{
				return element1.getName().compareTo(element2.getName());
			}

		});
		for(ISyntacticElement anElement : species)
		{
			visitorSpeciesForIndex.visit(anElement);
		}
		mDText.append(MarkdownTools.goBeginLine());
		mDText.append(MarkdownTools.goBeginLine());
		
		
		//add the experiments section and the number of species found
		mDText.append(MarkdownTools.addSubSubHeader(IParser.MARKDOWN_LABEL_EXPERIMENTS+" ("+experiments.size()+")"));

		//sort the experiments alphabetically 
		experiments.sort(new Comparator<ISyntacticElement>()
		{
			public int compare(ISyntacticElement element1, ISyntacticElement element2) 
			{
				return element1.getName().compareTo(element2.getName());
			}

		});
		for(ISyntacticElement anElement : experiments)
		{
			visitorExperimentsForIndex.visit(anElement);
		}
	}

	/**
	 * Method to load the species (species and grid) and the experiments of the model
	 */
	public void loadSpeciesAndExperiments()
	{
		this.modelElement.visitSpecies(visitorForLoading);
		this.modelElement.visitGrids(visitorForLoading);
		this.modelElement.visitExperiments(visitorForLoading);
	}
	

}
