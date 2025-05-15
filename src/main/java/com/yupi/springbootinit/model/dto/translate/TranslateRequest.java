package com.yupi.springbootinit.model.dto.translate;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author 黄昊
 * @version 1.0
 **/
@Getter
@Setter
public class TranslateRequest implements Serializable {
    //原始语言
    private String sourceLang;
    //目标语言
    private String targetLang;
    //字符串文本
    private String text;
}
