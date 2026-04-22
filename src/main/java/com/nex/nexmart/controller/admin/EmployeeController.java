package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.dto.login.PasswordUpdateDTO;
import com.nex.nexmart.model.dto.login.EmployeeRegisterDTO;
import com.nex.nexmart.model.dto.login.ProfileUpdateDTO;
import com.nex.nexmart.model.vo.LoginVO;
import com.nex.nexmart.model.vo.UserVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-员工管理")
@RestController
@RequestMapping("/api/admin/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final UserService userService;

    @Operation(summary = "注册员工账号")
    @PostMapping("/register")
    @PreAuthorize("hasRole('BOSS')")
    public Result<Void> registerAdmin(@RequestBody @Valid EmployeeRegisterDTO dto) {
        log.info("Employee register: {}", dto.getUsername());
        userService.registerAdmin(dto);
        return Result.success();
    }

    @Operation(summary = "分页查询员工列表")
    @GetMapping("/page")
    @PreAuthorize("hasRole('BOSS')")
    public Result<PageResult<UserVO>> page(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        log.info("Employee page current={} size={} keyword={}", current, size, keyword);
        return Result.success(userService.pageAdmins(current, size, keyword, userDetails.getUser().getId()));
    }

    @Operation(summary = "删除员工账号")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BOSS')")
    public Result<Void> deleteEmployee(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @PathVariable Long id) {
        log.info("Boss delete employee id={} by bossId={}", id, userDetails.getUser().getId());
        userService.deleteAdminEmployee(userDetails.getUser().getId(), id);
        return Result.success();
    }

    @Operation(summary = "启用/禁用员工账号（0=禁用 1=正常）")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('BOSS')")
    public Result<Void> updateEmployeeStatus(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @PathVariable Long id,
            @RequestParam Integer status) {
        log.info("Boss set employee id={} status={}", id, status);
        userService.updateAdminEmployeeStatus(userDetails.getUser().getId(), id, status);
        return Result.success();
    }
}
