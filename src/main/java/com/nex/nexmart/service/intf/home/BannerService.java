package com.nex.nexmart.service.intf.home;

import com.nex.nexmart.model.dto.home.BannerDTO;
import com.nex.nexmart.model.entity.home.Banner;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.vo.home.BannerVO;

import java.util.List;

/**
* @author Eric
* @description 针对表【banner】的数据库操作Service
* @createDate 2026-04-01 15:27:37
*/
public interface BannerService extends IService<Banner> {

	List<BannerVO> getActiveBanners();

	List<BannerVO> getAllBanners();

	void addBanner(BannerDTO dto);               // 新增
	
	void updateBanner(Long id, BannerDTO dto);   // 编辑
	
	void deleteBanner(Long id);                  // 删除
	
	void updateStatus(Long id, Integer status);  // 上下架

	BannerVO getDetails(Long id);
}
