package net.hirschauer.yaas.lighthouse.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.sciss.net.OSCMessage;

public class LogEntry {

	private static final DateFormat dateFormat = new SimpleDateFormat(
			"HH:mm:ss");

	private String message;
	private Object arg0;
	private Object arg1;
	private Object arg2;
	private String level;
	private long timestamp;

	public LogEntry() {
		timestamp = System.currentTimeMillis();
	}

	public LogEntry(OSCMessage m) {
		setMessage(m.getName());
		int count = m.getArgCount();
		if (count >= 1) {
			setArg0(m.getArg(0));
		}
		if (count >= 2) {
			setArg1(m.getArg(1));
		}
		if (count >= 3) {
			setArg2(m.getArg(2));
		}
		timestamp = System.currentTimeMillis();
	}

	public LogEntry(OSCMessageFromTask m) {
		setMessage(m.getName());
		if (m.getArgList() != null) {
			int count = m.getArgList().size();
			if (count >= 1) {
				setArg0(m.getArgList().get(0));
			}
			if (count >= 2) {
				setArg1(m.getArgList().get(1));
			}
			if (count >= 3) {
				setArg2(m.getArgList().get(2));
			}
		}
		timestamp = System.currentTimeMillis();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getArg0() {
		return arg0;
	}

	public void setArg0(Object arg0) {
		this.arg0 = arg0;
	}

	public Object getArg1() {
		return arg1;
	}

	public void setArg1(Object arg1) {
		this.arg1 = arg1;
	}

	public Object getArg2() {
		return arg2;
	}

	public void setArg2(Object arg2) {
		this.arg2 = arg2;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getTimeString() {
		return dateFormat.format(new Date(timestamp));
	}
}
