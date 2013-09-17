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
