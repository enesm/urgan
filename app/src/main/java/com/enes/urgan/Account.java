package com.enes.urgan;

class Account {
    private int id;
    private String sessionId;
    private String accountName;
    private int accountLevel;

    Account(int id, String sessionId, String accountName, int accountLevel) {
        this.id = id;
        this.sessionId = sessionId;
        this.accountName = accountName;
        this.accountLevel = accountLevel;
    }

    String getSessionId() {
        return sessionId;
    }

    void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    String getAccountName() {
        return accountName;
    }

    void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    int getAccountLevel() {
        return accountLevel;
    }

    String getAccountLevelName() {
        if (accountLevel == 0) {
            return "Bronz Hesap";
        } else if (accountLevel == 1) {
            return "Gümüş Hesap";
        } else if (accountLevel == 2) {
            return "Altın Hesap";
        } else {
            return "Platin Hesap";
        }
    }

    void setAccountLevel(int accountLevel) {
        this.accountLevel = accountLevel;
    }

    int getId() {
        return id;
    }
}
