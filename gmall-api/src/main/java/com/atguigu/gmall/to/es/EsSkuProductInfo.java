package com.atguigu.gmall.to.es;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class EsSkuProductInfo extends EsProduct implements Serializable {

    private String skuTitle; // sku特定的标题


    List<EsProductAttributeValue> attributeValues;


}
