package org.stjude.swingui.boot.event;

/**
 * Represents a single recorded action with its parameters
 */
public class ActionRecord {
    private final String actionId;
    private final double[] params;
    private final int channel;
    private final long timestamp;

    public ActionRecord(String actionId, double[] params, int channel) {
        this.actionId = actionId;
        this.params = params;
        this.channel = channel;
        this.timestamp = System.currentTimeMillis();
    }

    public String getActionId() {
        return actionId;
    }

    public double[] getParams() {
        return params;
    }

    public int getChannel() {
        return channel;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getParamsAsString() {
        if (params == null || params.length == 0) {
            return "None";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.2f", params[i]));
        }
        return sb.toString();
    }

    public String getChannelLabel() {
        return (channel == -1) ? "All" : String.valueOf(channel);
    }

    @Override
    public String toString() {
        return String.format("Ch: %-4s | %-15s | %s", 
            getChannelLabel(), actionId, getParamsAsString());
    }
}
