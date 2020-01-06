package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.pms.vo.PmsProductQueryParam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 商品信息 服务类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-03
 */
public interface ProductService extends IService<Product> {

    Map<String, Object> pageProduct(PmsProductQueryParam param);
}
