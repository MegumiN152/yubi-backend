package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Team;
import org.apache.ibatis.annotations.Mapper;

/**
* @author hejiajun
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2024-12-09 10:22:36
* @Entity com.hjj.lingxibi.model.entity.Team
*/
@Mapper
public interface TeamMapper extends BaseMapper<Team> {

}



