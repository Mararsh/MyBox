# Unzip source package. Edit this script to change directories as your env.
# In terminal window, enter path "MyBox/pack/mac", and run this script.
version=6.3.5
jpackagePath=/Library/Java/JavaVirtualMachines/jdk-14.0.1.jdk/Contents/Home/bin
jdkPath=/Library/Java/JavaVirtualMachines/jdk-14.0.1.jdk/Contents/Home

rm -rf jar/*.jar
rm -rf app/*
cd ../..
mvn clean
mvn -P mac package
cd pack/mac
$jpackagePath/jpackage  --type dmg --app-version $version --vendor Mara  --verbose  --runtime-image $jdkPath  --dest app  --name  MyBox  --input ../../target  --main-jar  MyBox-$version.jar  --icon res/MyBox.icns
cp ../../target/*.jar  .
