package com.karinaromero.sfrtcandroidapp;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.karinaromero.sfrtc.*;

import org.webrtc.EglBase;
import org.webrtc.Logging;
import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.util.ArrayList;
import java.util.List;

public class DemoActivity extends AppCompatActivity implements WebRTCClient.WClientListener {

    private static final String TAG = "CallRTCClient";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";

    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRender;
    private final List<VideoSink> remoteSinks = new ArrayList<>();

    private String wsuri = "ws://192.168.0.11:8888";
    private String username;
    private String callName;

    private Button btnCall;
    private Button btnHangUp;
    private Button btnSendMessage;
    private Button btnAnswer;

    private EditText edtNameCall;
    private EditText edtMessage;

    private WebRTCClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        edtNameCall = findViewById(R.id.edtNameCall);
        edtMessage = findViewById(R.id.edtMessage);

        btnCall = findViewById(R.id.btnCall);
        btnCall.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);

        btnHangUp = findViewById(R.id.btnHangUp);
        btnHangUp.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);

        btnSendMessage = findViewById(R.id.btnSendMessage);

        remoteRender = findViewById(R.id.pip_video_view);
        localRender = findViewById(R.id.fullscreen_video_view);

        btnAnswer = findViewById(R.id.btnAnswer);
        btnAnswer.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);

        btnCall.setOnClickListener(view -> {
            callName = edtNameCall.getText().toString();

            if (callName != null) {
                try {
                    client.call(callName);
                } catch (WebRTCClientException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Debes llenar el campo", Toast.LENGTH_SHORT).show();
            }
        });
        btnHangUp.setOnClickListener(view -> {
            callName = edtNameCall.getText().toString();
            Log.d("UserName", username);
            if (callName != null) {
                client.hangUp();
            } else {
                Toast.makeText(getApplicationContext(), "Debes llenar el campo", Toast.LENGTH_SHORT).show();
            }
        });
        btnSendMessage.setOnClickListener(view -> {
            String message = edtMessage.getText().toString();
            client.sendMessage(message);
        });

        btnAnswer.setOnClickListener(view -> {
            client.answer();
        });

        remoteSinks.add(remoteProxyRenderer);

        final Intent intent = getIntent();
        final EglBase eglBase = EglBase.create();

        remoteRender.init(eglBase.getEglBaseContext(), null);
        remoteRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        localRender.init(eglBase.getEglBaseContext(), null);
        localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);


        remoteRender.setZOrderMediaOverlay(true);
        remoteRender.setEnableHardwareScaler(true /* enabled */);
        localRender.setEnableHardwareScaler(false /* enabled */);


        Bundle bundle = intent.getExtras();
        username = (String) bundle.get("UserName");
        Log.d("userName", "onCreate: " + username);
        init();
    }

    /**
     * This method is to initialize the parameters of the video call.
     */
    private void init() {
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        //params of video call
        PeerConnectionParameters params = new PeerConnectionParameters(
                true, false, false, displaySize.x, displaySize.y, 30, 0, VIDEO_CODEC_VP9,
                true, false, 1, AUDIO_CODEC_OPUS, true, false, false,
                false, false, false, false, false, false, false);
        //params of data channel
        DataChannelParameters dataChannelParameters = new DataChannelParameters(true, 30, 30, "", true, 1);

        localProxyVideoSink.setTarget(localRender);

        try {
            client = new WebRTCClient(wsuri, username, this, params, dataChannelParameters, this, true);
        } catch (WebRTCClientException e) {
            e.printStackTrace();
        }


    }

    //region WClientListener
    @Override
    public void onCallReady(String callId) {
        this.callName = callId;
    }

    @Override
    public void onStatusChanged(String newStatus) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), newStatus, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onLocalStream(MediaStream localStream, VideoCapturer videoCapturer) {
        localStream.videoTracks.get(0).setEnabled(true);
        localStream.videoTracks.get(0).addSink(localProxyVideoSink);
    }

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, String otherName) {
        remoteProxyRenderer.setTarget(remoteRender);
        remoteStream.videoTracks.get(0).setEnabled(true);
        remoteStream.videoTracks.get(0).addSink(remoteProxyRenderer);
    }

    @Override
    public void onRemoveRemoteStream(String otherName) {
        remoteRender.release();
    }

    @Override
    public void onMessage(String message) {
        Log.d("ONMESSAGE:: ", message);
        String printM = callName + " dice: " + message;
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), printM, Toast.LENGTH_SHORT).show());

    }
    //endregion

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
}
