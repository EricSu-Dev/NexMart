package com.nex.nexmart.service.impl.cs;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.model.dto.CsMessageDTO;
import com.nex.nexmart.model.entity.cs.CsMessage;
import com.nex.nexmart.model.entity.cs.CsSession;
import com.nex.nexmart.model.entity.Promotion;
import com.nex.nexmart.model.entity.User;
import com.nex.nexmart.model.entity.order.Order;
import com.nex.nexmart.model.entity.order.OrderItem;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.vo.cs.CsMessageVO;
import com.nex.nexmart.model.vo.cs.CsOrderCardVO;
import com.nex.nexmart.model.vo.cs.CsProductCardVO;
import com.nex.nexmart.model.vo.cs.CsSessionVO;
import com.nex.nexmart.service.intf.cs.CsMessageService;
import com.nex.nexmart.mapper.base.CsMessageMapper;
import com.nex.nexmart.service.intf.cs.CsSessionService;
import com.nex.nexmart.service.intf.PromotionService;
import com.nex.nexmart.service.intf.UserService;
import com.nex.nexmart.service.intf.order.OrderItemService;
import com.nex.nexmart.service.intf.order.OrderService;
import com.nex.nexmart.service.intf.product.ProductService;
import com.nex.nexmart.websocket.CsWebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Eric
* 针对表【cs_message】的数据库操作Service实现
* 2026-04-03 12:47:40
*/
@Service
@RequiredArgsConstructor
public class CsMessageServiceImpl extends ServiceImpl<CsMessageMapper, CsMessage> implements CsMessageService {

	private final CsSessionService csSessionService;
	private final ProductService productService;
	private final OrderService orderService;
	private final OrderItemService orderItemService;
	private final UserService userService;
	private final CsWebSocketSessionManager sessionManager;
	private final ObjectMapper objectMapper;
	private final PromotionService promotionService;

