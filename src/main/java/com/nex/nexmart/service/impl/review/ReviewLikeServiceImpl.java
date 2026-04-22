package com.nex.nexmart.service.impl.review;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.model.entity.review.ReviewLike;
import com.nex.nexmart.service.intf.review.ReviewLikeService;
import com.nex.nexmart.mapper.base.ReviewLikeMapper;
import org.springframework.stereotype.Service;

/**
* @author Eric
* @description 针对表【review_like(评价点赞)】的数据库操作Service实现
* @createDate 2026-03-29 12:02:07
*/
@Service
public class ReviewLikeServiceImpl extends ServiceImpl<ReviewLikeMapper, ReviewLike>
    implements ReviewLikeService{

}




