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
package net.raphimc.b2rplugin.mixins;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.github.dirtpowered.betatorelease.configuration.Configuration;
import com.llamalad7.mixinextras.sugar.Local;
import net.raphimc.b2rplugin.Beta2ReleasePlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Configuration.class)
public abstract class MixinConfiguration {

    private static Map<String, Object> OVERRIDE_OPTIONS;

    @ModifyConstant(method = "<init>", constant = @Constant(stringValue = "config.toml"))
    private String redirectConfig(String filePath) {
        OVERRIDE_OPTIONS = Map.of(
                "bind-address", "127.0.0.1",
                "bind-port", 25565,
                "remote-address", "127.0.0.1",
                "remote-port", 25565,
                "haproxy-support", false
        );

        return Beta2ReleasePlugin.ROOT_FOLDER.toPath().resolve("config.toml").toString();
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/electronwill/nightconfig/core/file/CommentedFileConfig;save()V", shift = At.Shift.BEFORE))
    private void removeUnsupportedOptions(CallbackInfo ci, @Local CommentedFileConfig config) {
        OVERRIDE_OPTIONS.keySet().forEach(config::remove);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/electronwill/nightconfig/core/file/CommentedFileConfig;get(Ljava/lang/String;)Ljava/lang/Object;"))
    private Object handleUnsupportedOptions(CommentedFileConfig config, String key) {
        return OVERRIDE_OPTIONS.getOrDefault(key, config.get(key));
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/electronwill/nightconfig/core/file/CommentedFileConfig;getOrElse(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object handleUnsupportedOptions(CommentedFileConfig config, String key, Object fallback) {
        return OVERRIDE_OPTIONS.getOrDefault(key, config.getOrElse(key, fallback));
    }

}
