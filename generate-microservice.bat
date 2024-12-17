@echo off
setlocal

:: Set JAVA_HOME if not already set
IF NOT DEFINED JAVA_HOME (
    echo JAVA_HOME is not set. Please set JAVA_HOME.
    exit /b
)

:: Path to the JAR file
set JAR_PATH=SpringBootMicroserviceGenerator-1.0.jar

:: Run the Java application
java -jar "%~dp0%JAR_PATH%"
pause
