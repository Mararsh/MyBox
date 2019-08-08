
## ???? this does not work on CentOS~ Will check later 

rm -rf   /mybox/out
mkdir /mybox/out
/usr/java/jdk1.8.0_221-amd64/bin/javapackager  -deploy -native rpm -appclass mara.mybox.MyBox -srcdir  /mybox/jar -srcfiles MyBox-5.3.jar -outdir  /mybox/out  -outfile  MyBox -name MyBox -Bicon=/mybox/res/MyBox.ico
cp  /mybox/res/0*.*   /mybox/our/bundles/MyBox


