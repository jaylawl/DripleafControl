package de.jaylawl.dripleafcontrol.event.listener;

import de.jaylawl.dripleafcontrol.DripleafControl;
import de.jaylawl.dripleafcontrol.event.event.EntityTriggerBigDripleafEvent;
import de.jaylawl.dripleafcontrol.util.Voxel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.BigDripleaf;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DripleafListener implements Listener {

    private final ConcurrentHashMap<Voxel, BukkitRunnable> tiltProcesses = new ConcurrentHashMap<>();

    public DripleafListener() {
    }

    //

    public void terminateTiltProcesses() {
        final List<Voxel> voxels = new ArrayList<>();
        this.tiltProcesses.keySet().forEach(voxel -> {
            this.tiltProcesses.get(voxel).cancel();
            voxels.add(voxel);
        });
        voxels.forEach(voxel -> {
            final Location location = voxel.toLocation();
            if (location != null) {
                final Block block = location.getBlock();
                if (block.getType() == Material.BIG_DRIPLEAF) {
                    final BigDripleaf blockData = (BigDripleaf) block.getBlockData().clone();
                    blockData.setTilt(BigDripleaf.Tilt.NONE);
                    block.setBlockData(blockData, false);
                }
            }
        });
        this.tiltProcesses.clear();
    }

    //

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(final @NotNull PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        final Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (!block.getType().equals(Material.BIG_DRIPLEAF)) {
            return;
        }
        final BigDripleaf blockData = (BigDripleaf) block.getBlockData();
        if (blockData.getTilt().equals(BigDripleaf.Tilt.NONE)) {
            return;
        }
        if (!this.tiltProcesses.containsKey(Voxel.fromLocation(block.getLocation()))) {
            blockData.setTilt(BigDripleaf.Tilt.NONE);
            block.setBlockData(blockData, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(final @NotNull EntityChangeBlockEvent event) {

        final Block block = event.getBlock();
        if (!block.getType().equals(Material.BIG_DRIPLEAF)) {
            return;
        }
        if (!((BigDripleaf) block.getBlockData()).getTilt().equals(BigDripleaf.Tilt.NONE)) {
            // Nothing to do if the block is already in a tilted state
            return;
        }
        final Voxel blockVoxel = Voxel.fromLocation(block.getLocation());
        BukkitRunnable bukkitRunnable = this.tiltProcesses.get(blockVoxel);
        if (bukkitRunnable != null && !bukkitRunnable.isCancelled()) {
            // Block already has a process handling its tilt behaviour
            return;
        }

        final EntityTriggerBigDripleafEvent entityTriggerBigDripleafEvent = new EntityTriggerBigDripleafEvent(
                event.getEntity(), block,
                EntityTriggerBigDripleafEvent.TiltBehaviour.CUSTOM
        );
        Bukkit.getPluginManager().callEvent(entityTriggerBigDripleafEvent);

        if (entityTriggerBigDripleafEvent.isCancelled()) {
            // Block will not tilt at all
            event.setCancelled(true);
            return;
        } else if (entityTriggerBigDripleafEvent.getTiltBehaviour().equals(EntityTriggerBigDripleafEvent.TiltBehaviour.VANILLA)) {
            // Block will tilt directed by vanilla mechanics
            event.setCancelled(false);
            return;
        }

        bukkitRunnable = new BukkitRunnable() {
            private final Voxel voxel = blockVoxel;
            private final HashMap<Integer, BigDripleaf.Tilt> ticksUntilStateMap = entityTriggerBigDripleafEvent.getInvertedTicksUntilStateMap();
            private int tick = -1;
            private final int maxTicks = this.ticksUntilStateMap.keySet().stream().max(Integer::compareTo).orElse(0);

            @Override
            public void run() {
                this.tick++;

                final BigDripleaf.Tilt tilt = this.ticksUntilStateMap.get(this.tick);

                if (tilt != null) {
                    final Location location = this.voxel.toLocation();
                    if (location != null) {
                        final Block block = location.getWorld().getBlockAt(location);
                        if (block.getType().equals(Material.BIG_DRIPLEAF)) {
                            final BigDripleaf blockData = (BigDripleaf) block.getBlockData().clone();
                            blockData.setTilt(tilt);
                            block.setBlockData(blockData, false);
                            location.getWorld().playSound(
                                    location,
                                    (tilt == BigDripleaf.Tilt.NONE ? Sound.BLOCK_BIG_DRIPLEAF_TILT_UP : Sound.BLOCK_BIG_DRIPLEAF_TILT_DOWN),
                                    SoundCategory.BLOCKS,
                                    1f, // volume
                                    1f // pitch
                            );
                        }
                    }
                }

                if (tilt == BigDripleaf.Tilt.NONE || this.tick >= this.maxTicks) {
                    DripleafListener.this.tiltProcesses.remove(this.voxel);
                    cancel();
                }
            }

        };

        this.tiltProcesses.put(blockVoxel, bukkitRunnable);
        bukkitRunnable.runTaskTimer(DripleafControl.getInstance(), 0L, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(final @NotNull ChunkUnloadEvent event) {
        final long eventChunkKey = event.getChunk().getChunkKey();
        this.tiltProcesses.keySet().forEach(voxel -> {
            final Location location = voxel.toLocation();
            if (location != null) {
                if (Chunk.getChunkKey(location) == eventChunkKey) {
                    final Block block = location.getBlock();
                    if (block.getType() == Material.BIG_DRIPLEAF) {
                        final BigDripleaf blockData = (BigDripleaf) block.getBlockData().clone();
                        blockData.setTilt(BigDripleaf.Tilt.NONE);
                        block.setBlockData(blockData, false);
                    }
                    this.tiltProcesses.remove(voxel);
                }
            }
        });
    }

}
