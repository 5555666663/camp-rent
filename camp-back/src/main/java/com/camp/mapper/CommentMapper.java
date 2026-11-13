package com.camp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.camp.entity.Comment;
import com.camp.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 评价反馈 Mapper
 *
 * @author camp
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 分页查询评价列表（附带用户名、网点名称）
     *
     * @param page    分页参数
     * @param storeId 网点ID（可空）
     */
    @Select("<script>" +
            "SELECT c.*, u.username, s.store_name FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN store s ON c.store_id = s.id " +
            "WHERE 1=1 " +
            "<if test='storeId != null'> AND c.store_id = #{storeId} </if>" +
            "ORDER BY c.id DESC" +
            "</script>")
    IPage<CommentVO> selectCommentPage(Page<CommentVO> page, @Param("storeId") Long storeId);
}
