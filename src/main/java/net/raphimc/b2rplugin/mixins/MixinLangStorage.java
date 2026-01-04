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

import com.github.dirtpowered.betatorelease.data.lang.LangStorage;
import net.raphimc.b2rplugin.Beta2ReleasePlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mixin(LangStorage.class)
public abstract class MixinLangStorage {

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Ljava/nio/file/Paths;get(Ljava/lang/String;[Ljava/lang/String;)Ljava/nio/file/Path;"))
    private static Path redirectLangFile(String first, String... more) {
        if (first.equals("lang")) {
            final String other = String.join(File.separator, more);
            return Beta2ReleasePlugin.ROOT_FOLDER.toPath().resolve(first).resolve(other);
        } else {
            return Paths.get(first, more);
        }
    }

}
