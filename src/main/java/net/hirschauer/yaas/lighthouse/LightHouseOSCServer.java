package net.hirschauer.yaas.lighthouse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javafx.concurrent.Task;

import javax.sound.midi.InvalidMidiDataException;

import net.hirschauer.yaas.lighthouse.controller.AndroidController;
import net.hirschauer.yaas.lighthouse.controller.WiiController;
import net.hirschauer.yaas.lighthouse.controller.YaasController;
import net.hirschauer.yaas.lighthouse.model.OSCMessageFromTask;
import net.hirschauer.yaas.lighthouse.model.SensorValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class LightHouseOSCServer extends Task<SensorValue> implements OSCListener {
	
	private static final Logger logger = LoggerFactory.getLogger(OSCServer.class);
//	private boolean pause = false; // (must be an instance or static field to be

	private final Object sync = new Object();
	private OSCServer c = null;

	private YaasController yaasController;
	private AndroidController androidController;
	private WiiController wiiController;
	
	private static LightHouseOSCServer instance;
	
	protected LightHouseOSCServer(LightHouseMidi midi) {

		if (instance != null) {
			logger.warn("Creating multiple oscServers");
		}
		instance = this;
		yaasController = new YaasController(this, midi);
		wiiController = new WiiController(this, midi);
		androidController = new AndroidController(this, midi);
	}
	
	public static LightHouseOSCServer getInstance() {
		return instance;
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
	
	public void sendToYaas(OSCMessage m) throws IOException {
		c.send(m, new InetSocketAddress("localhost", 9190));
		logger.debug("Sent message " + m.getName() + " to YAAS");
	}
		
	public void messageReceived(OSCMessage m, SocketAddress addr, long time) {
		

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
//			pause = true;
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
				wiiController.handleMessage(m);
			} catch (InvalidMidiDataException e) {
				logger.error("Could not handle wii message: " + m.getName(), e);
			}
		} else if (m.getName().startsWith("/android")) {
			
			try {
				androidController.handleMessage(m);
			} catch (InvalidMidiDataException e) {
				logger.error("Could not handle android message: " + m.getName(), e);
			}
		} else if (m.getName().startsWith("/yaas")) {
			
			try {
				yaasController.handleMessage(m);
			} catch (InvalidMidiDataException e) {
				logger.error("Could not handle yaas message: " + m.getName(), e);
			}
		} else {
			String args = "";
			for (int i=0; i < m.getArgCount(); i++) {
				args += m.getArg(i) + " ";
			}

			logger.debug("Received message: " + m.getName() + " " + args + " from " + addr);
			updateMessage(m);
		}
	}
	
	public void updateMessage(OSCMessage m) {
		updateMessage(new OSCMessageFromTask(m));
	}
	public void updateMessage(OSCMessage m, String type) {
		updateMessage(new OSCMessageFromTask(m, type));
	}
	public void updateMessage(OSCMessageFromTask m) {
		updateMessage(m.toString());
	}
	public void updateSensorData(SensorValue data) {
		updateValue(data);
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



	public void fetchAvailableCommandsFromYaas() {
		
		OSCMessage m = new OSCMessage("/yaas/controller/send/info");
		try {
			sendToYaas(m);
		} catch (IOException e) {
			logger.error("Could not request controller info", e);
		}
	}
}
