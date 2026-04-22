package com.nex.nexmart.service.impl.home;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.constant.BannerStatusConstant;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.model.dto.home.BannerDTO;
import com.nex.nexmart.model.entity.home.Banner;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.vo.home.BannerVO;
import com.nex.nexmart.service.intf.home.BannerService;
import com.nex.nexmart.mapper.base.BannerMapper;
import com.nex.nexmart.service.intf.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author Eric
* @description 针对表【banner】的数据库操作Service实现
* @createDate 2026-04-01 15:27:37
*/
@Service
@RequiredArgsConstructor
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService{

	private final ProductService productService;
	private final StringRedisTemplate stringRedisTemplate;
	private static final String BANNER_LIST_KEY = "NexMart:banner:list";

	@Override
	public List<BannerVO> getActiveBanners() {
		String cached = stringRedisTemplate.opsForValue().get(BANNER_LIST_KEY);
		if (cached != null) {
			return JSON.parseArray(cached, BannerVO.class);
		}
		List<Banner> banners = lambdaQuery()
				.eq(Banner::getStatus, BannerStatusConstant.ACTIVE)
				.orderByAsc(Banner::getSort)
				.list();

		// 批量查商品，解决N+1
		List<Long> productIds = banners.stream()
				.map(Banner::getProductId)
				.toList();
		Map<Long, String> productNameMap = productService.listByIds(productIds)
				.stream()
				.collect(Collectors.toMap(Product::getId, Product::getName));

		List<BannerVO> list = banners.stream().map(b -> {
			BannerVO vo = new BannerVO();
			BeanUtils.copyProperties(b, vo);
			vo.setProductName(productNameMap.get(b.getProductId()));
			return vo;
		}).toList();
		stringRedisTemplate.opsForValue().set(BANNER_LIST_KEY, JSON.toJSONString(list), 3, TimeUnit.HOURS);
		return list;
	}

	@Override
	public List<BannerVO> getAllBanners() {
		List<Banner> list = lambdaQuery()
				.orderByDesc(Banner::getStatus)
				.orderByAsc(Banner::getSort)//排序
				.list();
		// 1. 一次性收集所有 productId
		List<Long> productIds = list.stream()
				.map(Banner::getProductId)
				.toList();

		// 2. 一次性查出所有商品，转成 Map 方便取值
		Map<Long, Product> productMap = productService.listByIds(productIds)
				.stream()
				.collect(Collectors.toMap(Product::getId, p -> p));

		// 3. 组装 VO
		return list.stream().map(b -> {
			BannerVO vo = new BannerVO();
			BeanUtils.copyProperties(b, vo);
			Product product = productMap.get(b.getProductId());
			if (product != null) {
				vo.setProductName(product.getName());
			}
			return vo;
		}).toList();
	}

	@Override
	public void addBanner(BannerDTO dto) {
		Product product = productService.getById(dto.getProductId());
		if(product == null){
			throw new BusinessException("商品不存在,请检查商品ID");
		}
		Banner banner = new Banner();
		BeanUtils.copyProperties(dto, banner);
		//created_at / updated_at 交给 MyBatis-Plus 自动填充
		save(banner);
		stringRedisTemplate.delete(BANNER_LIST_KEY);
	}

	@Override
	public void updateBanner(Long id, BannerDTO dto) {
		Banner b = getById(id);
		if(b == null){
			throw new BusinessException("Banner不存在");
		}
		Product product = productService.getById(dto.getProductId());
		if(product == null){
			throw new BusinessException("商品不存在,请检查商品ID");
		}

		Banner banner = new Banner();
		BeanUtils.copyProperties(dto, banner);
		banner.setId(id);
		updateById(banner);
		stringRedisTemplate.delete(BANNER_LIST_KEY);
	}

	@Override
	public void deleteBanner(Long id) {
		Banner b = getById(id);
		if(b == null){
			throw new BusinessException("Banner不存在");
		}
		removeById(id);
		stringRedisTemplate.delete(BANNER_LIST_KEY);
	}

	@Override
	public void updateStatus(Long id, Integer status) {
		lambdaUpdate().eq(Banner::getId, id).set(Banner::getStatus, status).update();
		stringRedisTemplate.delete(BANNER_LIST_KEY);
	}

	@Override
	public BannerVO getDetails(Long id) {
		Banner banner = getById(id);
		if(banner== null){
			throw new BusinessException("Banner不存在");
		}
		BannerVO vo = new BannerVO();
		BeanUtils.copyProperties(banner, vo);
		return vo;
	}
}




