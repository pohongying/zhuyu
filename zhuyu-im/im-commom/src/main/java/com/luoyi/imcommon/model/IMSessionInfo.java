package com.luoyi.imcommon.model;

import lombok.Data;

/**
 * 会话信息
 */
@Data
public class IMSessionInfo {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 终端类型
     */
    private Integer terminal;

}
