package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.pms.mapper.ProductMapper;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.pms.vo.PmsProductQueryParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-04
 */
@Component
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    ProductMapper productMapper;

    @Override
    public Map<String, Object> pageProduct(PmsProductQueryParam param) {
        // 复杂查询
        QueryWrapper<Product> wrapper = new QueryWrapper<>();

        if (param.getBrandId() != null) {
            wrapper.eq("brand_id", param.getBrandId());
        }
        if (StringUtils.isNotBlank(param.getKeyword())) {
            wrapper.like("name", param.getKeyword());
        }
        if (param.getProductCategoryId() != null) {
            wrapper.eq("product_category_id", param.getProductCategoryId());
        }
        if (StringUtils.isNotBlank(param.getProductSn())) {
            wrapper.like("product_sn", param.getProductSn());
        }
        if (param.getPublishStatus() != null) {
            wrapper.eq("publish_status", param.getPublishStatus());
        }
        if (param.getVerifyStatus() != null) {
            wrapper.eq("verify_status", param.getVerifyStatus());
        }

        //去数据库分页查
        IPage<Product> selectPage = productMapper.selectPage(new Page<Product>(param.getPageNum(), param.getPageSize()), wrapper);
        //封装数据
        Map<String, Object> map = new HashMap<>();
        map.put("pageSize", selectPage.getSize());
        map.put("totalPage", selectPage.getPages());
        map.put("total", selectPage.getTotal());
        map.put("pageNum", selectPage.getCurrent());
        map.put("list", selectPage.getRecords());
        return map;
    }
}
