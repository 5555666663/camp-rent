package com.camp.service;

/**
 * 用户租赁偏好服务：读取历史订单，生成个性化提示
 *
 * @author camp
 */
public interface UserPrefService {

    /**
     * 生成个性化提示文本；无历史记录时返回 null
     *
     * @param userId 用户 ID
     * @return 例如「您常租：帐篷、炊具」
     */
    String summary(Long userId);
}
