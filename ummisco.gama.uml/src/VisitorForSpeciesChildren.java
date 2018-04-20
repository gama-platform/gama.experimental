import java.util.HashMap;
import msi.gaml.compilation.ast.ISyntacticElement;
import msi.gaml.compilation.ast.ISyntacticElement.SyntacticVisitor;
import msi.gaml.types.Types;

public class VisitorForSpeciesChildren implements SyntacticVisitor{

	GamlToUMLConverter converter;
	String species;
	public VisitorForSpeciesChildren(GamlToUMLConverter theConverter)
	{
		this.converter = theConverter;
	}
	@Override
	public void visit(ISyntacticElement element) {
		//ACTIONS
		if(element.getKeyword().equals(IParser.GAMA_KEYWORD_ACTION))
		{
			converter.id=converter.id+1;
			if(converter.operations.containsKey(species)==false)
			{

				converter.operations.put(species, new HashMap<String,ISyntacticElement>());
			}
			converter.operations.get(species).put(Integer.toString(converter.id), element);
		}
		//ATTRIBUTES
		if(!((element.getKeyword().equals(IParser.GAMA_KEYWORD_INIT))||(element.getKeyword().equals(IParser.GAMA_KEYWORD_ACTION))||(element.isSpecies())||(element.getKeyword().equals(IParser.GAMA_KEYWORD_REFLEX))||(element.getKeyword().equals(IParser.GAMA_KEYWORD_ASPECT))||(element.getKeyword().equals(IParser.GAMA_KEYWORD_OUTPUT)||(element.getKeyword().equals(IParser.GAMA_KEYWORD_STATE))||(element.getKeyword().equals(IParser.GAMA_KEYWORD_PARAMETER)))))
		{
			converter.id=converter.id+1;
			if(converter.attributes.containsKey(species)==false)
			{
				converter.attributes.put(species, new HashMap<String,ISyntacticElement>());
			}
			String type = element.getKeyword();
			if(Types.get(type).toString().equals("unknown"))
			{
				if(converter.associations.containsKey(species)==false)
				{
					converter.associations.put(species, new HashMap<String,ISyntacticElement>());
				}
				converter.associations.get(species).put(Integer.toString(converter.id), element);
				converter.id=converter.id+1;
			}
			converter.attributes.get(species).put(Integer.toString(converter.id), element);
		}
		//REFLEXES
		if((element.getKeyword().equals(IParser.GAMA_KEYWORD_REFLEX))||(element.getKeyword().equals(IParser.GAMA_KEYWORD_INIT))||(element.getKeyword().equals(IParser.GAMA_KEYWORD_STATE)))
		{
			converter.id=converter.id+1;
			if(converter.operations.containsKey(species)==false)
			{

				converter.operations.put(species, new HashMap<String,ISyntacticElement>());
			}
			converter.operations.get(species).put(Integer.toString(converter.id), element);
		}
		//MICRO SPECIES
		if(element.isSpecies())
		{
			converter.id=converter.id+1;
			if(converter.compositions.containsKey(species)==false)
			{
				converter.compositions.put(species, new HashMap<String,ISyntacticElement>());
			}
			converter.compositions.get(species).put(Integer.toString(converter.id), element);
		}
	}
	
	public void setSpecies(String id)
	{
		this.species = id;
	}

}
