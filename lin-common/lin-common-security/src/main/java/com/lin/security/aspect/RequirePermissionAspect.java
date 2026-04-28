package com.lin.security.aspect;

import com.lin.common.result.ResultCode;
import com.lin.core.exception.BusinessException;
import com.lin.security.annotation.RequirePermission;
import com.lin.security.context.UserContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class RequirePermissionAspect {

    @Before("@annotation(requirePermission)")
    public void checkPermission(RequirePermission requirePermission) {
        if (!UserContext.getPermissions().contains(requirePermission.value())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
