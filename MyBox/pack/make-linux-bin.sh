version=5.7
cd MyBox
mvn clean
mvn -P linux package
cd ..
rm  -rf src/*
rm -rf  out/*
cp  MyBox/target/*.jar  src/MyBox-$version.jar
sleep 15
../jdk-14/bin/jpackage  --app-version $version --vendor Mara  --verbose  --runtime-image   /usr/java/jdk-12.0.1  --output  out   --name  MyBox  --input  src  --main-jar  MyBox-$version.jar  --icon res/MyBox.png 
cd  out
tar cfz  MyBox-$version-linux.tar.gz  MyBox
mv MyBox*.gz ..
cd ..
cp  MyBox/target/*.jar  ./MyBox-$version.jar
