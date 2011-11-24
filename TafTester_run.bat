echo off
REM TafTester_run.bat
REM this is fine for C001904
set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_01
set ORACLE_HOME=C:\oracle\product\client_11.2.0.2


set path=%JAVA_HOME%\bin;%ORACLE_HOME%\bin;%PATH%
set CLASSPATH=.\sk\diko\manageFiles.jar;%ORACLE_HOME%\jdbc\lib\ojdbc6.jar;%CLASSPATH%
set CONFIG_FILE=dummy
set CF_thin=VAX_thin.cfg
set CF_oci=VAX_oci.cfg

choice /C TO /T 60 /D T /M "Please chose [T]hin or [O]CI driver "

IF ERRORLEVEL 1 (
  set CONFIG_FILE=%CF_thin% 
) 
IF ERRORLEVEL 2 (
  set CONFIG_FILE=%CF_oci% 
)
echo using %CONFIG_FILE%

java -Duser.language=en  TafTester.TafTester cfg=%CONFIG_FILE%


echo on
pause