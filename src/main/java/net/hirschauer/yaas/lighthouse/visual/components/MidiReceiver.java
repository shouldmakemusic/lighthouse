package net.hirschauer.yaas.lighthouse.visual.components;

import static net.hirschauer.yaas.lighthouse.model.ConfigEntry.MIDI_NOTE_ON;
import static net.hirschauer.yaas.lighthouse.model.ConfigEntry.MIDI_CC;
import static net.hirschauer.yaas.lighthouse.model.ConfigEntry.MIDI_NOTE_OFF;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.hirschauer.yaas.lighthouse.LightHouseMidi;
import net.hirschauer.yaas.lighthouse.model.ConfigEntry;
import net.hirschauer.yaas.lighthouse.model.ConfigLightEntry;
import net.hirschauer.yaas.lighthouse.model.ConfigMidiEntry;
import net.hirschauer.yaas.lighthouse.model.MidiLogEntry;
import net.hirschauer.yaas.lighthouse.visual.popups.ControllerSettingsController;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidiReceiver {
	
	private static final Logger logger = LoggerFactory.getLogger(MidiReceiver.class);
	
	@FXML
	private ComboBox<String> midiCommandCombo;
    @FXML
    private TextField txtMidiValue, txtMidiFollowSignal;
    @FXML
    private Button btnReceiveMidi;
    
	public static MidiReceiver show(AnchorPane parent) {
		
		FXMLLoader loader = new FXMLLoader(
				ControllerSettingsController.class.getResource(
						"/view/components/MidiReceiver.fxml"));
		try {
			AnchorPane child = (AnchorPane) loader.load();		
			parent.getChildren().add(child);
			MidiReceiver controller = loader.getController();			
			return controller;
			
		} catch (Exception e) {
			logger.error("Could not open controller settings", e);
		}	
		return null;
	}


    @FXML
	private void initialize() {
		logger.debug("init");

		midiCommandCombo.setValue(MIDI_NOTE_ON);
		
		txtMidiFollowSignal.setTooltip(new Tooltip("This is only for controllers that used mackie control scripts.\nFirst comes a note and then an integer event type.\nPlace the value that shows as event type here:"));
		btnReceiveMidi.setTooltip(new Tooltip("Receive the next midi event from the controller\nselected in the Midi viewer."));
		
		LightHouseMidi midi = LightHouseMidi.getInstance();
		btnReceiveMidi.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				if (!midi.hasDevice()) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Midi device not set");
					alert.setContentText("You have to go to the \"Midi viewer\" and select the device first");
					alert.show();
					return;
				}
				btnReceiveMidi.setDisable(true);
				ListChangeListener<MidiLogEntry> changeListener = new ListChangeListener<MidiLogEntry>() {
	
					@Override
					public void onChanged(
							javafx.collections.ListChangeListener.Change<? extends MidiLogEntry> c) {
						c.next();
						MidiLogEntry nextMidi = c.getAddedSubList().get(0);
						int status = nextMidi.getStatus();
						
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								
								if (status == MidiLogEntry.STATUS_CC) {
									midiCommandCombo.setValue(MIDI_CC);
								} else if (status == MidiLogEntry.STATUS_NOTE_OFF) {
									midiCommandCombo.setValue(MIDI_NOTE_OFF);
								} else {
									midiCommandCombo.setValue(MIDI_NOTE_ON);
								}
		
								txtMidiValue.setText(nextMidi.getData1());
								txtMidiFollowSignal.setText("");
								btnReceiveMidi.setDisable(false);
							}
						});
						
						midi.logEntries.removeListener(this);
					}					
				};
				midi.logEntries.addListener(changeListener);
			}
		});
    }
    
    public String verify() {
    	String error = "";
    	if (StringUtils.isEmpty(txtMidiValue.getText())) {
    		error = "Midi value has to be set\n";
    	}
    	if (StringUtils.isEmpty(midiCommandCombo.getValue())) {
    		error += "Command has to be set\n";
    	}
    	return error;
    }
    
    public ConfigEntry getMidiInput() {
    	ConfigEntry ce = new ConfigEntry();
    	ce.setMidiCommand(midiCommandCombo.getValue());
    	ce.setMidiValue(txtMidiValue.getText());
    	ce.setMidiFollowSignal(txtMidiFollowSignal.getText());
    	return ce;
    }

}
