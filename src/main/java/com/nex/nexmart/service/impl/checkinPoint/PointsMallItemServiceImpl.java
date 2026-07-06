package com.nex.nexmart.service.impl.checkinPoint;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.constant.PointsChangeTypeConstant;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.UserPointsMapper;
import com.nex.nexmart.mapper.CouponUserMapper;
import com.nex.nexmart.model.dto.PointsMallItemDTO;
import com.nex.nexmart.model.entity.checkinPoint.PointsMallItem;
import com.nex.nexmart.model.entity.checkinPoint.UserPoints;
import com.nex.nexmart.model.entity.coupon.Coupon;
import com.nex.nexmart.model.entity.coupon.CouponUser;
import com.nex.nexmart.model.vo.ExchangeResultVO;
import com.nex.nexmart.model.vo.checkinPoint.PointsMallItemVO;
import com.nex.nexmart.model.vo.checkinPoint.PointsMallVO;
import com.nex.nexmart.service.intf.PointsMallItemService;
import com.nex.nexmart.mapper.PointsMallItemMapper;
import com.nex.nexmart.service.intf.checkinPoint.UserPointsService;
import com.nex.nexmart.service.intf.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Eric
*  针对表【points_mall_item(积分商城兑换项表)】的数据库操作Service实现
*  2026-04-12 13:44:58
*/
@Service
@RequiredArgsConstructor
public class PointsMallItemServiceImpl extends ServiceImpl<PointsMallItemMapper, PointsMallItem>
		implements PointsMallItemService {

	private final CouponService couponService;
	private final CouponUserMapper couponUserMapper;
	private final UserPointsService userPointsService;
	private final UserPointsMapper userPointsMapper;

	@Override
	@Transactional
	public void createItem(PointsMallItemDTO dto) {
		// 校验券存在且是订单券
		Coupon coupon = couponService.getById(dto.getCouponId());
		if (coupon == null) {
			throw new BusinessException("券不存在");
		}
		if (coupon.getCouponType() != 2) {
			throw new BusinessException("积分商城只支持订单券");
		}
		if(coupon.getReceiveChannel()!=null) {
			throw new BusinessException("当前订单券已存在领取渠道");
		}
		PointsMallItem item = new PointsMallItem();
		item.setCouponId(dto.getCouponId());
		item.setPointsCost(dto.getPointsCost());
		item.setStatus(1);
		save(item);
		couponService.lambdaUpdate()
				.eq(Coupon::getId, dto.getCouponId())
				.set(Coupon::getReceiveChannel,2)//领取渠道为积分商城
				.update();
	}

	@Override
	public void updateStatus(Long id, Integer status) {
		PointsMallItem item = getById(id);
		if (item == null) {
			throw new BusinessException("兑换项不存在");
		}
		lambdaUpdate()
				.eq(PointsMallItem::getId, id)
				.set(PointsMallItem::getStatus, status)
				.update();
	}

	@Override
	public List<PointsMallItemVO> listItems(String keyword, Integer discountType, Integer status) {
		return baseMapper.selectItemsWithCoupon(keyword, discountType, status);
	}

	@Override
	public void updatePointsCost(Long id, Integer pointsCost) {
		PointsMallItem item = getById(id);
		if (item == null) {
			throw new BusinessException("兑换项不存在");
		}
		if (item.getStatus() == 1) {
			throw new BusinessException("请先下架后再修改");
		}
		lambdaUpdate()
				.eq(PointsMallItem::getId, id)
				.set(PointsMallItem::getPointsCost, pointsCost)
				.update();
	}

	@Override
	@Transactional
	public void deleteItem(Long id) {
		PointsMallItem item = getById(id);
		if (item == null) {
			throw new BusinessException("兑换项不存在");
		}
		if (item.getStatus() == 1) {
			throw new BusinessException("请先下架后再删除");
		}
		removeById(id);
		//清除领取渠道
		couponService.lambdaUpdate()
				.eq(Coupon::getId, item.getCouponId())
				.set(Coupon::getReceiveChannel,null)
				.update();
	}

	@Override
	public PointsMallVO getUserMall(Long userId) {
		// 查上架兑换项
		List<PointsMallItemVO> items = baseMapper.selectItemsWithCoupon(null, null, 1);

		// 过滤已过领取截止时间的券（receiveEnd 为 null 视为不限时，不过滤）
		LocalDateTime now = LocalDateTime.now();
		items = items.stream()
				.filter(item -> item.getReceiveEnd() == null || item.getReceiveEnd().isAfter(now))
				.collect(Collectors.toList());

		// 查用户积分
		UserPoints userPoints = userPointsService.lambdaQuery().eq(UserPoints::getUserId, userId).one();
		int totalPoints = userPoints == null ? 0 : userPoints.getTotalPoints();

		PointsMallVO vo = new PointsMallVO();
		vo.setTotalPoints(totalPoints);
		vo.setItems(items);
		return vo;
	}

	@Override
	@Transactional
	public ExchangeResultVO exchange(Long userId, Long itemId) {
		// 1. 校验兑换项
		PointsMallItem item = getById(itemId);
		if (item == null || item.getStatus() != 1) {
			throw new BusinessException("兑换项不存在或已下架");
		}

		// 2. 校验券
		Coupon coupon = couponService.getById(item.getCouponId());
		if (coupon == null || coupon.getStatus() != 1) {
			throw new BusinessException("券不存在或已下架");
		}

		// 3. 校验领取时间窗口
		LocalDateTime now = LocalDateTime.now();
		if (now.isBefore(coupon.getReceiveStart()) || now.isAfter(coupon.getReceiveEnd())) {
			throw new BusinessException("该券不在领取时间范围内");
		}

		// 4. 校验券库存
		if (coupon.getTotal() != -1 && coupon.getRemained() <= 0) {
			throw new BusinessException("券已被领完");
		}

		// 5. 校验每人限领
		couponService.lambdaUpdate()
				.eq(Coupon::getId, coupon.getId())
				.setSql("updated_at = updated_at")
				.update();

		long received = couponUserMapper.selectCount(
				new LambdaQueryWrapper<CouponUser>()
						.eq(CouponUser::getUserId, userId)
						.eq(CouponUser::getCouponId, item.getCouponId())
		);
		if (received >= coupon.getPerLimit()) {
			throw new BusinessException("已达到该券领取上限");
		}

		// 6. 扣减积分（原子操作，UPDATE WHERE 兜底）
		int updated = userPointsMapper.deductPoints(userId, item.getPointsCost());
		if (updated == 0) {
			throw new BusinessException("积分不足");
		}

		// 7. 查最新余额
		UserPoints userPoints = userPointsService.lambdaQuery().eq(UserPoints::getUserId, userId).one();
		int remainPoints = userPoints.getTotalPoints();

		// 8. 写积分流水
		userPointsService.writeLog(userId, PointsChangeTypeConstant.EXCHANGE,
				-item.getPointsCost(), remainPoints,
				"兑换券：" + coupon.getName(), itemId);

		// 9. 扣减券库存
		// 10. 发券
		LocalDateTime expireAt = now.plusDays(coupon.getValidDays());
		if (coupon.getTotal() != -1) {
			boolean stockUpdated = couponService.lambdaUpdate()
					.eq(Coupon::getId, coupon.getId())
					.gt(Coupon::getRemained, 0)
					.setSql("remained = remained - 1")
					.update();
			if (!stockUpdated) {
				throw new BusinessException("Coupon stock is not enough");
			}
		}

		CouponUser couponUser = new CouponUser();
		couponUser.setUserId(userId);
		couponUser.setCouponId(coupon.getId());
		couponUser.setCouponType(2);
		couponUser.setStatus(0);
		couponUser.setReceivedAt(now);
		couponUser.setExpireAt(expireAt);
		couponUser.setCreatedAt(now);
		couponUser.setUpdatedAt(now);
		couponUserMapper.insert(couponUser);

		// 11. 组装返回
		ExchangeResultVO vo = new ExchangeResultVO();
		vo.setPointsUsed(item.getPointsCost());
		vo.setRemainPoints(remainPoints);
		vo.setCouponName(coupon.getName());
		vo.setExpireAt(expireAt);
		return vo;
	}

}




