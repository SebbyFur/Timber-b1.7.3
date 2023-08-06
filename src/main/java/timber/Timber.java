package timber;

import config.TimberConfig;
import event.TimberEvent;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.craftbukkit.Main;
import org.bukkit.plugin.java.JavaPlugin;


public class Timber extends JavaPlugin {

    private TimberConfig config;

    @Override
    public void onEnable() {
        config = TimberConfig.load(new File("config/timber.yml")); // init config
        if (config == null) {
            setEnabled(false);
            return;
        }

        TimberEvent event = new TimberEvent(this);
        getServer().getPluginManager().registerEvents(event, this); // init event

        Logger.getLogger(Main.class.getName()).log(Level.INFO, "[Timber] Timber started!");
    }

    @Override
    public void onDisable() {
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "[Timber] Bye bye!");
    }

    public TimberConfig getConfig() {
        return config;
    }
}
