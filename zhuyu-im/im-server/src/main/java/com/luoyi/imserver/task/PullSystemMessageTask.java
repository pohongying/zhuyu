package com.luoyi.imserver.task;

import com.luoyi.imcommon.contant.IMRedisKey;
import com.luoyi.imcommon.enums.IMCmdType;
import com.luoyi.imcommon.model.IMRecvInfo;
import com.luoyi.imcommon.annotation.RedisMQListener;
import com.luoyi.imserver.netty.processor.AbstractMessageProcessor;
import com.luoyi.imserver.netty.processor.ProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RedisMQListener(queue = IMRedisKey.IM_MESSAGE_SYSTEM_QUEUE,batchSize = 10)
public class PullSystemMessageTask extends AbstractPullMessageTask<IMRecvInfo> {

    @Override
    public void onMessage(IMRecvInfo recvInfo) {
        AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.SYSTEM_MESSAGE);
        processor.process(recvInfo);
    }

}
