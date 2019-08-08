rd/Q/S   d:\tmp\make-mybox\out
copy   D:\MyBox\target\*.jar    d:\tmp\make-mybox\jar
javapackager -deploy -native image -appclass mara.mybox.MyBox -srcdir D:\tmp\make-mybox\jar  -outdir D:\tmp\make-mybox\out -outfile MyBox -name MyBox -Bicon=D:\tmp\make-mybox\src\MyBox.ico
copy D:\tmp\make-mybox\src\0*.*   D:\tmp\make-mybox\out\bundles\MyBox

