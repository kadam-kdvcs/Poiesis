package org.kdvcs.poiesis.transport.pipe;

public enum PipeEndpointMode {
    INSERT,
    EXTRACT,
    DISABLED;

    public PipeEndpointMode next() {
        return switch (this) {
            case INSERT -> EXTRACT;
            case EXTRACT -> DISABLED;
            case DISABLED -> INSERT;
        };
    }

    public PipeEndpointMode previous() {
        return switch (this) {
            case INSERT -> DISABLED;
            case EXTRACT -> INSERT;
            case DISABLED -> EXTRACT;
        };
    }
}
