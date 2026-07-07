package com.nex.nexmart.service.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.nexmart.mapper.CouponUserMapper;
import com.nex.nexmart.mapper.OrderMapper;
import com.nex.nexmart.mapper.UserPointsMapper;
import com.nex.nexmart.mapper.base.ProductMapper;
import com.nex.nexmart.mapper.base.PromotionMapper;
import com.nex.nexmart.mapper.base.SeckillActivityMapper;
import com.nex.nexmart.model.entity.Promotion;
import com.nex.nexmart.model.entity.checkinPoint.UserPoints;
import com.nex.nexmart.model.entity.coupon.CouponUser;
import com.nex.nexmart.model.entity.order.Order;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.entity.seckill.SeckillActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiContextService {

	private final ProductMapper productMapper;
	private final OrderMapper orderMapper;
	private final CouponUserMapper couponUserMapper;
	private final UserPointsMapper userPointsMapper;
	private final PromotionMapper promotionMapper;
	private final SeckillActivityMapper seckillActivityMapper;
	private final ChatClient chatClient;
	private final ObjectMapper objectMapper;


	// 商品查询（按关键词，最多5条）
	public String queryProduct(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return "";
		}
		String[] keywords = keyword.split("\\s+");
		List<Product> products = productMapper.selectList(
				new LambdaQueryWrapper<Product>()
						.and(w -> {
							for (String kw : keywords) {
								w.or(inner -> inner
										.like(Product::getName, kw)
										.or()
										.like(Product::getDescription, kw));
							}
						})
						.eq(Product::getStatus, 1)
						.last("limit 5")
		);
		if (products.isEmpty()) return "商城中未找到与\"" + keyword + "\"相关的商品";
		StringBuilder sb = new StringBuilder("以下是相关商品：\n");
		for (Product p : products) {
			sb.append(String.format("- %s，售价%.2f元，库存%d件\n",
					p.getName(), p.getPrice(), p.getStock()));
		}
		return sb.toString();
	}

	// 进行中的促销活动
	public String queryPromotion() {
		LocalDateTime now = LocalDateTime.now();
		List<Promotion> list = promotionMapper.selectList(
				new LambdaQueryWrapper<Promotion>()
						.eq(Promotion::getStatus, 1)
						.le(Promotion::getStartTime, now)
						.ge(Promotion::getEndTime, now)
		);
		if (list.isEmpty()) return "当前没有进行中的促销活动。";
		StringBuilder sb = new StringBuilder("当前进行中的促销活动：\n");
		for (Promotion p : list) {
			String discount = p.getType() == 1
					? String.format("满%.0f减%.0f", p.getMinAmount(), p.getDiscountAmount())
					: String.format("%.0f折", p.getDiscountRate().multiply(BigDecimal.TEN));
			sb.append(String.format("- %s：%s，有效期至%s\n",
					p.getName(), discount,
					p.getEndTime().format(DateTimeFormatter.ofPattern("MM月dd日HH时"))));
		}
		return sb.toString();
	}

	// 进行中的秒杀活动
	public String querySeckill() {
		LocalDateTime now = LocalDateTime.now();
		List<SeckillActivity> list = seckillActivityMapper.selectList(
				new LambdaQueryWrapper<SeckillActivity>()
						.eq(SeckillActivity::getStatus, 1)
						.le(SeckillActivity::getStartTime, now)
						.ge(SeckillActivity::getEndTime, now)
		);
		if (list.isEmpty()) return "当前没有进行中的秒杀活动。";
		StringBuilder sb = new StringBuilder("当前进行中的秒杀活动:\n");
		for (SeckillActivity a : list) {
			sb.append(String.format("- %s,截止%s\n",
					a.getName(),
					a.getEndTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日HH时"))));
		}
		return sb.toString();
	}

	// 用户最近10条订单
	public String queryOrder(Long userId) {
		List<Order> orders = orderMapper.selectList(
				new LambdaQueryWrapper<Order>()
						.eq(Order::getUserId, userId)
						.orderByDesc(Order::getCreatedAt)
						.last("limit 10")
		);
		if (orders.isEmpty()) return "您暂时还没有订单。";
		StringBuilder sb = new StringBuilder("您最近的订单：\n");
		Map<Integer, String> statusMap = Map.of(
				0, "已取消", 1, "待付款", 2, "待发货", 3, "待收货", 4, "已完成"
		);
		for (Order o : orders) {
			sb.append(String.format("- 订单号%s，实付%.2f元，状态：%s\n",
					o.getOrderNo(), o.getFinalAmount(),
					statusMap.getOrDefault(o.getStatus(), "未知")));
		}
		return sb.toString();
	}

	// 用户未使用的优惠券
	public String queryCoupon(Long userId) {
		List<CouponUser> list = couponUserMapper.selectList(
				new LambdaQueryWrapper<CouponUser>()
						.eq(CouponUser::getUserId, userId)
						.eq(CouponUser::getStatus, 0)
						.ge(CouponUser::getExpireAt, LocalDateTime.now())
		);
		if (list.isEmpty()) return "您当前没有可用的优惠券。";
		return String.format("您当前有%d张可用优惠券，请前往\"个人中心\"的\"我的券包\"查看详情", list.size());
	}

	// 用户积分
	public String queryPoints(Long userId) {
		UserPoints up = userPointsMapper.selectOne(
				new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId)
		);
		if (up == null) return "您当前积分为0,可前往首页的\"签到领积分\"去领取积分";
		return String.format("您当前积分余额为%d分，可前往积分商城兑换优惠券。", up.getTotalPoints());
	}

	public String judgeSearchType(String keyword) {
		String prompt = String.format("""
            用户搜索："%s"
            判断这是在搜索一个商品分类，还是一个具体商品。
            只返回JSON，不要有任何多余文字：
            {"type":"category"} 或 {"type":"product"}
            
            例如：
            输入"水果" → {"type":"category"}
            输入"零食" → {"type":"category"}
            输入"华为手机" → {"type":"product"}
            输入"乐事薯片" → {"type":"product"}
            """, keyword);
		try {
			String result = chatClient.prompt()
					.user(prompt)
					.call()
					.content();
			if (result == null || result.trim().isEmpty()) {
				throw new RuntimeException("AI 返回内容为空，无法解析 IntentResult");
			}
			result = result.replaceAll("```json|```", "").trim();
			Map<String, String> map = objectMapper.readValue(result, new TypeReference<>() {});
			return map.getOrDefault("type", "product");
		} catch (Exception e) {
			// 解析失败默认走单品搜索
			return "product";
		}
	}

	public List<String> expandKeywords(String keyword) {
		String prompt = String.format("""
        用户搜索："%s"
        请联想与该搜索词相关的商品名称、品牌名、英文名、同义词、以及该品类下常见的具体商品名称。
        只返回JSON数组，不要有任何多余文字，最多返回8个词。
        
        例如：
        输入"华为手机" → ["华为","HUAWEI","手机","Mate","Nova","P60"]
        输入"水果" → ["苹果","香蕉","橙子","Apple","葡萄","草莓","芒果","榴莲"]
        输入"零食" → ["薯片","乐事","可比克","饼干","糖果","坚果","膨化食品","Lay's"]
        """, keyword);
		try {
			String result = chatClient.prompt()
					.user(prompt)
					.call()
					.content();
			// 安全处理 null 和清理 markdown
			if (result == null || result.trim().isEmpty()) {
				throw new RuntimeException("AI 返回内容为空，无法解析 IntentResult");
			}
			result = result.replaceAll("```json|```", "").trim();
			return objectMapper.readValue(result, new TypeReference<>() {});
		} catch (Exception e) {
			return List.of(keyword);
		}
	}

	public List<Product> searchByExpandedKeywords(List<String> keywords) {
		return productMapper.selectList(
				new LambdaQueryWrapper<Product>()
						.and(w -> {
							for (String kw : keywords) {
								w.or(inner -> inner
										.like(Product::getName, kw)
										.or()
										.like(Product::getDescription, kw));
							}
						})
						.eq(Product::getStatus, 1)
						.last("limit 8")
		);
	}
}
