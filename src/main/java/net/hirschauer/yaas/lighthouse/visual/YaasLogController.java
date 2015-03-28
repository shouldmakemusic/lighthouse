package net.hirschauer.yaas.lighthouse.visual;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import javafx.application.Platform;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.LogEntry;
import net.hirschauer.yaas.lighthouse.model.OSCMessageFromTask;
import net.hirschauer.yaas.lighthouse.model.YaasConfiguration;
import net.hirschauer.yaas.lighthouse.osccontroller.YaasController;
import net.hirschauer.yaas.lighthouse.util.IStorable;
import net.hirschauer.yaas.lighthouse.util.StoredProperty;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCMessage;

public class YaasLogController extends VisualController implements IStorable {

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
	private TextInputControl txtYaasLocation;

	private static final Logger logger = LoggerFactory.getLogger(YaasLogController.class);
	
	@StoredProperty
	private String fileName;
	
	@StoredProperty
	private String port;
	
	private ObservableList<LogEntry> yaasLogEntries = FXCollections.observableArrayList();
	private static YaasLogController instance;
	
	public YaasLogController() {
		logger.debug("initialize yaas log controller");
		instance = this;
	}
	
	public static YaasLogController getInstance() {
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

		FilteredList<LogEntry> leveledData = new FilteredList<>(yaasLogEntries,
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
//							logger.debug("Filtering " + lowerCaseFilter
//									+ " for message \"" + entry.getMessage()
//									+ "\"");

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
				yaasLogEntries.clear();
			}
		});
		
		txtYaasLocation.setEditable(false);
		btnFile.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				DirectoryChooser dirChooser = new DirectoryChooser();

				Window window = ((Node)event.getTarget()).getScene().getWindow();
				File file = dirChooser.showDialog(window);
                if (file != null) {
                	setFileName(file.getAbsolutePath());
                }
			}
		});
		YaasController.getInstance().yaasConfigurationProperty.addListener(new ChangeListener<YaasConfiguration>() {

			@Override
			public void changed(
					ObservableValue<? extends YaasConfiguration> observable,
					YaasConfiguration oldValue, YaasConfiguration newValue) {
				
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						setFileName(newValue.getYaasLocation());
						setErrorFile(newValue.getYaasErrorLogFile());
					}
				});				
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

	public static void log(OSCMessage m) {

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
		if (YaasLogController.getInstance().yaasLogEntries != null) {
			YaasLogController.getInstance().yaasLogEntries.add(logEntry);
		}
	}

	public static void log(OSCMessageFromTask m) {

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
		YaasLogController.getInstance().yaasLogEntries.add(logEntry);
	}

	public static void verbose(String m) {
		log(VERBOSE, m);
	}

	public static void log(String m) {

		log(INFO, m);
	}

	public static void debug(String m) {

		log(DEBUG, m);
	}

	public static void error(String m) {

		log(ERROR, m);
	}

	public static void log(String level, String m) {

		LogEntry logEntry = new LogEntry();
		logEntry.setLevel(level);
		logEntry.setMessage(m);
		YaasLogController.getInstance().yaasLogEntries.add(logEntry);
	}

	public void setErrorFile(String fileName) {
		logger.debug("setting error file to watch: " + fileName);
				
		// TODO: only one errorchange listener should be possible
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

			String errorFile = YaasController.getInstance().getYaasConfiguration().getYaasErrorLogFile();
			logger.debug("started file observer for " + errorFile);
			
			File errorLog = new File(errorFile);
			if (!errorLog.exists()) {
				logger.warn("Error log is not available");
				updateMessage("Cancelled");
				return null;
			}
			long lastmodified = errorLog.lastModified();
			//logger.debug("lastmodified " + lastmodified);
			List<String> lines = IOUtils.readLines(new FileInputStream(errorLog), "UTF-8");
			int lineCount = lines.size();
			logger.debug("starting line count " + lineCount);

			while (true) {
				// Block the thread for a short time, but be sure
				// to check the InterruptedException for cancellation
				try {					
					
					if (lastmodified != errorLog.lastModified()) {
							
						lines = IOUtils.readLines(new FileInputStream(errorLog), "UTF-8");
						logger.debug("starting line count " + lineCount);
						logger.debug("found lines " + lines.size());
						StringBuffer sb = new StringBuffer();
						for (int i = lineCount; i < lines.size(); i++) {
							sb.append(lines.get(i));
							sb.append("\n");
						}
						updateMessage(sb.toString());
						lastmodified = errorLog.lastModified();
						lineCount = lines.size();
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
					if (m.getName().equals("/yaas/config/port")) {
						txtYaasPort.setText(m.getFirstArg());		
						setPort(m.getFirstArg());
					}
				}
			}
		});
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
			this.txtYaasLocation.setText(fileName);			
		}
	}

	@Override
	public void store(Properties values) {
		if (port != null) {
			values.put(getClass().getName() + "|" + "port", port);
		}
		if (fileName != null) {
			values.put(getClass().getName() + "|" + "fileName", fileName);
		}
	}

	@Override
	public void load(Properties values) {
		
		String className = getClass().getName();
		
		for (Object keyObj : values.keySet()) {
			
			String key = keyObj.toString();
			
			if (key.startsWith(className)) {
				String[] entry = key.split("\\|");
				if (entry.length == 2) {
					String name = entry[1];
					if (name.equals("port")) {
						
						logger.debug("restoring port: " + values.getProperty(key));
						setPort(values.getProperty(key));	
						
					} else if (name.equals("fileName")) {
						String fileName = values.getProperty(key);
						logger.debug("restoring yaas location: " + fileName);
						YaasController.getInstance().yaasConfigurationProperty.set(new YaasConfiguration(fileName));
					}
				}
			}
		}
	}

	@Override
	protected String getMenuId() {
		
		return "yaasController";
	}
}
