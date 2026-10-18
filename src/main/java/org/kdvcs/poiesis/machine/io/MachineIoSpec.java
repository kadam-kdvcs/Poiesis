package org.kdvcs.poiesis.machine.io;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MachineIoSpec {

    private final Map<IoChannel, ChannelIoSpec> channels;

    private MachineIoSpec(Map<IoChannel, ChannelIoSpec> channels) {
        EnumMap<IoChannel, ChannelIoSpec> copy =
                new EnumMap<>(IoChannel.class);
        copy.putAll(channels);
        this.channels = Collections.unmodifiableMap(copy);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MachineIoSpec none() {
        return new MachineIoSpec(Map.of());
    }

    public boolean supports(IoChannel channel) {
        return channels.containsKey(channel);
    }

    public Optional<ChannelIoSpec> channel(IoChannel channel) {
        return Optional.ofNullable(channels.get(channel));
    }

    public Set<IoChannel> channels() {
        return Collections.unmodifiableSet(channels.keySet());
    }

    public IoChannel defaultPage() {
        if (supports(IoChannel.ITEM)) {
            return IoChannel.ITEM;
        }

        if (supports(IoChannel.FLUID)) {
            return IoChannel.FLUID;
        }

        if (supports(IoChannel.ENERGY)) {
            return IoChannel.ENERGY;
        }

        throw new IllegalStateException("Machine does not expose any configurable I/O channel");
    }

    public boolean isEmpty() {
        return channels.isEmpty();
    }

    public static final class Builder {

        private final EnumMap<IoChannel, ChannelIoSpec> channels =
                new EnumMap<>(IoChannel.class);

        public Builder item() {
            return channel(
                    IoChannel.ITEM,
                    generalModes(),
                    SideIoMode.NONE,
                    true,
                    true
            );
        }

        public Builder fluid() {
            return channel(
                    IoChannel.FLUID,
                    generalModes(),
                    SideIoMode.NONE,
                    true,
                    true
            );
        }

        public Builder fluidInput() {
            return channel(
                    IoChannel.FLUID,
                    EnumSet.of(SideIoMode.NONE, SideIoMode.INPUT),
                    SideIoMode.INPUT,
                    false,
                    false
            );
        }

        /**
         * MFC fluid input with an explicit electrochemical chamber route.
         * The default keeps legacy MFC sides on the anode input until the
         * player assigns them to the cathode or disables them.
         */
        public Builder mfcFluidInput() {
            return channel(
                    IoChannel.FLUID,
                    EnumSet.of(
                            SideIoMode.NONE,
                            SideIoMode.INPUT_ANODE,
                            SideIoMode.INPUT_CATHODE
                    ),
                    SideIoMode.INPUT_ANODE,
                    false,
                    false
            );
        }

        public Builder fluidOutput() {
            return channel(
                    IoChannel.FLUID,
                    EnumSet.of(SideIoMode.NONE, SideIoMode.OUTPUT),
                    SideIoMode.OUTPUT,
                    false,
                    true
            );
        }

        public Builder energyInput() {
            return channel(
                    IoChannel.ENERGY,
                    EnumSet.of(SideIoMode.NONE, SideIoMode.INPUT),
                    SideIoMode.INPUT,
                    true,
                    false
            );
        }

        public Builder energyOutput() {
            return channel(
                    IoChannel.ENERGY,
                    EnumSet.of(SideIoMode.NONE, SideIoMode.OUTPUT),
                    SideIoMode.OUTPUT,
                    false,
                    true
            );
        }

        public Builder energyBidirectional() {
            return channel(
                    IoChannel.ENERGY,
                    generalModes(),
                    SideIoMode.INPUT,
                    true,
                    true
            );
        }

        public Builder channel(
                IoChannel channel,
                Set<SideIoMode> allowedModes,
                SideIoMode defaultMode
        ) {
            boolean autoInput = allowedModes.stream().anyMatch(SideIoMode::allowsInput);
            boolean autoOutput = allowedModes.stream().anyMatch(SideIoMode::allowsOutput);

            return channel(
                    channel,
                    allowedModes,
                    defaultMode,
                    autoInput,
                    autoOutput
            );
        }

        public Builder channel(
                IoChannel channel,
                Set<SideIoMode> allowedModes,
                SideIoMode defaultMode,
                boolean supportsAutoInput,
                boolean supportsAutoOutput
        ) {
            channels.put(
                    channel,
                    new ChannelIoSpec(
                            channel,
                            allowedModes,
                            defaultMode,
                            supportsAutoInput,
                            supportsAutoOutput
                    )
            );
            return this;
        }

        public MachineIoSpec build() {
            return new MachineIoSpec(channels);
        }

        private static EnumSet<SideIoMode> generalModes() {
            return EnumSet.of(
                    SideIoMode.NONE,
                    SideIoMode.INPUT,
                    SideIoMode.OUTPUT,
                    SideIoMode.INPUT_OUTPUT
            );
        }
    }
}
