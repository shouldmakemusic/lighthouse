package net.hirschauer.yaas.lighthouse.visual;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import net.hirschauer.yaas.lighthouse.model.LogEntry;
import de.sciss.net.OSCMessage;

public class YaasLogController {
	
	   	@FXML
	    private TableView<LogEntry> yaasLogEntryTable;
	    @FXML
	    private TableColumn<LogEntry, String> messageColumn;
	    @FXML
	    private TableColumn<LogEntry, String> levelColumn;
	    @FXML
	    private TableColumn<LogEntry, String> timeColumn;
	    @FXML
	    private ComboBox<String> levelCombobox;
	    @FXML
	    private TextField inputFilter;
	    @FXML
	    private Button btnClear;

	    private static final Logger logger = LoggerFactory.getLogger(YaasLogController.class);
	    private ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();

	    public YaasLogController() {
	    }
	    
	    @FXML
	    private void initialize() {
	    	timeColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("timeString"));
	    	levelColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("level"));
	    	messageColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("message"));

			log("LogController initialized");

			FilteredList<LogEntry> leveledData = new FilteredList<>(logEntries, p -> true);
			levelCombobox.valueProperty().addListener(new ChangeListener<String>() {

				public void changed(ObservableValue<? extends String> observable,
						String oldValue, String newValue) {
			
					debug("Loglevel changed to " + newValue);
					leveledData.setPredicate(entry -> {
						if (newValue.toLowerCase().equals("info")) {
							if (entry.getLevel().equals("debug")) {
								return false;
							}
						}
						if (newValue.toLowerCase().equals("error")) {
							if (entry.getLevel().equals("info") || entry.getLevel().equals("debug")) {
								return false;
							}
						}
						return true;
					});
				}
			});
			
			FilteredList<LogEntry> filteredData = new FilteredList<>(leveledData, p -> true);
			inputFilter.textProperty().addListener((observable, oldValue, newValue) -> {
	            filteredData.setPredicate(entry -> {
	                // If filter text is empty, display all persons.
	                if (newValue == null || newValue.isEmpty()) {
	                    return true;
	                }

	                // Compare first name and last name of every person with filter text.
	                String lowerCaseFilter = newValue.toLowerCase();
	                logger.debug("Filtering " + lowerCaseFilter + " for message \"" + entry.getMessage() + "\"");

	                if (entry.getMessage().toLowerCase().indexOf(lowerCaseFilter) != -1) {
	                    return true; // Filter matches first name.
	                } else if (entry.getMessage().toLowerCase().indexOf(lowerCaseFilter) != -1) {
	                    return true; // Filter matches last name.
	                }
	                return false; // Does not match.
	            });
	        });
			yaasLogEntryTable.setItems(filteredData);
			
			btnClear.setOnAction(new EventHandler<ActionEvent>() {
				
				@Override
				public void handle(ActionEvent event) {
					logEntries.clear();
				}
			});
	    }

	    public void log(OSCMessage m) {
	        
	    	LogEntry logEntry = new LogEntry();
	    	logEntry.setLevel("debug");
	    	if (m.getName().endsWith("info")) {
	    		logEntry.setLevel("info");
	    	} else if (m.getName().endsWith("error")) {
	    		logEntry.setLevel("error");
	    	}	    	
	    	logEntry.setMessage(m.getArg(0).toString());
	    	logEntries.add(logEntry);
	    }
	    	    
	    public void log(String m) {
	        
	    	LogEntry logEntry = new LogEntry();
	    	logEntry.setLevel("info");    	
	    	logEntry.setMessage(m);
	    	logEntries.add(logEntry);
	    }
	    public void debug(String m) {
	        
	    	LogEntry logEntry = new LogEntry();
	    	logEntry.setLevel("debug");    	
	    	logEntry.setMessage(m);
	    	logEntries.add(logEntry);
	    }
	    public void error(String m) {
	        
	    	LogEntry logEntry = new LogEntry();
	    	logEntry.setLevel("error");    	
	    	logEntry.setMessage(m);
	    	logEntries.add(logEntry);
	    }
}
