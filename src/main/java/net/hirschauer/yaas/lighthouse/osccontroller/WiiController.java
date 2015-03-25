package net.hirschauer.yaas.lighthouse.osccontroller;

import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;

import net.hirschauer.yaas.lighthouse.LightHouseMidi;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.OSCMessageFromTask;
import net.hirschauer.yaas.lighthouse.model.SensorValue;
import net.hirschauer.yaas.lighthouse.model.SensorValue.SensorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCMessage;

public class WiiController extends OSCController {

	private static final Logger logger = LoggerFactory.getLogger(WiiController.class);
	private SensorValue sensorDataWii = new SensorValue(SensorType.WII, 4, 6);
	private long lastUpdateWii = 0;

	public WiiController(LightHouseOSCServer oscServer, LightHouseMidi midi) {
		super(oscServer, midi);
	}

	@Override
	public void handleMessage(OSCMessage m) throws InvalidMidiDataException {
		
		if (m.getName().startsWith(OSCMessageFromTask.TYPE_WII_EXEC)) {
			
			OSCMessageFromTask message = new OSCMessageFromTask((String)m.getArg(0));
			message.setType(OSCMessageFromTask.TYPE_WII_EXEC);
			ArrayList<String> args = new ArrayList<String>();
			for (int i=1; i<m.getArgCount(); i++) {
				args.add((String)m.getArg(i));
			}
			message.setArgList(args);
			updateMessage(message);
			
		} else if (m.getName().equals("/wii/1/accel/xyz")) {

			// show sensor values in barChart
			sensorDataWii.setValues(m.getArg(0), m.getArg(1), m.getArg(2));
			
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastUpdateWii > 500) {
				lastUpdateWii = currentTime;
				
				if (midi == null) {
					midi = LightHouseMidi.getInstance();
				}
				if (midi != null) {
					midi.sendMidiNote(12, sensorDataWii.getLiveXValue());
					midi.sendMidiNote(13, sensorDataWii.getLiveYValue());
					midi.sendMidiNote(14, sensorDataWii.getLiveZValue());
				}
			}
			
			try {
				// this is for the visual feedback
				// in another thread
				updateValue(sensorDataWii.clone());

			} catch (CloneNotSupportedException e) {
				logger.error("Could not update wii sensor values");
			}
			
		} else if (m.getName().equals("/wii/1/accel/pry")) {
			
			sensorDataWii.setPryValues(m.getArg(0), m.getArg(1), m.getArg(2), m.getArg(3));
		} else {
			updateMessage(m, OSCMessageFromTask.TYPE_WII);
		}
	}
	
	public SensorValue getSensorDataWii() {
		return sensorDataWii;
	}

	public void setSensorDataWii(SensorValue sensorDataWii) {
		this.sensorDataWii = sensorDataWii;
	}
	

}
