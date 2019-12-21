set version=5.9
rd/Q/S    D:\tmp\make-mybox\out\
del/Q/F   D:\tmp\make-mybox\src\*.jar
copy   D:\MyBox\target\MyBox-%version%.jar    D:\tmp\make-mybox\src\MyBox-%version%.jar
D:\Programs\jdk-14\bin\jpackage  --package-type app-image  --app-version %version% --vendor Mara --verbose --runtime-image  D:\Programs\Java\openjdk-13.0.1  --dest D:\tmp\make-mybox\out --name MyBox --input D:\tmp\make-mybox\src --main-jar MyBox-%version%.jar  --icon D:\tmp\make-mybox\res\MyBox.ico
copy   D:\tmp\make-mybox\res\*.txt    D:\tmp\make-mybox\out\MyBox


