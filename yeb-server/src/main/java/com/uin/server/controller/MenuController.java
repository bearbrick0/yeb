package com.uin.server.controller;


import com.uin.server.pojo.Menu;
import com.uin.server.service.IMenuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author wanglufei
 * @since 2021-08-06
 */
@RestController
@RequestMapping("/system/cfg")
public class MenuController {
    @Autowired
    private IMenuService iMenuService;

    @ApiOperation(value = "通过用户ID查询菜单列表")
    @GetMapping("/menu")
    public List<Menu> getMenusByAdminId() {
        return iMenuService.getMenusByAdminId();
    }

}