/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dashboard;

import com.mycompany.group_9_comp20081_app.App;
import filesystem.Directory;
import database.DirTable;
import com.mycompany.group_9_comp20081_app.DashboardController;
import java.util.ArrayList;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import utils.ErrorLogger;

/**
 *
 * @author michael
 */
public class FolderTree extends TreeView {
    
    static final String FOLDER_ICON_URL = "file:target/classes/images/folderIcon.png";
    
    DashboardController userDashboard;
    
    public FolderTree(DashboardController userDashboard) {
        this.userDashboard = userDashboard;
        generateTree();
                
        this.setOnMouseClicked(new EventHandler<MouseEvent>(){ 
            public void handle(MouseEvent me) {
                selectedItem();
            }
        });
    }
    
    private void selectedItem() {
        TreeItem<Directory> treeItem = (TreeItem<Directory>) this.getSelectionModel().getSelectedItem();
        if (treeItem != null) {
            Directory dir = treeItem.getValue();
            userDashboard.changeFileViewDir(dir);
        }
    }
    
    /**
     * Create the tree items using the users directories
     */
    private void generateTree() {
        // TreeView always needs one root TreeItem
        TreeItem<Directory> rootNode = new TreeItem<>();
        
        // create the TreeItem for the home directory
        Directory homeDir = Directory.root();
        TreeItem<Directory> home = createTreeItem(homeDir);
        
        // create TreeItems for all subdirectories of the home directory
        addDirsToTree(home, homeDir);
        
        // create a TreeItem for the shared folder
        Directory shared = Directory.shared();
        TreeItem<Directory> sharedDrive = createTreeItem(shared);
        
        // create a TreeItem for the recycle bin folder
        Directory recycle = Directory.recycle();
        TreeItem<Directory> recycleDir = createTreeItem(recycle);
        
        // add TreeItems to the root Tree Item
        rootNode.getChildren().addAll(home,sharedDrive,recycleDir);
        
        // set the TreeView root to be the root TreeItem
        this.setRoot(rootNode);
        // hide the root TreeItem it is not a directory
        this.setShowRoot(false);
    }
    
    
    public void refreshTree() {
        this.getChildren().removeAll();
        generateTree();
    }
    
    
    public void addDirToTree(Directory parentDir, Directory newDir) {
        TreeItem<Directory> parentTreeItem = findTreeItemByName(this.getRoot(),parentDir.getName());
        if (parentTreeItem != null) {
            parentTreeItem.getChildren().add(createTreeItem(newDir));
        }
    }
    
    public void removeDirFromTree(Directory removedDir) {
        TreeItem<Directory> deleteTreeItem = findTreeItemByName(this.getRoot(),removedDir.getName());
        
        if (deleteTreeItem != null) {
            TreeItem<Directory> parentTreeItem = deleteTreeItem.getParent();            
            parentTreeItem.getChildren().remove(deleteTreeItem);
        }
    }
    
    
    private static TreeItem<Directory> findTreeItemByName(TreeItem<Directory> parentTreeItem, String name) {    
        if (parentTreeItem.getValue() != null && parentTreeItem.getValue().getName().equals(name)) {
            return parentTreeItem;
        }
        
        for (TreeItem<Directory> treeItem: parentTreeItem.getChildren()) {
            TreeItem<Directory> item = findTreeItemByName(treeItem,name);
            if (item != null) return item;
        }
        
        return null;
    }
    
    /**
     * Recursive function. Creates TreeItems for each subdirectory of the given directory.
     * For each subdirectory this function is called again to create TreeItems for all of its subdirectories.
     * @param parentNode
     * @param userDashboard 
     */
    private void addDirsToTree(TreeItem<Directory> parentNode, Directory parent) {
        // get a list of subdirectories from the database for this user that are in this directory
        int userId = App.getCurrentUser().getId();
        DirTable dirTable = DirTable.getInstance();
        ArrayList<Directory> directories = dirTable.getUserDirs(userId, parent);
        // for each subdirectory in list
        for (Directory subDir: directories) {
            // create a TreeItem for this subdirectory
            TreeItem leafNode = createTreeItem(subDir);
            // add the TreeItem to the parent TreeItem
            parentNode.getChildren().add(leafNode);
            // call this function for this subdirectory
            addDirsToTree(leafNode, subDir);
        }
    }
    
    /**
     * Get a TreeItem that has a folder icon and name of the directory. <br>
     * Also sets a double click action listener for opening the directory in the FileViewer
     * @param dir
     * @return TreeItem for given directory
     */
    private TreeItem<Directory> createTreeItem(Directory dir) {
        Image image = new Image(FOLDER_ICON_URL, 20, 0, true, true);
        ImageView folderIcon = new ImageView(image);
        
        TreeItem<Directory> treeItem = new TreeItem<>(dir, folderIcon);
        return treeItem;
    }   
}
