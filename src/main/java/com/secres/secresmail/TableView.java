package com.secres.secresmail;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.mail.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

@Route(value = "mail")
@PageTitle("Mail | SecresMail")
public class TableView extends VerticalLayout {

    private static GridPro<EmailBean> grid;

    /**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     */
    public TableView() {
        createAndShowGrid();
    }

    private void createAndShowGrid() {
        grid = new GridPro<>();
        grid.addColumn(EmailBean::getSubject).setHeader("Subject")/*.setSortable(true)*/;
        grid.addEditColumn(EmailBean::getRead).checkbox((item, newValue) -> {
            item.setRead(newValue);
            try {
                MainView.getFolder().setFlags(new Message[]{MainView.getMessages()[(MainView.getMessages().length - 1) - MainView.getRowList().indexOf(item)]}, new Flags(Flags.Flag.SEEN), newValue);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }).setHeader("Read")/*.setSortable(true)*/;
        grid.addColumn(EmailBean::getFrom).setHeader("From")/*.setSortable(true)*/;
        grid.addColumn(EmailBean::getDate).setHeader("Date")/*.setSortable(true)*/;
        grid.setItems(MainView.getRowList());

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        IFrame htmlFrame = new IFrame();

        grid.addSelectionListener(event -> {
            //new Thread(() -> {
                Optional<EmailBean> selected = event.getFirstSelectedItem();

                Message message = null;
                Object content = null;
                String email = null;
                try {
                    if (!MainView.getFolder().isOpen()) {
                        MainView.getFolder().open(Folder.READ_WRITE);
                    }
                    int index = (MainView.getMessages().length - 1) - MainView.getRowList().indexOf(selected.get());
                    message = MainView.getMessages()[index];
                    content = message.getContent();
                    //mailTable.getModel().setValueAt(true, mailTable.convertRowIndexToModel(mailTable.getSelectedRow()), 1);
                    //ListModel<Object> model = (ListModel<Object>) attachmentsList.getModel();
                    //((DefaultListModel<Object>) model).removeAllElements();
                    email = "";
                    if (content instanceof Multipart) {
                        Multipart mp = (Multipart) content;
                        email = getText(mp.getParent());
                    } else {
                        email = message.getContent().toString();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                htmlFrame.setSrcdoc(email);

                /*
                int attachmentCount = getAttachmentCount(message);
                if(attachmentCount > 0) {
                    try {
                        Multipart multipart = (Multipart) message.getContent();

                        for(int i = 0; i < multipart.getCount(); i++) {
                            // System.out.println("Entered " + i + " file.");

                            BodyPart bodyPart = multipart.getBodyPart(i);
                            if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                                continue; // dealing with attachments only
                            }
                            // do not do this in production code -- a malicious email can easily contain
                            // this filename: "../etc/passwd", or any other path: They can overwrite _ANY_
                            // file on the system that this code has write access to!
                            File f = new File(bodyPart.getFileName());
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                */

                // System.out.println(getAttachmentCount(message)); // prints number of
                // attachments
            //}).start();
        });

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
        splitLayout.addToPrimary(grid);
        splitLayout.addToSecondary(htmlFrame);
        splitLayout.setWidthFull();

        add(splitLayout);
    }

    /**
     * Return the primary text content of the message.
     *
     * @param p the <code>Part</code>
     * @return String the primary text content
     */
    private String getText(Part p) throws MessagingException, IOException {
        if(p.isMimeType("text/*")) {
            return (String) p.getContent();
        }

        if(p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for(int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if(bp.isMimeType("text/plain")) {
                    if(text == null) text = getText(bp);
                    continue;
                }
                else if(bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if(s != null) return s;
                }
                else {
                    return getText(bp);
                }
            }
            return text;
        }
        else if(p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for(int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if(s != null) return s;
            }
        }

        return null;
    }

    public static GridPro<EmailBean> getGrid() {
        return grid;
    }

}
