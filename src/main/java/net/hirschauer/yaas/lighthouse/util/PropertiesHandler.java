package net.hirschauer.yaas.lighthouse.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import net.hirschauer.yaas.lighthouse.visual.ConfigurationController;
import net.hirschauer.yaas.lighthouse.visual.YaasLogController;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(PropertiesHandler.class);

	private URL dir;
	private Properties applicationProps;

	private String fileName;
		
	public PropertiesHandler() {
		loadProperties();
	}
	
	public void store(IStorable... objects) {
		try {
			for (IStorable obj : objects) {
				obj.store(applicationProps);
			}
			logger.debug("Filename to save: " + fileName);
			File f = new File(fileName);
			OutputStream out = new FileOutputStream( f );
			applicationProps.store(out, null);
			out.close();
			logger.debug("saved");
		} catch (IOException e) {
			logger.error("Could not store properties", e);
		}
	}
	
	public void load(IStorable... objects) {
		for (IStorable obj : objects) {
			obj.load(applicationProps);
		}
	}

	protected void loadProperties() {
		dir = getClass().getProtectionDomain().getCodeSource().getLocation();
		logger.debug("Dir: " + dir);

		Properties defaultProps = new Properties();
		try {
			InputStream in = getClass().getResourceAsStream(
					"/default.properties");
			if (in != null) {
				defaultProps.load(in);
				in.close();
				logger.debug(defaultProps.getProperty("init",
						"did not load default properties"));
			}
		} catch (IOException e) {
			logger.error("Could not load default properties", e);
		}
		applicationProps = new Properties(defaultProps);
		try {
			String propdir = dir.getFile();
			if (propdir.endsWith(File.separator)) {
				propdir = propdir.substring(0, propdir.length() - 1);
			}
			if (propdir.endsWith("classes")) {
				propdir = propdir.substring(0, propdir.length() - 8);
			}
			if (propdir.endsWith("target")) {
				propdir = propdir.substring(0, propdir.length() - 7);
			}
			fileName = propdir + File.separator + "lighthouse.properties";
			logger.debug("Application properties: " + fileName);
			File propertiesFile = new File(fileName);
			if (!propertiesFile.exists()) {
				propertiesFile.createNewFile();
			}

			FileInputStream in = FileUtils.openInputStream(propertiesFile);
			applicationProps.load(in);
			in.close();
		} catch (IOException e) {
			logger.error("Could not load properties", e);
		}
	}

}
