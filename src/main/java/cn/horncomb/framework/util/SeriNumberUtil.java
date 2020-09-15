package cn.horncomb.framework.util;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 流水号 工具
 * 如 3位   001-002....999-A01-A02...ZZZ
 */
public class SeriNumberUtil {
    /**
     * 生成下一个流水序号  SeriNumber
     * @param currentStr 当前流水序号
     * @param serialLength 流水序号长度
     * @return 返回流水序号
     * @throws Exception
     */
    public static String seriNumber(String currentStr, int serialLength) throws Exception {
        // 如果currentStr为空
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isEmpty(currentStr)) {
            for (int i = 0; i < serialLength; i++) {
                stringBuilder.append("0");
            }
        } else {
            stringBuilder.append(currentStr);
        }
        String driver = "";
        try {
            driver = driver(stringBuilder.toString());
        } catch (Exception e) {
            char charAt = stringBuilder.charAt(0);
            if (charAt == 'Z') {
                throw new Exception("数据头已溢出");
            }
            if (charAt == '9') {
                charAt = '@';
            }
            AtomicInteger atomic = new AtomicInteger(charAt);
            StringBuilder stringBuilder1 = new StringBuilder();
            for (int i = 0; i < serialLength; i++) {
                stringBuilder1.append("0");
            }
            driver = driver(stringBuilder1.toString());
        }
        return driver;
    }

    /**
     * 生成流水序号
     *
     * @param driver 当前头+流水序号
     * @return 返回下一个流水序号
     * @throws Exception
     */
    public static String driver(String driver) throws Exception {
        driver = driver.toUpperCase();
        char[] charArray = driver.toCharArray();
        AtomicInteger z = new AtomicInteger(0);
        for (int i = charArray.length - 1; i >= 0; i--) {
            char c = charArray[i];
            // 先判断满Z进1的情况
            if (c == 'Z') {
                if (i == 0) {
                    throw new Exception("数据已溢出");
                }
                charArray[i] = 'A';
                z.set(1);
                continue;
            }

            if (c == '9') {
                // 如果到了第一位还是9
                if (i == 0) {
                    charArray[i] = 'A';
                    break;
                }
                charArray[i] = '0';
                z.set(1);
                continue;
            }
            // 如果等于1则需要进位1
            if (z.intValue() == 1 || i == charArray.length - 1) {
                AtomicInteger atomic = new AtomicInteger(c);
                charArray[i] = (char) atomic.incrementAndGet();
                z.set(0);
                break;
            }
        }

        return String.valueOf(charArray);
    }

    public static void main(String[] args) throws Exception {
        String seriNumber = seriNumber("", 3);
        System.out.println(seriNumber);
    }
}




