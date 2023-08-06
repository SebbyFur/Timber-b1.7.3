package config;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.craftbukkit.Main;
import org.bukkit.util.config.Configuration;


public class TimberConfig {

    private int maxLogBlocks;
    private boolean autoReplant;

    public static TimberConfig load(File file) {
        Configuration config = new Configuration(file);
        TimberConfig ret = new TimberConfig();

        // Normally what you'd do is check on the read/write perms, but the API ignores all errors related to them.
        // I'm not rewriting this whole thing, so I'm clearly not handling it.
        if (!file.isFile()) {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "[Timber] No configuration file found, creating one");

            config.setProperty("maxLogBlocks", ret.maxLogBlocks);
            config.setProperty("autoReplant", ret.autoReplant);

            config.save();
            return ret;
        } else {
            config.load();
            if ((ret.maxLogBlocks = config.getInt("maxLogBlocks", 0)) <= 0) {
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "[Timber] Invalid config, maxLogBlocks must be a number > 0");
                return null;
            }

            Object autoReplant = config.getProperty("autoReplant");
            if (autoReplant == null || !(autoReplant instanceof Boolean)) {
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "[Timber] Invalid config, autoReplant must be a boolean");
                return null;
            }
            ret.autoReplant = (Boolean)autoReplant; 
        }

        return ret;
    }

    public TimberConfig(int maxLogBlocks, boolean autoReplant) {
        this.maxLogBlocks = maxLogBlocks;
        this.autoReplant = autoReplant;
    }

    public TimberConfig() {
        this(25, true);
    }

    public int getMaxLogBlocks() {
        return maxLogBlocks;
    }

    public boolean getAutoReplant() {
        return autoReplant;
    }
}
