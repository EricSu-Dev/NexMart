package com.nex.nexmart.service.impl.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.constant.ProductStatusConstant;
import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.base.ProductMapper;
import com.nex.nexmart.model.dto.product.ProductDTO;
import com.nex.nexmart.model.dto.product.ProductSpecDTO;
import com.nex.nexmart.model.entity.Category;
import com.nex.nexmart.model.entity.Promotion;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.entity.product.ProductSpec;
import com.nex.nexmart.model.vo.product.ProductSpecVO;
import com.nex.nexmart.model.vo.product.ProductVO;
import com.nex.nexmart.service.intf.CategoryService;
import com.nex.nexmart.service.intf.PromotionService;
import com.nex.nexmart.service.intf.product.ProductService;
import com.nex.nexmart.service.intf.product.ProductSpecService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

	private final CategoryService categoryService;
	private  final ProductSpecService productSpecService;
	private  final PromotionService promotionService;
	private final StringRedisTemplate stringRedisTemplate;

	private static final String HOME_MANUAL_SECTION_KEY_PREFIX = "NexMart:home:section:manual:";
	private static final String HOME_AUTO_SECTION_KEY_PREFIX = "NexMart:home:section:auto:";

	@Override
	public PageResult<ProductVO> pageProducts(long current, long size, String keyword, Long categoryId,
	                                          Integer status, BigDecimal minPrice, BigDecimal maxPrice,
	                                          String sortBy, String sortOrder, Integer role) {
		LambdaQueryChainWrapper<Product> wrapper = lambdaQuery()
				.like(StringUtils.hasText(keyword), Product::getName, keyword)
				.eq(categoryId != null, Product::getCategoryId, categoryId)
				.ge(minPrice != null, Product::getPrice, minPrice)
				.le(maxPrice != null, Product::getPrice, maxPrice);

		if (Objects.equals(role, UserRoleConstants.ROLE_USER)) {
			// 用户端只能看上架商品
			wrapper.eq(Product::getStatus, ProductStatusConstant.LISTED);
		} else {
			wrapper.eq(status != null, Product::getStatus, status);
		}

		applySort(wrapper, sortBy, sortOrder, role);

		Page<Product> page = wrapper.page(new Page<>(current, size));
		List<ProductVO> voList = convertToVOList(page.getRecords());
		return PageResult.of(voList, page.getTotal(), page.getCurrent(), page.getSize());
	}

	private void applySort(LambdaQueryChainWrapper<Product> wrapper,
	                       String sortBy, String sortOrder, Integer role) {
		// 默认降序
		boolean isAsc = "asc".equalsIgnoreCase(sortOrder);

		if (Objects.equals(role, UserRoleConstants.ROLE_USER)) {
			// 用户端：支持按上架时间、销量、价格排序
			switch (sortBy == null ? "" : sortBy) {
				case "sales":
					wrapper.orderBy(true, isAsc, Product::getSales);
					break;
				case "price":
					wrapper.orderBy(true, isAsc, Product::getPrice);
					break;
				case "time":
				default:
					// 默认按上架时间
					wrapper.orderBy(true, isAsc, Product::getUpdatedAt);
					break;
			}
		} else {
			// 管理端：支持按ID、价格、库存、时间、销量排序
			switch (sortBy == null ? "" : sortBy) {
				case "id":
					wrapper.orderBy(true, isAsc, Product::getId);
					break;
				case "price":
					wrapper.orderBy(true, isAsc, Product::getPrice);
					break;
				case "stock":
					wrapper.orderBy(true, isAsc, Product::getStock);
					break;
				case "sales":
					wrapper.orderBy(true, isAsc, Product::getSales);
					break;
				case "time":
				default:
					// 默认按时间
					wrapper.orderBy(true, isAsc, Product::getCreatedAt);
					break;
			}
		}
	}

	@Override
	public ProductVO getProductDetail(Long id) {
		Product product = getById(id);
		if (product == null) {
			throw new BusinessException("商品不存在");
		}
		ProductVO productVO = convertToVO(product);
		if (product.getHasSpec() == 1) {
			List<ProductSpec> list =
					productSpecService.lambdaQuery()
							.eq(ProductSpec::getProductId, id)
							//按 sort 字段排序返回，不然规格顺序可能每次不一样
							.orderByAsc(ProductSpec::getSort)
							.list();
			productVO.setProductSpecList(convertToSpecVOList(list));
			//计算总库存
			int sum = list.stream().mapToInt(ProductSpec::getStock).sum();
			//不一致更改库存
			if(!product.getStock().equals(sum))
			{
				lambdaUpdate().eq(Product::getId, id).set(Product::getStock, sum).update();
			}
			productVO.setStock(sum);
		}
		return productVO;
	}

	@Transactional
	@Override
	public void addProduct(@Valid ProductDTO dto) {
		if (categoryService.getById(dto.getCategoryId()) == null) {
			throw new BusinessException("分类不存在");
		}
		//如果库存被设置为0,那就把状态设置为售空
		if(dto.getStock()==0){
			dto.setStatus(ProductStatusConstant.SOLD_OUT);
		}
		Product product = new Product();
		BeanUtils.copyProperties(dto, product);

		//判断 Integer 等于几可以直接==,因为会自动拆箱
		// 比较-128~127 范围外的数要用 equals
		// Integer a = 1;
		// Integer b = 1;
		// a == b;  // true，因为 -128~127 范围内 Integer 有缓存，地址一样

		//Integer a = 200;
		//Integer b = 200;
		//a == b;  // false！超出缓存范围，是两个不同对象，地址不同

		//// 方式一：equals 比较值
		//Integer.valueOf(1).equals(dto.getHasSpec())
		//
		//// 方式二：先判断非空再用 == 触发自动拆箱
		//dto.getHasSpec() != null && dto.getHasSpec() == 1


		if(dto.getHasSpec() != null && dto.getHasSpec() == 1
				&& dto.getProductSpecList() != null
				&& !dto.getProductSpecList().isEmpty()){
			//用规格库存的总数覆盖商品库存
			int totalStock = dto.getProductSpecList().stream().mapToInt(ProductSpecDTO::getStock).sum();
			product.setStock(totalStock);
		}
		//无规格直接保存商品
		save(product);
		//否则再保存规格
		if (dto.getHasSpec() != null && dto.getHasSpec() == 1
				&& dto.getProductSpecList() != null
				&& !dto.getProductSpecList().isEmpty()) {
			List<ProductSpec> specs = dto.getProductSpecList().stream()
					.map(specDTO -> {
						ProductSpec spec = new ProductSpec();
						BeanUtils.copyProperties(specDTO, spec);
						spec.setProductId(product.getId());
						return spec;
					})
					.collect(Collectors.toList());
			productSpecService.saveBatch(specs);
		}
		clearHomeSectionAllCache();
	}

	@Transactional
	@Override
	public void updateProduct(Long id, @Valid ProductDTO dto) {
		if (lambdaQuery().eq(Product::getId, id).count() == 0) {
			throw new BusinessException("商品不存在");
		}
		if (categoryService.getById(dto.getCategoryId()) == null) {
			throw new BusinessException("分类不存在");
		}
		Product product = new Product();
		BeanUtils.copyProperties(dto, product);
		product.setId(id); // Set the ID from path variable
		if(dto.getHasSpec() != null && dto.getHasSpec() == 1
				&& dto.getProductSpecList() != null
				&& !dto.getProductSpecList().isEmpty()){
			//用规格库存的总数覆盖商品库存
			int totalStock = dto.getProductSpecList().stream().mapToInt(ProductSpecDTO::getStock).sum();
			product.setStock(totalStock);
		}
		//如果库存被设置为0,那就把状态设置为售空
		if(product.getStock()==0){
			product.setStatus(ProductStatusConstant.SOLD_OUT);
		}
		updateById(product);
		//先删除
		productSpecService.remove(new LambdaQueryWrapper<ProductSpec>().eq(ProductSpec::getProductId, id));
		//再新增
		if(dto.getHasSpec() != null && dto.getHasSpec() == 1
				&& dto.getProductSpecList() != null
				&& !dto.getProductSpecList().isEmpty()){
			List<ProductSpec> collect = dto.getProductSpecList().stream().map(specDTO -> {
				ProductSpec spec = new ProductSpec();
				BeanUtils.copyProperties(specDTO, spec);
				spec.setProductId(id);
				return spec;
			}).collect(Collectors.toList());
			productSpecService.saveBatch(collect);
		}
		clearHomeSectionAllCache();
	}

	@Override
	@Transactional
	public void deleteProduct(Long id) {
		if (lambdaQuery().eq(Product::getId, id).count() == 0) {
			throw new BusinessException("商品不存在");
		}
		removeById(id);
		productSpecService.remove(new LambdaQueryWrapper<ProductSpec>().eq(ProductSpec::getProductId, id));
		clearHomeSectionAllCache();
	}

	@Override
	public void updateProductStatus(Long id, Integer status) {
		Product product = getById(id);
		if(product == null){
			throw new BusinessException("商品不存在");
		}
		if(product.getStatus().equals(ProductStatusConstant.SOLD_OUT) ){
			throw new BusinessException("商品已售空,请先编辑数量");
		}
		lambdaUpdate().eq(Product::getId, id).set(Product::getStatus, status).update();
		clearHomeSectionAllCache();
	}

	private List<ProductVO> convertToVOList(List<Product> products) {
		if (products.isEmpty()) {
			return List.of();
		}

		// 分类名批量查询（已有）
		List<Long> categoryIds = products.stream()
				.map(Product::getCategoryId).distinct().collect(Collectors.toList());
		Map<Long, String> categoryMap = categoryService.getCategoryNameMap(categoryIds);

		// 一次性查出所有当前生效的活动
		List<Long> productIds = products.stream()
				.map(Product::getId).collect(Collectors.toList());

		List<Promotion> activePromotions = promotionService.getActivePromotionList(productIds, categoryIds);

		return products.stream().map(p -> {
			ProductVO vo = new ProductVO();
			BeanUtils.copyProperties(p, vo);
			vo.setCategoryName(categoryMap.getOrDefault(p.getCategoryId(), ""));

			Promotion best = promotionService.findBestPromotion(p, activePromotions);
			if (best != null) {
				vo.setPromotionName(best.getName());
				vo.setDiscountedPrice(promotionService.calcDiscountedPrice(p.getPrice(), best));
			}
			return vo;
		}).collect(Collectors.toList());
	}

	private ProductVO convertToVO(Product product) {
		ProductVO vo = new ProductVO();
		BeanUtils.copyProperties(product, vo);
		Category category = categoryService.getById(product.getCategoryId());
		if (category != null) {
			vo.setCategoryName(category.getName());
		}
		Promotion activePromotion = promotionService.getActivePromotion(product.getId(), product.getCategoryId(),product.getPrice());
		vo.setPromotionName(activePromotion != null ? activePromotion.getName() : null);
		BigDecimal bigDecimal = promotionService.calcDiscountedPrice(product.getPrice(), activePromotion);
		vo.setDiscountedPrice(bigDecimal);
		return vo;
	}

	private List<ProductSpecVO> convertToSpecVOList(List<ProductSpec> list) {
		if (list.isEmpty()) {
			return List.of();
		}
		List<ProductSpecVO> voList = new ArrayList<>();
		for(ProductSpec productSpec: list){
			ProductSpecVO vo = new ProductSpecVO();
			BeanUtils.copyProperties(productSpec, vo);
			voList.add(vo);
		}
		return voList;
	}

	@Override
	public List<Long> extractCategoryIds(List<Product> products) {
		return products.stream().map(Product::getCategoryId).toList();
	}

	public void clearHomeSectionAllCache(){
		List<String> keys = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			keys.add(HOME_MANUAL_SECTION_KEY_PREFIX + i);
			keys.add(HOME_AUTO_SECTION_KEY_PREFIX + i);
		}
		stringRedisTemplate.delete(keys);
	}
}
