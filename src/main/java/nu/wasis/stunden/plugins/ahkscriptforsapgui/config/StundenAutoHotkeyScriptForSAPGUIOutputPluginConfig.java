package nu.wasis.stunden.plugins.ahkscriptforsapgui.config;

import java.util.HashMap;
import java.util.Map;

public class StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig {

	private String sapExecutable;
	private String username;
	private String password;
	private boolean autoRun;
	private boolean printProjectMapping;
	private String autoHotkeyExecutable;
	private String outputScriptFilename;
	
	private Map<String, String> projectMapping = new HashMap<>();

	public String getSapExecutable() {
		return sapExecutable;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean isAutoRun() {
		return autoRun;
	}

	public String getAutoHotkeyExecutable() {
		return autoHotkeyExecutable;
	}
	
	public boolean getPrintProjectMapping() {
		return printProjectMapping;
	}

	public Map<String, String> getProjectMapping() {
		return projectMapping;
	}

	public String getOutputScriptFilename() {
		return outputScriptFilename;
	}

}
