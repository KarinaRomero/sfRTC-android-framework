package com.karinaromero.sfrtc;

/**
 * This class describe DataChannel configurations
 */
public class DataChannelParameters{
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
