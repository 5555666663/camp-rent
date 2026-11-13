package com.camp.vo;

import com.camp.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评价展示对象（附带用户名、网点名称）
 *
 * @author camp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentVO extends Comment {

    /** 评价用户名 */
    private String username;

    /** 网点名称 */
    private String storeName;
}
