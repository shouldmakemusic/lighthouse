package net.hirschauer.yaas.lighthouse.visual.components;

import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.hirschauer.yaas.lighthouse.model.ConfigCommand;
import net.hirschauer.yaas.lighthouse.osccontroller.YaasController;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.RangeSlider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RowEditor {

	private static final Logger logger = LoggerFactory
			.getLogger(RowEditor.class);

	@FXML
	private ComboBox<String> controllerCombo, commandCombo;
	@FXML
	private TextField txtValue1, txtValue2;
	@FXML
	protected AnchorPane paneInput, paneRangeSlider;
	@FXML
	private CheckBox checkInvert;
	@FXML
	private Button btnOk, btnCancel;

	private ObservableList<String> controllerEntries = FXCollections.observableArrayList();

	private RangeSlider hSlider;
	protected Stage stage;
	private ConfigCommand configCommand;
	
	public void updateControllerCombo() {
		
		Map<String, List<String>> yaasCommands = YaasController.getInstance().yaasCommands; 
		controllerEntries.clear();
		for (String name : yaasCommands.keySet()) {
//			logger.debug("added controller " + name);
			controllerEntries.add(name);
		}
		controllerCombo.setItems(controllerEntries);
	}

	protected void setEntry(ConfigCommand entry) {
		
		this.txtValue1.setText(entry.getValue1());
		this.txtValue2.setText(entry.getValue2());
		this.controllerCombo.setValue(entry.getController());
		this.commandCombo.setValue(entry.getCommand());
		
		String range = entry.getValue3();
		if (!StringUtils.isEmpty(range) && range.contains(";")) {
			String[] fromTo = range.split(";");
			double min = Double.parseDouble(fromTo[0]);
			double max = Double.parseDouble(fromTo[1]);
			if (max < min) {
				this.checkInvert.setSelected(true);
				min = max;
				max = Double.parseDouble(fromTo[0]);
			} else {
				this.checkInvert.setSelected(false);
			}
			this.hSlider.setLowValue(min);
			this.hSlider.setHighValue(max);
		}
		this.configCommand = entry;
		setConfigEntry(entry);
	}

	@FXML
	protected void initialize() {
		
		logger.debug("init");
		
		initSlider(0, 127, 0, 127);		
		
		btnCancel.setOnAction(event -> {
			stage.close();
		});
		
		btnOk.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String error = verifyAll();
		    	if (StringUtils.isNotEmpty(error)) {
		    		Alert alert = new Alert(AlertType.ERROR);
		    		alert.setTitle("Error Dialog");
		    		alert.setHeaderText(null);
		    		alert.setContentText(error);
		    		stage.setOpacity(0);
		    		alert.showAndWait();    	
		    		stage.setOpacity(1);
		    		return;
		    	}    	

				configCommand = getConfigEntry();
				configCommand.setCommand(commandCombo.getValue());
				configCommand.setController(controllerCombo.getValue());
				configCommand.setValue1(txtValue1.getText());
				configCommand.setValue2(txtValue2.getText());
				logger.debug("Slider " + hSlider.getLowValue() + " - " + hSlider.getHighValue());
				
				if (hSlider.getLowValue() > 0.0 || hSlider.getHighValue() < 100.0 || checkInvert.isSelected()) {
					
					if (checkInvert.isSelected()) {
						configCommand.setValue3(Math.round(hSlider.getHighValue()) + ";" + 
								Math.round(hSlider.getLowValue()));						
					} else {
						configCommand.setValue3(Math.round(hSlider.getLowValue()) + ";" + 
								Math.round(hSlider.getHighValue()));						
					}

				} else {
					configCommand.setValue3(null);
				}
				stage.close();
			}
		});
		
		YaasController.getInstance().yaasCommands.addListener(new MapChangeListener<String, List<String>>() {

			@Override
			public void onChanged(@SuppressWarnings("rawtypes") Change change) {
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						updateControllerCombo();
						commandCombo.setValue("");
						commandCombo.setItems(null);
					}
				});				
			}
		});
		
		updateControllerCombo();
		controllerCombo.valueProperty().addListener((ObservableValue<? extends String> observable,
					String oldValue, String newValue) -> {
				
				logger.debug("selected " + newValue);
				Map<String, List<String>> yaasCommands = YaasController.getInstance().yaasCommands; 				
				ObservableList<String> commandNames = FXCollections.observableArrayList();	
				if (yaasCommands.containsKey(newValue)) {
					for (String name : yaasCommands.get(newValue)) {
						logger.debug("added command " + name);
						commandNames.add(name);
					}
				}
				commandCombo.setItems(commandNames);
		});

	}
	
    protected String verifyAll() {
    	
    	String error = verify();
    	if (StringUtils.isEmpty(controllerCombo.getValue())) {
    		error += "Controller has to be set\n";
    	}
    	return error;
    }
	
	protected void initSlider(double min, double max, double low, double high) {
		hSlider = new RangeSlider(min, max, low, high);
		hSlider.setShowTickMarks(true);
		hSlider.setShowTickLabels(true);
		hSlider.setBlockIncrement(10);
		if (paneRangeSlider.getChildren().size() == 1) {
			paneRangeSlider.getChildren().remove(0);
		}
		paneRangeSlider.getChildren().add(hSlider);
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public ConfigCommand getEntry() {

		if (StringUtils.isEmpty(verifyAll())) {
			return configCommand;
		}
		return null;
	}
	
	protected abstract ConfigCommand getConfigEntry();
	protected abstract void setConfigEntry(ConfigCommand entry);
	protected abstract String verify();
}
