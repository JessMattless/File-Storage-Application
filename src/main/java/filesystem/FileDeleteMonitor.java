/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filesystem;

import database.DeleteScheduleTable;
import java.util.ArrayList;
import javafx.scene.control.Alert;
import utils.ErrorLogger;

/**
 *
 * @author michael
 */
public class FileDeleteMonitor implements Runnable {
    private static final int TIME_BETWEEN_CHECKS = 60000;
    
    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(TIME_BETWEEN_CHECKS);
                monitorDeleteSchedule(); 
            } catch (InterruptedException e) {
                ErrorLogger.logError("Delete schedule monitor thread interrupted",e.toString(), true);
            }
        }
    }
    
    
    private void monitorDeleteSchedule() {
        ArrayList<FileSystemObject> fileObjects = DeleteScheduleTable.getInstance().scheduledForDeletion();
        for (FileSystemObject fso: fileObjects) {
            fso.delete();
        }
    }
}
