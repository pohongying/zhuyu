package com.luoyi.imclient.multicast;

import com.luoyi.imclient.listener.MessageListenerMulticaster;
import com.luoyi.imcommon.contant.IMRedisKey;
import com.luoyi.imcommon.enums.IMListenerType;
import com.luoyi.imcommon.model.IMSendResult;
import com.luoyi.imcommon.annotation.RedisMQListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
@RedisMQListener(queue = IMRedisKey.IM_RESULT_GROUP_QUEUE, batchSize = 100)
public class GroupMessageResultResultTask extends AbstractMessageMultiCastTask<IMSendResult> {

    private final MessageListenerMulticaster listenerMulticaster;

    @Override
    public void onMessage(List<IMSendResult> results) {
        listenerMulticaster.multicast(IMListenerType.GROUP_MESSAGE, results);
    }


}
