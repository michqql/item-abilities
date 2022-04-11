package me.michqql.itemabilities.item.data;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class NumberDataType implements PersistentDataType<byte[], Number> {

    public static final NumberDataType NUMBER = new NumberDataType();

    @NotNull
    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @NotNull
    @Override
    public Class<Number> getComplexType() {
        return Number.class;
    }

    @Override
    public byte[] toPrimitive(@NotNull Number complex, @NotNull PersistentDataAdapterContext context) {
        long value = complex.longValue();

        byte[] bytes = new byte[Long.BYTES]; // length of 8 (8 bytes = 1 long)
        int length = bytes.length;
        for(int i = 0; i < length; i++) {
            bytes[length - 1 - i] = (byte) (value & 0xFF);
            value >>= 8;
        }
        return bytes;
    }

    @NotNull
    @Override
    public Number fromPrimitive(byte[] primitive, @NotNull PersistentDataAdapterContext context) {
        long value = 0;
        for(byte b : primitive) {
            value = (value << 8) + (b & 0xFF);
        }
        return value;
    }
}
