package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.Brand;
import com.atguigu.gmall.pms.mapper.BrandMapper;
import com.atguigu.gmall.pms.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 品牌表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-01-04
 */
@Component
@Service
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    @Autowired
    BrandMapper brandMapper;


    @Override
    public Map<String, Object> pageBrand(String keyword, Integer pageNum, Integer pageSize) {
        QueryWrapper<Brand> eq = null;
        //keyword 按照品牌名或者首字母模糊匹配
        if (!StringUtils.isEmpty(keyword)) {
            // like 模糊查询 还有likeLeft  likeRight  自动拼%
            eq = new QueryWrapper<Brand>().like("name", keyword)
                    .eq("first_letter", keyword);
        }
        IPage<Brand> selectPage = brandMapper.selectPage(new Page<>(pageNum, pageSize), eq);

        //封装数据
        Map<String, Object> map = new HashMap<>();
        map.put("pageSize", pageSize);
        map.put("totalPage", selectPage.getPages());
        map.put("total", selectPage.getTotal());
        map.put("pageNum", selectPage.getCurrent());
        map.put("list", selectPage.getRecords());
        return map;
    }
}
