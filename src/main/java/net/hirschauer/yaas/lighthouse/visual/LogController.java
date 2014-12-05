package net.hirschauer.yaas.lighthouse.visual;

import java.awt.Label;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import net.hirschauer.yaas.lighthouse.model.LogEntry;
import de.sciss.net.OSCMessage;

public class LogController {
	
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

	    @FXML
	    private Label firstNameLabel;
	    @FXML
	    private Label lastNameLabel;
	    @FXML
	    private Label streetLabel;
	    @FXML
	    private Label postalCodeLabel;
	    @FXML
	    private Label cityLabel;
	    @FXML
	    private Label birthdayLabel;

	    private ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();

	    /**
	     * The constructor.
	     * The constructor is called before the initialize() method.
	     */
	    public LogController() {
	    }

	    /**
	     * Initializes the controller class. This method is automatically called
	     * after the fxml file has been loaded.
	     */
	    @FXML
	    private void initialize() {
	    // Initialize the person table
	    	messageColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("message"));
	        arg0Column.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("arg0"));
	        arg1Column.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("arg1"));
	        arg2Column.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("arg2"));

			logEntries.add(new LogEntry(new OSCMessage("LogController initialized", new Object[] {})));
	        logEntryTable.setItems(logEntries);
	    }

	    /**
	     * Is called by the main application to give a reference back to itself.
	     * 
	     * @param mainApp
	     */
	    public void log(OSCMessage m) {
	        
	    	logEntries.add(new LogEntry(m));
	    }
}
