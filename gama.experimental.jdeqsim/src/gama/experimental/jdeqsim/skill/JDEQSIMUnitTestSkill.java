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

package gama.experimental.jdeqsim.skill;

import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.IConcept;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.experimental.jdeqsim.common.IKeyword;
import gama.experimental.jdeqsim.test.TestMessageFactory;
import gama.experimental.jdeqsim.test.TestMessageQueue;
import gama.experimental.jdeqsim.test.TestMessages;
import gama.experimental.jdeqsim.test.TestScheduler;
import gama.experimental.jdeqsim.test.TestVehicle;
import gama.gaml.skills.Skill;

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
		TestVehicle.test();
		return true;
	}
}
