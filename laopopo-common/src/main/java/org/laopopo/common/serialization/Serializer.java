package org.laopopo.common.serialization;

/**
 * 
 * @author BazingaLyn
 * @description 序列化接口
 * @time 2016年8月12日
 * @modifytime
 */
public interface Serializer {

	/**
	 * 将对象序列化成byte[]
	 * @param obj
	 * @return
	 */
    <T> byte[] writeObject(T obj);

    /**
     * 将byte数组反序列成对象
     * @param bytes
     * @param clazz
     * @return
     */
    <T> T readObject(byte[] bytes, Class<T> clazz);
}
