package net.hirschauer.yaas.lighthouse;

import java.io.IOException;
import java.net.InetAddress;

import javafx.concurrent.Task;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightHouseService extends Task<ServiceInfo> implements ServiceListener {
	
	private static final Logger logger = LoggerFactory.getLogger(LightHouseService.class);
	JmDNS jmdns = null;
	ServiceInfo serviceInfo;
	public static final String SERVICE_TYPE = "_yaas._tcp.local.";
	
	public LightHouseService() {
	}

	public void serviceAdded(ServiceEvent event) {
		logger.info("Service added: " + event.getName() + "." + event.getType());
	}

	public void serviceRemoved(ServiceEvent event) {
		logger.info("Service removed: " + event.getName() + "." + event.getType());
	}

	public void serviceResolved(ServiceEvent event) {
		logger.info("Service resolved: " + event.getInfo());
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

	@Override
	protected ServiceInfo call() throws Exception {
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
			
			jmdns.addServiceListener(SERVICE_TYPE, this);
			logger.info("Service Listener startet");
		} catch (IOException e) {
			logger.error("Could not start service discovery", e);
		}
		
		serviceInfo = ServiceInfo.create("_yaas._tcp.local.",
                "LightHouse", 9050,
                "Yaas OSC Server");
		try {
			jmdns.unregisterAllServices();
			jmdns.registerService(serviceInfo);
			logger.info("Registered service " + serviceInfo.getName());
		} catch (IOException e) {
			logger.error("Could not start service info", e);
		}	
		return serviceInfo;
	}
}
