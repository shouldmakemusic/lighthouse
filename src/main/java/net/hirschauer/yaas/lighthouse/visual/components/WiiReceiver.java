package net.hirschauer.yaas.lighthouse.visual.components;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.ConfigWii;
import net.hirschauer.yaas.lighthouse.model.LogEntry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WiiReceiver {
	
	private static final Logger logger = LoggerFactory.getLogger(WiiReceiver.class);
	
	@FXML
	private ComboBox<String> comboWiiSelector, comboMode;
    @FXML
    private TextField txtMessage, txtSwitchToMode;
    @FXML
    private Button btnReceiveWiiSignal;

	public static WiiReceiver show(AnchorPane parent) {
		
		FXMLLoader loader = new FXMLLoader(
				LightSettings.class.getResource(
						"/view/components/WiiReceiver.fxml"));
		try {
			AnchorPane child = (AnchorPane) loader.load();		
			parent.getChildren().add(child);
			WiiReceiver controller = loader.getController();
			return controller;
			
		} catch (Exception e) {
			logger.error("Could not open controller settings", e);
		}	
		return null;
	}


    @FXML
	private void initialize() {
		logger.debug("init");
		
		comboMode.setValue("Default");
		comboWiiSelector.setValue("/wii/1");
		
//		txtMidiFollowSignal.setTooltip(new Tooltip("This is only for controllers that used mackie control scripts.\nFirst comes a note and then an integer event type.\nPlace the value that shows as event type here:"));
//		btnReceiveMidi.setTooltip(new Tooltip("Receive the next midi event from the controller\nselected in the Midi viewer."));
		
		LightHouseOSCServer oscServer = LightHouseOSCServer.getInstance();
		btnReceiveWiiSignal.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				btnReceiveWiiSignal.setDisable(true);
				ListChangeListener<LogEntry> changeListener = new ListChangeListener<LogEntry>() {
	
					@Override
					public void onChanged(
							javafx.collections.ListChangeListener.Change<? extends LogEntry> c) {
						c.next();
						LogEntry nextWii = c.getAddedSubList().get(0);
						String received = nextWii.getMessage();
						String[] parts = received.split("/");
						if (!parts[1].equals("wii") || parts.length < 4 || !StringUtils.isNumeric(parts[2])) {
							return;
						}
						
						String message = "";
						for (int i = 3; i < parts.length; i++) {
							message += "/" + parts[i];
						}
						String wiiSelector = "/wii/" + parts[2];
						String textMessage = message;
						
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {								
								
								txtMessage.setText(textMessage);
								if (!comboWiiSelector.getItems().contains(wiiSelector)) {
									comboWiiSelector.getItems().add(wiiSelector);
								}
								comboWiiSelector.setValue(wiiSelector);
								btnReceiveWiiSignal.setDisable(false);
							}
						});
						
						oscServer.logEntries.removeListener(this);
					}					
				};
				oscServer.logEntries.addListener(changeListener);
			}
		});
    }
    
    public String verify() {
    	String error = "";
    	if (StringUtils.isEmpty(comboWiiSelector.getValue())) {
    		error = "Wii has to be set\n";
    	}
    	if (StringUtils.isEmpty(txtMessage.getText())) {        		
        	error += "Message has to be set";
    	}
    	return error;
    }
    
    public ConfigWii getWiiCommand() {
    	ConfigWii ce = new ConfigWii();
    	ce.setMessage(comboWiiSelector.getValue() + txtMessage.getText());
    	if (StringUtils.isNotEmpty(txtSwitchToMode.getText())) {
    		ce.setSwitchToMode(txtSwitchToMode.getText());
    	}
    	if (StringUtils.isNotEmpty(comboMode.getValue())) {
    		ce.setMode(comboMode.getValue());
    	}
    	return ce;
    }

    public void setWiiCommand(ConfigWii command) {

    	if (StringUtils.isNotEmpty(command.getMode())) {
    		comboMode.setValue(command.getMode());
    	}
    	if (StringUtils.isNotEmpty(command.getMessage())) {
    		comboWiiSelector.setValue(command.getMessage().substring(0, 6));
        	txtMessage.setText(command.getMessage().substring(6));
    	} else {
    		comboWiiSelector.setValue("");
        	txtMessage.setText("");
    	}
    }
}
