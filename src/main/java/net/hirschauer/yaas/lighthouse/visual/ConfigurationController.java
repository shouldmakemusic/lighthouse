package net.hirschauer.yaas.lighthouse.visual;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import net.hirschauer.yaas.lighthouse.model.ConfigEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
   	@FXML
    private TableView<ConfigEntry> configTable;
    @FXML
    private TableColumn<ConfigEntry, String> colMidiCommand;
    @FXML
    private TableColumn<ConfigEntry, String> colMidiValue;
    @FXML
    private TableColumn<ConfigEntry, String> colController;
    @FXML
    private TableColumn<ConfigEntry, String> colCommand;
    @FXML
    private TableColumn<ConfigEntry, String> colValue1;
    @FXML
    private TableColumn<ConfigEntry, String> colValue2;
    @FXML
    private TableColumn<ConfigEntry, String> colValue3;
    @FXML
    private TableColumn<ConfigEntry, String> colAction;
    @FXML
    private TextField txtMidiValue;
    @FXML
    private TextField txtValue1;
    @FXML
    private TextField txtValue2;
    @FXML
    private TextField txtValue3;
    @FXML
    private Button btnReceiveMidi;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnLoad;
    @FXML
    private Button btnReceive;
    @FXML
    private Button btnSend;
    
	private ObservableList<ConfigEntry> configEntries = FXCollections.observableArrayList();
    
    @FXML
	private void initialize() {
		logger.debug("init");
		
		colMidiCommand.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("midiCommand"));
		colMidiValue.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("midiValue"));
		colCommand.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("command"));
		colController.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("controller"));
		colValue1.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("value1"));
		colValue2.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("value2"));
		colValue3.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("value3"));

		configTable.setItems(configEntries);
		
		btnReceiveMidi.setDisable(true);
		btnReceive.setDisable(true);
		btnSend.setDisable(true);
		
		btnAdd.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				addInputToTable();
			}
		});
		
		btnSave.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				save(event);
			}
		});
		
		btnLoad.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				load(event);
			}
		});
	}
    
    protected void load(ActionEvent event) {
    	
		FileChooser fileChooser = new FileChooser();	
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Midi config", "*.conf")
            );
		Window window = ((Node)event.getTarget()).getScene().getWindow();
		File file = fileChooser.showOpenDialog(window);
        if (file != null) {
    		List<ConfigEntry> entries = new ArrayList<ConfigEntry>();
        	try {
				List<String> lines = FileUtils.readLines(file);
				int mode = 0;
				for (String line : lines){
					line = line.trim();
					switch (mode) {
						case 0:
							if (line.equals("midi_note_definitions = {")) {
								mode = 1;
							}
							if (line.equals("midi_cc_definitions = {")) {
								mode = 2;
							}
							break;
						case 1:
							if (line.equals("}")) {
								mode = 0;
								continue;
							} 
							if (line.startsWith("#")) {
								continue;
							}
							ConfigEntry entry = getEntryFromString(line);
							if (entry != null) {
								entry.setMidiCommand("Midi Note");
								entries.add(entry);
							}
							break;
					}
				}
			} catch (IOException e) {
				logger.error("Could not read file " + file.getAbsolutePath(), e);
			}
        	if (entries.size() > 0) {
				this.configEntries.clear();
				for (ConfigEntry entry : entries) {
					this.configEntries.add(entry);
				}
        	}
        }
    }
    
    protected ConfigEntry getEntryFromString(String line) {
    	
    	ConfigEntry entry = new ConfigEntry();
    	String[] midiCommand = line.split(":");
    	entry.setMidiValue(midiCommand[0].trim());

    	midiCommand[1] = midiCommand[1].trim();
    	String command = midiCommand[1].substring(1, midiCommand[1].length() - 2);
    	String[] commandParts = command.split(",");
    	
    	commandParts[0] = commandParts[0].trim();
    	entry.setController(commandParts[0].substring(1, commandParts[0].length() - 1));
    	
    	commandParts[1] = commandParts[1].trim();
    	entry.setCommand(commandParts[1].substring(1, commandParts[1].length() - 1));
    	
    	String concValues = "";
    	for (int i=2; i < commandParts.length; i++) {
    		concValues += commandParts[i];
    	}
    	concValues = concValues.trim();
    	concValues = concValues.substring(1, concValues.length() - 1);
    	String[] values = concValues.split(" ");
    	if (values.length >= 1) {
    		entry.setValue1(values[0]);
    	}
    	if (values.length >= 2) {
    		entry.setValue2(values[1]);
    	}
    	if (values.length >= 3) {
    		entry.setValue3(values[2]);
    	}
    	
    	return entry;
    }
    
    protected void save(ActionEvent event) {
    	
		FileChooser fileChooser = new FileChooser();	
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Midi config", "*.conf")
            );
		Window window = ((Node)event.getTarget()).getScene().getWindow();
		File file = fileChooser.showSaveDialog(window);
        if (file != null) {
        	FileWriter fw;
			try {
				fw = new FileWriter(file, false);
				fw.write("from consts import *\n\n");
				fw.write("midi_note_definitions = {\n");
				
				for (ConfigEntry entry : configEntries) {
					fw.write("\t" + getStringForEntry(entry) + "\n");	        	
				}
				
				fw.write("}\n");
	        	fw.close();
			} catch (IOException e) {
				logger.error("Could not save to file " + file.getAbsolutePath(), e);
			}
        }
    }
    
    private String getStringForEntry(ConfigEntry entry) {
    	StringBuffer sb = new StringBuffer();
    	
    	sb.append(entry.getMidiValue());
    	sb.append(" : ['");
    	sb.append(entry.getController());
    	sb.append("' , '");
    	sb.append(entry.getCommand());
    	sb.append("' , [");
    	if (StringUtils.isNotEmpty(entry.getValue1())) {
    		sb.append(entry.getValue1());
    	}
    	if (StringUtils.isNotEmpty(entry.getValue2())) {
    		sb.append(", ");
    		sb.append(entry.getValue2());
    	}
    	if (StringUtils.isNotEmpty(entry.getValue3())) {
    		sb.append(", ");
    		sb.append(entry.getValue3());
    	}
    	sb.append("]],");
    	return sb.toString();
    }
    
    protected void addInputToTable() {
    	
    	//TODO: check for plausability
    	
    	ConfigEntry ce = new ConfigEntry();
    	ce.setCommand(commandCombo.getValue());
    	ce.setController(controllerCombo.getValue());
    	ce.setMidiCommand(midiCommandCombo.getValue());
    	ce.setMidiValue(txtMidiValue.getText());
    	ce.setValue1(txtValue1.getText());
    	ce.setValue2(txtValue2.getText());
    	ce.setValue3(txtValue3.getText());
    	
    	configEntries.add(ce);
    }

	public void updateController(HashMap<String, List<String>> yaasCommands) {
		
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
