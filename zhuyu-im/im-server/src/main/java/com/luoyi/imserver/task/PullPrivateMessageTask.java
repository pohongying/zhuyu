package com.luoyi.imserver.task;

import com.luoyi.imcommon.contant.IMRedisKey;
import com.luoyi.imcommon.enums.IMCmdType;
import com.luoyi.imcommon.model.IMRecvInfo;
import com.luoyi.imcommon.annotation.RedisMQListener;
import com.luoyi.imserver.netty.processor.AbstractMessageProcessor;
import com.luoyi.imserver.netty.processor.ProcessorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RedisMQListener(queue = IMRedisKey.IM_MESSAGE_PRIVATE_QUEUE, batchSize = 10)
public class PullPrivateMessageTask extends AbstractPullMessageTask<IMRecvInfo> {

    @Override
    public void onMessage(IMRecvInfo recvInfo) {
        AbstractMessageProcessor processor = ProcessorFactory.createProcessor(IMCmdType.PRIVATE_MESSAGE);
        processor.process(recvInfo);
    }

}
