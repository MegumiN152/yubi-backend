package com.yupi.springbootinit.model.dto.Chat;

import lombok.Data;

import javax.websocket.Session;

/**
 * @author 黄昊
 * @version 1.0
 **/
@Data
public class AskContent {
    private String question;
    private Long  userid;
}
