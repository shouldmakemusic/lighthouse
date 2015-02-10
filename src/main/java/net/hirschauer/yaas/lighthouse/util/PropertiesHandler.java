package net.hirschauer.yaas.lighthouse.util;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

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
	
	public void store(Object[] objects) throws IOException {
		for (Object obj : objects) {
			store(obj);
		}
	}

	public void store(Object classType) throws IOException {

		for(Field field : classType.getClass().getDeclaredFields()){

			  String name = field.getName();
			  Annotation[] annotations = field.getDeclaredAnnotations();

			  for (Annotation annotation : annotations) {
				  
				  if (annotation.annotationType().equals(StoredProperty.class)) {
					  try {
						  for (PropertyDescriptor pd : Introspector.getBeanInfo(classType.getClass()).getPropertyDescriptors()) {
							  if (pd.getReadMethod() != null && !"class".equals(pd.getName()) && pd.getName().equals(name)) {
								  String value = (String) pd.getReadMethod().invoke(classType);
								  if (value == null) {
									  value = "";
								  }
								  applicationProps.put(classType.getClass().getName() + "|" + 
									  name, value);
								  logger.debug("save " + name + "=" + value);
							  }
						  }
					  } catch (Exception e) {
						  logger.error("Could not save property " + name + " from " + classType.getClass().getSimpleName(), e);
					  }
				  }
			  }
		}
	
		logger.debug("Filename to save: " + fileName);
		File f = new File(fileName);
        OutputStream out = new FileOutputStream( f );
        applicationProps.store(out, null);
	}
	
	public void setProperties(Object[] objects) {
		HashMap<String, Object> objs = new HashMap<String, Object>();
		for (Object obj : objects) {
			objs.put(obj.getClass().getName(), obj);
		}
		
		for (Object key : applicationProps.keySet()) {
			String propertyName = key.toString();
			if (propertyName.contains("|")) {
				String[] classMethod = propertyName.split("\\|");
				if (objs.containsKey(classMethod[0])) {
					try {
						for (PropertyDescriptor pd : Introspector.getBeanInfo(objs.get(classMethod[0]).getClass()).getPropertyDescriptors()) {
							if (pd.getWriteMethod() != null && !"class".equals(pd.getName()) && pd.getName().equals(classMethod[1])) {
								
								pd.getWriteMethod().invoke(objs.get(classMethod[0]), applicationProps.get(key));
								logger.debug("load " + classMethod[1] + "=" + applicationProps.get(key));
							}
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
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
