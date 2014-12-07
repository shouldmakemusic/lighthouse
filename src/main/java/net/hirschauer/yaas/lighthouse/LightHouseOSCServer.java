package net.hirschauer.yaas.lighthouse;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import net.hirschauer.yaas.lighthouse.model.LogEntry;
import net.hirschauer.yaas.lighthouse.model.SensorValue;
import net.hirschauer.yaas.lighthouse.visual.LogController;
import net.hirschauer.yaas.lighthouse.visual.SensorController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class LightHouseOSCServer extends Task<SensorValue> {
	
	private static final Logger logger = LoggerFactory.getLogger(OSCServer.class);
	private boolean pause = false; // (must be an instance or static field to be

	private final Object sync = new Object();
	private OSCServer c = null;
	
	private LogController logController;	
	
	private SensorValue sensorDataAndroid = new SensorValue(4, 6);
	private int counter = 0;
	
	public LightHouseOSCServer() {

	}
	
	public void stop() {
		try {
			this.c.stop();
		} catch (IOException e) {
			logger.error("Could not stop osc", e);
			this.c = null;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// now add a listener for incoming messages from
		// any of the active connections
		c.addOSCListener(new OSCListener() {
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
					getSensorData().setValues(m.getArg(0), m.getArg(1), m.getArg(2));
					
				} else if (m.getName().equals("/wii/1/accel/xyz")) {

					// show sensor values in barChart
					sensorDataAndroid.setValues(m.getArg(0), m.getArg(1), m.getArg(2));
					if (counter == 10) {
						try {
							updateValue(sensorDataAndroid.clone());
							counter = 0;
						} catch (CloneNotSupportedException e) {
							logger.error("Could not update android sensor values");
						}
					} else {
						counter++;
					}
					
				} else if (m.getName().equals("/wii/1/accel/pry")) {

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
		});
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

		return null;
	}

	public SensorValue getSensorData() {
		return sensorDataAndroid;
	}

	public void setSensorData(SensorValue sensorData) {
		this.sensorDataAndroid = sensorData;
	}
}
