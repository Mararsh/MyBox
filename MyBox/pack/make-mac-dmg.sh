version=6.3.1
cd MyBox
mvn clean
mvn -P mac package
cd ..
rm  -rf src/*.jar
rm -rf  out/*
cp  MyBox/target/*.jar  src/MyBox-$version.jar
sleep 15
jpackage  --type dmg --app-version $version --vendor Mara  --verbose  --runtime-image /Library/Java/JavaVirtualMachines/jdk-14.0.1.jdk/Contents/Home  --dest  out   --name  MyBox  --input  src  --main-jar  MyBox-$version.jar  --icon res/MyBox.icns
cp  MyBox/target/*.jar .
