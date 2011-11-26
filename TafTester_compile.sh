#!/bin/bash
# TafTester_compile.bat

# no JAVA_HOME needed on findling
#set JAVA_HOME="C:\Program Files\Java\jdk1.7.0_01"

#set path=%JAVA_HOME%\bin;%PATH%
#set CLASSPATH=.\sk\diko\manageFiles.jar;%CLASSPATH%

javac TafTester.java

mkdir TafTester 

mv TafTester.class .\TafTester

