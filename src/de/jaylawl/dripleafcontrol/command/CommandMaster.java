package de.jaylawl.dripleafcontrol.command;

import de.jaylawl.dripleafcontrol.DripleafControl;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CommandMaster implements CommandExecutor, TabCompleter {

    public CommandMaster() {
    }

    //

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] arguments) {
        if (!DripleafControl.getInstance().reload(commandSender)) {
            commandSender.sendMessage(ChatColor.RED + "Plugin is already reloading. You will be notified upon completion...");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] arguments) {
        return Collections.emptyList();
    }

}
