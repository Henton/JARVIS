package top.henton.jarvis.model.file.reader;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

public class MsgReader {

    private List<IReader> readers;

    private PriorityQueue<Msg> queue;

    private void init(List<IReader> readers) {
        if (Objects.isNull(readers) || readers.isEmpty() || readers.stream()
                .anyMatch(reader -> Objects.isNull(reader))) {
            return;
        }
        this.readers = readers;
        this.queue = new PriorityQueue<>(readers.size(),
                Comparator.comparingLong(Msg::getTimestamp));
        readAllFromFile();
    }

    public MsgReader(List<IReader> readers) throws IOException {
        init(readers);
    }

    public MsgReader(IReader... readers) throws IOException {
        List<IReader> readersList = Lists.newArrayList(readers);
        init(readersList);
    }

    private void readOneFromFile(int index) {
        IReader reader = readers.get(index);
        String nextLine = reader.nextLine();
        String[] params = nextLine.split(" ");
        Msg msg = new Msg();
        msg.setFrom((byte) index);
        msg.setTimestamp(Long.parseLong(params[0]));
        msg.setRid(params[1]);
        msg.setMsgCode(Integer.parseInt(params[2]));
        msg.setCostTime(Integer.parseInt(params[3]));
        queue.add(msg);
    }

    private void readAllFromFile() {
        for (int i = 0; i < readers.size(); ++i) {
            readOneFromFile(i);
        }
    }

    public boolean hasNext() {
        return !queue.isEmpty();
    }

    public Msg readMsg() {
        Msg poll = queue.poll();
        if (Objects.nonNull(poll)) {
            byte index = poll.getFrom();
            IReader reader = readers.get(index);
            if (reader.hasNext()) {
                readOneFromFile(index);
            }
        }
        return poll;
    }

    public void close() {
        readers.forEach(reader -> reader.close());
    }

}
