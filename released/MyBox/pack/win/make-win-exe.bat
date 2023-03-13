rem Unzip source package. Edit this script to change directories as your env.
set JAVA_HOME=D:\Programs\Java\openjdk-19.0.2
set CLASSPATH=.;%JAVA_HOME%/lib; 
set PATH=%JAVA_HOME%/bin;%PATH%
set jpackagePath=%JAVA_HOME%/bin

rd/Q/S    app\
rd/Q/S    jar\
md  app  jar
cd ../..
call mvn clean
call mvn package
call move target\MyBox.jar  pack\win\jar\
cd pack\win
%jpackagePath%\jpackage --type app-image --vendor Mara --verbose --runtime-image  %JAVA_HOME%  --dest app --name MyBox --input jar --main-jar MyBox.jar --icon res\MyBox.ico
move jar\*.jar  .
rd/Q/S    target\
