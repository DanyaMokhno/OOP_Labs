/**
 * Лабораторная работа №5
 *
 * @author Даниил Мохно 3312
 * @version 1.0
 */

import javax.swing.*;
import java.awt.*;
import java.awt.Taskbar;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Основной пользовательский интерфейс приложения, включает элементы окна,
 * панелей, кнопок, меню и таблицы для отображения и взаимодействия с данными.
 */
public class UserInterface {
    /** Главное окно приложения. */
    public JFrame window;

    /** Иконка приложения для отображения в заголовке окна. */
    private Image icon;

    /** Главная панель для размещения всех компонентов. */
    private JPanel panel;

    /** Верхняя панель интерфейса, используется для меню и поиска. */
    private JPanel header;

    /** Панель для размещения кнопки сохранения. */
    private JPanel saveArea;

    /** Панель для размещения поля и кнопки поиска. */
    private JPanel searchArea;

    /** Кнопка сохранения файла. */
    public JButton saveButton;

    /** Строка меню, содержащая основные действия приложения. */
    public JMenuBar menuBar;

    /** Поле поиска для ввода текста поиска. */
    public JTextField searchField;

    /** Кнопка для выполнения поиска по введённому тексту. */
    public JButton searchButton;

    /** Текст-заполнитель для поля поиска. */
    public String placeholder;

    /** Нижняя панель интерфейса, используется для кнопок добавления и справки. */
    private JPanel footer;

    /** Кнопка для отображения справочной информации. */
    public JButton referenceButton;

    /** Кнопка для добавления новой строки в таблицу. */
    public JButton add;

    /** Кнопка для удаления выбранной строки из таблицы. */
    public JButton del;

    /** Панель для размещения кнопок добавления и удаления. */
    private JPanel editPanel;

    /** Модель таблицы, управляющая данными, отображаемыми в таблице. */
    public DefaultTableModel tableModel;

    /** Таблица для отображения данных. */
    public JTable table;

    /** Панель прокрутки для таблицы, позволяет прокручивать данные при большом объёме. */
    public JScrollPane scrollPane;

    /** Обработчик для работы с файлами и таблицей. */
    public Processor processor;

    /**
     * Метод для отображения окна приложения
     */
    public void show() {
        if (System.getProperty("os.name").toLowerCase().contains("mac"))
            System.setProperty("apple.awt.application.name", "Поликлиника");

        // Создание основного окна
        window = new JFrame("Поликлиника");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800, 400);
        window.setLocationRelativeTo(null);

        // Добавление обработчика
        processor = new Processor(window);

