/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package terminal;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.mycompany.group_9_comp20081_app.App;
import filesystem.StorageContainer;
import utils.Helper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import utils.ErrorLogger;

/**
 * @author michael
 */
public class Terminal extends TextFlow {
    
    private static final String HOME_DIR = "/home/";
    private ArrayList<String> cmdHistory = new ArrayList<>();
    private String currentCmd = "";
    private int cmdHistoryCrsr = -1;
    private Path workingDirectory;
    private static final String[] PERMISSIBLE_COMMANDS = {"mv","cp","ls","mkdir","ps","whoami","tree","nano","cd","touch","--cmds"};
    private int cursorPos;
    private boolean inputBlocked = true;
    private static final char CMD_PROMPT_TERMINATOR = '$';
    private int cmdPromptIndex = 0;
    SequentialTransition cursorAn;
    private Caret caret;
    private StorageContainer selectedContainer = null;
    private String containerHostname = "";
    
    /**
     * Constructor for Terminal class.
     */
    public Terminal(StorageContainer storageContainer) {
        
        selectedContainer = storageContainer;
        
        caret = new Caret();
        this.getChildren().add(caret);
        caret.setManaged(false);
        this.getStyleClass().add("terminal");
        this.setPrefWidth(550);
        this.setPrefHeight(1.0);
        this.setPadding(new Insets(5));
        
        if (selectedContainer == null) {
            workingDirectory = Path.of(System.getProperty("user.dir")); 
            try {
                containerHostname = InetAddress.getLocalHost().getHostName();
            }
            catch (Exception _e) {
                containerHostname = App.getCurrentUser().getName();
            }
        }
        else {
            containerHostname = selectedContainer.getHostname();
            try {
               workingDirectory = selectedContainer.getPath();   
            }
            catch (JSchException | SftpException e) {
                ErrorLogger.logError("Failed to initialise Terminal",e.toString(), true);
            }
        }
                     
        openCommandPrompt();
        
        this.setOnKeyPressed(new EventHandler<KeyEvent>() {
            // EventHandler is abstract so concrete handle method needs to be implemented
            @Override
            public void handle(KeyEvent e) {
                if (getInputBlocked()) {
                    // do nothing
                }
                else if (e.getCode().equals(KeyCode.ENTER)) {
                    processCommand();
                }
//                else if (e.getText().matches("[a-zA-Z0-9 ./\"~-]")) {
//                    append(e.getText());
//                }
                else if (e.getCode().equals(KeyCode.BACK_SPACE)) {
                    backspace();
                }
                else if (e.getCode().equals(KeyCode.UP)) {
                    getCmdHistory(true);
                }
                else if (e.getCode().equals(KeyCode.DOWN)) {
                    getCmdHistory(false);
                }
                else if (e.getCode().equals(KeyCode.LEFT)) {
                    moveCursor(-1);
                }
                else if (e.getCode().equals(KeyCode.RIGHT)) {
                    moveCursor(1);
                }
                else if (e.getCode().equals(KeyCode.TAB)) {
                    e.consume();
                    autoComplete();
                }
                else if (e.getCode().equals(KeyCode.SHIFT)) {
                    e.consume();
                }
                else {
                    append(e.getText());
                }
            }
        });     
    }    
    
    public boolean getInputBlocked() {
        return inputBlocked;
    }
    
    private void autoComplete() {
        int cursorFromEnd = this.getChildren().size() - cursorPos;
        String command = getCommand();
        int cursorPos = command.length() - cursorFromEnd;
        int lastSpace = command.lastIndexOf(' ', cursorPos -1);
        String userInput = "";
        if (lastSpace+1 <= cursorPos) {
            userInput = command.substring(lastSpace+1,cursorPos);
        }
        
        // if there are any slashes then everything before the last slash will be part of the search path
        int lastSlash = userInput.lastIndexOf('/');
        String userEnteredPath = "";
        String searchQuery = Helper.stripLeadingChars(userInput, '/');
        if (lastSlash > 0) {
            userEnteredPath = userInput.substring(0, lastSlash);
            // everything after the last slash 
            searchQuery = userInput.substring(lastSlash+1);
        }
        // the first part of the search path is the current working directory
        Path absoluteSearchPath = workingDirectory;
        // if the entered path started with .. then move the the parent of this directory
        if (userEnteredPath.startsWith("..")) {
            absoluteSearchPath = absoluteSearchPath.getParent();
        }
        // remove characters from the start of the user entered path
        Character[] removeChars = {'.','/'};
        String cleanUserEnteredPath = Helper.stripLeadingChars(userEnteredPath, removeChars);
                
        // build the path using the current working directory and the user entered path
        absoluteSearchPath = absoluteSearchPath.resolve(cleanUserEnteredPath);
        
        String[] fileList = {};
        // get a list of everything in this directory
        if (selectedContainer == null) {
            fileList = absoluteSearchPath.toFile().list(); 
        }
        else {
            try {
                fileList = selectedContainer.getFileList(absoluteSearchPath);  
            }
            catch (SftpException | JSchException e) {
                this.printLine("Auto complete remote file error: " + e.toString());
            }
        }
        
        ArrayList<String> matches = new ArrayList<>();
     
        if (fileList != null) {
            for (String file : fileList) {
                if (file.startsWith(searchQuery)) {
                    matches.add(file);
                }
            }
            // if there is one match then auto complete using its name else do nothing
            if (matches.size() == 1) {
                String autoComplete = userEnteredPath + (userEnteredPath.isBlank()? "" : "/") + matches.get(0);
                if (!absoluteSearchPath.resolve(matches.get(0)).toFile().isFile()) autoComplete = autoComplete.concat("/");
                clearWord();
                appendWord(autoComplete);
            }
        }
    }
    
