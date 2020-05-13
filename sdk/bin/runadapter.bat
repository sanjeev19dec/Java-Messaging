echo off

set RV_JAR=%TIB_RV_HOME%\lib\tibrvj.jar
set ORACLE_JAR=..\lib\ojdbc14.jar
set SDK_JAR=..\lib\uti_sdk.jar

set CP=%CLASSPATH%;%RV_JAR%;%ORACLE_JAR%;%SDK_JAR%

set LOG_CFG=-Djava.util.logging.config.file=..\etc\uti_sdk_logging.properties

java -classpath %CP% %LOG_CFG% uti.nextgen.adapter.Adapter ..\etc\adapter.xml

exit 0
