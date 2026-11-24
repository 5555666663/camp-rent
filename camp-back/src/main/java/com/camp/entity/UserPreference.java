package com.camp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户推荐偏好缓存
 *
 * @author camp
 */
@Data
@TableName("user_preference")
public class UserPreference {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 偏好特征 JSON */
    private String prefJson;

    /** 偏好摘要文本 */
    private String summary;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 过期时间 */
    private LocalDateTime expireTime;
}
