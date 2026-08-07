package org.kdvcs.poiesis.machine.io;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.kdvcs.poiesis.machine.api.MachineComponent;

import java.util.EnumMap;

public final class SideConfigurationComponent
        implements MachineComponent {

    private final MachineIoSpec spec;
    private final Runnable changed;
    private final EnumMap<IoChannel, EnumMap<RelativeSide, SideIoMode>> modes =
            new EnumMap<>(IoChannel.class);

    public SideConfigurationComponent(
            MachineIoSpec spec,
            Runnable changed
    ) {
        if (spec == null || spec.isEmpty()) {
            throw new IllegalArgumentException("Side configuration requires at least one I/O channel");
        }

        this.spec = spec;
        this.changed = changed;
        resetToDefaults();
    }

    @Override
    public String key() {
        return "side_configuration";
    }

    public MachineIoSpec spec() {
        return spec;
    }

    public SideIoMode mode(
            IoChannel channel,
            RelativeSide side
    ) {
        EnumMap<RelativeSide, SideIoMode> channelModes = modes.get(channel);

        if (channelModes == null) {
            return SideIoMode.NONE;
        }

        return channelModes.getOrDefault(side, SideIoMode.NONE);
    }

    public boolean allowsInput(
            IoChannel channel,
            RelativeSide side
    ) {
        return mode(channel, side).allowsInput();
    }

    public boolean allowsOutput(
            IoChannel channel,
            RelativeSide side
    ) {
        return mode(channel, side).allowsOutput();
    }

    public void setMode(
            IoChannel channel,
            RelativeSide side,
            SideIoMode mode
    ) {
        ChannelIoSpec channelSpec = spec.channel(channel)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Machine does not support I/O channel " + channel
                ));

        if (!channelSpec.allowedModes().contains(mode)) {
            throw new IllegalArgumentException(
                    "Side mode " + mode + " is not allowed for channel " + channel
            );
        }

        EnumMap<RelativeSide, SideIoMode> channelModes = modes.get(channel);
        SideIoMode previous = channelModes.put(side, mode);

        if (previous != mode) {
            changed.run();
        }
    }

    public SideIoMode cycleMode(
            IoChannel channel,
            RelativeSide side,
            boolean reverse
    ) {
        ChannelIoSpec channelSpec = spec.channel(channel)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Machine does not support I/O channel " + channel
                ));

        SideIoMode next = channelSpec.nextMode(
                mode(channel, side),
                reverse
        );

        setMode(channel, side, next);
        return next;
    }

    public void resetToDefaults() {
        modes.clear();

        for (IoChannel channel : spec.channels()) {
            ChannelIoSpec channelSpec = spec.channel(channel).orElseThrow();
            EnumMap<RelativeSide, SideIoMode> channelModes =
                    new EnumMap<>(RelativeSide.class);

            for (RelativeSide side : RelativeSide.values()) {
                channelModes.put(side, channelSpec.defaultMode());
            }

            modes.put(channel, channelModes);
        }
    }

    @Override
    public void save(CompoundTag tag) {
        for (IoChannel channel : spec.channels()) {
            CompoundTag channelTag = new CompoundTag();

            for (RelativeSide side : RelativeSide.values()) {
                channelTag.putString(
                        side.name().toLowerCase(),
                        mode(channel, side).name()
                );
            }

            tag.put(channel.name().toLowerCase(), channelTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        resetToDefaults();

        for (IoChannel channel : spec.channels()) {
            String channelKey = channel.name().toLowerCase();

            if (!tag.contains(channelKey, Tag.TAG_COMPOUND)) {
                continue;
            }

            CompoundTag channelTag = tag.getCompound(channelKey);
            ChannelIoSpec channelSpec = spec.channel(channel).orElseThrow();
            EnumMap<RelativeSide, SideIoMode> channelModes = modes.get(channel);

            for (RelativeSide side : RelativeSide.values()) {
                String sideKey = side.name().toLowerCase();

                if (!channelTag.contains(sideKey, Tag.TAG_STRING)) {
                    continue;
                }

                SideIoMode parsed;

                try {
                    parsed = SideIoMode.valueOf(channelTag.getString(sideKey));
                } catch (IllegalArgumentException exception) {
                    continue;
                }

                if (channelSpec.allowedModes().contains(parsed)) {
                    channelModes.put(side, parsed);
                }
            }
        }
    }
}
