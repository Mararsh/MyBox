rem Unzip source package. Edit this script to change directories as your env.
set version=6.3.4
set jpackagePath=D:\Programs\Java\openjdk-14.0.1\bin
set jdkPath=D:\Programs\Java\openjdk-14.0.1

rd/Q/S    app\
del *.jar
cd ../..
call mvn clean
call mvn package
cd pack\win
%jpackagePath%\jpackage --type app-image   --app-version %version% --vendor Mara --verbose --runtime-image  %jdkPath%  --dest app --name MyBox --input ..\..\target --main-jar MyBox-%version%.jar --icon res\MyBox.ico
copy res\*.txt app\MyBox
copy ..\..\target\*.jar  .

