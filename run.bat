@echo off
REM Kafka Streams MSK Example - Run Script for Windows

echo === Kafka Streams MSK Example ===
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if errorlevel 1 (
    echo Maven not found in PATH, using Maven wrapper...
    set MAVEN_CMD=mvnw.cmd
) else (
    echo Maven found in PATH
    set MAVEN_CMD=mvn
)

echo Java version:
java -version 2>&1 | findstr "version"
echo Maven command: %MAVEN_CMD%
echo.

REM Create state directory (Windows equivalent)
echo Creating state directory...
if not exist "C:\tmp\kafka-streams-state" mkdir "C:\tmp\kafka-streams-state"

REM Build the project
echo Building the project...
call %MAVEN_CMD% clean package

if errorlevel 1 (
    echo Error: Build failed
    pause
    exit /b 1
)

echo.
echo Build successful!
echo.

REM Check if JAR file exists
if not exist "target\kafka-streams-msk-1.0.0.jar" (
    echo Error: JAR file not found. Build may have failed.
    pause
    exit /b 1
)

echo === Available Commands ===
echo 1. Run Streams Application:
echo    java -jar target\kafka-streams-msk-1.0.0.jar
echo.
echo 2. Send Test Messages:
echo    %MAVEN_CMD% exec:java -Dexec.mainClass="com.example.kafkastreams.TestProducer"
echo.
echo 3. View Word Count Results:
echo    %MAVEN_CMD% exec:java -Dexec.mainClass="com.example.kafkastreams.TestConsumer" -Dexec.args="output-topic"
echo.
echo 4. View Important Messages:
echo    %MAVEN_CMD% exec:java -Dexec.mainClass="com.example.kafkastreams.TestConsumer" -Dexec.args="important-messages-topic"
echo.

REM Ask user what to do
echo What would you like to do?
echo 1) Run the Streams Application
echo 2) Send Test Messages
echo 3) View Word Count Results
echo 4) View Important Messages
echo 5) Exit
echo.
set /p choice="Enter your choice (1-5): "

if "%choice%"=="1" (
    echo Starting Kafka Streams Application...
    java -jar target\kafka-streams-msk-1.0.0.jar
) else if "%choice%"=="2" (
    echo Sending test messages...
    call %MAVEN_CMD% exec:java -Dexec.mainClass="com.example.kafkastreams.TestProducer"
) else if "%choice%"=="3" (
    echo Starting consumer for word count results...
    call %MAVEN_CMD% exec:java -Dexec.mainClass="com.example.kafkastreams.TestConsumer" -Dexec.args="output-topic"
) else if "%choice%"=="4" (
    echo Starting consumer for important messages...
    call %MAVEN_CMD% exec:java -Dexec.mainClass="com.example.kafkastreams.TestConsumer" -Dexec.args="important-messages-topic"
) else if "%choice%"=="5" (
    echo Exiting...
    exit /b 0
) else (
    echo Invalid choice. Exiting...
    pause
    exit /b 1
)

pause 