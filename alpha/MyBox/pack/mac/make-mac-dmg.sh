# Unzip source package. Edit this script to change directories as your env.
# In terminal window, enter path "MyBox/pack/mac", and run this script.
version=6.7.1
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
mv ../../target/*.jar  jar/   &&  \
$jpackagePath/jpackage  --type dmg --app-version $version --vendor Mara  --verbose  --runtime-image $jdkPath  --dest app  --name  MyBox  --input jar --main-jar  MyBox-$version.jar  --icon res/MyBox.icns
mv jar/*.jar  .
rm -rf ../../target
