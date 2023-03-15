package wks.authz

import future.keywords

default allow = false

has_client_role := {
    "client_case", 
    "client_task", 
    "client_record"
}

has_manager_role := {
    "mgmt_form", 
    "mgmt_process_engine", 
    "mgmt_bpm_engine",
    "mgmt_bpm_engine_type",
    "mgmt_case_def", 
    "mgmt_record_type"
}

allow {
    input.path = "case"
    input.method in ["GET", "POST", "PUT", "DELETE", "OPTION"]
    is_user_profile
}

allow {
    input.path = "case-definition"
    input.method in ["GET", "OPTION"]
    is_user_profile
}

allow {
    input.path = "record-type"
    input.method in ["GET", "OPTION"]
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
    input.method in ["GET", "OPTION"]
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
}

allow {
    input.path = "record-type"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_manager_profile
}

allow {
    input.path = "form"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_manager_profile
}

allow {
    input.path = "bpm-engine"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_manager_profile
}

allow {
    input.path = "bpm-engine-type"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_manager_profile
}

allow {
    input.path = "process-definition"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_manager_profile
}

allow {
    input.path = "case-definition"
    input.method in ["GET", "POST", "PATCH", "DELETE", "OPTION", "HEAD"]
    is_manager_profile
}

allow {
    input.path = "healthCheck"
    input.method in ["GET"]
}

is_user_profile {
    some role in input.realm_access.roles
    has_client_role[role]
}

is_manager_profile {
    some role in input.realm_access.roles
    has_manager_role[role]
}
