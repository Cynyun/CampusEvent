package com.Cynyun.campusevent.policy;

import com.Cynyun.campusevent.common.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    private static final String HASH = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

    @Test
    void matchesDemoPassword() {
        assertTrue(PasswordUtil.matches("123456", HASH));
        assertFalse(PasswordUtil.matches("wrong", HASH));
    }
}