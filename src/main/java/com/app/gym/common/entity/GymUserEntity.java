package com.app.gym.common.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "GYM_MST_USER")
public class GymUserEntity extends GymBaseEntity {

    @Column(
        name = "USERNAME",
        nullable = false,
        length = 100
    )
    private String username;

    @Column(
        name = "PASSWORD_HASH",
        nullable = false,
        length = 255
    )
    private String passwordHash;

    @Column(
        name = "FULL_NAME",
        nullable = false,
        length = 150
    )
    private String fullName;

    @Column(
        name = "EMAIL",
        nullable = false,
        length = 254
    )
    private String email;

    @Column(
        name = "MOBILE_NUMBER",
        length = 20
    )
    private String mobileNumber;

    @Column(
        name = "ADDRESS",
        length = 500
    )
    private String address;

    @Column(
        name = "AUTH_VERSION",
        nullable = false
    )
    private Long authVersion = 0L;

    @Column(
        name = "MUST_CHANGE_PASSWORD",
        nullable = false,
        precision = 1
    )
    private Integer mustChangePassword = 1;

    @Column(
        name = "FAILED_LOGIN_COUNT",
        nullable = false
    )
    private Integer failedLoginCount = 0;

    @Column(
        name = "LOGIN_LOCKED",
        nullable = false
    )
    private Integer loginLocked = 0;

    @Column(name = "LAST_OTP_AT")
    private LocalDateTime lastOtpAt;

    public GymUserEntity() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getAuthVersion() {
        return authVersion;
    }

    public void setAuthVersion(Long authVersion) {
        this.authVersion = authVersion;
    }

    public Integer getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Integer mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public Integer getFailedLoginCount() {
        return failedLoginCount;
    }

    public void setFailedLoginCount(Integer failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }

    public Integer getLoginLocked() {
        return loginLocked;
    }

    public void setLoginLocked(Integer loginLocked) {
        this.loginLocked = loginLocked;
    }

    public LocalDateTime getLastOtpAt() {
        return lastOtpAt;
    }

    public void setLastOtpAt(LocalDateTime lastOtpAt) {
        this.lastOtpAt = lastOtpAt;
    }
}