package org.example;

import org.example.api.Dto.HanggliderDTO;
import org.example.api.Factory.HanggliderFactory;
import org.example.api.Misc.Archiver;

import java.io.IOException;
import java.util.Comparator;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var storage = HanggliderFactory.getInstance();
        Scanner scanner = new Scanner(System.in);

        boolean t1 = false;

        do {
            System.out.println("Из какого файла прочитать данные? (txt, xml, json)");
            String fileToRead = scanner.nextLine();
            fileToRead = fileToRead.toLowerCase();
            switch (fileToRead) {
                case "txt":
                    storage.readFromFile("hangglider.txt");
                    t1 = true;
                    break;

                case "xml":
                    storage.setListStorage(storage.readFromXml("hangglider.xml"));
                    t1 = true;
                    break;

                case "json":
                    storage.setListStorage(storage.readDataFromJsonFile("hangglider.json"));
                    t1 = true;
                    break;

                default:
                    System.out.println("Неправильный формат файла. Попробуйте снова.");
                    break;
            }
        } while (!t1);
        System.out.println("Список парашютов получен.");
        for (HanggliderDTO dto : storage.getList()) {
            System.out.println(dto.toString());
        }
        System.out.println();
        int id = -1;
        String name = "";
        String description = "";
        boolean t = true;
        do {
            System.out.println("Введите данные о делтаплане в формате cost,name,description:");
            try {
                String input = scanner.nextLine();
                String[] parts = input.split(",");
                id = Integer.parseInt(parts[0]);
                name = parts[1];
                description = parts[2];
                int finalId = id;
                String finalDescription = description;
                String finalName = name;
                if (storage.getList().stream().anyMatch(parachuteDTO -> parachuteDTO.getCost() == finalId) &&
                        storage.getList().stream().anyMatch(ParachuteDTO -> ParachuteDTO.getDescription().equals(finalDescription)) &&
                        storage.getList().stream().anyMatch(CategoryDto -> CategoryDto.getName().equals(finalName))
                ) {
                    System.out.println("Такой делтаплан уже получен!");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Попробуйте снова");
                t = false;
            }
        } while (t != true);
        System.out.println(storage.getList());

        var newParachute = new HanggliderDTO(id, name, description);
        storage.addToListStorage(newParachute);
        storage.addToMapStorage(id, newParachute);

        storage.writeToFile("hangglider.txt");
        storage.writeToXml("hangglider.xml", storage.getList());
        storage.writeDataToJsonFile("hangglider.json", storage.getList());

        System.out.println("Обновленный список парашютов" + storage.getList());
        boolean ans = false;

        do {
            System.out.println("Выберете поле для сортировки(cost,name,description):");
            String typeSort = scanner.nextLine();
            typeSort = typeSort.toLowerCase();

            switch (typeSort) {

                case "cost":
                    storage.getList().sort(Comparator.comparing(HanggliderDTO::getCost));
                    System.out.println("Парашюты сортированные по cost: ");
                    for (HanggliderDTO dto : storage.getList()) {
                        System.out.println(dto.toString());
                    }
                    ans = true;
                    break;

                case "name":
                    storage.getList().sort(Comparator.comparing(HanggliderDTO::getName));
                    System.out.println("Парашюты сортированные по названию: " + storage.getList());
                    ans = true;
                    break;

                case "description":
                    storage.getList().sort(Comparator.comparing(HanggliderDTO::getDescription));
                    System.out.println("Парашюты сортированные по описанию: " + storage.getList());
                    ans = true;
                    break;
                default:
                    System.out.println("Введено неверное поле");
                    break;
            }
        } while (!ans);

        String[] files = new String[]{
                "hangglider.txt",
                "hangglider.json",
                "hangglider.xml"
        };

        Archiver archiver = new Archiver();
        try {
            archiver.createZipArchive("zipResult.zip", files);
            archiver.createJarArchive("jarResult.jar", files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}