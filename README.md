# Treasure-Hunt
MMNET Project Team 04

In order to setup this project you should use Eclipse IDE (with Android SDK). Make sure you also have OpenCV for Android SDK installed. Follow this guide in order to have all the environment properly set:

http://docs.opencv.org/doc/tutorials/introduction/android_binary_package/android_dev_intro.html#android-dev-intro

In order to import the project, create a new Eclipse workspace that must contain:

-Treasure Hunt folder
-Appcompat_v7 folder
-OpenCV-android-sdk folder

Then: file-->import-->existing projects into workspace and import (one at a time) all the three previously mentioned folders as root directories.

After that, check if project-->properties-->android "Library" section contains the required references to appcompat_v7 and OpenCV library.

The JAVADOC documentation is contained into the /doc folder. 

Under the /bin folder you will find directly the APK. When you install for the first time our app on your device, you will be asked to download the OpenCV manager from playstore. This is a small (6MB) app that simply contains all the required OpenCV libraries that run on your device. 

If you have the Developer Options activated on your device, make sure that the voice "Don't keep activities" is unchecked. 

-->Enjoy!
