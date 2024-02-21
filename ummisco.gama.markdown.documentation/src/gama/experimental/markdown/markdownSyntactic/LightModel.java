package gama.experimental.markdown.markdownSyntactic;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;

import gama.gaml.compilation.GAML;
import gama.gaml.compilation.ast.ISyntacticElement;
import gama.gaml.compilation.ast.ISyntacticElement.SyntacticVisitor;

/**
 *
 * @author damienphilippon Date : 19 Dec 2017 Class used to have few information on a model without using too much
 *         memory
 */
public class LightModel {
	/**
	 * ISyntacticElement representing the model
	 */
	public ISyntacticElement modelElement;

	/**
	 * Variable representing all the species and the link to the documentation files that will present them
	 */
	public Map<String, String> speciesLink = new HashMap<>();
	/**
	 * Variable representing all the experiments and the link to the documentation files that will present them
	 */
	public Map<String, String> experimentsLink = new HashMap<>();

	/**
	 * Visitor of the model to load the species and experiment in the link maps
	 */
	SyntacticVisitor visitorForLoading = element -> {
		if (element.isSpecies() || element.getKeyword().equals(IParser.GAMA_KEYWORD_GRID)) {
			speciesLink.put(element.getName(), element.getKeyword() + "-" + element.getName());
		}
		if (element.isExperiment()) {
			experimentsLink.put(element.getName(), element.getKeyword() + "-" + element.getName() + "-"
					+ element.getExpressionAt(IParser.GAMA_KEYWORD_TYPE) + "-");
		}
	};

	/**
	 * Constructor of LightModel using an URI to get the contents from that URI
	 * 
	 * @param aUri
	 *            {@code URI} the URI used to get the contents
	 */
	public LightModel(final URI aUri) {
		this.modelElement = GAML.getContents(aUri);
		this.modelElement.visitSpecies(visitorForLoading);
		this.modelElement.visitGrids(visitorForLoading);
		this.modelElement.visitExperiments(visitorForLoading);
	}

	/**
	 * Method to release the memory used by the different variables of the object Light Model
	 */
	public void dispose() {
		speciesLink = null;
		experimentsLink = null;
		modelElement = null;
	}

}
