package net.hirschauer.yaas.lighthouse.model;

import de.sciss.net.OSCMessage;

public class LogEntry {
	
	private String message;
	private Object arg0;
	private Object arg1;
	private Object arg2;
	
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
}
