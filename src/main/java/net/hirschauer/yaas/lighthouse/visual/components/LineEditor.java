package net.hirschauer.yaas.lighthouse.visual.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.hirschauer.yaas.lighthouse.model.ConfigCommand;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.RangeSlider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LineEditor {

	private static final Logger logger = LoggerFactory
			.getLogger(LineEditor.class);

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

	private RangeSlider hSlider;
	private Stage stage;
	private ConfigCommand configCommand;

	public static LineEditor show(AnchorPane parent, ConfigCommand entry) {

		FXMLLoader loader = new FXMLLoader(
				LineEditor.class
						.getResource("/view/components/LineEditor.fxml"));
		try {
			AnchorPane child = (AnchorPane) loader.load();
			parent.getChildren().add(child);
			LineEditor controller = loader.getController();
			controller.setEntry(entry);
			return controller;

		} catch (Exception e) {
			logger.error("Could not open controller settings", e);
		}
		return null;
	}

	private void setEntry(ConfigCommand entry) {
		
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
		
		initSlider(0, 100, 0, 100);		
		
		btnCancel.setOnAction(event -> {
			stage.close();
		});
		
		btnOk.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
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

		return configCommand;
	}
	
	protected abstract ConfigCommand getConfigEntry();
	protected abstract void setConfigEntry(ConfigCommand entry);
}
