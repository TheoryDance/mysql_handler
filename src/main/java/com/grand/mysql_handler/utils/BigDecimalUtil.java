package com.grand.mysql_handler.utils;

import java.math.BigDecimal;

/**
 * @author Yeafel
 * 2019/5/22 14:38
 * Do or Die,To be a better man!
 */
public class BigDecimalUtil {

    public static BigDecimal add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }

    public static BigDecimal sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }


    public static BigDecimal mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }

    /**
     * v1/v2的值
     * 四舍六入5留双
     * 默认无小数
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal div(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, 0, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * 返回v1/v2的值
     * 四舍六入5留双
     * 默认无小数
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal div_scale(double v1, double v2, int scale) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * 四舍六入5留双
     *
     * @param data
     * @param scale
     * @return
     */
    public static double objTodouble(Double data, int scale) {
        if (data == null)
            return 0;
        return BigDecimal.valueOf(data).setScale(BigDecimal.ROUND_HALF_EVEN, scale).doubleValue();
    }


    /**
     * 四舍五入,取一位小数
     * 先保留3位，待会儿co还要做四舍六入五留双操作。
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal divForCo(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, 3, BigDecimal.ROUND_HALF_UP);
    }

}
