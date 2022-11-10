package me.michqql.itemabilities.item.data;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class BooleanDataType implements PersistentDataType<byte[], Boolean> {

    public static final BooleanDataType BOOLEAN = new BooleanDataType();

    @NotNull
    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @NotNull
    @Override
    public Class<Boolean> getComplexType() {
        return Boolean.class;
    }

    @Override
    public byte[] toPrimitive(@NotNull Boolean complex, @NotNull PersistentDataAdapterContext context) {
        return new byte[]{
                (byte) (complex ? 1 : 0)
        };
    }

    @NotNull
    @Override
    public Boolean fromPrimitive(byte[] primitive, @NotNull PersistentDataAdapterContext context) {
        byte b = primitive[0];
        return (b & 0b00000001) == 0b00000001;
    }
}
