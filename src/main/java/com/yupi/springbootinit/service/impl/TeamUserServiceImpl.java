package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.TeamUserMapper;
import com.yupi.springbootinit.model.entity.TeamUser;
import com.yupi.springbootinit.service.TeamUserService;
import org.springframework.stereotype.Service;

/**
 * @author 黄昊
 * @version 1.0
 **/
@Service
public class TeamUserServiceImpl extends ServiceImpl<TeamUserMapper, TeamUser> implements TeamUserService {
}
