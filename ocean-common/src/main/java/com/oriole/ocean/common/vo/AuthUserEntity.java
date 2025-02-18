package com.oriole.ocean.common.vo;

import lombok.Data;

import java.util.List;

import static com.oriole.ocean.common.enumerate.ResultCode.DOCS_NOT_APPROVED;
import static com.oriole.ocean.common.enumerate.ResultCode.UNAUTHORIZED_OPERATION;

@Data
public class AuthUserEntity implements java.io.Serializable {
    private String username = null;
    private String role = null;

    public Boolean isUserOwn(List<String> ownerships){
        return ownerships.contains(this.username) || role.equals("ADMIN");
    }

    public Boolean isUserOwn(String ownership){
        return ownership.equals(this.username) || role.equals("ADMIN");
    }

    public Boolean isAdmin(){
        return role.equals("ADMIN");
    }


    public String getAllowOperationUsername(String username){
        if (username == null) {
            username = this.username;
        } else if(!isUserOwn(username)){
            throw new BusinessException(UNAUTHORIZED_OPERATION);
        }
        return username;
    }

    public AuthUserEntity(String username, String role) {
        this.username = username;
        this.role = role;
    }
}
