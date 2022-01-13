package com.github.sandtechnology.ccattackblocker;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.event.ClientConnectEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class CCAttackBlocker extends Plugin implements Listener {

    Cache<String,Integer> ipAccessingCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
    Map<String,Long> bannedIpMap=new ConcurrentHashMap<>();
    Map<String, Integer> bannedIpCountMap=new ConcurrentHashMap<>();

    public int getBannedIpCount() {
        return bannedIpMap.size();
    }

    @Override
    public void onEnable() {
        new QueryCommand(this,"ccblocker","ccblocker.query","");
        getProxy().getPluginManager().registerListener(this,this);
    }
    @Override
    public void onDisable() {
        getProxy().getPluginManager().unregisterListener(this);
        // Plugin shutdown logic
    }
    private long getBannedExpiredTime(int bannedTimes){
        long bannedExpiredTime=System.currentTimeMillis();
        switch (bannedTimes){
            case 1:
            bannedExpiredTime+=TimeUnit.MINUTES.toMillis(1);
            break;
            case 2:
            bannedExpiredTime+=TimeUnit.MINUTES.toMillis(5);
                break;
            case 3:
            bannedExpiredTime+=TimeUnit.MINUTES.toMillis(10);
                break;
            default:
            bannedExpiredTime+=bannedTimes*TimeUnit.MINUTES.toMillis(10);

        }
        return bannedExpiredTime;
    }
    @EventHandler
    public void onPlayerConnected(ClientConnectEvent event){
        SocketAddress socketAddress=event.getSocketAddress();

        if (socketAddress instanceof InetSocketAddress){
            InetAddress address=((InetSocketAddress) socketAddress).getAddress();
            if(!address.isAnyLocalAddress()) {
                String ip = address.getHostAddress();
                Long bannedExpiredTime=bannedIpMap.get(ip);
                if(bannedExpiredTime!=null){
                    if(bannedExpiredTime<System.currentTimeMillis()){
                        event.setCancelled(true);
                        return;
                    }else {
                        bannedIpMap.remove(ip);
                        ipAccessingCache.invalidate(ip);
                    }
                }
                Integer integer= ipAccessingCache.getIfPresent(ip);
                if(integer!=null){
                    if(integer>=10){
                        int bannedTimes=bannedIpCountMap.merge(ip,1, Integer::sum);
                        bannedIpMap.put(ip,getBannedExpiredTime(bannedTimes));
                        event.setCancelled(true);
                        return;
                    }
                    ipAccessingCache.put(ip,integer+1);
                }else {
                    ipAccessingCache.put(ip,1);
                }
            }
        }
    }
}
