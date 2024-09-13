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

import com.github.steveice10.mc.protocol.data.MagicValues;
import com.github.steveice10.mc.protocol.data.game.entity.EntityStatus;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityStatusPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerEntityStatusPacket.class)
public abstract class MixinServerEntityStatusPacket {

    @Redirect(method = "read", at = @At(value = "INVOKE", target = "Lcom/github/steveice10/mc/protocol/data/MagicValues;key(Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private <T> T key(Class<T> keyType, Object value) {
        try {
            return MagicValues.key(keyType, value);
        } catch (Throwable t) {
            return (T) EntityStatus.TOTEM_OF_UNDYING_MAKE_SOUND;
        }
    }

}
