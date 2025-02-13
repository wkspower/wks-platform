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
    "mgmt_case_def", 
    "mgmt_record_type"
}

has_email_to_case_role := {
    "email_to_case", 
}

allow {
    input.path == "case"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION"]
	check_origin_request
    is_user_profile
}

allow {
    input.path = "case-definition"
    input.method in ["GET", "OPTION"]
	check_origin_request    
    is_user_profile
}

allow {
    input.path = "record-type"
    input.method in ["GET", "OPTION"]
	check_origin_request    
    is_user_profile
}

allow {
    input.path = "record"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
	check_origin_request    
    is_user_profile
}

allow {
    input.path = "task"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
	check_origin_request    
    is_user_profile
}

allow {
    input.path = "form"
    input.method in ["GET", "OPTION"]
	check_origin_request    
    is_user_profile
}

allow {
    input.path = "variable"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
	check_origin_request    
    is_user_profile
}

allow {
    input.path = "process-instance"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
	check_origin_request    
    is_user_profile
}

allow {
    input.path = "process-definition"
    input.method in ["POST", "OPTION", "HEAD"]
	check_origin_request    
    is_user_profile
}

allow {
    input.path = "queue"
    input.method in ["GET", "OPTION"]
	check_origin_request    
    is_user_profile
}

allow {
    input.path = "case-email"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
    check_origin_request
    is_user_profile
}

allow {
    input.path = "case-email"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
    is_email_to_case_profile
}

allow {
    input.path = "record-type"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
	check_origin_request    
    is_manager_profile
}

allow {
    input.path = "form"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
	check_origin_request    
    is_manager_profile
}

allow {
    input.path = "process-definition"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
	check_origin_request    
    is_manager_profile
}

allow {
    input.path = "deployment"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
	check_origin_request    
    is_manager_profile
}

allow {
    input.path = "case-definition"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
	check_origin_request    
    is_manager_profile
}

allow {
    input.path = "message"
    input.method in ["POST", "OPTION", "HEAD"]
	check_origin_request    
    is_manager_profile
}

allow {
    input.path = "queue"
    input.method in ["GET","OPTION"]
	check_origin_request
    debug("check_origin_request passed")
    is_user_profile
    debug("is_user_profile passed")
}

allow {
    input.path = "queue"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
	check_origin_request
    debug("check_origin_request passed")
    is_manager_profile
    debug("is_manager_profile passed")
}

allow {
    input.path = "storage"
    input.method in ["GET", "POST", "OPTION", "HEAD"]
}

allow {
    input.path = "product"
    input.method in ["GET", "POST", "PATCH", "PUT", "DELETE", "OPTION", "HEAD"]
	check_origin_request    
    is_manager_profile
}

check_origin_request if {
    input.allowed_origin == "*"  # Allow any origin
    input.host == "*"            # Allow any host
    input.org == "*"  # Allow any origin
} else {
    not is_null(input.org)  # Ensure 'org' is not null
    input.allowed_origin == input.host  # Match allowed_origin with org
} else {
    is_null(input.host)    # Allow null host
    input.allowed_origin == input.org  # Match allowed_origin with org
} else {
    input.host == ""     # Allow empty host
    input.allowed_origin == input.org  # Match allowed_origin with org
} else {
    input.host == ""             # Allow empty host
    not is_null(input.org)       # Ensure 'org' is not null
}

is_user_profile {
    some role in input.realm_access.roles
    has_client_role[role]
}

is_manager_profile {
    some role in input.realm_access.roles
    has_manager_role[role]
}

is_email_to_case_profile {
    some role in input.realm_access.roles
    has_email_to_case_role[role]
}