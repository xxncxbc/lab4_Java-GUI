package org.example.GUI;

import org.example.api.Dto.HanggliderDTO;
import org.example.api.Factory.HanggliderFactory;
import org.example.persistence.Repositories.AbstractStorage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Comparator;

public class HanggliderGUI {

    private AbstractStorage<HanggliderDTO> storage;
    private JFrame frame;
    private JTextField costField, nameField, descriptionField;
    private JTable table;
    private DefaultTableModel tableModel;

    public HanggliderGUI() {
        storage = HanggliderFactory.getInstance();
    }

    public void createAndShowGUI() {
        frame = new JFrame("Hangglider Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);


        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2));

        inputPanel.add(new JLabel("Cost:"));
        costField = new JTextField();
        inputPanel.add(costField);

        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField();
        inputPanel.add(descriptionField);

        JButton addButton = new JButton("Add Hangglider");
        addButton.addActionListener(new AddButtonListener());
        addButton.setSize(new Dimension(10,10));
        inputPanel.add(addButton);

        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        tableModel = new DefaultTableModel(new Object[] {"Cost", "Name", "Description"}, 0);

        table = new JTable(tableModel);
        table.setBackground(Color.LIGHT_GRAY);
        JScrollPane tableScrollPane = new JScrollPane(table);

        JPanel Panel = new JPanel();
        Panel.setLayout(new BoxLayout(Panel, BoxLayout.Y_AXIS));
        Panel.setAlignmentX(Component.CENTER_ALIGNMENT );

        JButton readButton = new JButton("Read ");
        readButton.addActionListener(new ReadButtonListener());
        readButton.setSize(new Dimension(300, 100));

        JButton writeButton = new JButton("Write");
        writeButton.addActionListener(new WriteButtonListener());
        writeButton.setSize(new Dimension(300, 100));

        Panel.add(readButton);
        Panel.add(writeButton);

        JButton sortButton = new JButton("Sort ");
        sortButton.addActionListener(new SortButtonListener());
        sortButton.setSize(new Dimension(300, 100));

        Panel.add(sortButton);


        frame.setLayout(new BorderLayout());
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(tableScrollPane, BorderLayout.CENTER);
        frame.add(Panel, BorderLayout.EAST);

        frame.setVisible(true);
    }

    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cost = costField.getText();
            String name = nameField.getText();
            String description = descriptionField.getText();

            if (!cost.isEmpty() && !name.isEmpty() && !description.isEmpty()) {
                try {
                    int costInt = Integer.parseInt(cost);
                    HanggliderDTO parachute = new HanggliderDTO(costInt, name, description);

                    boolean isDuplicate = storage.getList().stream()
                            .anyMatch(p -> p.getName().equalsIgnoreCase(name));

                    if (isDuplicate) {
                        JOptionPane.showMessageDialog(frame, "Parachute with the same name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        storage.addToListStorage(parachute);
                        storage.addToMapStorage(costInt, parachute);

                        tableModel.addRow(new Object[] {parachute.getCost(), parachute.getName(), parachute.getDescription()});
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid cost format", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ReadButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] options = {"hangglider.txt", "hangglider.xml", "hangglider.json"};
            String fileType = (String) JOptionPane.showInputDialog(frame,
                    "Select file to read from", "Select File",
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (fileType != null) {
                new Thread(() -> {
                    try {
                        tableModel.setRowCount(0);
                        storage.getList().clear();

                        switch (fileType) {
                            case "hangglider.txt":
                                storage.readFromFile(fileType);
                                break;
                            case "hangglider.xml":
                                storage.setListStorage(storage.readFromXml(fileType));
                                break;
                            case "hangglider.json":
                                storage.setListStorage(storage.readDataFromJsonFile(fileType));
                                break;
                            default:
                                throw new IOException("Unsupported file format");
                        }

                        updateTable();

                        JOptionPane.showMessageDialog(frame, "Data successfully loaded from " + fileType,
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Error reading file: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }).start();
            }
        }
    }

    private class WriteButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(() -> { // чтобы не мешать исполнению программы, запись проиходит независимо
                Thread txtWriter = new Thread(() -> storage.writeToFile("hangglider.txt"));
                Thread xmlWriter = new Thread(() -> storage.writeToXml("hangglider.xml", storage.getList()));
                Thread jsonWriter = new Thread(() -> storage.writeDataToJsonFile("hangglider.json", storage.getList()));

                txtWriter.start(); xmlWriter.start(); jsonWriter.start();
                try {
                    txtWriter.join(); xmlWriter.join(); jsonWriter.join();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error during file writing: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(frame, "Data written to all files", "Success", JOptionPane.INFORMATION_MESSAGE);
            }).start();
        }
    }

    private class SortButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] options = {"cost", "name", "description"};
            String field = (String) JOptionPane.showInputDialog(frame,
                    "Select sorting field", "Sort by",
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (field != null) {
                new Thread(() -> {
                    switch (field) {
                        case "cost":
                            storage.getList().sort(Comparator.comparingInt(HanggliderDTO::getCost));
                            break;
                        case "name":
                            storage.getList().sort(Comparator.comparing(HanggliderDTO::getName));
                            break;
                        case "description":
                            storage.getList().sort(Comparator.comparing(HanggliderDTO::getDescription));
                            break;
                    }
                    updateTable();
                }).start();
            }
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (HanggliderDTO parachute : storage.getList()) {
            tableModel.addRow(new Object[] {parachute.getCost(), parachute.getName(), parachute.getDescription()});
        }
    }
}
