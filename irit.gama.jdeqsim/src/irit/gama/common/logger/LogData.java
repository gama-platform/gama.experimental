/*******************************************************************************************************
 *
 * LogData.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.common.logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import irit.gama.core.INamable;
import irit.gama.core.message.Message;

/**
 * Log data for debugging
 * 
 * @author Jean-François Erdelyi
 */
public class LogData implements Comparable<LogData> {
	private INamable emitter;
	private INamable receiver;
	private Message message;
	private String comment;
	private String date;

	public LogData(INamable emitter, INamable receiver, Message message, String comment, Date date) {
		this.emitter = emitter;
		this.receiver = receiver;
		this.message = message;
		this.comment = comment;

		// Conversion to ISO time
		SimpleDateFormat sdf;
		sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));
		this.date = sdf.format(date);
	}

	public INamable getEmitter() {
		return emitter;
	}

	public INamable getReceivingUnit() {
		return receiver;
	}

	public Message getMessage() {
		return message;
	}

	public String getComment() {
		return comment;
	}

	public String getDate() {
		return date;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[");
		sb.append(date);
		sb.append("] message ");

		if (message != null) {
			sb.append(message.getName());
			sb.append(" (");
			sb.append(message.getVehicle().getName());
			sb.append(") at ");
			sb.append(message.getMessageArrivalTime().toISOString());
			sb.append(" ");
		}

		sb.append("from ");
		sb.append(emitter.getName());

		if (receiver != null) {
			sb.append(" to ");
			sb.append(receiver.getName());
		}

		if (comment != null) {
			sb.append(" with the comment \"");
			sb.append(comment);
			sb.append("\"");
		}

		return sb.toString();
	}

	@Override
	public int compareTo(LogData otherMessage) {
		return date.compareTo(otherMessage.getDate());
	}
}
