package com.karinaromero.sfrtc;

import android.content.Context;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;

/**
 * This class manage the Peer Connection.
 */
public class WebRTCClient implements MessagesHandlerToSignaling.MessagesToSignalingListener, SdpObserver, PeerConnection.Observer, DataChannel.Observer {

    private final static String TAG = WebRTCClient.class.getCanonicalName();


    public MessagesHandlerToSignaling messagesHandlerToSignaling;
    private WClientListener wClientListener;
    private String url;
    private String userName;
    private String otherName;
    private PeerConnectionParameters peerConnectionParameters;
    private DataChannelParameters dataChannelParameters;
    private Context appContext;

    private PeerConnectionFactory peerConnectionFactory;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private MediaConstraints mediaConstraints = new MediaConstraints();

    private MediaStream localMS;
    private VideoSource videoSource;
    private VideoCapturer videoCapturer;

    private PeerConnection peerConnection;
    private DataChannel dataChannel;
    private boolean enableDataChannel;

    /**
     * Implement this interface to be notified of events.
     */
    public interface WClientListener {
        void onCallReady(String callId);

        void onStatusChanged(String newStatus);

        void onLocalStream(MediaStream localStream, VideoCapturer videoCapturer);

        void onAddRemoteStream(MediaStream remoteStream, String otherName);

        void onRemoveRemoteStream(String otherName);

        void onMessage(String message);
    }

    /**
     * Class constructor that initializes the parameters of a client.
     *
     * @param wClientListener          listening states Peer connection
     * @param url                      ip address
     * @param peerConnectionParameters parameters of the view where the local video will be shown and remote
     * @param dataChannelParameters    parameters of the view where the local video will be shown and remote
     * @param appContext               app context, where view video call show
     * @param userName                 user name
     * @param enableDataChannel        to enable Data Channel
     */
    public WebRTCClient(String url, String userName, WClientListener wClientListener, PeerConnectionParameters peerConnectionParameters, DataChannelParameters dataChannelParameters, Context appContext, boolean enableDataChannel) throws WebRTCClientException {
        if (url == null) {
            throw new WebRTCClientException("The url is null");
        }
        if (appContext == null) {
            throw new WebRTCClientException("The appContext is null");
        }
        if (dataChannelParameters == null) {
            throw new WebRTCClientException("The dataChannelParameters is null");
        }
        if (userName == null) {
            throw new WebRTCClientException("The userName is null");
        }
        if (wClientListener == null) {
            throw new WebRTCClientException("The wClientListener is null");
        }
        if (peerConnectionParameters == null) {
            throw new WebRTCClientException("The peerConnectionParameters is null");
        }

        this.url = url;
        this.appContext = appContext;
        this.dataChannelParameters = dataChannelParameters;
        this.url = url;
        this.userName = userName;
        this.wClientListener = wClientListener;
        this.peerConnectionParameters = peerConnectionParameters;
        this.enableDataChannel = enableDataChannel;
        initializePeerConnection();
    }

    /**
     * Peer Connection Configurations.
     */
    private void initializePeerConnection() {
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(appContext)
                .setEnableInternalTracer(true)
                .setEnableVideoHwAcceleration(true)
                .createInitializationOptions());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .createPeerConnectionFactory();

        PeerConnection.IceServer turnServer =
                PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                        .createIceServer();
        iceServers.add(turnServer);

        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        createLocalStream();

        messagesHandlerToSignaling = new MessagesHandlerToSignaling(url, userName, this);

