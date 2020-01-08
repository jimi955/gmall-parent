package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.pms.vo.PmsProductParam;
import com.atguigu.gmall.pms.vo.PmsProductQueryParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保存商品信息:
 * ------------------------------
 * 考虑事务的添加:
 * 1):哪些东西是一定要回滚的 哪些不需要回滚
 * 商品的核心信息(基本数据 sku)的保存的时候 不要受到无关信息的影响
 * 无关信息出问题 核心信息不用回滚
 * 2):事务的传播行为:
 *      开启事务  如果发现事务开启失败 主程序加注解@EnableTransactionManagement
 *  如何整个方法内的某些方法不回滚
 *
 *  事务的传播行为(propagation): 当前方法的事务(是否要和别人共用一个事务)
 *      REQUIRED(必须): 如果以前有有事务 支持使用以前的事务 如果之前没有 创建一个新的
 *      REQUIRES_NEW(必须用新的): 不管之前有没有支持事务 都创建新的事务使用
 *      SUPPORTS(支持): 有事务就用 没有事务不用
 *      MANDATORY(强制):  一定要有事务 没有事务就会报错
 *      NOT_SUPPORTED(不支持): 不管有没有加事务 我都不生效
 *      NEVER(从不): 只要给我加事务 我就报错  和MANDATORY相反
 *      NESTED: 开启子事务 mysql不支持
 *
 *  外事务{
 *      内事务1 A()  REQUIRED
 *      内事务2 B()  REQUIRED_NEW  可以理解为B有自己单独的事务
 *      内事务3 C()  REQUIRED
 *      内事务3 D()  REQUIRED_NEW
 *  }
 *  场景一:C出了问题后 AC回滚 B不回滚 因为B使用自己的新事务 而B正常完成 因此B不回滚
 *  场景二:D出现了问题 D自己回滚 AC回滚 B不回滚


 *  外事务{
 *      内事务1 A()  REQUIRED
 *      内事务2 B()  REQUIRED_NEW  可以理解为B有自己单独的事务
 *      try{
 *          内事务3 C()  REQUIRED
 *      }catch(){
 *          //C出现异常
 *      }
 *      内事务3 D()  REQUIRED_NEW
 *  }
 *
 * 场景三:如果 C出了问题后 C回滚 因为没有被外事务感知 程序继续执行D  因此DBA不回滚
 *
 *  事务存在的问题:
 *      Service自己调用自己的方法 无法加上真正的自己内部调整的各个事务
 *      解决: 如果是 对象.方法那就好了
 *          把我们的service放到IOC容器中 使用的时候再获取当前类的真正代理对象 使用代理对象调用方法
 *          导入starter-aop 在主类上开启@EnableAspectJAutoProxy(exposeProxy = true): 暴露代理对象
    -----------------------------
    事务的4种隔离级别: 数据库解决读写加锁的问题(数据的底层方案)
    http://blog.itpub.net/26736162/viewspace-2638951/

    读未提交: 读数据库的时候别人的操作没有commit的时候 也是可以读的 数据不安全 可能不一致
    读已提交: 别人的操作没有commit的时候 加锁 别人不可以读 提交后才可以读
    可重复读: 与数据库建立一个长连接 读的时候 也加锁
    串行化:

    -----------------------------
    异常回滚策略:
    异常:
        运行时异常(不受检查异常)
        编译时异常(受检异常)
            FileNotFound 这种异常要么throw要么try_catch
    运行时的异常是一定回滚
    编译时的异常默认是不回滚的
        解决方法:@Transactional的rollbackFor属性 指定哪些异常一定回滚
                    @Transactional(rollbackFor={Exceptioin.class}) :所有的异常都回滚
                @Transactional的noRollbackFor 指定哪些异常不回滚
                    @Transactional(rollbackFor={Exceptioin.class}) :所有的异常都不回滚

 *
 */
@Slf4j
@Component
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    ProductMapper productMapper;

    @Autowired
    ProductAttributeValueMapper productAttributeValueMapper;

    @Autowired
    ProductFullReductionMapper productFullReductionMapper;

    @Autowired
    ProductLadderMapper productLadderMapper;

    @Autowired
    SkuStockMapper skuStockMapper;

    @Autowired
    MemberPriceMapper memberPriceMapper;

    ThreadLocal<Long> productId = new ThreadLocal<>();



    /**
     * 保存商品信息
     *
     * @param productParam
     * @result
     */

    @Transactional
    @Override
    public void saveProduct(PmsProductParam productParam) {
        ProductServiceImpl proxy = (ProductServiceImpl) AopContext.currentProxy();

        // 1) pms_product:保存商品的基本信息
        proxy.savaBaseInfo(productParam);
        // 2) productAttributeValueList 保存这个商品对应的所有属性的值
        proxy.saveProductAttributeValue(productParam);
        // 3) productFullReductionList 满减信息
        proxy.saveFullReduction(productParam);
        // 4) productLadderList 阶梯价格
        proxy.saveProductLadder(productParam);

        // 5) skuStockList sku价格信息
        proxy.saveSkuStock(productParam);

        // 6) memberPriceList 会员价格信息
        proxy.saveMemberPrice(productParam);
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveMemberPrice(PmsProductParam productParam) {
        List<MemberPrice> memberPriceList = productParam.getMemberPriceList();
        memberPriceList.forEach((item) -> {
            item.setProductId(productId.get());
            memberPriceMapper.insert(item);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSkuStock(PmsProductParam productParam) {
        List<SkuStock> skuStockList = productParam.getSkuStockList();
        skuStockList.forEach((item) -> {
            item.setProductId(productId.get());
            skuStockMapper.insert(item);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductLadder(PmsProductParam productParam) {
        List<ProductLadder> ladderList = productParam.getProductLadderList();
        ladderList.forEach((item) -> {
            item.setProductId(productId.get());
            productLadderMapper.insert(item);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFullReduction(PmsProductParam productParam) {
        List<ProductFullReduction> fullReductionList = productParam.getProductFullReductionList();
        fullReductionList.forEach((item) -> {
            item.setProductId(productId.get());
            productFullReductionMapper.insert(item);
//            int i = 10 / 0;
//            File file = new File("xxx");
//            new FileInputStream(file);
        });
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductAttributeValue(PmsProductParam productParam) {
        List<ProductAttributeValue> valueList = productParam.getProductAttributeValueList();
        valueList.forEach(item -> {
            item.setProductId(productId.get());
            productAttributeValueMapper.insert(item);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savaBaseInfo(PmsProductParam productParam) {
        Product product = new Product();
        BeanUtils.copyProperties(productParam, product);
        productMapper.insert(product);
        // mybatis可以获取存入的id
        // log.debug("刚才保存的商品的id:{}",product.getId());
        productId.set(product.getId());
    }


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
