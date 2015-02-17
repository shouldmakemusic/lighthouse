package net.hirschauer.yaas.lighthouse.visual;

import java.util.HashMap;
import java.util.Properties;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;

import net.hirschauer.yaas.lighthouse.LightHouseMidi;
import net.hirschauer.yaas.lighthouse.LightHouseMidiReceiver;
import net.hirschauer.yaas.lighthouse.model.MidiLogEntry;
import net.hirschauer.yaas.lighthouse.util.IStorable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidiLogController implements IStorable {

	Logger logger = LoggerFactory.getLogger(MidiLogController.class);
	
   	@FXML
    private TableView<MidiLogEntry> tableMidi;
    @FXML
    private TableColumn<MidiLogEntry, String> timestampColumn;
    @FXML
    private TableColumn<MidiLogEntry, String> eventTypeColumn;
    @FXML
    private TableColumn<MidiLogEntry, String> channelColumn;
    @FXML
    private TableColumn<MidiLogEntry, String> statusColumn;
    @FXML
    private TableColumn<MidiLogEntry, String> dataColumn1;
    @FXML
    private TableColumn<MidiLogEntry, String> dataColumn2;
    @FXML
    private TableColumn<MidiLogEntry, String> descriptionColumn;
	@FXML
	private ComboBox<String> midiInputCombobox;
	@FXML
	private Button btnClear;

	private HashMap<String, Info> midiInfos;
	private String name;
	private LightHouseMidi midi;

	public void setMidi(LightHouseMidi midi) {
		
		if (midi == null) {
			return;
		}
		this.midi = midi;
		tableMidi.setItems(LightHouseMidiReceiver.getInstance().logEntries);
		
		btnClear.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				LightHouseMidiReceiver.getInstance().logEntries.clear();
			}
		});

		
		ObservableList<String> midiNames = FXCollections.observableArrayList();
		midiInfos = midi.getPossibleMidiInfos();
		for (Info info: midiInfos.values()) {
			midiNames.add(info.getName());
		}
		midiInputCombobox.setItems(midiNames);
		midiInputCombobox.valueProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				
				logger.debug("selected " + newValue);
				name = newValue;
				init(name);
			}
			
		});
	}
	
	private void init(String name) {
		if (name != null) {

			boolean found = false;
			Exception exc = null;
				for (Info info : MidiSystem.getMidiDeviceInfo()) {
					logger.debug("passing through " + info.getName());
					if (info != null) {
						try {
							if (name.equals(info.getName())) {
								logger.debug("Found device info");
								MidiDevice device = midi.getMidiDevice(info);
								Transmitter trans = device.getTransmitter();
								trans.setReceiver(LightHouseMidiReceiver.getInstance().setDevice(device));
								found = true;
							}
						} catch (MidiUnavailableException e) {
							exc = e;
						}
//						if (oldValue != null && oldValue.equals(info.getName())) {
//							logger.debug("Found device info");
//							MidiDevice device;
//							try {
//								device = midi.getMidiDevice(info);
//								device.close();
//							} catch (MidiUnavailableException e) {
//								logger.error("Could not get midi device " + oldValue + " for closing", exc);
//							}
//							
//						}
					}
				}
			if (!found) {
				logger.error("Could not get midi device " + name, exc);
//				midiInputCombobox.setValue(oldValue);
			}
		}
	}

    @FXML
    private void initialize() {
    	timestampColumn.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>("timeString"));
    	eventTypeColumn.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>("eventType"));
    	channelColumn.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>("channel"));
    	statusColumn.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>("status"));
    	dataColumn1.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>("data1"));
    	dataColumn2.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>("data2"));
    	descriptionColumn.setCellValueFactory(new PropertyValueFactory<MidiLogEntry, String>("description"));

    }

	@Override
	public void store(Properties values) {
		if (name != null) {
			values.put(getClass().getName() + "|" + "midi", name);
		}
	}

	@Override
	public void load(Properties values) {
		String name = values.getProperty(getClass().getName() + "|" + "midi");
		if (StringUtils.isNoneEmpty(name)) {
//			init(name);
			midiInputCombobox.setValue(name);
		}
	}
}
