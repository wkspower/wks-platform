package com.wks.caseengine.rest.server;

import org.springframework.web.bind.annotation.*;
import com.wks.caseengine.service.KeycloakUserService;

import java.util.Map;

@RestController
@RequestMapping("/task/users")
public class UserController {

	private final KeycloakUserService userService;

	public UserController(KeycloakUserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public Map<String, Object> getUsers() throws Exception {
		return userService.getUsers();
	}

	@PutMapping("/{userId}")
	public Map<String, Object> updateUser(@PathVariable String userId, @RequestBody Map<String, Object> data) throws Exception {
		return userService.updateUser(userId, data);
	}

	@GetMapping("/roles")
	public Map<String, Object> getRealmRoles() throws Exception {
		return userService.getRealmRoles();
	}
}
