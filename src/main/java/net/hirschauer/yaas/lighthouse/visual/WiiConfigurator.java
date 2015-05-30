package net.hirschauer.yaas.lighthouse.visual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import net.hirschauer.yaas.lighthouse.model.ConfigCommand;
import net.hirschauer.yaas.lighthouse.model.ConfigWii;
import net.hirschauer.yaas.lighthouse.model.YaasConfiguration;
import net.hirschauer.yaas.lighthouse.osccontroller.YaasController;
import net.hirschauer.yaas.lighthouse.visual.components.ConfigTable;
import net.hirschauer.yaas.lighthouse.visual.components.RowEditor;
import net.hirschauer.yaas.lighthouse.visual.components.WiiRowEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We want to receive osc messages indirectly from the wii and send them to lighthouse (per osc?)
 * 
 * The settings will just be stored in the lighthouse.properties (at least for now)
 * Later: you can load/save the configuration in a different file if you want
 * Later: signals will be optionally sent via midi
 * Later: or alternativly direct to yaas
 * 
 * @author manuelhirschauer
 */
public class WiiConfigurator extends Configurator {
	
	private static final Logger logger = LoggerFactory.getLogger(WiiConfigurator.class);
	
    @FXML
    private Button btnAdd, btnWiiSettings, btnSend, btnSendTemp, btnLoad, btnSave, btnClear;
    
    @FXML
    private AnchorPane configTablePane, sensorPane;

	private ConfigTable configController;
	
	public static WiiConfigurator show(AnchorPane configurationTablePane) throws IOException {
		
		FXMLLoader loader = new FXMLLoader(YaasConfigurator.class.getResource("/view/configurators/WiiConfigurator.fxml"));
		AnchorPane tabContent = (AnchorPane) loader.load();
        configurationTablePane.getChildren().add(tabContent);

		WiiConfigurator wiiConfigurator = loader.getController();        
        return wiiConfigurator;
	}
	
	public WiiConfigurator() {
		super();
	}
	
    @FXML
    public void initialize() throws IOException {
		logger.debug("init");

		configController = ConfigTable.show(configTablePane);
		configController.setConfigurator(this);
		
		SensorController.show(sensorPane);
		
		btnAdd.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				configEntries.add(new ConfigWii());
				configController.showEditRow(configEntries.size() -1);
			}
		});
		
//		btnWiiSettings.setOnAction(event -> {
//			configLightEntries = WiiSettings.show(configLightEntries);
//		});
		
//		btnSend.setOnAction(event -> {
//			copy(configTablePane.getScene().getWindow());
//		});
		
//		btnSendTemp.setOnAction(event -> {
//			sendConfigurationToYaas(configTablePane.getScene().getWindow());
//		});
		
//		btnLoad.setOnAction(event -> loadFromFile(btnLoad.getScene().getWindow()));
//		btnSave.setOnAction(event -> saveToFile(btnLoad.getScene().getWindow()));
		btnClear.setOnAction(event -> clear());
		
	}

	@Override
	public RowEditor getLineEditor(AnchorPane root, ConfigCommand configEntry) {
		RowEditor editor = WiiRowEditor.show(root, configEntry);
		return editor;
	}

	@Override
	public void handleConfigurationLine(String mode, String prefix, String line, int lineNumber) {

//		switch (mode) {
//
//			case WII_DEFINITIONS:
//				try {
//					ConfigMidi entry = new ConfigMidi(line, prefix);
//					entry.setMidiCommand(MIDI_NOTE_ON);
//					configEntries.add(entry);
//				} catch (ConfigurationException e) {
//					logger.warn("Ignored line " + lineNumber, e);
//				}
//				break;
//			case CONTROLLER_DEFINITIONS:						
//				try {
//					ConfigLight lightEntry = new ConfigLight(line);
//					configLightEntries.add(lightEntry);
//				} catch (ConfigurationException e) {
//					logger.warn("Ignored line " + lineNumber, e);
//				}		
//		}

	}

	@Override
	public String getFileString() {
		StringBuffer sb = new StringBuffer();
		
//		sb.append("[Wii]\n");
//
//		sb.append("midi_note_definitions = {\n");	
//		sb.append(getStringForCommand(MIDI_NOTE_ON));
//
//		sb.append("midi_note_off_definitions = {\n");				
//		sb.append(getStringForCommand(MIDI_NOTE_OFF));
//
//		sb.append("[CC]\n");
//		sb.append("midi_cc_definitions = {\n");				
//		sb.append(getStringForCommand(MIDI_CC));
//		
////		controller_definitions
//		sb.append("[Addons]\n");
//		sb.append("controller_definitions = {\n");				
//		for (ConfigLight entry : configLightEntries) {
//			
//			sb.append("\t" + entry.toString() + "\n");
//		}				
//		sb.append("\t}\n\n");

		return sb.toString();
	}

	@Override
	public void loadCommands(String value, String[] entry) {
		if (entry[1].equals("config")) {
			
			ConfigCommand configEntry = gson.fromJson(value, ConfigWii.class);
			configEntries.add(configEntry);
			
		} else if (entry[1].equals("controller")) {
			ObservableMap<String, List<String>> commands = YaasController.getInstance().yaasCommands;
			if (!commands.containsKey(entry[2])) {
				commands.put(entry[2], new ArrayList<String>());
			}
			commands.get(entry[2]).add(value);
		}

	}

	@Override
	String getConfigFileName() {
		YaasConfiguration config = YaasController.getInstance().yaasConfigurationProperty.get();
		return config.getWiiConfigFile();
	}
}
