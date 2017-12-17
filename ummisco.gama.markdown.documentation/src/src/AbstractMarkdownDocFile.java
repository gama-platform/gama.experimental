package src;

import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;

import com.google.common.collect.Iterables;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.lang.gaml.ui.hover.GamlDocumentationProvider;
import msi.gaml.descriptions.ExperimentDescription;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.SkillDescription;
import msi.gaml.descriptions.StatementDescription;
import msi.gaml.descriptions.StatementWithChildrenDescription;
import msi.gaml.descriptions.VariableDescription;
import msi.gaml.descriptions.IDescription.DescriptionVisitor;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.ActionStatement;
import msi.gaml.statements.IStatement;

public class AbstractMarkdownDocFile {
	String mDText="";
	String path="";
	DescriptionVisitor<IDescription> outputDisplay=new DescriptionVisitor<IDescription>()
	{
		@Override
		public boolean visit(IDescription desc) {
			generateDocumentationDisplayChildren(desc);
			return true;
		}
	};
	DescriptionVisitor<IDescription> outputParameters=new DescriptionVisitor<IDescription>()
	{

		@Override
		public boolean visit(IDescription desc) {
			generateDocumentationParametersChildren(desc);
			return true;
		}
	};
	String buffer="";
	int nbParameters=0;
	int nbDisplays=0;
	public AbstractMarkdownDocFile(String path)
	{
		this.path = path;
	}

