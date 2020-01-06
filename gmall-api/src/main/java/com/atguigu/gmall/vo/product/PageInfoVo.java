package com.atguigu.gmall.vo.product;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 封装分页传输的数据
 */

@AllArgsConstructor
@NoArgsConstructor
@ApiModel // swagger文档说明注解
@Data  // lombok 自动set get方法
public class PageInfoVo {

    @ApiModelProperty("总记录数")
    private Integer total;

    @ApiModelProperty("总页数")
    private Integer totalPage;

    @ApiModelProperty("每页显示的记录数")
    private Integer pageSize;

    @ApiModelProperty("分页查出的数据")
    private List<? extends Object> list;

    @ApiModelProperty("当前页页码")
    private Integer pageNum;

}
