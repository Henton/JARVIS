package top.henton.jarvis.model.file.reader;

public interface IReader {

    boolean hasNext();

    String nextLine();

    void close();
}
