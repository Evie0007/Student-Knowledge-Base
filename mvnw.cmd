@echo off
rem Maven Wrapper: runs Maven without installing it. Usage: mvnw.cmd clean javafx:run
setlocal
set "MAVEN_HOME="
for /f "usebackq delims=" %%i in (`powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0.mvn\wrapper\mvnw.ps1"`) do set "MAVEN_HOME=%%i"
if not defined MAVEN_HOME (
  echo Could not set up Maven. See the error above. 1>&2
  exit /b 1
)
call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
