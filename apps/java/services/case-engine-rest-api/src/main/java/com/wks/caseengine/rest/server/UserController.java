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

	@PutMapping()
	public Map<String, Object> updateUser(@RequestBody Map<String, Object> data) throws Exception {
		return userService.updateUser(data);
	}

	@GetMapping("/roles")
	public Map<String, Object> getRealmRoles() throws Exception {
		return userService.getRealmRoles();
	}
	
	@GetMapping("/groups")
    public Map<String, Object> getUserGroups() throws Exception {
        return userService.getAllGroups();
    }
	
	@GetMapping("/search")
	public Map<String, Object> getUsers(@RequestParam("search") String search) throws Exception {
		return userService.searchUsers(search);
	}
}
