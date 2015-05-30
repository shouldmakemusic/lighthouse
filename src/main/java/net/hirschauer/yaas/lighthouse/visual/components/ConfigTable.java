package net.hirschauer.yaas.lighthouse.visual.components;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import net.hirschauer.yaas.lighthouse.model.ConfigCommand;
import net.hirschauer.yaas.lighthouse.visual.Configurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigTable {

	private static final Logger logger = LoggerFactory.getLogger(ConfigTable.class);
		
   	@FXML
   	protected TableView<ConfigCommand> configTable;
   	
    @FXML
    protected TableColumn<ConfigCommand, String> colConfigCommand, colConfigValue, colController, 
    	colCommand, colMidiFollowSignal, colValue1, colValue2, colValue3;
    
    @FXML
    protected BorderPane borderPane;
    
    protected Configurator configurator;

    public ConfigTable() {}
    
    public void setConfigurator(Configurator configurator) {
    	this.configurator = configurator;
		configTable.setItems(configurator.getConfigEntries());
    }
    
	public static ConfigTable show(AnchorPane configTablePane) throws IOException {
		
		FXMLLoader loader = new FXMLLoader(ConfigTable.class.getResource("/view/configurators/ConfigTable.fxml"));
		AnchorPane configTable = (AnchorPane) loader.load();
		configTablePane.getChildren().add(configTable);
		
		ConfigTable configController = loader.getController();		
		return configController;
	}

    @FXML
    public void initialize() {
		logger.debug("init");
		
		setCellFactories();

		configTable.setEditable(false);
		configTable.setRowFactory( tv -> {
		    TableRow<ConfigCommand> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
		            logger.debug("double click on row");
		            showEditRow(row.getIndex());
		        }
		    });
		    return row ;
		});
				
		initContextMenu();
	}
    
	private void initContextMenu() {
		MenuItem mnuDel = new MenuItem("Delete row");
		mnuDel.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent t) {
		        logger.debug("Delete row");
                ConfigCommand p = configTable.getSelectionModel().getSelectedItem();
                if (p != null) {
                    configurator.getConfigEntries().remove(p);
                }
		    }
		});
		MenuItem mnuEdit = new MenuItem("Edit");
		mnuEdit.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent t) {
		        logger.debug("Edit row");
                showEditRow(configTable.getSelectionModel().getSelectedIndex());
		    }
		});
		configTable.setContextMenu(new ContextMenu(mnuEdit, mnuDel));
	}
    
	public void showEditRow(int index) {
		
		ConfigCommand configEntry = configurator.getConfigEntries().get(index);
        if (configEntry != null) {
        	AnchorPane root = new AnchorPane();
			Stage editStage = new Stage();				
			Scene editScene = new Scene(root);
			
            RowEditor editor = configurator.getLineEditor(root, configEntry);
            editor.setStage(editStage);

            editStage.setScene(editScene);
			editStage.setAlwaysOnTop(true);
			editStage.showAndWait();
						
			configurator.getConfigEntries().remove(index);
			
			ConfigCommand entry = (ConfigCommand) editor.getEntry();
			if (entry != null) {
				configurator.getConfigEntries().add(index, entry);
			}
        }
	}

	private void setCellFactories() {
		colConfigCommand.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("configCommand"));
		colConfigValue.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("configValue"));
		colMidiFollowSignal.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("midiFollowSignal"));
		colCommand.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("command"));
		colController.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("controller"));
		colValue1.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("value1"));
		colValue2.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("value2"));
		colValue3.setCellValueFactory(new PropertyValueFactory<ConfigCommand, String>("value3"));
	}

	public void refresh() {
		configTable.setVisible(false);
		configTable.setVisible(true);
	}
}
