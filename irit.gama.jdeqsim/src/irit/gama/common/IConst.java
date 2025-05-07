/*******************************************************************************************************
 *
 * IConst.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.common;

/**
 * Constants JDEQSIM
 * 
 * @author Jean-François Erdelyi
 */
public interface IConst {
	/**
	 * The priorities of the messages. a higher priority comes first in the message
	 * queue (when same time) usage: for example a person has a enter road message
	 * at the same time as leaving the previous road (need to keep the messages in
	 * right order) for events with same time stamp: <br>
	 * leave < arrival < departure < enter especially for testing this is important
	 */
	public static final int PRIORITY_LEAVE_ROAD_MESSAGE = 200;
	public static final int PRIORITY_ARRIVAL_MESSAGE = 150;
	public static final int PRIORITY_DEPARTUARE_MESSAGE = 125;
	public static final int PRIORITY_ENTER_ROAD_MESSAGE = 100;
}
