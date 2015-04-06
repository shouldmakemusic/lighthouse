package net.hirschauer.yaas.lighthouse.model;

import net.hirschauer.yaas.lighthouse.exceptions.ConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigLightEntry extends ConfigEntry {

	private static final long serialVersionUID = 6828720410040824824L;
	private static final Logger logger = LoggerFactory.getLogger(ConfigLightEntry.class);
	
	public enum Command { PLAY, STOP, RECORD, OFFSET1, OFFSET2 };

	private Command command;
	
	public ConfigLightEntry() {}
	
	public ConfigLightEntry(Command command) {
		this.command = command;
	}
	
    public ConfigLightEntry(String line) throws ConfigurationException {
    	
    	// TODO: write tests for backward compability
    	// 	'PLAY : ['Midi Note On' , 10],
    	String[] midiDefinition = line.split(":");
    	if (midiDefinition.length < 2) {
    		throw new ConfigurationException("Wrong length: " + line);
    	}
    	setCommand(Command.valueOf(midiDefinition[0].trim()));

    	// ['Midi Note On' , 10],
    	midiDefinition[1] = midiDefinition[1].trim();
    	String command = midiDefinition[1].substring(1, midiDefinition[1].length() - 2);
    	String[] commandParts = command.split(",");
    	
    	// 'Midi Note On'
    	commandParts[0] = commandParts[0].trim();
    	String midiCommand = commandParts[0].substring(1, commandParts[0].length() - 1);
    	if (midiCommand.equals(MIDI_CC) || midiCommand.equals(MIDI_NOTE_ON)) {
    		this.midiCommand = midiCommand;
    	} else {
    		throw new ConfigurationException("Midi command not valid in: " + line);
    	}
    	
    	// 10
    	commandParts[1] = commandParts[1].trim();
    	setMidiValue(commandParts[1]);
    	
    }

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}
	
	@Override
	public String toString() {
    	StringBuffer sb = new StringBuffer();
    	
    	sb.append(getCommand());
    	sb.append(" : ['");
    	sb.append(getMidiCommand());
    	sb.append("' , ");
    	sb.append(getMidiValue());
    	sb.append("],");
    	return sb.toString();

	}
}
