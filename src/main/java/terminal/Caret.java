/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package terminal;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.text.Text;

/**
 * Animated caret used in the terminal emulator
 * @author michael
 */
public class Caret extends Text {
    private boolean showCursor = true;
    private static final String CURSOR = "█";
    private static final int BLINK_RATE = 300;
    FadeTransition animation;
    
    Caret() {
        this.setText(CURSOR);
        this.getStyleClass().add("commandText");
        setId("caret");
        animation = new FadeTransition();
        animation.setFromValue(1);
        animation.setToValue(0);
        animation.setAutoReverse(true);
        animation.setDuration(Duration.millis(BLINK_RATE));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setNode(this);
        animation.play();
    }
    
    /**
     * Set the position of the caret
     * @param x
     * @param y 
     */
    public void setPosition(double x, double y) {
        this.setX(x);
        this.setY(y);
    }
}
