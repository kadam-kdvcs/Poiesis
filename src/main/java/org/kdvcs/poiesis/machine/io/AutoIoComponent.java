package org.kdvcs.poiesis.machine.io;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.kdvcs.poiesis.machine.api.MachineComponent;

import java.util.EnumMap;

public final class AutoIoComponent implements MachineComponent {

    private final MachineIoSpec spec;
    private final Runnable changed;
    private final EnumMap<IoChannel, Boolean> autoInput =
            new EnumMap<>(IoChannel.class);
    private final EnumMap<IoChannel, Boolean> autoOutput =
            new EnumMap<>(IoChannel.class);

    public AutoIoComponent(
            MachineIoSpec spec,
            Runnable changed
    ) {
        if (spec == null || spec.isEmpty()) {
            throw new IllegalArgumentException(
                    "Automatic I/O requires at least one I/O channel"
            );
        }

        this.spec = spec;
        this.changed = changed;
        resetToDefaults();
    }

    @Override
    public String key() {
        return "auto_io";
    }

    public boolean supports(
            IoChannel channel,
            AutoIoDirection direction
    ) {
        return spec.channel(channel)
                .map(channelSpec -> switch (direction) {
                    case INPUT -> channelSpec.supportsAutoInput();
                    case OUTPUT -> channelSpec.supportsAutoOutput();
                })
                .orElse(false);
    }

    public boolean enabled(
            IoChannel channel,
            AutoIoDirection direction
    ) {
        if (!supports(channel, direction)) {
            return false;
        }

        return switch (direction) {
            case INPUT -> autoInput.getOrDefault(channel, false);
            case OUTPUT -> autoOutput.getOrDefault(channel, false);
        };
    }

    public void setEnabled(
            IoChannel channel,
            AutoIoDirection direction,
            boolean enabled
    ) {
        if (!supports(channel, direction)) {
            return;
        }

        EnumMap<IoChannel, Boolean> values = switch (direction) {
            case INPUT -> autoInput;
            case OUTPUT -> autoOutput;
        };

        boolean previous = values.getOrDefault(channel, false);
        values.put(channel, enabled);

        if (previous != enabled) {
            changed.run();
        }
    }

    public void toggle(
            IoChannel channel,
            AutoIoDirection direction
    ) {
        setEnabled(channel, direction, !enabled(channel, direction));
    }

    public void resetToDefaults() {
        autoInput.clear();
        autoOutput.clear();

        for (IoChannel channel : spec.channels()) {
            autoInput.put(channel, false);
            autoOutput.put(channel, false);
        }
    }

    @Override
    public void save(CompoundTag tag) {
        for (IoChannel channel : spec.channels()) {
            CompoundTag channelTag = new CompoundTag();

            if (supports(channel, AutoIoDirection.INPUT)) {
                channelTag.putBoolean(
                        "input",
                        enabled(channel, AutoIoDirection.INPUT)
                );
            }

            if (supports(channel, AutoIoDirection.OUTPUT)) {
                channelTag.putBoolean(
                        "output",
                        enabled(channel, AutoIoDirection.OUTPUT)
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

            if (supports(channel, AutoIoDirection.INPUT)
                    && channelTag.contains("input", Tag.TAG_BYTE)) {
                autoInput.put(channel, channelTag.getBoolean("input"));
            }

            if (supports(channel, AutoIoDirection.OUTPUT)
                    && channelTag.contains("output", Tag.TAG_BYTE)) {
                autoOutput.put(channel, channelTag.getBoolean("output"));
            }
        }
    }
}
