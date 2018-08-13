import msi.gaml.descriptions.IDescription.IFacetVisitor;

import java.util.HashMap;

import msi.gaml.descriptions.IExpressionDescription;

public class VisitorForSpeciesFacets implements IFacetVisitor{

	GamlToUMLConverter converter;
	String species;
	public VisitorForSpeciesFacets(GamlToUMLConverter aConverter)
	{
		this.converter = aConverter;
	}
	@Override
	public boolean visit(String name, IExpressionDescription exp) {
		if(name.equals(IParser.GAMA_KEYWORD_PARENT))
		{
			converter.generalizations.put(species, exp.toString());
		}
		return true;
	}
	public void setSpecies(String id)
	{
		this.species = id;
	}
	
	
}
