import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

/**
 * Класс Processor выполняет операции по работе с файлами и таблицами
 * для сохранения, открытия, редактирования и отображения данных в формате CSV.
 */
public class Processor {
    /**
     * Основное окно приложения
     */
    public JFrame window;

    /**
     * Таблица для отображения данных
     */
    public JTable table;

    /**
     * Модель таблицы для управления данными
     */
    public DefaultTableModel tableModel;

    /**
     * Текущий путь к файлу
     */
    public String currentFilePath = "";  //

    /**
     * Конструктор, принимающий основное окно приложения.
     *
     * @param frame основное окно приложения JFrame
     */
    Processor(JFrame frame) {
        window = frame;
    }

    /**
     * Открывает диалоговое окно для выбора CSV-файла и загружает данные в таблицу.
     */
    public void openFile() {
        FileDialog fd = new FileDialog(window, "Открыть", java.awt.FileDialog.LOAD);
        fd.setFile(".csv");
        fd.setDirectory(System.getProperty("user.dir"));
        fd.setVisible(true);

        currentFilePath = fd.getDirectory() + fd.getFile();
        String[][] text = readFile(currentFilePath);
        tableModel.setRowCount(0); // Очищает таблицу перед загрузкой новых данных
        tableFill(text);
    }

    /**
     * Сохраняет данные таблицы в текущий CSV-файл.
     */
    public void saveFile() {
        writeFile(currentFilePath);
    }

    /**
     * Открывает диалоговое окно для выбора нового места и имени для сохранения CSV-файла.
     */
    public void saveFileAs() {
        FileDialog fd = new FileDialog(window, "Сохранить как", java.awt.FileDialog.SAVE);
        fd.setFile("Untitled.csv");
        fd.setDirectory(System.getProperty("user.home"));
        fd.setVisible(true);
        String path = fd.getDirectory() + fd.getFile();
        if (!path.equals("nullnull")) {
            currentFilePath = path;
            writeFile(currentFilePath);
        }
    }

    /**
     * Выводит сообщение о печати файла.
     * Этот метод можно расширить для добавления функциональности печати.
     */
    public void printFile() {
        JOptionPane.showMessageDialog(window, "Печать", "Печать", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Читает содержимое CSV-файла и возвращает его как двумерный массив строк.
     *
     * @param pathToFile путь к файлу для чтения
     * @return двумерный массив строк, представляющий данные таблицы,
     * или null в случае ошибки чтения файла
     */
    public String[][] readFile(String pathToFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(pathToFile))) {
            String[] text = br.lines().toArray(String[]::new);

            String[][] data = new String[text.length][];
            for (int i = 0; i < text.length; i++) {
                String[] line = text[i].split(",", 5);
                data[i] = new String[line.length];
                System.arraycopy(line, 0, data[i], 0, line.length);
            }
            return data;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(window, "Проблема с открытием файла " + pathToFile + "\n" +
                    "Проверьте, существует ли файл", "ERROR", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
    }

    /**
     * Записывает содержимое таблицы в CSV-файл.
     *
     * @param pathToFile путь к файлу для записи
     */
    public void writeFile(String pathToFile) {
        try (BufferedWriter br = new BufferedWriter(new FileWriter(pathToFile))) {
            for (int i = 0; i < table.getRowCount(); i++) {
                for (int j = 0; j < table.getColumnCount(); j++) {
                    br.write(tableModel.getValueAt(i, j).toString());
                    br.write(",");
                }
                br.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(window, "Проблема с сохранением в файл " + pathToFile,
                    "ERROR", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Заполняет таблицу данными из двумерного массива строк.
     *
     * @param text двумерный массив строк, представляющий данные для таблицы
     */
    private void tableFill(String[][] text) {
        if (text != null) {
            for (String[] s : text) {
                tableModel.addRow(s);
            }
        }
    }

    /**
     * Создает таблицу с заданными именами столбцов.
     *
     * @param colNames массив строк с именами столбцов
     */
    public void createTable(String[] colNames) {
        tableModel = new DefaultTableModel(null, colNames);
        table = new JTable(tableModel);
    }
}
