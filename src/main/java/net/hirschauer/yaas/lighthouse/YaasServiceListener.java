package net.hirschauer.yaas.lighthouse;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YaasServiceListener implements ServiceListener {
	
	private static final Logger logger = LoggerFactory.getLogger(YaasServiceListener.class);

	public void serviceAdded(ServiceEvent event) {
		logger.info("Service added: " + event.getName() + "." + event.getType());
	}

	public void serviceRemoved(ServiceEvent event) {
		logger.info("Service removed: " + event.getName() + "." + event.getType());
	}

	public void serviceResolved(ServiceEvent event) {
		logger.info("Service resolved: " + event.getInfo());

		
	}

}
