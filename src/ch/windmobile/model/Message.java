package ch.windmobile.model;

import java.util.Date;

public class Message {

    private Date date;
    private String pseudo;
    private String text;
    private String emailHash;

    public Message(Date date, String pseudo, String text, String emailHash) {
        this.date = date;
        this.pseudo = pseudo;
        this.text = text;
        this.emailHash = emailHash;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getEmailHash() {
        return emailHash;
    }

    public void setEmailHash(String emailHash) {
        this.emailHash = emailHash;
    }
}
