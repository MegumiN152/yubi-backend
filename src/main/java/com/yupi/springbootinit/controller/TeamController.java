package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.dto.chart.ChartQueryRequest;
import com.yupi.springbootinit.model.dto.chart.ChartRegenRequest;
import com.yupi.springbootinit.model.dto.team.TeamAddRequest;
import com.yupi.springbootinit.model.dto.team.TeamQueryRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.Team;
import com.yupi.springbootinit.model.vo.BIResponse;
import com.yupi.springbootinit.model.vo.TeamVO;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.service.TeamService;
import com.yupi.springbootinit.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 黄昊
 * @version 1.0
 **/
@RestController
@RequestMapping("/team")
public class TeamController {
    @Resource
    private TeamService teamService;

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Boolean> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        boolean b = teamService.addTeam(teamAddRequest, request);
        return ResultUtils.success(b);
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<TeamVO>> listTeam(@RequestBody TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        Page<TeamVO> teamVOPage = teamService.listTeam(teamQueryRequest, request);
        return ResultUtils.success(teamVOPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody Team team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        boolean b = teamService.joinTeam(team, request);
        return ResultUtils.success(b);
    }

    @PostMapping("/exit")
    public BaseResponse<Boolean> exitTeam(@RequestBody Team team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        boolean b = teamService.exitTeam(team, request);
        return ResultUtils.success(b);
    }

    @PostMapping("/page/my/joined")
    public BaseResponse<Page<TeamVO>> listMyJoinedTeam(@RequestBody TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        Page<TeamVO> teamVOPage = teamService.pageMyJoinTeamVO(teamQueryRequest, request);
        return ResultUtils.success(teamVOPage);
    }

    @PostMapping("/chart/page")
    public BaseResponse<Page<Chart>> listTeamChartByPage(@RequestBody ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        Long teamId = chartQueryRequest.getTeamId();
        if (teamId == null && teamId < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        Page<Chart> chartPage = chartService.pageTeamChart(chartQueryRequest);
        return ResultUtils.success(chartPage);
    }
    @GetMapping("/list/my/joined")
    public BaseResponse<List<Team>> listAllMyJoinedTeam(HttpServletRequest request) {
        return ResultUtils.success(teamService.listAllMyJoinTeam(request));
    }
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/page")
    public BaseResponse<Page<Team>> pageTeam(@RequestBody TeamQueryRequest teamQueryRequest) {
        if (teamQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Team> teamPage = teamService.pageTeams(teamQueryRequest);
        return ResultUtils.success(teamPage);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team, HttpServletRequest request) {
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = teamService.updateTeam(team, request);
        return ResultUtils.success(b);
    }

    @AuthCheck(mustRole = "admin")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = teamService.deleteTeam(deleteRequest);
        return ResultUtils.success(b);
    }
    @PostMapping("/chart/regen")
    public BaseResponse<BIResponse> regenChart(@RequestBody ChartRegenRequest chartRegenRequest,
                                               HttpServletRequest request) {
        if (chartRegenRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        BIResponse biResponse = chartService.regenChartByAsyncMqFromTeam(chartRegenRequest, request);
        return ResultUtils.success(biResponse);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(Long id) {
        if (id == null || id < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        return ResultUtils.success(team);
    }
}
