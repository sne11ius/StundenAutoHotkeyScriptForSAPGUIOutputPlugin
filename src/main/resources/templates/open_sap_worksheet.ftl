<#include "run_sap.ftl">
;;; Open desired Node in Tree
Click 38, 185
Click 100, 200
Click 100, 200
WinWait Arbeitszeitblatt
;;; Activate date field & enter the start date
Click 161, 190
Send {Home}
Send {SHIFT}+{End}
Send ${periodBegin}
;;; Click the edit button
Click 20, 115
WinWait Arbeitszeitblatt: Erfassungssicht
;;; Now do the stuff...
