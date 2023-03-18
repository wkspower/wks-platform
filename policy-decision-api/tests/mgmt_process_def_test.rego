package wks.authz

import future.keywords

test_not_allow_when_not_contain_role_for_user_manager if {
    not allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "host": "app.wkspower.local",
        "allowed_origin": "app.wkspower.local",
        "org": "app",        
        "method": "GET",
        "path": "process-definition"
    }
}

test_allow_when_contain_role_for_user_manager if {
    allow with input as { 
        "realm_access": { "roles": ["mgmt_process_engine"] },
        "host": "app.wkspower.local",
        "allowed_origin": "app.wkspower.local",
        "org": "app",        
        "method": "GET",
        "path": "process-definition"
    }
}