package com.nex.nexmart.service.impl.review;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.model.entity.review.ReviewComment;
import com.nex.nexmart.service.intf.review.ReviewCommentService;
import com.nex.nexmart.mapper.base.ReviewCommentMapper;
import org.springframework.stereotype.Service;

/**
* @author Eric
* @description 针对表【review_comment(评价评论)】的数据库操作Service实现
* @createDate 2026-03-29 12:01:45
*/
@Service
public class ReviewCommentServiceImpl extends ServiceImpl<ReviewCommentMapper, ReviewComment>
    implements ReviewCommentService{

}




