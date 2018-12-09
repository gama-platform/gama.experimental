package genstar.gamaplugin.operators;

import java.util.Arrays;

import core.configuration.dictionary.AttributeDictionary;
import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.AttributeFactory;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;
import core.util.excpetion.GSIllegalRangedData;
import gospl.generator.util.GSUtilGenerator;
import msi.gama.metamodel.shape.IShape;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.runtime.IScope;
import msi.gama.util.IList;

public class GenstarDefaultGenerationOperators {
	
	@SuppressWarnings({ "unchecked" })	
	@operator(value = "dummy_generator")
	@doc("Generate N individuals of a simple population, randomly spatialized with a single attribute, iris, with values in  \"765400102\", \"765400101\" ")
	public static IList<IShape> dummyPopGeneration(IScope scope, Integer number) {
		IPopulation<ADemoEntity, Attribute<? extends IValue>> pop;
		
		AttributeDictionary atts = new AttributeDictionary();
		try {
			atts.addAttributes(AttributeFactory.getFactory()
					.createAttribute("iris", GSEnumDataType.Nominal, Arrays.asList("765400102", "765400101")));
		} catch (GSIllegalRangedData e1) {
			e1.printStackTrace();
		}
		
		GSUtilGenerator ug = new GSUtilGenerator(atts);
		pop = ug.generate(number);
	
		return GenstarGenerationOperators.genPop(scope, pop, null, number);
	}	
}
