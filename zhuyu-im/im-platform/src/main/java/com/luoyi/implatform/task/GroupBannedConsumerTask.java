package com.luoyi.implatform.task;

import com.luoyi.imclient.IMClient;
import com.luoyi.imcommon.enums.IMTerminalType;
import com.luoyi.imcommon.model.IMGroupMessage;
import com.luoyi.imcommon.model.IMUserInfo;
import com.luoyi.imcommon.mq.RedisMQConsumer;
import com.luoyi.imcommon.annotation.RedisMQListener;
import com.luoyi.implatform.contant.Constant;
import com.luoyi.implatform.contant.RedisKey;
import com.luoyi.implatform.dto.GroupBanDTO;
import com.luoyi.implatform.entity.GroupMessage;
import com.luoyi.implatform.enums.MessageStatus;
import com.luoyi.implatform.enums.MessageType;
import com.luoyi.implatform.service.GroupMemberService;
import com.luoyi.implatform.service.GroupMessageService;
import com.luoyi.implatform.util.BeanUtils;
import com.luoyi.implatform.vo.GroupMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author: Blue
 * @date: 2024-07-15
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RedisMQListener(queue = RedisKey.IM_QUEUE_GROUP_BANNED)
public class GroupBannedConsumerTask extends RedisMQConsumer<GroupBanDTO> {

    private final IMClient imClient;

    private final GroupMessageService groupMessageService;

    private final GroupMemberService groupMemberService;

    @Override
    public void onMessage(GroupBanDTO dto) {
        log.info("群聊被封禁处理,群id:{},原因:{}", dto.getId(), dto.getReason());
        // 群聊成员列表
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(dto.getId());
        // 保存消息
        GroupMessage msg = new GroupMessage();
        msg.setGroupId(dto.getId());
        String tip = "本群聊已被管理员封禁,原因:" + dto.getReason();
        msg.setContent(tip);
        msg.setSendId(Constant.SYS_USER_ID);
        msg.setSendTime(new Date());
        msg.setStatus(MessageStatus.UNSEND.code());
        msg.setSendNickName("系统管理员");
        msg.setType(MessageType.TIP_TEXT.code());
        groupMessageService.save(msg);
        // 推送提示语到群聊中
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(Constant.SYS_USER_ID, IMTerminalType.PC.code()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setSendResult(true);
        sendMessage.setSendToSelf(false);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
    }
}
