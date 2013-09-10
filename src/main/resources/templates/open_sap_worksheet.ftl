;;; Run and activate SAP
Run ${sapExecutable},,,sapPid
WinWait SAP Logon 730
WinActivate SAP Logon 730
;;; Click the login button
Click 35, 35
WinWait SAP
Sleep 2000
;;; Do the Login
Send ${username}{Tab}${password}{Enter}
WinWait SAP Easy Access
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
