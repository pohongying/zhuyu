package com.luoyi.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoyi.implatform.contant.RedisKey;
import com.luoyi.implatform.entity.Friend;
import com.luoyi.implatform.entity.User;
import com.luoyi.implatform.exception.GlobalException;
import com.luoyi.implatform.mapper.FriendMapper;
import com.luoyi.implatform.mapper.UserMapper;
import com.luoyi.implatform.service.FriendService;
import com.luoyi.implatform.session.SessionContext;
import com.luoyi.implatform.session.UserSession;
import com.luoyi.implatform.vo.FriendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = RedisKey.IM_CACHE_FRIEND)  // 统一设置reids
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    private final UserMapper userMapper;


    @Override
    public List<Friend> findFriendByUserId(Long userId) {
        LambdaQueryWrapper<Friend> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Friend::getUserId, userId);
        return this.list(queryWrapper);
    }


    @Transactional(rollbackFor = Exception.class)  // todo 事务是通过代理对象来处理
    @Override
    public void addFriend(Long friendId) {
        long userId = 0;
        try {
            userId = SessionContext.getSession().getUserId();
        } catch (Exception e) {
            throw new GlobalException("无法获取当前登录用户ID");
        }
        if (friendId.equals(userId)) {
            throw new GlobalException("不允许添加自己为好友");
        }
        // 互相绑定好友关系
        // todo 利用springaop 创建代理对象
        FriendServiceImpl proxy = (FriendServiceImpl) AopContext.currentProxy();
        proxy.bindFriend(userId, friendId);
        proxy.bindFriend(friendId, userId);
        log.info("添加好友，用户id:{},好友id:{}", userId, friendId);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delFriend(Long friendId) {
        long userId = 0;
        try {
            userId = SessionContext.getSession().getUserId();
        } catch (Exception e) {
            throw new GlobalException("无法获取当前登录用户ID");
        }
        // 互相解除好友关系，走代理清理缓存
        FriendServiceImpl proxy = (FriendServiceImpl) AopContext.currentProxy();
        proxy.unbindFriend(userId, friendId);
        proxy.unbindFriend(friendId, userId);
        log.info("删除好友，用户id:{},好友id:{}", userId, friendId);
    }


    @Cacheable(key = "#userId1+':'+#userId2")
    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId1)
                .eq(Friend::getFriendId, userId2);
        return this.count(queryWrapper) > 0;
    }


    @Override
    public void update(FriendVO vo) {
        long userId = SessionContext.getSession().getUserId();
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, vo.getId());
        Friend f = this.getOne(queryWrapper);
        if (Objects.isNull(f)) {
            throw new GlobalException("对方不是您的好友");
        }
        f.setFriendHeadImage(vo.getHeadImage());
        f.setFriendNickName(vo.getNickName());
        this.updateById(f);
    }


    /**
     * 单向绑定好友关系
     *
     * @param userId   用户id
     * @param friendId 好友的用户id
     */
    // todo 调用该方法时，会自动检测是否缓存，有则直接返回，没有则调用方法，并将返回值缓存
    @Cacheable(key = "#userId+':'+#friendId")
    public void bindFriend(Long userId, Long friendId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
        if (this.count(queryWrapper) == 0) {
            Friend friend = new Friend();
            friend.setUserId(userId);
            friend.setFriendId(friendId);
            User friendInfo = userMapper.selectById(friendId);
            friend.setFriendHeadImage(friendInfo.getHeadImage());
            friend.setFriendNickName(friendInfo.getNickName());
            this.save(friend);
        }
    }


    /**
     * 单向解除好友关系
     *
     * @param userId   用户id
     * @param friendId 好友的用户id
     */
    @CacheEvict(key = "#userId+':'+#friendId")
    public void unbindFriend(Long userId, Long friendId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
        this.remove(queryWrapper);
    }


    @Cacheable(key = "#friendId")
    @Override
    public FriendVO findFriend(Long friendId) {
        UserSession session = SessionContext.getSession();
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(Friend::getUserId, session.getUserId())
                .eq(Friend::getFriendId, friendId);
        Friend friend = this.getOne(wrapper);
        if (Objects.isNull(friend)) {
            throw new GlobalException("对方不是您的好友");
        }
        FriendVO vo = new FriendVO();
        vo.setId(friend.getFriendId());
        vo.setHeadImage(friend.getFriendHeadImage());
        vo.setNickName(friend.getFriendNickName());
        return vo;
    }
}
