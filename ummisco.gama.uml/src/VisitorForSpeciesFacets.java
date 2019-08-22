import msi.gaml.descriptions.IDescription.IFacetVisitor;
import msi.gaml.descriptions.IExpressionDescription;

public class VisitorForSpeciesFacets implements IFacetVisitor {

	GamlToUMLConverter converter;
	String species;

	public VisitorForSpeciesFacets(final GamlToUMLConverter aConverter) {
		this.converter = aConverter;
	}

	@Override
	public boolean process(final String name, final IExpressionDescription exp) {
		if (name.equals(IParser.GAMA_KEYWORD_PARENT)) {
			converter.generalizations.put(species, exp.toString());
		}
		return true;
	}

	public void setSpecies(final String id) {
		this.species = id;
	}

}
