package top.henton.jarvis;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import top.henton.jarvis.converter.DateTimeConverter;
import top.henton.jarvis.converter.OpcodesConverter;
import top.henton.jarvis.model.file.reader.FileListReader;
import top.henton.jarvis.model.file.reader.IReader;
import top.henton.jarvis.model.file.reader.Msg;
import top.henton.jarvis.model.file.reader.MsgReader;
import top.henton.jarvis.model.file.reader.SingleFileReader;

@Command(name = "dataExtract", mixinStandardHelpOptions = true, version = "DataExtract 0.1", description = "extract valid data from log!")
public class DataExtract implements Callable<Integer> {

    protected static final Logger logger = LoggerFactory.getLogger(DataExtract.class);

    @Parameters(paramLabel = "FILE", description = "source log files")
    Set<File> sourceFiles;

    @Option(names = {"-tf",
            "--target-file"}, required = true, description = "target result directory")
    File targetFile;

    @Option(names = {
            "-cs"}, required = true, converter = OpcodesConverter.class, description = "opcodes to extract")
    Set<Integer> opcodes;

    @Option(names = {"-tc",
            "--target-count"}, description = "target role count to calculate, default 0(ps:use real data)")
    int targetCount = 0;

    @Option(names = {"-d",
            "--duration"}, required = true, description = "statistic duration(second)")
    int duration;

    @Option(names = {"-st",
            "--start-time"}, converter = DateTimeConverter.class, description = "start time to statistic(not required)")
    long startTime = 0L;

    @Option(names = {"-et",
            "--end-time"}, converter = DateTimeConverter.class, description = "end time to statistic(not required)")
    long endTime = Long.MAX_VALUE;


    private void extract() throws IOException {
        if (targetFile.exists()) {
            logger.warn("dest file [{}] exists, deleted", targetFile);
            targetFile.delete();
        }
        targetFile.createNewFile();
        Map<Integer, Integer> msgCount = Maps.newHashMap();
        this.opcodes.forEach(opcode -> msgCount.put(opcode, 0));
        long durationMills = TimeUnit.SECONDS.toMillis(this.duration);
        long lastTime = 0L;
        HashSet<String> totalRids = Sets.newHashSet();
        HashSet<String> moduleRids = Sets.newHashSet();
        int totalCount = 0;
        int moduleCount = 0;
        List<IReader> readers = Lists.newArrayList();
        for (File file : this.sourceFiles) {
            if (file.isHidden()) {
                continue;
            }
            if (file.isFile()) {
                SingleFileReader singleFileReader = new SingleFileReader(file);
                readers.add(singleFileReader);
                continue;
            }
            FileListReader fileListReader = new FileListReader(
                    Arrays.stream(file.listFiles()).filter(File::isFile)
                            .filter(file1 -> !file1.isHidden())
                            .map(File::getAbsolutePath).collect(Collectors.toList()));
            readers.add(fileListReader);
        }
        MsgReader filesReader = new MsgReader(readers);
        List<String> lines = Lists.newArrayList();
        String info = "time,total tps,tps,total player,player,msg percent,player percent";
        if (targetCount != 0) {
            info = info + ",target tps(*1000),target player";
        }
        for (Integer k : msgCount.keySet()) {
            info = info + "," + k + " tps," + k + " count";
            if (targetCount != 0) {
                info = info + "," + k + " target tps(*1000)," + k + " target count";
            }
        }
        lines.add(info);
        while (filesReader.hasNext()) {
            Msg msg = filesReader.readMsg();
            String rid = msg.getRid();
            int msgCode = msg.getMsgCode();
            long time = msg.getTimestamp();
            if (time < startTime) {
                continue;
            }
            if (time >= endTime) {
                break;
            }
            if (lastTime == 0) {
                lastTime = time;
            }
            while (time - lastTime > durationMills) {
                lastTime += durationMills;
                float nowTps = (float) moduleCount / durationMills * 1000;
                int playerCount = moduleRids.size();
                int totalPlayer = totalRids.size();
                info = new DateTime(lastTime).toLocalDateTime().toString() + ","
                        + (float) totalCount / durationMills * 1000 + ","
                        + nowTps + "," + totalPlayer + ","
                        + playerCount + "," + (float) moduleCount / totalCount * 100 + ","
                        + (float) playerCount / totalPlayer * 100;
                if (targetCount != 0) {
                    float rate = targetCount / (totalPlayer != 0 ? totalPlayer : targetCount);
                    info = info + "," + (nowTps * rate * 1000) + "," + (playerCount * rate);
                }
                for (Integer k : msgCount.keySet()) {
                    int count = msgCount.getOrDefault(k, 0);
                    float nowMsgTps = (float) count / durationMills * 1000;
                    info = info + "," + nowMsgTps + "," + count;
                    if (targetCount != 0) {
                        float rate = targetCount / (totalPlayer != 0 ? totalPlayer : targetCount);
                        info = info + "," + (nowMsgTps * rate * 1000) + "," + (count * rate);
                    }
                    msgCount.put(k, 0);
                }
                lines.add(info);
                totalCount = 0;
                moduleCount = 0;
                totalRids.clear();
                moduleRids.clear();
            }
            totalCount += 1;
            totalRids.add(rid);
            if (msgCount.containsKey(msgCode)) {
                moduleCount += 1;
                moduleRids.add(rid);
                msgCount.put(msgCode, msgCount.getOrDefault(msgCode, 0) + 1);
            }
        }
        filesReader.close();
        lastTime += durationMills;
        float nowTps = (float) moduleCount / durationMills * 1000;
        int playerCount = moduleRids.size();
        int totalPlayer = totalRids.size();
        info = new DateTime(lastTime).toLocalDateTime().toString() + ","
                + (float) totalCount / durationMills * 1000 + ","
                + nowTps + "," + totalPlayer + ","
                + playerCount + "," + (float) moduleCount / totalCount * 100 + ","
                + (float) playerCount / totalPlayer * 100;
        if (targetCount != 0) {
            float rate = targetCount / (totalPlayer != 0 ? totalPlayer : targetCount);
            info = info + "," + (nowTps * rate * 1000) + "," + (playerCount * rate);
        }
        for (Integer k : msgCount.keySet()) {
            int count = msgCount.getOrDefault(k, 0);
            float nowMsgTps = (float) count / durationMills * 1000;
            info = info + "," + nowMsgTps + "," + count;
            if (targetCount != 0) {
                float rate = targetCount / (totalPlayer != 0 ? totalPlayer : targetCount);
                info = info + "," + (nowMsgTps * rate * 1000) + "," + (count * rate);
            }
            msgCount.put(k, 0);
        }
        lines.add(info);
        FileUtils.writeLines(targetFile, "UTF-8", lines);
    }

    public static void main(String[] args) throws IOException {
        int exitCode = new CommandLine(new DataExtract()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws IOException {
        logger.info(
                "source files is [{}], target file is [{}], opcodes is [{}], duration is [{}], start time is [{}], end time is [{}]",
                sourceFiles, targetFile, opcodes, duration, startTime, endTime);
        extract();
        return 0;
    }
}
