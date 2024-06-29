package com.yupi.springbootinit.bizmq;

/**
 * @author 黄昊
 * @version 1.0
 **/
public interface BiMqConstant {
    String BI_EXCHANGE_NAME = "bi_exchange";

    String BI_QUEUE_NAME = "bi_queue";

    String BI_ROUTING_KEY = "bi_routing_key";

    String BI_DLX_QUEUE_NAME = "bi_dlx_queue";

    String BI_DLX_ROUTING_KEY = "bi_dlx_routing_key";

    String BI_DLX_EXCHANGE_NAME = "bi_dlx_exchange";

    // 限制同时生成的图表数量
    int MAX_CONCURRENT_CHARTS = 5;
}
