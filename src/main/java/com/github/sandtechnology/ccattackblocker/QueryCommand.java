package com.github.sandtechnology.ccattackblocker;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class QueryCommand extends Command {
    CCAttackBlocker plugin;
    public QueryCommand(CCAttackBlocker plugin,String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin=plugin;
        plugin.getProxy().getPluginManager().registerCommand(plugin,this);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length<=1){
            sender.sendMessage("Invalid args, use list/info/get/remove command instead");
        }else switch (args[0].toLowerCase(Locale.ROOT)){
            case "list":
                sender.sendMessage("Blocked Ip List:\n"+ String.join("\n", plugin.bannedIpMap.keySet()));
                break;
            case "info":
                sender.sendMessage("Blocked Ips: "+plugin.getBannedIpCount()+", Banned Counts"+ plugin.bannedIpCountMap.values().parallelStream().mapToInt(Integer::intValue).sum());
                break;
            case "get":
                if(args.length==2) {
                    sender.sendMessage("IP Info for "+args[1]);
                    sender.sendMessage("Banned "+plugin.bannedIpCountMap.getOrDefault(args[1],0)+" Times,"+" connected "+plugin.ipAccessingCache.getIfPresent(args[1])+" Times in the last minutes");
                    long bannedExpiredAfter=System.currentTimeMillis()-plugin.bannedIpMap.getOrDefault(args[1],0L);
                    if(bannedExpiredAfter>0L){
                        sender.sendMessage("Unbanned after "+ TimeUnit.MINUTES.convert(bannedExpiredAfter,TimeUnit.MILLISECONDS)+ " minutes");
                    }
                }else {
                        sender.sendMessage("Please provide a ip for lookup");
                }
                break;
            case "remove":
                if(args.length==2) {
                    plugin.ipAccessingCache.invalidate(args[1]);
                    plugin.bannedIpMap.remove(args[1]);
                    sender.sendMessage("Succeed unblock ip "+args[1]);
                }else {
                    sender.sendMessage("Please provide a ip for unblock");
                }
                break;
            default:
                sender.sendMessage("Invalid args, use list/get/remove command instead");
        }
    }
}
