package markdownSyntactic;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;

import msi.gama.lang.gaml.ui.hover.GamlDocumentationProvider;
/**
 * 
 * @author damienphilippon
 * Date : 19 Dec 2017
 * Class used only for Markdown related methods
 */
public class MarkdownTools {
	
	/**
	 * Method that returns to the beginning of the line in markdown
	 * @return a String 
	 */
	public static String goBeginLine()
	{
		return "\n";
	}
	/**
	 * Method that returns the comments defined in GAML from an Element
	 * @param element {@code EObject} the element to get the comments for
	 * @return a String 
	 */
	public static String getCommentsFromElement(EObject element)
	{
		GamlDocumentationProvider p = (GamlDocumentationProvider)IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(URI.createFileURI("toto.gaml")).get(IEObjectDocumentationProvider.class);
		String comments = p.getOnlyComment(element);
		return comments==null?"":comments;
	}
	/**
	 * Method that add a new line (like in html) in markdown
	 * @return a String 
	 */
	public static String addBr()
	{
		return " <br/> ";
	}
	/**
	 * Method that returns a real line
	 * @return a String 
	 */
	public static String addLine()
	{
		String result = goBeginLine();
		result += "------";
		result +=goBeginLine();
		return result;
	}
	/**
	 * Method that returns a header
	 * @param header {@code String} a String that is going to be the header
	 * @return a String in Markdown for a header
	 */
	public static String addHeader(String header)
	{
		String result=goBeginLine();
		result += IParser.MARKDOWN_KEYWORD_HEADER+IParser.MARKDOWN_KEYWORD_SPACE+header;
		result +=goBeginLine();
		return result;
	}
	/**
	 * Method that returns a subheader
	 * @param header {@code String} a String that is going to be the subheader
	 * @return a String in Markdown for a subheader
	 */
	public static String addSubHeader(String header)
	{
		String result=goBeginLine();
		result += IParser.MARKDOWN_KEYWORD_SUBHEADER+IParser.MARKDOWN_KEYWORD_SPACE+header;
		result +=goBeginLine();
		return result;
	}
	/**
	 * Method that returns a subsubheader
	 * @param header {@code String} a String that is going to be the subsubheader
	 * @return a String in Markdown for a subsubheader
	 */
	public static String addSubSubHeader(String header)
	{
		String result=goBeginLine();
		result += IParser.MARKDOWN_KEYWORD_SUBSUBHEADER+IParser.MARKDOWN_KEYWORD_SPACE+header;
		result += goBeginLine();
		return result;
	}
	/**
	 * Method that returns a subsubsubheader
	 * @param header {@code String} a String that is going to be the subsubsubheader
	 * @return a String in Markdown for a subsubsubheader
	 */
	public static String addSubSubSubHeader(String header)
	{
		String result=goBeginLine();
		result += IParser.MARKDOWN_KEYWORD_SUBSUBSUBHEADER+IParser.MARKDOWN_KEYWORD_SPACE+header;
		result += goBeginLine();
		return result;
	}
	/**
	 * Method that returns a text that will be in italic
	 * @param toItalic {@code String } a String that is going to be the text in italic
	 * @return a String in Markdown for an italic text
	 */
	public static String addItalic(String toItalic)
	{
		return "*"+toItalic+"*";
	}

	/**
	 * Method that returns a markdown link
	 * @param label {@code String} a String that is going to be the label of the link
	 * @param url {@code String} a String that is going to be url of the link
	 * @return a String in Markdown for a link
	 */
	public static String addLink(String label, String url)
	{
		return " ["+label+"]("+url+")";
	}
	/**
	 * Method that returns a markdown quote
	 * @param quote {@code String} a String that is going to be the text of the quote
	 * @return a String in Markdown for a quote
	 */
	public static String addQuote(String quote)
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

	/**
	 * Method that returns a markdown code section
	 * @param code {@code String} a String that is going to be the code
	 * @return a String in Markdown for a code section
	 */
	public static String addCode(String code)
	{
		String result="";
		if(code.equals("")==false)
		{
			result+=" ``` "+code+" ```  ";
		}
		return result;
	}
	/**
	 * Method that returns a html beginning table tag
	 * @return a String in HTML for beginning a table
	 */
	public static String beginTable()
	{
		return IParser.HTML_BEGIN_ARRAY;
	}
	/**
	 * Method that returns a html table header from the list of String given
	 * @param theHeaders {@code List<String>} the headers of the table
	 * @return a String in HTML for the header
	 */
	public static String addTableHeader(List<String> theHeaders)
	{
		String result =beginRow();
		for(String aString : theHeaders)
		{
			result+=IParser.HTML_BEGIN_HEADER+aString+IParser.HTML_END_HEADER;
		}
		result +=endRow();
		return result;
	}
	/**
	 * Method that returns a html table cell for a given string
	 * @param value {@code String} the value to put in the cell
	 * @return a String in HTML for a table cell
	 */
	public static String addCell(String value)
	{
		return IParser.HTML_BEGIN_CELL+value+IParser.HTML_END_CELL;
	}

	/**
	 * Method that returns a html table row beginning tag
	 * @return a String in HTML for a table row beginning tag
	 */
	public static String beginRow()
	{
		return IParser.HTML_BEGIN_ROW;
	}

	/**
	 * Method that returns a html table row ending tag
	 * @return a String in HTML for a table row ending tag
	 */
	public static String endRow()
	{
		return IParser.HTML_END_ROW;
	}

	/**
	 * Method that returns a html table ending tag
	 * @return a String in HTML for a table ending tag
	 */
	public static String endTable()
	{
		return IParser.HTML_END_ARRAY;
	}
}
