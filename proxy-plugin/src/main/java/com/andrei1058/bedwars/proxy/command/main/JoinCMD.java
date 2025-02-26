package com.andrei1058.bedwars.proxy.command.main;

import com.andrei1058.bedwars.proxy.arenamanager.ArenaManager;
import com.andrei1058.bedwars.proxy.api.ArenaStatus;
import com.andrei1058.bedwars.proxy.api.CachedArena;
import com.andrei1058.bedwars.proxy.command.SubCommand;
import com.andrei1058.bedwars.proxy.api.Messages;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.andrei1058.bedwars.proxy.BedWarsProxy.getParty;
import static com.andrei1058.bedwars.proxy.language.Language.getMsg;

public class JoinCMD extends SubCommand {

    public JoinCMD(String name, String permission) {
        super(name, permission);
    }

    @Override
    public void execute(CommandSender s, String[] args) {
        if (s instanceof ConsoleCommandSender) return;
        Player p = (Player) s;
        if (args.length < 1) {
            s.sendMessage(getMsg(p, Messages.COMMAND_JOIN_USAGE));
            return;
        }
        if (args[0].equalsIgnoreCase("random")) {
            if (!ArenaManager.getInstance().joinRandomArena(p)) {
                s.sendMessage(getMsg(p, Messages.COMMAND_JOIN_NO_EMPTY_FOUND));
            }
            return;
        }
        if (ArenaManager.hasGroup(args[0])) {
            ArenaManager.getInstance().joinRandomFromGroup(p, args[0]);
            return;
        } else  {
            if (getParty().hasParty(p.getUniqueId()) && !getParty().isOwner(p.getUniqueId())) {
                p.sendMessage(getMsg(p, Messages.COMMAND_JOIN_DENIED_NOT_PARTY_LEADER));
                return;
            }

            final CachedArena arena = ArenaManager.getArenas().stream()
                .filter(a -> a.getArenaName().contains(args[0]))
                .filter(a -> a.getStatus() == ArenaStatus.WAITING || a.getStatus() == ArenaStatus.STARTING)
                .filter(a -> a.getCurrentPlayers() + 1 <= a.getMaxPlayers())
                .max(Comparator.comparingInt(CachedArena::getCurrentPlayers))
                .orElse(null);

            if (arena == null) {
                s.sendMessage(getMsg(p, Messages.COMMAND_JOIN_GROUP_OR_ARENA_NOT_FOUND).replace("{name}", args[0]));
                return;
            }

            arena.addPlayer(p, null);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender s, String alias, String[] args, Location location) {
        List<String> tab = new ArrayList<>();
        for (CachedArena ad : ArenaManager.getArenas()) {
            if (!tab.contains(ad.getArenaGroup())) tab.add(ad.getArenaGroup());
        }
        for (CachedArena arena : ArenaManager.getArenas()) {
            tab.add(arena.getArenaName());
        }
        return tab;
    }
}
