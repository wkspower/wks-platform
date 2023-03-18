package wks.authz

import future.keywords

test_all_methods_allowed_when_user_profile if {
    allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "app.wkspower.local",
        "allowed_origin": "app.wkspower.local",
        "org": "app",        
        "method": "GET",
        "path": "case"
    }
    allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "app.wkspower.local",
        "allowed_origin": "app.wkspower.local",
        "org": "app",        
        "method": "POST",
        "path": "case"
    }
    allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "app.wkspower.local",
        "allowed_origin": "app.wkspower.local",
        "org": "app",        
        "method": "OPTION",
        "path": "case"
    }
}