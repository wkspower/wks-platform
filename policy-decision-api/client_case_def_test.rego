package wks.authz

import future.keywords

test_all_methods_allowed_when_user_profile if {
    allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "app.wkspower.local",
        "method": "GET",
        "path": "case-definition"
    }
    allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "app.wkspower.local",
        "method": "OPTION",
        "path": "case-definition"
    }
    not allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "app.wkspower.local",
        "method": "POST",
        "path": "case-definition"
    }
}