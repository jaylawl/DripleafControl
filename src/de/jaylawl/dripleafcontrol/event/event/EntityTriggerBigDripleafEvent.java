package de.jaylawl.dripleafcontrol.event.event;

import de.jaylawl.dripleafcontrol.DripleafControl;
import de.jaylawl.dripleafcontrol.util.ConfigurableData;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.BigDripleaf;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class EntityTriggerBigDripleafEvent extends EntityEvent implements Cancellable {

    public enum TiltBehaviour {
        VANILLA, CUSTOM
    }

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Block block;
    private boolean cancelled = false;
    private TiltBehaviour tiltBehaviour;
    private final HashMap<BigDripleaf.Tilt, Integer> ticksUntilState = new HashMap<>();

    public EntityTriggerBigDripleafEvent(final @NotNull Entity entity, final @NotNull Block block, final @NotNull TiltBehaviour tiltBehaviour) {
        super(entity);
        this.block = block;
        this.tiltBehaviour = tiltBehaviour;
        final ConfigurableData configurableData = DripleafControl.getInstance().getConfigurableData();
        this.ticksUntilState.put(BigDripleaf.Tilt.UNSTABLE, configurableData.ticksUntilUnstableTilt);
        this.ticksUntilState.put(BigDripleaf.Tilt.PARTIAL, configurableData.ticksUntilPartialTilt);
        this.ticksUntilState.put(BigDripleaf.Tilt.FULL, configurableData.ticksUntilFullTilt);
        this.ticksUntilState.put(BigDripleaf.Tilt.NONE, configurableData.ticksUntilNoneTilt);
    }

    //

    public @NotNull Block getBlock() {
        return this.block;
    }

    public @NotNull TiltBehaviour getTiltBehaviour() {
        return this.tiltBehaviour;
    }

    public void setTiltBehaviour(final @NotNull TiltBehaviour tiltBehaviour) {
        this.tiltBehaviour = tiltBehaviour;
    }

    public int getTicksUntilState(final @NotNull BigDripleaf.Tilt tilt) {
        return this.ticksUntilState.get(tilt);
    }

    public void setTicksUntilState(final @NotNull BigDripleaf.Tilt tilt, final int ticks) {
        this.ticksUntilState.put(tilt, ticks);
    }

    @Deprecated
    public @NotNull HashMap<Integer, BigDripleaf.Tilt> getInvertedTicksUntilStateMap() {
        final HashMap<Integer, BigDripleaf.Tilt> invertedTicksUntilStateMap = new HashMap<>();
        for (final BigDripleaf.Tilt tilt : this.ticksUntilState.keySet()) {
            invertedTicksUntilStateMap.put(this.ticksUntilState.get(tilt), tilt);
        }
        return invertedTicksUntilStateMap;
    }

    //

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
