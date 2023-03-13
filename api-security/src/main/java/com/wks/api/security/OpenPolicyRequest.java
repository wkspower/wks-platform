package com.wks.api.security;
import java.util.Map;

public class OpenPolicyRequest {

    private Map<String, Object> input;

    public OpenPolicyRequest(Map<String, Object> input) {
        this.input = input;
    }
    
    public Map<String, Object> getInput() {
        return input;
    }
}