package com.lin.admin.controller;

import com.lin.admin.model.dto.TestRequestDTO;
import com.lin.common.result.Result;
import com.lin.core.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "测试模块", description = "用于框架验证的测试接口")
@RestController
@RequestMapping("/test")
public class TestController {

    @Operation(summary = "测试成功返回", description = "验证统一返回格式是否生效")
    @GetMapping("/success")
    public Result<String> success() {
        return Result.success("请求成功的数据");
    }

    @Operation(summary = "测试业务异常", description = "验证是否被全局异常拦截器接管")
    @GetMapping("/business-error")
    public Result<Void> businessError() {
        throw new BusinessException("主动抛出的业务异常");
    }

    @Operation(summary = "测试系统异常", description = "验证未知异常的模糊处理")
    @GetMapping("/system-error")
    public Result<Void> systemError() {
        int a = 1 / 0; // 模拟系统异常
        return Result.success();
    }

    @Operation(summary = "测试参数校验", description = "验证 Validation 是否对非法参数拦截")
    @PostMapping("/validate")
    public Result<TestRequestDTO> validate(@Validated @RequestBody TestRequestDTO requestDTO) {
        // 参数校验通过后，直接返回传入的数据
        return Result.success(requestDTO);
    }
}
