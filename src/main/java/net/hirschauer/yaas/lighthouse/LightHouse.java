package net.hirschauer.yaas.lighthouse;

import java.io.IOException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.hirschauer.yaas.lighthouse.model.SensorValue.SensorType;
import net.hirschauer.yaas.lighthouse.visual.ConfigurationController;
import net.hirschauer.yaas.lighthouse.visual.LogController;
import net.hirschauer.yaas.lighthouse.visual.MidiLogController;
import net.hirschauer.yaas.lighthouse.visual.SensorController;
import net.hirschauer.yaas.lighthouse.visual.YaasLogController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCMessage;

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
	private LightHouseMidi midi;
    
	@FXML
	TabPane tabPane;
    @FXML
    AnchorPane logTablePane;
    @FXML
    HBox topBox;
    @FXML
    AnchorPane yaasLogTablePane;
    @FXML
    AnchorPane midiLogTablePane;
    @FXML
    AnchorPane configurationTablePane;

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
		
		midi = new LightHouseMidi();

		if (oscServer == null) {
			oscServer = new LightHouseOSCServer(midi);
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
        
        // Load the root layout from the fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LightHouseSurface.fxml"));
        loader.setController(this);
        rootLayout = (AnchorPane) loader.load();
        
        Scene scene = new Scene(rootLayout);
        scene.getStylesheets().add("/view/stylesheet.css");
        primaryStage.setScene(scene);
        primaryStage.show();               
        
        showBarCharts();
        showOSCLogTable();
        showYaasLogTable();
        showMidiLogTable();
        showConfigurationEditor();
	}
	
	private void showYaasLogTable() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/YaasLogTable.fxml"));
		AnchorPane childLogTable = (AnchorPane) loader.load();

        // Give the controller access to the main app
        YaasLogController controller = loader.getController();
        controller.setOscServer(oscServer);
        
        yaasLogTablePane.getChildren().add(childLogTable);
	}
	
	private void showConfigurationEditor() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ConfigurationEditor.fxml"));
		AnchorPane childLogTable = (AnchorPane) loader.load();

        // Give the controller access to the main app
        ConfigurationController controller = loader.getController();
        
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {

			@Override
			public void changed(
					ObservableValue<? extends Tab> observable,
							Tab oldValue, Tab newValue) {

				if (newValue.getText() != null && newValue.getText().equals("YAAS Config")) {
					logger.debug("yaas config selected");
					if (LightHouseOSCServer.yaasCommands.size() == 0) {
						oscServer.fetchAvailableCommandsFromYaas();
					}
					controller.updateController(LightHouseOSCServer.yaasCommands);
				}				
			}
		});
        configurationTablePane.getChildren().add(childLogTable);
	}
	
	private void showMidiLogTable() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MidiLogTable.fxml"));
		AnchorPane childLogTable = (AnchorPane) loader.load();

        // Give the controller access to the main app
        MidiLogController controller = loader.getController();
        controller.setMidi(midi);
        
        midiLogTablePane.getChildren().add(childLogTable);
	}

	private void showOSCLogTable() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/OSCLogTable.fxml"));
		AnchorPane childLogTable = (AnchorPane) loader.load();

        // Give the controller access to the main app
        LogController controller = loader.getController();
        controller.setOscServer(oscServer);
        
        logTablePane.getChildren().add(childLogTable);
	}
	
    public void showBarCharts() throws IOException {
    	
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SensorBarChart.fxml"));	    
		AnchorPane page = (AnchorPane) loader.load();				
		sensorController = loader.getController();	 
		topBox.getChildren().add(page);
		sensorController.listenTo(oscServer, SensorType.ANDROID);
		topBox.getChildren().get(0).getStyleClass().add("series-android");
		
		loader = new FXMLLoader(getClass().getResource("/view/SensorBarChart.fxml"));	   
		page = (AnchorPane) loader.load();			
		sensorController = loader.getController();	 
		topBox.getChildren().add(page);	
		sensorController.listenTo(oscServer, SensorType.WII);
		topBox.getChildren().get(1).getStyleClass().add("series-wii");
    }
	
	@Override
	public void stop() throws Exception {		
		super.stop();
		if (oscServer != null) {
			oscServer.stop();
			oscServer = null;
		}
		try {
			service.finalize();
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		oscThread.stop();
		oscThread = null;
		YaasLogController.getInstance().finalize();
	}
}