    /**
     * Scrolls through the recent command history. Controlled by up and dow arrows
     * @param scrollup boolean to control the direction 
     */
    private void getCmdHistory(boolean scrollup) {
        int cmdHistoryLen = cmdHistory.size();
        if (cmdHistoryCrsr == -1 && scrollup) {
            currentCmd = getCommand();
        }
        if (cmdHistoryCrsr < cmdHistoryLen -1 && scrollup) {
            cmdHistoryCrsr++;
        }
        else if (cmdHistoryCrsr > -1 && !scrollup) {
            cmdHistoryCrsr--;
        }
        clearCommand();
        String newCommand = "";
        if (cmdHistoryCrsr == -1) {
            newCommand = currentCmd;
        }
        else {
            newCommand = cmdHistory.get(cmdHistoryCrsr);
        }
        appendWord(newCommand);
    }
    
    /**
     * Clear everything after the command terminator
     */
    private void clearCommand() {
        Node prevNode = this.getChildren().get(this.getChildren().size()-1);
        while (prevNode.idProperty().getValue() != "terminator") {
            this.getChildren().remove(prevNode);
            prevNode = this.getChildren().get(this.getChildren().size()-1);
        }
        setCursor(cmdPromptIndex +1);
    }
    
    /**
     * Clear everything after the command terminator
     */
    private void clearWord() {
        try {
            Node prevNode = this.getChildren().get(cursorPos-1);
            while ((!((Text) prevNode).getText().equals(" ")) && prevNode.idProperty().getValue() != "terminator") {
                this.getChildren().remove(prevNode);
                prevNode = this.getChildren().get(cursorPos-2);
                moveCursor(-1);
            }
        } catch (IndexOutOfBoundsException e) {
            this.printLine(e.toString());
        }
    }
    
    /**
     * Get everything after the command terminator
     * @return command as string
     */
    private String getCommand() {
        String commandLine = this.getText();
        int commandStart = commandLine.lastIndexOf(CMD_PROMPT_TERMINATOR) + 2;
        return commandLine.substring(commandStart);
    }
    
    
    private String getText() {
        StringBuilder sb = new StringBuilder();
        for (Node node : this.getChildren()) {
            sb.append(((Text) node).getText());
        }
        return sb.toString();
    }
    
    
    private void appendWord(String word) {
        for (char c: word.toCharArray()) {
            append(""+c);
        }
    }
//    
//    /**
//     * Append a string to the textarea from the current cursor position
//     * @param string
//     */
    private void append(String string) {
        try {
            Text text = new Text(string);
            text.getStyleClass().add("commandText");
            this.getChildren().add(cursorPos, text);
            moveCursor(1);
        } catch(IndexOutOfBoundsException | IllegalArgumentException e) {
            this.printLine("Error appending text to command line - " + e.toString());
        }
    }
    
    /**
     * Prints a line to the end of the terminal
     * @param line 
     */
    private void printLine(String line) {
        Text text = new Text(line + "\n");
        text.getStyleClass().add("terminalResponse");
        this.getChildren().add(text);
    }
    
    
    private void moveCursor(int increment) {
        cursorPos += increment;
        if (cursorPos < cmdPromptIndex +1) {
            cursorPos = cmdPromptIndex +1;
        } 
        else if (cursorPos > this.getChildren().size()) {
            cursorPos = this.getChildren().size();
        }
        waitToPositionCaret(50);
    }

