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

package io.github.retrooper.packetevents.velocity.injector;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.UserConnectEvent;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import io.github.retrooper.packetevents.velocity.handlers.PacketEventsDecoder;
import io.github.retrooper.packetevents.velocity.handlers.PacketEventsEncoder;
import io.netty.channel.Channel;

public class ServerConnectionInitializer {
    public static void addChannelHandlers(Channel channel, PacketEventsDecoder decoder, PacketEventsEncoder encoder) {
        channel.pipeline().addBefore("minecraft-decoder", PacketEvents.DECODER_NAME, decoder);
        channel.pipeline().addBefore("minecraft-encoder", PacketEvents.ENCODER_NAME, encoder);
    }

    public static void initChannel(Channel channel, ConnectionState state) {
        User user = new User(channel, state, null, new UserProfile(null, null));
        UserConnectEvent connectEvent = new UserConnectEvent(user);
        PacketEvents.getAPI().getEventManager().callEvent(connectEvent);
        if (connectEvent.isCancelled()) {
            channel.unsafe().closeForcibly();
            return;
        }
        PacketEventsDecoder decoder = new PacketEventsDecoder(user);
        PacketEventsEncoder encoder = new PacketEventsEncoder(user);
        addChannelHandlers(channel, decoder, encoder);
        PacketEvents.getAPI().getProtocolManager().setUser(channel, user);
    }

    public static void destroyChannel(Channel channel) {
        User user = PacketEvents.getAPI().getProtocolManager().getUser(channel);
        UserDisconnectEvent disconnectEvent = new UserDisconnectEvent(user);
        PacketEvents.getAPI().getEventManager().callEvent(disconnectEvent);
        channel.pipeline().remove(PacketEvents.DECODER_NAME);
        channel.pipeline().remove(PacketEvents.ENCODER_NAME);
    }

    public static void reloadChannel(Channel channel) {
        PacketEventsDecoder decoder = (PacketEventsDecoder) channel.pipeline().remove(PacketEvents.DECODER_NAME);
        PacketEventsEncoder encoder = (PacketEventsEncoder) channel.pipeline().remove(PacketEvents.ENCODER_NAME);
        addChannelHandlers(channel, decoder, encoder);
    }
}
