package net.hirschauer.yaas.lighthouse.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javafx.concurrent.Task;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorLogListener extends Task<Void> {
	
	private static final Logger logger = LoggerFactory.getLogger(ErrorLogListener.class);
	private String errorFile;
	private long lastmodified;
	private int lineCount = 0;
		
	@Override
	protected Void call() throws Exception {
		
		logger.debug("call");
		Thread.sleep(2000);

		//logger.debug("lastmodified " + lastmodified);
//			logger.debug("starting line count " + lineCount);

		while (true) {
			try {					
					
//				logger.debug("awake");
				File errorLog = new File(errorFile);
				if (errorLog.exists()) {

					if (lastmodified != errorLog.lastModified()) {
							
						List<String> lines = IOUtils.readLines(new FileInputStream(errorLog), "UTF-8");
						logger.debug("starting line count " + lineCount);
						logger.debug("found lines " + lines.size());
						StringBuffer sb = new StringBuffer();
						for (int i = lineCount; i < lines.size(); i++) {
							sb.append(lines.get(i));
							sb.append("\n");
						}
						updateMessage(sb.toString());
//						logger.debug(sb.toString());
						lastmodified = errorLog.lastModified();
						lineCount = lines.size();
					}
				}
				
				Thread.sleep(2000);

			} catch (InterruptedException interrupted) {
				if (isCancelled()) {
					updateMessage("Cancelled");
					logger.debug("cancelled");
					break;
				}
			}
		}
		return null;
	}

	public void setErrorFile(String fileName) {
		
		logger.debug("errorfile changed to " + fileName);
		
		this.errorFile = fileName;
		File errorLog = new File(errorFile);
		if (errorLog.exists()) {
			
			lastmodified = errorLog.lastModified();
			try {
				List<String> lines = IOUtils.readLines(new FileInputStream(errorLog), "UTF-8");
				lineCount = lines.size();
			} catch (Exception e) {
				logger.error("Could not init error file", e);
			}
		}
	}
}
