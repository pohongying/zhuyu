package com.luoyi.imclient.listener;


import com.luoyi.imcommon.model.IMSendResult;

import java.util.List;

/** 消息监听器*/
public interface MessageListener<T> {

     void process(List<IMSendResult<T>> result);

}
