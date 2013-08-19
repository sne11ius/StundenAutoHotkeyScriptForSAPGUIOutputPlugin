package nu.wasis.stunden.plugins.ahkscriptforsapgui;

public class StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig {

	private String sapExecutable;
	private String username;
	private String password;
	private boolean autoRun;
	private String autoHotkeyExecutable;
	public String getSapExecutable() {
		return sapExecutable;
	}
	public void setSapExecutable(String sapExecutable) {
		this.sapExecutable = sapExecutable;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isAutoRun() {
		return autoRun;
	}
	public void setAutoRun(boolean autoRun) {
		this.autoRun = autoRun;
	}
	public String getAutoHotkeyExecutable() {
		return autoHotkeyExecutable;
	}
	public void setAutoHotkeyExecutable(String autoHotkeyExecutable) {
		this.autoHotkeyExecutable = autoHotkeyExecutable;
	}
	
}
