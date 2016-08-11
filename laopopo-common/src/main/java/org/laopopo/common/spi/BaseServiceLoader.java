package org.laopopo.common.spi;

import java.util.ServiceLoader;

public final class BaseServiceLoader {

    public static <S> S load(Class<S> serviceClass) {
        return ServiceLoader.load(serviceClass).iterator().next();
    }
}