	public void handleMessage(WebSocketSession wsSession, String payload) {
		try {
			CsMessageDTO dto = objectMapper.readValue(payload, CsMessageDTO.class);

			URI uri = wsSession.getUri();
			if (uri == null || uri.getQuery() == null) {
				wsSession.close(CloseStatus.BAD_DATA);
				return;
			}
			String query = uri.getQuery();
			if (query == null) {
				wsSession.close(CloseStatus.BAD_DATA);
				return;
			}

			String userIdStr = extractParam(query, "userId");
			if (userIdStr == null) {
				wsSession.close(CloseStatus.BAD_DATA);
				return;
			}
			Long senderId = Long.parseLong(userIdStr);
			String role = extractParam(query, "role");
			boolean isAdmin = UserRoleConstants.STRING_ROLE_ADMIN.equals(role) || UserRoleConstants.STRING_ROLE_BOSS.equals(role);

			// 心跳标记已读，标记后直接返回
			if ("READ_ACK".equals(dto.getAction())) {
				markRead(dto.getSessionId(), isAdmin ? 2 : 1);
				sendReadAckToFrontEnd(dto.getSessionId(), isAdmin);
				return;
			}

			CsMessage message = new CsMessage();
			message.setSessionId(dto.getSessionId());
			message.setSenderType(isAdmin ? 2 : 1);
			message.setSenderId(senderId);
			message.setContent(dto.getContent());
			message.setIsRead(0);
			message.setType(dto.getType() != null ? dto.getType() : 1);
			message.setImages(dto.getImages());
			message.setProductId(dto.getProductId());
			message.setOrderId(dto.getOrderId());
			this.save(message);

			CsMessageVO vo = toVO(message);
			String json = objectMapper.writeValueAsString(vo);

			if (isAdmin) {
				//管理员发的消息 → 找到这个会话属于哪个用户 → 推给那个用户
				CsSession session = csSessionService.getById(dto.getSessionId());
				if (session != null) {
					sessionManager.sendToUser(session.getUserId(), json);
				}
			} else {
				sessionManager.broadcastToAdmins(json);
			}
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}

	public CsSession createSession(Long userId) {
		CsSession existing = csSessionService
				.lambdaQuery()
				.eq(CsSession::getUserId, userId)
				.eq(CsSession::getStatus, 1).one();
		if (existing != null) return existing;

		CsSession session = new CsSession();
		session.setUserId(userId);
		session.setStatus(1);
		csSessionService.save(session);
		return session;
	}

	//  拉取历史消息
	public List<CsMessageVO> getMessages(Long sessionId, int readerType) {

		// 拉消息的同时标记已读
		markRead(sessionId, readerType);
		sendReadAckToFrontEnd(sessionId, readerType == 2);

		List<CsMessage> messages = lambdaQuery()
						.eq(CsMessage::getSessionId, sessionId)
						.orderByAsc(CsMessage::getCreatedAt)
						.list();

		// 批量查商品
		List<Long> productIds = messages.stream()
				.filter(m -> m.getType() == 3 && m.getProductId() != null)
				.map(CsMessage::getProductId).distinct().collect(Collectors.toList());


		// 批量查订单
		List<Long> orderIds = messages.stream()
				.filter(m -> m.getType() == 4 && m.getOrderId() != null)
				.map(CsMessage::getOrderId).distinct().collect(Collectors.toList());

		Map<Long, CsProductCardVO> productMap = productIds.isEmpty() ? Collections.emptyMap() :
				buildProductCardMap(productIds);

		Map<Long, CsOrderCardVO> orderMap = orderIds.isEmpty() ? Collections.emptyMap() :
				buildOrderCardMap(orderIds);



		return messages.stream().map(msg -> {
			CsMessageVO vo = new CsMessageVO();
			BeanUtils.copyProperties(msg, vo);
			if (msg.getType() == 2 && StringUtils.hasText(msg.getImages())) {
				vo.setImageList(Arrays.asList(msg.getImages().split(",")));
			}
			if (msg.getType() == 3) vo.setProductCard(productMap.get(msg.getProductId()));
			if (msg.getType() == 4) vo.setOrderCard(orderMap.get(msg.getOrderId()));
			return vo;
		}).collect(Collectors.toList());
	}

	public void closeSession(Long sessionId) {
		CsSession session = csSessionService.getById(sessionId);
		if (session != null) {
			session.setStatus(2);
			csSessionService.updateById(session);
		}
	}

	@Override
	public PageResult<CsOrderCardVO> getOrderCards(long current, long size, Long userId, Integer status, String keyword) {
		//先分页
		Page<Order> page = orderService.lambdaQuery()
				.eq(userId != null, Order::getUserId, userId)
				.eq(status != null, Order::getStatus, status)
				.eq(keyword != null, Order::getOrderNo, keyword)
				.orderByDesc(Order::getCreatedAt)
				.page(new Page<>(current, size));
		//批量获取id集合和map集合
		List<Long> orderId = page.getRecords().stream().map(Order::getId).toList();
		Map<Long, CsOrderCardVO> cardMap = buildOrderCardMap(orderId);
		List<CsOrderCardVO> list = orderId.stream()
				.map(cardMap::get)
				.filter(Objects::nonNull)
				.toList();
		return PageResult.of(list, page.getTotal(), current, size);
	}

	public Integer getUnreadCount(Long sessionId) {
		return lambdaQuery()
				.eq(CsMessage::getSessionId, sessionId)
				.eq(CsMessage::getSenderType, 2)  // 管理员发的
				.eq(CsMessage::getIsRead, 0).count().intValue();
	}

	//--------------------------管理端---------------------------------
	public PageResult<CsSessionVO> getSessions(long current, long size, String keyword) {
		// 分页查会话，keyword 搜索用户名需要先查用户表
		List<Long> filteredUserIds = null;
		if (StringUtils.hasText(keyword)) {
			filteredUserIds = userService.lambdaQuery()
					.like(User::getUsername, keyword)
					.list()
					.stream().map(User::getId).toList();
			if (filteredUserIds.isEmpty()) {
				return PageResult.of(Collections.emptyList(), 0L, current, size);
			}
		}

		// 分页查会话
		List<Long> finalFilteredUserIds = filteredUserIds;
		//拿到匹配的 userId 列表后，查会话时加 in 条件，只查这些用户的会话。keyword 为空时 in 条件不生效，查全部。
		Page<CsSession> page = csSessionService.lambdaQuery()
				.in(finalFilteredUserIds != null, CsSession::getUserId, finalFilteredUserIds)
				.orderByDesc(CsSession::getId)
				.page(new Page<>(current, size));

		List<Long> userIds = page.getRecords().stream()
				.map(CsSession::getUserId).toList();

		if (userIds.isEmpty()) {
			return PageResult.of(Collections.emptyList(), 0L, current, size);
		}

		// 批量查用户信息
		Map<Long, User> userMap = userService.listByIds(userIds)
				.stream().collect(Collectors.toMap(User::getId, u -> u));

		List<Long> sessionIds = page.getRecords().stream()
				.map(CsSession::getId).toList();

		// 批量查最后一条消息
		Map<Long, CsMessage> lastMessageMap = getLastMessageMap(sessionIds);

		// 批量查未读数
		Map<Long, Long> unreadMap = getUnreadCountMap(sessionIds);

		List<CsSessionVO> list = page.getRecords().stream().map(session -> {
			CsSessionVO vo = new CsSessionVO();
			vo.setSessionId(session.getId());
			vo.setUserId(session.getUserId());

			User user = userMap.get(session.getUserId());
			if (user != null) {
				vo.setUsername(user.getUsername());
				vo.setAvatar(user.getAvatarUrl());
			}

			CsMessage lastMsg = lastMessageMap.get(session.getId());
			if (lastMsg != null) {
				vo.setLastMessage(buildLastMessagePreview(lastMsg));
				vo.setCreatedAt(lastMsg.getCreatedAt());
			}

			vo.setUnreadCount(unreadMap.getOrDefault(session.getId(), 0L).intValue());
			return vo;
		}).toList();

		// 未读数降序排序，未读数相同的按最后消息时间降序
		List<CsSessionVO> sortedList = list.stream()
				.sorted(Comparator
						.comparingInt(CsSessionVO::getUnreadCount).reversed()
						.thenComparing(
								CsSessionVO::getCreatedAt,
								Comparator.nullsLast(Comparator.reverseOrder())
						)
				)
				.toList();

		return PageResult.of(sortedList, page.getTotal(), current, size);
	}

	// -------- 私有方法 --------

	public void markRead(Long sessionId, int readerType) {
		// readerType 是"谁在看"，把对方发的消息标记已读
		// 用户在看(readerType=1) → 标记管理员发的(senderType=2)已读
		// 管理员在看(readerType=2) → 标记用户发的(senderType=1)已读
		int oppositeSenderType = readerType == 1 ? 2 : 1;

		this.update(new LambdaUpdateWrapper<CsMessage>()
				.eq(CsMessage::getSessionId, sessionId)
				.eq(CsMessage::getSenderType, oppositeSenderType)
				.eq(CsMessage::getIsRead, 0)
				.set(CsMessage::getIsRead, 1)
		);
	}

	private void sendReadAckToFrontEnd(Long sessionId, boolean isAdmin) {
		Map<String, Object> ack = new HashMap<>();
		ack.put("action", "READ_ACK");
		ack.put("sessionId", sessionId);
		String ackJson;
		try {
			ackJson = objectMapper.writeValueAsString(ack);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		if (isAdmin) {
			CsSession session = csSessionService.getById(sessionId);
			if (session != null) {
				sessionManager.sendToUser(session.getUserId(), ackJson);
			}
		} else {
			sessionManager.broadcastToAdmins(ackJson);
		}
	}

	private CsMessageVO toVO(CsMessage msg) {
		CsMessageVO vo = new CsMessageVO();
		BeanUtils.copyProperties(msg, vo);
		if (msg.getType() == 2 && StringUtils.hasText(msg.getImages())) {
			vo.setImageList(Arrays.asList(msg.getImages().split(",")));
		}
		if (msg.getType() == 3 && msg.getProductId() != null) {
			Product product = productService.getById(msg.getProductId());
			Promotion activePromotion = promotionService.getActivePromotion(product.getId(), product.getCategoryId(), product.getPrice());
			vo.setProductCard(buildProductCard(product, activePromotion));
		}
		if (msg.getType() == 4 && msg.getOrderId() != null) {
			List<Long> orderIds = new ArrayList<>();
			orderIds.add(msg.getOrderId());
			Map<Long, CsOrderCardVO> CsOrderCardVOMap = buildOrderCardMap(orderIds);
			vo.setOrderCard(CsOrderCardVOMap.get(msg.getOrderId()));
		}
		return vo;
	}

	private Map<Long, CsProductCardVO> buildProductCardMap(List<Long> productIds) {
		List<Product> products = productService.listByIds(productIds);
		List<Long> categoryIds = products.stream().map(Product::getCategoryId).toList();
		List<Promotion> promotionList = promotionService.getActivePromotionList(productIds, categoryIds);
		return products.stream().collect(Collectors.toMap(Product::getId, product->buildProductCard(product,promotionList)));
	}


	private CsProductCardVO buildProductCard(Product product, Promotion promotion) {
		CsProductCardVO card = buildProductCardBase(product);
		card.setPromotionName(promotion.getName());
		card.setDiscountedPrice(promotionService.calcDiscountedPrice(product.getPrice(), promotion));
		return card;
	}


	private CsProductCardVO buildProductCard(Product product, List<Promotion> promotionList) {
		CsProductCardVO card = buildProductCardBase(product);
		Promotion best = promotionService.findBestPromotion(product, promotionList);
		if (best != null) {
			card.setPromotionName(best.getName());
			card.setDiscountedPrice(promotionService.calcDiscountedPrice(product.getPrice(), best));
		}
		return card;
	}

	private CsProductCardVO buildProductCardBase(Product product) {
		if (product == null) return null;
		CsProductCardVO card = new CsProductCardVO();
		BeanUtils.copyProperties(product, card);
		card.setCoverImage(product.getCoverUrl());
		return card;
	}


	private Map<Long, CsOrderCardVO> buildOrderCardMap(List<Long> orderIds) {
		// 批量查订单
		Map<Long, Order> orderMap = orderService.listByIds(orderIds)
				.stream()
				.collect(Collectors.toMap(Order::getId, o -> o));

		// 批量查订单项
		Map<Long, List<OrderItem>> itemMap = orderItemService.lambdaQuery()
				.in(OrderItem::getOrderId, orderIds)
				.orderByAsc(OrderItem::getId)
				.list()
				.stream()
				.collect(Collectors.groupingBy(OrderItem::getOrderId));


		Map<Long,CsOrderCardVO> result = new HashMap<>();
		for (Long id : orderIds) {
			Order order = orderMap.get(id);
			if (order == null) continue;  // 跳过，不放入 map
			List<OrderItem> items = itemMap.getOrDefault(id, Collections.emptyList());
			Integer itemCount = items.stream().mapToInt(OrderItem::getQuantity).sum();
			//展示优惠后单价最贵的商品
			OrderItem most = items.stream()
					.max(Comparator.comparing(item -> item.getPromotionalPrice() != null ? item.getPromotionalPrice() : item.getPrice()))
					.orElse(null);

			CsOrderCardVO card = new CsOrderCardVO();
			card.setOrderId(order.getId());
			card.setOrderNo(order.getOrderNo());
			card.setStatus(order.getStatus());
			card.setTotalAmount(order.getOriginalAmount());
			card.setActualTotalAmount(order.getFinalAmount());
			card.setItemCount(itemCount);
			card.setStatusDesc(orderService.getStatusDesc(order.getStatus()));
			if (most != null) {
				card.setFirstItemName(most.getProductName());
				card.setFirstItemImage(most.getCoverUrl());
				Product mostProduct = productService.getById(most.getProductId());
				Promotion mostPromotion = promotionService.getActivePromotion(mostProduct.getId(), mostProduct.getCategoryId(), mostProduct.getPrice());
				if (mostPromotion != null) {
					card.setPromotionName(mostPromotion.getName());
				}
			}
			result.put(id, card);
		}
		return result;
	}

	private String extractParam(String query, String key) {
		for (String param : query.split("&")) {
			String[] kv = param.split("=");
			if (kv[0].equals(key)) return kv[1];
		}
		return null;
	}


	// 批量查每个会话最后一条消息
	private Map<Long, CsMessage> getLastMessageMap(List<Long> sessionIds) {
		// 每个 sessionId 取最新一条，用子查询或者 Java 过滤
		return sessionIds.stream().collect(Collectors.toMap(
				id -> id,
				id -> lambdaQuery().eq(CsMessage::getSessionId, id).orderByDesc(CsMessage::getCreatedAt).last("LIMIT 1").one()
		));
	}

	// 批量查每个会话的未读数（管理员看用户发的未读）
	private Map<Long, Long> getUnreadCountMap(List<Long> sessionIds) {
		// 查所有未读消息，按 sessionId 分组计数
		Map<Long, Long> map = new HashMap<>();
		sessionIds.forEach(id -> {
			long count = lambdaQuery()
					.eq(CsMessage::getSessionId, id)
					.eq(CsMessage::getSenderType, 1)  // 用户发的
					.eq(CsMessage::getIsRead, 0)
					.count();
			map.put(id, count);
		});
		return map;
	}

	// 最后一条消息的预览文字
	private String buildLastMessagePreview(CsMessage msg) {
		return switch (msg.getType()) {
			case 2 -> "[图片]";
			case 3 -> "[商品]";
			case 4 -> "[订单]";
			default -> msg.getContent();
		};
	}
}




