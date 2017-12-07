package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import msi.gama.kernel.model.IModel;
import msi.gaml.descriptions.ExperimentDescription;
import msi.gaml.species.ISpecies;

public class MarkdownIndexDocFile extends AbstractMarkdownDocFile{
	
	public MarkdownIndexDocFile(String path)
	{
		super(path);
	}
	public void generateIndex(IModel model) {
		mDText+=addHeader(IParser.MARKDOWN_TEXT_INDEX_MODEL);
		mDText+=addLine();
		mDText+=goBeginLine();
		mDText+=addSubHeader( IParser.MARKDOWN_TEXT_SPECIES+" ("+model.getAllSpecies().size()+")");

		ArrayList<String> keySpecies = new ArrayList<String>(model.getAllSpecies().keySet());
        Collections.sort(keySpecies);
		for(String aKeySpecies : keySpecies)
        {
			ISpecies aSpecies = model.getSpecies(aKeySpecies);
        	mDText+=goBeginLine();
    		mDText=mDText+"- ";
    		mDText=mDText+getDirectLinkToSpecies(getSpeciesName(aSpecies));
        }
		mDText+=goBeginLine();
		mDText+=addLine();
		mDText+=goBeginLine();
		mDText+=addSubHeader(IParser.MARKDOWN_TEXT_EXPERIMENTS+" ("+model.getDescription().getModelDescription().getExperiments().size()+")");

		Comparator<ExperimentDescription> comparator = new Comparator<ExperimentDescription>()
		{
			@Override
			public int compare(ExperimentDescription o1, ExperimentDescription o2) {
				return o1.getName().compareTo(o2.getName());
			    
			}
		};
		ArrayList<ExperimentDescription> experimentsList = new ArrayList<ExperimentDescription>(model.getDescription().getModelDescription().getExperiments());
		experimentsList.sort( comparator);
		for(ExperimentDescription aKeyExperiment : experimentsList)
        {
			mDText+=goBeginLine();
    		mDText=mDText+"- ";
    		mDText=mDText+getDirectLinkToExperiment(aKeyExperiment.getName());
        }
	}
}
