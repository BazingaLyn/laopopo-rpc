package org.laopopo.common.serialization;

import org.laopopo.common.spi.BaseServiceLoader;


/**
 * 
 * @author BazingaLyn
 * @description 序列化的入口,基于SPI方式
 * @time 2016年8月12日
 * @modifytime
 */
public final class SerializerHolder {

    // SPI
    private static final Serializer serializer = BaseServiceLoader.load(Serializer.class);

    public static Serializer serializerImpl() {
        return serializer;
    }
}
