package net.hirschauer.yaas.lighthouse.model.osc;

import java.util.ArrayList;

public class OSCMessage {
	
	protected String name;
	protected ArrayList<String> args = new ArrayList<String>();

	public OSCMessage(String name) {
		this.name = name;
	}
	
	public OSCMessage(String name, Object[] args) {
		this.name = name;
		for (Object arg : args) {
			this.args.add(arg.toString());
		}
	}
	
	public OSCMessage(String name, String... params) {
		
		this.name = name;
		for (Object arg : params) {
			this.args.add(arg.toString());
		}
	}
	public de.sciss.net.OSCMessage getMessage() {
		return new de.sciss.net.OSCMessage(name, args.toArray());
	}
}
