package com.hscloud.hs.cost.account.model.dto;

import lombok.Data;

@Data
public class PageDto{

    protected long size = 20L;

    protected long current = 1L;

}
