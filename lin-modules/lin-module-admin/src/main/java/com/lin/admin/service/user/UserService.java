package com.lin.admin.service.user;

import com.lin.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    public void checkUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BusinessException("用户不存在");
        }
        log.info("用户校验通过: {}", userId);
    }
}
