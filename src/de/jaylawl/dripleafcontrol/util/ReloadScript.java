package de.jaylawl.dripleafcontrol.util;

import de.jaylawl.dripleafcontrol.DripleafControl;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class ReloadScript extends IReloadScript {

    private final FileUtil fileUtil;
    private final ConfigurableData configurableData;

    public ReloadScript() {
        super(DripleafControl.getInstance());
        final DripleafControl dripleafControl = (DripleafControl) this.pluginInstance;
        this.fileUtil = dripleafControl.getFileUtil();
        this.configurableData = dripleafControl.getConfigurableData();
    }

    //

    @Override
    public void finalSyncTasks() {

        // config.yml
        configFileSection:
        {
            final File configFile = new File(this.fileUtil.getBaseDirectoryPath() + "/config.yml");
            switch (this.fileUtil.ensureFileExists(configFile)) {
                case FILE_EXISTS -> {
                }
                case MKDIR_ERROR, CREATE_FILE_ERROR -> {
                    this.logger.warning("Unable to load data from \"" + configFile + "\":");
                    this.logger.warning("File could neither be found nor generated automatically");
                    this.totalWarnings++;
                    break configFileSection;
                }
                case CREATED_DIR_AND_FILE, CREATED_FILE -> {
                    this.logger.info("Successfully generated missing file \"" + configFile + "\"");
                }
            }

            final YamlConfiguration yaml = new YamlConfiguration();
            try {
                yaml.load(configFile);
            } catch (IOException | InvalidConfigurationException exception) {
                this.logger.warning(exception.getClass().getSimpleName() + " was thrown while trying to read \"" + configFile + "\" as yaml file");
                this.totalWarnings++;
                break configFileSection;
            }

            final YamlConfiguration defaultConfigYaml = new YamlConfiguration();
            final InputStream inputStream = getClass().getResourceAsStream("/de/jaylawl/dripleafcontrol/resources/config.yml");
            if (inputStream == null) {
                this.logger.warning("Unable to find default config within plugins \"resources\" package");
                this.totalWarnings++;
                break configFileSection;
            } else {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                try {
                    defaultConfigYaml.load(bufferedReader);
                } catch (InvalidConfigurationException | IOException exception) {
                    this.logger.warning(exception.getClass().getSimpleName() + " was thrown while trying to read the default config as yaml file");
                    this.totalWarnings++;
                    exception.printStackTrace();
                    break configFileSection;
                }
            }

            // Populate with missing default values:
            {
                boolean populatedFileWithDefaultValues = false;
                for (final String key : defaultConfigYaml.getKeys(true)) {
                    if (yaml.get(key) == null) {
                        yaml.set(key, defaultConfigYaml.get(key));
                        populatedFileWithDefaultValues = true;
                    }
                }
                if (populatedFileWithDefaultValues) {
                    try {
                        yaml.save(configFile);
                        this.logger.info("Updated file \"" + configFile + "\" with missing values from default config");
                    } catch (IOException exception) {
                        this.logger.warning(exception.getClass().getSimpleName() + " was thrown while trying to save updated file \"" + configFile + "\":");
                        exception.printStackTrace();
                        this.totalWarnings++;
                    }
                }
            }

            this.configurableData.ticksUntilUnstableTilt = yaml.getInt("BigDripleaf.TiltProcess.TicksUntilState.UNSTABLE", ConfigurableData.DEFAULT_TICKS_UNTIL_UNSTABLE_TILT);
            this.configurableData.ticksUntilPartialTilt = yaml.getInt("BigDripleaf.TiltProcess.TicksUntilState.PARTIAL", ConfigurableData.DEFAULT_TICKS_UNTIL_PARTIAL_TILT);
            this.configurableData.ticksUntilFullTilt = yaml.getInt("BigDripleaf.TiltProcess.TicksUntilState.FULL", ConfigurableData.DEFAULT_TICKS_UNTIL_FULL_TILT);
            this.configurableData.ticksUntilNoneTilt = yaml.getInt("BigDripleaf.TiltProcess.TicksUntilState.NONE", ConfigurableData.DEFAULT_TICKS_UNTIL_NONE_TILT);
            boolean revertToDefaultValues = false;
            if (this.configurableData.ticksUntilUnstableTilt < 0 ||
                    this.configurableData.ticksUntilPartialTilt < 0 ||
                    this.configurableData.ticksUntilFullTilt < 0 ||
                    this.configurableData.ticksUntilNoneTilt < 0) {
                this.logger.warning("Tick values provided must be greater than or equal to 0");
                this.totalWarnings++;
                revertToDefaultValues = true;
            }
            if (this.configurableData.ticksUntilPartialTilt <= this.configurableData.ticksUntilUnstableTilt) {
                this.logger.warning("\"TicksUntilState.PARTIAL\" must be greater than \"TicksUntilState.UNSTABLE\"");
                this.totalWarnings++;
                revertToDefaultValues = true;
            }
            if (this.configurableData.ticksUntilFullTilt <= this.configurableData.ticksUntilPartialTilt) {
                this.logger.warning("\"TicksUntilState.FULL\" must be greater than \"TicksUntilState.PARTIAL\"");
                this.totalWarnings++;
                revertToDefaultValues = true;
            }
            if (this.configurableData.ticksUntilNoneTilt <= this.configurableData.ticksUntilFullTilt) {
                this.logger.warning("\"TicksUntilState.NONE\" must be greater than \"TicksUntilState.FULL\"");
                this.totalWarnings++;
                revertToDefaultValues = true;
            }
            if (revertToDefaultValues) {
                this.logger.warning("Reverting to default \"TicksUntilState\" values...");
                this.configurableData.ticksUntilUnstableTilt = ConfigurableData.DEFAULT_TICKS_UNTIL_UNSTABLE_TILT;
                this.configurableData.ticksUntilPartialTilt = ConfigurableData.DEFAULT_TICKS_UNTIL_PARTIAL_TILT;
                this.configurableData.ticksUntilFullTilt = ConfigurableData.DEFAULT_TICKS_UNTIL_FULL_TILT;
                this.configurableData.ticksUntilNoneTilt = ConfigurableData.DEFAULT_TICKS_UNTIL_NONE_TILT;
            }

        }

    }

    @Override
    public void finish() {
        this.logger.info("Reload completed within " + this.elapsedSeconds + " s. and with " + this.totalWarnings + " warning(s)");
        this.logger.info("+ Configured ticks until states unstable/partial/full/none: " +
                this.configurableData.ticksUntilUnstableTilt + "/" +
                this.configurableData.ticksUntilPartialTilt + "/" +
                this.configurableData.ticksUntilFullTilt + "/" +
                this.configurableData.ticksUntilNoneTilt);

        notifySubscribers(getSubscriberNotification());
    }

}
