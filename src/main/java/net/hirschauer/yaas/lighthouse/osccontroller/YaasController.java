package net.hirschauer.yaas.lighthouse.osccontroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.adapter.JavaBeanStringProperty;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import javax.sound.midi.InvalidMidiDataException;

import net.hirschauer.yaas.lighthouse.LightHouseMidi;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.OSCMessageFromTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCMessage;

public class YaasController extends OSCController {

	private static final Logger logger = LoggerFactory.getLogger(YaasController.class);

	public ObservableMap<String, List<String>> yaasCommands = FXCollections.observableHashMap();
	private String yaasErrorLogFile;
	private String yaasStdOutLogFile;
	private String yaasConfigFile;
	public JavaBeanStringProperty yaasErrorLogFileProperty;
	public JavaBeanStringProperty yaasConfigFileProperty;
	public JavaBeanStringProperty yaasStdOutLogFileProperty;
	
	private static YaasController instance;
	
	public YaasController(LightHouseOSCServer oscServer, LightHouseMidi midi) {
		super(oscServer, midi);
		
		JavaBeanStringPropertyBuilder sb = JavaBeanStringPropertyBuilder.create();
		sb.bean(this);
		try {
			sb.name("yaasErrorLogFile");
			yaasErrorLogFileProperty = sb.build();
			sb.name("yaasStdOutLogFile");
			yaasStdOutLogFileProperty = sb.build();
			sb.name("yaasConfigFile");
			yaasConfigFileProperty = sb.build();
		} catch (NoSuchMethodException e) {
			logger.error(e.getMessage(), e);
		}
		instance = this;
		logger.debug("Initialized");
	}
	
	public static YaasController getInstance() {
		return instance;
	}

	@Override
	public void handleMessage(OSCMessage m) throws InvalidMidiDataException {

		if (m.getName().startsWith("/yaas/log")) {
			
			updateMessage(m, OSCMessageFromTask.TYPE_YAAS);
		} else if (m.getName().startsWith("/yaas/config")) {
			
			if (m.getName().equals("/yaas/config/errorfile")) {
				setYaasErrorLogFile((String) m.getArg(0));
			} else if (m.getName().equals("/yaas/config/configfile")) {
				setYaasConfigFile((String) m.getArg(0));
			} else if (m.getName().equals("/yaas/config/stdoutfile")) {
				setYaasStdOutLogFile((String) m.getArg(0));
			}
			updateMessage(m, OSCMessageFromTask.TYPE_YAAS);
		} else if (m.getName().startsWith("/yaas/commands")) {
			
			if (m.getName().endsWith("clear")) {
				yaasCommands.clear();
			} else if (m.getName().endsWith("list")) {
				String className = (String) m.getArg(0);
				String methodName = (String) m.getArg(1);
				if (!yaasCommands.containsKey(className)) {
					yaasCommands.put(className, new ArrayList<String>());
				}
				yaasCommands.get(className).add(methodName);
			} else if (m.getName().endsWith("done")) {
				logger.info("Got available commands from YAAS");
				updateMessage(new OSCMessageFromTask("Got available commands from YAAS"));
			}
		} else {			
			updateMessage(m);
		}
	}
	
	public void fetchAvailableCommandsFromYaas() {
		
		OSCMessage m = new OSCMessage("/yaas/controller/send/info");
		try {
			sendToYaas(m);
		} catch (IOException e) {
			logger.error("Could not request controller info", e);
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
}
