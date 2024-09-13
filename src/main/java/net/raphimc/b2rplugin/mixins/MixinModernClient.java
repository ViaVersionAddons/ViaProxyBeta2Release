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
package net.raphimc.b2rplugin.mixins;

import com.github.dirtpowered.betatorelease.network.session.Session;
import com.github.dirtpowered.betatorelease.proxy.ModernClient;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.SessionFactory;
import com.github.steveice10.packetlib.packet.PacketProtocol;
import net.lenni0451.reflect.stream.RStream;
import net.raphimc.b2rplugin.session.ViaProxyModernSessionFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModernClient.class)
public abstract class MixinModernClient {

    @Shadow
    @Final
    private Session betaSession;

    @Redirect(method = "connect", at = @At(value = "NEW", target = "(Ljava/lang/String;ILcom/github/steveice10/packetlib/packet/PacketProtocol;Lcom/github/steveice10/packetlib/SessionFactory;)Lcom/github/steveice10/packetlib/Client;"))
    private Client redirectClientInit(String host, int port, PacketProtocol protocol, SessionFactory factory) {
        return new Client(host, port, protocol, new ViaProxyModernSessionFactory(RStream.of(this.betaSession).fields().by("channel").get()));
    }

}
