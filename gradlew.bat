@echo off
setlocal
set "GRADLE_VERSION=8.8"
if not defined GRADLE_USER_HOME set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
set "BASE=%GRADLE_USER_HOME%\nova-bootstrap"
set "DIST=%BASE%\gradle-%GRADLE_VERSION%"
set "ZIP=%BASE%\gradle-%GRADLE_VERSION%-bin.zip"
if exist "%DIST%\bin\gradle.bat" goto runGradle
if not exist "%BASE%" mkdir "%BASE%"
if not exist "%ZIP%" (
  echo NOVA: downloading Gradle %GRADLE_VERSION% (first run only)...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP%'"
  if errorlevel 1 exit /b 1
)
echo NOVA: extracting Gradle %GRADLE_VERSION%...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%BASE%' -Force"
if errorlevel 1 exit /b 1
:runGradle
call "%DIST%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
