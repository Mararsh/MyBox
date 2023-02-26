# Unzip source package. Edit this script to change directories as your env.
# In terminal window, enter path "MyBox/pack/linux", and run this script.
export JAVA_HOME="/usr/java/jdk-19.0.2"
export CLASSPATH=".;$JAVA_HOME/lib" 
export PATH="$JAVA_HOME/bin;$PATH"
export jpackagePath="%JAVA_HOME%/bin"

rm -rf app/*
rm -rf jar/*
mkdir  app
mkdir  jar
cd ../..
mvn clean
mvn -P linux package
cd pack/linux
mv ../../target/MyBox.jar  jar/   &&  \
$jpackagePath/jpackage  --type  app-image --vendor Mara  --verbose  --runtime-image $JAVA_HOME  --dest app --name  MyBox  --input jar --main-jar  MyBox.jar  --icon res/MyBox.png 
mv jar/MyBox.jar  .
gzip MyBox.jar 
mv MyBox.jar.gz  MyBox-linux.jar.gz
cd  app
tar cfz  MyBox-CentOS7-x64.tar.gz  MyBox
cd ..
rm -rf ../../target
