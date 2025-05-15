package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.TeamMapper;
import com.yupi.springbootinit.model.dto.team.TeamAddRequest;
import com.yupi.springbootinit.model.dto.team.TeamQueryRequest;
import com.yupi.springbootinit.model.entity.Team;
import com.yupi.springbootinit.model.entity.TeamChart;
import com.yupi.springbootinit.model.entity.TeamUser;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.TeamVO;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.service.TeamChartService;
import com.yupi.springbootinit.service.TeamService;
import com.yupi.springbootinit.service.TeamUserService;
import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 黄昊
 * @version 1.0
 **/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {
    @Resource
    private UserService userService;

    @Resource
    private TeamUserService teamUserService;

    @Resource
    private TeamChartService teamChartService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTeam(TeamAddRequest teamAddRequest, HttpServletRequest request) {
        String name = teamAddRequest.getName();
        String imgUrl = teamAddRequest.getImgUrl();
        String description = teamAddRequest.getDescription();
        Integer maxNum = teamAddRequest.getMaxNum();
        if (StringUtils.isEmpty(name)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名不能为空");
        }
        if (StringUtils.isEmpty(imgUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍头像不能为空");
        }
        if (maxNum == null || maxNum <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不能为空");
        }
        if (description.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不能超过100个字符");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        team.setUserId(userId);
        boolean result1 = this.save(team);
        TeamUser teamUser = new TeamUser();
        teamUser.setTeamId(team.getId());
        teamUser.setUserId(userId);
        boolean result2 = teamUserService.save(teamUser);
        return result1 && result2;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(DeleteRequest deleteRequest) {
        Long teamId = deleteRequest.getId();
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        boolean b1 = this.removeById(teamId);
        boolean b2 = teamUserService.remove(new QueryWrapper<TeamUser>().eq("teamId", teamId));
        boolean b3 = teamChartService.remove(new QueryWrapper<TeamChart>().eq("teamId", teamId));
        return b1 && b2 && b3;
    }

    @Override
    public boolean updateTeam(Team team, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        if (!userService.isAdmin(loginUser) && !userId.equals(team.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改队伍信息");
        }
        return this.updateById(team);
    }

    @Override
    public Page<TeamVO> listTeam(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        String searchParams = teamQueryRequest.getSearchParam();
        long current = teamQueryRequest.getCurrent();
        long pageSize = teamQueryRequest.getPageSize();
        String sortField = teamQueryRequest.getSortField();
        String sortOrder = teamQueryRequest.getSortOrder();

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(searchParams), "name", searchParams);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);
        Page<Team> teamPage = this.page(new Page<>(current, pageSize), queryWrapper);
        List<Team> teamPageRecords = teamPage.getRecords();
        List<TeamVO> teamVOList = this.getTeamVOList(teamPageRecords, request);
        Page<TeamVO> teamVOPage = new Page<>(current, pageSize);
        teamVOPage.setRecords(teamVOList);
        teamVOPage.setTotal(teamPage.getTotal());
        return teamVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(Team team, HttpServletRequest request) {
        Long teamId = team.getId();
        team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        if (isInTeam(team, request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已加入该队伍");
        }
        QueryWrapper<TeamUser> teamUserQueryWrapper = new QueryWrapper<>();
        teamUserQueryWrapper.eq("teamId", teamId);
        long count = teamUserService.count(teamUserQueryWrapper);
        if (count >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数已满");
        }
        Long userId = userService.getLoginUser(request).getId();
        TeamUser teamUser = new TeamUser();
        teamUser.setTeamId(teamId);
        teamUser.setUserId(userId);
        return teamUserService.save(teamUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean exitTeam(Team team, HttpServletRequest request) {
        Long teamId = team.getId();
        team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        if (!isInTeam(team, request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入该队伍");
        }
        QueryWrapper<TeamUser> teamUserQueryWrapper = new QueryWrapper<>();
        teamUserQueryWrapper.eq("teamId", teamId);
        long count = teamUserService.count(teamUserQueryWrapper);
        if (count <= 1) {
            teamUserQueryWrapper.eq("userId", userId);
            boolean b1 = teamUserService.remove(teamUserQueryWrapper);
            boolean b2 = this.removeById(teamId);
            QueryWrapper<TeamChart> teamChartQueryWrapper = new QueryWrapper<>();
            teamChartQueryWrapper.eq("teamId", teamId);
            boolean b3 = teamChartService.remove(teamChartQueryWrapper);
            return b1 && b2 && b3;
        }
        teamUserQueryWrapper.orderBy(true, true, "createTime").last("limit 2");
        TeamUser teamUser = teamUserService.list(teamUserQueryWrapper).get(1);
        Long newCaptainId = teamUser.getUserId();
        team.setUserId(newCaptainId);
        boolean b = this.updateById(team);
        teamUserQueryWrapper = new QueryWrapper<>();
        teamUserQueryWrapper.eq("teamId", teamId).eq("userId", userId);
        return b && teamUserService.remove(teamUserQueryWrapper);
    }

    @Override
    public Page<TeamVO> pageMyJoinTeamVO(TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        long current = teamQueryRequest.getCurrent();
        long pageSize = teamQueryRequest.getPageSize();
        String sortField = teamQueryRequest.getSortField();
        String sortOrder = teamQueryRequest.getSortOrder();

        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        QueryWrapper<TeamUser> teamUserQueryWrapper = new QueryWrapper<>();
        teamUserQueryWrapper.eq("userId", userId);
        teamUserQueryWrapper.orderBy(SqlUtils.validSortField(sortField), CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);
        Page<TeamUser> teamUserPage = teamUserService.page(new Page<>(current, pageSize), teamUserQueryWrapper);
        Set<Long> teamIds = teamUserPage.getRecords().stream().map(TeamUser::getTeamId).collect(Collectors.toSet());
        List<Team> teams = this.listByIds(teamIds);
        List<TeamVO> teamVOList = this.getTeamVOList(teams, request);
        Page<TeamVO> teamVOPage = new Page<TeamVO>(current, pageSize).setRecords(teamVOList);
        teamVOPage.setTotal(teamUserPage.getTotal());
        return teamVOPage;
    }

    @Override
    public List<Team> listAllMyJoinTeam(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long id = loginUser.getId();
        QueryWrapper<TeamUser> teamUserQueryWrapper = new QueryWrapper<>();
        teamUserQueryWrapper.eq("userId", id);
        List<TeamUser> teamUsers = teamUserService.list(teamUserQueryWrapper);
        Set<Long> teamIds = teamUsers.stream().map(TeamUser::getTeamId).collect(Collectors.toSet());
        return this.listByIds(teamIds);
    }

    @Override
    public Page<Team> pageTeams(TeamQueryRequest teamQueryRequest) {
        String searchParams = teamQueryRequest.getSearchParam();
        long current = teamQueryRequest.getCurrent();
        long pageSize = teamQueryRequest.getPageSize();
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.like(StringUtils.isNotBlank(searchParams), "name", searchParams);
        teamQueryWrapper.like(StringUtils.isNotBlank(searchParams), "description", searchParams);
        return this.page(new Page<>(current, pageSize), teamQueryWrapper);
    }

    private boolean isInTeam(Team team, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long id = loginUser.getId();
        QueryWrapper<TeamUser> teamUserQueryWrapper = new QueryWrapper<>();
        teamUserQueryWrapper.eq("userId", id);
        teamUserQueryWrapper.eq("teamId", team.getId());
        long count = teamUserService.count(teamUserQueryWrapper);
        return count > 0;
    }

    private List<TeamVO> getTeamVOList(List<Team> teamPageRecords, HttpServletRequest request) {
        Set<Long> userIds = teamPageRecords.stream().map(Team::getUserId).collect(Collectors.toSet());
        Map<Long, UserVO> userMap = userService.listByIds(userIds).stream().map(user -> userService.getUserVO(user)).collect(Collectors.toMap(UserVO::getId, UserVO -> UserVO));
        return teamPageRecords.stream().map(team -> {
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team, teamVO);
            if (userMap.containsKey(team.getUserId())) {
                teamVO.setUserVO(userMap.get(team.getUserId()));
            }
            teamVO.setInTeam(isInTeam(team, request));
            return teamVO;
        }).collect(Collectors.toList());
    }

}
