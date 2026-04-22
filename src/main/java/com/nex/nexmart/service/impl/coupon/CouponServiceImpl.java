package com.nex.nexmart.service.impl.coupon;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.PointsMallItemMapper;
import com.nex.nexmart.mapper.base.CategoryMapper;
import com.nex.nexmart.mapper.CouponUserMapper;
import com.nex.nexmart.mapper.base.ProductMapper;
import com.nex.nexmart.model.dto.coupon.CouponCreateDTO;
import com.nex.nexmart.model.dto.coupon.CouponPageQueryDTO;
import com.nex.nexmart.model.dto.coupon.CouponUpdateDTO;
import com.nex.nexmart.model.entity.Category;
import com.nex.nexmart.model.entity.checkinPoint.PointsMallItem;
import com.nex.nexmart.model.entity.coupon.Coupon;
import com.nex.nexmart.model.entity.coupon.CouponUser;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.vo.coupon.CouponListVO;
import com.nex.nexmart.model.vo.coupon.CouponStatsVO;
import com.nex.nexmart.model.vo.coupon.CouponVO;
import com.nex.nexmart.service.intf.coupon.CouponService;
import com.nex.nexmart.mapper.base.CouponMapper;
import com.nex.nexmart.util.CouponDescHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Eric
* 针对表【coupon(优惠券模板表)】的数据库操作Service实现
* 2026-04-08 21:08:53
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {


	private final CouponUserMapper couponUserMapper;
	private final CategoryMapper categoryMapper;
	private final ProductMapper productMapper;
	private final CouponDescHelper couponDescHelper;
	private final PointsMallItemMapper pointsMallItemMapper;


	@Override
	public void createCoupon(CouponCreateDTO dto) {
		validateCouponDTO(dto.getCouponType(), dto.getDiscountType(),
				dto.getMinAmount(), dto.getDiscountAmount(), dto.getDiscountRate(),
				dto.getScope(), dto.getScopeId());

		Coupon coupon = new Coupon();
		BeanUtils.copyProperties(dto, coupon);

		if (dto.getTotal() ==-1 && dto.getCouponType() ==2) {
			throw new BusinessException("订单券不能设置为不限量");
		}
		coupon.setRemained(dto.getTotal() == -1 ? Integer.MAX_VALUE : dto.getTotal());
		coupon.setStatus(1);
		if(dto.getCouponType()==1){
			coupon.setReceiveChannel(1);
		}
		save(coupon);
	}

	@Override
	public PageResult<CouponVO> pageCoupon(CouponPageQueryDTO dto) {

		Page<Coupon> page = lambdaQuery()
				.like(StringUtils.hasText(dto.getName()), Coupon::getName, dto.getName())
				.eq(dto.getCouponType() != null, Coupon::getCouponType, dto.getCouponType())
				.eq(dto.getDiscountType() != null, Coupon::getDiscountType, dto.getDiscountType())
				.eq(dto.getStatus() != null, Coupon::getStatus, dto.getStatus())
				.eq(dto.getReceiveChannel() != null, Coupon::getReceiveChannel, dto.getReceiveChannel())
				.isNull(Boolean.TRUE.equals(dto.getNoReceiveChannel()), Coupon::getReceiveChannel)//一定要用isNull 而不是 eq
				.orderByDesc(Coupon::getCreatedAt)
				.page(new Page<>(dto.getCurrent(), dto.getSize()));

		List<CouponVO> voList = page.getRecords().stream()
				.map(this::toVO)
				.collect(Collectors.toList());

		return PageResult.of(voList, page.getTotal(), page.getCurrent(), page.getSize());
	}


	@Override
	@Transactional
	public void updateCoupon(CouponUpdateDTO dto) {
		Coupon coupon = getById(dto.getId());
		if (coupon == null) throw new RuntimeException("优惠券不存在");

		boolean hasReceived = couponUserMapper.selectCount(
				new LambdaQueryWrapper<CouponUser>()
						.eq(CouponUser::getCouponId, dto.getId())) > 0;

		if (hasReceived) {
			// 已有人领取，只允许改name、receiveEnd、status
			if (dto.getName() != null) coupon.setName(dto.getName());
			if (dto.getReceiveEnd() != null) coupon.setReceiveEnd(dto.getReceiveEnd());
			if (dto.getStatus() != null) coupon.setStatus(dto.getStatus());
		} else {
			// 无人领取，全量更新
			validateCouponDTO(dto.getCouponType(), dto.getDiscountType(),
					dto.getMinAmount(), dto.getDiscountAmount(), dto.getDiscountRate(),
					dto.getScope(), dto.getScopeId());
			BeanUtils.copyProperties(dto, coupon);
			if (dto.getTotal() != null && dto.getTotal() != -1) {
				coupon.setRemained(dto.getTotal());
			}
		}
		updateById(coupon);
	}

	@Override
	public void updateStatus(Long id, Integer status) {
		Coupon coupon = getById(id);
		if (coupon == null) throw new RuntimeException("优惠券不存在");
		coupon.setStatus(status);
		updateById(coupon);
	}

	@Override
	public void deleteCoupon(Long id) {
		boolean hasReceived = couponUserMapper.selectCount(
				new LambdaQueryWrapper<CouponUser>()
						.eq(CouponUser::getCouponId, id)) > 0;
		if (hasReceived) throw new BusinessException("已有用户领取，不可删除");

		boolean inPointsMall = pointsMallItemMapper.selectCount(
				new LambdaQueryWrapper<PointsMallItem>()
						.eq(PointsMallItem::getCouponId, id)) > 0;
		if (inPointsMall) throw new BusinessException("该券已被用于积分商城中，请先在积分商城管理中删除");

		removeById(id);
	}

	@Override
	public CouponStatsVO getCouponStats(Long id) {
		Coupon coupon = getById(id);
		if (coupon == null) throw new RuntimeException("优惠券不存在");

		List<CouponUser> list = couponUserMapper.selectList(
				new LambdaQueryWrapper<CouponUser>()
						.eq(CouponUser::getCouponId, id));

		long usedCount = list.stream().filter(c -> c.getStatus() == 1).count();
		long expiredCount = list.stream().filter(c -> c.getStatus() == 2).count();
		long unusedCount = list.stream().filter(c ->
				c.getStatus() == 0 && c.getExpireAt() != null && c.getExpireAt().isAfter(LocalDateTime.now())).count();

		CouponStatsVO vo = new CouponStatsVO();
		vo.setCouponId(id);
		vo.setCouponName(coupon.getName());
		vo.setTotal(coupon.getTotal());
		vo.setRemained(coupon.getRemained());
		vo.setReceivedCount(list.size());
		vo.setUsedCount((int) usedCount);
		vo.setExpiredCount((int) expiredCount);
		vo.setUnusedCount((int) unusedCount);
		return vo;
	}

	@Override
	public List<CouponListVO> getAvailableCoupons(Long userId, Integer discountType) {
		LocalDateTime now = LocalDateTime.now();

		// 查询在领取时间内且上架的券
		List<Coupon> coupons = lambdaQuery()
				.eq(Coupon::getStatus, 1)// 上架
				.eq(Coupon::getCouponType,1)//普通商品券
				.le(Coupon::getReceiveStart, now)
				.ge(Coupon::getReceiveEnd, now)
				.eq(discountType != null, Coupon::getDiscountType, discountType)
				.list();

		//获取分类名称map以及商品名称map
		CouponDescHelper.ScopeNameMaps scopeNameMaps = couponDescHelper.buildScopeNameMaps(coupons);

		// 统计当前用户已领取的每张优惠券各领了几次
		Map<Long, Long> receivedCountMap = new HashMap<>();
		if (userId != null) {
			List<CouponUser> myList = couponUserMapper.selectList(
					new LambdaQueryWrapper<CouponUser>().eq(CouponUser::getUserId, userId));
			receivedCountMap = myList.stream()
					.collect(Collectors.groupingBy(CouponUser::getCouponId, Collectors.counting()));
		}

		Map<Long, Long> finalReceivedCountMap = receivedCountMap;
		return coupons.stream().map(coupon -> {
					CouponListVO vo = new CouponListVO();
					BeanUtils.copyProperties(coupon, vo);
					// 填充VO
					couponDescHelper.fillCommonDesc(coupon,
							vo::setCouponTypeDesc,
							vo::setDiscountTypeDesc,
							vo::setScopeDesc,
							vo::setScopeName,
							scopeNameMaps.getCategoryNameMap(),
							scopeNameMaps.getProductNameMap());

					// 判断库存
					boolean remainedOk = coupon.getTotal() == -1 || coupon.getRemained() > 0;
					//判断是否超出限领数
					long myCount = finalReceivedCountMap.getOrDefault(coupon.getId(), 0L);
					boolean limitOk = myCount < coupon.getPerLimit();
					vo.setReceivable(remainedOk && limitOk);

					return vo;
				}).sorted(Comparator.comparing(CouponListVO::getReceivable).reversed())
				.collect(Collectors.toList());
	}

	// ---- 私有方法 ----

	private void validateCouponDTO(Integer couponType, Integer discountType,
	                               BigDecimal minAmount, BigDecimal discountAmount,
	                               BigDecimal discountRate, Integer scope, Long scopeId) {
		if (discountType == 1) { // 满减
			if (minAmount == null || discountAmount == null)
				throw new RuntimeException("满减券必须填写门槛金额和减免金额");
		} else if (discountType == 2) { // 折扣
			if (discountRate == null)
				throw new RuntimeException("折扣券必须填写折扣率");
		} else if (discountType == 3) { // 无门槛
			if (discountAmount == null)
				throw new RuntimeException("无门槛券必须填写减免金额");
		}

		if (couponType == 1) { // 商品券
			if (scope == null) throw new RuntimeException("商品券必须选择适用范围");
			if (scope != 1 && scopeId == null) throw new RuntimeException("单分类/单商品券必须选择具体范围");
			if (discountType == 1 && scope == 3) throw new RuntimeException("满减券不支持单商品范围");
		}
		if (couponType == 2) {
			if (discountType == 1) throw new RuntimeException("秒杀券不支持满减方式");
			if (scope == 2) throw new RuntimeException("秒杀券不支持单分类范围");
			if (scope == 3) throw new RuntimeException("秒杀券不支持单商品范围");
		}
	}

	private CouponVO toVO(Coupon coupon) {
		CouponVO vo = new CouponVO();
		BeanUtils.copyProperties(coupon, vo);

		vo.setCouponTypeDesc(switch (coupon.getCouponType()) {
			case 1 -> "商品券";
			case 2 -> "秒杀订单券";
			default -> "未知";
		});

		vo.setDiscountTypeDesc(switch (coupon.getDiscountType()) {
			case 1 -> "满减";
			case 2 -> "折扣";
			case 3 -> "无门槛";
			default -> "未知";
		});

		if (coupon.getCouponType() == 1) { // 商品券才有范围
			vo.setScopeDesc(switch (coupon.getScope()) {
				case 1 -> "全场";
				case 2 -> "单分类";
				case 3 -> "单商品";
				default -> "未知";
			});
			if (coupon.getScope() == 2 && coupon.getScopeId() != null) {
				Category category = categoryMapper.selectById(coupon.getScopeId());
				vo.setScopeName(category != null ? category.getName() : null);
			} else if (coupon.getScope() == 3 && coupon.getScopeId() != null) {
				Product product = productMapper.selectById(coupon.getScopeId());
				vo.setScopeName(product != null ? product.getName() : null);
			}
		} else { // 秒杀订单券全场
			vo.setScopeDesc("全场");
		}
		return vo;
	}


}






