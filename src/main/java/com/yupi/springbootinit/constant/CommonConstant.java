package com.yupi.springbootinit.constant;

/**
 * 通用常量
 *
 * @author <a href="https://github.com/MegumiN152">黄昊</a>
 * @from <a href="http://www.huanghao.icu/">GBC智能BI</a>
 */
public interface CommonConstant {

    /**
     * 升序
     */
    String SORT_ORDER_ASC = "ascend";

    /**
     * 降序
     */
    String SORT_ORDER_DESC = " descend";
    String DS_CHAT_MODEL="deepseek-chat";
    String DS_CODE_MODEL="deepseek-coder";

    String USER_AVATAR="https://th.bing.com/th/id/R.2b0c813e3c2124e950be25400b36f4ad?rik=j8QLvmowDnkKhg&pid=ImgRaw&r=0";

    Long BI_MODEL_ID=1798553695154806785L;
    String PRECONDITION="请把你的回答以markdown的格式展现出来,只保留和以下问题相关的内容";
    String REDIS_LIMITER_ID="genChartByAi_";
}
