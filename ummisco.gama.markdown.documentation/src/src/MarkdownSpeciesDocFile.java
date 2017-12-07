package src;

import msi.gaml.species.ISpecies;

public class MarkdownSpeciesDocFile extends AbstractMarkdownDocFile{
	
	public MarkdownSpeciesDocFile(String aPath)
	{
		super(aPath);
		mDText+=addLink("Return to Index","./../"+IParser.MARKDOWN_INDEX_FILE_NAME+".md");
	}
	public void generateSpecies(ISpecies species)
	{
		generateNameSpecies(species);
		generateParentSpecies(species);
		generateMacroSpecies(species);
		generateMicroSpecies(species);
		generateSkillSpecies(species);
		generateControlSpecies(species);
		generateDescriptionSpecies(species);
		generateAttributesSpecies(species);
		generateActionsSpecies(species);
		generateReflexesSpecies(species);
		generateAspectsSpecies(species);
	}
}
