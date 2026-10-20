package com.app.gym.common.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "GYM_MST_USER")
public class GymUserEntity extends GymBaseEntity {

    @Column(name = "USERNAME", nullable = false, length = 100)
    private String username;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "FULL_NAME", nullable = false, length = 150)
    private String fullName;

    @Column(name = "EMAIL", length = 254)
    private String email;

    @Column(name = "MOBILE_NUMBER", length = 20)
    private String mobileNumber;

    @Column(
        name = "MUST_CHANGE_PASSWORD",
        nullable = false,
        precision = 1
    )
    private Integer mustChangePassword = 1;

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

    public Integer getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Integer mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}