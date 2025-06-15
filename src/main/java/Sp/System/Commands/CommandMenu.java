package Sp.System.Commands;

import Sp.System.Manager.MenuManager;
import Sp.System.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMenu implements CommandExecutor {
    private final MenuManager menuManager;

    public CommandMenu(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtils.getColoredMessage("&cSolo jugadores"));
            return true;
        }

        menuManager.openMainMenu(player);
        return true;
    }
}