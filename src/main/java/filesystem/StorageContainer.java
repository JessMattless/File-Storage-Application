/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package filesystem;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

/**
 *
 * @author michael
 */
public class StorageContainer {
    private String username;
    private String password;
    private String host;
    private JSch jsch;
    private Session session;
    private ChannelSftp channelSftp;
    private ChannelExec channelExec;
    private String currentDirectory = "/";
    
    StorageContainer(String username, String password, String host) {
        this.username = username;
        this.password = password;
        this.host = host;
    }
    
    
    public Path getPath() throws JSchException, SftpException {
        String path = "";
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
                
        channelSftp.cd(currentDirectory);
                
        path = channelSftp.pwd();
                
        channelSftp.disconnect();
        
        return Path.of(path);
    }
    
    public String[] getFileList(Path filepath) throws SftpException, JSchException {
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
                
        Vector files = channelSftp.ls(filepath.toString());
        String[] filelist = new String[files.size()];
        
        for (int i = 0; i < files.size(); i++) {
            LsEntry entry = (LsEntry) files.elementAt(i);
            filelist[i] = entry.getFilename();
        }
                
        channelSftp.disconnect();
                
        return filelist;
    }
    
    public void changeDirectory(String path) {
        currentDirectory = path;
    }
    
    
    public String executeCommand(String command) throws IOException, JSchException {
        
        channelExec = (ChannelExec) session.openChannel("exec");
                
        channelExec.setCommand("cd " + currentDirectory + " && " + command);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        channelExec.setOutputStream(outputStream);
        
        channelExec.connect();
        
        StringBuilder sb = new StringBuilder();
                
        try ( var reader = new BufferedReader(new InputStreamReader(channelExec.getInputStream())) ) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        }
        
        channelExec.disconnect();
        
        return sb.toString();
    }
    
    
    public void connect() throws JSchException {
        jsch = new JSch();
        jsch.setKnownHosts("~/.ssh/known_hosts");
        session = jsch.getSession(username, host);
        session.setPassword(password);
        
        var config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        
        session.setConfig(config);
        session.setDaemonThread(true);
        session.connect();
    }
    
    
    public void uploadFile(InputStream fileStream, String fragmentLocation) throws JSchException, SftpException {
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
        
        Path fragmentPath = Path.of(fragmentLocation);
        
        // TODO: a nicer way to check if directory exists
        try {
            channelSftp.mkdir(fragmentPath.getParent().toString());
        }
        catch(Exception e) {}
        
        channelSftp.put(fileStream, fragmentLocation );
        channelSftp.exit();
    }
    
    public byte[] downloadFile(String fragmentLocation) throws JSchException, SftpException {
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        channelSftp.get(fragmentLocation, outputStream);
        
        byte[] data = outputStream.toByteArray();
        
        channelSftp.exit();
        
        return data;
    }
    
    public void deleteFile(String fragmentLocation) throws JSchException, SftpException {
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
        
        channelSftp.rm(fragmentLocation);
      
        channelSftp.exit();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }
    
    public String getHostname() {
        return getUsername() + "@" + getHost();
    }    
}
