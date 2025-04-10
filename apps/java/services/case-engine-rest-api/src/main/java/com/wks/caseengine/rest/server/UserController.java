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
    public Map<String, Object> getUsers(@RequestParam(defaultValue = "master") String realm) {
        return userService.getUsers(realm);
    }
    
    @PutMapping("/{userId}/attributes")
    public Map<String, Object> updateUserAttributes(
    		@RequestParam(defaultValue = "master") String realm,
            @PathVariable String userId,
            @RequestBody Map<String, String> attributes) {
    	return userService.updateUserAttributes(realm, userId, attributes);
    }
}

