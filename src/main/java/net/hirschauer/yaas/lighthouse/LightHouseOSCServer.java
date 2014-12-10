package net.hirschauer.yaas.lighthouse;

import java.io.IOException;
import java.net.SocketAddress;

import javafx.concurrent.Task;
import net.hirschauer.yaas.lighthouse.model.SensorValue;
import net.hirschauer.yaas.lighthouse.model.SensorValue.SensorType;
import net.hirschauer.yaas.lighthouse.visual.LogController;

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
	
	private LogController logController;	
	
	private SensorValue sensorDataAndroid = new SensorValue(SensorType.ANDROID, -10, 10);
	private SensorValue sensorDataWii = new SensorValue(SensorType.WII, 4, 6);
	
	public LightHouseOSCServer() {

	}
	
	public void stop() {
		c.removeOSCListener(this);
		try {
			c.stop();
		} catch (IOException e) {
			c = null;
		}
	}
	
	public void setLogController(LogController lc) {
		this.logController = lc;
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
			
		} else if (m.getName().equals("/yaas/sensor")) {
			// show sensor values in barChart
			sensorDataAndroid.setValues(m.getArg(0), m.getArg(1), m.getArg(2));
			
		} else if (m.getName().equals("/wii/1/accel/xyz")) {

			// show sensor values in barChart
			sensorDataWii.setValues(m.getArg(0), m.getArg(1), m.getArg(2));
			try {
				// this is for the visual feedback
				// in another thread
				updateValue(sensorDataWii.clone());

			} catch (CloneNotSupportedException e) {
				logger.error("Could not update android sensor values");
			}
			
		} else if (m.getName().equals("/wii/1/accel/pry")) {
			
			//sensorDataAndroid.setPryValues(m.getArg(0), m.getArg(1), m.getArg(2), m.getArg(3));

		} else if (m.getName().startsWith("/wii")) {
			if (logController != null) {
				logController.log(m);
			}
		} else {
			logger.debug("Received message: " + m.getName() + " " + args + " from "
				+ addr);
			if (logController != null) {
				logController.log(m);
			}
		}
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
