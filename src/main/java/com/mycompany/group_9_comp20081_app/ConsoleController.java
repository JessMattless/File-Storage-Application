/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.group_9_comp20081_app;

import filesystem.SecureStorage;
import filesystem.StorageContainer;
import terminal.Terminal;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import utils.ErrorLogger;
import javafx.scene.robot.Robot;
import utils.Helper;

/**
 * FXML Controller class
 *
 * @author michael
 */
public class ConsoleController implements Initializable {
    
    private static final int MAX_TABS = 5;
    private static final int TAB_TITLE_LEN = 15;
    private int terminalCounter = 1;

    @FXML 
    private MenuButton newTerminalMenu;
    
    @FXML
    private TabPane tabs;
    
    @FXML
    private Tab firstTab = new Tab();
    
    public ConsoleController(){}
    
    private String hostname;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception _e) {
            hostname = App.getCurrentUser().getName();
        }
        
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        firstTab.setClosable(false);
        firstTab.setText(Helper.summarise(hostname, TAB_TITLE_LEN, true));
        ScrollPane sp = new ScrollPane();
        Terminal terminal = new Terminal(null);
        sp.setContent(terminal);
        sp.getStyleClass().add("scrollPane");
        
        terminal.heightProperty().addListener(new ChangeListener() {
            public void changed(ObservableValue obsVal, Object oldVal, Object newVal) {
                sp.setVvalue((Double) newVal);
            }
        });
                
        firstTab.setContent(sp);

        tabs.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>(){
            public void handle(MouseEvent e) {
                for (Tab t : tabs.getTabs()) {
                    if (t.isSelected()) {
                        Node node = t.getContent();
                        ((ScrollPane) node).getContent().requestFocus();
                    }
                }
            }
        });

        newTerminalMenu.setPopupSide(Side.TOP);
        
        MenuItem localMenuItem = new MenuItem(hostname);
        localMenuItem.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent ae) {
                newTerminal(null);
            }
        });
        newTerminalMenu.getItems().add(localMenuItem);
        
        for (StorageContainer storageContainer: SecureStorage.getContainerList()) {
            MenuItem menuItem = new MenuItem();
            menuItem.setOnAction(new EventHandler<ActionEvent>(){
                public void handle(ActionEvent ae) {
                    newTerminal(storageContainer);
                }
            });
            menuItem.setText(storageContainer.getHostname());
            newTerminalMenu.getItems().add(menuItem);
        }
    }   
    
    @FXML
    private void returnToDashboard() {
        try {
            App.setRoot("dashboard");
        } catch (IOException e) {
            ErrorLogger.logError("Failed to load dashboard", e.toString(), true);
        }
    }
    
    private void newTerminal(StorageContainer storageContainer) {
        terminalCounter++;
        Tab tab = new Tab();
        String tabTitle = storageContainer != null? storageContainer.getHostname() : hostname;
           
        tabTitle = Helper.summarise(tabTitle, TAB_TITLE_LEN, true);
        
        tab.setText(tabTitle);
            
        ScrollPane sp = new ScrollPane();
        Terminal terminal = new Terminal(storageContainer);
        sp.setContent(terminal);
        sp.getStyleClass().add("scrollPane");
        tab.setContent(sp);
        
        terminal.heightProperty().addListener(new ChangeListener() {
            public void changed(ObservableValue obsVal, Object oldVal, Object newVal) {
                sp.setVvalue((Double) newVal);
            }
        });
        
        tabs.getTabs().add(tab);
        tabs.getSelectionModel().select(tab);
        tab.getContent().requestFocus();
        if (tabs.getTabs().size() >= MAX_TABS) {
            newTerminalMenu.setDisable(true);
        }
        tab.setOnClosed(new EventHandler<Event>(){
            public void handle(Event e) {
                if (tabs.getTabs().size() < MAX_TABS) {
                    newTerminalMenu.setDisable(false);
                }
            }
        });
    }  
    
    @FXML
    private void keyPressed(KeyEvent keyEvent) {
        Tab selectedTab = tabs.getSelectionModel().getSelectedItem();
        
        ScrollPane sp = (ScrollPane) selectedTab.getContent();
        Terminal selectedTerminal = (Terminal) sp.getContent();
        
        if (!selectedTerminal.isFocused()) {
            selectedTerminal.requestFocus();
            Robot robot = new Robot();
            robot.keyRelease(keyEvent.getCode()); 
            robot.keyPress(keyEvent.getCode());
            robot.keyRelease(keyEvent.getCode()); 
        }
    }
}
