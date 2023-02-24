rem Unzip source package. Edit this script to change directories as your env.
set jpackagePath=D:\Programs\Java\openjdk-19.0.2\bin
set jdkPath=D:\Programs\Java\openjdk-19.0.2

rd/Q/S    app\
rd/Q/S    jar\
md  app  jar
cd ../..
call mvn clean
call mvn package
call move target\MyBox.jar  pack\win\jar\
cd pack\win
%jpackagePath%\jpackage --type app-image --vendor Mara --verbose --runtime-image  %jdkPath%  --dest app --name MyBox --input jar --main-jar MyBox.jar --icon res\MyBox.ico
move jar\*.jar  .
rd/Q/S    target\
