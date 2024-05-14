/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  michael
 * Created: 29 Jan 2023
 */

CREATE TABLE IF NOT EXISTS User (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    password TEXT,
    salt TEXT,
    publicKey BLOB,
    last_login TEXT,
    admin INTEGER,
    loggedIn INTEGER
);

CREATE TABLE IF NOT EXISTS Directory (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    owner INTEGER NOT NULL,
    directoryID INTEGER,
    FOREIGN KEY(owner) REFERENCES User(id),
    FOREIGN KEY(directoryID) REFERENCES Directory(id)
);

CREATE TABLE IF NOT EXISTS StoredFile (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner INTEGER NOT NULL,
    directoryID INTEGER,
    name TEXT,
    location TEXT,
    readOnly INT DEFAULT(0),
    aesKey BLOB,
    FOREIGN KEY(owner) REFERENCES User(id),
    FOREIGN KEY(directoryID) REFERENCES Directory(id)
);

CREATE TABLE IF NOT EXISTS FileFragment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fileId INTEGER NOT NULL,
    frag_index INTEGER NOT NULL,
    location TEXT,
    container INTEGER NOT NULL,
    FOREIGN KEY(fileId) REFERENCES StoredFile(id) ON DELETE CASCADE 
);

CREATE TABLE IF NOT EXISTS DeleteSchedule (
    storedItemType TEXT NOT NULL,
    itemID INT NOT NULL,
    deleteWhen TEXT NOT NULL,
    restoreLocation INT,
    PRIMARY KEY(storedItemType, itemID)
);

CREATE TABLE IF NOT EXISTS SharedFile (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner INT NOT NULL,
    directoryID INTEGER,
    fileID INT NOT NULL,
    aesKey BLOB,
    writePrivilege INT NOT NULL
);