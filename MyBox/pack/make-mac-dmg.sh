version=5.7
cd MyBox
mvn clean
mvn -P mac package
cd ..
rm  -rf src/*.jar
rm -rf  out/*
cp  MyBox/target/*.jar  src/MyBox-$version.jar
sleep 15
jdk-14/contents/Home/bin/jpackage --package-type dmg --app-version $version --vendor Mara  --verbose  --runtime-image /usr/local/openjdk-12.0.2/Contents/Home  --output  out   --name  MyBox  --input  src  --main-jar  MyBox-$version.jar  --icon res/MyBox.icns
cp  MyBox/target/*.jar .
