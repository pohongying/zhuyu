package com.luoyi.implatform.controller;

import com.luoyi.imcommon.model.VerificationCode;
import com.luoyi.implatform.dto.LoginDTO;
import com.luoyi.implatform.dto.ModifyPwdDTO;
import com.luoyi.implatform.dto.RegisterDTO;
import com.luoyi.implatform.exception.GlobalException;
import com.luoyi.implatform.result.Result;
import com.luoyi.implatform.result.ResultUtils;
import com.luoyi.implatform.service.UserService;
import com.luoyi.implatform.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Tag(name = "注册登陆")
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登陆", description = "用户登陆")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        System.out.println("用户登登录……");
        LoginVO vo = userService.login(dto);
        return ResultUtils.success(vo);
    }

    @PutMapping("/refreshToken")
    @Operation(summary = "刷新token", description = "用refreshtoken换取新的token")
    public Result refreshToken(@RequestHeader("refreshToken") String refreshToken) {
        LoginVO vo = userService.refreshToken(refreshToken);
        return ResultUtils.success(vo);
    }

    @GetMapping("/verifyCode")
    @Operation(summary = "获取验证码", description = "获取验证码")
    public void getVerifyCode(HttpServletResponse response, HttpSession session) throws IOException {
        try {
            System.out.println("……");
            VerificationCode code = new VerificationCode();
            BufferedImage image = code.getImage();
            String text = code.getText();
            session.setAttribute("verify_code", text);
            // 输出图像
            VerificationCode.output(image, response.getOutputStream());
            System.out.println("输出图像……");
        } catch (IOException e) {
            throw new GlobalException("获取验证码异常");
        }
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册")
    public Result register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return ResultUtils.success();
    }

    @PutMapping("/modifyPwd")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public Result modifyPassword(@Valid @RequestBody ModifyPwdDTO dto) {
        userService.modifyPassword(dto);
        return ResultUtils.success();
    }

}
