package net.hirschauer.yaas.lighthouse;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javafx.concurrent.Task;

import javax.sound.midi.InvalidMidiDataException;

import net.hirschauer.yaas.lighthouse.model.OSCMessageFromTask;
import net.hirschauer.yaas.lighthouse.model.SensorValue;
import net.hirschauer.yaas.lighthouse.model.SensorValue.SensorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class LightHouseOSCServer extends Task<SensorValue> implements OSCListener {
	
	private static final Logger logger = LoggerFactory.getLogger(OSCServer.class);
	private boolean pause = false; // (must be an instance or static field to be

	private final Object sync = new Object();
	private OSCServer c = null;
	
	private SensorValue sensorDataAndroid = new SensorValue(SensorType.ANDROID, -10, 10);
	private SensorValue sensorDataWii = new SensorValue(SensorType.WII, 4, 6);
	
	private static HashMap<String, List<String>> yaasCommands = new HashMap<String, List<String>>();
	
	private long lastUpdateWii = 0;
	
	private LightHouseMidi midi;
	
	public LightHouseOSCServer(LightHouseMidi midi) {
		this.midi = midi;
	}
	
	public void stop() {
		logger.debug("stop()");
		c.removeOSCListener(this);
		try {
			c.stop();
		} catch (IOException e) {			
		}
		c = null;
	}

	@Override
	protected SensorValue call() throws Exception {
		logger.debug("call()");
		try {
			// create UDP server on port 9050
			c = OSCServer.newUsing(OSCServer.UDP, 9050, false);
			logger.info("OSC Server started UDP 9050");
		} catch (IOException e1) {
			logger.error("Could not start osc server", e1);
			return null;
		}
		//c.dumpOSC(OSCServer.kDumpBoth, System.err);
		try {
			c.start();
		} catch (IOException e) {
			logger.error("Could not start osc server", e);
		}

		// now add a listener for incoming messages from
		// any of the active connections
		c.addOSCListener(this);

		return null;
	}
		
	public void messageReceived(OSCMessage m, SocketAddress addr,
			long time) {
		
		String args = "";
		for (int i=0; i < m.getArgCount(); i++) {
			args += m.getArg(i) + " ";
		}

		// first of all, send a reply message (just a demo)
		try {
			c.send(new OSCMessage("/done", new Object[] { m.getName() }),
					addr);
		} catch (IOException e1) {
			logger.error("Could not send reply");
		}

		if (m.getName().equals("/pause")) {
			// tell the main thread to pause the server,
			// wake up the main thread
			pause = true;
			synchronized (sync) {
				sync.notifyAll();
			}
		} else if (m.getName().equals("/quit")) {
			// wake up the main thread
			synchronized (sync) {
				sync.notifyAll();
			}
		} else if (m.getName().equals("/dumpOSC")) {
			// change dumping behaviour
			c.dumpOSC(((Number) m.getArg(0)).intValue(), System.err);
			
		} else if (m.getName().startsWith("/wii")) {
			
			try {
				handleWiiMessages(m);
			} catch (InvalidMidiDataException e) {
				logger.error(e.getMessage(), e);
			}
		} else if (m.getName().startsWith("/yaas")) {
			
			try {
				handleYaasMessages(m);
			} catch (InvalidMidiDataException e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			logger.debug("Received message: " + m.getName() + " " + args + " from " + addr);
			updateMessage(m);
		}
	}
	
	private void updateMessage(OSCMessage m) {
		updateMessage(new OSCMessageFromTask(m).toString());
	}
	// try {
	// do {
	// if (pause) {
	// System.out.println("  waiting four seconds...");
	// try {
	// Thread.sleep(4000);
	// } catch (InterruptedException e1) {
	// }
	// pause = false;
	// }
	// System.out.println("  start()");
	// // start the server (make it attentive for incoming connection
	// // requests)
	// c.start();
	// try {
	// synchronized (sync) {
	// sync.wait();
	// }
	// } catch (InterruptedException e1) {
	// }
	//
	// System.out.println("  stop()");
	// c.stop();
	// } while (pause);
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }

	private void handleYaasMessages(OSCMessage m) throws InvalidMidiDataException {
		if (m.getName().equals("/yaas/sensor")) {
			
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
		} else if (m.getName().startsWith("/yaas/play")) {
			updateMessage(m);
			midi.sendMidiNote(1, 1);
			
		} else if (m.getName().startsWith("/yaas/stop")) {
			updateMessage(m);
			midi.sendMidiNote(1, 2);
			
		} else if (m.getName().startsWith("/yaas/log")) {
			
			updateMessage(m);
		} else if (m.getName().startsWith("/yaas/commands")) {
			
			if (m.getName().endsWith("clear")) {
				yaasCommands = new HashMap<String, List<String>>();
			} else if (m.getName().endsWith("list")) {
				String className = (String) m.getArg(0);
				String methodName = (String) m.getArg(1);
				if (!yaasCommands.containsKey(className)) {
					yaasCommands.put(className, new ArrayList<String>());
				}
				yaasCommands.get(className).add(methodName);
			} else if (m.getName().endsWith("done")) {
				logger.info("Got available commands from YAAS");
				updateMessage(new OSCMessageFromTask("Got available commands from YAAS").toString());
			}
		} else {
			updateMessage(m);
		}
	}

	private void handleWiiMessages(OSCMessage m) throws InvalidMidiDataException {
		if (m.getName().equals("/wii/1/accel/xyz")) {

			// show sensor values in barChart
			sensorDataWii.setValues(m.getArg(0), m.getArg(1), m.getArg(2));
			
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastUpdateWii > 500) {
				lastUpdateWii = currentTime;
				
				midi.sendMidiNote(12, sensorDataWii.getLiveXValue());
				midi.sendMidiNote(13, sensorDataWii.getLiveYValue());
				midi.sendMidiNote(14, sensorDataWii.getLiveZValue());
			}
			
			try {
				// this is for the visual feedback
				// in another thread
				updateValue(sensorDataWii.clone());

			} catch (CloneNotSupportedException e) {
				logger.error("Could not update wii sensor values");
			}
			
		} else if (m.getName().equals("/wii/1/accel/pry")) {
			
			//sensorDataAndroid.setPryValues(m.getArg(0), m.getArg(1), m.getArg(2), m.getArg(3));
		} else {
			updateMessage(m);
		}
	}
	public SensorValue getSensorDataAndroid() {
		return sensorDataAndroid;
	}

	public void setSensorDataAndroid(SensorValue sensorDataAndroid) {
		this.sensorDataAndroid = sensorDataAndroid;
	}

	public SensorValue getSensorDataWii() {
		return sensorDataWii;
	}

	public void setSensorDataWii(SensorValue sensorDataWii) {
		this.sensorDataWii = sensorDataWii;
	}
}
