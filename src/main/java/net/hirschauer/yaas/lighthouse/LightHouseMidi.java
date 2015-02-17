package net.hirschauer.yaas.lighthouse;

import java.util.Date;
import java.util.HashMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightHouseMidi {
	
	private static final Logger logger = LoggerFactory.getLogger(LightHouseMidi.class);
	public static final String YAAS_BUS = "YaasBus";
	private Info yaasBus;
	private MidiDevice yaasDevice;
	private Receiver yaasReceiver;
	private HashMap<String, Info> possibleMidiInfos = new HashMap<String, Info>();
	private static LightHouseMidi instance;

	public static LightHouseMidi getInstance() {
		if (instance == null) {
			instance = new LightHouseMidi();			
		}
		return instance;
	}
	private LightHouseMidi() {
		
		for (Info info : MidiSystem.getMidiDeviceInfo()) {
			logger.debug("Found device " + info.getName() + " - " + info.getDescription());
			
			this.possibleMidiInfos.put(info.getName(), info);
			
			if (YAAS_BUS.equals(info.getName())) {
				this.yaasBus = info;
				logger.info("Using device " + info.getName());
			}
		}
		
		if (yaasBus != null) {
			try {
				yaasDevice = getMidiDevice(this.yaasBus);
			} catch (MidiUnavailableException e) {
				logger.error("Couldn't initialize midi device", e);
			}
		}
		
		if (yaasDevice != null) {
			try {
				yaasDevice.open();				
			} catch (MidiUnavailableException e) {
				logger.error("Couldn't open midi device", e);
			}			
		}
		
		if (yaasDevice != null) {
			try {
				yaasReceiver = yaasDevice.getReceiver();
			} catch (MidiUnavailableException e) {
				logger.error("Couldn't get receiver", e);
			}
		}
		
		if (yaasReceiver != null) {
			try {
				sendMidiNote(1, 1, 1);
			} catch (InvalidMidiDataException e) {
				logger.error("Couldn't send midi note", e);
			}
		}
	}
	
	public MidiDevice getMidiDevice(Info info) throws MidiUnavailableException {
		return MidiSystem.getMidiDevice(info);
	}
	
	public void sendMidiNote(int channel, int note, int value) throws InvalidMidiDataException {
		ShortMessage message = new ShortMessage();
		if (value < 0) value = 0;
		else if (value > 127) value = 127;
		message.setMessage(ShortMessage.NOTE_ON, channel, note, value);
		yaasReceiver.send(message, new Date().getTime());
//		message.setMessage(ShortMessage.NOTE_OFF, channel, note, value);
//		yaasReceiver.send(message, new Date().getTime());
	}
	public void sendMidiNote(int note, int value) throws InvalidMidiDataException {
		sendMidiNote(1, note, value);
	}

	public HashMap<String, Info> getPossibleMidiInfos() {
		return possibleMidiInfos;
	}

	public void setPossibleMidiInfos(HashMap<String, Info> possibleMidiInfos) {
		this.possibleMidiInfos = possibleMidiInfos;
	}
}
