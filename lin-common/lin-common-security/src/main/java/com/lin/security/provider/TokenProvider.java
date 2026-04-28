package com.lin.security.provider;

import com.lin.security.context.LoginUser;

public interface TokenProvider {
    String createToken(LoginUser user);
    LoginUser parseToken(String token);
    boolean validateToken(String token);
}
