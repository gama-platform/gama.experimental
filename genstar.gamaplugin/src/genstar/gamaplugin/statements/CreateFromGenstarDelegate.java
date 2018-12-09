/*********************************************************************************************
 *
 * 'gamaplugin.CreateFromGenstarDelegate.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/

package genstar.gamaplugin.statements;

import core.metamodel.IPopulation;
import core.metamodel.attribute.Attribute;
import core.metamodel.attribute.MappedAttribute;
import core.metamodel.entity.ADemoEntity;
import core.metamodel.value.IValue;
import core.util.random.GenstarRandomUtils;
import genstar.gamaplugin.operators.GenstarGenerationOperators;
import genstar.gamaplugin.types.GamaPopGenerator;
import genstar.gamaplugin.types.GamaPopGeneratorType;
import genstar.gamaplugin.utils.GenStarGamaUtils;
import msi.gama.common.interfaces.ICreateDelegate;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMapFactory;
import msi.gaml.operators.Spatial;
import msi.gaml.statements.Arguments;
import msi.gaml.statements.CreateStatement;
import msi.gaml.types.IType;
import msi.gaml.variables.IVariable;
import spll.SpllEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Class CreateFromGenstarDelegate.
 *
 * @author Patrick Taillandier
 * @since 30 january 2017
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CreateFromGenstarDelegate implements ICreateDelegate {

	public static IType type = new GamaPopGeneratorType();
	/**
	 * Method acceptSource()
	 *
	 * @see msi.gama.common.interfaces.ICreateDelegate#acceptSource(IScope, java.lang.Object)
	 */
	@Override
	public boolean acceptSource(IScope scope, final Object source) {
		return source instanceof GamaPopGenerator;
	}

	
	/**
	 * Method fromFacetType()
	 * 
	 * @see msi.gama.common.interfaces.ICreateDelegate#fromFacetType()
	 */
	@Override
	public IType fromFacetType() {
		if (type == null) type = new GamaPopGeneratorType();
		if (type.getName() == null) ((GamaPopGeneratorType) type).init();
		return type;
	}

	@Override
	public boolean createFrom(IScope scope, List<Map<String, Object>> inits, Integer number, Object source, Arguments init,
			CreateStatement statement) {
		GamaPopGenerator gen = (GamaPopGenerator) source;
		if (gen == null) 
			return false;

		if (number == null) number = -1;
		IPopulation<? extends ADemoEntity, Attribute<? extends IValue>> population = GenstarGenerationOperators.generatePop(scope, gen, number);
		
		// TODO : check if it is used 
		gen.setGeneratedPopulation(population);
		
		// Used to transform the GamaRange into a GAMA value...
		IAgent executor = scope.getAgent();
		msi.gama.metamodel.population.IPopulation<? extends IAgent> gama_pop = executor.getPopulationFor(statement.getDescription().getSpeciesContext().getName());
		
		final Collection<Attribute<? extends IValue>> attributes = population.getPopulationAttributes();
	    int nb = 0;
        List<ADemoEntity> es = new ArrayList(population);
        if (number > 0 && number < es.size()) es = scope.getRandom().shuffle(es);
        for (final ADemoEntity e : es) {
        	final Map map = (Map) GamaMapFactory.create();
        	//if (population instanceof SpllPopulation) {
        	if (e instanceof SpllEntity) {	
        		SpllEntity spllE = (SpllEntity) e;
        		if (spllE.getLocation() == null) continue;
        		map.put(IKeyword.SHAPE, new GamaShape(gen.getCrs() != null
						? Spatial.Projections.to_GAMA_CRS(scope, new GamaShape(spllE.getLocation()), gen.getCrs())
						: Spatial.Projections.to_GAMA_CRS(scope, new GamaShape(spllE.getLocation()))));
        	}
        	for (final Attribute<? extends IValue> attribute : attributes) {
        		// Get the species variable associated to the Genstar Attribute  
        		IVariable var = gama_pop.getVar(attribute.getAttributeName());
        		Object attributeValue;
        		Attribute attributeToPut = attribute;
        		// if the Attribute does not correspond to a variable, and if it is a MappedAttribute, try to get the Variable corresponding to the ReferentAttribute
        		if(var == null) {
        			if (attribute instanceof MappedAttribute) {
                		Attribute referent_attribute = ((MappedAttribute) attribute).getReferentAttribute();
        				var = gama_pop.getVar(referent_attribute.getAttributeName());
        				if(var == null) {
                			throw GamaRuntimeException.error("Neither the attribute " + attribute.getAttributeName() + ", nor its referent attribute " + 
                							referent_attribute.getAttributeName() +"are defined in the species " + gama_pop.getSpecies().getName(), scope);
        				} else {
        					
        					Collection<? extends IValue> possibleValues = attribute.findMappedAttributeValues(e.getValueForAttribute(attribute));
        					
                    		attributeValue = GenStarGamaUtils.toGAMAValue(scope, GenstarRandomUtils.oneOf(possibleValues), true, var.getType());		
                    		attributeToPut = referent_attribute;
        				}
        			} else {
            			throw GamaRuntimeException.error("The attribute " + attribute.getAttributeName() + "is not defined in the species " + gama_pop.getSpecies().getName(), scope);
            		}
        		} else {
            		attributeValue = GenStarGamaUtils.toGAMAValue(scope, e.getValueForAttribute(attribute), true, var.getType());	
        		}
        		
        		map.put(attributeToPut.getAttributeName(), attributeValue);
        	}
        	statement.fillWithUserInit(scope, map);
    		inits.add(map);
            nb ++;
            if (number > 0 && nb >= number) break;
        }	
		return true;
	}

}
