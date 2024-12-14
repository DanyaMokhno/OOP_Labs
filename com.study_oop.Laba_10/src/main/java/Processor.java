import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

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
    public String currentFilePath = "";

    private static final Logger log = Logger.getLogger(Processor.class);

    /**
     * Конструктор, принимающий основное окно приложения.
     *
     * @param frame основное окно приложения JFrame
     */
    Processor(JFrame frame) {
        PropertyConfigurator.configure("src/main/resources/log4j.properties"); // Укажите путь к вашему файлу конфигурации
        window = frame;
    }

    /**
     * Открывает диалоговое окно для выбора CSV-файла и загружает данные в таблицу.
     */
    public void openFile() {
        log.info("Открытие файла: " + currentFilePath);
        FileDialog fd = new FileDialog(window, "Открыть", java.awt.FileDialog.LOAD);
        fd.setFile(".xml");
        fd.setDirectory(System.getProperty("user.dir"));
        fd.setVisible(true);
        currentFilePath = fd.getDirectory() + fd.getFile();
        if (currentFilePath.endsWith(".xml")) {
            Document data = Parser.parse(currentFilePath);
            tableModel.setRowCount(0); // Очищает таблицу перед загрузкой новых данных
            tableFill(data);
        } else {
            String[][] text = readFile(currentFilePath);
            tableModel.setRowCount(0); // Очищает таблицу перед загрузкой новых данных
            tableFill(text);
        }
        log.debug("Данные успешно загружены из файла: " + currentFilePath);
    }

    /**
     * Сохраняет данные таблицы в текущий CSV/XML-файл.
     */
    public void saveFile() {
        if (currentFilePath.isEmpty()) {
            log.warn("Попытка сохранить файл без указания пути.\nСохраняем в новый файл");
            saveFileAs();
        }
        else if (currentFilePath.endsWith(".xml")) {
            log.info("Сохранение данных формата xml в файл: " + currentFilePath);
            writeFile(Parser.to_xml(tableModel));
        }
        else {
            log.info("Сохранение данных формата csv в файл: " + currentFilePath);
            writeFile(currentFilePath);
        }
    }

    /**
     * Открывает диалоговое окно для выбора нового места и имени для сохранения CSV-файла.
     */
    public void saveFileAs() {
        FileDialog fd = new FileDialog(window, "Сохранить как", java.awt.FileDialog.SAVE);
        fd.setFile("Untitled.xml");
        fd.setDirectory(System.getProperty("user.home"));
        fd.setVisible(true);
        String path = fd.getDirectory() + fd.getFile();
        if (!path.equals("nullnull")) {
            currentFilePath = path;
            if (path.endsWith(".xml")) writeFile(Parser.to_xml(tableModel));
            else writeFile(currentFilePath);
        }
    }

    /**
     * Выводит файл на печать.
     */
    public void printFile() {
        try {
            JasperPrint print = getReport(Parser.to_xml(tableModel));
            if (print != null)
                JasperPrintManager.printReport(print, true);
            else
                JOptionPane.showMessageDialog(window, "Ошибка при формировании отчёта", "ERROR", JOptionPane.INFORMATION_MESSAGE);
        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    /**
     * Экспортирует файлы.
     *
     * @param format Формат файла "html" или "pdf"
     */
    public void exportFile(String format) {
        log.info("Экспорт данных в формат: " + format);
        // Этап 1: создание документа XML в отдельном потоке
        Thread loadDataThread = new Thread(() -> {
            try {
                Document doc = Parser.to_xml(tableModel);
                    // Этап 2: подготовка отчёта в другом потоке
                    Thread generateReportThread = new Thread(() -> {
                        JasperPrint print = getReport(doc);
                        if (print != null) {
                            // Этап 3: экспорт отчёта в файл в третьем потоке
                            Thread exportFileThread = new Thread(() -> {
                                try {
                                    FileDialog fd = new FileDialog(window, "", FileDialog.SAVE);
                                    fd.setDirectory(System.getProperty("user.dir"));
                                    fd.setFile("Untitled." + format);
                                    fd.setVisible(true);
                                    String output_file_path = fd.getDirectory() + fd.getFile();
                                    if (output_file_path.endsWith("pdf")) {
                                        File output_file = new File(output_file_path);
                                        JasperExportManager.exportReportToPdfFile(print, output_file.getAbsolutePath());
                                    } else if (output_file_path.endsWith("html")) {
                                        File output_file = new File(output_file_path);
                                        JasperExportManager.exportReportToHtmlFile(print, output_file.getAbsolutePath());
                                    }
                                    log.debug("Отчёт успешно создан для экспорта в формат " + format);
                                } catch (JRException e) {
                                    log.error("Ошибка при создании отчёта для экспорта", e);
                                }
                            });
                            exportFileThread.start(); // Запуск потока экспорта
                        } else JOptionPane.showMessageDialog(window, "Ошибка при формировании отчёта", "ERROR", JOptionPane.INFORMATION_MESSAGE);
                    });
                    generateReportThread.start(); // Запуск потока генерации отчёта
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        loadDataThread.start(); // Запуск потока загрузки данных
    }

    /**
     * Конвертирует таблицу в объект для печати или экспорта
     *
     * @param doc объект Document содержащий xml таблицу
     * @return объект JasperPrint для экспорта или печати или null в случае ошибки
     */
    private JasperPrint getReport(Document doc) {
        try {
            String template = "MyReports/data.jrxml";
            JasperReport jasperReport = JasperCompileManager.compileReport(template);
            Map params = new HashMap();
            params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, doc);
            return JasperFillManager.fillReport(jasperReport, params);
        } catch (JRException e) {
            e.printStackTrace();
        }
        return null;
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
     * Записывает содержимое таблицы в XML-файл.
     *
     * @param data объект xml документа
     */
    private void writeFile(Document data) {
        try {
            // Создание преобразователя документа
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            // Создание файла с именем books.xml для записи документа
            java.io.FileWriter fw = new FileWriter(currentFilePath);
            // Запись документа в файл
            trans.transform(new DOMSource(data), new StreamResult(fw));
        } catch (TransformerException | IOException e) {
            JOptionPane.showMessageDialog(window, "Проблема с сохранением в файл " + currentFilePath,
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
     * Заполняет таблицу данными из двумерного массива строк.
     *
     * @param data объект xml документа
     */
    private void tableFill(Document data) {
        if (data != null) {
            NodeList list = data.getElementsByTagName("record");
            // Цикл просмотра списка элементов и запись данных в таблицу
            for (int i = 0; i < list.getLength(); i++) {
                // Выбор очередного элемента списка
                Node elem = list.item(i);
                // Получение списка атрибутов элемента
                NamedNodeMap attrs = elem.getAttributes();
                // Чтение атрибутов элемента
                String client = attrs.getNamedItem("client").getNodeValue();
                String doctor = attrs.getNamedItem("doctor").getNodeValue();
                String date = attrs.getNamedItem("date").getNodeValue();
                String time = attrs.getNamedItem("time").getNodeValue();
                String symptoms = attrs.getNamedItem("symptoms").getNodeValue();
                // Запись данных в таблицу
                tableModel.addRow(new String[]{client, doctor, date, time, symptoms});
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

    /**
     * Класс Parser предоставляет методы для работы с XML-файлами.
     * Содержит статические методы для чтения данных из XML-файла в объект `Document`
     * и для преобразования данных таблицы в XML-формат.
     */
    static class Parser {
        /**
         * Парсит XML-файл и возвращает объект `Document`.
         *
         * @param pathToFile путь к XML-файлу для парсинга
         * @return объект `Document`, представляющий содержимое XML-файла,
         * или null в случае ошибки чтения или парсинга
         */
        public static Document parse(String pathToFile) {
            try {
                // Создание парсера документа
                DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                // Чтение документа из файла
                Document doc = dBuilder.parse(new File(pathToFile));
                // Нормализация документа
                doc.getDocumentElement().normalize();
                return doc;
            } catch (ParserConfigurationException | IOException | SAXException e) {
                JOptionPane.showMessageDialog(null, "Проблема с чтением файла " + pathToFile,
                        "ERROR", JOptionPane.INFORMATION_MESSAGE);
            }
            // Обработка ошибки парсера при чтении данных из XML-файла
            return null;
        }

        /**
         * Преобразует данные из таблицы в XML-формат и возвращает объект `Document`.
         *
         * @param tableModel модель таблицы `DefaultTableModel`, содержащая данные для преобразования
         * @return объект `Document`, представляющий данные в XML-формате
         * @throws RuntimeException если возникает ошибка конфигурации парсера
         */
        public static Document to_xml(DefaultTableModel tableModel) {
            try {
                // Создание парсера документа
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                // Создание пустого документа
                Document doc = builder.newDocument();

                // Создание корневого элемента booklist и добавление его в документ
                Node data = doc.createElement("base");
                doc.appendChild(data);

                // Создание дочерних элементов book и присвоение значений атрибутам
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Element book = doc.createElement("record");
                    data.appendChild(book);
                    book.setAttribute("client", (String) tableModel.getValueAt(i, 0));
                    book.setAttribute("doctor", (String) tableModel.getValueAt(i, 1));
                    book.setAttribute("date", (String) tableModel.getValueAt(i, 2));
                    book.setAttribute("time", (String) tableModel.getValueAt(i, 3));
                    book.setAttribute("symptoms", (String) tableModel.getValueAt(i, 4));
                }
                return doc;
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

