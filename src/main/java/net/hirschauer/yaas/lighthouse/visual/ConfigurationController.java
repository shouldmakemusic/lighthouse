package net.hirschauer.yaas.lighthouse.visual;

import java.util.HashMap;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationController {

	Logger logger = LoggerFactory.getLogger(ConfigurationController.class);
	
	@FXML
	private ComboBox<String> midiCommandCombo;	
	
	@FXML
	private ComboBox<String> controllerCombo;	
	
	@FXML
	private ComboBox<String> commandCombo;

	private HashMap<String, List<String>> yaasCommands;	
	
	@FXML
	private void initialize() {
		logger.debug("init");
	}

	public void updateController(HashMap<String, List<String>> yaasCommands) {
		
		this.yaasCommands = yaasCommands;
		
		ObservableList<String> controllerNames = FXCollections.observableArrayList();
		for (String name : yaasCommands.keySet()) {
			logger.debug("added controller " + name);
			controllerNames.add(name);
		}
		controllerCombo.setItems(controllerNames);
		controllerCombo.valueProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				
				logger.debug("selected " + newValue);
				ObservableList<String> commandNames = FXCollections.observableArrayList();	
				if (yaasCommands.containsKey(newValue)) {
					for (String name : yaasCommands.get(newValue)) {
						logger.debug("added command " + name);
						commandNames.add(name);
					}
				}
				commandCombo.setItems(commandNames);
			}
		});
	}
}
