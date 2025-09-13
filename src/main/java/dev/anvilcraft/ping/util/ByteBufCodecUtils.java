package dev.anvilcraft.ping.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public interface ByteBufCodecUtils {
    StreamCodec<ByteBuf, Vec3> VEC3 = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf byteBuf, Vec3 vec3) {
            byteBuf.writeDouble(vec3.x());
            byteBuf.writeDouble(vec3.y());
            byteBuf.writeDouble(vec3.z());
        }

        @Override
        public Vec3 decode(ByteBuf byteBuf) {
            return new Vec3(byteBuf.readDouble(), byteBuf.readDouble(), byteBuf.readDouble());
        }
    };

    static <T extends Enum<T>> StreamCodec<ByteBuf, T> enumCodec(Class<T> clazz) {
        return new StreamCodec<>() {
            @Override
            public void encode(ByteBuf byteBuf, T t) {
                byteBuf.writeInt(t.ordinal());
            }

            @Override
            public T decode(ByteBuf byteBuf) {
                return clazz.getEnumConstants()[byteBuf.readInt()];
            }
        };
    }
}
