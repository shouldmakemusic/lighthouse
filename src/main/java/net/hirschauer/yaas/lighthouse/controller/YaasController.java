package net.hirschauer.yaas.lighthouse.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;

import net.hirschauer.yaas.lighthouse.LightHouseMidi;
import net.hirschauer.yaas.lighthouse.LightHouseOSCServer;
import net.hirschauer.yaas.lighthouse.model.OSCMessageFromTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCMessage;

public class YaasController extends Controller {

	private static final Logger logger = LoggerFactory.getLogger(YaasController.class);

	public static HashMap<String, List<String>> yaasCommands = new HashMap<String, List<String>>();
	public static String yaasErrorLogFile;
	public static String yaasStdOutLogFile;
	public static String yaasConfigFile;
	
	public YaasController(LightHouseOSCServer oscServer, LightHouseMidi midi) {
		super(oscServer, midi);
	}

	@Override
	public void handleMessage(OSCMessage m) throws InvalidMidiDataException {

		if (m.getName().startsWith("/yaas/log")) {
			
			updateMessage(m, OSCMessageFromTask.TYPE_YAAS);
		} else if (m.getName().startsWith("/yaas/config")) {
			
			if (m.getName().equals("/yaas/config/errorfile")) {
				yaasErrorLogFile = (String) m.getArg(0);
			} else if (m.getName().equals("/yaas/config/configfile")) {
				yaasConfigFile = (String) m.getArg(0);
			} else if (m.getName().equals("/yaas/config/stdoutfile")) {
				yaasStdOutLogFile = (String) m.getArg(0);
			}
			updateMessage(m, OSCMessageFromTask.TYPE_YAAS);
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
				updateMessage(new OSCMessageFromTask("Got available commands from YAAS"));
			}
		} else {			
			updateMessage(m);
		}
	}
}
