package com.nex.nexmart.service.intf.home;

import com.nex.nexmart.model.dto.home.HomeSectionConfigDTO;
import com.nex.nexmart.model.entity.home.HomeSection;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.entity.home.HomeSectionItem;
import com.nex.nexmart.model.vo.home.HomeSectionVO;
import com.nex.nexmart.model.vo.home.HomeVO;
import com.nex.nexmart.model.vo.product.ProductVO;

import java.util.List;

/**
* @author Eric
* @description 针对表【home_section】的数据库操作Service
* @createDate 2026-04-01 20:30:33
*/
public interface HomeSectionService extends IService<HomeSection> {
	List<HomeSectionVO> getAllSections();
	void updateConfig(Integer type, HomeSectionConfigDTO dto);
	List<ProductVO> getItems(Integer type);
	void addItem(Integer type, Long productId);
	void removeItem(Integer type, Long itemId);
	void updateSort(Integer type, List<HomeSectionItem> items);

	HomeVO getHome();
}
