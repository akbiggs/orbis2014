@ECHO OFF

set JDK_HOME05="C:\Program Files\Java\jdk1.8.0_20\bin"
set JRE8="C:\Program Files\Java\jre8\bin"

set JDK_HOME86_05="C:\Program Files (x86)\Java\jdk1.8.0_20\bin"
set JRE868="C:\Program Files (x86)\Java\jre8\bin"

SET JARPATH="%CD%\tron.jar;%CD%\lib\*;%CD%\lib\tronclient.jar"
SET CLASS_PATH="%CD%\tronplayer"
SET BUILD_CLASS_PATH=%JARPATH%;%CLASS_PATH%
SET SRC_PATH="%CD%\tronplayer\PlayerAI.java"

echo [Checking PATH Variable]
javac.exe -version >nul 2>&1
if ERRORLEVEL 1 goto :DefaultHome
echo [Found `]
set JAVA=javac.exe
goto :JavaFound

:DefaultHome
echo [Checking JDK_HOME Variable]
IF DEFINED JDK_HOME "%JDK_HOME%"\bin\javac.exe -version >nul 2>&1
if ERRORLEVEL 1 goto :UserHome05
echo [Found Java]
set JAVA="%JDK_HOME%"\bin\javac.exe
set PATH=%Path%;%JDK_HOME%\bin
goto :JavaFound

:UserHome05
echo [Checking Default JDK 1.8.0_20 Folder]
IF DEFINED JDK_HOME05 %JDK_HOME05%\javac.exe -version >nul 2>&1
if ERRORLEVEL 1 goto :usage
echo [Found Java]
set JAVA=%JDK_HOME05%\javac.exe
set PATH=%Path%;%JDK_HOME05%
goto :JavaFound


:usage
echo javac.exe was not found on your path!
echo This script requires:
echo 1) an installation of the Java Development Kit, and
echo 2) either javac.exe is on the path, or that the JDK_HOME environment variable 
echo is set and points to the installation directory. By default it will look under
echo C:\Program Files\Java\jdk1.8.0_20\bin

:JavaFound
if exist %CLASS_PATH%\*.class (
  del /Q %CLASS_PATH%\*.class
)

%JAVA% -classpath %BUILD_CLASS_PATH% %SRC_PATH% -verbose 2>&1
if ERRORLEVEL 1 goto :errorend
echo ==================================
echo COMPILATION COMPLETED SUCCESSFULLY 
echo ==================================
goto :eof

:errorend
echo ==================================
echo COMPILATION FAILED
echo Check above to find the compilation errors
echo ==================================
runas /user:# "" >nul 2>&1