    public void setCursor(int position) {
        cursorPos = position;
        waitToPositionCaret(200);
    }
    
    /**
     * Wait for nodes to be rendered before positioning the caret
     */
    private void waitToPositionCaret(int wait){
        Thread waitToPosition = new Thread() {
            public void run() {
                try {
                    sleep(wait);
                    positionCaret();
                } catch (InterruptedException e) {
                    printLine(e.toString());
                }
            }
        };
        waitToPosition.start();
    }
    
    /**
     * @brief Sets the position of the caret animation based on the current caret position in the text
     */
    public void positionCaret() {
        double x,y;
        Node adjacentNode = this.getChildren().get(cursorPos-1);
        double caretWidth = caret.getBoundsInParent().getWidth();
        if (cursorPos == cmdPromptIndex+1) {
            x = adjacentNode.getLayoutX() + caretWidth*2;
            y = adjacentNode.getLayoutY() + 12;
        }
        else {
            x = adjacentNode.getLayoutX() + caretWidth;
            y = adjacentNode.getLayoutY() + 12;   
        }
        caret.setPosition(x, y);
    }
    
    /**
     * Remove the character before the current cursor position
     */
    private void backspace() {
        Node prevNode = this.getChildren().get(cursorPos -1);
        if (prevNode.idProperty().getValue() != "terminator") {
            this.getChildren().remove(prevNode);
            moveCursor(-1);
        }
    }
    
    /**
     * Print the command terminator to the terminal
     */
    private void openCommandPrompt() {
        String currentDir = workingDirectory.toString();
        currentDir = currentDir.replaceFirst("home", "~");
        Text cmdPromptHostname = new Text(containerHostname + ":");
        Text cmdPromptFilepath = new Text(currentDir);
        cmdPromptHostname.getStyleClass().add("promptHostname");
        cmdPromptFilepath.getStyleClass().add("promptFilepath");
        Text terminator = new Text(CMD_PROMPT_TERMINATOR + " ");
        terminator.setId("terminator");
        terminator.getStyleClass().add("promptTerminator");
        this.getChildren().addAll(cmdPromptHostname,cmdPromptFilepath, terminator);
        
        cmdPromptIndex = this.getChildren().indexOf(terminator);
        setCursor(cmdPromptIndex+1);
        // allow input to the terminal
        inputBlocked = false;
       
    }

    /**
     * Takes the contents of the terminal and extracts the command. <br>
     * Command is then executed.
     */
    private void processCommand() {
        // block input into the terminal
        inputBlocked = true;

        String command = getCommand();
        
        this.append("\n");
        
        command = command.strip();
        // add this command to the history
        cmdHistory.add(0,command);
        
        command = command.replaceAll("\\r|\\n|\\t", "");
        
        routeCommand(command);
        
        openCommandPrompt();
    }
    
    /**
     * @brief Forwards the entered command to the appropriate handler
     * Not all commands can be executed by the emulator in a standard way, for example launching nano and remote commands
     * @param command 
     */
    private void routeCommand(String command) {
        // split the command to be used with process builder
        String [] splitCommand = command.split(" ");
        String commandName = splitCommand[0];
        
        // block any commands that are not available to this emulator
        if (!Helper.inArray(commandName, PERMISSIBLE_COMMANDS)) {
            this.printLine(String.format(
                    "\'%s\' is not available.\n" +
                    "This terminal emulator is running a reduced set of commands.\n" + 
                    "For a list of available commands enter '--cmds'.", commandName));
        }
        else {
            switch (commandName) {
            case "cd":
                if (selectedContainer == null) {
                    changeDirectory(splitCommand[splitCommand.length - 1]);
                }
                else {
                    remoteChangeDirectory(splitCommand[splitCommand.length - 1]);
                }
                break;
            case "--cmds":
                this.printLine("Commands available to this emulator:\n" +
                        "mv\ncp\nls\nmkdir\nps\nwhoami\ntree\nnano\ncd"
                        );
                break;
            case "nano":
                try {
                    if (selectedContainer == null) {
                        nano(splitCommand);
                    }
                    else {
                        nano(remoteNano(splitCommand), true);
                    }
                    
                } catch (IOException e) {
                    this.printLine(e.toString());
                }
                break;
            default:
                try {
                    if (selectedContainer == null) {
                        execCommand(splitCommand);
                    }
                    else {
                        execCommand(remoteCommand(command), true);
                    }
                } catch (IOException | InterruptedException ex) {
                    this.printLine(ex.getMessage());
                }  
            }
        }
    }
    
