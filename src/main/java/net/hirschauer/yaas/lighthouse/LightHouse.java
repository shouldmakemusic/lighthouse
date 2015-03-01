package net.hirschauer.yaas.lighthouse;

import java.io.IOException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.hirschauer.yaas.lighthouse.model.OSCMessageFromTask;
import net.hirschauer.yaas.lighthouse.model.SensorValue.SensorType;
import net.hirschauer.yaas.lighthouse.osccontroller.YaasController;
import net.hirschauer.yaas.lighthouse.util.PropertiesHandler;
import net.hirschauer.yaas.lighthouse.util.TextAreaAppender;
import net.hirschauer.yaas.lighthouse.visual.ConfigurationController;
import net.hirschauer.yaas.lighthouse.visual.LogController;
import net.hirschauer.yaas.lighthouse.visual.MidiLogController;
import net.hirschauer.yaas.lighthouse.visual.SensorController;
import net.hirschauer.yaas.lighthouse.visual.YaasLogController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightHouse extends Application {

	private static final Logger logger = LoggerFactory
			.getLogger(LightHouse.class);
	
	private static LightHouseOSCServer oscServer;
	private static LightHouseService service;
	private Thread oscThread;
	private Thread serviceThread;
	
	private Stage primaryStage;
    private AnchorPane rootLayout;
    
	private SensorController sensorController;
	private ConfigurationController configurationController;
    
	@FXML
	TabPane tabPane;
    @FXML
    AnchorPane logTablePane, logTableWii, logTableAndroid, yaasLogTablePane;
    @FXML
    HBox topBox;
    @FXML
    AnchorPane midiLogTablePane, configurationTablePane;
    @FXML
    MenuBar menuBar;
    @FXML
    AnchorPane paneAndroidChart, paneWiiChart;
    @FXML
    TextArea loggingView;
    @FXML
    MenuItem menuClose, menuYaasSettings;
    
    private YaasLogController yaasLogController;
    private MidiLogController midiLogController;
    
    private PropertiesHandler properties;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

	public LightHouse() {

		logger.debug("LightHouse created");
	}	

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		logger.debug("start");
		Image icon = new Image(getClass().getResourceAsStream( "/music94.png" ));
		primaryStage.getIcons().add(icon); 
		
        // Load the root layout from the fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LightHouseSurface.fxml"));
        loader.setController(this);
        rootLayout = (AnchorPane) loader.load();
        setupMenu();
		TextAreaAppender.setTextArea(loggingView);

		properties = new PropertiesHandler();

		if (oscServer == null) {
			oscServer = LightHouseOSCServer.getInstance();
			oscThread = new Thread(oscServer);
			oscThread.start();
		}

		if (service == null) {
			service = new LightHouseService();
			serviceThread = new Thread(service);
			serviceThread.start();
		}
		
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("LightHouse - Service Discovery and OSC Client/Server");
        
        
        Scene scene = new Scene(rootLayout);
        scene.getStylesheets().add("/view/stylesheet.css");
        primaryStage.setScene(scene);
        primaryStage.show();               
                
        showBarCharts();
        showOSCLogTable();
        showYaasLogTable();
        showMidiLogTable();
        showConfigurationEditor();        
        
        properties.load(yaasLogController, configurationController, midiLogController);
        
	}
	
	private void setupMenu() {
		menuBar.setUseSystemMenuBar(true);
		menuClose.setOnAction(new EventHandler<ActionEvent>() {			
			@Override
			public void handle(ActionEvent event) {
				primaryStage.close();
			}
		});
	}

	private void showYaasLogTable() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/YaasLogTable.fxml"));
		AnchorPane childLogTable = (AnchorPane) loader.load();

        // Give the controller access to the main app
		yaasLogController = loader.getController();
		yaasLogController.setOscServer(oscServer);
        
        yaasLogTablePane.getChildren().add(childLogTable);
	}
	
	private void showConfigurationEditor() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ConfigurationEditor.fxml"));
		AnchorPane childLogTable = (AnchorPane) loader.load();

        // Give the controller access to the main app
		configurationController = loader.getController();
        
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {

			@Override
			public void changed(
					ObservableValue<? extends Tab> observable,
							Tab oldValue, Tab newValue) {

				if (newValue.getText() != null && newValue.getText().equals("YAAS config")) {
					logger.debug("yaas config selected");
					YaasController.getInstance().fetchAvailableCommandsFromYaas();
				}				
			}
		});
        configurationTablePane.getChildren().add(childLogTable);
	}
	
	private void showMidiLogTable() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MidiLogTable.fxml"));
		AnchorPane childLogTable = (AnchorPane) loader.load();

        // Give the controller access to the main app
		midiLogController = loader.getController();
        
        midiLogTablePane.getChildren().add(childLogTable);
	}

	private void showOSCLogTable() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OSCLogTable.fxml"));
        LogController controller = new LogController(OSCMessageFromTask.TYPE_OSC);
        loader.setController(controller);
		AnchorPane childLogTable = (AnchorPane) loader.load();

        // Give the controller access to the main app
        controller.setOscServer(oscServer);
        logTablePane.getChildren().add(childLogTable);

        loader = new FXMLLoader(getClass().getResource("/view/OSCLogTable.fxml"));
        controller = new LogController(OSCMessageFromTask.TYPE_ANDROID);
        loader.setController(controller);
        childLogTable = (AnchorPane) loader.load();

        controller.setOscServer(oscServer);
        logTableAndroid.getChildren().add(childLogTable);

        loader = new FXMLLoader(getClass().getResource("/view/OSCLogTable.fxml"));
        controller = new LogController(OSCMessageFromTask.TYPE_WII);
        loader.setController(controller);
        childLogTable = (AnchorPane) loader.load();

        controller.setOscServer(oscServer);
        logTableWii.getChildren().add(childLogTable);
}
	
    public void showBarCharts() throws IOException {
    	
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SensorBarChart.fxml"));	    
		AnchorPane page = (AnchorPane) loader.load();				
		sensorController = loader.getController();	 
		paneAndroidChart.getChildren().add(page);
		sensorController.listenTo(oscServer, SensorType.ANDROID);
		topBox.getChildren().get(0).getStyleClass().add("series-android");
		
		loader = new FXMLLoader(getClass().getResource("/view/SensorBarChart.fxml"));	   
		page = (AnchorPane) loader.load();			
		sensorController = loader.getController();	 
		paneWiiChart.getChildren().add(page);	
		sensorController.listenTo(oscServer, SensorType.WII);
		topBox.getChildren().get(1).getStyleClass().add("series-wii");
    }
	
	@Override
	public void stop() throws Exception {		
		super.stop();

		properties.store(yaasLogController, configurationController, midiLogController);
		LightHouseMidi.getInstance().close();
		
		if (oscServer != null) {
			oscServer.stop();
			oscServer = null;
		}
		try {
			service.cancel();
			service.finalize();			
			serviceThread = null;
		} catch (Throwable e) {
			logger.error("service not stopped correctly", e);
		}
		oscThread = null;
		logger.debug("stopped");
		System.exit(0);
	}
}
