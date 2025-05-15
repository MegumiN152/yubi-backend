package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.manager.SSEManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;

/**
 * @author 黄昊
 * @version 1.0
 **/
@RestController
@RequestMapping("/sse")
public class SSEController {
    @Resource
    private SSEManager sseManager;

    @GetMapping("/user/connect")
    public SseEmitter userSSEConnect(@RequestParam("userId") long userId){
        return sseManager.createChartSSEConnection(userId);
    }
    @GetMapping("/team/connect")
    public SseEmitter teamSSEConnect(@RequestParam("teamId") long teamId){
        return sseManager.createTeamChartSSEConnection(teamId);
    }
}
