/*******************************************************************************************************
 *
 * Logger.java, in plugin irit.gama.jdeqsim, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8.1)
 *
 * (c) 2007-2021 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/

package irit.gama.common.logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import irit.gama.common.Param;
import irit.gama.core.INamable;
import irit.gama.core.message.Message;

/**
 * Logger for debugging
 * 
 * @author Jean-François Erdelyi
 */
public class Logger {
	private static PriorityQueue<LogData> messages = new PriorityQueue<LogData>();

	public static void addMessage(INamable emitter) {
		if (Param.DEBUG_ON && (Param.DEBUG_LEVEL == Param.LogLevel.traceOnly || Param.DEBUG_LEVEL == Param.LogLevel.all)
				&& emitter != null) {
			messages.add(new LogData(emitter, null, null, null, new Date(System.currentTimeMillis())));
		}
	}

	public static void addMessage(INamable emitter, String comment) {
		if (Param.DEBUG_ON && (Param.DEBUG_LEVEL == Param.LogLevel.traceOnly || Param.DEBUG_LEVEL == Param.LogLevel.all)
				&& emitter != null) {
			messages.add(new LogData(emitter, null, null, comment, new Date(System.currentTimeMillis())));
		}
	}

	public static void addMessage(INamable emitter, INamable receiver, Message message) {
		if (Param.DEBUG_ON
				&& (Param.DEBUG_LEVEL == Param.LogLevel.scheduleOnly || Param.DEBUG_LEVEL == Param.LogLevel.all)
				&& emitter != null) {
			messages.add(new LogData(emitter, receiver, message, null, new Date(System.currentTimeMillis())));
		}
	}

	public static void addMessage(INamable emitter, INamable receiver, Message message, String comment) {
		if (Param.DEBUG_ON
				&& (Param.DEBUG_LEVEL == Param.LogLevel.scheduleOnly || Param.DEBUG_LEVEL == Param.LogLevel.all)
				&& emitter != null) {
			messages.add(new LogData(emitter, receiver, message, comment, new Date(System.currentTimeMillis())));
		}
	}

	public static void print() {
		Iterator<LogData> value = messages.iterator();
		while (value.hasNext()) {
			System.out.println(value.next());
		}
	}

	// Print by vehicle (for scheduling data)
	public static void printByVehicle() {
		Map<String, List<String>> vehiclesLog = new HashMap<String, List<String>>();

		Iterator<LogData> value = messages.iterator();
		while (value.hasNext()) {
			LogData next = value.next();
			if (next.getMessage() != null && next.getMessage().getVehicle() != null) {
				String key = next.getMessage().getVehicle().getName();
				if (vehiclesLog.containsKey(key)) {
					vehiclesLog.get(key).add(next.toString());
				} else {
					vehiclesLog.put(key, new ArrayList<>());
					vehiclesLog.get(key).add(next.toString());
				}
			}
		}

		Iterator<Entry<String, List<String>>> mapValue = vehiclesLog.entrySet().iterator();
		while (mapValue.hasNext()) {
			Entry<String, List<String>> next = mapValue.next();
			System.out.println(next.getKey());
			for (var data : next.getValue()) {
				System.out.println("\t" + data);
			}
		}
	}

	public static void flush() {
		messages = new PriorityQueue<>();
	}
}
