StundenAutoHotkeyScriptForSAPGUIOutputPlugin
============================================

Plugin for https://github.com/sne11ius/stunden that creates an AutoHotkey script for SAP.

This plugin requires AutoHotkey to be installed. See http://www.autohotkey.com/

This plugin will control your mouse and keyboard, run other programs and do all kind of weird stuff.
It may also desecrate your SAP, so be careful. You probably want to set `autoRun` to `false` (see below).

Configuration
=============

`sapExecutable` the SAP executable. Example: `C:/Program Files (x86)/SAP/FrontEnd/SAPgui/saplogon.exe`

`autoHotkeyExecutable` the AutoHotkey executable. Example: `C:\\Program Files\\AutoHotkey\\AutoHotkey.exe`

`outputScriptFilename` full filename to write the created script to. Example: `C:/Users/MyUser/Desktop/DoTheSap.ahk`

`username` the SAP username. Example: `my_sap_username`

`password` the SAP password. Example: `secret_pwd`

`autoRun` boolean indicating whether to run the created script right away. Example: `false`

`printProjectMapping` boolean indicating whether to stdout the generated (project name => psp element name) mapping. Example: `true`

`projectMapping` mapping from project names to psp element names. Exampl: `{"My Project 1" : "PSP XYZ", "My Other Project" : "Some other PSP"}`

Build
=====
see https://github.com/sne11ius/stunden
