package net.hirschauer.yaas.lighthouse.visual;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import net.hirschauer.yaas.lighthouse.LightHouseMidiReceiver;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.ConfigEntry;
import net.hirschauer.yaas.lighthouse.model.MidiLogEntry;
import net.hirschauer.yaas.lighthouse.model.YaasConfiguration;
import net.hirschauer.yaas.lighthouse.osccontroller.YaasController;
import net.hirschauer.yaas.lighthouse.util.IStorable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.sciss.net.OSCMessage;

public class ConfigurationController implements IStorable {

	Logger logger = LoggerFactory.getLogger(ConfigurationController.class);
	
	public static final String MIDI_NOTE = "Midi Note";
	public static final String MIDI_CC = "Midi CC";
	
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
    @FXML
    private Button btnCopy;
    @FXML
    private BorderPane borderPane;
    
	private ObservableList<ConfigEntry> configEntries = FXCollections.observableArrayList();
	private ObservableList<String> controllerEntries = FXCollections.observableArrayList();
	private ObservableList<String> commandEntries = FXCollections.observableArrayList();
    
    private static ConfigurationController instance;
    
    Gson gson = new Gson();
    
    public ConfigurationController() {
		instance = this;
	}
    
    public static ConfigurationController getInstance() {
    	return instance;
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
                ConfigEntry p = configTable.getSelectionModel().getSelectedItem();
                if(p!=null) {
                    configEntries.remove(p);
                }
		    }
		});
		configTable.setContextMenu(new ContextMenu(mnuDel));
		
		midiCommandCombo.setValue(MIDI_NOTE);
		
		btnReceiveMidi.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				if (!LightHouseMidiReceiver.getInstance().hasDevice()) {
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
								} else {
									midiCommandCombo.setValue(MIDI_NOTE);
								}
								// TODO: midi note off			
								txtMidiValue.setText(nextMidi.getData1());
								btnReceiveMidi.setDisable(false);
							}
						});

						LightHouseMidiReceiver.getInstance().logEntries.removeListener(this);
					}					
				};
				LightHouseMidiReceiver.getInstance().logEntries.addListener(changeListener);
			}
		});
		btnReceive.setDisable(true);
		
		btnSend.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				sendConfigurationToYaas(((Node)event.getTarget()).getScene().getWindow());
			}
		});
		
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
		
		btnCopy.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				copy(((Node)event.getTarget()).getScene().getWindow());
			}
		});		
		
		YaasController.getInstance().yaasCommands.addListener(new MapChangeListener<String, List<String>>() {

			@Override
			public void onChanged(Change change) {
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
		
		controllerCombo.valueProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				
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
			}
		});

	}
    
	protected void copy(Window window) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText("Overwrite current 'config_midi.py' from YAAS?");
		alert.setContentText("Requires a restart of Live. There will be a better solution soon than overwriting a python file...");

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

    
    protected void load(ActionEvent event) {
    	
		FileChooser fileChooser = new FileChooser();	
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("config", "*.conf, *.py")
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
						case 2:
							if (line.equals("}")) {
								mode = 0;
								continue;
							} 
							if (line.startsWith("#")) {
								continue;
							}
							ConfigEntry entry = getEntryFromString(line);
							if (entry != null) {
								entry.setMidiCommand(MIDI_NOTE);
								if (mode == 2) {
									entry.setMidiCommand(MIDI_CC);
								}
								entries.add(entry);
							}
							break;
					}
				}
			} catch (IOException e) {
				logger.error("Could not read file " + file.getAbsolutePath(), e);
			}
        	if (entries.size() > 0) {
				this.getConfigEntries().clear();
				for (ConfigEntry entry : entries) {
					this.getConfigEntries().add(entry);
				}
        	}
        }
    }
    
    protected void sendConfigurationToYaas(Window window) {
    	// /yaas/controller/receive/configuration
    	LightHouseOSCServer oscServer = LightHouseOSCServer.getInstance();
    	try {
        	Object[] args = new Object[] {"start"};
        	OSCMessage m = new OSCMessage("/yaas/controller/receive/configuration", args);
			oscServer.sendToYaas(m);

			for (ConfigEntry entry : this.getConfigEntries()) {
    		
				String value1 = entry.getValue1() != null ? entry.getValue1() : "";
				String value2 = entry.getValue2() != null ? entry.getValue2() : "";
				String value3 = entry.getValue3() != null ? entry.getValue3() : "";
	        	args = new Object[] {entry.getMidiCommand(), entry.getMidiValue(), entry.getController(),
	        			entry.getCommand(), value1, value2, value3};
	        	m = new OSCMessage("/yaas/controller/receive/configuration", args);
				oscServer.sendToYaas(m);
    		}
        	args = new Object[] {"end"};
        	m = new OSCMessage("/yaas/controller/receive/configuration", args);
			oscServer.sendToYaas(m);
			
		} catch (IOException e) {
			logger.error("Error when sending configuration", e);
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Could not send configuration to YAAS");
			alert.show();
		}
    }
    
    protected ConfigEntry getEntryFromString(String line) {
    	
    	// TODO: add error handling
    	ConfigEntry entry = new ConfigEntry();
    	String[] midiCommand = line.split(":");
    	if (midiCommand.length < 2) {
    		return null;
    	}
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
                new FileChooser.ExtensionFilter("Midi config", "*.conf"),
                new FileChooser.ExtensionFilter("Python config", "*.py")
            );
		Window window = ((Node)event.getTarget()).getScene().getWindow();
		File file = fileChooser.showSaveDialog(window);
        if (file != null) {
        	writeToFile(file);
        }
    }
    
    private void writeToFile(File file) {
    	FileWriter fw;
		try {
			fw = new FileWriter(file, false);
			fw.write("from consts import *\n\n");

			fw.write("midi_note_definitions = {\n");				
			for (ConfigEntry entry : getConfigEntries()) {
				
				if (entry.getMidiCommand().equals(MIDI_NOTE)) {
					fw.write("\t" + getStringForEntry(entry) + "\n");
				}
			}				
			fw.write("}\n\n");

			fw.write("midi_cc_definitions = {\n");				
			for (ConfigEntry entry : getConfigEntries()) {
				
				if (entry.getMidiCommand().equals(MIDI_CC)) {
					fw.write("\t" + getStringForEntry(entry) + "\n");
				}
			}				
			fw.write("}\n\n");
			
        	fw.close();
		} catch (IOException e) {
			logger.error("Could not save to file " + file.getAbsolutePath(), e);
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
    	
    	String error = "";
    	if (StringUtils.isEmpty(txtMidiValue.getText())) {
    		error = "Midi value has to be set\n";
    	} else {
    		for (ConfigEntry entry : configEntries) {
    			if (entry.getMidiValue().equals(txtMidiValue.getText())) {
    				
    			}
    		}
    	}
    	if (StringUtils.isEmpty(controllerCombo.getValue())) {
    		error += "Controller has to be set\n";
    	}
    	if (StringUtils.isEmpty(midiCommandCombo.getValue())) {
    		error += "Command has to be set\n";
    	}
    	if (StringUtils.isNotEmpty(error)) {
    		Alert alert = new Alert(AlertType.ERROR);
    		alert.setTitle("Error Dialog");
    		alert.setHeaderText(null);
    		alert.setContentText(error);
    		alert.showAndWait();    	
    		return;
    	}
    	ConfigEntry ce = new ConfigEntry();
    	ce.setCommand(commandCombo.getValue());
    	ce.setController(controllerCombo.getValue());
    	ce.setMidiCommand(midiCommandCombo.getValue());
    	ce.setMidiValue(txtMidiValue.getText());
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

	public ObservableList<ConfigEntry> getConfigEntries() {
		return configEntries;
	}

	public void setConfigEntries(ObservableList<ConfigEntry> configEntries) {
		this.configEntries = configEntries;
	}

	@Override
	public void store(Properties values) {
		
		String className = getClass().getName();
		for (int i=0; i<configEntries.size(); i++) {
			ConfigEntry entry = configEntries.get(i);
			values.put(className + "|" + i, gson.toJson(entry));
		}
	}

	@Override
	public void load(Properties values) {
		
		String className = getClass().getName();
		for (Object keyObj : values.keySet()) {
			String key = keyObj.toString();
			if (key.startsWith(className)) {
				int i = Integer.parseInt(key.split("\\|")[1]);
				
				ConfigEntry entry = gson.fromJson((String) values.get(keyObj), ConfigEntry.class);
				configEntries.add(entry);
			}
		}
	}
	
	private void setCellFactories() {
		colMidiCommand.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("midiCommand"));
		colMidiCommand.setCellFactory(TextFieldTableCell.forTableColumn());
		colMidiCommand.setOnEditCommit(
		    new EventHandler<CellEditEvent<ConfigEntry, String>>() {
		        @Override
		        public void handle(CellEditEvent<ConfigEntry, String> t) {
		            ((ConfigEntry) t.getTableView().getItems().get(
		                t.getTablePosition().getRow())
		                ).setMidiCommand(t.getNewValue());
		        }
		    }
		);
		colMidiValue.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("midiValue"));
		colMidiValue.setCellFactory(TextFieldTableCell.forTableColumn());
		colMidiValue.setOnEditCommit(
		    new EventHandler<CellEditEvent<ConfigEntry, String>>() {
		        @Override
		        public void handle(CellEditEvent<ConfigEntry, String> t) {
		        	
		        	if (StringUtils.isNumeric(t.getNewValue())) {
			            ((ConfigEntry) t.getTableView().getItems().get(
			                t.getTablePosition().getRow())
			                ).setMidiValue(t.getNewValue());
		        	} else {
		        		
		        		Alert alert = new Alert(AlertType.INFORMATION);
		        		alert.setTitle("Information Dialog");
		        		alert.setHeaderText(null);
		        		alert.setContentText("Midi value has to be a integer!");
		        		alert.showAndWait();
		        		t.getTableView().getColumns().get(1).setVisible(false);
		        		t.getTableView().getColumns().get(1).setVisible(true);		        		
		        	}
		        }
		    });
		colMidiValue.setComparator(new Comparator<String>() {
			
			@Override
			public int compare(String o1, String o2) {
				Integer i1 = Integer.parseInt(o1);
				Integer i2 = Integer.parseInt(o2);
				return i1.compareTo(i2);
			}
		});
		colCommand.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("command"));
		colCommand.setCellFactory(ComboBoxTableCell.forTableColumn(commandEntries));
		colCommand.setOnEditStart(new EventHandler<TableColumn.CellEditEvent<ConfigEntry,String>>() {
			@Override
			public void handle(CellEditEvent<ConfigEntry, String> event) {
				String controller = event.getRowValue().getController();
				Map<String, List<String>> yaasCommands = YaasController.getInstance().yaasCommands;
				commandEntries.clear();
				if (yaasCommands.containsKey(controller)) {
					for (String name : yaasCommands.get(controller)) {
						commandEntries.add(name);
					}
				}
			}
		});
		colCommand.setOnEditCommit(
		    new EventHandler<CellEditEvent<ConfigEntry, String>>() {
		        @Override
		        public void handle(CellEditEvent<ConfigEntry, String> t) {
		            ((ConfigEntry) t.getTableView().getItems().get(
		                t.getTablePosition().getRow())
		                ).setCommand(t.getNewValue());
		        }
		    }
		);
		colController.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("controller"));
		colController.setCellFactory(ComboBoxTableCell.forTableColumn(controllerEntries));
		colController.setOnEditCommit(
		    new EventHandler<CellEditEvent<ConfigEntry, String>>() {
		        @Override
		        public void handle(CellEditEvent<ConfigEntry, String> t) {
		            if (t.getNewValue() != "" && !t.getNewValue().equals(t.getRowValue().getController())) {
		            	t.getRowValue().setCommand("");
		            	configTable.getColumns().get(3).setVisible(false);
		            	configTable.getColumns().get(3).setVisible(true);
		            }
		            ((ConfigEntry) t.getTableView().getItems().get(
			                t.getTablePosition().getRow())
			                ).setController(t.getNewValue());
		        }
		    }
		);
		colValue1.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("value1"));
		colValue1.setCellFactory(TextFieldTableCell.forTableColumn());
		colValue1.setOnEditCommit(
		    new EventHandler<CellEditEvent<ConfigEntry, String>>() {
		        @Override
		        public void handle(CellEditEvent<ConfigEntry, String> t) {
		            ((ConfigEntry) t.getTableView().getItems().get(
		                t.getTablePosition().getRow())
		                ).setValue1(t.getNewValue());
		        }
		    }
		);
		colValue2.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("value2"));
		colValue2.setCellFactory(TextFieldTableCell.forTableColumn());
		colValue2.setOnEditCommit(
		    new EventHandler<CellEditEvent<ConfigEntry, String>>() {
		        @Override
		        public void handle(CellEditEvent<ConfigEntry, String> t) {
		            ((ConfigEntry) t.getTableView().getItems().get(
		                t.getTablePosition().getRow())
		                ).setValue2(t.getNewValue());
		        }
		    }
		);
		colValue3.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("value3"));
		colValue3.setCellFactory(TextFieldTableCell.forTableColumn());
		colValue3.setOnEditCommit(
		    new EventHandler<CellEditEvent<ConfigEntry, String>>() {
		        @Override
		        public void handle(CellEditEvent<ConfigEntry, String> t) {
		            ((ConfigEntry) t.getTableView().getItems().get(
		                t.getTablePosition().getRow())
		                ).setValue3(t.getNewValue());
		        }
		    }
		);
	}

}
