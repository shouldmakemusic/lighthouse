package net.hirschauer.yaas.lighthouse;

import java.io.IOException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.hirschauer.yaas.lighthouse.model.LogEntry;
import net.hirschauer.yaas.lighthouse.visual.LogController;
import net.hirschauer.yaas.lighthouse.visual.SensorController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCMessage;

public class LightHouse extends Application {

	private static final Logger logger = LoggerFactory
			.getLogger(LightHouse.class);
	
	private static LightHouseOSCServer oscServer;
	private static final LightHouseService service = new LightHouseService();
	
	private Stage primaryStage;
    private AnchorPane rootLayout;
    
    @FXML
    AnchorPane logTablePane;
    @FXML
    AnchorPane controlPane;

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

		if (oscServer == null) {
			oscServer = new LightHouseOSCServer();
		}
		
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("LightHouse - Service Discovery and OSC Client/Server");
        
        // Load the root layout from the fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LightHouseSurface.fxml"));
        loader.setController(this);
        rootLayout = (AnchorPane) loader.load();
        
        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        primaryStage.show();               
        
        showBarChart();
        showLogTable();
	}
	
	private void showLogTable() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/LogTable.fxml"));
		AnchorPane childLogTable = (AnchorPane) loader.load();

        // Give the controller access to the main app
        LogController controller = loader.getController();
        oscServer.setLogController(controller);
        
        logTablePane.getChildren().add(childLogTable);
	}
	
    public void showBarChart() throws IOException {
    	
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/SensorBarChart.fxml"));	    
		AnchorPane page = (AnchorPane) loader.load();				
		
		SensorController sc = loader.getController();
		oscServer.setSensorController(sc);
	    
		controlPane.getChildren().add(page);
    }
	
	@Override
	public void stop() throws Exception {		
		super.stop();
		this.oscServer.stop();
	}
}
