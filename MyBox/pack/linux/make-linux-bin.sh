# Unzip source package. Edit this script to change directories as your env.
# In terminal window, enter path "MyBox/pack/linux", and run this script.
version=6.3.5
jpackagePath=/home/mara/jdk-14/bin
jdkPath=/usr/java/jdk-14.0.1

rm -rf app/*
cd ../..
mvn clean
mvn -P linux package
cd pack/linux
$jpackagePath/jpackage  --package-type  app-image --app-version $version --vendor Mara  --verbose  --runtime-image $jdkPath  --dest app --name  MyBox  --input ../../target --main-jar  MyBox-$version.jar  --icon res/MyBox.png 
cd  app
tar cfz  MyBox-$version-linux.tar.gz  MyBox
mv MyBox*.gz ..
cd ..
cp ../../target/*.jar  .
