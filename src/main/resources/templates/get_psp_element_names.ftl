<#include "open_sap_worksheet.ftl">
;;; Attach to console
DllCall("AttachConsole", "int", -1, "int")
conout := FileOpen("CONOUT$", "w")

;;; Copy all PSP element names and put them to stdout
;;; Max 30 Elements to avoid Hell on Earth if something goes wrong
Loop, 30
{
	WinActivate Arbeitszeitblatt
	LineTwo =
	
	Click 195, 239
	Send, {SHIFT}+{End}
	Send ^c
	LineOne = %Clipboard%
	FileAppend,%LineOne%,*
	FileAppend,`n,*
	
	Click 195, 262
	Send, {SHIFT}+{End}
	Send ^c
	LineTwo = %Clipboard%
	if (LineTwo = LineOne)
		break
	Click 1035, 343
}

conout.Close()

<#include "exit_sap.ftl">
