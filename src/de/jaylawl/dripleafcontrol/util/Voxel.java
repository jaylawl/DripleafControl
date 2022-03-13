package de.jaylawl.dripleafcontrol.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Voxel(String worldName, int x, int y, int z) {

    public Voxel(@NotNull String worldName, int x, int y, int z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static @NotNull Voxel fromLocation(@NotNull Location location) {
        return new Voxel(
                location.getWorld().getName(),
                (int) Math.floor(location.getX()),
                (int) Math.floor(location.getY()),
                (int) Math.floor(location.getZ())
        );
    }

    public @NotNull Vector toVector() {
        return new Vector(this.x, this.y, this.z);
    }

    public @Nullable Location toLocation() {
        World world = Bukkit.getWorld(this.worldName);
        return world != null ? new Location(world, this.x, this.y, this.z) : null;
    }

    //

    @Override
    public boolean equals(Object o) {
        return o instanceof Voxel other &&
                this.worldName.equals(other.worldName) &&
                this.x == other.x &&
                this.y == other.y &&
                this.z == other.z;
    }

}
