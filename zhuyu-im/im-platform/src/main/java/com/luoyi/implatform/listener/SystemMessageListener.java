package com.luoyi.implatform.listener;

import com.luoyi.imcommon.annotation.IMListener;
import com.luoyi.imclient.listener.MessageListener;
import com.luoyi.imcommon.enums.IMListenerType;
import com.luoyi.imcommon.enums.IMSendCode;
import com.luoyi.imcommon.model.IMSendResult;
import com.luoyi.implatform.vo.SystemMessageVO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@IMListener(type = IMListenerType.SYSTEM_MESSAGE)
public class SystemMessageListener implements MessageListener<SystemMessageVO> {

    @Override
    public void process(List<IMSendResult<SystemMessageVO>> results) {
        for(IMSendResult<SystemMessageVO> result : results){
            SystemMessageVO messageInfo = result.getData();
            if (result.getCode().equals(IMSendCode.SUCCESS.code())) {
                log.info("消息送达，消息id:{},接收者:{},终端:{}", messageInfo.getId(), result.getReceiver().getId(), result.getReceiver().getTerminal());
            }
        }
    }
}
