package org.laopopo.example.benchmark.big_request_compress;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.laopopo.client.consumer.Consumer.SubscribeManager;
import org.laopopo.client.consumer.ConsumerClient;
import org.laopopo.client.consumer.ConsumerConfig;
import org.laopopo.client.consumer.proxy.ProxyFactory;
import org.laopopo.example.generic.test_2.HelloService;
import org.laopopo.remoting.netty.NettyClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author BazingaLyn
 * @description 测试大的普通字符串(这边的字符串使用普通的字母，不使用CommonBenchmarkClient中生成char的方式，这样做的好处就是更加贴近生产环境)，压缩前后的性能提高
 * @time 2016年10月18日
 * @modifytime
 * 
 * 2016-10-18 19:53:06.387 WARN  [main] [StringBenchmarkClient] - count=1280000
 * 2016-10-18 19:53:06.387 WARN  [main] [StringBenchmarkClient] - Request count: 1280000, time: 174 second, qps: 7356
 */
public class StringBenchmarkClient {
	
	private static final Logger logger = LoggerFactory.getLogger(StringBenchmarkClient.class);
	
	private static String request = "";
	
	//一行数据是1k
	static {
		request = "人来人往的机场大厅，赵默笙坐在大厅的椅子上休息，年轻女子一脸愧疚提醒赵默笙赶紧检查一下背包，赵默笙在年轻女子的提醒下打开背包拿出一台相机仔细检查，相机完好无损没有问题，赵默笙将相机放回到背包里面没有再为难年轻女子，年轻女子见识出赵默笙手中的相机非常昂贵，脸上露出一丝惊讶猜出赵默笙是一名摄影师，一般摄影师都是男性居多，年轻女子一脸惊讶对赵默笙产生了敬意，赵默笙性格内向没有跟年轻女子闲聊，年轻女子坐在赵默笙身边涛涛不绝讲述在美国生活的情景。何以琛来到一家公司跟客户开会，开会的客户是一名洋人，洋人向何以琛介绍助手许影，许影与何以琛曾是大学同学，两人当场亲密握手，洋人一脸惊讶方才意识到何以琛与许影认识。会议结束何以琛离开会议室遇到妹妹何以玫，许影不知道何以玫与何以琛是兄妹关系，还";
//		request += "以为何以琛跟何以玫是恋人关系，何以琛见许影产生误会，只得主动公布跟何以玫是兄妹关系，许影见何以琛与何以玫不是情侣，脸上露出笑容非常开心，何以玫的面色看起来却开始变得不太自然。赵默笙来到一所杂志社面试，杂志社的老总是一个中年女子，中年女子笑容满面与赵默笙谈话，赵默笙给中年女子的感觉文艺时尚，中年女子当场提醒赵默笙可以来杂志社工作。何以玫在开车过程中提议跟何以琛晚上逛街看电影，何以琛没有心情陪何以玫看电影，何以玫心中升起不悦加快车速开车险些撞到一个年轻男子，年轻男子的名字叫路远风，路远风一眼认出何以玫，何以玫在市内小有名气主持过电视节目，路远风惊喜交加向何以玫索要签名，何以玫二话不说写下签名送给路远风。路远风得到何以玫的签名回到杂志社工作，赵默笙成为了路远风的新同事，路远风向";
//		request += "上级讲述之前险遇车祸受伤的经过。赵默笙晚上逛超市购物意外遇到何以琛，何以琛与何以玫正在超市里面购物，赵默笙惊喜交加看着何以琛，何以琛却像是没有认出赵默笙一样一声不吭离去。赵默笙与同事路远风为第一个客户拍照，第一个客户的名字叫少梅是赵默笙的同学，赵默笙拍完照总觉得没有拍好，路远风劝说赵默笙没有必要过份认真工作，两人说话的时候少梅一边走出摄影室一边提醒赵默笙一起到外面喝咖啡。人来人往的机场大厅，赵默笙坐在大厅的椅子上休息，一个年轻女人来到赵默笙身边坐下，一不小心碰掉了赵默笙手中的背包，赵默笙的背包里面放着非常贵重的相机，年轻女子一脸愧疚提醒赵默笙赶紧检查一下背包，赵默笙在年轻女子的提醒下打开背包拿出一台相机仔细检查，相机完好无损没有问题，赵默笙将相机放回到背包里面没有再为难年";
//		request += "轻女子，年轻女子见识出赵默笙手中的相机非常昂贵，脸上露出一丝惊讶猜出赵默笙是一名摄影师，一般摄影师都是男性居多，年轻女子一脸惊讶对赵默笙产生了敬意，赵默笙性格内向没有跟年轻女子闲聊，年轻女子坐在赵默笙身边涛涛不绝讲述在美国生活的情景。何以琛来到一家公司跟客户开会，开会的客户是一名洋人，洋人向何以琛介绍助手许影，许影与何以琛曾是大学同学，两人当场亲密握手，洋人一脸惊讶方才意识到何以琛与许影认识。会议结束何以琛离开会议室遇到妹妹何以玫，许影不知道何以玫与何以琛是兄妹关系，还以为何以琛跟何以玫是恋人关系，何以琛见许影产生误会，只得主动公布跟何以玫是兄妹关系，许影见何以琛与何以玫不是情侣，脸上露出笑容非常开心，何以玫的面色看起来却开始变得不太自然。赵默笙来到一所杂志社面试，杂志社";
//		request	+= "的老总是一个中年女子，中年女子笑容满面与赵默笙谈话，赵默笙给中年女子的感觉文艺时尚，中年女子当场提醒赵默笙可以来杂志社工作。何以玫在开车过程中提议跟何以琛晚上逛街看电影，何以琛没有心情陪何以玫看电影，何以玫心中升起不悦加快车速开车险些撞到一个年轻男子，年轻男子的名字叫路远风，路远风一眼认出何以玫，何以玫在市内小有名气主持过电视节目，路远风惊喜交加向何以玫索要签名，何以玫二话不说写下签名送给路远风。路远风得到何以玫的签名回到杂志社工作，赵默笙成为了路远风的新同事，路远风向上级讲述之前险遇车祸受伤的经过。赵默笙晚上逛超市购物意外遇到何以琛，何以琛与何以玫正在超市里面购物，赵默笙惊喜交加看着何以琛，何以琛却像是没有认出赵默笙一样一声不吭离去。赵默笙与同事路远风为第一个客户拍照，";
//		request	+= "第一个客户的名字叫少梅是赵默笙的同学，赵默笙拍完照总觉得没有拍好，路远风劝说赵默笙没有必要过份认真工作，两人说话的时候少梅一边走出摄影室一边提醒赵默笙一起到外面喝咖啡。少梅是赵默笙的大学同学，成名之后少梅改名为箫筱，箫筱与赵默笙业务合作故意提出停止合作，当年赵默笙狠心扔下何以琛出国，箫筱依然记得当年何以琛因为赵默笙出国茶饭不思如同丢了三魂六魄。为了教训一下赵默笙，箫筱故意提出跟赵默笙的公司结束合作，路远风见箫筱无原无故取消合作，脸上升起不悦与箫筱发生争吵，箫筱一副高高在上的姿态就是不愿意再合作，路远风拿出合约书提醒箫筱无故结束合作等同违约，箫筱趁着路远风不注意夺过合约书撕得粉碎，路远风惊怒交加看着箫筱，箫筱声称事后要控告路远风的公司。路远风与赵默笙回到杂志社向张主编";
//		request += "路远风惊怒交加看着箫筱，箫筱声称事后要控告路远风的公司。路远风与赵默笙回到杂志社向张主编汇报与箫筱吵架的经过，张主编愤愤不平决定主动控告箫筱，箫筱为人蛮不讲理已经得罪过很多公司，张主编心知就算她不主动控告箫筱，箫筱也会主动控告杂志社。正如张主编猜测的一样，箫筱在经纪人凯文的陪同下来到律师事务所找何以琛求助，何以琛是箫筱的法律顾问，箫筱要求何以琛控告张主编，何以琛见箫筱又跟客户发生纠纷，脸上升起不悦盘问箫筱为何控告张主编，箫筱不愿把原因说出来，何以琛一本正经提醒箫筱跟他合约结束之后就不会再续约，箫筱见何以琛不愿意再做她的法律顾问，脸上升起不悦只得在凯文的陪同下离去。赵默笙来到何氏律师事务所想找何以琛，由于不知道律师事务所的律师就是何以琛，赵默笙走进律师事务所向前台工作人员提出";
//		request	+= "找何律师谈事情，何以琛出门办事不在律师事务所，赵默笙只得在一家餐厅等待何以琛。何以琛开车来到餐厅外面不愿意跟赵默笙见面，而是打电话让手下人来餐厅从赵默笙手中拿走资料，赵默笙送出资料离开餐厅冒雨回家，何以琛从在汽车里面目不转睛盯着赵默笙从汽车外面经过。赵默笙离去不久，何以琛回到办公室查看赵默笙转送的资料，何以玫来到办公室接何以琛一眼看到资料上写着赵默笙的名字。文总监是赵默笙的上司，赵默笙因为跟箫筱闹不和影响杂志社推出新期刊，文总监因为工作无法顺利开展训了赵默笙一顿。赵默笙为了完成任务邀请一名知名洋人男模拍摄相片，张主编对赵默笙拍摄的相片非常满意，叮嘱赵默笙不要再担心其它不相关的事情，把所有精神放在工作上便可。何以琛晚上来到超市购物，在购物过程中何以琛想起不久之前与赵默笙在超市";
//		request += "相遇的经过，当时赵默笙一脸震惊看着何以琛，何以琛神色复杂盯着赵默笙一会儿扬长离去。默笙下班回到家发现灯泡坏了，只得来到超市购买。超市保安看到她发现似曾相识，想起之前捡到皮夹里夹的照片似乎就是眼前这位姑娘，他拿来皮夹问默笙这是不是她的，默笙说不是自己的，但保安坚持让她打开皮夹看看那照片是不是她，默笙一看照片确实是自己的，但推说皮夹不是自己的坚持不能拿，保安说既然皮夹的主人藏着她的照片，必然跟她有关联，说不定自己还能促成一段好姻缘呢。默笙回家打开皮夹，费起了思量，如果说这皮夹是以琛的，但他如今似乎都不想见自己了，怎么还会放着他的照片？她翻过照片看到背后的留言“my sunshine”，笔迹确实是以琛的。默笙决定将皮夹送去袁向何律师事务所，以琛不在，默笙将皮夹交给美婷，让她转交。主编宣布一个好消";
//		request += "息，因为封面启用了大卫摩根，本期杂志的销售特别好，销量超过往期50%，并且仍在突破，大家得知这一消息都十分兴奋。事务所的两个小律师记错了开庭时间，被何以琛一顿批评。袁律师拿着皮夹来找何以琛八卦，让他承认自己在外面干了什么骗财骗色的事？为什么害得人家小姑娘一直躲在咖啡馆等他出门才敢把东西送来？以琛打开皮夹发现照片不见了，他知道皮夹是默笙送来的。他不禁回想起两人的第一次见面，那时的默笙就是一个死缠烂打的小丫头，只为了知道自己的名字，不知羞地追着自己走。小红三八地赶来告诉默笙有人找，还垂涎地说是个帅哥哦。来人正是以琛，一见面以琛公事公办地叫她“赵小姐”，默笙只得犹豫地叫他“何先生”。以琛告诉默笙自己是萧筱的律师，自己此行除了公事还想要回自己皮夹里的照片，默笙说照片里的人是自己，以琛劝";
	   System.out.println(request.getBytes().length);
	
	}
	
