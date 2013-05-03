@echo off 
setlocal enableDelayedExpansion
if "%OS%" == "Windows_NT" setlocal
set "QUICKQUEST_PROG_DIR=%cd%"
set "JAVA_LIBRARY_PATH=%QUICKQUEST_PROG_DIR%\lib"
cd "%QUICKQUEST_PROG_DIR%"
set JAVA_CLASSPATH=quickquest-win32.jar;
for /r . %%g in (.\lib\*.jar) do (
	set "JAVA_CLASSPATH=!JAVA_CLASSPATH!;lib\%%~nxg"
)
echo "QuickQuest Program Path:" %QUICKQUEST_PROG_DIR%
echo "Class Path:" %JAVA_CLASSPATH%

start javaw -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9898 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=127.0.0.1 -Djava.library.path="%JAVA_LIBRARY_PATH%" -Dquickquest.prog.dir="%QUICKQUEST_PROG_DIR%" -cp %JAVA_CLASSPATH% net.ubuntudaily.quickquest.Main