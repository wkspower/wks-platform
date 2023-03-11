package wks.authz

import future.keywords

test_allow_when_method_get_and_user_profile if {
    not allow with input as { 
        "realm_access": { "roles": ["user"] },
        "host": "app.wkspower.local",
        "method": "GET",
        "path": "process-definition"
    }
}
