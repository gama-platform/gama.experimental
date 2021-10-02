package ummisco.gama.unity.skills;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.variable;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gaml.types.IType;


@doc (" -- ")
@vars ({ @variable (
		name = "ButtonName",
		type = IType.STRING,
		doc = @doc ("Button Name")),
	@variable (name = "Label",
		type = IType.STRING,
		doc = @doc ("String ...")),
	})
//@skill(name = "ExtendSkill", concept = {IConcept.COMMUNICATION, IConcept.SKILL })
public class ExtendUnitySkill extends UnitySkill {
	
	public ExtendUnitySkill() {
		super();
	}
	
}
