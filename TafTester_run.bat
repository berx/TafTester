REM TafTester_run.bat

REM this is fine for C001904
set JAVA_HOME="C:\Program Files\Java\jdk1.7.0_01"
set ORACLE_HOME=C:\oracle\product\client_11.2.0.2

set path=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=.\sk\diko\manageFiles.jar;%ORACLE_HOME%\jdbc\lib\ojdbc6.jar;%CLASSPATH%

java TafTester.TafTester cfg=VAX_thin.cfg

pause
