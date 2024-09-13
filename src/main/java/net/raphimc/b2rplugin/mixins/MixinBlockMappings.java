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

import com.github.dirtpowered.betatorelease.data.remap.BlockMappings;
import net.raphimc.b2rplugin.Beta2ReleasePlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@Mixin(BlockMappings.class)
public abstract class MixinBlockMappings {

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Ljava/io/File;<init>(Ljava/lang/String;)V"))
    private static File redirectMappingsFile(String name) {
        Beta2ReleasePlugin.ROOT_FOLDER.mkdirs();
        return new File(Beta2ReleasePlugin.ROOT_FOLDER, name);
    }

}
