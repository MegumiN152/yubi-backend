package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.model.dto.team.TeamAddRequest;
import com.yupi.springbootinit.model.dto.team.TeamQueryRequest;
import com.yupi.springbootinit.model.entity.Team;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.TeamVO;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 黄昊
 * @version 1.0
 **/
public interface TeamService extends IService<Team> {
    @Transactional(rollbackFor = Exception.class)
    boolean addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request);

    boolean deleteTeam(DeleteRequest deleteRequest);

    boolean updateTeam(Team team, HttpServletRequest request);

    Page<TeamVO> listTeam(TeamQueryRequest teamQueryRequest, HttpServletRequest request);

    @Transactional(rollbackFor = Exception.class)
    boolean joinTeam(Team team, HttpServletRequest request);

    @Transactional(rollbackFor = Exception.class)
    boolean exitTeam(Team team, HttpServletRequest request);

    Page<TeamVO> pageMyJoinTeamVO(TeamQueryRequest teamQueryRequest, HttpServletRequest request);

    List<Team> listAllMyJoinTeam(HttpServletRequest request);

    Page<Team> pageTeams(TeamQueryRequest teamQueryRequest);
}
