package com.project.shopapp.controller;

import com.project.shopapp.models.Role;
import com.project.shopapp.responses.ResponseObject;
import com.project.shopapp.services.role.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllRoles(){
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message("Get all roles successfully")
                        .status(HttpStatus.OK)
                        .data(roles)
                .build());
    }
}
