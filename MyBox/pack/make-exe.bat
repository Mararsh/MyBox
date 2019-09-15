rd/Q/S   d:\tmp\make-mybox\out
del/Q/F   d:\tmp\make-mybox\jar\*.*
copy   D:\MyBox\target\*.jar    d:\tmp\make-mybox\jar
javapackager -deploy -native image -appclass mara.mybox.MyBox -srcdir D:\tmp\make-mybox\jar  -outdir D:\tmp\make-mybox\out -outfile MyBox -name MyBox -Bicon=D:\tmp\make-mybox\src\MyBox.ico
copy D:\tmp\make-mybox\src\0*.*   D:\tmp\make-mybox\out\bundles\MyBox
copy D:\Programs\Java\openjdk-12\bin\java.exe   D:\tmp\make-mybox\out\bundles\MyBox\runtime\bin

