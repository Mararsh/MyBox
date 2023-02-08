# Unzip source package. Edit this script to change directories as your env.
# In terminal window, enter path "MyBox/pack/mac", and run this script.
jpackagePath=/Library/Java/JavaVirtualMachines/jdk-18.jdk/Contents/Home/bin
jdkPath=/Library/Java/JavaVirtualMachines/jdk-18.jdk/Contents/Home

rm -rf app/*
rm -rf jar/*
mkdir  app
mkdir  jar
cd ../..
mvn clean
mvn -P mac package
cd pack/mac
mv ../../target/MyBox.jar  jar/   &&  \
$jpackagePath/jpackage  --type dmg --vendor Mara  --verbose  --runtime-image $jdkPath  --dest app  --name  MyBox  --input jar --main-jar  MyBox.jar  --icon res/MyBox.icns
mv jar/MyBox.jar  .
gzip MyBox.jar 
mv MyBox.jar.gz  MyBox-mac.jar.gz
mv app/MyBox.dmg  MyBox-mac.dmg
rm -rf ../../target
