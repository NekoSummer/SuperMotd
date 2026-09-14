package tk.complexicon.supermotd;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements Listener {
    private BukkitAudiences audiences;

    private @NotNull BukkitAudiences audiences() {
        if (audiences == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return audiences;
    }

    Logger l = getLogger();
    FileConfiguration cfg = getConfig();

    String heading;
    List<String> motd;

    @Override
    public void onEnable() {
        audiences = BukkitAudiences.create(this);
        l.info("Registering Events...");
        Bukkit.getPluginManager().registerEvents(this, this);

        l.info("Loading Config...");
        cfg.addDefault("heading", "&cSuperMOTD Default Heading");
        cfg.addDefault("motdlist", Arrays.asList("&aMOTD Random Line1", "&aMOTD Random Line2", "&aMOTD Random Line3"));
        cfg.options().copyDefaults(true);
        saveConfig();
        heading = cfg.getString("heading");
        motd = cfg.getStringList("motdlist");

        audiences.console().sendMessage(LegacyComponentSerializer
                .legacyAmpersand()
                .deserialize("Setting MOTD Heading to: " + heading)
        );

        l.info("Loaded!");
    }

    @Override
    public void onDisable() {
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("reloadmotd")){
            if(sender.hasPermission("supermotd.reloadmotd")){
                Audience audSender = audiences.sender(sender);
                audSender.sendMessage(Component.text("Reloading MOTD Config...", NamedTextColor.GREEN));
                reloadConfig();
                cfg = getConfig();
                heading = cfg.getString("heading");
                motd = cfg.getStringList("motdlist");
                audSender.sendMessage(Component.text("New MOTD Heading: " + heading));
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onServerPing(ServerListPingEvent e){
        Random r = new Random();
        String randString = motd.get(r.nextInt(motd.size()));
        String fullMotd = heading + "\n" + randString;

        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(fullMotd);

        String legacyString = LegacyComponentSerializer.legacySection().serialize(component);
        e.setMotd(legacyString);
    }

}
