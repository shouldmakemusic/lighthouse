package net.hirschauer.yaas.lighthouse.osccontroller;

import javax.sound.midi.InvalidMidiDataException;

import net.hirschauer.yaas.lighthouse.LightHouseMidi;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.SensorValue;
import net.hirschauer.yaas.lighthouse.model.SensorValue.SensorType;
import net.hirschauer.yaas.lighthouse.model.osc.OSCMessageFromTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCMessage;

public class AndroidController extends OSCController {

	private static final Logger logger = LoggerFactory.getLogger(AndroidController.class);

	public AndroidController(LightHouseOSCServer oscServer, LightHouseMidi midi) {
		super(oscServer, midi);
	}

	private SensorValue sensorDataAndroid = new SensorValue(SensorType.ANDROID, -10, 10);

	@Override
	public void handleMessage(OSCMessage m) throws InvalidMidiDataException {
		
		if (m.getName().equals("/android/sensor")) {
			
			sensorDataAndroid.setValues(m.getArg(0), m.getArg(1), m.getArg(2));

			midi.sendMidiNote(2, sensorDataAndroid.getLiveXValue());
			midi.sendMidiNote(3, sensorDataAndroid.getLiveYValue());
			midi.sendMidiNote(4, sensorDataAndroid.getLiveZValue());

			try {
				// this is for the visual feedback
				// in another thread
				updateValue(sensorDataAndroid.clone());

			} catch (CloneNotSupportedException e) {
				logger.error("Could not update android sensor values");
			}
		} else if (m.getName().startsWith("/android/play")) {
			
			updateMessage(m, OSCMessageFromTask.TYPE_ANDROID);
			midi.sendMidiNote(1, 1);
			
		} else if (m.getName().startsWith("/android/stop")) {
			updateMessage(m, OSCMessageFromTask.TYPE_ANDROID);
			midi.sendMidiNote(1, 2);
		}
	}	
	
	public SensorValue getSensorDataAndroid() {
		return sensorDataAndroid;
	}

	public void setSensorDataAndroid(SensorValue sensorDataAndroid) {
		this.sensorDataAndroid = sensorDataAndroid;
	}

}
