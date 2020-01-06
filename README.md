    gmall-parent[pom]
        gmall-common（通用模块）【工具类，通用依赖】（jar）
        gmall-api（dubbo分包建议，接口层&Model层抽取）（jar）
        gmall-cms（Content Manage System：内容管理系统[文章、评论等]）[service]
        gmall-pms（Product Manage System：商品管理系统）[service]
        gmall-oms（Order Manage System：订单管理系统）[service]
        gmall-wms（Warehouse Manage System：仓库管理系统[库存，出入库等]）[ service]
        gmall-ums（User Manage System：用户管理系统[会员/管理员、登陆、注销等]）[service]
        gmall-sms（Sale Manage System：营销管理系统[优惠券、活动等]）[service]
        gmall-admin-web（后台管理web-restapi层，对接前端的Vue项目）[boot-web]
    ==========================为后台提供服务的系统===============================
        gmall-list（商品列表系统）[boot- service]
        gmall-cart（购物车系统）[boot-service]
        gmall-search（检索系统）[boot- service]
        gmall-cas（中央认证系统）[boot- service]
        gmall-order（订单系统）[boot- service]
        gmall-ware（库存系统）[boot- service]
        gmall-seckill（秒杀系统）[boot- service]
    ========================为前端提供服务的系统===============================
        gmall-portal-web（前端web RestAPI层，对接app以及web）[boot-web]


dubbo服务端口分配
        
    208xx gmall-cms（Content Manage System：内容管理系统[文章、评论等]）[service]
    209xx gmall-pms（Product Manage System：商品管理系统）[service]
    210xx gmall-oms（Order Manage System：订单管理系统）[service]
    211xx gmall-wms（Warehouse Manage System：仓库管理系统[库存，出入库等]）[ service]
    212xx gmall-ums（User Manage System：用户管理系统[会员/管理员、登陆、注销等]）[service]
    213xx gmall-sms（Sale Manage System：营销管理系统[优惠券、活动等]）[service]