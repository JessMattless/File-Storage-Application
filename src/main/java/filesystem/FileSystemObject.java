/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package filesystem;

/**
 *
 * @author michael
 */
public interface FileSystemObject {
    public void delete();
    public void copy(Directory to);
    public void move(Directory to);
    public int getId();
    public int getParentID();
    public String getName();
    public String getExtension();
    public void updateName(String name);
    public void moveToRecycling();
    public void restore();
    public boolean isFile();
    public boolean isReadOnly();
}
