package com.mkyuan.fountaingateway.admin.service;

import com.mkyuan.fountaingateway.admin.bean.LoginBean;
import com.mkyuan.fountaingateway.admin.bean.UserInfo;

public interface ILoginService {
    public boolean verifiedLogin(LoginBean loginBean)throws Exception;

    public UserInfo doLogin(LoginBean loginBean) throws  Exception;
}
