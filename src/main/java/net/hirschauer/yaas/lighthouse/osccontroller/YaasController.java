package net.hirschauer.yaas.lighthouse.osccontroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import javax.sound.midi.InvalidMidiDataException;

import net.hirschauer.yaas.lighthouse.LightHouseMidi;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.YaasConfiguration;
import net.hirschauer.yaas.lighthouse.model.osc.OSCMessageFromTask;
import net.hirschauer.yaas.lighthouse.visual.YaasLogController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCMessage;

public class YaasController extends OSCController {

	private static final Logger logger = LoggerFactory.getLogger(YaasController.class);

	public ObservableMap<String, List<String>> yaasCommands = FXCollections.observableHashMap();
	private Map<String, List<String>> yaasCommandsReceive = new HashMap<String, List<String>>();
		
	private static YaasController instance;
	
	private YaasConfiguration yaasConfiguration;
	public JavaBeanObjectProperty<YaasConfiguration> yaasConfigurationProperty;
	
	public static YaasController getInstance() {
		
		return instance;
	}
	public YaasController(LightHouseOSCServer oscServer, LightHouseMidi midi) {
		super(oscServer, midi);
		
		logger.debug("YaasController created");
		instance = this;
		
		@SuppressWarnings("unchecked")
		JavaBeanObjectPropertyBuilder<YaasConfiguration> pb = JavaBeanObjectPropertyBuilder.create();
		pb.bean(this);
		pb.name("yaasConfiguration");
		try {
			yaasConfigurationProperty = pb.build();
		} catch (NoSuchMethodException e) {
			logger.error("Could not initialize yaasConfigurationProperty", e);
		}
		
		logger.debug("Initialized");
	}

	@Override
	public void handleMessage(OSCMessage m) throws InvalidMidiDataException {

		if (m.getName().startsWith("/yaas/log")) {
			
			updateMessage(m, OSCMessageFromTask.TYPE_YAAS);
			YaasLogController.log(m);

		} else if (m.getName().startsWith("/yaas/config")) {
			
			if (m.getName().equals("/yaas/config/location")) {
				yaasConfigurationProperty.set(new YaasConfiguration((String)m.getArg(0)));
			}
			updateMessage(m, OSCMessageFromTask.TYPE_YAAS);
			
		} else if (m.getName().startsWith("/yaas/commands")) {
			
			if (m.getName().endsWith("clear")) {
				yaasCommandsReceive = new HashMap<String, List<String>>();
			} else if (m.getName().endsWith("list")) {
				String className = (String) m.getArg(0);
				String methodName = (String) m.getArg(1);
				if (!yaasCommandsReceive.containsKey(className)) {
					yaasCommandsReceive.put(className, new ArrayList<String>());
				}
				yaasCommandsReceive.get(className).add(methodName);
			} else if (m.getName().endsWith("done")) {
				logger.info("Got available commands from YAAS");
				updateMessage(new OSCMessageFromTask("Got available commands from YAAS"));
				yaasCommands.clear();
				yaasCommands.putAll(yaasCommandsReceive);
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

	public YaasConfiguration getYaasConfiguration() {
		return yaasConfiguration;
	}

	public void setYaasConfiguration(YaasConfiguration yaasConfiguration) {
		this.yaasConfiguration = yaasConfiguration;
	}
}
