package net.hirschauer.yaas.lighthouse.model;

import java.io.Serializable;

import net.hirschauer.yaas.lighthouse.exceptions.ConfigurationException;

public class ConfigMidi extends ConfigCommand implements Serializable {

	private static final long serialVersionUID = 554632560561838564L;
	
	public static final String MIDI_NOTE_ON = "Midi Note On";
	public static final String MIDI_NOTE_OFF = "Midi Note Off";
	public static final String MIDI_CC = "Midi CC";

	protected String midiCommand;
	protected String midiValue;
	protected String midiFollowSignal;

	public ConfigMidi() {
		super();
	}
	
	public ConfigMidi(String line, String prefix) throws ConfigurationException {
		super(line, prefix);
	}
	
	public void init(ConfigMidi midiInput) {
		this.midiCommand = midiInput.midiCommand;
		this.midiFollowSignal = midiInput.midiFollowSignal;
		this.midiValue = midiInput.midiValue;
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

	@Override
	public void setAdditionalValue(String additionalValue) {
		setMidiFollowSignal(additionalValue);
	}

	@Override
	public void setConfigValue(String configValue) {
		setMidiValue(configValue);
	}

	@Override
	public String getAdditionalValue() {		
		return getMidiFollowSignal();
	}

	@Override
	public String getConfigValue() {
		return getMidiValue();
	}

	@Override
	public void setConfigCommand(String configCommand) {
		this.midiCommand = configCommand;
	}

	@Override
	public String getConfigCommand() {
		return this.midiCommand;
	}

}