/*******************************************************************************************************
 *
 * ITool.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.common;

import java.time.temporal.ChronoUnit;

import irit.gama.core.plan.Activity;
import msi.gama.util.GamaDate;

/**
 * Tools of the JDEQSIM simulation
 * 
 * @author Jean-François Erdelyi
 */
public interface ITool {
	public enum ActivityDurationInterpretation {
		minOfDurationAndEndTime, tryEndTimeThenDuration
	}

	// TODO : It is not clear, see Activity
	public static GamaDate decideOnActivityEndTime(Activity act, GamaDate now,
			ActivityDurationInterpretation activityDurationInterpretation) {

		switch (activityDurationInterpretation) {
		case tryEndTimeThenDuration:
			if (act.getEndTime() != null) {
				return act.getEndTime();
			} else if (act.getMaximumDuration() > 0.0) {
				return now.plus(act.getMaximumDuration() * 1000.0, ChronoUnit.MILLIS);
			} else {
				return null;
			}

		case minOfDurationAndEndTime:
			if (act.getEndTime() == null && act.getMaximumDuration() < 0.0) {
				return null;
			} else if (act.getMaximumDuration() <= 0.0) {
				return act.getEndTime();
			} else if (act.getEndTime() == null) {
				return now.plus(act.getMaximumDuration() * 1000.0, ChronoUnit.MILLIS);
			} else {
				GamaDate durationBasedEndTime = now.plus(act.getMaximumDuration() * 1000.0, ChronoUnit.MILLIS);
				return act.getEndTime().isSmallerThan(durationBasedEndTime, false) ? act.getEndTime()
						: durationBasedEndTime;
			}

		default:
			throw new IllegalArgumentException(
					"Unsupported 'activityDurationInterpretation' enum type: " + activityDurationInterpretation);
		}
	}
}
