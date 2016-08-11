package org.laopopo.common.serialization;

import org.laopopo.common.spi.BaseServiceLoader;


public final class SerializerHolder {

    // SPI
    private static final Serializer serializer = BaseServiceLoader.load(Serializer.class);

    public static Serializer serializerImpl() {
        return serializer;
    }
}
