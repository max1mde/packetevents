/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.retrooper.packetevents.spigot;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.event.simple.*;
import com.github.retrooper.packetevents.util.TimeStampMode;
import io.github.retrooper.packetevents.spigot.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public class PacketEventsPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        //Register your listeners
        PacketEvents.getAPI().getSettings().debug(false).bStats(true).checkForUpdates(true).timeStampMode(TimeStampMode.MILLIS).reEncodeByDefault(true);
        PacketEvents.getAPI().init();
        SimplePacketListenerAbstract listener = new SimplePacketListenerAbstract(PacketListenerPriority.HIGH) {
            @Override
            public void onPacketLoginSend(PacketLoginSendEvent event) {
            }

            @Override
            public void onPacketConfigReceive(PacketConfigReceiveEvent event) {
            }

            @Override
            public void onPacketConfigSend(PacketConfigSendEvent event) {
            }

            @Override
            public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
            }

            @Override
            public void onPacketPlaySend(PacketPlaySendEvent event) {
            }

            @Override
            public void onUserConnect(UserConnectEvent event) {
                PacketEvents.getAPI().getLogManager().debug("User: (host-name) " + event.getUser().getAddress().getHostString() + " connected...");
            }

            @Override
            public void onUserLogin(UserLoginEvent event) {
                PacketEvents.getAPI().getLogManager().debug("You logged in! User name: " + event.getUser().getProfile().getName());
            }

            @Override
            public void onUserDisconnect(UserDisconnectEvent event) {
                PacketEvents.getAPI().getLogManager().debug("User: (host-name) " + event.getUser().getAddress().getHostString() + " disconnected...");
            }
        };
        //PacketEvents.getAPI().getEventManager().registerListener(listener);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}
