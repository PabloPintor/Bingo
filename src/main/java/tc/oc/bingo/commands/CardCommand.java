package tc.oc.bingo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import tc.oc.bingo.Bingo;
import tc.oc.bingo.config.Config;
import tc.oc.bingo.menu.BingoCardMenu;

@CommandAlias("bingo")
public class CardCommand extends BaseCommand {

  @Default
  @CommandPermission("bingo.card")
  public void bingoCard(CommandSender sender, @Optional Integer index) {
    if (sender instanceof Player) {
      Player player = (Player) sender;

      if (!Bingo.get().isBingoCardLoaded(player.getUniqueId())) {
        sender.sendMessage("Your Bingo Card has not yet loaded, please try again.");
        return;
      }

      Bingo.get()
          .loadBingoCard(player.getUniqueId())
          .whenComplete(
              (bingoPlayerCard, throwable) -> {
                if (throwable == null) {
                  throwable.printStackTrace();
                  sender.sendMessage("An error occured when running this command");
                  return;
                }

                BingoCardMenu.get(bingoPlayerCard, index).open(player);
              });
    }
  }

  @Subcommand("show")
  @CommandPermission("bingo.card.other")
  public void bingoCardOther(CommandSender sender, OnlinePlayer target) {
    if (sender instanceof Player) {
      Player senderPlayer = (Player) sender;
      Player targetPlayer = target.getPlayer();

      Bingo.get()
          .getBingoCard(targetPlayer.getUniqueId())
          .whenComplete(
              (bingoPlayerCard, throwable) -> {
                if (throwable == null) {
                  throwable.printStackTrace();
                  sender.sendMessage("An error occured when running this command");
                  return;
                }

                BingoCardMenu.get(bingoPlayerCard).open(senderPlayer);
              });
    }
  }

  @Subcommand("resync")
  @CommandPermission("bingo.reload")
  public void bingoResync(CommandSender sender) {
    sender.sendMessage("Fetching updated Bingo card data.");
    Bingo.get().loadBingoCard();
  }

  @Subcommand("reload")
  @CommandPermission("bingo.reload")
  public void bingoReload(CommandSender sender) {
    sender.sendMessage("Reloading Bingo config file.");
    Bingo.get().reloadConfig();
    FileConfiguration config = Bingo.get().getConfig();
    Config.create(config);
    Bingo.get().loadTrackerConfigs(config);
  }
}