	public static void main(String[] args) throws Exception {
		
		int processors = Runtime.getRuntime().availableProcessors();

		NettyClientConfig registryNettyClientConfig = new NettyClientConfig();
		registryNettyClientConfig.setDefaultAddress("127.0.0.1:18010");

		NettyClientConfig provideClientConfig = new NettyClientConfig();

		ConsumerClient client = new ConsumerClient(registryNettyClientConfig, provideClientConfig, new ConsumerConfig());

		client.start();

		SubscribeManager subscribeManager = client.subscribeService("LAOPOPO.TEST.SAYHELLO");

		if (!subscribeManager.waitForAvailable(3000l)) {
			throw new Exception("no service provider");
		}

		final HelloService helloService = ProxyFactory.factory(HelloService.class).consumer(client).timeoutMillis(3000l).newProxyInstance();

		for (int i = 0; i < 500; i++) {
			String str = helloService.sayHello(request);
			System.out.println(str);
		}
		final int t = 50000;
		final int step = 6;
		long start = System.currentTimeMillis();
		final CountDownLatch latch = new CountDownLatch(processors << step);
		final AtomicLong count = new AtomicLong();
		for (int i = 0; i < (processors << step); i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < t; i++) {
						try {
							helloService.sayHello(request);

							if (count.getAndIncrement() % 10000 == 0) {
								logger.warn("count=" + count.get());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					latch.countDown();
				}
			}).start();
		}
		try {
			latch.await();
			logger.warn("count=" + count.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long second = (System.currentTimeMillis() - start) / 1000;
		logger.warn("Request count: " + count.get() + ", time: " + second + " second, qps: " + count.get() / second);
		
	}
	

}
