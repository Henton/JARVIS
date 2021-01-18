package top.henton.jarvis.model.statistic;

import java.util.Objects;
import top.henton.jarvis.common.Constants;

public class RTStat {

    private int[] rts;

    private int totalCount = 0;

    public RTStat() {
        this(Constants.MAX_RESPONSE_TIME);
    }

    public RTStat(int maxResponseTime) {
        rts = new int[maxResponseTime + 1];
    }

    public void addRT(int responseTime) {
        addRT(responseTime, 1);
    }

    public void addRT(int responseTime, int count) {
        rts[Math.min(responseTime, rts.length - 1)] += count;
        totalCount += count;
    }

    public int getPercentRT(int percent) {
        if (percent <= 0 || Objects.isNull(rts) || rts.length <= 0) {
            return 0;
        }
        int currentCount = 0;
        for (int i = 0; i < rts.length; ++i) {
            currentCount += rts[i];
            int percentNow = currentCount * 100 / totalCount;
            if (percentNow >= percent) {
                return i;
            }
        }
        return rts.length - 1;
    }

}
