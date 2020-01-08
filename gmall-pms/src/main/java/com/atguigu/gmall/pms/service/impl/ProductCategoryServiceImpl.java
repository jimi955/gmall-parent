package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.pms.entity.ProductCategory;
import com.atguigu.gmall.pms.mapper.ProductCategoryMapper;
import com.atguigu.gmall.pms.service.ProductCategoryService;
import com.atguigu.gmall.pms.vo.PmsProductCategoryWithChildrenItem;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 产品分类 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-04
 */
@Slf4j
@Component
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {


    // 分布式缓存推荐使用redis
    @Autowired
    RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    ProductCategoryMapper productCategoryMapper;

    /**
     * 查某个菜单的所有子菜单
     */
    @Override
    public List<PmsProductCategoryWithChildrenItem> listWithChildren() {

        ValueOperations<Object, Object> ops = redisTemplate.opsForValue();
        Object cache = ops.get(SysCacheConstant.PRODUCT_CATEGORY_CACHE_KEY);
        List<PmsProductCategoryWithChildrenItem> items;
        if (cache != null) {
            log.debug("PRODUCT_CATEGORY_CACHE 缓存命中....");
            //转化过来返回出去
            items = (List<PmsProductCategoryWithChildrenItem>) cache;
            return items;
        }
        log.debug("PRODUCT_CATEGORY_CACHE 缓存未命中，去查询数据库");

        items = productCategoryMapper.listWithChildren(0); // 查询一级子目录
        // 存入缓存
        ops.set(SysCacheConstant.PRODUCT_CATEGORY_CACHE_KEY, items);
        return items;
    }
}
