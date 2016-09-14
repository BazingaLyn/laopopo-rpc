package org.laopopo.common.utils;




/**
 * 
 * @author BazingaLyn
 * @description 一些常量
 * @time 2016年8月11日
 * @modifytime
 */
public class Constants {
	
	/**************可用的CPU数****************/
	public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	/**********读心跳的默认时间间隔****************/
	public static final int READER_IDLE_TIME_SECONDS = 60;
	/***********写心跳的默认时间间隔***********/
	public static final int WRITER_IDLE_TIME_SECONDS = 30;
	/**********默认的权重负载***************/
	public static final int DEFAULT_WEIGHT = 50;
	/******默认的consumer和provider之间默认的链接数*********/
	public static final int DEFAULT_CONNECTION_COUNT = 1;
	/*****一分钟默认的最大调用次数******/
	public static final int DEFAULT_MAX_CALLCOUN_TINMINUTE = 10000;
	
	
}
