package net.hirschauer.yaas.lighthouse;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import net.hirschauer.yaas.lighthouse.model.MidiLogEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightHouseMidiReceiver implements Receiver {

	private static final Logger logger = LoggerFactory
			.getLogger(LightHouseMidiReceiver.class);
	public MidiDevice device;
	private static LightHouseMidiReceiver instance;

	public static LightHouseMidiReceiver getInstance() {
		if (instance == null) {
			instance = new LightHouseMidiReceiver();
		}
		return instance;
	}
	
	public LightHouseMidiReceiver setDevice(MidiDevice device) throws MidiUnavailableException {
		logger.debug("set midi device: " + device.getDeviceInfo().toString());
		this.device = device;
		device.open();
		return this;
	}
	
	public boolean hasDevice() {
		return device != null;
	}
	
	private LightHouseMidiReceiver() {
	}
	
	public ObservableList<MidiLogEntry> logEntries = FXCollections.observableArrayList();

	@Override
	public void send(MidiMessage message, long timeStamp) {

//		logger.debug("Received midi with state " + message.getStatus());
		MidiLogEntry entry = new MidiLogEntry();

		byte[] messageBytes = message.getMessage();
		// int data0 = (int)(messageBytes[0] & 0xFF);
		// same as status
		int data1 = (int) (messageBytes[1] & 0xFF);
		int data2 = (int) (messageBytes[2] & 0xFF);

		if (message.getLength() == 3) {
			entry.setChannel("1");
		} else {
			logger.info("Received midi message different: "
					+ message.getMessage());
			return;
		}
		entry.setData1("" + data1);
		entry.setData2("" + data2);

		int status = message.getStatus();
		entry.setStatus(status);

		// TODO: add note name

		logEntries.add(entry);
	}

	@Override
	public void close() {
		device.close();
	}

}
