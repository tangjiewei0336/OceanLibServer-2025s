package com.oriole.ocean.common.vo.thirdLogin;

import lombok.Data;

@Data
public class BITLoginInfoEntity {
    private String execution;
    private String pwdEncryptSalt;
    private String cookie;
}
