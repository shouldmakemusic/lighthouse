package net.hirschauer.yaas.lighthouse.model;

import java.io.Serializable;

public class ConfigEntry implements Serializable {

	private static final long serialVersionUID = 554632560561838564L;
	
	public static final String MIDI_NOTE_ON = "Midi Note On";
	public static final String MIDI_NOTE_OFF = "Midi Note Off";
	public static final String MIDI_CC = "Midi CC";

	protected String midiCommand;
	protected String midiValue;
	protected String midiFollowSignal;

	public ConfigEntry() {
		super();
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

	public String getMidiFollowSignal() {
		return midiFollowSignal;
	}

	public void setMidiFollowSignal(String midiFollowSignal) {
		this.midiFollowSignal = midiFollowSignal;
	}

}