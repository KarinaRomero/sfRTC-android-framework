package com.karinaromero.sfrtcandroidapp;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.karinaromero.sfrtc.DataChannelParameters;
import com.karinaromero.sfrtc.PeerConnectionParameters;
import com.karinaromero.sfrtc.WebRTCClient;
import com.karinaromero.sfrtc.WebRTCClientException;

import org.webrtc.EglBase;
import org.webrtc.MediaStream;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

// implement WClientListener listener to receive events
public class MirrorDemoActivity extends AppCompatActivity implements WebRTCClient.WClientListener  {

    private static final String TAG = "Mirror Activity Demo";

    // Manage video
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    private SurfaceViewRenderer localRender;

    // To configure Video Codecs
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";

    // New client
    private WebRTCClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mirror_demo);

        // Find surface to render video
        localRender = findViewById(R.id.fullscreen_video_view);

        // Create a EglBase to render video
        final EglBase eglBase = EglBase.create();

        // Initialize render with EglBase context
        localRender.init(eglBase.getEglBaseContext(), null);
        localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localRender.setEnableHardwareScaler(false /* enabled */);

        //init();

        // To get default size view
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);

        //params of video call
        PeerConnectionParameters params = new PeerConnectionParameters(
                true, false, false, displaySize.x, displaySize.y, 30, 0, VIDEO_CODEC_VP9,
                true, false, 1, AUDIO_CODEC_OPUS, true, false, false,
                false, false, false, false, false, false, false);

        // Initialize manage video whit local render
        localProxyVideoSink.setTarget(localRender);

        try {
            client = new WebRTCClient(null, "Mirror", this, params, null, this, false);
        } catch (WebRTCClientException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCallReady(String callId) {

    }

    @Override
    public void onStatusChanged(String newStatus) {

    }

    @Override
    public void onLocalStream(MediaStream localStream, VideoCapturer videoCapturer) {
        localStream.videoTracks.get(0).setEnabled(true);
        localStream.videoTracks.get(0).addSink(localProxyVideoSink);
    }

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, String otherName) {

    }

    @Override
    public void onRemoveRemoteStream(String otherName) {

    }

    @Override
    public void onMessage(String message) {

    }

    // class to set video frames
    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Log.d(TAG, "Dropping frame in proxy because target is null.");
                return;
            }

            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }
}
