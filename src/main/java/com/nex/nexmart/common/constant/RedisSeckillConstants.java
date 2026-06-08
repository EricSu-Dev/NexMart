package com.nex.nexmart.common.constant;

public class RedisSeckillConstants {
	// 秒杀券
	public static final String SECKILL_COUPON_STOCK = "NexMart:seckill:coupon:stock:";
	public static final String SECKILL_COUPON_USERS = "NexMart:seckill:coupon:users:";

	// 秒杀商品
	public static final String SECKILL_PRODUCT_STOCK = "NexMart:seckill:product:stock:";
	public static final String SECKILL_PRODUCT_USERS = "NexMart:seckill:product:users:";

	// 秒杀活动
	public static final String SECKILL_ACTIVITY = "NexMart:seckill:activity:list:";
	public static final String SECKILL_ACTIVITY_LOCK= "NexMart:seckill:activity:lock:";

	// 秒杀券列表
	public static final String SECKILL_COUPON_LIST = "NexMart:seckill:coupon:list:";
	public static final String SECKILL_COUPON_LIST_LOCK = "NexMart:seckill:coupon:lock:";

	// 秒杀商品列表
	public static final String SECKILL_PRODUCT_LIST = "NexMart:seckill:product:list:";
	public static final String SECKILL_PRODUCT_LIST_LOCK = "NexMart:seckill:product:lock:";

	// 秒杀检验库存以及限购的Lua脚本
	public static final String SECKILL_LUA =
			"local stock = tonumber(redis.call('GET', KEYS[1])) " +
					"if not stock or stock <= 0 then return -1 end " +
					"local bought = tonumber(redis.call('HGET', KEYS[2], ARGV[1])) or 0 " +
					"if bought >= tonumber(ARGV[2]) then return -2 end " +
					"redis.call('DECR', KEYS[1]) " +
					"redis.call('HINCRBY', KEYS[2], ARGV[1], 1) " +
					"return 1";

	/**
	 * 安全释放锁的 Lua 脚本
	 * 如果当前锁的值等于传入的 requestId，则删除锁并返回1
	 * 否则返回0（避免误删其他线程的锁）
	 */
	public static final String RELEASE_LOCK_SCRIPT =
			"if redis.call('get', KEYS[1]) == ARGV[1] " +
					"then return redis.call('del', KEYS[1]) " +
					"else return 0 end";

	//mq失败回滚脚本
	public static final String SECKILL_ROLLBACK_LUA =
			"redis.call('incr', KEYS[1])\n" +
					"redis.call('hincrby', KEYS[2], ARGV[1], -1)\n" +
					"return 1";
}
