package net.hirschauer.yaas.lighthouse.visual;

import static net.hirschauer.yaas.lighthouse.model.ConfigMidi.MIDI_CC;
import static net.hirschauer.yaas.lighthouse.model.ConfigMidi.MIDI_NOTE_OFF;
import static net.hirschauer.yaas.lighthouse.model.ConfigMidi.MIDI_NOTE_ON;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.exceptions.ConfigurationException;
import net.hirschauer.yaas.lighthouse.model.ConfigCommand;
import net.hirschauer.yaas.lighthouse.model.ConfigLight;
import net.hirschauer.yaas.lighthouse.model.ConfigMidi;
import net.hirschauer.yaas.lighthouse.model.YaasConfiguration;
import net.hirschauer.yaas.lighthouse.model.osc.OSCMessage;
import net.hirschauer.yaas.lighthouse.model.osc.OSCMessageReceiveConfiguration;
import net.hirschauer.yaas.lighthouse.osccontroller.YaasController;
import net.hirschauer.yaas.lighthouse.util.IStorable;
import net.hirschauer.yaas.lighthouse.visual.components.LineEditor;
import net.hirschauer.yaas.lighthouse.visual.components.MidiLineEditor;
import net.hirschauer.yaas.lighthouse.visual.popups.ControllerSettings;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class ConfigurationController extends VisualController implements IStorable {

	Logger logger = LoggerFactory.getLogger(ConfigurationController.class);
		
   	@FXML
    private TableView<ConfigCommand> configTable;
    @FXML
    private TableColumn<ConfigCommand, String> colMidiCommand, colMidiValue, colController, 
    	colCommand, colMidiFollowSignal, colValue1, colValue2, colValue3;
    @FXML
    private Button btnAdd, btnLightSettings, btnSend, btnSendTemp;
    @FXML
    private BorderPane borderPane;
    
	private ObservableList<ConfigCommand> configEntries = FXCollections.observableArrayList();
    
    Gson gson = new Gson();

	private List<ConfigLight> configLightEntries = new ArrayList<ConfigLight>();

	public ConfigurationController() {
	}

    @FXML
	private void initialize() {
		logger.debug("init");
		
		setCellFactories();

		configTable.setItems(getConfigEntries());
		configTable.setEditable(false);
		configTable.setRowFactory( tv -> {
		    TableRow<ConfigCommand> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
		            logger.debug("double click on row");
		            showEditRow(row.getIndex());
		        }
		    });
		    return row ;
		});
				
		initMenu();
						
		btnAdd.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				configEntries.add(new ConfigMidi());
				showEditRow(configEntries.size() -1);
			}
		});
		
		btnLightSettings.setOnAction(event -> {
			configLightEntries = ControllerSettings.show(configLightEntries);
		});
		
		btnSend.setOnAction(event -> {
			copy(null);
		});
		
		btnSend.setOnAction(event -> {
			sendConfigurationToYaas(null);
		});
		
	}

	private void initMenu() {
		MenuItem mnuDel = new MenuItem("Delete row");
		mnuDel.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent t) {
		        logger.debug("Delete row");
                ConfigCommand p = configTable.getSelectionModel().getSelectedItem();
                if (p != null) {
                    configEntries.remove(p);
                }
		    }
		});
		MenuItem mnuEdit = new MenuItem("Edit");
		mnuEdit.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent t) {
		        logger.debug("Edit row");
                showEditRow(configTable.getSelectionModel().getSelectedIndex());
		    }
		});
		configTable.setContextMenu(new ContextMenu(mnuEdit, mnuDel));
	}
    
	private void showEditRow(int index) {
		
		ConfigCommand p = configEntries.get(index);
        if (p != null) {
        	AnchorPane root = new AnchorPane();
			Stage editStage = new Stage();				
			Scene editScene = new Scene(root);
			
            LineEditor editor = MidiLineEditor.show(root, p);
            editor.setStage(editStage);

            editStage.setScene(editScene);
			editStage.setAlwaysOnTop(true);
			editStage.showAndWait();
						
			configEntries.remove(index);
			
			ConfigCommand entry = (ConfigCommand) editor.getEntry();
			if (entry != null) {
				configEntries.add(index, entry);
			}
        }
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
    		List<ConfigCommand> entries = new ArrayList<ConfigCommand>();
    		List<ConfigLight> lightEntries = new ArrayList<ConfigLight>();
    		
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
								ConfigMidi entry = new ConfigMidi(line);
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
								ConfigLight lightEntry = new ConfigLight(line);
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

			for (ConfigCommand entry : this.getConfigEntries()) {
	    		
				String value1 = entry.getValue1() != null ? entry.getValue1() : "";
				String value2 = entry.getValue2() != null ? entry.getValue2() : "";
				String value3 = entry.getValue3() != null ? entry.getValue3() : "";
				String[] args;
				if (StringUtils.isEmpty(entry.getAdditionalValue())) {
					args = new String[] {entry.getConfigCommand(), entry.getConfigValue(), entry.getController(),
	        			entry.getCommand(), value1, value2, value3};
				} else {
					args = new String[] {entry.getConfigCommand(), entry.getConfigValue(), entry.getController(),
		        			entry.getCommand(), value1, value2, value3, entry.getAdditionalValue()};					
				}
	        	m = new OSCMessageReceiveConfiguration(args);
				oscServer.sendToYaas(m);
    		}
			for (ConfigLight entry : configLightEntries) {
	    		
	        	m = new OSCMessageReceiveConfiguration(entry.getLightCommand().toString(), entry.getMidiCommand(), entry.getMidiValue());
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
			for (ConfigCommand entry : getConfigEntries()) {
				
				if (entry.getConfigCommand().equals(MIDI_NOTE_ON)) {
					fw.write("\t" + entry.toString() + "\n");
				}
			}				
			fw.write("\t}\n\n");

			fw.write("midi_note_off_definitions = {\n");				
			for (ConfigCommand entry : getConfigEntries()) {
				
				if (entry.getConfigCommand().equals(MIDI_NOTE_OFF)) {
					fw.write("\t" + entry.toString() + "\n");
				}
			}				
			fw.write("\t}\n\n");

			fw.write("[CC]\n");
			fw.write("midi_cc_definitions = {\n");				
			for (ConfigCommand entry : getConfigEntries()) {
				
				if (entry.getConfigCommand().equals(MIDI_CC)) {
					fw.write("\t" + entry.toString() + "\n");
				}
			}				
			fw.write("\t}\n\n");
			
//			controller_definitions
			fw.write("[Addons]\n");
			fw.write("controller_definitions = {\n");				
			for (ConfigLight entry : configLightEntries) {
				
				fw.write("\t" + entry.toString() + "\n");
			}				
			fw.write("\t}\n\n");
			
        	fw.close();
		} catch (IOException e) {
			logger.error("Could not save to file " + file.getAbsolutePath(), e);
		}

    }

	public ObservableList<ConfigCommand> getConfigEntries() {
		return configEntries;
	}

	public void setConfigEntries(ObservableList<ConfigCommand> configEntries) {
		this.configEntries = configEntries;
	}

	@Override
	public void store(Properties values) {
		
		String className = getClass().getName();
		for (int i=0; i<configEntries.size(); i++) {
			ConfigCommand entry = configEntries.get(i);
			values.put(className + "|config|" + i, gson.toJson(entry));
		}
		for (int i=0; i<configLightEntries.size(); i++) {
			ConfigLight entry = configLightEntries.get(i);
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
					
					ConfigCommand configEntry = gson.fromJson((String) (String)values.get(key), ConfigMidi.class);
					configEntries.add(configEntry);
					
				} else if (entry[1].equals("light")) {
						
					ConfigLight configEntry = gson.fromJson((String) (String)values.get(key), ConfigLight.class);
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
		colMidiCommand.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("midiCommand"));
		colMidiValue.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("midiValue"));
		colMidiFollowSignal.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("midiFollowSignal"));
		colCommand.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("command"));
		colController.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("controller"));
		colValue1.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("value1"));
		colValue2.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("value2"));
		colValue3.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("value3"));
	}

	public void clear() {
		configEntries.clear();
		configLightEntries.clear();
	}
	
	protected String getMenuId() {
		return "configuration";
	}
}
