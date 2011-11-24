REM TafTester_compile.bat

REM this is fine for C001904
set JAVA_HOME="C:\Program Files\Java\jdk1.7.0_01"

set path=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=.\sk\diko\manageFiles.jar;%CLASSPATH%

javac TafTester.java

pause
