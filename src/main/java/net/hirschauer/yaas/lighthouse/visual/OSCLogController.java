package net.hirschauer.yaas.lighthouse.visual;

import java.io.IOException;

import de.sciss.net.OSCMessage;
import net.hirschauer.yaas.lighthouse.model.osc.OSCMessageFromTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class OSCLogController extends LogController {
	
	@FXML
	protected ComboBox<String> combOscCommand, comboTarget1, comboTarget2;
	
	@FXML
	protected TextField txtOscParameter1, txtOscParameter2, txtOscCommand;
	
	@FXML
	Button btnSend1, btnSend2;
	
	protected ObservableList<String> oscCommandList = FXCollections.observableArrayList();
	protected ObservableList<String> oscTargetList = FXCollections.observableArrayList();
	
	static final String YAAS = "Yaas";
	static final String LIGHTHOUSE = "LightHouse";
	
	public OSCLogController() {
		super(OSCMessageFromTask.TYPE_OSC);
	}

    @FXML
	protected void initialize() {
    	super.initialize();
    	
    	oscCommandList.addAll(
    		"/osc/test",
    		"/wii/hallo"
    	);
    	combOscCommand.setItems(oscCommandList);
    	
    	oscTargetList.addAll(YAAS, LIGHTHOUSE);
    	comboTarget1.setItems(oscTargetList);
    	comboTarget1.setValue(LIGHTHOUSE);
    	comboTarget2.setItems(oscTargetList);
    	comboTarget2.setValue(YAAS);
    	
    	btnSend1.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				OSCMessage m = new OSCMessage(combOscCommand.getValue(), new String[] {txtOscParameter1.getText()});
	    		sendMessage(m, comboTarget1.getValue());
			}
		});
    	btnSend2.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
	    		OSCMessage m = new OSCMessage(txtOscCommand.getText(), new String[] {txtOscParameter1.getText()});
	    		sendMessage(m, comboTarget2.getValue());
			}
		});
    }
    
    protected void sendMessage(OSCMessage m, String target) {
		try {
			if (YAAS.equals(target)) {
				oscServer.sendToYaas(m);
			} else if (LIGHTHOUSE.equals(target)) {
				oscServer.sendToLightHouse(m);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Could not send command to " + target);
			alert.show();

		}
    	
    }
}
