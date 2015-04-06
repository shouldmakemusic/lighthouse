package net.hirschauer.yaas.lighthouse.visual;

import static net.hirschauer.yaas.lighthouse.model.ConfigEntry.MIDI_CC;
import static net.hirschauer.yaas.lighthouse.model.ConfigEntry.MIDI_NOTE_OFF;
import static net.hirschauer.yaas.lighthouse.model.ConfigEntry.MIDI_NOTE_ON;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.exceptions.ConfigurationException;
import net.hirschauer.yaas.lighthouse.model.ConfigLightEntry;
import net.hirschauer.yaas.lighthouse.model.ConfigMidiEntry;
import net.hirschauer.yaas.lighthouse.model.YaasConfiguration;
import net.hirschauer.yaas.lighthouse.model.osc.OSCMessage;
import net.hirschauer.yaas.lighthouse.model.osc.OSCMessageReceiveConfiguration;
import net.hirschauer.yaas.lighthouse.osccontroller.YaasController;
import net.hirschauer.yaas.lighthouse.util.IStorable;
import net.hirschauer.yaas.lighthouse.visual.components.MidiReceiver;
import net.hirschauer.yaas.lighthouse.visual.popups.ControllerSettings;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class ConfigurationController extends VisualController implements IStorable {

	Logger logger = LoggerFactory.getLogger(ConfigurationController.class);
		
	@FXML
	private ComboBox<String> controllerCombo, commandCombo;
   	@FXML
    private TableView<ConfigMidiEntry> configTable;
    @FXML
    private TableColumn<ConfigMidiEntry, String> colMidiCommand, colMidiValue, colController, 
    	colCommand, colMidiFollowSignal, colValue1, colValue2, colValue3;
    @FXML
    private TextField txtValue1, txtValue2, txtValue3;
    @FXML
    private Button btnAdd, btnLightSettings;
    @FXML
    private BorderPane borderPane;
    @FXML
    private AnchorPane paneInput;
    
	private ObservableList<ConfigMidiEntry> configEntries = FXCollections.observableArrayList();
	private ObservableList<String> controllerEntries = FXCollections.observableArrayList();
	private ObservableList<String> commandEntries = FXCollections.observableArrayList();
    
    Gson gson = new Gson();

	private List<ConfigLightEntry> configLightEntries = new ArrayList<ConfigLightEntry>();

	private MidiReceiver midiInputController;
	
	public ConfigurationController() {
	}

    @FXML
	private void initialize() {
		logger.debug("init");
		
		setCellFactories();

		configTable.setItems(getConfigEntries());
		configTable.setEditable(true);
		
		MenuItem mnuDel = new MenuItem("Delete row");
		mnuDel.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent t) {
		        logger.debug("Delete row");
                ConfigMidiEntry p = configTable.getSelectionModel().getSelectedItem();
                if (p != null) {
                    configEntries.remove(p);
                }
		    }
		});
		configTable.setContextMenu(new ContextMenu(mnuDel));
				
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
		
		btnAdd.setOnAction(event -> addInputToTable());
		
		btnLightSettings.setOnAction(event -> {
			configLightEntries = ControllerSettings.show(configLightEntries);
		});
		
		midiInputController = MidiReceiver.show(paneInput);
	}
    
	public void copy(Window window) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText("Overwrite current 'midi_mapping.cfg' from YAAS?");
		alert.setContentText("Requires a restart of Live to take effect.");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			YaasConfiguration config = YaasController.getInstance().yaasConfigurationProperty.get();
		    File file = new File(config.getYaasConfigFile());
		    if (!file.exists()) {
		    	try {
					file.createNewFile();
				} catch (IOException e) {
					logger.error("Could not create config file " + file.getAbsolutePath(), e);
				}
		    }
		    if (file.canWrite()) {
		    	writeToFile(file);
		    	alert = new Alert(AlertType.INFORMATION);
		    	alert.setTitle("OK");
		    	alert.setContentText("Configuration written to YAAS");
		    	alert.show();
		    }
		}
	}

    public void loadFromFile(Window window) {
    	
		FileChooser fileChooser = new FileChooser();	
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("config", "*.conf", "*.py", "*.cfg")
            );
		File file = fileChooser.showOpenDialog(window);
        if (file != null) {
    		List<ConfigMidiEntry> entries = new ArrayList<ConfigMidiEntry>();
    		List<ConfigLightEntry> lightEntries = new ArrayList<ConfigLightEntry>();
    		
    		List<String> lines = null;
        	try {
				lines = FileUtils.readLines(file);
			} catch (IOException e) {
				logger.error("Could not read file " + file.getAbsolutePath(), e);
			}
        	if (lines != null) {
        		
				int mode = 0;
				int lineNumber = 0;
				for (String line : lines) {
					
					lineNumber++;
					line = line.trim();
					if (line.startsWith("[") && line.endsWith("]")) {
						continue;
					}
					if (line.equals("}")) {
						mode = 0;
						continue;
					} 
					if (line.startsWith("#")) {
						continue;
					}
					switch (mode) {
						case 0:
							if (line.equals("midi_note_definitions = {")) {
								mode = 1;
							}
							if (line.equals("midi_cc_definitions = {")) {
								mode = 2;
							}
							if (line.equals("midi_note_off_definitions = {")) {
								mode = 3;
							}
							if (line.equals("controller_definitions = {")) {
								mode = 4;
							}
							break;
						case 1:
						case 2:
						case 3:
							try {
								ConfigMidiEntry entry = new ConfigMidiEntry(line);
								entry.setMidiCommand(MIDI_NOTE_ON);
								if (mode == 2) {
									entry.setMidiCommand(MIDI_CC);
								}
								if (mode == 3) {
									entry.setMidiCommand(MIDI_NOTE_OFF);
								}
								entries.add(entry);
							} catch (ConfigurationException e) {
								logger.warn("Ignored line " + lineNumber, e);
							}
							break;
						case 4:						
							try {
								ConfigLightEntry lightEntry = new ConfigLightEntry(line);
								lightEntries.add(lightEntry);
							} catch (ConfigurationException e) {
								logger.warn("Ignored line " + lineNumber, e);
							}
					}
				}
        	}
        	if (entries.size() > 0) {
				this.configEntries.clear();
				this.configEntries.addAll(entries);
        	}
        	if (lightEntries.size() > 0) {
        		this.configLightEntries.clear();
        		this.configLightEntries.addAll(lightEntries);
        	}
        }
    }
    
    public void sendConfigurationToYaas(Window window) {
    	// /yaas/controller/receive/configuration
    	LightHouseOSCServer oscServer = LightHouseOSCServer.getInstance();
    	try {
        	OSCMessage m = new OSCMessageReceiveConfiguration("start");
			oscServer.sendToYaas(m);

			for (ConfigMidiEntry entry : this.getConfigEntries()) {
	    		
				String value1 = entry.getValue1() != null ? entry.getValue1() : "";
				String value2 = entry.getValue2() != null ? entry.getValue2() : "";
				String value3 = entry.getValue3() != null ? entry.getValue3() : "";
				String[] args;
				if (StringUtils.isEmpty(entry.getMidiFollowSignal())) {
					args = new String[] {entry.getMidiCommand(), entry.getMidiValue(), entry.getController(),
	        			entry.getCommand(), value1, value2, value3};
				} else {
					args = new String[] {entry.getMidiCommand(), entry.getMidiValue(), entry.getController(),
		        			entry.getCommand(), value1, value2, value3, entry.getMidiFollowSignal()};					
				}
	        	m = new OSCMessageReceiveConfiguration(args);
				oscServer.sendToYaas(m);
    		}
			for (ConfigLightEntry entry : configLightEntries) {
	    		
	        	m = new OSCMessageReceiveConfiguration(entry.getCommand().toString(), entry.getMidiCommand(), entry.getMidiValue());
				oscServer.sendToYaas(m);
    		}
			
        	m = new OSCMessageReceiveConfiguration("end");
			oscServer.sendToYaas(m);
			
		} catch (IOException e) {
			logger.error("Error when sending configuration", e);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Could not send configuration to YAAS");
			alert.show();
		}
    }    
    
    public void saveToFile(Window window) {
    	
		FileChooser fileChooser = new FileChooser();	
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Midi config", "*.conf, *.cfg"),
                new FileChooser.ExtensionFilter("Python config", "*.py")
            );
		File file = fileChooser.showSaveDialog(window);
        if (file != null) {
        	writeToFile(file);
        }
    }
    
    private void writeToFile(File file) {
    	FileWriter fw;
		try {
			fw = new FileWriter(file, false);
			fw.write("[MidiIn]\n");

			fw.write("midi_note_definitions = {\n");				
			for (ConfigMidiEntry entry : getConfigEntries()) {
				
				if (entry.getMidiCommand().equals(MIDI_NOTE_ON)) {
					fw.write("\t" + entry.toString() + "\n");
				}
			}				
			fw.write("\t}\n\n");

			fw.write("midi_note_off_definitions = {\n");				
			for (ConfigMidiEntry entry : getConfigEntries()) {
				
				if (entry.getMidiCommand().equals(MIDI_NOTE_OFF)) {
					fw.write("\t" + entry.toString() + "\n");
				}
			}				
			fw.write("\t}\n\n");

			fw.write("[CC]\n");
			fw.write("midi_cc_definitions = {\n");				
			for (ConfigMidiEntry entry : getConfigEntries()) {
				
				if (entry.getMidiCommand().equals(MIDI_CC)) {
					fw.write("\t" + entry.toString() + "\n");
				}
			}				
			fw.write("\t}\n\n");
			
//			controller_definitions
			fw.write("[Addons]\n");
			fw.write("controller_definitions = {\n");				
			for (ConfigLightEntry entry : configLightEntries) {
				
				fw.write("\t" + entry.toString() + "\n");
			}				
			fw.write("\t}\n\n");
			
        	fw.close();
		} catch (IOException e) {
			logger.error("Could not save to file " + file.getAbsolutePath(), e);
		}

    }
    
    protected void addInputToTable() {
    	
    	String error = midiInputController.verify();
    	if (StringUtils.isEmpty(controllerCombo.getValue())) {
    		error += "Controller has to be set\n";
    	}
    	if (StringUtils.isNotEmpty(error)) {
    		Alert alert = new Alert(AlertType.ERROR);
    		alert.setTitle("Error Dialog");
    		alert.setHeaderText(null);
    		alert.setContentText(error);
    		alert.showAndWait();    	
    		return;
    	}
    	
    	ConfigMidiEntry ce = new ConfigMidiEntry(midiInputController.getMidiInput());
    	ce.setCommand(commandCombo.getValue());
    	ce.setController(controllerCombo.getValue());
    	ce.setValue1(txtValue1.getText());
    	ce.setValue2(txtValue2.getText());
    	ce.setValue3(txtValue3.getText());
    	
    	getConfigEntries().add(ce);
    }

	public void updateControllerCombo() {
		
		Map<String, List<String>> yaasCommands = YaasController.getInstance().yaasCommands; 
		controllerEntries.clear();
		for (String name : yaasCommands.keySet()) {
//			logger.debug("added controller " + name);
			controllerEntries.add(name);
		}
		controllerCombo.setItems(controllerEntries);
	}

	public ObservableList<ConfigMidiEntry> getConfigEntries() {
		return configEntries;
	}

	public void setConfigEntries(ObservableList<ConfigMidiEntry> configEntries) {
		this.configEntries = configEntries;
	}

	@Override
	public void store(Properties values) {
		
		String className = getClass().getName();
		for (int i=0; i<configEntries.size(); i++) {
			ConfigMidiEntry entry = configEntries.get(i);
			values.put(className + "|config|" + i, gson.toJson(entry));
		}
		for (int i=0; i<configLightEntries.size(); i++) {
			ConfigLightEntry entry = configLightEntries.get(i);
			values.put(className + "|light|" + i, gson.toJson(entry));
		}
		ObservableMap<String, List<String>> commands = YaasController.getInstance().yaasCommands;
		for (Entry<String, List<String>> controller : commands.entrySet()) {
			
			for (int i = 0; i < controller.getValue().size(); i++) {
				values.put(className + "|controller|" + controller.getKey() + "|" + i, controller.getValue().get(i));
			}
		}
	}

	@Override
	public void load(Properties values) {
		
		String className = getClass().getName();
		for (Object keyObj : values.keySet()) {
			String key = keyObj.toString();
			if (key.startsWith(className)) {
				
				String[] entry = key.split("\\|");
				
				if (entry[1].equals("config")) {
					
					ConfigMidiEntry configEntry = gson.fromJson((String) (String)values.get(key), ConfigMidiEntry.class);
					configEntries.add(configEntry);
					
				} else if (entry[1].equals("light")) {
						
					ConfigLightEntry configEntry = gson.fromJson((String) (String)values.get(key), ConfigLightEntry.class);
					configLightEntries.add(configEntry);
						
				} else if (entry[1].equals("controller")) {
					ObservableMap<String, List<String>> commands = YaasController.getInstance().yaasCommands;
					if (!commands.containsKey(entry[2])) {
						commands.put(entry[2], new ArrayList<String>());
					}
					commands.get(entry[2]).add((String)values.get(key));
				}
			}
		}
	}
	
	private void setCellFactories() {
		colMidiCommand.setCellValueFactory(new PropertyValueFactory<ConfigMidiEntry, String>("midiCommand"));
		colMidiCommand.setCellFactory(TextFieldTableCell.forTableColumn());
		colMidiCommand.setOnEditCommit(cellEditEvent -> {
		            ((ConfigMidiEntry) cellEditEvent.getTableView().getItems().get(
		            		cellEditEvent.getTablePosition().getRow())
		                ).setMidiCommand(cellEditEvent.getNewValue());
		    }
		);
		colMidiValue.setCellValueFactory(new PropertyValueFactory<ConfigMidiEntry, String>("midiValue"));
		colMidiValue.setCellFactory(TextFieldTableCell.forTableColumn());
		colMidiValue.setOnEditCommit(cellEditEvent -> {
		        	
		        	if (StringUtils.isNumeric(cellEditEvent.getNewValue())) {
			            ((ConfigMidiEntry) cellEditEvent.getTableView().getItems().get(
			            		cellEditEvent.getTablePosition().getRow())
			                ).setMidiValue(cellEditEvent.getNewValue());
		        	} else {
		        		
		        		Alert alert = new Alert(AlertType.INFORMATION);
		        		alert.setTitle("Information Dialog");
		        		alert.setHeaderText(null);
		        		alert.setContentText("Midi value has to be a integer!");
		        		alert.showAndWait();
		        		cellEditEvent.getTableView().getColumns().get(1).setVisible(false);
		        		cellEditEvent.getTableView().getColumns().get(1).setVisible(true);		        		
		        	}
		    });
		colMidiFollowSignal.setCellValueFactory(new PropertyValueFactory<ConfigMidiEntry, String>("midiFollowSignal"));
		colMidiFollowSignal.setCellFactory(TextFieldTableCell.forTableColumn());
		colMidiFollowSignal.setOnEditCommit(cellEditEvent -> {
		        	
		        	if (StringUtils.isEmpty(cellEditEvent.getNewValue()) || StringUtils.isNumeric(cellEditEvent.getNewValue())) {
			            ((ConfigMidiEntry) cellEditEvent.getTableView().getItems().get(
			            		cellEditEvent.getTablePosition().getRow())
			                ).setMidiFollowSignal(cellEditEvent.getNewValue());
		        	} else {
		        		
		        		Alert alert = new Alert(AlertType.INFORMATION);
		        		alert.setTitle("Information Dialog");
		        		alert.setHeaderText(null);
		        		alert.setContentText("Midi follow signal has to be a integer!");
		        		alert.showAndWait();
		        		cellEditEvent.getTableView().getColumns().get(1).setVisible(false);
		        		cellEditEvent.getTableView().getColumns().get(1).setVisible(true);		        		
		        	}
		    });		
		colMidiValue.setComparator((String o1, String o2) -> {
				Integer i1 = Integer.parseInt(o1);
				Integer i2 = Integer.parseInt(o2);
				return i1.compareTo(i2);
		});
		colCommand.setCellValueFactory(new PropertyValueFactory<ConfigMidiEntry, String>("command"));
		colCommand.setCellFactory(ComboBoxTableCell.forTableColumn(commandEntries));
		colCommand.setOnEditStart(cellEditEvent -> {
				String controller = cellEditEvent.getRowValue().getController();
				Map<String, List<String>> yaasCommands = YaasController.getInstance().yaasCommands;
				commandEntries.clear();
				if (yaasCommands.containsKey(controller)) {
					for (String name : yaasCommands.get(controller)) {
						commandEntries.add(name);
					}
				}
		});		
		colCommand.setOnEditCommit(cellEditEvent ->  {
		            ((ConfigMidiEntry) cellEditEvent.getTableView().getItems().get(
		            		cellEditEvent.getTablePosition().getRow())
		                ).setCommand(cellEditEvent.getNewValue());
		            colCommand.setVisible(false);
		            colCommand.setVisible(true);
		    }
		);

		colController.setCellValueFactory(new PropertyValueFactory<ConfigMidiEntry, String>("controller"));
		colController.setCellFactory(ComboBoxTableCell.forTableColumn(controllerEntries));
		colController.setOnEditCommit(cellEditEvent -> {
		            if (cellEditEvent.getNewValue() != "" && !cellEditEvent.getNewValue().equals(cellEditEvent.getRowValue().getController())) {
		            	cellEditEvent.getRowValue().setCommand("");
		            	configTable.getColumns().get(3).setVisible(false);
		            	configTable.getColumns().get(3).setVisible(true);
		            }
		            ((ConfigMidiEntry) cellEditEvent.getTableView().getItems().get(
		            		cellEditEvent.getTablePosition().getRow())
			                ).setController(cellEditEvent.getNewValue());		       
		    }
		);
		colValue1.setCellValueFactory(new PropertyValueFactory<ConfigMidiEntry, String>("value1"));
		colValue1.setCellFactory(TextFieldTableCell.forTableColumn());
		colValue1.setOnEditCommit(cellEditEvent ->  {
		            ((ConfigMidiEntry) cellEditEvent.getTableView().getItems().get(
		            		cellEditEvent.getTablePosition().getRow())
		                ).setValue1(cellEditEvent.getNewValue());
		    }
		);
		colValue2.setCellValueFactory(new PropertyValueFactory<ConfigMidiEntry, String>("value2"));
		colValue2.setCellFactory(TextFieldTableCell.forTableColumn());
		colValue2.setOnEditCommit(cellEditEvent ->  {
		            ((ConfigMidiEntry) cellEditEvent.getTableView().getItems().get(
		            		cellEditEvent.getTablePosition().getRow())
		                ).setValue2(cellEditEvent.getNewValue());
		        }
		);
		colValue3.setCellValueFactory(new PropertyValueFactory<ConfigMidiEntry, String>("value3"));
		colValue3.setCellFactory(TextFieldTableCell.forTableColumn());
		colValue3.setOnEditCommit(cellEditEvent -> {
			((ConfigMidiEntry) cellEditEvent.getTableView().getItems().get(
					cellEditEvent.getTablePosition().getRow())
		                ).setValue3(cellEditEvent.getNewValue());
		    });
	}

	public void clear() {
		configEntries.clear();
		configLightEntries.clear();
	}
	
	protected String getMenuId() {
		return "configuration";
	}
}
