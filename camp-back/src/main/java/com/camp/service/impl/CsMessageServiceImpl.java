package com.camp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.camp.entity.CsMessage;
import com.camp.mapper.CsMessageMapper;
import com.camp.service.CsMessageService;
import org.springframework.stereotype.Service;

/**
 * 客服咨询消息服务实现
 *
 * @author camp
 */
@Service
public class CsMessageServiceImpl extends ServiceImpl<CsMessageMapper, CsMessage> implements CsMessageService {
}
