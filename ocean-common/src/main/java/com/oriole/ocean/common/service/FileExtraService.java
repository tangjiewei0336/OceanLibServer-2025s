package com.oriole.ocean.common.service;

import com.oriole.ocean.common.enumerate.DocStatisticItemType;

public interface FileExtraService {
    boolean docBaseStatisticsInfoChange(Integer fileID, String addNumber, DocStatisticItemType docStatisticItemType);
}
