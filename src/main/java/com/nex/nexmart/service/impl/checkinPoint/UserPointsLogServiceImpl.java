package com.nex.nexmart.service.impl.checkinPoint;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.model.entity.checkinPoint.UserPointsLog;
import com.nex.nexmart.service.intf.checkinPoint.UserPointsLogService;
import com.nex.nexmart.mapper.base.UserPointsLogMapper;
import org.springframework.stereotype.Service;

/**
* @author Eric
* @description 针对表【user_points_log(积分流水表)】的数据库操作Service实现
* @createDate 2026-04-11 18:06:57
*/
@Service
public class UserPointsLogServiceImpl extends ServiceImpl<UserPointsLogMapper, UserPointsLog>
    implements UserPointsLogService{

}




