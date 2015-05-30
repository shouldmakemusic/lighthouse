package net.hirschauer.yaas.lighthouse.model;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YaasConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(YaasConfiguration.class);
	
	private String yaasErrorLogFile;
	private String yaasStdOutLogFile;
	private String yaasConfigFile;
	private String yaasLocation;
	private String name;
	private String wiiConfigFile;
	
	public YaasConfiguration(String yaasLocation) {
		logger.info("Create yaas configuration for " + yaasLocation);
		File dir = new File(yaasLocation);
		if (dir.exists()) {
			this.yaasLocation = yaasLocation;
			
			File stdErr = new File(dir.getAbsolutePath() + File.separatorChar + "stderr.txt");
			if (stdErr.exists()) {
				setYaasErrorLogFile(stdErr.getAbsolutePath());
				logger.debug("Error log: " + stdErr.getAbsolutePath());
			}
			stdErr = null;
			
			File stdOut = new File(dir.getAbsolutePath() + File.separatorChar + "stdout.txt");
			if (stdOut.exists()) {
				setYaasStdOutLogFile(stdOut.getAbsolutePath());
				logger.debug("Stdout log: " + stdOut.getAbsolutePath());
			}
			stdOut = null;
			
			File config = new File(dir.getAbsolutePath() + File.separatorChar + 
					"config" + File.separatorChar + "midi_mapping.cfg");
			if (config.exists()) {
				setYaasConfigFile(config.getAbsolutePath());
				logger.debug("Config file: " + config.getAbsolutePath());
			}
			config = null;
			
			File wii = new File(dir.getAbsolutePath() + File.separatorChar + 
					"config" + File.separatorChar + "wii_mapping.cfg");
			if (wii.exists()) {
				wiiConfigFile = wii.getAbsolutePath();
				logger.debug("Config file: " + wiiConfigFile);
			}
			wii = null;
			
			this.name = dir.getName();
			dir = null;
		}		
	}
	
	public String getYaasErrorLogFile() {
		return yaasErrorLogFile;
	}

	public void setYaasErrorLogFile(String yaasErrorLogFile) {
		this.yaasErrorLogFile = yaasErrorLogFile;
	}

	public String getYaasStdOutLogFile() {
		return yaasStdOutLogFile;
	}

	public void setYaasStdOutLogFile(String yaasStdOutLogFile) {
		this.yaasStdOutLogFile = yaasStdOutLogFile;
	}

	public String getYaasConfigFile() {
		return yaasConfigFile;
	}

	public void setYaasConfigFile(String yaasConfigFile) {
		this.yaasConfigFile = yaasConfigFile;
	}

	public String getYaasLocation() {
		return yaasLocation;
	}

	public void setYaasLocation(String yaasLocation) {
		this.yaasLocation = yaasLocation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWiiConfigFile() {
		return wiiConfigFile;
	}
}
