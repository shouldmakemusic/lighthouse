package net.hirschauer.yaas.lighthouse.visual.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import net.hirschauer.yaas.lighthouse.model.ConfigCommand;
import net.hirschauer.yaas.lighthouse.model.ConfigWii;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WiiRowEditor extends RowEditor {
	
	private static final Logger logger = LoggerFactory
			.getLogger(RowEditor.class);
	
	private WiiReceiver wiiReceiver;
	
	public static RowEditor show(AnchorPane parent, ConfigCommand entry) {

		FXMLLoader loader = new FXMLLoader(
				RowEditor.class
						.getResource("/view/components/WiiRowEditor.fxml"));
		try {
			AnchorPane child = (AnchorPane) loader.load();
			parent.getChildren().add(child);
			
			RowEditor controller = loader.getController();
			controller.setEntry(entry);
			return controller;

		} catch (Exception e) {
			logger.error("Could not open controller settings", e);
		}
		return null;
	}
	@FXML
	protected void initialize() {
		super.initialize();
		logger.debug("init");
		
		wiiReceiver = WiiReceiver.show(paneInput);
	}
	
	@Override
	protected ConfigCommand getConfigEntry() {
		ConfigWii entry = new ConfigWii();
		entry.init(wiiReceiver.getWiiCommand());
		return entry;
	}

	@Override
	protected void setConfigEntry(ConfigCommand entry) {
		wiiReceiver.setWiiCommand((ConfigWii)entry);
		
	}

	@Override
	protected String verify() {
		return wiiReceiver.verify();
	}
}
