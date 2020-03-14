package ummisco.gaml.extensions.fuzzylogic.utils.validator;

import msi.gama.common.interfaces.IGamlIssue;
import msi.gaml.compilation.IDescriptionValidator;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.SkillDescription;
import msi.gaml.descriptions.SpeciesDescription;
import msi.gaml.descriptions.StatementDescription;
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