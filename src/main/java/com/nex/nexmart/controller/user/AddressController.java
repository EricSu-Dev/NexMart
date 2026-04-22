package com.nex.nexmart.controller.user;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.entity.Address;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户端-地址接口")
@RestController
@RequestMapping("/api/user/address")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "获取当前用户所有地址")
    @GetMapping("/list")
    public Result<List<Address>> list(@AuthenticationPrincipal SecurityUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
		log.info("获取用户{}所有地址", userId);
        return Result.success(addressService.listByUserId(userId));
    }

	@Operation(summary = "新增地址")
	@PostMapping("/add")
	public Result<String> add(@RequestBody Address address,
	                          @AuthenticationPrincipal SecurityUserDetails userDetails) {
		address.setUserId(userDetails.getUser().getId());
		addressService.addAddress(address);
		return Result.success("新增成功");
	}

	@Operation(summary = "修改地址")
	@PutMapping("/update")
	public Result<String> update(@RequestBody Address address,
	                             @AuthenticationPrincipal SecurityUserDetails userDetails) {
		address.setUserId(userDetails.getUser().getId());
		addressService.updateAddress(address);
		return Result.success("修改成功");
	}

    @Operation(summary = "设置默认地址")
    @PutMapping("/default/{id}")
    public Result<String> setDefault(@PathVariable Long id, @AuthenticationPrincipal SecurityUserDetails userDetails) {
		addressService.setDefault(userDetails.getUser().getId(), id);
        return Result.success("设置成功");
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, @AuthenticationPrincipal SecurityUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        addressService.removeAddress(userId, id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取地址详情")
    @GetMapping("/{id}")
    public Result<Address> getById(@PathVariable Long id) {
        return Result.success(addressService.getById(id));
    }
}
