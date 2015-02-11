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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.LogEntry;
import net.hirschauer.yaas.lighthouse.model.OSCMessageFromTask;
import net.hirschauer.yaas.lighthouse.util.StoredProperty;

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
	@FXML
	private Button btnFile;
	@FXML
	private TextInputControl txtYaasPort;
	@FXML
	private TextInputControl txtYaasErrorLog;

	private static final Logger logger = LoggerFactory.getLogger(YaasLogController.class);
	private ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
	
	@StoredProperty
	private String fileName;
	
	@StoredProperty
	private String port;
	
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
		timeColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("timeString"));
		levelColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("level"));
		messageColumn.setCellValueFactory(new PropertyValueFactory<LogEntry, String>("message"));
		messageColumn.setCellFactory
		 (
		   column ->
		    {
		      return new TableCell<LogEntry, String>()
		       {
		         @Override
		         protected void updateItem(String item, boolean empty)
		          {
		             super.updateItem(item, empty);
		             if (!empty) {
			             setText( item );
			             setTooltip(new Tooltip(item));
		             } else {
		            	 setText( null );
		            	 setTooltip(null);
		             }
		          }
		       };
		    });

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
		
		txtYaasErrorLog.setEditable(false);
		btnFile.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();	
				fileChooser.getExtensionFilters().addAll(
		                new FileChooser.ExtensionFilter("Text", "stderr.txt")
		            );
				Window window = ((Node)event.getTarget()).getScene().getWindow();
				File file = fileChooser.showOpenDialog(window);
                if (file != null) {
                	setFileName(file.getAbsolutePath());
                }
			}
		});
		
		txtYaasPort.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				port = newValue;
				// TODO: initialize yaas port new
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
		logEntry.setMessage(m.getArgList().get(0));
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

			logger.debug("started file observer for " + getFileName());
			File errorLog = new File(getFileName());
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
				
				if (newValue.startsWith(OSCMessageFromTask.TYPE_YAAS)) {

					OSCMessageFromTask m = new OSCMessageFromTask(newValue);	
//					logger.debug("name:" + m.getName());
//					logger.debug("arg0:" + m.getArgList().get(0));
					log(m);
					if (m.getName().equals("/yaas/config/errorfile")) {
						setFileName(m.getFirstArg());
						debug("Error logging is enabled");						
					}
					if (m.getName().equals("/yaas/config/port")) {
						txtYaasPort.setText(m.getFirstArg());		
						setPort(m.getFirstArg());
					}
				}
			}
		});
	}
	
	@Override
	public void finalize() throws Exception{
		
		try {
			super.finalize();
		} catch (Throwable e) {
			throw new Exception(e);
		}
		errorLogChangeListener.cancel(true);
		logger.debug("finalize");
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
		this.txtYaasPort.setText(port);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		
		if (!fileName.equals(this.fileName)) {
			this.fileName = fileName;
			this.txtYaasErrorLog.setText(fileName);
			YaasLogController.getInstance().setErrorFile(fileName);
		}
	}
}
