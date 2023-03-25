package wks.authz

import future.keywords

test_storage if {
    allow with input as {
        "path": "storage",
        "method": "GET",
        "realm_access": { "roles": ["client_case"] },
        "org": "localhost", 
        "host": "localhost", 
        "allowed_origin": "localhost"
    }
}