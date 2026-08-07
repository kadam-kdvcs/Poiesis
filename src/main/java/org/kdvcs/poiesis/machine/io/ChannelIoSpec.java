package org.kdvcs.poiesis.machine.io;

import java.util.EnumSet;
import java.util.Set;

public record ChannelIoSpec(
        IoChannel channel,
        Set<SideIoMode> allowedModes,
        SideIoMode defaultMode,
        boolean supportsAutoInput,
        boolean supportsAutoOutput
) {
    public ChannelIoSpec {
        if (channel == null || allowedModes == null || defaultMode == null) {
            throw new IllegalArgumentException("I/O channel specification cannot contain null values");
        }

        if (allowedModes.isEmpty()) {
            throw new IllegalArgumentException("An I/O channel must allow at least one side mode");
        }

        allowedModes = Set.copyOf(EnumSet.copyOf(allowedModes));

        if (!allowedModes.contains(defaultMode)) {
            throw new IllegalArgumentException("Default side mode must be allowed by the channel");
        }

        if (supportsAutoInput
                && allowedModes.stream().noneMatch(SideIoMode::allowsInput)) {
            throw new IllegalArgumentException(
                    "Automatic input requires at least one input-capable side mode"
            );
        }

        if (supportsAutoOutput
                && allowedModes.stream().noneMatch(SideIoMode::allowsOutput)) {
            throw new IllegalArgumentException(
                    "Automatic output requires at least one output-capable side mode"
            );
        }
    }

    public SideIoMode nextMode(SideIoMode current, boolean reverse) {
        SideIoMode[] values = SideIoMode.values();
        int step = reverse ? -1 : 1;
        int index = current.ordinal();

        for (int attempts = 0; attempts < values.length; attempts++) {
            index = Math.floorMod(index + step, values.length);
            SideIoMode candidate = values[index];

            if (allowedModes.contains(candidate)) {
                return candidate;
            }
        }

        return defaultMode;
    }
}
