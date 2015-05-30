package net.hirschauer.yaas.lighthouse.visual;

import static net.hirschauer.yaas.lighthouse.model.ConfigMidi.MIDI_CC;
import static net.hirschauer.yaas.lighthouse.model.ConfigMidi.MIDI_NOTE_OFF;
import static net.hirschauer.yaas.lighthouse.model.ConfigMidi.MIDI_NOTE_ON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.exceptions.ConfigurationException;
import net.hirschauer.yaas.lighthouse.model.ConfigCommand;
import net.hirschauer.yaas.lighthouse.model.ConfigLight;
import net.hirschauer.yaas.lighthouse.model.ConfigMidi;
import net.hirschauer.yaas.lighthouse.model.YaasConfiguration;
import net.hirschauer.yaas.lighthouse.model.osc.OSCMessage;
import net.hirschauer.yaas.lighthouse.model.osc.OSCMessageReceiveConfiguration;
import net.hirschauer.yaas.lighthouse.osccontroller.YaasController;
import net.hirschauer.yaas.lighthouse.visual.components.ConfigTable;
import net.hirschauer.yaas.lighthouse.visual.components.LightSettings;
import net.hirschauer.yaas.lighthouse.visual.components.RowEditor;
import net.hirschauer.yaas.lighthouse.visual.components.MidiRowEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YaasConfigurator extends Configurator {
	
	public static final String CONTROLLER_DEFINITIONS = "controller_definitions";
	public static final String MIDI_NOTE_OFF_DEFINITIONS = "midi_note_off_definitions";
	public static final String MIDI_CC_DEFINITIONS = "midi_cc_definitions";
	public static final String MIDI_NOTE_DEFINITIONS = "midi_note_definitions";

	private static final Logger logger = LoggerFactory.getLogger(YaasConfigurator.class);
	
    @FXML
    private Button btnAdd, btnLightSettings, btnSend, btnSendTemp, btnLoad, btnSave, btnClear;
    
    @FXML
    private AnchorPane configTablePane;

	private List<ConfigLight> configLightEntries = new ArrayList<ConfigLight>();
	private List<ConfigLight> backupConfigLightEntries = new ArrayList<ConfigLight>();
	private ConfigTable configController;
	
	public static YaasConfigurator show(AnchorPane configurationTablePane) throws IOException {
		
		FXMLLoader loader = new FXMLLoader(YaasConfigurator.class.getResource("/view/configurators/YaasConfigurator.fxml"));
		AnchorPane tabContent = (AnchorPane) loader.load();

		YaasConfigurator yaasConfigurator = loader.getController();        
        configurationTablePane.getChildren().add(tabContent);
        return yaasConfigurator;
	}
	
	public YaasConfigurator() {
		super();
	}
	
    @FXML
    public void initialize() throws IOException {
		logger.debug("init");

		configController = ConfigTable.show(configTablePane);
		configController.setConfigurator(this);

		btnAdd.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				configEntries.add(new ConfigMidi());
				configController.showEditRow(configEntries.size() -1);
			}
		});
		
		btnLightSettings.setOnAction(event -> {
			configLightEntries = LightSettings.show(configLightEntries);
		});
		
		btnSend.setOnAction(event -> {
			copy(configTablePane.getScene().getWindow());
		});
		
		btnSendTemp.setOnAction(event -> {
			sendConfigurationToYaas(configTablePane.getScene().getWindow());
		});
		
		btnLoad.setOnAction(event -> loadFromFile(btnLoad.getScene().getWindow()));
		btnSave.setOnAction(event -> saveToFile(btnLoad.getScene().getWindow()));
		btnClear.setOnAction(event -> clear());
		
	}

	@Override
	public RowEditor getLineEditor(AnchorPane root, ConfigCommand configEntry) {
		RowEditor editor = MidiRowEditor.show(root, configEntry);
		return editor;
	}

	@Override
	public void handleConfigurationLine(String mode, String prefix, String line, int lineNumber) {

		switch (mode) {

			case MIDI_NOTE_DEFINITIONS:
			case MIDI_CC_DEFINITIONS:
			case MIDI_NOTE_OFF_DEFINITIONS:
				try {
					ConfigMidi entry = new ConfigMidi(line, prefix);
					entry.setMidiCommand(MIDI_NOTE_ON);
					if (mode.equals(MIDI_CC_DEFINITIONS)) {
						entry.setMidiCommand(MIDI_CC);
					}
					if (mode.equals(MIDI_NOTE_OFF_DEFINITIONS)) {
						entry.setMidiCommand(MIDI_NOTE_OFF);
					}
					configEntries.add(entry);
				} catch (ConfigurationException e) {
					logger.warn("Ignored line " + lineNumber, e);
				}
				break;
			case CONTROLLER_DEFINITIONS:						
				try {
					ConfigLight lightEntry = new ConfigLight(line);
					configLightEntries.add(lightEntry);
				} catch (ConfigurationException e) {
					logger.warn("Ignored line " + lineNumber, e);
				}		
		}

	}

	@Override
	public void clearConfig() {
		backupConfigLightEntries.clear();
		backupConfigLightEntries.addAll(configLightEntries);
		configLightEntries = new ArrayList<ConfigLight>();
	}

	@Override
	public void restoreConfig() {
		configLightEntries = backupConfigLightEntries;
		configController.setConfigurator(this);
		configController.refresh();
	}

	@Override
	public void sendIndividualConfiguration(LightHouseOSCServer oscServer) throws IOException {
		for (ConfigLight entry : configLightEntries) {
    		
			OSCMessage m = new OSCMessageReceiveConfiguration(entry.getLightCommand().toString(), entry.getMidiCommand(), entry.getMidiValue());
			oscServer.sendToYaas(m);
		}
	}

	@Override
	public String getFileString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("[MidiIn]\n");

		sb.append("midi_note_definitions = {\n");	
		sb.append(getStringForCommand(MIDI_NOTE_ON));

		sb.append("midi_note_off_definitions = {\n");				
		sb.append(getStringForCommand(MIDI_NOTE_OFF));

		sb.append("[CC]\n");
		sb.append("midi_cc_definitions = {\n");				
		sb.append(getStringForCommand(MIDI_CC));
		
//		controller_definitions
		sb.append("[Addons]\n");
		sb.append("controller_definitions = {\n");				
		for (ConfigLight entry : configLightEntries) {
			
			sb.append("\t" + entry.toString() + "\n");
		}				
		sb.append("\t}\n\n");

		return sb.toString();
	}

	@Override
	public void storeIndividualCommands(Properties values) {
		String className = getClass().getName();
		for (int i=0; i<configLightEntries.size(); i++) {
			ConfigLight entry = configLightEntries.get(i);
			values.put(className + "|light|" + i, gson.toJson(entry));
		}
	}

	@Override
	public void loadCommands(String value, String[] entry) {
		if (entry[1].equals("config")) {
			
			ConfigCommand configEntry = gson.fromJson(value, ConfigMidi.class);
			configEntries.add(configEntry);
			
		} else if (entry[1].equals("light")) {
				
			ConfigLight configEntry = gson.fromJson(value, ConfigLight.class);
			configLightEntries.add(configEntry);
				
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
		return config.getYaasConfigFile();
	}
}
