package net.hirschauer.yaas.lighthouse.visual.popups;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.hirschauer.yaas.lighthouse.model.ConfigEntry;
import net.hirschauer.yaas.lighthouse.model.ConfigLightEntry;
import net.hirschauer.yaas.lighthouse.model.ConfigLightEntry.Command;
import net.hirschauer.yaas.lighthouse.visual.VisualController;
import net.hirschauer.yaas.lighthouse.visual.components.MidiReceiver;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerSettings extends VisualController {
	
	private static final Logger logger = LoggerFactory.getLogger(ControllerSettings.class);
	
	private List<ConfigLightEntry> settings;

	@FXML
	Button btnCancel, btnSave;
	
	@FXML
	TextField txtPlay, txtStop, txtRecord, txtOffset1, txtOffset2,
		txtParamOffset1, txtParamOffset2;
	
	@FXML
	ComboBox<String> comboPlay, comboStop, comboRecord, 
		comboOffset1, comboOffset2;
	
	@FXML
	AnchorPane paneInput;

	private Stage stage;
	
	public static List<ConfigLightEntry> show(List<ConfigLightEntry> configLightEntries) {
		
		FXMLLoader loader = new FXMLLoader(
				ControllerSettings.class.getResource(
						"/view/popups/ControllerSettings.fxml"));
		try {
			AnchorPane child = (AnchorPane) loader.load();
			
			Stage confStage = new Stage();	
			confStage.setTitle("Light settings");
			Scene confScene = new Scene(child);
			
			ControllerSettings controller = loader.getController();
			controller.setStage(confStage);
			if (configLightEntries.size() > 0) {
				controller.setLightSettings(configLightEntries);
			}

			confStage.setScene(confScene);
			confStage.showAndWait();
			
			return controller.settings;
			
		} catch (Exception e) {
			logger.error("Could not open controller settings", e);
		}	
		return null;
	}
    
	@FXML
	private void initialize() {
		logger.debug("init");
		
		comboPlay.setValue(ConfigEntry.MIDI_NOTE_ON);
		comboStop.setValue(ConfigEntry.MIDI_NOTE_ON);
		comboRecord.setValue(ConfigEntry.MIDI_NOTE_ON);
		comboOffset1.setValue(ConfigEntry.MIDI_NOTE_ON);
		comboOffset2.setValue(ConfigEntry.MIDI_NOTE_ON);
		
		btnCancel.setOnAction(event -> {
			stage.close();
		});
		
		btnSave.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
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
				if (StringUtils.isNotEmpty(txtOffset1.getText())) {
					command = new ConfigLightEntry(Command.OFFSET1);
					command.setMidiCommand(comboOffset1.getValue());
					command.setMidiValue(txtOffset1.getText());
					command.setMidiFollowSignal(txtParamOffset1.getText());
					settings.add(command);
				}
				if (StringUtils.isNotEmpty(txtOffset2.getText())) {
					command = new ConfigLightEntry(Command.OFFSET2);
					command.setMidiCommand(comboOffset2.getValue());
					command.setMidiValue(txtOffset2.getText());
					command.setMidiFollowSignal(txtParamOffset2.getText());
					settings.add(command);
				}
				stage.close();
			}
		});
		
		MidiReceiver.show(paneInput);
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
					txtParamOffset1.setText(entry.getMidiFollowSignal());
					break;
				case OFFSET2:
					comboOffset2.setValue(entry.getMidiCommand());
					txtOffset2.setText(entry.getMidiValue());
					txtParamOffset2.setText(entry.getMidiFollowSignal());
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