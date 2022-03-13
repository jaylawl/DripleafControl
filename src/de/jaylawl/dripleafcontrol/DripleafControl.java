package de.jaylawl.dripleafcontrol;

import de.jaylawl.dripleafcontrol.command.CommandMaster;
import de.jaylawl.dripleafcontrol.event.listener.DripleafListener;
import de.jaylawl.dripleafcontrol.util.ConfigurableData;
import de.jaylawl.dripleafcontrol.util.FileUtil;
import de.jaylawl.dripleafcontrol.util.ReloadScript;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class DripleafControl extends JavaPlugin {

    private static DripleafControl INSTANCE;

    private DripleafListener dripleafListener;

    private ConfigurableData configurableData;
    private FileUtil fileUtil;
    private ReloadScript latestReloadScript = null;

    public DripleafControl() {
    }

    //

    @Override
    public void onEnable() {

        INSTANCE = this;

        final Logger logger = getLogger();
        final PluginManager pluginManager = getServer().getPluginManager();

        this.fileUtil = new FileUtil(this);
        if (!this.fileUtil.createDirectories()) {
            logger.warning("Issue(s) occurred while trying to generate the plugins directories");
        }

        final PluginCommand masterCommand = getCommand("dripleafcontrol");
        if (masterCommand != null) {
            final CommandMaster commandMaster = new CommandMaster();
            masterCommand.setExecutor(commandMaster);
            masterCommand.setTabCompleter(commandMaster);
        } else {
            logger.severe("Unable to find plugins master command");
            logger.severe("Disabling plugin...");
            pluginManager.disablePlugin(this);
            return;
        }

        this.configurableData = new ConfigurableData();

        this.dripleafListener = new DripleafListener();
        pluginManager.registerEvents(this.dripleafListener, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!reload(getServer().getConsoleSender())) {
                    logger.warning("Initial reload of plugin data failed");
                    logger.warning("Try running the plugins reload command manually");
                }
            }
        }.runTaskLater(this, 1L);

    }

    @Override
    public void onDisable() {
        this.dripleafListener.terminateTiltProcesses();
    }

    //

    public boolean reload(@NotNull CommandSender issuer) {
        if (this.latestReloadScript != null && !this.latestReloadScript.isConcluded()) {
            if (issuer instanceof Player player) {
                this.latestReloadScript.addSubscriber(player.getUniqueId());
            }
            return false;
        }
        this.latestReloadScript = new ReloadScript();
        if (issuer instanceof Player player) {
            this.latestReloadScript.addSubscriber(player.getUniqueId());
        }
        this.latestReloadScript.run();
        return true;
    }

    //

    public static DripleafControl getInstance() {
        return INSTANCE;
    }

    public DripleafListener getDripleafListener() {
        return this.dripleafListener;
    }

    public FileUtil getFileUtil() {
        return this.fileUtil;
    }

    public ConfigurableData getConfigurableData() {
        return this.configurableData;
    }

}
