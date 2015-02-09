package net.hirschauer.yaas.lighthouse.model;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import de.sciss.net.OSCMessage;

public class OSCMessageFromTask {
	
	private String name;
	private String args;
	private ArrayList<String> argList;

	public OSCMessageFromTask(OSCMessage m) {
		
		this.setName(m.getName());
		
		this.argList = new ArrayList<String>();
		for (int i = 0; i<m.getArgCount(); i++) {
			this.argList.add(m.getArg(i).toString());
		}
		this.args = StringUtils.join(getArgList().toArray(), "|");
	}
	
	public OSCMessageFromTask(String message) {
		
		if (!message.contains("|")) {
			this.name = message;
		} else {
			String[] components = message.split("\\|");
			this.name = components[0];
			this.argList = new ArrayList<String>();
			
			for (int i = 1; i < components.length; i++) {
				this.argList.add(components[i]);				
			}
			this.args = message;
		}
	}
	
	@Override
	public String toString() {
		
		return getName() + "|" + getArgs();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public ArrayList<String> getArgList() {
		return argList;
	}

	public void setArgList(ArrayList<String> argList) {
		this.argList = argList;
	}
}
