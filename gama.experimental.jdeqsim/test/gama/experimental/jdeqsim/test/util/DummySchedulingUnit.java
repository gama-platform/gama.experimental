/*******************************************************************************************************
 *
 * SimUnit.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package gama.experimental.jdeqsim.test.util;

import gama.core.runtime.IScope;
import gama.experimental.jdeqsim.core.SchedulingUnit;
import gama.experimental.jdeqsim.core.unit.Scheduler;

/**
 * The basic building block for all simulation units.
 *
 * @author rashid_waraich
 */
public class DummySchedulingUnit extends SchedulingUnit {

	public DummySchedulingUnit(IScope scope, Scheduler scheduler) {
		super(scope, null, scheduler);
	}
}
