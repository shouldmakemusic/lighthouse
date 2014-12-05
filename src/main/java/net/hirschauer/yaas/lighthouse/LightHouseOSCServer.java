package net.hirschauer.yaas.lighthouse;

import java.io.IOException;
import java.net.SocketAddress;

import net.hirschauer.yaas.lighthouse.model.LogEntry;
import net.hirschauer.yaas.lighthouse.model.SensorValue;
import net.hirschauer.yaas.lighthouse.visual.LogController;
import net.hirschauer.yaas.lighthouse.visual.SensorController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class LightHouseOSCServer {
	
	private static final Logger logger = LoggerFactory.getLogger(OSCServer.class);
	private boolean pause = false; // (must be an instance or static field to be

	private final Object sync = new Object();
	private OSCServer c = null;
	
	private SensorController sensorController;
	private LogController logController;
	private SensorValue sensorData = new SensorValue();

	public LightHouseOSCServer() {
		try {
			// create UDP server on port 9050
			c = OSCServer.newUsing(OSCServer.UDP, 9050, false);
			logger.info("OSC Server started UDP 9050");
		} catch (IOException e1) {
			logger.error("Could not start osc server", e1);
			return;
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
					if (sensorController != null) {
						sensorData.setValues(m.getArg(0), m.getArg(1), m.getArg(2));
						sensorController.setSensorData(sensorData);
					}
				} else {
					logger.debug("Received message: " + m.getName() + " " + args + " from "
						+ addr);
					logController.log(m);
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

	}
	
	public void stop() {
		try {
			this.c.stop();
		} catch (IOException e) {
			logger.error("Could not stop osc", e);
			this.c = null;
		}
	}
	
	public void setSensorController(SensorController sc) {
		this.sensorController = sc;
		this.sensorController.setSensorData(this.sensorData);
	}
	public void setLogController(LogController lc) {
		this.logController = lc;
	}
}
