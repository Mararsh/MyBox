# Unzip source package. Edit this script to change JAVA_HOME as your env.
# In terminal window, enter path "MyBox/pack/mac", and run this script.
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-22.0.1.jdk/Contents/Home"

rm -rf app/*
rm -rf jar/*
mkdir  app
mkdir  jar
cd ../..
mvn clean
mvn -P mac package
cd pack/mac
mv ../../target/MyBox.jar  jar/   &&  \
$JAVA_HOME/bin/jpackage  --type dmg --vendor Mara  --verbose  --runtime-image $JAVA_HOME  --dest app  --name  MyBox  --input jar --main-jar  MyBox.jar  --icon res/MyBox.icns
mv jar/MyBox.jar  ./MyBox.jar
gzip MyBox.jar 
mv MyBox.jar.gz  MyBox-mac.jar.gz
mv app/MyBox-1.0.dmg  MyBox-mac.dmg
rm -rf ../../target