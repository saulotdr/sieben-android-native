package com.sieben.docsystem.sieben;

import android.support.annotation.NonNull;

class User {
    private String login;
    private String password;

    String getLogin() {
        return login;
    }

    void setLogin(String login) {
        this.login = login;
    }

    String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }

    @NonNull
    public String toString() {
        if (login.contains("\\")){
            login = login.replace("\\", "\\\\");
        }
        return "javascript: " +
                "var user = document.getElementById('user').value='"+ login +"'; " +
                "var pass = document.getElementById('password').value='"+ password +"';";
    }
}
