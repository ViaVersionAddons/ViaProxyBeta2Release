/*
 * This file is part of ViaProxyBeta2Release - https://github.com/ViaVersionAddons/ViaProxyBeta2Release
 * Copyright (C) 2024-2025 RK_01/RaphiMC and contributors
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

import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpPacketCodec;
import com.github.steveice10.packetlib.tcp.TcpPacketEncryptor;
import com.github.steveice10.packetlib.tcp.TcpPacketSizer;
import com.github.steveice10.packetlib.tcp.TcpSession;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;

public class ViaProxyTcpSession extends TcpSession {

    private final Client client;
    private final Channel viaProxyClientChannel;
    private EmbeddedChannel embeddedChannel;

    public ViaProxyTcpSession(final Client client, final Channel viaProxyClientChannel) {
        super(client.getHost(), client.getPort(), client.getPacketProtocol());
        this.client = client;
        this.viaProxyClientChannel = viaProxyClientChannel;
    }

    @Override
    public void connect(final boolean wait) {
        this.embeddedChannel = new EmbeddedChannel();
        this.embeddedChannel.closeFuture().addListener(future -> ViaProxyTcpSession.this.viaProxyClientChannel.close());
        this.viaProxyClientChannel.closeFuture().addListener(future -> ViaProxyTcpSession.this.embeddedChannel.close());
        this.viaProxyClientChannel.pipeline().addAfter("b2r-user_session", "mcpl-pipeline", new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                ViaProxyTcpSession.this.embeddedChannel.writeOneInbound(msg, promise);
            }
        });

        this.getPacketProtocol().newClientSession(this.client, this);
        final ChannelPipeline pipeline = this.embeddedChannel.pipeline();
        this.refreshReadTimeoutHandler(this.embeddedChannel);
        this.refreshWriteTimeoutHandler(this.embeddedChannel);
        pipeline.addLast("encryption", new TcpPacketEncryptor(this));
        pipeline.addLast("sizer", new TcpPacketSizer(this));
        pipeline.addLast("codec", new TcpPacketCodec(this));
        pipeline.addLast("manager", this);
        try {
            this.channelActive(pipeline.firstContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(final Packet packet) {
        super.send(packet);
        final ByteBuf data = this.embeddedChannel.readOutbound();
        if (data != null) {
            try {
                this.viaProxyClientChannel.pipeline().context("mcpl-pipeline").fireChannelRead(data);
            } catch (Throwable e) {
                this.exceptionCaught(null, e);
            }
        }
    }

}
