package wks.authz

import future.keywords

default allow = false

allow {
    input.path = "case"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

allow {
    input.path = "case-definition"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

allow {
    input.path = "record-type"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

allow {
    input.path = "record"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

allow {
    input.path = "task"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

allow {
    input.path = "form"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

allow {
    input.path = "variable"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

allow {
    input.path = "process-instance"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

allow {
    input.path = "email"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

allow {
    input.path = "form"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

allow {
    input.path = "bpm-engine"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

allow {
    input.path = "bpm-engine-type"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

allow {
    input.path = "process-definition"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_user_profile
}

is_user_profile {
    some role in input.realm_access.roles
    role == "user"
}

is_admin_profile {
    some role in input.realm_access.roles
    role == "manager"
}
