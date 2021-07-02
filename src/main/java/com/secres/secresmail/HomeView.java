package com.secres.secresmail;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.templatemodel.TemplateModel;

@Route(value = "", layout = HomeLayout.class)
@PWA(name = "SecresMail",
        shortName = "SecresMail",
        description = "This is an example Vaadin application.",
        enableInstallPrompt = false)
@Tag("home-view")
@JsModule("./src/views/home-view.js")
public class HomeView extends PolymerTemplate<TemplateModel> {

    @Id("header")
    Div header;

    @Id("main")
    Div main;

    @Id("footer")
    Div footer;

    public HomeView() {
        try {
            if (LoginView.getLoginOverlay() != null) {
                LoginView.getLoginOverlay().setOpened(false);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        createHeader();

        Div hero = new Div();
        hero.addClassName("hero");
        hero.add(new H1("SecresMail"));
        hero.add(new Paragraph("A mail client made in Java for both the frontend and backend. " +
                "Jakarta Mail (under Jakarta EE) is the core backend for interacting with the mail server using the IMAP and SMTP protocols. " +
                "All supported platforms share some of the features listed below."));

        Div cardList = new Div();
        cardList.addClassName("card-list");
        ExCard card;
        Button button;

        card = new ExCard("Desktop");
        card.add(new UnorderedList(new ListItem("Custom installation"),
                new ListItem("Find/Search support"),
                new ListItem("View attachments"),
                new ListItem("Send email")));
        button = new Button("Visit now");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs("window.open(\"https://github.com/PranavAmarnath/SecresMail\", \"_self\");");
        });
        card.add(button);
        cardList.add(card);

        card = new ExCard("Web");
        card.add(new UnorderedList(new ListItem("PWA installation"),
                new ListItem("Secure login"),
                new ListItem("Mark as (un)read"),
                new ListItem("Lazy loading")));
        button = new Button("Login");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        card.add(button);
        cardList.add(card);

        card = new ExCard("Mobile");
        card.add(new UnorderedList(new ListItem("PWA installation"),
                new ListItem("Sorting by columns"),
                new ListItem("Secure with SSL"),
                new ListItem("Synchronous updates")));
        button = new Button("Coming soon!");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        card.add(button);
        cardList.add(card);

        createFooter();

        main.add(hero);
        main.add(cardList);
    }

    public Component createTopMenu() {
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
        menuBar.addItem(new Anchor("", "Home"));
        menuBar.addItem(new Anchor("/mail", "Mail"));
        Button loginButton = new Button("Login", e -> {
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        menuBar.addItem(loginButton);

        return menuBar;
    }

    public void createHeader() {
        header.add(new H2("Secres"), createTopMenu());
    }

    private void createFooter() {
        Div box;

        box = new Div();
        box.addClassName("copyright-box");
        Paragraph copyright = new Paragraph("© 2021");
        copyright.addClassName("copyright");
        box.add(copyright);
        footer.add(box);

        box = new Div();
        box.add(new H2("Secres"));
        box.add(new UnorderedList(new ListItem(new Anchor("https://github.com/PranavAmarnath/SecresBrowser", "SecresBrowser")),
                new ListItem(new Anchor("https://github.com/PranavAmarnath/SecresCam", "SecresCam")),
                new ListItem(new Anchor("https://github.com/PranavAmarnath/SecresCSV", "SecresCSV")),
                new ListItem(new Anchor("https://github.com/PranavAmarnath/SecresMail", "SecresMail")),
                new ListItem(new Anchor("https://github.com/PranavAmarnath/SecresMail-Web", "SecresMail-Web")),
                new ListItem(new Anchor("https://github.com/PranavAmarnath/SecresOS", "SecresOS"))));
        footer.add(box);

        box = new Div();
        box.add(new H2("Resources"));
        box.add(new UnorderedList(new ListItem(new Anchor("https://cdn.pixabay.com/photo/2016/06/13/17/30/mail-1454731_1280.png", "Favicon")),
                new ListItem(new Anchor("https://github.com/PranavAmarnath/SecresMail-Web/blob/master/LICENSE", "License")),
                new ListItem(new Anchor("https://github.com/PranavAmarnath", "Profile")),
                new ListItem(new Anchor("https://myaccount.google.com/lesssecureapps", "Less Secure Apps")),
                new ListItem(new Anchor("https://accounts.google.com/DisplayUnlockCaptcha", "Display Unlock Captcha"))));
        footer.add(box);

        box = new Div();
        box.add(new H2("Libraries"));
        box.add(new UnorderedList(new ListItem(new Anchor("https://eclipse-ee4j.github.io/mail/", "Jakarta Mail")),
                new ListItem(new Anchor("https://spring.io/projects/spring-boot", "Spring Boot")),
                new ListItem(new Anchor("https://vaadin.com/flow", "Vaadin Flow"))));
        footer.add(box);
    }

}
