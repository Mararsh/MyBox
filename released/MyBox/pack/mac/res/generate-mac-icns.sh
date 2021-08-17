# https://stackoverflow.com/questions/12306223/how-to-manually-create-icns-files-using-iconutil
rm -rf MyBox.iconset
mkdir MyBox.iconset
cd MyBox.iconset
sips -z 16 16     ../MyBox.png  --out icon_16x16.png
sips -z 16 16     ../MyBox.png --out icon_16x16.png
sips -z 32 32     ../MyBox.png --out icon_16x16@2x.png
sips -z 32 32     ../MyBox.png --out icon_32x32.png
sips -z 64 64     ../MyBox.png --out icon_32x32@2x.png
sips -z 64 64     ../MyBox.png --out icon_64x64.png
sips -z 128 128   ../MyBox.png --out icon_64x64@2x.png
sips -z 128 128   ../MyBox.png --out icon_128x128.png
sips -z 256 256   ../MyBox.png --out icon_128x128@2x.png
sips -z 256 256   ../MyBox.png --out icon_256x256.png
sips -z 512 512   ../MyBox.png --out icon_256x256@2x.png
sips -z 512 512   ../MyBox.png --out icon_512x512.png
sips -z 1024 1024   ../MyBox.png --out icon_512x512@2x.png
cd ..
iconutil -c icns -o MyBox.icns MyBox.iconset
rm -rf MyBox.iconset

