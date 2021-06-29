package com.secres.secresmail;

import jakarta.mail.Address;

import java.util.Date;

public class EmailBean {

    private String subject;
    private boolean read;
    private Address from;
    private Date date;

    public EmailBean() {

    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        try {
            if (this.subject.equals(((EmailBean) obj).getSubject())) {
                if (this.read == ((EmailBean) obj).getRead()) {
                    if (this.from.equals(((EmailBean) obj).getFrom())) {
                        if (this.date.equals(((EmailBean) obj).getDate())) {
                            return true;
                        }
                    }
                }
            }
        } catch (NullPointerException e1) {
            return false;
        }
        return false;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return this.subject;
    }

    public EmailBean(String subject, boolean read, Address from, Date date) {
        this.subject = subject;
        this.read = read;
        this.from = from;
        this.date = date;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setFrom(Address from) {
        this.from = from;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public boolean getRead() {
        return read;
    }

    public Address getFrom() {
        return from;
    }

    public Date getDate() {
        return date;
    }

}
