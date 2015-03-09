package net.hirschauer.yaas.lighthouse.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import net.hirschauer.yaas.lighthouse.LightHouse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyYaas {
	private static final Logger logger = LoggerFactory
			.getLogger(CopyYaas.class);

	public static boolean run(String targetname) throws IOException {
		File dir;
		String path1 = "/Applications";
		String path2 = "/Contents/App-Resources/MIDI Remote Scripts";
		String name = "Ableton Live";
		
		if (SystemUtils.IS_OS_WINDOWS) {
			path1 = "c:\\ProgramData\\Ableton";
			path2 = "\\Resources\\MIDI Remote Scripts";
			name = "Live";
		}
		
		dir = new File(path1);
		if (dir.exists()) {
			File live = null;
			for (File child : dir.listFiles()) {
				if (child.getName().startsWith(name)) {
					live = child;
				}
			}
			
			if (live != null) {
				File scripts = new File(live.getAbsolutePath() + path2);
				if (scripts.exists()) {
					File yaas = new File(scripts.getAbsolutePath()
							+ File.separator + targetname);
					if (yaas.exists()) {
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setTitle("Confirmation Dialog");
						alert.setHeaderText("The directory already exists");
						alert.setContentText("Overwrite?");

						Optional<ButtonType> overwrite = alert.showAndWait();
						if (overwrite.get() != ButtonType.OK) {
							return false;
						}
					} else {
						yaas.mkdir();
					}
					String path = LightHouse.class.getProtectionDomain()
							.getCodeSource().getLocation().getPath();
					try {
						path = URLDecoder.decode(path, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						logger.error(e.getMessage(), e);
					}

					if (path != null) {

						File src;
						if (path.endsWith("classes" + File.separator)) {
							src = new File(new File(path).getParentFile()
									.getParentFile().getPath()
									+ File.separator + "yaas");
						} else {
							src = new File(new File(path).getParentFile()
									.getPath() + File.separator + "yaas");
						}
						if (src.exists()) {
							FileUtils.copyDirectory(src, yaas);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
