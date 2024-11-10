package com.luoyi.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luoyi.implatform.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService extends IService<SensitiveWord> {

    /**
     * 查询所有开启的敏感词
     * @return
     */
    List<String> findAllEnabledWords();


    /**
     * 添加敏感词
     * @param sensitiveWord
     */
    Boolean addSensitiveWord(SensitiveWord sensitiveWord);

    /**
     * 删除敏感词
     * @param id
     */
    Boolean deleteSensitiveWord(Long id);


}
