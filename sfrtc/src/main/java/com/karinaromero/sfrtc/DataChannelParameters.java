package com.karinaromero.sfrtc;

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
 * This class describe DataChannel configurations
 */
public class DataChannelParameters {
    public final boolean ordered;
    public final int maxRetransmitTimeMs;
    public final int maxRetransmits;
    public final String protocol;
    public final boolean negotiated;
    public final int id;

    /**
     * Constructor to config params
     * @param ordered
     * @param maxRetransmitTimeMs
     * @param maxRetransmits
     * @param protocol
     * @param negotiated
     * @param id
     */
    public DataChannelParameters(boolean ordered, int maxRetransmitTimeMs, int maxRetransmits,
                                 String protocol, boolean negotiated, int id) {
        this.ordered = ordered;
        this.maxRetransmitTimeMs = maxRetransmitTimeMs;
        this.maxRetransmits = maxRetransmits;
        this.protocol = protocol;
        this.negotiated = negotiated;
        this.id = id;
    }
}
