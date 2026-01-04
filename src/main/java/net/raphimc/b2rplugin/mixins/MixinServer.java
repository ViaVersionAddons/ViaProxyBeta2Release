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
package net.raphimc.b2rplugin.mixins;

import com.github.dirtpowered.betatorelease.Server;
import com.github.dirtpowered.betatorelease.network.codec.PacketDecoder;
import com.github.dirtpowered.betatorelease.network.codec.PacketEncoder;
import com.github.dirtpowered.betatorelease.network.registry.SessionRegistry;
import com.github.dirtpowered.betatorelease.proxy.translator.registry.BetaToModernRegistry;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import net.raphimc.b2rplugin.ServerPipelineHooker;
import net.raphimc.b2rplugin.session.ViaProxyBetaSession;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Server.class)
public abstract class MixinServer {

    @Shadow
    @Final
    private SessionRegistry sessionRegistry;

    @Shadow
    @Final
    private BetaToModernRegistry betaToModernRegistry;

    @Overwrite
    private void bind(String address, int port) {
        ServerPipelineHooker.prepare(channel -> {
            channel.pipeline().addFirst("b2r-decoder", new PacketDecoder());
            channel.pipeline().addAfter("b2r-decoder", "b2r-encoder", new PacketEncoder());
            channel.pipeline().addAfter("b2r-encoder", "b2r-user_session", new ViaProxyBetaSession((Server) (Object) this, channel, this.sessionRegistry, this.betaToModernRegistry));
        });
    }

    @Redirect(method = "stop", at = @At(value = "INVOKE", target = "Lio/netty/channel/EventLoopGroup;shutdownGracefully()Lio/netty/util/concurrent/Future;"))
    private Future<?> shutdown(EventLoopGroup eventLoopGroup) {
        // No op. The object is null because we overwrote the bind method
        return null;
    }

}
