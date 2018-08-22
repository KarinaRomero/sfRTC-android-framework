# sfRTC-android-framework

This is a framework to simplify implementation webRTC protocol to android apps.

## Tools

I recommend to use Android Studio.

## Generate module library

- If you have Android studio, then open the Gradle tasks, expand :sfrtc and build.

- Synchronize assembleRelease task and wait the process finished.

- Next, navigate into project folder appFolder>sfrtc>build>outputs>aar

## Add library to your project

- Firstly create a new Android Studio Project 

- Next into your project select option New Module, File>New>New Module and select import .JAR/.AAR package option and then click Next.

- After select .aar file and click Finish.

- Finally add the next lines into the file build.gradle module app in the section dependences

    `implementation project(':sfrtc')`

    `implementation 'org.webrtc:google-webrtc:1.0.23295'`

## Run demo 

Bbefore

Open the project and configure a device before you will Run app.

## Usages

To create a [simple mirror](https://github.com/KarinaRomero/sfRTC-android-framework/blob/master/app/src/main/java/com/karinaromero/sfrtcandroidapp/MirrorDemoActivity.java) 

## License

Copyright 2018  Karina Betzabe Romero Ulloa
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
