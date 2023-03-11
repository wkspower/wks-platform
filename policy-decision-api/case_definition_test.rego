package wks.authz

import future.keywords

test_deny_when_method_not_get_and_user_profile if {
    not allow with input as { 
        "realm_access": { "roles": ["user"] },
        "host": "app.wkspower.local",
        "method": "POST",
        "path": "case-definition"
    }
}

test_allow_when_method_get_and_user_profile if {
    allow with input as { 
        "realm_access": { "roles": ["user"] },
        "host": "app.wkspower.local",
        "method": "GET",
        "path": "case-definition"
    }
}