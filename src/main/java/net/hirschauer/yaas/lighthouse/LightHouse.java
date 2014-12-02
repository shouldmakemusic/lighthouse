package net.hirschauer.yaas.lighthouse;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class LightHouse {

	private static final Logger logger = LoggerFactory
			.getLogger(LightHouse.class);
	private boolean pause = false; // (must be an instance or static field to be
	
	JmDNS jmdns = null;
	ServiceInfo serviceInfo;

	final Object sync = new Object();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new LightHouse().run();
	}

	private void run() {
		// TODO Auto-generated method stub

	}

	public LightHouse() {

		logger.debug("LightHouse created");

		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			InetAddress bindingAddress = InetAddress.getByName(ip);
			logger.debug("at " + ip);
			
			InetAddress in  = InetAddress.getLocalHost();  
			InetAddress[] all = InetAddress.getAllByName(in.getHostName());  
			for (int i=0; i<all.length; i++) {  
				logger.debug("  address = " + all[i]);  
			}
			
			jmdns = JmDNS.create(bindingAddress);
			String type = "_yaas._tcp.local.";
			jmdns.addServiceListener(type, new YaasServiceListener());
			logger.info("Service Listener startet");
		} catch (IOException e) {
			logger.error("Could not start service discovery", e);
		}
		
		serviceInfo = ServiceInfo.create("_yaas._tcp.local.",
                "LightHouse", 0x5455,
                "Yaas OSC Server");
		try {
			jmdns.unregisterAllServices();
			jmdns.registerService(serviceInfo);
			logger.info("Registered service " + serviceInfo.getName());
		} catch (IOException e) {
			logger.error("Could not start service info", e);
		}

		final OSCServer c;
		try {
			// create UDP server on port 9050
			c = OSCServer.newUsing(OSCServer.UDP, 9050, false);
			logger.info("OSC Server started UDP 9050");
		} catch (IOException e1) {
			logger.error("Could not start osc server", e1);
			return;
		}
		c.dumpOSC(OSCServer.kDumpBoth, System.err);
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
				
				logger.debug("Received message: " + m.getName() + " from " + addr);

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
				} 
			}
		});
//		try {
//			do {
//				if (pause) {
//					System.out.println("  waiting four seconds...");
//					try {
//						Thread.sleep(4000);
//					} catch (InterruptedException e1) {
//					}
//					pause = false;
//				}
//				System.out.println("  start()");
//				// start the server (make it attentive for incoming connection
//				// requests)
//				c.start();
//				try {
//					synchronized (sync) {
//						sync.wait();
//					}
//				} catch (InterruptedException e1) {
//				}
//
//				System.out.println("  stop()");
//				c.stop();
//			} while (pause);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}

	}
	
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		logger.info("finalize()");
		if (jmdns != null) {
			jmdns.unregisterService(serviceInfo);
			jmdns.close();
		}
	}

}
