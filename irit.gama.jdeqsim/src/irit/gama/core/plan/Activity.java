/*******************************************************************************************************
 *
 * Activity.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.core.plan;

import irit.gama.core.IPlanElement;
import irit.gama.core.unit.Road;
import msi.gama.util.GamaDate;

/**
 * Some comments:
 * <ul>
 * <li>When we developed the API, we were afraid of potentially inconsistent
 * Coord, LinkId and FacilityId. As a result, one can set the Activity either
 * from Coord or from LinkId, but not from both. The FacilityId cannot be set at
 * all (in the API).
 * </ul>
 *
 */
public class Activity implements IPlanElement {

	// TODO : WTF is the difference between duration and end time ? Flexibility ?
	private double duration;
	private GamaDate endTime; // We use only end time
	private Road road; // The road where the activity took place

	public Activity(GamaDate endTime, double duration, Road road) {
		super();
		this.endTime = endTime;
		this.duration = duration;
		this.road = road;
	}

	public void setMaximumDuration(double duration) {
		this.duration = duration;
	}

	public void setEndTime(GamaDate endTime) {
		this.endTime = endTime;
	}

	public Road getRoad() {
		return road;
	}

	public double getMaximumDuration() {
		return duration;
	}

	public GamaDate getEndTime() {
		return endTime;
	}

}
