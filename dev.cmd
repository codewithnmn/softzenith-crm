@echo off
rem Local dev environment. Usage: dev [start|stop|status] [-All]   (see scripts\dev.ps1)
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\dev.ps1" %*
