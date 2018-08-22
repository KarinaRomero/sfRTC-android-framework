package com.karinaromero.sfrtcandroidapp;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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

/**
 * Copyright 2018  Karina Betzabe Romero Ulloa
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

public class DemoActivity extends AppCompatActivity implements WebRTCClient.WClientListener {

    private static final String TAG = "CallRTCClient";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";

    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRender;
    private final List<VideoSink> remoteSinks = new ArrayList<>();

    private String wsuri = "ws://your.url.signaling";
    private String username;
    private String callName;

    private Button btnCall;
    private Button btnHangUp;
    private Button btnSendMessage;
    private Button btnAnswer;
    private Button btnShowMessage;
    private Button btnCloseMessage;

    private EditText edtNameCall;
    private EditText edtMessage;
    private TextView txtOnCallMessage;
    private TextView txtMessages;

    private LinearLayout linearLayoutMessages;
    private LinearLayout linearLayoutOnCall;

    private WebRTCClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        linearLayoutMessages = findViewById(R.id.llyt_message);

        linearLayoutOnCall = findViewById(R.id.llyt_oncall);

        txtOnCallMessage = findViewById(R.id.txtOnCallMessage);

        edtNameCall = findViewById(R.id.edtNameCall);
        edtMessage = findViewById(R.id.edtMessage);

        btnCall = findViewById(R.id.btnCall);

        btnHangUp = findViewById(R.id.btnHangUp);

        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnCloseMessage = findViewById(R.id.btnCloseMessages);
        txtMessages = findViewById(R.id.txtMessages);

        remoteRender = findViewById(R.id.pip_video_view);
        localRender = findViewById(R.id.fullscreen_video_view);

        btnAnswer = findViewById(R.id.btnAnswer);
        btnShowMessage = findViewById(R.id.btnShowMessage);

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
            client.hangUp();
        });
        btnSendMessage.setOnClickListener(view -> {
            String message = edtMessage.getText().toString();
            client.sendMessage(message);
        });

        btnAnswer.setOnClickListener(view -> {
            client.answer();
            hidingLayout(linearLayoutOnCall);
            //btnCall.setVisibility(View.GONE);
            //edtNameCall.setVisibility(View.GONE);
        });
        btnShowMessage.setOnClickListener(view -> {
            this.showingLayout(linearLayoutMessages);
        });
        btnCloseMessage.setOnClickListener(view -> {
            this.hidingLayout(linearLayoutMessages);
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
        txtOnCallMessage.setText("Llamada de " + callName);
        this.showingLayout(linearLayoutOnCall);

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
        btnCall.setVisibility(View.VISIBLE);
        edtNameCall.setVisibility(View.VISIBLE);
        //remoteProxyRenderer.setTarget(null);
        remoteRender.release();
    }

    @Override
    public void onMessage(String message) {
        String printM = callName + " dice: " + message;
        runOnUiThread(() -> txtMessages.setText(printM));

    }
    //endregion

    //region ApplicationDemo

    /**
     * Pause the video call when changing screen
     */
    @Override
    public void onPause() {
        super.onPause();
        if (client != null) {
            client.onPause();
        }
    }

    /**
     * If any element of the graphical interface has changed while the activity was in the background this method is called.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (client != null) {
            client.onResume();
        }
    }

    /***
     * Eliminates any background process video call.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
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

    public void showingLayout(LinearLayout linearLayout) {
        if (linearLayout.getVisibility() == View.GONE) {
            animation(true, linearLayout);
            linearLayout.setVisibility(View.VISIBLE);
        }
    }

    public void hidingLayout(LinearLayout linearLayout) {
        if (linearLayout.getVisibility() == View.VISIBLE) {
            animation(false, linearLayout);
            linearLayout.setVisibility(View.GONE);
        }

    }
    private void animation(boolean mostrar, LinearLayout layoutAnimado)
    {
        AnimationSet set = new AnimationSet(true);
        Animation animation = null;
        if (mostrar)
        {
            //desde la esquina inferior derecha a la superior izquierda
            animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        }
        else
        {    //desde la esquina superior izquierda a la esquina inferior derecha
            animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        }
        //duración en milisegundos
        animation.setDuration(500);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.25f);

        layoutAnimado.setLayoutAnimation(controller);
        layoutAnimado.startAnimation(animation);
    }

}
