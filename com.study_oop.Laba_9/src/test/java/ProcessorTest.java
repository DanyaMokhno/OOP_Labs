import org.junit.Before;
import org.junit.Test;
import javax.swing.JFrame;
import java.io.File;
import static org.junit.Assert.assertNotEquals;

public class ProcessorTest {
    Processor processor;
    @Before
    public void setUp() {
        processor = new Processor(new JFrame());
        processor.createTable();
    }

    @Test
    public void testOpenFile() {
        processor.openFile();
        assertNotEquals("Таблица должна быть заполнена данными", 0, processor.tableModel.getRowCount());
    }

    @Test
    public void testSaveFile() {
        processor.currentFilePath = System.getProperty("user.dir")+"/file.xml";
        System.out.println(processor.currentFilePath);
        processor.saveFile();
        assertNotEquals("файл должен существовать", false, (new File(processor.currentFilePath)).exists());
    }
}