        createPeerConnection();
    }

    /**
     * Create Peer Connection
     */

    public void createPeerConnection() {
        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(iceServers);
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, this);

        if (enableDataChannel) {
            enableDataChannel = enableDataChannel;
            setupDataChannelConnection();
        }
        peerConnection.addStream(localMS); //, new MediaConstraints()

        wClientListener.onStatusChanged("CONNECTING");
    }

    /**
     * Create Data channel
     */
    private void setupDataChannelConnection() {
        DataChannel.Init dcInit = new DataChannel.Init();
        dcInit.id = 1;
        dataChannel = peerConnection.createDataChannel("DataChannel", dcInit);
        dataChannel.registerObserver(this);
    }

    /**
     * Start the localStream.
     */

    private void createLocalStream() {
        localMS = peerConnectionFactory.createLocalMediaStream("ARDAMS");
        if (peerConnectionParameters.videoCallEnabled) {
            MediaConstraints videoConstraints = new MediaConstraints();
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(peerConnectionParameters.videoHeight)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(peerConnectionParameters.videoWidth)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(peerConnectionParameters.videoFps)));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(peerConnectionParameters.videoFps)));

            videoSource = peerConnectionFactory.createVideoSource(getVideoCapturer());
            videoCapturer.startCapture(peerConnectionParameters.videoWidth, peerConnectionParameters.videoWidth, peerConnectionParameters.videoFps);

            localMS.addTrack(peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource));
        }

        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localMS.addTrack(peerConnectionFactory.createAudioTrack("ARDAMSa0", audioSource));

        wClientListener.onLocalStream(localMS, videoCapturer);

    }

    /**
     * Start camera.
     */
    private VideoCapturer getVideoCapturer() {
        if (Camera2Enumerator.isSupported(appContext)) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(appContext));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(false));
        }
        return videoCapturer;
    }

    /**
     * Get camera.
     */
    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    /**
     * Call this method in Activity.onPause()
     */
    public void onPause() {
        if (videoSource != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Call this method in Activity.onResume()
     */
    public void onResume() {
        if (videoSource != null) {
            videoCapturer.startCapture(peerConnectionParameters.videoWidth, peerConnectionParameters.videoHeight, peerConnectionParameters.videoFps);
        }
    }

    /**
     * This method sends an offer with the user name you want to call it is called in the call button on the main activity.
     *
     * @param otherName It contains the name to call
     */
    public void call(String otherName) throws WebRTCClientException {
        if (otherName == null) {
            throw new WebRTCClientException("The other id is null");
        }
        this.otherName = otherName;

        if (peerConnection != null) {
            peerConnection.createOffer(this, mediaConstraints);
            wClientListener.onStatusChanged("CALLING");
        }

    }

    /**
     * This method sends an answer with the user name you want to response, it is called in the answer button on the main activity.
     */
    public void answer() {
        JSONObject jsonSDP = new JSONObject();
        JSONObject jsonObject = new JSONObject();

        try {
            jsonSDP.put("type", "answer");
            jsonSDP.put("sdp", peerConnection.getLocalDescription().description);

            jsonObject.put("type", "answer");
            jsonObject.put("answer", jsonSDP);
            jsonObject.put("name", this.otherName);
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e);
        }
        messagesHandlerToSignaling.sendMessageToSignaling(jsonObject.toString());

    }

    /**
     * Notifies signaling that the call is over, this method is called on the main activity, in the HangUp button.
     */
    public void hangUp() {

        JSONObject sendLive = new JSONObject();
        try {
            sendLive.put("type", "leave");
            sendLive.put("name", otherName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        messagesHandlerToSignaling.sendMessageToSignaling(sendLive.toString());
        removePeer();
    }

    /**
     * Send Message by data channel
     */
    public void sendMessage(String message) {
        //ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        //dataChannel.send(new DataChannel.Buffer(buffer, false));
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(Charset.defaultCharset()));
        //localDataChannel.send(new DataChannel.Buffer(data, false));
        dataChannel.send(new DataChannel.Buffer(buffer, false));
    }

    /**
     * Remove the peer connection.
     */
    private void removePeer() {
        try {
            wClientListener.onRemoveRemoteStream(otherName);
            peerConnection.close();
            peerConnection = null;
        } catch (Exception e) {
            Log.d(TAG, "error: " + e);
        }
    }

    //region MessagesToSignalingListener
    @Override
    public void onLogin(boolean success) {
        Log.d(TAG, "onLogin: " + success);
        wClientListener.onStatusChanged("LOGIN");
    }

    @Override
    public void onOffer(String offer, String otherName) {
        wClientListener.onStatusChanged("RECEIVING");
        this.otherName = otherName;

        if (peerConnection != null) {
            SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("OFFER"), offer);
            peerConnection.setRemoteDescription(this, sdp);
            peerConnection.createAnswer(this, mediaConstraints);
            wClientListener.onCallReady(this.otherName);
        }
    }

    @Override
    public void onAnswer(String answer) {
        SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("ANSWER"), answer);
        peerConnection.setRemoteDescription(this, sdp);

        wClientListener.onStatusChanged("ANSWERING");

    }

    @Override
    public void onCandidate(String candidate, String sdpMid, int sdpMLineIndex) {
        IceCandidate iceCandidate;
        if (peerConnection.getRemoteDescription() != null) {
            iceCandidate = new IceCandidate(
                    sdpMid,
                    sdpMLineIndex,
                    candidate
            );
            peerConnection.addIceCandidate(iceCandidate);
        }
    }

    @Override
    public void onLeave() {
        wClientListener.onStatusChanged("LEAVE");
        removePeer();
    }
    //endregion

    //region SDPObserver
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        peerConnection.setLocalDescription(this, sessionDescription);

    }

    @Override
    public void onSetSuccess() {

    }

    @Override
    public void onCreateFailure(String s) {

    }

    @Override
    public void onSetFailure(String s) {

    }

    //endregion


    //region PeerConnectionObserver
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

        Log.d("SIGNSTATE", signalingState.toString());
        if (signalingState.equals(PeerConnection.SignalingState.HAVE_LOCAL_OFFER)) {
            JSONObject sigOffer = new JSONObject();
            JSONObject sendOffer = new JSONObject();
            try {
                sigOffer.put("type", "offer");
                sigOffer.put("sdp", peerConnection.getLocalDescription().description);

                sendOffer.put("type", "offer");
                sendOffer.put("offer", sigOffer);
                sendOffer.put("name", otherName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            messagesHandlerToSignaling.sendMessageToSignaling(sendOffer.toString());
        }
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
            removePeer();
            wClientListener.onStatusChanged("DISCONNECTED");
        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {

    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        wClientListener.onAddRemoteStream(mediaStream, otherName);

    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {

        removePeer();
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        dataChannel.registerObserver(this);
    }

    @Override
    public void onRenegotiationNeeded() {

    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

    }

    @Override
    public void onTrack(RtpTransceiver transceiver) {

    }


    //endregion

    //region DataChannelObserver
    @Override
    public void onBufferedAmountChange(long l) {

    }

    @Override
    public void onStateChange() {

    }

    @Override
    public void onMessage(DataChannel.Buffer buffer) {
        ByteBuffer data = buffer.data;
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);
        final String message = new String(bytes);
        wClientListener.onMessage(message);
    }
    //endregion
}

