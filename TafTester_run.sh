#!/bin/bash 
#
# TafTester_run.sh
# 

CF_thin=VAX_thin.cfg
CF_oci=VAX_oci.cfg

# 32 or 64 bits? 
unset BITS
while [ -z $BITS ] ; do
  read -p "Do you prefer 32 or 64 bit? [36] "  BIT  # -i "3" BIT
#  echo $BIT
  case $BIT in
        [32]* ) BITS=32;;
        [64]* ) BITS=64;;
        * ) echo "Please answer [3]2 or [6]4";;
    esac
done

#echo $BITS

# setup for findling
# no JAVA_HOME needed
#set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_01
export ORACLE_HOME=/Users/berx/TafTester/instantclient_10_2_$BITS
#set path=%JAVA_HOME%\bin;%ORACLE_HOME%\bin;%PATH%
export PATH=$PATH:$ORACLE_HOME
#set CLASSPATH=.:.\sk\diko\manageFiles.jar;%ORACLE_HOME%\jdbc\lib\ojdbc6.jar;%CLASSPATH%
export CLASSPATH=$CLASSPATH:.:$ORACLE_HOME:$ORACLE_HOME/classes12.jar

unset CONFIG_FILE
unset jdbc
while [ -z $CONFIG_FILE ] ; do
  read -p "Do you prefer [t]hin or [o]ci ? "  jdbc
  echo $jdbc
  case $jdbc in
    [thn]* ) CONFIG_FILE=$CF_thin ;;
    [oc]* )  CONFIG_FILE=$CF_oci ;; 
  esac
done

echo $CONFIG_FILE 

java -d$BITS TafTester.TafTester cfg=$CONFIG_FILE

