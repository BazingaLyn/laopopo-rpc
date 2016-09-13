package org.laopopo.example.serializer;

import org.laopopo.example.netty.TestCommonCustomBody;
import org.laopopo.example.netty.TestCommonCustomBody.ComplexTestObj;
import static org.laopopo.common.serialization.SerializerHolder.serializerImpl;

/**
 * 
 * @author BazingaLyn
 * @description 
 * 
 * 1)使用protoStuff序列化测试
 * 修改org.laopopo.common.serialization.Serializer中的内容为：
 * org.laopopo.common.serialization.proto.ProtoStuffSerializer
 * 
 * 2)使用fastjson序列化测试
 * 修改org.laopopo.common.serialization.Serializer中的内容为：
 * org.laopopo.common.serialization.fastjson.FastjsonSerializer
 * 
 * 3)使用kryo序列化测试
 * 修改org.laopopo.common.serialization.Serializer中的内容为：
 * org.laopopo.common.serialization.kryo.KryoSerializer
 * 
 * @time 2016年8月12日
 * @modifytime
 */
public class SerializerTest {
	
	public static void main(String[] args) {
		
		long beginTime = System.currentTimeMillis();
		
		for(int i = 0;i < 100000;i++){
			ComplexTestObj complexTestObj = new ComplexTestObj("attr1", 2);
			TestCommonCustomBody commonCustomHeader = new TestCommonCustomBody(1, "test",complexTestObj);
			byte[] bytes = serializerImpl().writeObject(commonCustomHeader);
			
			TestCommonCustomBody body = serializerImpl().readObject(bytes, TestCommonCustomBody.class);
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println((endTime - beginTime));
		
	}

}
