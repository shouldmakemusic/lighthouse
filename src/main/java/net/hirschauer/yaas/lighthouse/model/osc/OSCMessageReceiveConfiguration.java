package net.hirschauer.yaas.lighthouse.model.osc;

public class OSCMessageReceiveConfiguration extends OSCMessage {
	
	public OSCMessageReceiveConfiguration(String arg) {
		super("/yaas/controller/receive/configuration", arg);
	}
	public OSCMessageReceiveConfiguration(String... args) {
		super("/yaas/controller/receive/configuration", args);
	}
}
