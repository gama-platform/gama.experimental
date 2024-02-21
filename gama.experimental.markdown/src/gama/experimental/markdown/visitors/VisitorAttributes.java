package gama.experimental.markdown.visitors;

import java.util.ArrayList;
import java.util.Map;

import gama.experimental.markdown.markdownSyntactic.IParser;
import gama.experimental.markdown.markdownSyntactic.MarkdownTools;
import gama.gaml.compilation.ast.ISyntacticElement;
import gama.gaml.compilation.ast.ISyntacticElement.SyntacticVisitor;
import gama.gaml.types.Types;

/**
 * 
 * @author damienphilippon
 * Date : 19 Dec 2017
 * Class representing the visitor of Attributes of a Species.
 * This visitor is used to generate the Markdown text of an ISyntacticElement visited for the Documentation.
 */
public class VisitorAttributes implements SyntacticVisitor{
	/**
	 * Boolean to precise whether or not it is the first attribute of the species
	 */
	boolean first = true;
	
	/**
	 * StringBuilder representing the Markdown text generated for the documentation
	 */
	StringBuilder mDText;
	
	/**
	 * Variable representing all the species and the link to the documentation files that will present them
	 */
	Map<String, String> speciesLink;
	
	/**
	 * Variable representing all the experiments and the link to the documentation files that will present them
	 */
	Map<String, String> experimentsLink;
	
	/**
	 * Constructor of the visitor, using the linkSpecies and linkExperiments for replacing unknown variable types by their corresponding variables
	 * @param linkSpecies {@code Map<String, String>}, the map giving the link of a species to the markdown document describing it
	 * @param linkExperiments {@code Map<String, String>}, the map giving the link of an experiment to the markdown document describing it
	 */
	public VisitorAttributes(Map<String, String> linkSpecies,Map<String, String> linkExperiments)
	{
		this.speciesLink=linkSpecies;
		this.experimentsLink=linkExperiments;
	}

	/**
	 * Method used to visit a ISyntacticElement (expecting an Attribute here), generating the markdown Text of it
	 * @param element {@code ISyntacticElement}, the ISyntacticElement representing an Attribute that will be used to generate the markdown code
	 */
	public void visit(ISyntacticElement element) {
		
		//Filter the possible ISyntacticElement that could not be Arguments (ie INIT, ACTIONS, REFLEXES,ASPECTS, OUTPUT,STATES AND PARAMETERS
		if(!((element.getKeyword().equals(IParser.GAMA_KEYWORD_INIT))||(element.getKeyword().equals(IParser.GAMA_KEYWORD_ACTION))||(element.getKeyword().equals(IParser.GAMA_KEYWORD_REFLEX))||(element.getKeyword().equals(IParser.GAMA_KEYWORD_ASPECT))||(element.getKeyword().equals(IParser.GAMA_KEYWORD_OUTPUT)||(element.getKeyword().equals(IParser.GAMA_KEYWORD_STATE))||(element.getKeyword().equals(IParser.GAMA_KEYWORD_PARAMETER)))))
		{

			VisitorDebug.DEBUG("          doing the attribute "+element.getName());
			//If it is the first element, then creates the header of the tables of the attributes
			if(first)
			{
				mDText.append(MarkdownTools.beginTable());
				ArrayList<String> header = new ArrayList<String>();
				header.add("Type");
				header.add("Name");
				mDText.append(MarkdownTools.addTableHeader(header));
				first=false;
			}
			//Add the argument to the table ( type | Attribute name )
			//But first try to determine if the type is not a built-in type
			//in order to make a link with the documentation of the species and put the link in the left column
			mDText.append(MarkdownTools.beginRow());
			String type = element.getKeyword();
			if(Types.get(type).toString().equals("unknown"))
			{
				type=MarkdownTools.addLink(type, speciesLink.get(type));
			}
			mDText.append(MarkdownTools.addCell(type));
			//Add the type of the attributes in the right column
			mDText.append(MarkdownTools.addCell(element.getName()+MarkdownTools.addBr()+MarkdownTools.addCode(MarkdownTools.getCommentsFromElement(element.getElement()))));
		}
	}
	/**
	 * Method to directly initialise the markdown text of the visitor, in order to let it add its generated text and return it to the model descriptor
	 * @param aBuilder {@code StringBuilder}, the StringBuilder of a model descriptor that will receive the generated text
	 */
	public void setText(StringBuilder aBuilder)
	{
		mDText=aBuilder;
	}
	
	/**
	 * Function that returns the StringBuilder of an Attribute Visitor once the visitor has done its job (adding text of an Attribute)
	 * @return {@code StringBuilder} the StringBuilder of a model descriptor 
	 */
	public StringBuilder getText()
	{
		return mDText;
	}

	/**
	 * Method to dispose all the objects that have been used by the VisitorAttributes and release memory
	 */
	public void dispose()
	{
		this.experimentsLink=null;
		this.speciesLink=null;
		this.first=true;
		this.mDText=null;
	}
}
