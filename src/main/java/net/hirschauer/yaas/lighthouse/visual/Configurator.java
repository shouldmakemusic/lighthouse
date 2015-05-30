package net.hirschauer.yaas.lighthouse.visual;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.ConfigCommand;
import net.hirschauer.yaas.lighthouse.model.YaasConfiguration;
import net.hirschauer.yaas.lighthouse.model.osc.OSCMessage;
import net.hirschauer.yaas.lighthouse.model.osc.OSCMessageReceiveConfiguration;
import net.hirschauer.yaas.lighthouse.osccontroller.YaasController;
import net.hirschauer.yaas.lighthouse.util.IStorable;
import net.hirschauer.yaas.lighthouse.visual.components.LineEditor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public abstract class Configurator extends VisualController implements IStorable {
	
	private static final Logger logger = LoggerFactory.getLogger(Configurator.class);
	
    protected Gson gson = new Gson();
	protected ObservableList<ConfigCommand> configEntries = FXCollections.observableArrayList();
    
	public Configurator() { }

	abstract String getFileString();
	abstract void clearConfig();
	abstract void restoreConfig();
	abstract void handleConfigurationLine(String mode, String line, String prefix, int lineNumber);
	abstract void sendIndividualConfiguration(LightHouseOSCServer oscServer) throws IOException;
	public abstract LineEditor getLineEditor(AnchorPane root, ConfigCommand configEntry);
	abstract void storeIndividualCommands(Properties values);
	abstract void loadCommands(String value, String[] entry);
	
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
    		ObservableList<ConfigCommand> backupEntries = FXCollections.observableArrayList();
    		backupEntries.addAll(configEntries);
    		configEntries.clear();
    		clearConfig();
    		
    		List<String> lines = null;
        	try {
				lines = FileUtils.readLines(file);
			} catch (IOException e) {
				logger.error("Could not read file " + file.getAbsolutePath(), e);
			}
        	if (lines != null) {
        		
				String mode = "init";
				int lineNumber = 0;
				String prefix = null;
				for (String line : lines) {
					
					lineNumber++;
					line = line.trim();
					if (line.startsWith("[") && line.endsWith("]")) {
						continue;
					}
					if (line.equals("}")) {
						mode = "init";
						continue;
					} 
					if (line.startsWith("#")) {
						continue;
					}
					switch (mode) {
						case "init":
							if (line.endsWith(" = {")) {
								mode = line.substring(0, line.length() - 4).trim();
							}
							break;
						default:
							if (line.endsWith(": [")) {
								prefix = line.split(":")[0].trim();
								continue;
							}
							if (line.equals("],")) {
								prefix = null;
								break;
							}
							handleConfigurationLine(mode, prefix, line, lineNumber);
							break;
					}
				}
        	}
        	if (configEntries.size() == 0) {
				this.configEntries = backupEntries;
				restoreConfig();
        	}
        }
    }
    
    public void sendConfigurationToYaas(Window window) {
    	// /yaas/controller/receive/configuration
    	LightHouseOSCServer oscServer = LightHouseOSCServer.getInstance();
    	try {
        	OSCMessage m = new OSCMessageReceiveConfiguration("start");
			oscServer.sendToYaas(m);

			for (ConfigCommand entry : configEntries) {
	    		
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
			sendIndividualConfiguration(oscServer);
			
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
    
    protected String getStringForCommand(String configCommand) {
    	StringBuffer sb = new StringBuffer();
		Map<Object, List<ConfigCommand>> commandEntries = getPreparedValues(configEntries, configCommand);
		
		for (Object key : commandEntries.keySet()) {
			List<ConfigCommand> commandEntryList = commandEntries.get(key);
			
			if (commandEntryList.size() == 1) {
									
				sb.append("\t" + commandEntryList.get(0).toString() + "\n");
			} else {
				
				sb.append("\t" + key + ": [\n");
				for (ConfigCommand command : commandEntryList) {
					sb.append("\t\t" + command.getAsString() + "\n");
				}
				sb.append("\t],\n");
			}
		}				
		sb.append("\t}\n\n");
		return sb.toString();
    }
    
    private Map<Object, List<ConfigCommand>> getPreparedValues(ObservableList<ConfigCommand> entries, String configCommand) {
    	
    	HashMap<Object, List<ConfigCommand>> commands = new HashMap<Object, List<ConfigCommand>>();
    	for (ConfigCommand entry : entries) {
    		if (configCommand.equals(entry.getConfigCommand())) {
    			Object key = entry.getConfigValue();
    			if (!commands.containsKey(key)) {
    				commands.put(key, new ArrayList<ConfigCommand>());
    			}
    			commands.get(key).add(entry);
    		}
    	}
    	return commands;    	
    }
        
    private void writeToFile(File file) {
    	FileWriter fw;
		try {
			fw = new FileWriter(file, false);
			fw.write(getFileString());
        	fw.close();
		} catch (IOException e) {
			logger.error("Could not save to file " + file.getAbsolutePath(), e);
		}

    }


	@Override
	public void store(Properties values) {
		
		String className = getClass().getName();
		for (int i=0; i<configEntries.size(); i++) {
			ConfigCommand entry = configEntries.get(i);
			values.put(className + "|config|" + i, gson.toJson(entry));
		}
		storeIndividualCommands(values);
		
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
				loadCommands((String)values.get(key), entry);				
			}
		}
	}
	public void clear() {
		configEntries.clear();
		clearConfig();
	}
	
	protected String getMenuId() {
		return "configuration";
	}

	public ObservableList<ConfigCommand> getConfigEntries() {
		
		return this.configEntries;
	}	

}
