package nu.wasis.stunden.plugins.ahkscriptforsapgui;

public enum AHKScriptFile {

	CLOSE_SAP_WORKSHEET,
	ENTER_WORK_HOURS,
	EXIT_SAP,
	FILL_PSP,
	GET_PSP_ELEMENT_NAMES,
	GOTO_WEEK,
	OPEN_SAP_WORKSHEET,
	PULL_ALL_PSPS,
	RUN_SAP;
	
	public String getFilename() {
		return this.name().toLowerCase() + ".ftl";
	}
}