    /**
     * @biref Converts the directory entered by the user into a path
     * @param directory
     * @return Path
     */
    private Path parseCdCommand(String directory) {
        // create a local copy of wd
        Path path = workingDirectory;
        
        // if the command is cd or cd ~ then set path to home directory
        if (directory.equals("cd") || directory.equals("~")) {
            path = Path.of(HOME_DIR);
        }
        else {
            // if .. then set path to its parent
            if (directory.startsWith("..") && path.getParent() != null) {
                path = path.getParent();
            }
            // remove leading . and / from the directory argument
            Character[] removeChars = {'.','/'};
            directory = Helper.stripLeadingChars(directory,removeChars);
            // add the directory argument to the path
            path = path.resolve(Path.of(directory));
        }
        
        return path;
    }

    
    /**
     * Change the current directory for this terminal
     * @param directory 
     */
    private void changeDirectory(String directory) {
        Path path = parseCdCommand(directory);
        
        // if path is valid set out working directory to it
        if (path.toFile().isDirectory()) {
            workingDirectory = path;
        }
    }
    
    
    /**
     * @brief Wraps the command entered in a shell command to run on the remote machine
     * @param command
     * @return Remote command
     * @throws IOException
     * @throws JSchException
     * @throws InterruptedException 
     */
    private String[] remoteCommand(String command) {
        String shellCommand = "cd " + workingDirectory + "; " + command;
        
        String[] remoteCmd = new String[3];
        remoteCmd[0] = "ssh";
        remoteCmd[1] = containerHostname;
        remoteCmd[2] = shellCommand;
        
        return remoteCmd;
    }
    
    /**
     * @brief Changes the current directory of the remote shell
     * @param directory 
     */
    private void remoteChangeDirectory(String directory){
        Path path = parseCdCommand(directory);
        
        String currentPath = workingDirectory.toString();
        
        // try to change the dir and if exception occurs try to change it back
        try {
            selectedContainer.changeDirectory(path.toString());
            workingDirectory = selectedContainer.getPath();
        }
        catch (Exception _e) {
            try {
                selectedContainer.changeDirectory(currentPath);
                workingDirectory = selectedContainer.getPath();
            }
            catch (JSchException | SftpException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(e.toString());
                alert.initModality(Modality.APPLICATION_MODAL);
                Platform.runLater(() -> alert.show());
            }
        }
    }
    
    /**
     * @brief Wraps the nano command in ssh command so it can be launched with remote file
     * @param command
     * @return Remote nano ssh command
     */
    private String[] remoteNano(String[] command){
        
        String[] remoteCmd = new String[4];
        remoteCmd[0] = "ssh";
        remoteCmd[1] = "-t";
        remoteCmd[2] = containerHostname;
        
        String filepath = Path.of(workingDirectory.toString(), command[command.length -1]).toString();
        command[command.length -1] = filepath;
        
        String cmds = "";
        
        for (String cmd: command) {
            cmds += cmd + " ";
        }
        
        remoteCmd[3] = cmds;
        
        return remoteCmd;
    }
    
    /**
     * @brief Executes command that has been entered by the user
     * @param command
     * @throws IOException
     * @throws InterruptedException 
     */
    private void execCommand(String[] command) throws IOException, InterruptedException {
        execCommand(command, false);
    }
    
    /**
     * @brief Executes command that has been entered by the user
     * @param command
     * @param isRemote
     * @throws IOException
     * @throws InterruptedException 
     */
    private void execCommand(String[] command, boolean isRemote) throws IOException, InterruptedException {
        var processBuilder = new ProcessBuilder();
        if (!isRemote) {
            processBuilder.directory(workingDirectory.toFile());
        }
        processBuilder.command(command);
        processBuilder.redirectErrorStream(true);
        var process = processBuilder.start();
        process.waitFor();     
        try ( var reader = new BufferedReader(new InputStreamReader(process.getInputStream())) ) {
            String line;
            while ((line = reader.readLine()) != null) {
                this.printLine(line);
            }
        }
    }
    
    /**
     * @brief Launch nano text editor
     * @param command
     * @throws IOException 
     */
    private void nano(String[] command) throws IOException {
        nano(command, false);
    }
    
    /**
     * @brief Launch nano text editor
     * @param command
     * @param isRemote
     * @throws IOException 
     */
    private void nano(String[] command, boolean isRemote) throws IOException {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("terminator");
        cmds.add("-x");
        for (String cmd : command) {
            cmds.add(cmd);
        }
        var processBuilder = new ProcessBuilder();
        if (!isRemote) {
           processBuilder.directory(workingDirectory.toFile()); 
        }
        processBuilder.command(cmds);
        Process process = processBuilder.start();
    }
}

