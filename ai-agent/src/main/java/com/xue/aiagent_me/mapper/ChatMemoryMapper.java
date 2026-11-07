package com.xue.aiagent_me.mapper;

import com.xue.aiagent_me.domain.ChatMemory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* @author 薛
* @description 针对表【chatmemory】的数据库操作Mapper
* @createDate 2026-03-21 19:43:43
* @Entity com.xue.aiagent_me.domain.Chatmemory
*/
@Mapper
public interface ChatMemoryMapper extends BaseMapper<ChatMemory> {

    /**
     * 获取最大消息序号
     */
    @Select("select max(message_order) FROM chatmemory WHERE conversation_id = #{conversationId} AND is_delete = 0")
    Integer getMaxOrder(@Param("conversationId")String conversationId);


    /**
     * 获取会话消息数量
     */
    @Select("SELECT COUNT(*) FROM chatmemory WHERE conversation_id = #{conversationId} AND is_delete = 0")
    int getMessageCount(@Param("conversationId") String conversationId);


    /**
     * 获取最近消息，按消息顺序降序
     */
    @Select("SELECT * FROM chatmemory WHERE conversation_id = #{conversationId} AND is_delete = 0 ORDER BY message_order DESC LIMIT #{limit}")
    List<ChatMemory> getLatestMessages(@Param("conversationId") String conversationId, @Param("limit") int limit);

    /**
     * 逻辑删除会话消息
     */
    @Update("UPDATE chatmemory SET is_delete = 1, update_time = NOW() WHERE conversation_id = #{conversationId} AND is_delete = 0")
    int logicalDeleteByConversationId(@Param("conversationId")String conversationId);

    /**
     * 分页获取消息
     */

    @Select("SELECT * FROM chatmemory where  conversation_id = #{conversationId} AND is_delete = 0 ORDER BY message_order DESC LIMIT #{pageSize} OFFSET #{offset}")
    List<ChatMemory> getMessagesPaginated(@Param("conversationId") String conversationId,
                                          @Param("pageSize") int pageSize, @Param("offset") int offset);
    /**
     * 获取指定偏移和数量的消息
     */
    @Select("SELECT * FROM chatmemory WHERE conversation_id = #{conversationId} AND is_delete = 0 ORDER BY message_order DESC LIMIT #{limit} OFFSET #{offset}")
    List<ChatMemory> getMessagesWithOffset(@Param("conversationId") String conversationId, @Param("limit") int limit,
                                           @Param("offset") int offset);
}




