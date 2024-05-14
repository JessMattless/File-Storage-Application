/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.group_9_comp20081_app;

import javafx.scene.control.CheckBox;

/**
 *
 * @author michael
 */
public class UserModel extends User {
    private CheckBox shared = new CheckBox();
    private CheckBox readOnly = new CheckBox();
    
    public UserModel(int id, String name) {
        super(id,name);
  
    }
    
    public CheckBox getShared() {
        return shared;
    }
    
    public CheckBox getReadOnly() {
        return readOnly;
    }
    
    public boolean getIsShared() {
        return shared.isSelected();
    }
    
    public boolean getIsReadOnly() {
        return readOnly.isSelected();
    }
}
