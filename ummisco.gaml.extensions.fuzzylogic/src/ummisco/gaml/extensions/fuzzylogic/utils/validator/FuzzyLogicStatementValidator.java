package ummisco.gaml.extensions.fuzzylogic.utils.validator;

import gama.gaml.compilation.IDescriptionValidator;
import gama.gaml.descriptions.IDescription;
import gama.gaml.descriptions.SkillDescription;
import gama.gaml.descriptions.SpeciesDescription;
import gama.gaml.descriptions.StatementDescription;
import gama.gaml.interfaces.IGamlIssue;
import ummisco.gaml.extensions.fuzzylogic.gaml.skills.FuzzylogicSkill;

public class FuzzyLogicStatementValidator implements IDescriptionValidator<StatementDescription> {

	/**
	 * Method validate()
	 *
	 * @see msi.gaml.compilation.IDescriptionValidator#validate(msi.gaml.descriptions.IDescription)
	 */
	@Override
	public void validate(final StatementDescription description) {
		String statementName = description.getKeyword();
		IDescription superDesc = description.getEnclosingDescription();
		while (! (superDesc instanceof SpeciesDescription) ) {
			superDesc = superDesc.getEnclosingDescription();
		}
		
		
					
		for(SkillDescription skillDesc : ((SpeciesDescription)superDesc).getSkills()) {
			if(skillDesc.getInstance() instanceof FuzzylogicSkill) {return;}
		}
		
		description.error("`" + statementName + "` must be used in the context of an agent with the Fuzzy Logic control architecture",
				IGamlIssue.WRONG_CONTEXT);		
	}
}