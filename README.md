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

- Before, check the [signaling](https://github.com/KarinaRomero/signaling) project and make sure it run.
- Then, open the project add the custom URL signaling into appFolder > app > src > main > sfrtcandroidapp > DemoActivity.java in this line:

    `63 private String wsuri = "ws://your.url.signaling";`

- Finally configure a device and you will Run app.

## Usages

To create a [simple mirror](https://github.com/KarinaRomero/sfRTC-android-framework/blob/master/app/src/main/java/com/karinaromero/sfrtcandroidapp/MirrorDemoActivity.java) 

## License

This framework is licenced under [MIT Licence](https://opensource.org/licenses/MIT) 
