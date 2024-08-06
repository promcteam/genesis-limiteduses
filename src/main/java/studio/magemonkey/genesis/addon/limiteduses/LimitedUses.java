package studio.magemonkey.genesis.addon.limiteduses;

import org.bukkit.command.CommandSender;
import studio.magemonkey.genesis.api.GenesisAddon;

public class LimitedUses extends GenesisAddon {

    private GenesisListener    listener;
    private LimitedUsesManager manager;

    @Override
    public String getAddonName() {
        return "LimitedUses";
    }

    @Override
    public String getRequiredGenesisVersion() {
        return "1.0.0";
    }

    @Override
    public void enableAddon() {
        getCommand("limiteduses").setExecutor(new CommandManager(this));
        manager = new LimitedUsesManager(this);
        listener = new GenesisListener(this, manager);
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void genesisFinishedLoading() {
        listener.enable();
    }


    @Override
    public void disableAddon() {
        listener.disable(); //includes saving
    }

    @Override
    public void genesisReloaded(CommandSender sender) {
        listener.disable(); //includes saving
        listener.enable();
    }


    public LimitedUsesManager getLimitedUsesManager() {
        return manager;
    }


}
