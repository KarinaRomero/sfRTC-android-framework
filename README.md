# sfRTC-android-framework

This is a framework to simplify implementation WebRTC protocol to android apps.

## Tools

I recommend to use Android Studio.

## Generate module library

- If you have Android studio, then open the Gradle tasks, expand :sfrtc and build options.

- Synchronize assembleRelease task and wait the process finished.

- Next, navigate into project folder appFolder > sfrtc > build > outputs > aar.

## Add library to your project

- Firstly create a new Android Studio Project.

- Next into your project select option New Module, File > New > New Module and select import .JAR/.AAR package option and then click Next.

- After select .aar file and click Finish.

- Finally add the next lines into the file build.gradle module app in the section dependences :

  `implementation project(':sfrtc')`

  `implementation 'org.webrtc:google-webrtc:1.0.23295'`

Note: This is a little guide, for more information about Android libraries click [here](https://developer.android.com/studio/projects/android-library?hl=es-419).

## Usages

To implement the framework you must do the following:

*Note: Before make sure the signaling channel is running, to more information go to [https://github.com/KarinaRomero/signaling](https://github.com/KarinaRomero/signaling).

- To show local and remote video, add the following structure in your XML file :

```XML
<!--To show local video-->
<org.webrtc.SurfaceViewRenderer
        android:id="@+id/fullscreen_video_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        tools:layout_editor_absoluteX="0dp"
        tools:layout_editor_absoluteY="0dp" />

<!--To show remote video-->
    <org.webrtc.SurfaceViewRenderer
        android:id="@+id/pip_video_view"
        android:layout_width="wrap_content"
        android:layout_height="144dp"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp" />
```

- Create the following class to establish the frames :

```Java
private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }
```

- In your java Activity class create the following variables :

```Java
public class JavaActivity extends AppCompatActivity {

    // To establish the video decoder
    private static final String VIDEO_CODEC_VP9 = "VP9";
    // To establish the audio decoder
    private static final String AUDIO_CODEC_OPUS = "opus";

    // To establish the video frames local and remote
    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();

    // To render the video local and remote in the view
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRender;

    // Your URL signaling
    private String wsuri = "ws://your.url.signaling";

    // The id user logged and id remote user
    private String username = "I";
    private String callName = "YOU";

    // To create a clientWebRTC
    private WebRTCClient client;

    // To configure the parameters to send video and audio
    private PeerConnectionParameters params;

    // To configure the parameters to send data
    private DataChannelParameters dataChannelParameters

    ...
}
```

- In the onCreate method, you should initialize the following :

*Note: For this example, buttons were added to make the call , hang up, answer and send messages.

```Java
@Override
    protected void onCreate(Bundle savedInstanceState) {

        ...

        // Button to call
        btnCall.setOnClickListener(view -> {
            // This method allows you to call to other user, receive the name or id of string type
            client.call(callName);
        });

        // Answer button
        btnAnswer.setOnClickListener(view -> {
            // This method allows you to answer
            client.answer();
        });

        // Hang up button
        btnHangUp.setOnClickListener(view -> {
            // This method allows you to hang up a call
            client.hangUp();
        });

        // Button to send message
        btnSendMessage.setOnClickListener(view -> {
            String message = edtMessage.getText().toString();
            // This method allow you to send message, receive a string type value
            client.sendMessage(message);
        });

        // Create a eglBase to draw video with OpenGL
        final EglBase eglBase = EglBase.create();

        // Initialize the view render with OpenGL context.
        remoteRender.init(eglBase.getEglBaseContext(), null);
        remoteRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        localRender.init(eglBase.getEglBaseContext(), null);
        localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        // To scall the renders in the view
        remoteRender.setZOrderMediaOverlay(true);
        remoteRender.setEnableHardwareScaler(true /* enabled */);
        localRender.setEnableHardwareScaler(false /* enabled */);

        /**
        * Constructor to config params
        * @param videoCallEnabled
        * @param loopback
        * @param tracing
        * @param videoWidth
        * @param videoHeight
        * @param videoFps
        * @param videoMaxBitrate
        * @param videoCodec
        * @param videoCodecHwAcceleration
        * @param videoFlexfecEnabled
        * @param audioStartBitrate
        * @param audioCodec
        * @param noAudioProcessing
        * @param aecDump
        * @param saveInputAudioToFile
        * @param useOpenSLES
        * @param disableBuiltInAEC
        * @param disableBuiltInAGC
        * @param disableBuiltInNS
        * @param disableWebRtcAGCAndHPF
        * @param enableRtcEventLog
        * @param useLegacyAudioDevice
        */
        // For a quick configuration it is recommended to use the following
        params = new PeerConnectionParameters(
                true, false, false, displaySize.x, displaySize.y, 30, 0, VIDEO_CODEC_VP9,
                true, false, 1, AUDIO_CODEC_OPUS, true, false, false,
                false, false, false, false, false, false, false);
        /**
        * Constructor to config params
        * @param ordered
        * @param maxRetransmitTimeMs
        * @param maxRetransmits
        * @param protocol
        * @param negotiated
        * @param id
        */
        // For a quick configuration it is recommended to use the following
        dataChannelParameters = new DataChannelParameters(true, 30, 30, "", true, 1);

        // Stablish the frames local video
        localProxyVideoSink.setTarget(localRender);

        /**
        * Class constructor that initializes the parameters of a client.
        * @param wClientListener          listening states Peer connection
        * @param url                      ip address
        * @param peerConnectionParameters parameters of the view where the local video will be shown and remote
        * @param dataChannelParameters    parameters of the view where the local video will be shown and remote
        * @param appContext               app context, where view video call show
        * @param userName                 user name
        * @param enableDataChannel        to enable Data Channel
        */
        client = new WebRTCClient(wsuri, username, this, params, dataChannelParameters, this, true);
    }
```

- After, you should implement the listener WebRTCClient.WClientListener and add the override method.

```Java
public class JavaActivity extends AppCompatActivity implements WebRTCClient.WClientListener {

    ...
    // When a call is received, this method returns the id or name of the calling user.
    @Override
    public void onCallReady(String callId) {
        this.callName = callId;
    }

    // This method returns a notification the call status.
    @Override
    public void onStatusChanged(String newStatus) {}

    // This method return the local video
    @Override
    public void onLocalStream(MediaStream localStream, VideoCapturer videoCapturer) {
        // Add the video to the view and set the frames
        localStream.videoTracks.get(0).setEnabled(true);
        localStream.videoTracks.get(0).addSink(localProxyVideoSink);
    }

    // This method returns the video and audio remote
    @Override
    public void onAddRemoteStream(MediaStream remoteStream, String otherName) {
        // Get the frames
        remoteProxyRenderer.setTarget(remoteRender);
        // Add the remote video to the view and set the frames
        remoteStream.videoTracks.get(0).setEnabled(true);
        remoteStream.videoTracks.get(0).addSink(remoteProxyRenderer);
    }

    // This method notify when the remote video is removed.
    @Override
    public void onRemoveRemoteStream(String otherName) {
        // Remove the video remote to the view
        remoteRender.release();
    }

    // This method returns the received messages by the data channel.
    @Override
    public void onMessage(String message) {}
}
```

- Finally, add the override methods to pause and resume application to manage the video :

```Java
    @Override
    public void onPause() {
        ...
        if (client != null) {
            // Pause the video call when changing screen
            client.onPause();
        }
    }

    @Override
    public void onResume() {
        ...
        // If any element of the graphical interface has changed while the activity was in the background this method is called.
        if (client != null) {
            client.onResume();
        }
    }
```

*To Create a [simple mirror](https://github.com/KarinaRomero/sfRTC-android-framework/blob/master/app/src/main/java/com/karinaromero/sfrtcandroidapp/MirrorDemoActivity.java).

## Run demo

- Before, check the [signaling](https://github.com/KarinaRomero/signaling) project and make sure it run.
- Then, open the project add the custom URL signaling into appFolder > app > src > main > sfrtcandroidapp > DemoActivity.java in this line :

  `63 private String wsuri = "ws://your.url.signaling";`

- Finally configure a device and you will Run app.

## License

This framework is licenced under [MIT Licence](https://opensource.org/licenses/MIT).
