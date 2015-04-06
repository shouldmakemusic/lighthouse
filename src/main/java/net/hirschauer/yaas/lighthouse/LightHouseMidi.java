package net.hirschauer.yaas.lighthouse;

import java.util.Date;
import java.util.HashMap;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import net.hirschauer.yaas.lighthouse.model.MidiLogEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightHouseMidi implements Receiver {

	private static final Logger logger = LoggerFactory.getLogger(LightHouseMidi.class);
	
	private Receiver transmitter;
	private Transmitter receiver;
	private MidiDevice transmitterDevice;
	private MidiDevice receiverDevice;
	
	private HashMap<String, Info> possibleMidiInfos = new HashMap<String, Info>();
	private static LightHouseMidi instance;
	public ObservableList<MidiLogEntry> logEntries;
	
	private LightHouseMidi() {

		for (Info info : MidiSystem.getMidiDeviceInfo()) {

			logger.debug("Found device " + info.getDescription()+ " (" + info.getName() + ")");
			this.possibleMidiInfos.put(info.getDescription(), info);
			logEntries = FXCollections.observableArrayList();
		}
	}
	
	public static LightHouseMidi getInstance() {
		if (instance == null) {
			logger.debug("create lighthouse midi");
			instance = new LightHouseMidi();
		}
		return instance;
	}

	public boolean hasDevice() {
		return receiver != null;
	}

	public void sendMidiNote(int channel, int note, int value) throws InvalidMidiDataException {
		
		ShortMessage message = new ShortMessage();
		if (value < 0)
			value = 0;
		else if (value > 127)
			value = 127;
		message.setMessage(ShortMessage.NOTE_ON, channel, note, value);
		send(message, new Date().getTime());
		// message.setMessage(ShortMessage.NOTE_OFF, channel, note, value);
		if (transmitter != null) {
			transmitter.send(message, new Date().getTime());
		} else {
			logger.debug("No transmitter for sending");
		}
		
	}

	public void sendMidiNote(int note, int value) throws InvalidMidiDataException {
		
		sendMidiNote(1, note, value);
	}

	@Override
	public void send(MidiMessage message, long timeStamp) {

		// logger.debug("Received midi with state " + message.getStatus());
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

		entry.setDescription(getNoteName(data1));

		logEntries.add(entry);
	}

	private String getNoteName(int value) {
		String notes = "C C#D D#E F F#G G#A A#B ";
		int octave;
		String note;
		// for (int noteNum = 0; noteNum < 128; noteNum++) {
		// octave = noteNum / 12 - 1;
		// note = notes.substring((noteNum % 12) * 2, (noteNum % 12) * 2 + 2);
		// System.out.println("Note number " + noteNum + " is octave "
		// + octave + " and note " + note);
		// }
		octave = value / 12 - 1;
		note = notes.substring((value % 12) * 2, (value % 12) * 2 + 2);
		return octave + " " + note;
	}

	@Override
	public void close() {
		logger.debug("closing midi devices");
		if (transmitter != null) {
			transmitter.close();
			transmitter = null;
		}
		if (receiverDevice != null) {
			receiverDevice.close();
			receiverDevice = null;
		}
		if (transmitterDevice != null) {
			transmitterDevice.close();
			transmitterDevice = null;
		}
	}

	public HashMap<String, Info> getPossibleMidiInfos() {
		return possibleMidiInfos;
	}

	public MidiDevice getMidiDevice(Info info) throws MidiUnavailableException {
		return MidiSystem.getMidiDevice(info);
	}

	public void setDevice(String name) throws MidiUnavailableException {
		logger.debug("set midi device: " + name);
		if (name != null) {

			close();

			for (Info info : MidiSystem.getMidiDeviceInfo()) {
				logger.debug("passing through " + info.getDescription() + " (" + info.getName() + ")");
//				logger.debug("name: " + info.getName());
//				logger.debug("vendor: " + info.getVendor());
//				logger.debug("version: " + info.getVersion());
				if (info != null) {
					if (name.equals(info.getDescription())) {
						logger.debug("Found device info");
						MidiDevice device = getMidiDevice(info);

						if (device.getClass().getSimpleName().equals("MidiOutDevice")) {
							
							transmitter = device.getReceiver();
							device.open();
							transmitterDevice = device;
							logger.debug("transmitter opened");
//							try {
//								sendMidiNote(1, 1, 1);
//							} catch (InvalidMidiDataException e) {
//								logger.error("Couldn't send midi note", e);
//							}
						} else if (device.getClass().getSimpleName().equals("MidiInDevice")) {
							
							receiver = device.getTransmitter();
							receiver.setReceiver(this);
							device.open();
							receiverDevice = device;
							logger.debug("receiver opened");
						}
					}
				}
			}
		}
	}
}
