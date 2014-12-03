package net.hirschauer.yaas.lighthouse;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightHouse extends Application {

	private static final Logger logger = LoggerFactory
			.getLogger(LightHouse.class);
	
	private static LightHouseOSCServer oscServer;
	private static final LightHouseService service = new LightHouseService();

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
		
		primaryStage.setTitle("Hello World!");
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
 
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
		
	}
	
	@Override
	public void stop() throws Exception {		
		super.stop();
		this.oscServer.stop();
	}
}
