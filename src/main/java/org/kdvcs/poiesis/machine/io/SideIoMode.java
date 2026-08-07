package org.kdvcs.poiesis.machine.io;

public enum SideIoMode {
    NONE(false, false),
    INPUT(true, false),
    OUTPUT(false, true),
    INPUT_OUTPUT(true, true);

    private final boolean input;
    private final boolean output;

    SideIoMode(boolean input, boolean output) {
        this.input = input;
        this.output = output;
    }

    public boolean allowsInput() {
        return input;
    }

    public boolean allowsOutput() {
        return output;
    }
}
