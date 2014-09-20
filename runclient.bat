@ECHO OFF

set JDK_HOME05="C:\Program Files\Java\jdk1.8.0_20\bin"
set JDK_HOME05_="C:\Program Files (x86)\Java\jdk1.8.0_20\bin"

SET JARPATH="%CD%\tron.jar;%CD%\lib\*"
SET CLASS_PATH="%CD%\tronplayer"
SET BUILD_CLASS_PATH=%JARPATH%;%CLASS_PATH%
SET SRC_PATH="%CD%\tronplayer\PlayerAI.java"


echo [Checking PATH Variable]
java.exe -version >nul 2>&1
if ERRORLEVEL 1 goto :DefaultHome
ECHO [Java Found]
java.exe -classpath %BUILD_CLASS_PATH% RunClient %*
goto :error_msg

:DefaultHome
echo [Checking JDK_HOME Variable]
IF DEFINED JDK_HOME "%JDK_HOME%"\bin\java.exe -version >nul 2>&1
if ERRORLEVEL 1 goto :UserHome05
ECHO [Java Found]
"%JDK_HOME%"\bin\java.exe -classpath %BUILD_CLASS_PATH% RunClient %*
goto :error_msg

:UserHome05
echo [Checking Default JDK 1.8.0_20 Folder]
IF DEFINED JDK_HOME05 %JDK_HOME05%\java.exe -version >nul 2>&1
if ERRORLEVEL 1 goto :UserHome05_
ECHO [Java Found]
%JDK_HOME05%\java.exe -classpath %BUILD_CLASS_PATH% RunClient %*
goto :error_msg


:UserHome05_
IF DEFINED JDK_HOME05_ %JDK_HOME05_%\java.exe -version >nul 2>&1
if ERRORLEVEL 1 goto :error_msg
ECHO [Java Found]
%JDK_HOME05_%\java.exe -classpath %BUILD_CLASS_PATH% RunClient %*
goto :error_msg

:usage
echo java.exe was not found on your path!
echo This script requires:
echo 1) an installation of the Java Development Kit, and
echo 2) either java.exe is on the path, or that the JDK_HOME environment variable 
echo is set and points to the installation directory. By default it will look under
echo C:\Program Files\Java\jdk1.8.0_20\bin

:error_msg
echo press enter to exit
runas /user:# "" >nul 2>&1
