package com.atguigu.gmall.vo.ums;


import lombok.Data;

@Data
public class LoginResponseVo {



    private Long memberLevelId;

    private String username;

    private String nickname;

    private String phone;

    private String accessToken;//访问令牌
}
