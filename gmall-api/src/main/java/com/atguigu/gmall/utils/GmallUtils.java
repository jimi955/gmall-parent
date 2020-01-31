package com.atguigu.gmall.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.HashMap;
import java.util.Map;

public class GmallUtils {
    public static Map<String, Object> getPageInfoMap(IPage selectPage){
        Map<String, Object> map = new HashMap<>();
        map.put("pageSize", selectPage.getSize());
        map.put("totalPage", selectPage.getPages());
        map.put("pageNum", selectPage.getCurrent());
        map.put("total", selectPage.getTotal());
        map.put("list", selectPage.getRecords());
        return map;
    }
}
