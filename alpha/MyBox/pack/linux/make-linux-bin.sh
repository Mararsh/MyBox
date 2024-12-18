# Unzip source package. Edit this script to change JAVA_HOME as your env.
# In terminal window, enter path "MyBox/pack/linux", and run this script.
export JAVA_HOME="/usr/java/jdk-23.0.1"

rm -rf app/*
rm -rf jar/*
mkdir  app
mkdir  jar
cd ../..
mvn clean
mvn -P linux package
cd pack/linux
mv ../../target/MyBox.jar  jar/   &&  \
$JAVA_HOME/bin/jpackage  --type  app-image --vendor Mara  --verbose  --runtime-image $JAVA_HOME  --dest app --name  MyBox  --input jar --main-jar  MyBox.jar  --icon res/MyBox.png
mv jar/MyBox.jar  .
gzip MyBox.jar
mv MyBox.jar.gz  MyBox-linux.jar.gz
cd  app
tar cfz  MyBox-CentOS7-x64.tar.gz  MyBox
cd ..
mv app/MyBox-CentOS7-x64.tar.gz  .
rm -rf ../../target