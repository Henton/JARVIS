package top.henton.jarvis.converter;

import com.google.common.collect.Sets;
import java.util.Set;
import picocli.CommandLine.ITypeConverter;

public class OpcodesConverter implements ITypeConverter<Set> {

    @Override
    public Set<Integer> convert(String value) throws Exception {
        String[] split = value.split(",");
        Set<Integer> result = Sets.newHashSet();
        for (String opcode : split) {
            if (opcode.contains("-")) {
                String[] opcodes = opcode.split("-");
                int start = Integer.parseInt(opcodes[0]);
                int end = Integer.parseInt(opcodes[1]);
                for (int i = start; i <= end; ++i) {
                    result.add(i);
                }
            } else {
                result.add(Integer.parseInt(opcode));
            }
        }
        return result;
    }
}
