# Unzip source package. Edit this script to change directories as your env.
# In terminal window, enter path "MyBox/pack/linux", and run this script.
version=6.4.7
jpackagePath=/usr/java/jdk-16/bin
jdkPath=/usr/java/jdk-16

rm -rf app/*
rm -rf jar/*
mkdir  app
mkdir  jar
cd ../..
mvn clean
mvn -P linux package
cd pack/linux
mv ../../target/*.jar  jar/   &&  \
$jpackagePath/jpackage  --type  app-image --app-version $version --vendor Mara  --verbose  --runtime-image $jdkPath  --dest app --name  MyBox  --input jar --main-jar  MyBox-$version.jar  --icon res/MyBox.png 
cd  app
tar cfz  MyBox-$version-linux.tar.gz  MyBox
mv MyBox*.gz ..
cd ..
mv jar/*.jar  .
rm -rf ../../target
