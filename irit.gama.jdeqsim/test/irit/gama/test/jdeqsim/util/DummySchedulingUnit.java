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

package irit.gama.test.jdeqsim.util;

import irit.gama.core.SchedulingUnit;
import irit.gama.core.unit.Scheduler;
import msi.gama.runtime.IScope;

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
