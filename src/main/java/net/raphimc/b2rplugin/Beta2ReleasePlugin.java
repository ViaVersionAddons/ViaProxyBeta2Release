/*
 * This file is part of ViaProxyBeta2Release - https://github.com/ViaVersionAddons/ViaProxyBeta2Release
 * Copyright (C) 2024-2026 RK_01/RaphiMC and contributors
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
package net.raphimc.b2rplugin;

import com.github.dirtpowered.betatorelease.network.session.BetaPlayer;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.lenni0451.classtransform.mixinstranslator.MixinsTranslator;
import net.lenni0451.classtransform.utils.loader.InjectionClassLoader;
import net.lenni0451.lambdaevents.EventHandler;
import net.lenni0451.reflect.stream.RStream;
import net.raphimc.viaproxy.ViaProxy;
import net.raphimc.viaproxy.plugins.PluginManager;
import net.raphimc.viaproxy.plugins.ViaProxyPlugin;
import net.raphimc.viaproxy.plugins.events.Client2ProxyChannelInitializeEvent;
import net.raphimc.viaproxy.plugins.events.ShouldVerifyOnlineModeEvent;
import net.raphimc.viaproxy.plugins.events.types.ITyped;
import net.raphimc.viaproxy.util.logging.Logger;

import java.io.File;
import java.util.List;

public class Beta2ReleasePlugin extends ViaProxyPlugin {

    public static final File ROOT_FOLDER = new File(PluginManager.PLUGINS_DIR, "Beta2Release");
    private List<BetaPlayer> b2rPlayers;
    private boolean b2rOnlineMode;

    @Override
    public void onEnable() {
        Beta2ReleasePlugin.ROOT_FOLDER.mkdirs();

        final InjectionClassLoader injectionClassLoader = (InjectionClassLoader) this.getClassLoader();
        injectionClassLoader.getTransformerManager().addTransformerPreprocessor(new MixinsTranslator());
        injectionClassLoader.getTransformerManager().addTransformer("net.raphimc.b2rplugin.mixins.MixinBlockMappings");
        injectionClassLoader.getTransformerManager().addTransformer("net.raphimc.b2rplugin.mixins.MixinConfiguration");
        injectionClassLoader.getTransformerManager().addTransformer("net.raphimc.b2rplugin.mixins.MixinLangStorage");
        injectionClassLoader.getTransformerManager().addTransformer("net.raphimc.b2rplugin.mixins.MixinModernClient");
        injectionClassLoader.getTransformerManager().addTransformer("net.raphimc.b2rplugin.mixins.MixinServer");

        try {
            final Class<?> mainClass = injectionClassLoader.loadClass("com.github.dirtpowered.betatorelease.Main");
            RStream.of(mainClass).methods().by("main").invoke();
            this.b2rPlayers = RStream.of(mainClass).fields().by("server").stream().fields().by("onlinePlayers").get();
            this.b2rOnlineMode = RStream.of(mainClass).fields().by("server").stream().fields().by("configuration").stream().fields().by("onlineMode").get();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to start Beta2Release", e);
        }

        ViaProxy.EVENT_MANAGER.register(this);
    }

    @EventHandler
    private void onClient2ProxyChannelInit(final Client2ProxyChannelInitializeEvent event) {
        if (event.getType() != ITyped.Type.PRE) return;

        event.getChannel().pipeline().addFirst("b2r-initial-handler", new SimpleChannelInboundHandler<ByteBuf>() {
            @Override
            protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf msg) {
                if (!ctx.channel().isOpen()) return;
                if (!msg.isReadable(2)) return;

                final int lengthOrPacketId = msg.getUnsignedByte(0);
                ctx.pipeline().remove(this);

                if ((lengthOrPacketId == 2 && msg.getByte(1) == 0)/*<= 1.3*/) {
                    Logger.LOGGER.info("Detected pre 1.7 client connection. Adding Beta2Release handlers.");
                    ServerPipelineHooker.addB2R(ctx.channel());
                    ctx.pipeline().fireChannelRead(msg.retain());
                } else {
                    ctx.fireChannelRead(msg.retain());
                }
            }
        });
    }

    @EventHandler
    private void onShouldVerifyOnlineModeEvent(final ShouldVerifyOnlineModeEvent event) {
        if (!this.b2rOnlineMode) return;

        final String username = event.getProxyConnection().getGameProfile().getName();
        if (username == null) return;

        final BetaPlayer player = this.b2rPlayers.stream().filter(p -> p.getSession().getPlayerName().equals(username)).findFirst().orElse(null);
        if (player != null && player.getUuid() != null) {
            event.getProxyConnection().setGameProfile(new GameProfile(player.getUuid(), username));
            event.setCancelled(true);
        }
    }

}
