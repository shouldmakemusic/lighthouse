package net.hirschauer.yaas.lighthouse.model;

import org.apache.commons.lang3.StringUtils;

import net.hirschauer.yaas.lighthouse.exceptions.ConfigurationException;

public class ConfigLightEntry extends ConfigEntry {

	private static final long serialVersionUID = 6828720410040824824L;
	
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
    	String command = midiDefinition[0].trim();
    	if (command.startsWith("'")) {
    		command = command.substring(1, command.length() - 1);
    	}
    	setCommand(Command.valueOf(command));

    	// ['Midi Note On' , 10(, 8)],
    	midiDefinition[1] = midiDefinition[1].trim();
    	String midi_command = midiDefinition[1].substring(1, midiDefinition[1].length() - 2);
    	String[] commandParts = midi_command.split(",");
    	
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
    	
    	if (commandParts.length >= 3) {
    		commandParts[2] = commandParts[2].trim();
    		if (StringUtils.isNoneEmpty(commandParts[2])) {
    			setMidiFollowSignal(commandParts[2]);
    		}
    	}
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
    	
    	sb.append("'");
    	sb.append(getCommand());
    	sb.append("'");
    	sb.append(" : ['");
    	sb.append(getMidiCommand());
    	sb.append("', ");
    	sb.append(getMidiValue());
    	if (getMidiFollowSignal() != null) {
        	sb.append(", ");
    		sb.append(getMidiFollowSignal());
    	}
    	sb.append("],");
    	return sb.toString();

	}
}
