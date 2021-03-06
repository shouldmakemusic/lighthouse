package net.hirschauer.yaas.lighthouse.model;

import java.io.Serializable;

import net.hirschauer.yaas.lighthouse.exceptions.ConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConfigCommand implements Serializable {

	private static final long serialVersionUID = 8907267743815270553L;
	private static final Logger logger = LoggerFactory.getLogger(ConfigCommand.class);

	private String controller;
	private String command;
	private String value1;
	private String value2;
	private String value3;
	
	public ConfigCommand() {
	}
	
	public ConfigCommand(String line, String prefix) throws ConfigurationException {
		load(line, prefix);
	}
	
    public void load(String line, String prefix) throws ConfigurationException {
    	
    	// TODO: write tests for backward compability
    	// 	8 : ['TrackController' , 'toggle_solo_track' , [0, 1]],
    	String[] midiCommand = line.split(":");
    	if (midiCommand.length < 2) {
    		if (prefix == null) {
    			throw new ConfigurationException("Wrong length: " + line);
    		} else {
    			midiCommand = new String[] {prefix, line};
    		}
    	} 
    	setConfigValue(midiCommand[0].trim());
    	
    	// ['TrackController' , 'toggle_solo_track' , [0, 1]],
    	midiCommand[1] = midiCommand[1].trim();
    	String command = midiCommand[1].substring(1, midiCommand[1].length() - 2);
    	String[] commandParts = command.split(",");
    	
    	// 'TrackController'
    	commandParts[0] = commandParts[0].trim();
    	setController(commandParts[0].substring(1, commandParts[0].length() - 1));
    	
    	// 'toggle_solo_track'
    	commandParts[1] = commandParts[1].trim();
    	setCommand(commandParts[1].substring(1, commandParts[1].length() - 1));
    	
    	// [0 1] or [0 1] [234]
    	boolean hasFollowSignal = false;
    	String concValues = commandParts[2] + ",";
    	for (int i=3; i < commandParts.length; i++) {
    		if (commandParts[i].trim().startsWith("[")) {
    			hasFollowSignal = true;
    			break;
    		}
    		concValues += commandParts[i] + ",";
    	}
    	concValues = concValues.trim();
    	concValues = concValues.substring(1, concValues.length() - 1);
    	String[] values = concValues.split(",");
    	if (values.length >= 1) {
    		setValue1(fixEntry(values[0]));
    	}
    	if (values.length >= 2) {
    		setValue2(fixEntry(values[1]));
    	}
    	if (values.length >= 3) {
    		setValue3(fixEntry(values[2]));
    	}
    	if (hasFollowSignal) {
    		String followSignal = commandParts[commandParts.length -1].trim();
    		logger.debug("Follow Signal: " + followSignal);
    		setAdditionalValue(followSignal.substring(1, followSignal.length() - 1));
    	}
    }

	abstract public void setAdditionalValue(String additionalValue);
	abstract public String getAdditionalValue();

	abstract public void setConfigValue(String configValue);
	abstract public String getConfigValue();

	abstract public void setConfigCommand(String configCommand);
	abstract public String getConfigCommand();

	private String fixEntry(String value) {

		value = value.trim();
		if (value.endsWith("]")) {
			value = value.substring(0, value.length() -1 );
		}
		if (value.startsWith("'") && !value.equals("'")) {
			value = value.substring(1, value.length() -1 );
		}
		if (value.startsWith("[")) {
			value = value.substring(1);
		}
		if (value.endsWith("]")) {
			value = value.substring(0, value.length() -1 );
		}
		return value;
    }
	
	public ConfigCommand(ConfigCommand midiInput) {
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
	
	public String getAsString() {
		
		StringBuffer sb = new StringBuffer();    	
    	sb.append("['");
    	sb.append(getController());
    	sb.append("' , '");
    	sb.append(getCommand());
    	sb.append("' , [");
    	if (StringUtils.isNotEmpty(getValue1())) {
    		if (!StringUtils.isNumeric(getValue1())) {
    			sb.append("'");
    		}
    		sb.append(getValue1());
    		if (!StringUtils.isNumeric(getValue1())) {
    			sb.append("'");
    		}
    	}
    	if (StringUtils.isNotEmpty(getValue2()) || StringUtils.isNotEmpty(getValue3())) {
    		
    		if (StringUtils.isEmpty(getValue1())) {
    			sb.append("' '");
    		}
			sb.append(", ");
	    	if (StringUtils.isNotEmpty(getValue2())) {
	    		if (!StringUtils.isNumeric(getValue2())) {
	    			sb.append("'");
	    		}
	    		sb.append(getValue2());
	    		if (!StringUtils.isNumeric(getValue2())) {
	    			sb.append("'");
	    		}
	    	} else {
	    		sb.append("' '");
	    	}
	    	if (StringUtils.isNotEmpty(getValue3())) {
				sb.append(", ");
	    		if (!StringUtils.isNumeric(getValue3())) {
	    			sb.append("'");
	    		}
	    		sb.append(getValue3());
	    		if (!StringUtils.isNumeric(getValue3())) {
	    			sb.append("'");
	    		}
	    	}
    	}
    	sb.append("]");
    	if (StringUtils.isNotEmpty(getAdditionalValue())) {
    		sb.append(", [");
    		sb.append(getAdditionalValue());
    		sb.append("]");
    	}
    	sb.append("],");
    	return sb.toString();
	}
	
	@Override
    public String toString() {
		StringBuffer sb = new StringBuffer();    	
    	sb.append(getConfigValue());
    	sb.append(" : ");
    	sb.append(getAsString());
    	return sb.toString();
    }

}
