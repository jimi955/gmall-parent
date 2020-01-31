package com.atguigu.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.search.SearchProductService;
import com.atguigu.gmall.to.es.EsProduct;
import com.atguigu.gmall.vo.search.SearchParam;
import com.atguigu.gmall.vo.search.SearchResponse;
import com.atguigu.gmall.vo.search.SearchResponseAttrVo;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

import static com.atguigu.gmall.constant.EsConstant.PRODUCT_INDEX;
import static com.atguigu.gmall.constant.EsConstant.PRODUCT_TYPE;

@Slf4j
@Service
@Component
public class searchServiceImpl implements SearchProductService {
    @Autowired
    JestClient jestClient;

    @Override
    public SearchResponse searchProduct(SearchParam searchParam) {

        // 1：构建检索条件
        String ds1 = buildDs1(searchParam);
        log.error("商品检索的详细数据：{}", ds1);

        Search search = new Search.Builder("").addIndex(PRODUCT_INDEX).addType(PRODUCT_TYPE).build();
        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            log.error("==== 商品检索失败：原因是==> {}", e.getMessage());
        }

        // 3：将返回的result封装成SearchResponse
        SearchResponse searchResponse = buildSearchResponse(execute);

        //4、封装分页信息
        searchResponse.setPageNum(searchParam.getPageNum());
        searchResponse.setPageSize(searchParam.getPageSize());
        searchResponse.setTotal(execute.getTotal());
        return searchResponse;
    }

    /**
     * 封装检索的结果
     *
     * @param result
     * @return
     */
    private SearchResponse buildSearchResponse(SearchResult result) {
        System.out.println(result.getTotal() + "==>" + result.toString());
        SearchResponse searchResponse = new SearchResponse();
        //1、封装所有的商品信息
        List<SearchResult.Hit<EsProduct, Void>> hits = result.getHits(EsProduct.class);
        for (SearchResult.Hit<EsProduct, Void> hit : hits) {
            EsProduct source = hit.source;
            searchResponse.getProducts().add(source);
        }

        /**
         *  {name:"品牌",values:["小米","苹果"]}
         */
        //2、封装属性信息
        //2.1）、封装品牌进response
        MetricAggregation aggregations = result.getAggregations();

        SearchResponseAttrVo brandId = new SearchResponseAttrVo();
        brandId.setName("品牌");
        //2.2）、获取到品牌
        if (aggregations.getTermsAggregation("brandIdAgg") != null) {
            aggregations.getTermsAggregation("brandIdAgg").getBuckets().forEach((b) -> {
                b.getTermsAggregation("brandNameAgg").getBuckets().forEach((bb) -> {
                    String key = bb.getKey();
                    brandId.getValue().add(key);
                });
            });
        }
        searchResponse.setBrand(brandId);


        //2.3）、获取到分类
        SearchResponseAttrVo category = new SearchResponseAttrVo();
        category.setName("分类");
        if (aggregations.getTermsAggregation("brandIdAgg") != null) {

            aggregations.getTermsAggregation("categoryIdAgg").getBuckets().forEach((b) -> {
                b.getTermsAggregation("productCategoryNameAgg").getBuckets().forEach((bb) -> {
                    String key = bb.getKey();
                    category.getValue().add(key);
                });
            });
        }
        searchResponse.setCatelog(category);


        //2.4）、获取到属性
        if (aggregations.getChildrenAggregation("productAttrAgg") != null) {
            TermsAggregation termsAggregation = aggregations.getChildrenAggregation("productAttrAgg")
                    .getChildrenAggregation("productAttrIdAgg")
                    .getTermsAggregation("productAttrIdAgg");

//        Bucket bucket = childrenAggregation.getAggregation("productAttrId", Bucket.class);
            List<TermsAggregation.Entry> buckets = termsAggregation.getBuckets();
            buckets.forEach((b) -> {
                SearchResponseAttrVo attrVo = new SearchResponseAttrVo();
                //第一层属性id
                attrVo.setProductAttributeId(Long.parseLong(b.getKey()));
                b.getTermsAggregation("productAttrNameAgg").getBuckets().forEach((bb) -> {
                    //第二层是属性的名
                    attrVo.setName(bb.getKey());
                    bb.getTermsAggregation("productAttrValueAgg").getBuckets().forEach((bbb) -> {
                        //第三层是属性的值
                        attrVo.getValue().add(bbb.getKey());
                    });
                });

                searchResponse.getAttrs().add(attrVo);
            });
        }


        return searchResponse;
    }

    /**
     * 构建检索条件
     *
     * @param param
     * @return
     */
    private String buildDs1(SearchParam param) {
        SearchSourceBuilder searchSource = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1、查询
        if (!StringUtils.isEmpty(param.getKeyword())) {
            //小米  must必须满足。match是用来做模糊的，term是用来做精确的，
            // filter是用来和match一样但不计算相关性评分的
            boolQuery.must(QueryBuilders.matchQuery("name", param.getKeyword()));
            //subTitle与keywords作为加分项
            boolQuery.should(QueryBuilders.matchQuery("subTitle", param.getKeyword()));
            boolQuery.should(QueryBuilders.matchQuery("keywords", param.getKeyword()));
        } else {
            //新加的逻辑
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        //2、过滤；重新修改的逻辑
        if (!StringUtils.isEmpty(param.getCatelog3())) {
            //传了分类id
            boolQuery.filter(QueryBuilders.termsQuery("productCategoryName", param.getCatelog3()));
        }
        if (!StringUtils.isEmpty(param.getBrand())) {
            //传了品牌
            boolQuery.filter(QueryBuilders.termsQuery("brandName", param.getBrand()));
        }

        //传了属性，过滤属性
        if (param.getProps() != null && param.getProps().length > 0) {
            String[] props = param.getProps();

            for (String prop : props) {
                String productAttrId = prop.split(":")[0];
                String productAttrValue = prop.split(":")[1];
                boolQuery.filter(
                        QueryBuilders.nestedQuery("attrValueList",
                                QueryBuilders.boolQuery()
                                        .must(
                                                QueryBuilders.termQuery("attrValueList.productAttributeId", productAttrId))
                                        .must(
                                                QueryBuilders.termQuery("attrValueList.value", productAttrValue)), ScoreMode.None)
                );
            }
        }


        String[] props = param.getProps();
        if (props != null) {
            for (String prop : props) {
                //  props=2:a-b-c
                String valus = prop.split(":")[1];

                String[] split = valus.split("-");

                BoolQueryBuilder must = QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("attrValueList.productAttributeId", prop.split(":")[0]))
                        .must(QueryBuilders.termsQuery("attrValueList.value", split));
                //过滤属性
                boolQuery.filter(QueryBuilders.nestedQuery("attrValueList", must, ScoreMode.None));
            }
        }

        //价格区间过滤
        if (param.getPriceFrom() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(param.getPriceFrom()));
        }
        if (param.getPriceTo() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").lte(param.getPriceTo()));
        }
        searchSource.query(boolQuery);


        //2、聚合
        //searchSource.aggregation()
        //2.1、聚合品牌信息
        TermsAggregationBuilder brandAggs = AggregationBuilders.terms("brandIdAgg")
                .field("brandId")
                .size(100)
                .subAggregation(
                        AggregationBuilders.terms("brandNameAgg")
                                .field("brandName")
                                .size(10)
                );
        searchSource.aggregation(brandAggs);

        //2.2）、聚合分类信息
        TermsAggregationBuilder categoryAggs = AggregationBuilders.terms("categoryIdAgg")
                .field("productCategoryId")
                .size(100)
                .subAggregation(
                        AggregationBuilders.terms("productCategoryNameAgg")
                                .field("productCategoryName")
                                .size(100)
                );
        searchSource.aggregation(categoryAggs);


        //2.3）、属性聚合
        FilterAggregationBuilder filter = AggregationBuilders
                .filter(
                        "productAttrIdAgg",
                        QueryBuilders.termQuery("attrValueList.type", "1"
                        )
                );

        filter.subAggregation(AggregationBuilders.terms("productAttrIdAgg")
                .field("attrValueList.productAttributeId")
                .size(100)
                .subAggregation(
                        AggregationBuilders.terms("productAttrNameAgg")
                                .field("attrValueList.name").size(100)
                                .subAggregation(
                                        AggregationBuilders.terms("productAttrValueAgg")
                                                .field("attrValueList.value").size(100)
                                )
                )
        );

        //2.3）、属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("productAttrAgg", "attrValueList")
                .subAggregation(filter);
        searchSource.aggregation(attrAgg);

        //3、高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name").preTags("<b style='color:red'>").postTags("</b>");
        searchSource.highlighter(highlightBuilder);

        //4、分页  0,5  5,5  10,5
        //param.getPageNum()
        searchSource.from((param.getPageNum() - 1) * param.getPageSize());
        searchSource.size(param.getPageSize());

        //5、排序
        if (!StringUtils.isEmpty(param.getOrder())) {
            String order = param.getOrder();
            String type = order.split(":")[0];
            String asc = order.split(":")[1];//asc;desc  2:asc 3:40-50

            if ("0".equals(type)) {
                searchSource.sort(SortBuilders.scoreSort().order(SortOrder.fromString(asc)));
            }
            if ("1".equals(type)) {
                //1、如果一开始没映射上可能导致数据没有。删索引，重新映射
                searchSource.sort(SortBuilders.fieldSort("sale").order(SortOrder.fromString(asc)));
            }
            if ("2".equals(type)) {
                searchSource.sort(SortBuilders.fieldSort("price").order(SortOrder.fromString(asc)));
            }
        }


        System.out.println(searchSource.toString());
        return searchSource.toString();
    }
}
