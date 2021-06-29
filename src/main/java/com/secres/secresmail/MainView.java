package com.secres.secresmail;

import com.sun.mail.imap.IMAPFolder;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import jakarta.mail.*;
import jakarta.mail.event.MessageChangedEvent;
import jakarta.mail.event.MessageChangedListener;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and
 * use @Route annotation to announce it in a URL as a Spring managed
 * bean.
 * Use the @PWA annotation make the application installable on phones,
 * tablets and some desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every
 * browser tab/window.
 */
@Route
@PWA(name = "SecresMail",
        shortName = "SecresMail",
        description = "This is an example Vaadin application.",
        enableInstallPrompt = false)
@PageTitle("Login | SecresMail")
public class MainView extends VerticalLayout {

    private String USERNAME;
    private String PASSWORD;
    private final int IMAPS_PORT = 993;
    private final String HOST = "imap.googlemail.com";
    private static Folder emailFolder;
    private static ArrayList<EmailBean> rowList = new ArrayList<>();
    private static Message[] messages;
    private UI ui = UI.getCurrent();

    /**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     */
    public MainView() {
        final LoginOverlay loginOverlay = new LoginOverlay();
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setAdditionalInformation("Allow less secure app access and unlock captcha to log in");
        loginOverlay.setI18n(i18n);
        loginOverlay.setOpened(true);
        loginOverlay.setForgotPasswordButtonVisible(false);
        loginOverlay.setTitle("SecresMail");
        loginOverlay.setDescription("A Mail Client for the Web");

        if(TableView.getGrid() != null) {
            rowList.clear(); // clear when going back to login screen
            TableView.getGrid().setItems(rowList);
        }

        loginOverlay.addLoginListener(e -> {
            USERNAME = e.getUsername();
            PASSWORD = e.getPassword();
            boolean isAuthenticated = authenticate(e);
            if(isAuthenticated) {
                loginOverlay.getUI().ifPresent(ui -> ui.navigate("mail"));
                loginOverlay.setOpened(false);
                rowList.clear(); // clear
                TableView.getGrid().setItems(rowList);
                new Thread(() -> {
                    try {
                        messages = emailFolder.getMessages();

                        for(int i = messages.length - 1; i >= 0; i--) {
                            if(!emailFolder.isOpen()) {
                                emailFolder.open(Folder.READ_WRITE);
                            }
                            Message message = messages[i];

                            EmailBean row = new EmailBean();
                            row.setSubject(message.getSubject());
                            row.setRead(message.isSet(Flags.Flag.SEEN));
                            row.setFrom(message.getFrom()[0]);
                            row.setDate(message.getSentDate());
                            rowList.add(row);

                            ui.access(() -> {
                                TableView.getGrid().setItems(rowList);
                                TableView.getGrid().getDataProvider().refreshAll();
                            });
                        }
                    } catch (MessagingException messagingException) {
                        messagingException.printStackTrace();
                    }
                }).start();
            }
            else {
                loginOverlay.setError(true);
            }
        });

        add(loginOverlay);
    }

    private boolean authenticate(AbstractLogin.LoginEvent e) {
        // create properties field
        Properties properties = new Properties();
        properties.setProperty("mail.imaps.partialfetch", "false");
        properties.setProperty("mail.user", USERNAME);
        properties.setProperty("mail.password", PASSWORD);

        Session emailSession = Session.getDefaultInstance(properties);

        Store store;
        try {
            store = emailSession.getStore("imaps");
        } catch (NoSuchProviderException noSuchProviderException) {
            noSuchProviderException.printStackTrace();
            return false;
        }

        try {
            store.connect(HOST, IMAPS_PORT, USERNAME, PASSWORD);
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }

        try {
            // create the folder object and open it
            emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);

            startLoop();

            // Adding a MessageCountListener to "listen" to new messages
            emailFolder.addMessageCountListener(new MessageCountAdapter() {
                @Override
                public void messagesAdded(MessageCountEvent ev) {
                    try {
                        if(!emailFolder.isOpen()) {
                            emailFolder.open(Folder.READ_WRITE);
                        }
                        Message[] msgs = ev.getMessages();
                        for(Message message : msgs) {
                            rowList.add(0, new EmailBean(message.getSubject(), message.isSet(Flags.Flag.SEEN), message.getFrom()[0], message.getSentDate()));

                            ui.access(() -> {
                                TableView.getGrid().setItems(rowList);
                                TableView.getGrid().getDataProvider().refreshAll();
                            });

                            messages = emailFolder.getMessages(); // update messages array length
                        }
                    } catch (MessagingException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void messagesRemoved(MessageCountEvent ev) {
                    try {
                        if(!emailFolder.isOpen()) {
                            emailFolder.open(Folder.READ_WRITE);
                        }
                        Message[] msgs = ev.getMessages();
                        for(Message message : msgs) {
                            rowList.remove((messages.length - 1) - Arrays.asList(messages).indexOf(message));

                            ui.access(() -> {
                                TableView.getGrid().setItems(rowList);
                                TableView.getGrid().getDataProvider().refreshAll();
                            });

                            emailFolder.expunge();
                            messages = emailFolder.getMessages(); // update messages array length
                        }
                    } catch (MessagingException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            emailFolder.addMessageChangedListener(new MessageChangedListener() {
                @Override
                public void messageChanged(MessageChangedEvent e) {
                    if(e.getMessageChangeType() == MessageChangedEvent.FLAGS_CHANGED) {
                        try {
                            if(rowList.get((messages.length - 1) - Arrays.asList(messages).indexOf(e.getMessage())).getRead() == e.getMessage().isSet(Flags.Flag.SEEN)) {
                                // If this flag (SEEN) is not the one that has changed i.e. the read values on client and server are same, return
                                return;
                            }
                            else {
                                // Set message read value to server's value
                                rowList.get((messages.length - 1) - Arrays.asList(messages).indexOf(e.getMessage())).setRead(e.getMessage().isSet(Flags.Flag.SEEN));
                                ui.access(() -> {
                                    TableView.getGrid().setItems(rowList);
                                    TableView.getGrid().getDataProvider().refreshAll();
                                });
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
        } catch (MessagingException e2) {
            e2.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Creates a new {@link Thread} to enter idle mode.
     */
    private void startLoop() {
        new Thread(() -> {
            while(true) {
                // idle() corresponds to the IMAP IDLE command. The server automatically sends
                // notifications, eliminating the need for polling.
                // We put this in a loop because the idle() method will return when an IMAP
                // command is issued (for example when we mark as read).
                try {
                    ((IMAPFolder) emailFolder).idle();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static Message[] getMessages() {
        return messages;
    }

    public static Folder getFolder() {
        return emailFolder;
    }

    public static List<EmailBean> getRowList() {
        return rowList;
    }

}
