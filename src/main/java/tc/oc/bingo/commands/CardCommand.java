package tc.oc.bingo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import java.util.Collections;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import tc.oc.bingo.Bingo;
import tc.oc.bingo.config.Config;
import tc.oc.bingo.database.ObjectiveItem;
import tc.oc.bingo.menu.BingoCardMenu;
import tc.oc.bingo.util.Exceptions;
import tc.oc.pgm.api.PGM;

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

      Exceptions.handle(
          Bingo.get()
              .getPlayerCard(player.getUniqueId())
              .whenComplete(
                  (bingoPlayerCard, throwable) -> {
                    if (throwable != null) {
                      throwable.printStackTrace();
                      sender.sendMessage("An error occurred when running this command");
                      return;
                    }

                    BingoCardMenu.get(bingoPlayerCard, index).open(player);
                  }));
    }
  }

  @Subcommand("show")
  @CommandPermission("bingo.card.other")
  public void bingoCardOther(CommandSender sender, OnlinePlayer target) {
    if (sender instanceof Player) {
      Player senderPlayer = (Player) sender;
      Player targetPlayer = target.getPlayer();

      Exceptions.handle(
          Bingo.get()
              .getPlayerCard(targetPlayer.getUniqueId())
              .whenCompleteAsync(
                  (bingoPlayerCard, throwable) -> {
                    if (throwable != null) {
                      throwable.printStackTrace();
                      sender.sendMessage("An error occurred when running this command");
                      return;
                    }

                    BingoCardMenu.get(bingoPlayerCard).open(senderPlayer);
                  },
                  PGM.get().getExecutor()));
    }
  }

  @Subcommand("complete")
  @CommandPermission("bingo.complete")
  public void bingoCardComplete(CommandSender sender, OnlinePlayer target, int index) {
    ObjectiveItem objectiveItem = Bingo.get().getBingoCard().getObjectiveByIndex(index);

    if (objectiveItem == null) {
      sender.sendMessage("Unable to find an objective with that index.");
      return;
    }

    Bingo.get()
        .getRewards()
        .rewardPlayers(objectiveItem.getSlug(), Collections.singletonList(target.getPlayer()));
  }

  @Subcommand("refresh")
  @CommandPermission("bingo.reload")
  public void bingoCardRefresh(CommandSender sender, OnlinePlayer target) {
    if (sender instanceof Player) {
      Player targetPlayer = target.getPlayer();

      Bingo.get()
          .loadPlayerCard(targetPlayer.getUniqueId())
          .whenCompleteAsync(
              (bingoPlayerCard, throwable) -> {
                if (throwable != null) {
                  throwable.printStackTrace();
                  sender.sendMessage("An error occurred when running this command");
                  return;
                }
                sender.sendMessage("Bingo card data updated for " + targetPlayer.getName() + ".");
              },
              PGM.get().getExecutor());
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
    Config.load(config);
    Bingo.get().loadTrackerConfigs(config);
  }
}