        // Добавление иконки
        icon = window.getToolkit().getImage(getClass().getResource("/img/icon.png"));
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            Taskbar.getTaskbar().setIconImage(icon);
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        } else {
            window.setIconImage(icon);
        }

        // Устанавливаем панель
        panel = new JPanel();
        panel.setBackground(new Color(216, 240, 211));
        panel.setLayout(new BorderLayout());

        // Создание меню
        menuBar = new JMenuBar();
        String[] menus = {"File", "Edit", "View", "Help"};
        String[][] menuItems = {{"Open file", "Save", "Save as", "Print"}, {"Add", "Delete"}};
        Runnable[][] events = {{
                processor::openFile,
                processor::saveFile,
                processor::saveFileAs,
                processor::printFile,
        }, {this::addRow, this::delRow}};
        for (int i = 0; i < menus.length; i++) {
            JMenu menu = new JMenu(menus[i]);
            for (int j = 0; menuItems.length > i && j < menuItems[i].length; j++) {
                JMenuItem item = new JMenuItem(menuItems[i][j]);
                final int menuIndex = i;
                final int itemIndex = j;
                item.addActionListener(e -> events[menuIndex][itemIndex].run());
                menu.add(item);
            }
            menuBar.add(menu);
        }
        // Добавление меню экспорта
        JMenu export = new JMenu("Export");
        JMenuItem html = new JMenuItem("HTML");
        html.addActionListener(e -> processor.exportFile("html"));
        JMenuItem pdf = new JMenuItem("PDF");
        pdf.addActionListener(e -> processor.exportFile("pdf"));
        export.add(html);
        export.add(pdf);
        menuBar.getMenu(0).add(export);

        // Cоздаём шапку
        header = new JPanel();
        header.setBackground(new Color(211, 240, 228));
        header.setLayout(new BorderLayout());

        // Кнопка сохранения
        saveArea = new JPanel();
        saveArea.setBackground(new Color(211, 240, 228));
        saveButton = new JButton("Сохранить");
        saveArea.add(saveButton);
        header.add(saveArea, BorderLayout.WEST);


        // Добавление меню
        window.setJMenuBar(menuBar);

        // Поле поиска
        searchField = getSearchField();

        // Кнопка поиска
        searchButton = new JButton("Искать");
        searchButton.setPreferredSize(new Dimension(100, 30));

        // Добавляем поиск в шапку
        searchArea = new JPanel();
        searchArea.add(searchButton, BorderLayout.EAST);
        searchArea.setBackground(new Color(211, 240, 228));
        searchArea.add(searchField, BorderLayout.EAST);
        header.add(searchArea, BorderLayout.EAST);

        // Добавляем шапку на панель
        panel.add(header, BorderLayout.NORTH);

        // создаём футер
        footer = new JPanel();
        footer.setBackground(new Color(211, 240, 228));
        footer.setLayout(new BorderLayout());

        // Кнопка показа справки
        referenceButton = new JButton("Справка");
        referenceButton.setPreferredSize(new Dimension(100, 30));
        footer.add(referenceButton, BorderLayout.WEST);

        // Кнопка добавления строки в таблицу
        add = new JButton("+");
        del = new JButton("-");
        editPanel = new JPanel();
        editPanel.setBackground(new Color(211, 240, 228));
        editPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        editPanel.add(del);
        editPanel.add(add);

        footer.add(editPanel, BorderLayout.EAST);
        panel.add(footer, BorderLayout.SOUTH);


        // Создаём табличку
        String[] columnNames = {"ФИО ПАЦИЕНТА", "ФИО ВРАЧА", "ДАТА ЗАПИСИ", "ВРЕМЯ ЗАПИСИ", "ЖАЛОБЫ"};
        processor.createTable(columnNames);
        table = processor.table;
        tableModel = processor.tableModel;

        // Оформляем таблицу
        table.getTableHeader().setBackground(new Color(100, 180, 100)); // зеленый заголовок
        table.getTableHeader().setForeground(Color.WHITE);
        table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false); // Запрет менять местами
        table.setGridColor(new Color(19, 54, 11));
        table.setSelectionBackground(new Color(216, 240, 211));
        table.setSelectionForeground(new Color(4, 17, 3));

        // Добавляем скролл
        scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Добавление панели в основное окно
        window.add(panel);

        add_filters();

        // Отображение окна
        window.setVisible(true);
    }

    /**
     * Создаёт поле поиска с автоматически убирающимся текстом внутри
     *
     * @return поле поиска
     */
    private JTextField getSearchField() {
        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(100, 30));

        // Устанавливаем начальный текст как плэйсхолдер
        placeholder = "🔍Поиск";
        searchField.setText(placeholder);
        searchField.setForeground(Color.GRAY);

        // Добавляем FocusListener для обработки placeholder
        searchField.addFocusListener(new FocusAdapter() {
            /**
             * Убирает плэйсхолдер при фокусировании на поле
             * @param e событие для обработки
             */
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK); // Обычный цвет текста
                }
            }

            /**
             * Добавляет плэйсхолдер при расфокусировании из поля
             * @param e событие для обработки
             */
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(placeholder);
                    searchField.setForeground(Color.GRAY); // Цвет placeholder
                }
            }
        });
        return searchField;
    }

    /**
     * Добавляет обработчики событий к элементам управления.
     */
    private void add_filters() {
        JComponent[] fields = {add, del, searchButton, saveButton, referenceButton};
        Runnable[] func = {this::addRow, this::delRow, this::search, processor::saveFile, this::show_ref};

        for (int i = 0; i < fields.length; i++) {
            final int index = i;
            // Добавляем слушатель
            ((AbstractButton) fields[i]).addActionListener(new ActionListener() {
                /** Добавляем действие */
                @Override
                public void actionPerformed(ActionEvent e) {
                    func[index].run();
                }
            });
        }
    }

    /**
     * Добавление строки в таблицу
     */
    private void addRow() {
        tableModel.addRow(new Object[]{"", "", "", "", ""});
    }

    /**
     * Удаление строки из таблицы
     */
    private void delRow() {
        if (table.getSelectedRow() != -1)
            for (int i : getSelectedRows())
                tableModel.removeRow(i);
    }

    /**
     * Получает выделенные строки таблицы и возвращает их в перевёрнутом порядке.
     *
     * @return перевёрнутый массив выделенных строк таблицы
     */
    private int[] getSelectedRows() {
        int[] s_rows = table.getSelectedRows();
        for (int i = 0; i < s_rows.length / 2; i++) {
            int temp = s_rows[i];
            s_rows[i] = s_rows[s_rows.length - 1 - i];
            s_rows[s_rows.length - 1 - i] = temp;
        }
        return s_rows;
    }

    /**
     * Выполняет поиск по тексту, введенному в поле поиска.
     */
    private void search() {
        try{
            NotSearched.checkString(searchField.getText(), placeholder);
            JOptionPane.showMessageDialog(window, "Ищем: " + searchField.getText());
        }catch (NotSearched e){
            JOptionPane.showMessageDialog(window, e.getMessage());
        }
    }

    /**
     * Отображает справочную информацию.
     */
    private void show_ref() {
        if (table.getSelectedRow() != -1) {
            int selectedRow = table.getSelectedRow();
            String fio = (String) table.getValueAt(selectedRow, 0);
            String responsible = (String) table.getValueAt(selectedRow, 1);
            String date = (String) table.getValueAt(selectedRow, 2);
            String symptom = (String) table.getValueAt(selectedRow, 4);

            String reference = "<html>"
                    + "<head>"
                    + "<style>"
                    + "body { font-family: Arial, sans-serif; padding: 20px; }"
                    + "h1 { text-align: center; }"
                    + "p { font-size: 14px; line-height: 1.5; }"
                    + "strong { font-weight: bold; }"
                    + "table { width: 100%; margin-top: 20px; border-collapse: collapse; }"
                    + "th, td { border: 1px solid #000; padding: 8px; text-align: left; }"
                    + "th { background-color: #f2f2f2; }"
                    + "</style>"
                    + "</head>"
                    + "<body>"
                    + "<h1>Медицинская справка</h1>"
                    + "<p><strong>ФИО пациента:</strong> " + fio + "</p>"
                    + "<p><strong>Дата обращения:</strong> " + date + "</p>"
                    + "<p><strong>Жалобы:</strong> " + symptom + "</p>"
                    + "<p><strong>Назначения врача:</strong> </p>"
                    + "<table>"
                    + "<tr><th>Дата</th><th>Назначение</th></tr>"
                    + "<tr><td>" + date + "</td><td></td></tr>" // Здесь вы можете добавить свои назначения
                    + "</table>"
                    + "<p>Врач " + responsible + "</p>"
                    + "<p>Подпись: _____________________</p>"
                    + "<p>Дата: " + date + "</p>"
                    + "</body>";
            JOptionPane.showMessageDialog(null, reference, "Справка", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Исключение, которое выбрасывается, когда операция поиска выполняется без действительного ввода.
     */
    private static class NotSearched extends Exception {
        /**
         * Конструктор для создания исключения NotSearched с сообщением по умолчанию.
         */
        public NotSearched() {
            super("Вы не ввели текст для поиска😬");
        }
        /**
         * Проверяет, является ли предоставленная строка пустой или совпадает с плейсхолдером.
         *
         * @param str         строка для проверки
         * @param placeholder плейсхолдер, указывающий на отсутствие действительного ввода
         * @throws NotSearched если строка пустая или равна плейсхолдеру
         */
        private static void checkString(String str, String placeholder) throws NotSearched {
            if ((str.isEmpty() || str.equals(placeholder))) {
                throw new NotSearched();
            }
        }
    }

    public static void main(String[] args) {
        UserInterface userInterface = new UserInterface();
        userInterface.show();
    }
}

