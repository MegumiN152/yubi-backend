package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.TeamUser;
import org.apache.ibatis.annotations.Mapper;

/**
* @author hejiajun
* @description 针对表【team_user(队伍用户关系表)】的数据库操作Mapper
*/
@Mapper
public interface TeamUserMapper extends BaseMapper<TeamUser> {

}



