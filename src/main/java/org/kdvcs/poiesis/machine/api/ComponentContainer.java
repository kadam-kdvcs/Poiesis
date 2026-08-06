package org.kdvcs.poiesis.machine.api;

import net.minecraft.nbt.CompoundTag;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ComponentContainer {
    private final Map<Class<?>, MachineComponent> components = new LinkedHashMap<>();

    public <T extends MachineComponent> T add(Class<T> type, T component) {
        if (components.putIfAbsent(type, component) != null) {
            throw new IllegalStateException("Duplicate machine component: " + type.getName());
        }
        return component;
    }

    public <T extends MachineComponent> T require(Class<T> type) {
        MachineComponent component = components.get(type);
        if (component == null) {
            throw new IllegalStateException("Missing machine component: " + type.getName());
        }
        return type.cast(component);
    }

    public void tickServer(MachineContext context) {
        components.values().forEach(component -> component.tickServer(context));
    }

    public void save(CompoundTag root) {
        CompoundTag componentsTag = new CompoundTag();
        components.values().forEach(component -> {
            CompoundTag componentTag = new CompoundTag();
            component.save(componentTag);
            componentsTag.put(component.key(), componentTag);
        });
        root.put("components", componentsTag);
    }

    public void load(CompoundTag root) {
        CompoundTag componentsTag = root.getCompound("components");
        components.values().forEach(component ->
                component.load(componentsTag.getCompound(component.key())));
    }

    public void invalidate() {
        components.values().forEach(MachineComponent::invalidate);
    }
}
