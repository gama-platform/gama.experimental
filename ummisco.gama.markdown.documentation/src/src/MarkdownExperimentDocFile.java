package src;

import msi.gaml.descriptions.ExperimentDescription;

public class MarkdownExperimentDocFile extends AbstractMarkdownDocFile{
	
	
	public MarkdownExperimentDocFile(String path)
	{
		super(path);
		mDText+=addLink("Return to Index","./../"+IParser.MARKDOWN_INDEX_FILE_NAME+".md");
	}
	
	public void generateExperiments(ExperimentDescription anExperiment) {
		generateNameExperiment(anExperiment);
		generateDescriptionExperiment(anExperiment);
		generateDisplaysExperiment(anExperiment);
		generateParametersExperiment(anExperiment);
	}
	
	
}
