package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.TeamChart;
import org.apache.ibatis.annotations.Mapper;

/**
* @author hejiajun
* @description 针对表【team_chart(队伍图表关系表)】的数据库操作Mapper
* @createDate 2024-12-12 12:53:59
* @Entity com.hjj.lingxibi.model.entity.TeamChart
*/
@Mapper
public interface TeamChartMapper extends BaseMapper<TeamChart> {

}



