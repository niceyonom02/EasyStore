import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class EasyStore extends JavaPlugin {
    public static EasyStore EasyStore;
    private CustomEconomy customEconomy;

    @Override
    public void onEnable(){
        EasyStore = this;
        registerConfig();
        customEconomy = (CustomEconomy) Bukkit.getPluginManager().getPlugin("customeconomy");
        new StoreManager();

    }

    private void registerConfig(){
        File f = new File(getDataFolder(), File.separator + "config.yml");
        if(!f.exists()) {
            saveResource("config.yml", false);
        }
    }

    public CustomEconomy getEconomy() {
        return customEconomy;
    }

    @Override
    public void onDisable(){

    }
}
