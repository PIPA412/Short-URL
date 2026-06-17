package com.shortlink.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.dto.CreateShortLinkDTO;
import com.shortlink.dto.ShortLinkVO;
import com.shortlink.dto.UpdateShortLinkDTO;

public interface ShortLinkService {

    ShortLinkVO create(CreateShortLinkDTO dto, Long userId);

    Page<ShortLinkVO> page(int pageNum, int pageSize, Long userId);

    ShortLinkVO getById(Long id);

    ShortLinkVO getByShortCode(String shortCode);

    void update(Long id, UpdateShortLinkDTO dto, Long userId);

    void delete(Long id, Long userId);

    String getOriginalUrlAndIncrement(String shortCode);
}
