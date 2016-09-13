package org.laopopo.common.spi;

import java.util.ServiceLoader;

/**
 * 
 * @author BazingaLyn
 * @description SPI loader
 * @time 2016年8月11日
 * @modifytime
 */
public final class BaseServiceLoader {

    public static <S> S load(Class<S> serviceClass) {
        return ServiceLoader.load(serviceClass).iterator().next();
    }
}
