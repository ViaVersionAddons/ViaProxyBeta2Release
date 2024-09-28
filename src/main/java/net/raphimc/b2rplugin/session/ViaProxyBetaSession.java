/*
 * This file is part of ViaProxyBeta2Release - https://github.com/ViaVersionAddons/ViaProxyBeta2Release
 * Copyright (C) 2024-2024 RK_01/RaphiMC and contributors
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
package net.raphimc.b2rplugin.session;

import com.github.dirtpowered.betaprotocollib.model.Packet;
import com.github.dirtpowered.betatorelease.Server;
import com.github.dirtpowered.betatorelease.network.registry.SessionRegistry;
import com.github.dirtpowered.betatorelease.network.session.Session;
import com.github.dirtpowered.betatorelease.proxy.translator.registry.BetaToModernRegistry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class ViaProxyBetaSession extends Session {

    private final Channel channel;

    public ViaProxyBetaSession(final Server server, final Channel channel, final SessionRegistry sessionRegistry, final BetaToModernRegistry betaToModernRegistry) {
        super(server, channel, sessionRegistry, betaToModernRegistry);
        this.channel = channel;
        super.channelActive(this.channel.pipeline().context(this));
    }

    @Override
    public void sendPacket(final Packet packet) {
        if (this.channel.isActive()) {
            this.channel.pipeline().context("b2r-user_session").writeAndFlush(packet);
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext context) {
        context.fireChannelActive();
        super.channelActive(context);
    }

    @Override
    public void channelInactive(final ChannelHandlerContext context) {
        context.fireChannelInactive();
        super.channelInactive(context);
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {
        context.fireExceptionCaught(cause);
        super.exceptionCaught(context, cause);
    }

}
