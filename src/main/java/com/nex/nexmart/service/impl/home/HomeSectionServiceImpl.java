package com.nex.nexmart.service.impl.home;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.constant.HomeSectionStatusConstants;
import com.nex.nexmart.common.constant.HomeSectionTypeConstant;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.base.HomeSectionMapper;
import com.nex.nexmart.model.dto.home.HomeSectionConfigDTO;
import com.nex.nexmart.model.entity.Promotion;
import com.nex.nexmart.model.entity.home.HomeSection;
import com.nex.nexmart.model.entity.home.HomeSectionItem;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.entity.product.ProductSpec;
import com.nex.nexmart.model.vo.home.HomeSectionVO;
import com.nex.nexmart.model.vo.home.HomeVO;
import com.nex.nexmart.model.vo.product.ProductSpecVO;
import com.nex.nexmart.model.vo.product.ProductVO;
import com.nex.nexmart.service.intf.CategoryService;
import com.nex.nexmart.service.intf.PromotionService;
import com.nex.nexmart.service.intf.home.HomeSectionItemService;
import com.nex.nexmart.service.intf.home.HomeSectionService;
import com.nex.nexmart.service.intf.product.ProductService;
import com.nex.nexmart.service.intf.product.ProductSpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 首页商品模块配置服务实现
 */
@Service
@RequiredArgsConstructor
public class HomeSectionServiceImpl extends ServiceImpl<HomeSectionMapper, HomeSection> implements HomeSectionService {

	private final HomeSectionItemService homeSectionItemService;
	private final ProductService productService;
	private final CategoryService categoryService;
	private final ProductSpecService productSpecService;
	private final PromotionService promotionService;
	private final StringRedisTemplate stringRedisTemplate;

	private static final String HOME_MANUAL_SECTION_KEY_PREFIX = "NexMart:home:section:manual:";
	private static final String HOME_AUTO_SECTION_KEY_PREFIX = "NexMart:home:section:auto:";
	private static final long MANUAL_SECTION_CACHE_MINUTES = 10;
	private static final long AUTO_SECTION_CACHE_MINUTES = 5;

	@Override
	public List<HomeSectionVO> getAllSections() {
		List<HomeSection> sections = list();
		return sections.stream().map(s -> {
			HomeSectionVO vo = new HomeSectionVO();
			BeanUtils.copyProperties(s, vo);
			// 仅手动模式展示已配置商品
			if (s.getAutoMode() == 0) {
				vo.setItems(getItems(s.getSectionType()));
			}
			return vo;
		}).toList();
	}

