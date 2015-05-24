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
	
	public ConfigMidi(String line) throws ConfigurationException {
		super(line);
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
	protected void setAdditionalValue(String additionalValue) {
		setMidiFollowSignal(additionalValue);
	}

	@Override
	protected void setConfigValue(String configValue) {
		setMidiValue(configValue);
	}

	@Override
	protected String getAdditionalValue() {		
		return getMidiCommand();
	}

	@Override
	protected String getConfigValue() {
		return getMidiValue();
	}

}