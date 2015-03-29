package net.hirschauer.yaas.lighthouse.model;

import java.io.Serializable;

public class ConfigEntry implements Serializable {

	private static final long serialVersionUID = 8907267743815270553L;

	private String midiCommand;
	private String midiValue;
	private String midiFollowSignal;
	private String controller;
	private String command;
	private String value1;
	private String value2;
	private String value3;
	
	public ConfigEntry() {
	}
	
	public String getMidiCommand() {
		return midiCommand;
	}
	public void setMidiCommand(String midiCommand) {
		this.midiCommand = midiCommand;
	}
	public String getMidiValue() {
		return midiValue;
	}
	public void setMidiValue(String midiValue) {
		this.midiValue = midiValue;
	}
	public String getController() {
		return controller;
	}
	public void setController(String controller) {
		this.controller = controller;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getValue1() {
		return value1;
	}
	public void setValue1(String value1) {
		this.value1 = value1;
	}
	public String getValue2() {
		return value2;
	}
	public void setValue2(String value2) {
		this.value2 = value2;
	}
	public String getValue3() {
		return value3;
	}
	public void setValue3(String value3) {
		this.value3 = value3;
	}

	public String getMidiFollowSignal() {
		return midiFollowSignal;
	}

	public void setMidiFollowSignal(String midiFollowSignal) {
		this.midiFollowSignal = midiFollowSignal;
	}
}