	@Override
	public List<ProductVO> getItems(Integer type) {
		getSectionByTypeOrThrow(type);
		String cacheKey = buildManualSectionKey(type);
		String cached = stringRedisTemplate.opsForValue().get(cacheKey);
		if (cached != null) {
			return JSON.parseArray(cached, ProductVO.class);
		}
		List<ProductVO> items = listManualItems(type);
		stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(items), MANUAL_SECTION_CACHE_MINUTES, TimeUnit.MINUTES);
		return items;
	}

	private List<ProductVO> listManualItems(Integer type) {
		List<HomeSectionItem> sectionItems = homeSectionItemService.lambdaQuery()
				.eq(HomeSectionItem::getSectionType, type)
				.orderByAsc(HomeSectionItem::getSort)
				.list();
		if (sectionItems.isEmpty()) {
			return List.of();
		}

		List<Long> productIds = sectionItems.stream()
				.map(HomeSectionItem::getProductId)
				.toList();
		List<Product> products = productService.lambdaQuery()
				.in(Product::getId, productIds)
				.list();
		if (products.isEmpty()) {
			return List.of();
		}

		List<Long> categoryIds = products.stream().map(Product::getCategoryId).toList();
		Map<Long, String> categoryMap = categoryService.getCategoryNameMap(categoryIds);

		List<Long> hasSpecProductIds = products.stream()
				.filter(s -> s.getHasSpec() == 1)
				.map(Product::getId)
				.toList();
		Map<Long, List<ProductSpecVO>> specVOListMap = hasSpecProductIds.isEmpty()
				? Map.of()
				: productSpecService.lambdaQuery()
				.in(ProductSpec::getProductId, hasSpecProductIds)
				.orderByAsc(ProductSpec::getSort)
				.list()
				.stream()
				.collect(Collectors.groupingBy(
						ProductSpec::getProductId,
						Collectors.mapping(s -> {
							ProductSpecVO vo = new ProductSpecVO();
							BeanUtils.copyProperties(s, vo);
							return vo;
						}, Collectors.toList())
				));

		Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
		List<Promotion> activePromotions = promotionService.getActivePromotionList(productIds, categoryIds);

		List<ProductVO> productVOs = new ArrayList<>();
		for (HomeSectionItem item : sectionItems) {
			Product product = productMap.get(item.getProductId());
			if (product != null) {
				ProductVO vo = new ProductVO();
				BeanUtils.copyProperties(product, vo);
				vo.setId(product.getId());
				vo.setItemId(item.getId());
				vo.setCategoryName(categoryMap.get(product.getCategoryId()));
				if (product.getHasSpec() == 1) {
					vo.setProductSpecList(specVOListMap.get(product.getId()));
				}
				Promotion best = promotionService.findBestPromotion(product, activePromotions);
				if (best != null) {
					vo.setPromotionName(best.getName());
					vo.setDiscountedPrice(promotionService.calcDiscountedPrice(product.getPrice(), best));
				}
				productVOs.add(vo);
			}
		}
		return productVOs;
	}

	@Override
	public void updateConfig(Integer type, HomeSectionConfigDTO dto) {
		HomeSection section = getSectionByTypeOrThrow(type);
		BeanUtils.copyProperties(dto, section);
		updateById(section);
		clearSectionCache(type);
	}

	@Override
	public void addItem(Integer type, Long productId) {
		Product product = productService.getById(productId);
		if (product == null) {
			throw new BusinessException("商品不存在");
		}
		HomeSectionItem exists = homeSectionItemService.lambdaQuery()
				.eq(HomeSectionItem::getSectionType, type)
				.eq(HomeSectionItem::getProductId, productId)
				.one();
		if (exists != null) {
			throw new BusinessException("该商品已添加");
		}

		HomeSectionItem lastItem = homeSectionItemService.lambdaQuery()
				.eq(HomeSectionItem::getSectionType, type)
				.orderByDesc(HomeSectionItem::getSort)
				.last("LIMIT 1")
				.one();
		int nextSort = lastItem == null ? 1 : lastItem.getSort() + 1;

		HomeSectionItem item = new HomeSectionItem();
		item.setSectionType(type);
		item.setProductId(productId);
		item.setSort(nextSort);
		homeSectionItemService.save(item);
		clearSectionCache(type);
	}

	@Override
	public void removeItem(Integer type, Long itemId) {
		HomeSectionItem item = homeSectionItemService.getById(itemId);
		if (item == null || !item.getSectionType().equals(type)) {
			throw new BusinessException("配置项不存在");
		}
		homeSectionItemService.removeById(itemId);
		clearSectionCache(type);
	}

	@Override
	public void updateSort(Integer type, List<HomeSectionItem> items) {
		getSectionByTypeOrThrow(type);
		homeSectionItemService.updateBatchById(items);
		clearSectionCache(type);
	}

	//用户端
	@Override
	public HomeVO getHome() {
		HomeVO vo = new HomeVO();
		List<HomeSection> sections = lambdaQuery()
				.eq(HomeSection::getStatus, HomeSectionStatusConstants.ENABLED)
				.list();
		if (sections == null) {
			return null;
		}

		for (HomeSection section : sections) {
			List<ProductVO> items;
			if (section.getAutoMode() == 0) {
				items = getItems(section.getSectionType());
			} else {
				items = getAutoModeItems(section);
			}

			switch (section.getSectionType()) {
				case HomeSectionTypeConstant.HOT_SALE -> vo.setHotSales(items);
				case HomeSectionTypeConstant.NEW_ARRIVAL -> vo.setNewArrivals(items);
				case HomeSectionTypeConstant.RECOMMENDATION -> vo.setRecommendations(items);
				default -> {
				}
			}
		}
		return vo;
	}

	private List<ProductVO> getAutoModeItems(HomeSection section) {
		// 随机推荐保留每次请求随机特性，不缓存
		if (Objects.equals(section.getSectionType(), HomeSectionTypeConstant.RECOMMENDATION)) {
			return listAutoModeItems(section);
		}

		String cacheKey = buildAutoSectionKey(section.getSectionType());
		String cached = stringRedisTemplate.opsForValue().get(cacheKey);
		if (cached != null) {
			return JSON.parseArray(cached, ProductVO.class);
		}

		List<ProductVO> items = listAutoModeItems(section);
		stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(items), AUTO_SECTION_CACHE_MINUTES, TimeUnit.MINUTES);
		return items;
	}

	private List<ProductVO> listAutoModeItems(HomeSection section) {
		int autoLimit = Optional.ofNullable(section.getAutoLimit()).orElse(0);
		if (autoLimit <= 0) {
			return List.of();
		}

		List<Product> products = switch (section.getSectionType()) {
			case HomeSectionTypeConstant.HOT_SALE -> productService.lambdaQuery()
					.eq(Product::getStatus, 1)
					.orderByDesc(Product::getSales)
					.last("LIMIT " + autoLimit)
					.list();
			case HomeSectionTypeConstant.NEW_ARRIVAL -> productService.lambdaQuery()
					.eq(Product::getStatus, 1)
					.orderByDesc(Product::getUpdatedAt)
					.last("LIMIT " + autoLimit)
					.list();
			case HomeSectionTypeConstant.RECOMMENDATION -> productService.lambdaQuery()
					.eq(Product::getStatus, 1)
					.last("ORDER BY RAND() LIMIT " + autoLimit)
					.list();
			default -> productService.lambdaQuery()
					.eq(Product::getStatus, 1)
					.last("LIMIT " + autoLimit)
					.list();
		};

		return autoModeProductConvertVO(products);
	}

	private HomeSection getSectionByTypeOrThrow(Integer type) {
		HomeSection section = lambdaQuery()
				.eq(HomeSection::getSectionType, type)
				.one();
		if (section == null) {
			throw new BusinessException("模块不存在");
		}
		return section;
	}

	private void clearSectionCache(Integer type) {
		stringRedisTemplate.delete(List.of(buildManualSectionKey(type), buildAutoSectionKey(type)));
	}

	private String buildManualSectionKey(Integer type) {
		return HOME_MANUAL_SECTION_KEY_PREFIX + type;
	}

	private String buildAutoSectionKey(Integer type) {
		return HOME_AUTO_SECTION_KEY_PREFIX + type;
	}

	private List<ProductVO> autoModeProductConvertVO(List<Product> products) {
		if (products.isEmpty()) {
			return List.of();
		}

		List<Long> categoryIds = productService.extractCategoryIds(products);
		Map<Long, String> categoryMap = categoryService.getCategoryNameMap(categoryIds);

		List<Long> hasSpecProductIds = products.stream()
				.filter(s -> s.getHasSpec() == 1)
				.map(Product::getId)
				.toList();
		Map<Long, List<ProductSpecVO>> specVOListMap = hasSpecProductIds.isEmpty()
				? Map.of()
				: productSpecService.lambdaQuery()
				.in(ProductSpec::getProductId, hasSpecProductIds)
				.orderByAsc(ProductSpec::getSort)
				.list()
				.stream()
				.collect(Collectors.groupingBy(
						ProductSpec::getProductId,
						Collectors.mapping(s -> {
							ProductSpecVO vo = new ProductSpecVO();
							BeanUtils.copyProperties(s, vo);
							return vo;
						}, Collectors.toList())
				));

		List<Long> productIds = products.stream().map(Product::getId).toList();
		List<Promotion> promotions = promotionService.getActivePromotionList(productIds, categoryIds);

		return products.stream().map(product -> {
			ProductVO vo = new ProductVO();
			BeanUtils.copyProperties(product, vo);
			vo.setId(product.getId());
			vo.setCategoryName(categoryMap.get(product.getCategoryId()));
			if (product.getHasSpec() == 1) {
				vo.setProductSpecList(specVOListMap.get(product.getId()));
			}
			Promotion best = promotionService.findBestPromotion(product, promotions);
			if (best != null) {
				vo.setPromotionName(best.getName());
				vo.setDiscountedPrice(promotionService.calcDiscountedPrice(product.getPrice(), best));
			}
			return vo;
		}).toList();
	}
}
