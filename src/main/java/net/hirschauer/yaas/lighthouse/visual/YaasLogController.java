package net.hirschauer.yaas.lighthouse.visual;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
	    private ComboBox<String> levelCombobox;


	    private ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();

	    public YaasLogController() {
	    }

	    @FXML
	    private void initialize() {
	    	levelColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("level"));
	    	messageColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("message"));

			logEntries.add(new LogEntry(new OSCMessage("LogController initialized", new Object[] {})));
			yaasLogEntryTable.setItems(logEntries);
			
			levelCombobox.valueProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(
						ObservableValue<? extends String> observable,
						String oldValue, String newValue) {
					
					logEntries.add(new LogEntry(new OSCMessage("Loglevel changed to " + newValue, new Object[] {})));
				}
			});
	    }

	    public void log(OSCMessage m) {
	        
	    	LogEntry logEntry = new LogEntry();
	    	if (m.getName().endsWith("info")) {
	    		logEntry.setLevel("info");
	    	} else if (m.getName().endsWith("debug")) {
	    		logEntry.setLevel("debug");
	    	} else if (m.getName().endsWith("error")) {
	    		logEntry.setLevel("error");
	    	}	    	
	    	logEntry.setMessage(m.getArg(0).toString());
	    	logEntries.add(logEntry);
	    }
}
