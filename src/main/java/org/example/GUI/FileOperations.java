package org.example.GUI;

import org.example.api.Dto.HanggliderDTO;
import org.example.persistence.Repositories.AbstractStorage;

import java.io.*;

public class FileOperations {

    public static void readFromFile(AbstractStorage<HanggliderDTO> storage, String filename) throws IOException {
        if (filename.endsWith(".txt")) {
            storage.readFromFile(filename);
        } else if (filename.endsWith(".xml")) {
            storage.setListStorage(storage.readFromXml(filename));
        } else if (filename.endsWith(".json")) {
            storage.setListStorage(storage.readDataFromJsonFile(filename));
        }
    }

    public static void writeToFile(AbstractStorage<HanggliderDTO> storage, String filename) {
        if (filename.endsWith(".txt")) {
            storage.writeToFile(filename);
        } else if (filename.endsWith(".xml")) {
            storage.writeToXml(filename, storage.getList());
        } else if (filename.endsWith(".json")) {
            storage.writeDataToJsonFile(filename, storage.getList());
        }
    }
}
