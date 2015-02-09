package net.hirschauer.yaas.lighthouse.visual;

import java.util.HashMap;

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
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import net.hirschauer.yaas.lighthouse.LightHouseMidi;
import net.hirschauer.yaas.lighthouse.model.MidiLogEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidiLogController {

	private LightHouseMidi midi;
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

    private ObservableList<MidiLogEntry> logEntries = FXCollections.observableArrayList();
	private HashMap<String, Info> midiInfos;

	public void setMidi(LightHouseMidi midi) {
		this.midi = midi;

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
				if (newValue != null) {

					boolean found = false;
					Exception exc = null;
						for (Info info : MidiSystem.getMidiDeviceInfo()) {
							logger.debug("passing through " + info.getName());
							if (info != null) {
								try {
									if (newValue.equals(info.getName())) {
										logger.debug("Found device info");
										MidiDevice device = midi.getMidiDevice(info);
										Transmitter trans = device.getTransmitter();
										trans.setReceiver(new MidiInputReceiver(device.getDeviceInfo().toString()));
										device.open();
										found = true;
									}
								} catch (MidiUnavailableException e) {
									exc = e;
								}
								if (oldValue != null && oldValue.equals(info.getName())) {
									logger.debug("Found device info");
									MidiDevice device;
									try {
										device = midi.getMidiDevice(info);
										device.close();
									} catch (MidiUnavailableException e) {
										logger.error("Could not get midi device " + oldValue + " for closing", exc);
									}
									
								}
							}
						}
					if (!found) {
						logger.error("Could not get midi device " + newValue, exc);
						midiInputCombobox.setValue(oldValue);
					}
				}
			}
			
		});
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

		tableMidi.setItems(logEntries);
		
		btnClear.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				logEntries.clear();
			}
		});
    }
    
    public class MidiInputReceiver implements Receiver {
    	
        public String name;
        public MidiInputReceiver(String name) {
        	logger.debug("created midi input receiver " + name);
            this.name = name;
        }
		@Override
		public void send(MidiMessage message, long timeStamp) {
			
			logger.debug("Received midi with state " + message.getStatus());
			MidiLogEntry entry = new MidiLogEntry();
			
			byte[] messageBytes = message.getMessage();
			//int data0 = (int)(messageBytes[0] & 0xFF);
			// same as status
			int data1 = (int)(messageBytes[1] & 0xFF);
			int data2 = (int)(messageBytes[2] & 0xFF);
			
			if (message.getLength() == 3) {
				entry.setChannel("1");
			} else {
				logger.info("Received midi message different: " + message.getMessage());
				return;
			}
			entry.setData1("" + data1);
			entry.setData2("" + data2);

			int status = message.getStatus();
			entry.setStatus("" + status);
			switch (status) {
				case 144:
					entry.setEventType("Note on");
					break;
				case 128:
					entry.setEventType("Note off");
					break;
				case 176:
					entry.setEventType("Control change");
					entry.setDescription("CC#" + data1);
					break;
			}
			
			// TODO: add note name
			
			logEntries.add(entry);
			
		}
		@Override
		public void close() {
		}
        
    }
}
