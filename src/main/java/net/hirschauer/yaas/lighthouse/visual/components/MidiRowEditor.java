package net.hirschauer.yaas.lighthouse.visual.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import net.hirschauer.yaas.lighthouse.model.ConfigCommand;
import net.hirschauer.yaas.lighthouse.model.ConfigMidi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidiRowEditor extends RowEditor {
	
	private static final Logger logger = LoggerFactory
			.getLogger(RowEditor.class);
	
	private MidiReceiver midiInputController;

	public static RowEditor show(AnchorPane parent, ConfigCommand entry) {

		FXMLLoader loader = new FXMLLoader(
				RowEditor.class
						.getResource("/view/components/MidiRowEditor.fxml"));
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
		
		midiInputController = MidiReceiver.show(paneInput);
		midiInputController.setStage(stage);
	}
	
	@Override
	protected ConfigCommand getConfigEntry() {
		ConfigMidi entry = new ConfigMidi();
		entry.init(midiInputController.getMidiInput());
		return entry;
	}

	@Override
	protected void setConfigEntry(ConfigCommand entry) {
		midiInputController.setMidiInput((ConfigMidi)entry);
		
	}

	@Override
	protected String verify() {
		return midiInputController.verify();
	}
}
