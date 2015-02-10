package net.hirschauer.yaas.lighthouse.model;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import de.sciss.net.OSCMessage;

public class OSCMessageFromTask {
	
	public static final String TYPE_OSC = "/osc";
	public static final String TYPE_YAAS = "/yaas/log";
	public static final String TYPE_WII = "/wii";
	public static final String TYPE_ANDROID = "/android";
	
	private String type;
	private String name;
	private ArrayList<String> argList;

	public OSCMessageFromTask(OSCMessage m) {
		init(m, OSCMessageFromTask.TYPE_OSC);
	}
	public OSCMessageFromTask(OSCMessage m, String type) {
		init(m, type);
	}
	
	protected void init(OSCMessage m, String type) {
		this.setName(m.getName());
		
		this.argList = new ArrayList<String>();
		for (int i = 0; i<m.getArgCount(); i++) {
			this.argList.add(m.getArg(i).toString());
		}
		this.type = type;
	}
	
	public OSCMessageFromTask(String message) {
		
		if (!message.contains("|")) {
			this.name = message;
			this.setType(TYPE_OSC);
		} else {
			String[] components = message.split("\\|");
			this.type = components[0];
			this.name = components[1];
			this.argList = new ArrayList<String>();
			
			for (int i = 2; i < components.length; i++) {
				this.argList.add(components[i]);				
			}
		}
	}
	
	@Override
	public String toString() {
		
		String output = getType() + "|" + getName();
		if (this.argList != null && this.argList.size() > 0) {
			output += "|" + StringUtils.join(this.argList.toArray(), "|");
		}
		return output;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getArgList() {
		return argList;
	}

	public void setArgList(ArrayList<String> argList) {
		this.argList = argList;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
