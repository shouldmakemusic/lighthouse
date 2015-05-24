package net.hirschauer.yaas.lighthouse.visual.components;

import javafx.fxml.FXML;
import net.hirschauer.yaas.lighthouse.model.ConfigCommand;
import net.hirschauer.yaas.lighthouse.model.ConfigMidi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidiLineEditor extends LineEditor {
	
	private static final Logger logger = LoggerFactory
			.getLogger(LineEditor.class);
	
	private MidiReceiver midiInputController;

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
