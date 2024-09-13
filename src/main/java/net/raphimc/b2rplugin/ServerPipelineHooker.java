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
package net.raphimc.b2rplugin;

import io.netty.channel.Channel;

import java.util.function.Consumer;

public class ServerPipelineHooker {

    private static Consumer<Channel> handlerAdder;

    public static void prepare(final Consumer<Channel> handlerAdder) {
        ServerPipelineHooker.handlerAdder = handlerAdder;
    }

    public static void addB2R(final Channel channel) {
        if (ServerPipelineHooker.handlerAdder == null) {
            throw new IllegalStateException("ServerPipelineHooker has not been prepared yet");
        }

        ServerPipelineHooker.handlerAdder.accept(channel);
    }

}
