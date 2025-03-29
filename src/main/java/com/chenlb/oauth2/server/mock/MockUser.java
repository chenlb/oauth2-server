package com.chenlb.oauth2.server.mock;

public class MockUser {

    private String username;
    private String password;

    // profile
    private String nickname;
    private String avatar;

    // email
    private String email;

    public boolean isMatchPassword(String password) {
        return this.password != null && this.password.equals(password);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "MockUser{" +
                "username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
