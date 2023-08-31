package wks.authz

import future.keywords

test_denied_if_org_diff_dns_prefix if {
    not allow with input as { 
        "realm_access": { "roles": ["client_case"] },
        "org": "invalid",
        "host": "app.wkspower.local",
        "method": "GET",
        "path": "case"
    }
}