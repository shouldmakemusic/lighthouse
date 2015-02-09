package net.hirschauer.yaas.lighthouse.visual;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.LogEntry;
import net.hirschauer.yaas.lighthouse.model.OSCMessageFromTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCMessage;

public class LogController {
	
	Logger logger = LoggerFactory.getLogger(LogController.class);
	
   	@FXML
    private TableView<LogEntry> logEntryTable;
    @FXML
    private TableColumn<LogEntry, String> messageColumn;
    @FXML
    private TableColumn<LogEntry, String> arg0Column;
    @FXML
    private TableColumn<LogEntry, String> arg1Column;
    @FXML
    private TableColumn<LogEntry, String> arg2Column;


    private ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();

    public LogController() {
    }

    @FXML
    private void initialize() {
    	messageColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("message"));
        arg0Column.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("arg0"));
        arg1Column.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("arg1"));
        arg2Column.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("arg2"));

		logEntries.add(new LogEntry(new OSCMessage("LogController initialized", new Object[] {})));
        logEntryTable.setItems(logEntries);
    }

    protected void log(OSCMessage m) {
        
    	logEntries.add(new LogEntry(m));
    }

    protected void log(OSCMessageFromTask m) {
        
    	logEntries.add(new LogEntry(m));
    }

	public void setOscServer(LightHouseOSCServer oscServer) {
		oscServer.messageProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				
				if (!newValue.startsWith("/yaas")) {

					//logger.debug("changed");
					OSCMessageFromTask m = new OSCMessageFromTask(newValue);								
					log(m);
					
				}
			}
		});	
	}
}
