package net.hirschauer.yaas.lighthouse.model;

import net.hirschauer.yaas.lighthouse.exceptions.ConfigurationException;

public class ConfigWii extends ConfigCommand {

	private static final long serialVersionUID = 6828720410040824824L;
	
//	public enum WiiCommand { Up, Down, Left, Right, A, B, One, Two, Plus, Minus, Home  };
	
	protected String message;
	protected String mode;
	protected String switchToMode;
	
	public ConfigWii() {
		super();
	}
	
	public ConfigWii(String line, String prefix) throws ConfigurationException {
		super(line, prefix);
	}
	
	public void init(ConfigWii wiiInput) {
		this.message = wiiInput.message;
		this.mode = wiiInput.mode;
		this.switchToMode = wiiInput.switchToMode;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public String getMode() {
		return this.mode;
	}
	
	public void setSwitchToMode(String switchToMode) {
		this.switchToMode = switchToMode;
	}
	
	public String getSwitchToMode() {
		return this.switchToMode;
	}
	
	@Override
	public void setAdditionalValue(String additionalValue) {
		this.switchToMode = additionalValue;
	}

	@Override
	public String getAdditionalValue() {		
		return switchToMode;
	}

	@Override
	public void setConfigValue(String configValue) {
		this.mode = configValue;
	}

	@Override
	public String getConfigValue() {
		return mode;
	}

	@Override
	public void setConfigCommand(String configCommand) {
		this.message = configCommand;
	}

	@Override
	public String getConfigCommand() {
		return message;
	}
}