	public String goBeginLine()
	{
		return " \n";
	}
	public String addLine()
	{
		String result = goBeginLine();
		result += "------";
		result +=goBeginLine();
		return result;
	}
	public String addHeader(String header)
	{
		String result=goBeginLine();
		result += IParser.MARKDOWN_HEADER+IParser.MARKDOWN_SPACE+header;
		result +=goBeginLine();
		return result;
	}
	public String addSubHeader(String header)
	{
		String result=goBeginLine();
		result += IParser.MARKDOWN_SUBHEADER+IParser.MARKDOWN_SPACE+header;
		result +=goBeginLine();
		return result;
	}
	public String addSubSubHeader(String header)
	{
		String result=goBeginLine();
		result += IParser.MARKDOWN_SUBSUBHEADER+IParser.MARKDOWN_SPACE+header;
		result += goBeginLine();
		return result;
	}
	public String addItalic(String toItalic)
	{
		return "*"+toItalic+"*";
	}
	public String addLink(String label, String url)
	{
		return " ["+label+"]("+url+")";
	}
	public String addQuote(String quote)
	{
		String result="";
		if(quote.equals("")==false)
		{
			result=goBeginLine();
			result+="> "+quote;
			result+=goBeginLine();
		}
		return result;
	}
	public String getCommentsFromDescription(IDescription description)
	{
		GamlDocumentationProvider p = (GamlDocumentationProvider)IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(URI.createFileURI("toto.gaml")).get(IEObjectDocumentationProvider.class);
		String comments = p.getOnlyComment(description.getUnderlyingElement(null));
		return comments==null?"":comments;
	}
	public static String getSpeciesName(ISpecies species)
	{
		if(species.getDescription().isModel()==false)
    	{
        	return species.getName();
    	}
    	else
    	{
    		return "world";
    	}
	}
	public void generateNameSpecies(ISpecies species)
	{
		String speciesName =species.getDescription().isAbstract()?"Abstract ":"";
		speciesName+=getSpeciesName(species);
		mDText+=goBeginLine();
		mDText+=addHeader(speciesName);
		mDText+=goBeginLine();
	}
	public void generateParentSpecies(ISpecies species)
	{
		mDText+=goBeginLine();
		if(species.getParentSpecies()!=null)
		{
			String parent ="";
			parent = getLinkToSpecies(getSpeciesName(species.getParentSpecies()));
			mDText+=addItalic(IParser.MARKDOWN_TEXT_DERIVATED+parent);
		}
	}
	public void generateSkillSpecies(ISpecies species)
	{
		mDText+=goBeginLine();
		mDText+=addLine();
		int nbSkills = 0;
		String skillString="";
		
		for(SkillDescription skillName : species.getDescription().getSkills())
		{
			if(skillName.isControl()==false)
			{
				nbSkills++;
				skillString+=goBeginLine();
				skillString+=IParser.MARKDOWN_LIST+skillName.getTitle();
			}
		}
		mDText+=addSubHeader(IParser.MARKDOWN_TEXT_SKILL+" ("+nbSkills+")");
		mDText+=skillString;
	}
	public void generateControlSpecies(ISpecies species)
	{
		mDText+=goBeginLine();
		mDText+=addLine();
		String tmpControl ="";
		int nbSkills=0;
		
		for(SkillDescription skillName : species.getDescription().getSkills())
		{
			if(skillName.isControl()==true)
			{
				nbSkills=nbSkills+1;
				tmpControl+=goBeginLine();
				tmpControl+=IParser.MARKDOWN_LIST+skillName.getName();
			}
		}
		mDText+=addSubHeader(IParser.MARKDOWN_TEXT_CONTROL+" ("+nbSkills+")");
		mDText+=tmpControl;
	}
	public void generateDescriptionSpecies(ISpecies species)
	{
		String comment = getCommentsFromDescription(species.getDescription());
		if(comment.equals("")==false)
		{
			mDText+=goBeginLine();
			mDText+=addLine();
			mDText+=comment;
		}
	}
	public void generateDescriptionExperiment(ExperimentDescription anExperiment)
	{
		String comment = getCommentsFromDescription(anExperiment);
		if(comment.equals("")==false)
		{
			mDText+=goBeginLine();
			mDText+=addLine();
			mDText+=comment;
		}
	}
	public void generateAttributesSpecies(ISpecies species)
	{
		int nbAttributes = 0;
		String tmpAttributes="";
		mDText+=goBeginLine();
		mDText+=addLine();
		
		for(VariableDescription aVariable : species.getDescription().getOwnAttributes())
		{
			if(aVariable.isBuiltIn()==false)
			{
				nbAttributes++;
				tmpAttributes+=goBeginLine();
				tmpAttributes+=IParser.MARKDOWN_LIST;
				if(aVariable.getType().isAgentType())
				{
					tmpAttributes+=getLinkToSpecies(aVariable.getType().getName());
				}
				else
				{
					tmpAttributes+=IParser.MARKDOWN_SPACE+aVariable.getType().getName();
				}
				tmpAttributes+=IParser.MARKDOWN_SPACE+aVariable.getName();
				tmpAttributes+=addQuote(getCommentsFromDescription(aVariable));
			}
		}
		mDText+=addSubHeader(IParser.MARKDOWN_TEXT_ATTRIBUTES+" ("+nbAttributes+")");
		mDText+=tmpAttributes;
	}
	public void generateActionsSpecies(ISpecies species)
	{
		mDText+=goBeginLine();
		mDText+=addLine();
		int nbActions = 0;
		String tmpSpecies="";
		for(ActionStatement anAction :species.getActions())
    	{
			tmpSpecies+=goBeginLine();
    		if(!anAction.getDescription().isBuiltIn())
    		{
    			nbActions=nbActions+1;
    			int i = 0;
    			tmpSpecies+=IParser.MARKDOWN_LIST;
				String argDescription = "(";
				for(IDescription sDescription: anAction.getDescription().getFormalArgs())
				{
					if(sDescription.getType().isAgentType()==false)
					{
						argDescription=argDescription+sDescription.getType()+" "+sDescription.getName();
					}
					else
					{
						argDescription=argDescription+getLinkToSpecies(sDescription.getType().toString())+" "+sDescription.getName();
					}
					i=i+1;
					if(i<Iterables.size(anAction.getDescription().getFormalArgs()))
					{
						argDescription=argDescription+" , ";
					}
				}
				argDescription+=")";
				String typeAction = anAction.getDescription().getType().isAgentType()?getLinkToSpecies(anAction.getDescription().getType().getName()):(anAction.getDescription().getType().getName().equals("unknown")?"":anAction.getDescription().getType().getName());
				tmpSpecies=tmpSpecies+IParser.MARKDOWN_SPACE+typeAction+IParser.MARKDOWN_SPACE+anAction.getName()+IParser.MARKDOWN_SPACE+argDescription;
				tmpSpecies+=addQuote(getCommentsFromDescription(anAction.getDescription()));
    		}
    	}
		mDText+=addSubHeader(IParser.MARKDOWN_TEXT_ACTIONS+" ("+nbActions+")");
		mDText+=tmpSpecies;
	}
	public void generateReflexesSpecies(ISpecies species)
	{
		mDText+=addLine();
		int nbReflexes=0;
		String tmpReflexes="";
		for(IStatement aReflex :species.getBehaviors())
    	{
			if(aReflex.getDescription().isBuiltIn()==false)
			{
				nbReflexes++;
				tmpReflexes+=goBeginLine();
				tmpReflexes+=IParser.MARKDOWN_LIST;
				tmpReflexes+=IParser.MARKDOWN_SPACE+aReflex.getName();
				tmpReflexes+=addQuote(getCommentsFromDescription(aReflex.getDescription()));
			}
    	}
		mDText+=addSubHeader(IParser.MARKDOWN_TEXT_REFLEXES+" ("+nbReflexes+")");
		mDText+=tmpReflexes;
	}
	public String getLinkToSpecies(String species)
	{
		return "["+species+"]("+"./../"+IParser.MARKDOWN_SPECIES_FOLDER+"/"+species+".md)";
	}
	public String getLinkToExperiment(String experiment)
	{
		return "["+experiment+"]("+"./../"+IParser.MARKDOWN_EXPERIMENT_FOLDER+"/"+experiment+".md)";
	}
	public String getDirectLinkToSpecies(String species)
	{
		return "["+species+"]("+"./"+IParser.MARKDOWN_SPECIES_FOLDER+"/"+species+".md)";
	}
	public String getDirectLinkToExperiment(String experiment)
	{
		return "["+experiment+"]("+"./"+IParser.MARKDOWN_EXPERIMENT_FOLDER+"/"+experiment+".md)";
	}
	public void generateMacroSpecies(ISpecies species)
	{
		mDText+=goBeginLine();
		if((species.getMacroSpecies()!=null)&&(species.getMacroSpecies().getDescription().isModel()==false))
		{
			mDText=mDText+IParser.MARKDOWN_TEXT_MACRO_SPECIES+getLinkToSpecies(getSpeciesName(species.getMacroSpecies()));
		}
	}
	public void generateMicroSpecies(ISpecies species)
	{
		mDText+=goBeginLine();
		if(species.getMicroSpecies().size()>0)
		{
			mDText=mDText+IParser.MARKDOWN_TEXT_MICRO_SPECIES+" ("+species.getMicroSpecies().size()+")";
		}
		for(ISpecies aSpecies : species.getMicroSpecies())
		{
			mDText+=goBeginLine();
			mDText+=IParser.MARKDOWN_LIST;
			mDText=mDText+getLinkToSpecies(getSpeciesName(aSpecies));
		}
	}
	public void generateAspectsSpecies(ISpecies species)
	{
		goBeginLine();
		addLine();
		if(species.getAspects().size()>0)
		{
			addSubHeader(IParser.MARKDOWN_TEXT_ASPECTS+" ("+species.getAspects().size()+")");
		}
		for(StatementDescription aspect : species.getDescription().getAspects())
		{
			goBeginLine();
			mDText+=IParser.MARKDOWN_LIST;
			mDText=mDText+IParser.MARKDOWN_SPACE+aspect.getName()+IParser.MARKDOWN_SPACE;
		}
	}
	public void saveFile()
	{
		FileWriter fw;
		try {
			fw = new FileWriter(path, false);
			fw.write(mDText);
			reinitializeEverything();
	        fw.flush();
	        fw.close();
            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void generateDocumentationDisplayChildren(final IDescription desc)
	{
		if(desc instanceof StatementWithChildrenDescription)
		{
			StatementWithChildrenDescription descWithChildren = (StatementWithChildrenDescription) desc;
			if(descWithChildren.getKeyword().equals("output"))
			{
				descWithChildren.visitOwnChildren(outputDisplay);
			}
			if(descWithChildren.getKeyword().equals("display"))
			{
				nbDisplays++;
				buffer+=goBeginLine();
				buffer+=IParser.MARKDOWN_LIST+IParser.MARKDOWN_SPACE+descWithChildren.getName();
				descWithChildren.visitOwnChildren(outputDisplay);
			}
		}
	}
	public void generateDocumentationParametersChildren(final IDescription desc)
	{
		if(desc instanceof VariableDescription)
		{
			VariableDescription variableDescription = (VariableDescription) desc;
			if(variableDescription.isBuiltIn()==false)
			{
				nbParameters++;
				buffer+=goBeginLine();
				if(variableDescription.getType().isAgentType())
				{

					buffer+=IParser.MARKDOWN_LIST+IParser.MARKDOWN_SPACE+getLinkToSpecies(variableDescription.getType().getName())+IParser.MARKDOWN_SPACE+variableDescription.getName();
				}
				else
				{

					buffer+=IParser.MARKDOWN_LIST+IParser.MARKDOWN_SPACE+variableDescription.getType().getName()+IParser.MARKDOWN_SPACE+variableDescription.getName();
				}
				buffer+=addQuote(getCommentsFromDescription(variableDescription));
			}
		}
	}
	
	public void generateDisplaysExperiment(ExperimentDescription anExperiment)
	{
		mDText+=goBeginLine();
		mDText+=addLine();
		
		anExperiment.visitOwnChildren(outputDisplay);
		mDText+=addSubHeader(IParser.MARKDOWN_TEXT_DISPLAYS+" ("+nbDisplays+")");
		mDText+=buffer;
		reinitialize();
	}
	public void reinitializeEverything()
	{
		reinitialize();
		mDText="";
	}
	public void reinitialize()
	{
		nbDisplays=0;
		nbParameters=0;
		buffer="";
	}
	public void generateParametersExperiment(ExperimentDescription anExperiment)
	{
		mDText+=goBeginLine();
		mDText+=addLine();
		anExperiment.visitOwnChildren(outputParameters);
		mDText+=addSubHeader(IParser.MARKDOWN_TEXT_PARAMETERS+" ("+nbParameters+")");
		mDText+=buffer;
		reinitialize();
	}
	public void generateNameExperiment(ExperimentDescription anExperiment)
	{
		mDText+=goBeginLine();
		mDText+=addHeader(anExperiment.getName());
		mDText+=goBeginLine();
		mDText+=addItalic(IParser.MARKDOWN_TEXT_EXPERIMENT_TYPE+IParser.MARKDOWN_SPACE+anExperiment.getLitteral(IKeyword.TYPE));
		mDText+=goBeginLine();
	}
}
