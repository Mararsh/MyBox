
## The generated dmg installs correct;y but can not start MyBox~ Will check later 

rm -rf   /users/rsh/mybox-pack/out
mkdir /users/rsh/mybox-pack/out
/Library/Java/JavaVirtualMachines/jdk1.8.0_221.jdk/Contents/Home/bin/javapackager -deploy -native dmg -appclass mara.mybox.MyBox -srcdir  /users/rsh/mybox-pack/jar  -srcfiles MyBox-5.3.jar  -outdir  /users/rsh/mybox-pack/out  -outfile  MyBox -name MyBox -Bicon=/users/rsh/mybox-pack/res/MyBox.ico
#cp  /mybox/res/0*.*   /mybox/our/bundles/MyBox


