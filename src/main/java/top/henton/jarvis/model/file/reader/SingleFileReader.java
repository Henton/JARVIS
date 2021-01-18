package top.henton.jarvis.model.file.reader;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class SingleFileReader implements IReader {

    private File file;

    private LineIterator lineIterator;

    public SingleFileReader(String path) throws IOException {
        File file = new File(path);
        if (file.exists() && file.isFile() && file.canRead()) {
            this.file = file;
            lineIterator = FileUtils.lineIterator(file);
        }
    }

    public SingleFileReader(File file) throws IOException {
        if (file.exists() && file.isFile() && file.canRead()) {
            this.file = file;
            lineIterator = FileUtils.lineIterator(file);
        }
    }

    @Override
    public boolean hasNext() {
        return lineIterator.hasNext();
    }

    @Override
    public String nextLine() {
        return lineIterator.nextLine();
    }

    public File getFile() {
        return file;
    }

    @Override
    public void close() {
        lineIterator.close();
    }
}
