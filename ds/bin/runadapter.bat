echo off

set RV_JAR=%TIB_RV_HOME%\lib\tibrvj.jar
set UTI_SDK_JAR=..\lib\uti_sdk.jar
set DS_JAR=..\lib\uti_ds.jar

set CP=%CLASSPATH%;%RV_JAR%;%UTI_SDK_JAR%;%DS_JAR%

set LOG_CFG=-Djava.util.logging.config.file=..\etc\logging.properties

java -classpath %CP% %LOG_CFG% uti.nextgen.adapter.Adapter ..\etc\adapter.xml
