import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
public class UserInterfaceTest {
    UserInterface userInterface;
    Processor processor;
    @Before
    public void setUp() {
        userInterface = new UserInterface();
        userInterface.window = new JFrame();
        processor = new Processor(userInterface.window);
        processor.createTable();
        userInterface.tableModel = processor.tableModel;
    }
    @Test
    public void testAddRow() {
        userInterface.addRow();
        Assert.assertTrue("должна быть одна строка",processor.table.getRowCount() > 0);
    }
}