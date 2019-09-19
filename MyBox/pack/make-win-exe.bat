rd/Q/S    D:\tmp\make-mybox\out\*.*
del/Q/F   D:\tmp\make-mybox\src\*.jar
copy   D:\MyBox\target\MyBox-5.5-jar-with-dependencies.jar    D:\tmp\make-mybox\src\MyBox-5.5.jar
D:\Programs\jdk-14\bin\jpackage  --app-version 5.5 --vendor Mara --verbose --runtime-image  D:\Programs\Java\openjdk-12  --output D:\tmp\make-mybox\out  --name MyBox --input D:\tmp\make-mybox\src --main-jar MyBox-5.5.jar  --icon D:\tmp\make-mybox\res\MyBox.ico

