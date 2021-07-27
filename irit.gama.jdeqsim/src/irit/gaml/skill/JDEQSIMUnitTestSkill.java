/*******************************************************************************************************
*
* JDEQSIMUnitTestSkill.java, in plugin irit.gama.jdeqsim,
* is part of the source code of the GAMA modeling and simulation platform (v. 1.8.1)
*
* (c) 2007-2020 UMI 209 UMMISCO IRD/SU & Partners
*
* Visit https://github.com/gama-platform/gama for license information and contacts.
* 
********************************************************************************************************/

package irit.gaml.skill;

import irit.gama.common.IKeyword;
import jdeqsim.irit.gama.test.jdeqsim.TestMessageFactory;
import jdeqsim.irit.gama.test.jdeqsim.TestMessageQueue;
import jdeqsim.irit.gama.test.jdeqsim.TestScheduler;
import jdeqsim.irit.gama.test.jdeqsim.TestMessages;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.skills.Skill;

/**
 * JDQSIM unit test implementation
 * 
 * @author Jean-François Erdelyi
 */
@skill(name = IKeyword.JDQSIM_SIMUNITTEST, concept = { IKeyword.JDQSIM_SIMUNITTEST, IConcept.SKILL }, internal = true)
public class JDEQSIMUnitTestSkill extends Skill {
	@action(name = "test")
	public Object test(final IScope scope) throws GamaRuntimeException {
		TestMessageFactory.test();
		TestMessageQueue.test();
		TestScheduler.test();
		TestMessages.test();
		return true;
	}
}
