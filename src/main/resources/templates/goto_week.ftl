<#include "close_sap_worksheet.ftl">
;;; Activate date field & enter the start date
Click 166, 195
Send {Home}
Send {SHIFT}+{End}
Send ${periodBegin}
;;; Click the edit button
Click 20, 115
WinWait Arbeitszeitblatt: Erfassungssicht
;;; Now do the stuff...
