package net.hirschauer.yaas.lighthouse.visual;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.LogEntry;
import net.hirschauer.yaas.lighthouse.model.OSCMessageFromTask;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCMessage;

public class YaasLogController {

	private static final String VERBOSE = "verbose";
	private static final String DEBUG = "debug";
	private static final String INFO = "info";
	private static final String ERROR = "error";

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
	private String fileName;
	
	private static YaasLogController instance;

	public YaasLogController() {
		instance = this;
		logger.debug("initialize yaas log controller");
	}
	
	public static YaasLogController getInstance() {
		if (instance == null) {
			instance = new YaasLogController();
		}
		return instance;
	}

	@FXML
	private void initialize() {
		timeColumn
				.setCellValueFactory(new PropertyValueFactory<LogEntry, String>(
						"timeString"));
		levelColumn
				.setCellValueFactory(new PropertyValueFactory<LogEntry, String>(
						"level"));
		messageColumn
				.setCellValueFactory(new PropertyValueFactory<LogEntry, String>(
						"message"));

		log("LogController initialized");

		FilteredList<LogEntry> leveledData = new FilteredList<>(logEntries,
				p -> true);
		levelCombobox.valueProperty().addListener(new ChangeListener<String>() {

			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {

				// debug("Loglevel changed to " + newValue);
				leveledData.setPredicate(entry -> {
					if (newValue.toLowerCase().equals(DEBUG)) {
						if (entry.getLevel().equals(VERBOSE)) {
							return false;
						}
					}
					if (newValue.toLowerCase().equals(INFO)) {
						if (entry.getLevel().equals(DEBUG)
								|| entry.getLevel().equals(VERBOSE)) {
							return false;
						}
					}
					if (newValue.toLowerCase().equals(ERROR)) {
						if (!entry.getLevel().equals(ERROR)) {
							return false;
						}
					}
					return true;
				});
			}
		});
		levelCombobox.valueProperty().set(DEBUG);

		FilteredList<LogEntry> filteredData = new FilteredList<>(leveledData,
				p -> true);
		inputFilter.textProperty().addListener(
				(observable, oldValue, newValue) -> {
					filteredData.setPredicate(entry -> {
						// If filter text is empty, display all persons.
							if (newValue == null || newValue.isEmpty()) {
								return true;
							}

							// Compare first name and last name of every person
							// with filter text.
							String lowerCaseFilter = newValue.toLowerCase();
							logger.debug("Filtering " + lowerCaseFilter
									+ " for message \"" + entry.getMessage()
									+ "\"");

							if (entry.getMessage().toLowerCase()
									.indexOf(lowerCaseFilter) != -1) {
								return true; // Filter matches first name.
							} else if (entry.getMessage().toLowerCase()
									.indexOf(lowerCaseFilter) != -1) {
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
		logEntry.setLevel(DEBUG);
		if (m.getName().endsWith(VERBOSE)) {
			logEntry.setLevel(VERBOSE);
		} else if (m.getName().endsWith(INFO)) {
			logEntry.setLevel(INFO);
		} else if (m.getName().endsWith(ERROR)) {
			logEntry.setLevel(ERROR);
		}
		logEntry.setMessage(m.getArg(0).toString());
		logEntries.add(logEntry);
	}

	public void log(OSCMessageFromTask m) {

		LogEntry logEntry = new LogEntry();
		logEntry.setLevel(DEBUG);
		if (m.getName().endsWith(VERBOSE)) {
			logEntry.setLevel(VERBOSE);
		} else if (m.getName().endsWith(INFO)) {
			logEntry.setLevel(INFO);
		} else if (m.getName().endsWith(ERROR)) {
			logEntry.setLevel(ERROR);
		}
		logEntry.setMessage(m.getArgs());
		logEntries.add(logEntry);
	}

	public void verbose(String m) {
		log(VERBOSE, m);
	}

	public void log(String m) {

		log(INFO, m);
	}

	public void debug(String m) {

		log(DEBUG, m);
	}

	public void error(String m) {

		log(ERROR, m);
	}

	public void log(String level, String m) {

		LogEntry logEntry = new LogEntry();
		logEntry.setLevel(level);
		logEntry.setMessage(m);
		logEntries.add(logEntry);
	}

	public void setErrorFile(String fileName) {
		logger.debug("setting error file to watch: " + fileName);
		this.fileName = fileName;
		new Thread(errorLogChangeListener).start();
		errorLogChangeListener.messageProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				error(newValue);
			}
		});
	}

	Task<Void> errorLogChangeListener = new Task<Void>() {
		@Override
		protected Void call() throws Exception {

			logger.debug("started file observer for " + fileName);
			File errorLog = new File(fileName);
			if (!errorLog.exists()) {
				logger.warn("Error log is not available");
				updateMessage("Cancelled");
				return null;
			}
			long lastmodified = errorLog.lastModified();
			//logger.debug("lastmodified " + lastmodified);
			List<String> lines = IOUtils.readLines(new FileInputStream(errorLog), "UTF-8");
			int lineCount = lines.size();

			while (true) {
				// Block the thread for a short time, but be sure
				// to check the InterruptedException for cancellation
				try {					
					
					if (lastmodified != errorLog.lastModified()) {
						//logger.debug("changed");
						lines = IOUtils.readLines(new FileInputStream(errorLog), "UTF-8");
						for (int i = lineCount; i < lines.size(); i++) {
							updateMessage(lines.get(i));
						}
						lineCount = lines.size();
						lastmodified = errorLog.lastModified();
					}
					
					Thread.sleep(2000);
	
				} catch (InterruptedException interrupted) {
					if (isCancelled()) {
						updateMessage("Cancelled");
						break;
					}
				}
			}
			return null;
		}
	};

	public void setOscServer(LightHouseOSCServer oscServer) {
		oscServer.messageProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				
				if (newValue.startsWith("/yaas/log")) {

					OSCMessageFromTask m = new OSCMessageFromTask(newValue);	
					if (m.getName().equals("/yaas/log/errorfile")) {
						YaasLogController.getInstance().setErrorFile((String)m.getArgs());
					}
					log(m);
				}
			}
		});
	}
}
