package net.hirschauer.yaas.lighthouse.osccontroller;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;

import net.hirschauer.yaas.lighthouse.LightHouseMidi;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.OSCMessageFromTask;
import net.hirschauer.yaas.lighthouse.model.SensorValue;
import de.sciss.net.OSCMessage;

public abstract class OSCController {
	
	protected LightHouseMidi midi;
	protected LightHouseOSCServer oscServer;
	
	public OSCController(LightHouseOSCServer oscServer, LightHouseMidi midi) {
		this.midi = midi;
		this.oscServer = oscServer;
	}

	abstract public void handleMessage(OSCMessage m) throws InvalidMidiDataException;
	
	public void updateMessage(OSCMessage m) {
		this.oscServer.updateMessage(m);
	}
	public void updateMessage(OSCMessage m, String type) {
		this.oscServer.updateMessage(m, type);
	}
	public void updateMessage(OSCMessageFromTask m) {
		this.oscServer.updateMessage(m);
	}
	
	public void updateValue(SensorValue clone) {
		this.oscServer.updateSensorData(clone);
	}
	
	public void sendToYaas(OSCMessage m) throws IOException {
		this.oscServer.sendToYaas(m);
	}

}
