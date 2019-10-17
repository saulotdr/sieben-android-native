package com.sieben.docsystem.sieben;

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

    public String toString() {
        String result = login;
        if (login.contains("\\")){
            result = login.replace("\\", "\\\\");
        }
        return "javascript: " +
                "var user = document.getElementById('user').value='"+ result +"'; " +
                "var pass = document.getElementById('password').value='"+ password +"';";
    }
}
