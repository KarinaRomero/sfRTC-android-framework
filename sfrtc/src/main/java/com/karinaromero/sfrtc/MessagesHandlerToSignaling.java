package com.karinaromero.sfrtc;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.crossbar.autobahn.websocket.*;
import io.crossbar.autobahn.websocket.exceptions.WebSocketException;
import io.crossbar.autobahn.websocket.interfaces.IWebSocketConnectionHandler;
import io.crossbar.autobahn.websocket.types.ConnectionResponse;

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

/**
 * This class manage the signaling messages.
 */
public class MessagesHandlerToSignaling {

    private final static String TAG = MessagesHandlerToSignaling.class.getCanonicalName();
    private final WebSocketConnection mConnection = new WebSocketConnection();

    private String url;
    private String userName;
    private MessagesToSignalingListener messagesToSignalingListener;

    /**
     * Implement this interface to be notified of events to signaling.
     */
    public interface MessagesToSignalingListener {
        void onLogin(boolean success);

        void onOffer(String offer, String otherName);

        void onAnswer(String answer);

        void onCandidate(String candidate, String sdpMid, int sdpMLineIndex);

        void onLeave();
    }

    /***
     * Class constructor initializes the parameters; IP address and user name
     *
     * @param url    IP adress
     * @param userName user name
     */

    public MessagesHandlerToSignaling(String url, String userName, MessagesToSignalingListener messagesToSignalingListener) {
        this.messagesToSignalingListener = messagesToSignalingListener;
        this.url = url;
        this.userName = userName;
        try {
            this.webConnection();
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToSignaling(String message) {
        mConnection.sendMessage(message);
    }

    /***
     * WebSocket connection method
     *
     * @throws Exception
     */

    private void webConnection() throws WebSocketException {
        mConnection.connect(url, new IWebSocketConnectionHandler() {


            @Override
            public void onConnect(ConnectionResponse response) {
                Log.d("OnOPEN", response.toString());
            }

            /**
             * WebSocketHandler class method that initializes the connection, sends a message to the signaling login.
             */
            @Override
            public void onOpen() {
                Log.d(TAG, "Status: Connected to " + url);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "login");
                    message.put("name", userName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mConnection.sendMessage(message.toString());
            }

            /**
             * WebSocketHandler method of the class was listening to receive messages sent by the signaling.
             *
             * @param payload Message received by the signaling.
             */
            @Override
            public void onMessage(String payload) {
                Log.d(TAG, "Got echo: " + payload);
                if (payload.equals("Hello world")) {
                    Log.d("Hello : ", payload);
                } else {
                    JSONObject message = null;
                    try {
                        message = new JSONObject(payload);

                        switch (message.getString("type")) {
                            case "login":

                                Boolean success = new Boolean(message.getString("success"));
                                messagesToSignalingListener.onLogin(success);
                                break;

                            case "offer":

                                String offer = message.getJSONObject("offer").getString("sdp");
                                String otherName = message.getString("name");
                                messagesToSignalingListener.onOffer(offer, otherName);
                                break;

                            case "answer":

                                String answer = message.getJSONObject("answer").getString("sdp");
                                messagesToSignalingListener.onAnswer(answer);
                                break;

                            case "candidate":

                                String candidate = message.getJSONObject("candidate").getString("candidate");
                                String sdpMid = message.getJSONObject("candidate").getString("sdpMid");
                                int sdpMLineIndex = Integer.parseInt(message.getJSONObject("candidate").getString("sdpMLineIndex"));

                                messagesToSignalingListener.onCandidate(candidate, sdpMid, sdpMLineIndex);
                                break;

                            case "leave":
                                messagesToSignalingListener.onLeave();
                                break;

                            default:
                                break;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }

            @Override
            public void onMessage(byte[] payload, boolean isBinary) {

            }

            @Override
            public void onPing() {

            }

            @Override
            public void onPing(byte[] payload) {

            }

            @Override
            public void onPong() {

            }

            @Override
            public void onPong(byte[] payload) {

            }

            @Override
            public void setConnection(WebSocketConnection connection) {

            }

            /**
             * This method closes the connection WebSocket.
             *
             * @param code identifier code.
             * @param reason String to the cause.
             */
            @Override
            public void onClose(int code, String reason) {
                Log.d(TAG, "Connection lost, code: " + code + " reason: " + reason);
            }
        });
    }

    public void closeConnection() {
        mConnection.sendClose();
    }
}

