package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.ProductAttribute;
import com.atguigu.gmall.pms.mapper.ProductAttributeMapper;
import com.atguigu.gmall.pms.service.ProductAttributeService;
import com.atguigu.gmall.utils.GmallUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p>
 * 商品属性参数表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-04
 */
@Service
@Component
public class ProductAttributeServiceImpl extends ServiceImpl<ProductAttributeMapper, ProductAttribute> implements ProductAttributeService {

    @Autowired
    ProductAttributeMapper productAttributeMapper;

    @Override
    public Map<String, Object> getCategoryAttributes(Long cid, Integer type, Integer pageSize, Integer pageNum) {

        QueryWrapper wrapper = new QueryWrapper<ProductAttribute>()
                .eq("product_attribute_category_id", cid)
                .eq("type", type);
        IPage<ProductAttribute> selectPage = productAttributeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        // 封装数据
        Map<String, Object> pageInfoMap = GmallUtils.getPageInfoMap(selectPage);
        return pageInfoMap;
    }
}
