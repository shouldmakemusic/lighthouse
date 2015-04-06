package net.hirschauer.yaas.lighthouse.visual;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.hirschauer.yaas.lighthouse.model.ConfigEntry;
import net.hirschauer.yaas.lighthouse.model.ConfigLightEntry;
import net.hirschauer.yaas.lighthouse.model.ConfigLightEntry.Command;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerSettingsController extends VisualController {
	
	private static final Logger logger = LoggerFactory.getLogger(ControllerSettingsController.class);
	
	private List<ConfigLightEntry> settings;

	@FXML
	Button btnReceiveMidi, btnCancel, btnSave;
	
	@FXML
	TextField txtMidiValue, txtPlay, txtStop, txtRecord, txtOffset1, txtOffset2,
		txtParamOffset1, txtParamOffset2;
	
	@FXML
	ComboBox<String> midiCommandCombo, comboPlay, comboStop, comboRecord, 
		comboOffset1, comboOffset2;

	private Stage stage; 
    
	@FXML
	private void initialize() {
		logger.debug("init");
		
		midiCommandCombo.setValue(ConfigEntry.MIDI_NOTE_ON);
		comboPlay.setValue(ConfigEntry.MIDI_NOTE_ON);
		comboStop.setValue(ConfigEntry.MIDI_NOTE_ON);
		comboRecord.setValue(ConfigEntry.MIDI_NOTE_ON);
		comboOffset1.setValue(ConfigEntry.MIDI_NOTE_ON);
		comboOffset2.setValue(ConfigEntry.MIDI_NOTE_ON);
		
		btnCancel.setOnAction(event -> {
			stage.close();
		});
		
		btnSave.setOnAction(event -> {
			settings = new ArrayList<ConfigLightEntry>();
			
			ConfigLightEntry command;
			
			if (StringUtils.isNotEmpty(txtPlay.getText())) {
				command = new ConfigLightEntry(Command.PLAY);
				command.setMidiCommand(comboPlay.getValue());
				command.setMidiValue(txtPlay.getText());
				settings.add(command);
			}
			if (StringUtils.isNotEmpty(txtStop.getText())) {
				command = new ConfigLightEntry(Command.STOP);
				command.setMidiCommand(comboStop.getValue());
				command.setMidiValue(txtStop.getText());
				settings.add(command);
			}
			if (StringUtils.isNotEmpty(txtRecord.getText())) {
				command = new ConfigLightEntry(Command.RECORD);
				command.setMidiCommand(comboRecord.getValue());
				command.setMidiValue(txtRecord.getText());
				settings.add(command);
			}
			// TODO: add other buttons
			stage.close();
		});
    }
    
	@Override
	protected String getMenuId() {
		return null;
	}

	public List<ConfigLightEntry> getSettings() {

		return settings;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void setLightSettings(List<ConfigLightEntry> lightSettings) {
		this.settings = lightSettings;
		for (ConfigLightEntry entry : lightSettings) {
			switch (entry.getCommand()) {
				case OFFSET1:
					comboOffset1.setValue(entry.getMidiCommand());
					txtOffset1.setText(entry.getMidiValue());
					break;
				case OFFSET2:
					comboOffset2.setValue(entry.getMidiCommand());
					txtOffset2.setText(entry.getMidiValue());
					break;
				case PLAY:
					comboPlay.setValue(entry.getMidiCommand());
					txtPlay.setText(entry.getMidiValue());
					break;
				case RECORD:
					comboRecord.setValue(entry.getMidiCommand());
					txtRecord.setText(entry.getMidiValue());
					break;
				case STOP:
					comboStop.setValue(entry.getMidiCommand());
					txtStop.setText(entry.getMidiValue());
					break;
				default:
					break;
				}
		}
	}
}
