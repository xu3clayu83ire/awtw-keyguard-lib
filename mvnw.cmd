@echo off
setlocal

set BASE_DIR=%~dp0
set WRAPPER_DIR=%BASE_DIR%.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set PROPERTIES_FILE=%WRAPPER_DIR%\maven-wrapper.properties

if not exist "%PROPERTIES_FILE%" (
  echo Missing %PROPERTIES_FILE%
  exit /b 1
)

for /f "tokens=1,* delims==" %%A in (%PROPERTIES_FILE%) do (
  if "%%A"=="wrapperUrl" set WRAPPER_URL=%%B
)

if not defined WRAPPER_URL (
  echo Missing wrapperUrl in %PROPERTIES_FILE%
  exit /b 1
)

if not exist "%WRAPPER_JAR%" (
  where curl >nul 2>nul
  if errorlevel 1 (
    echo curl is required to download Maven Wrapper.
    exit /b 1
  )
  curl -fsSL "%WRAPPER_URL%" -o "%WRAPPER_JAR%"
)

java "-Dmaven.multiModuleProjectDirectory=%BASE_DIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
endlocal