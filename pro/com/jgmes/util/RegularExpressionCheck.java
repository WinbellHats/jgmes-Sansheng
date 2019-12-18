package com.jgmes.util;

public class RegularExpressionCheck {

    /**
     * 校验 yyyy-MM-dd 格式的字符串
     * @param dateString
     * @return
     */
    public Boolean checkDate(String dateString,Boolean haveTime){
        if (haveTime){
            if(dateString.matches("([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))\\s([0-5]\\d):([0-5]\\d):([0-5]\\d)$"))
                return  true;
                return false;
        }else{
            if(dateString.matches("([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))"))
                return true;
                return false;
        }
    }

    /**
     * 校验是否是正整数
     * @param numStr
     * @param includeZero 校验包含0,true 为包含0.即正整数+0
     * @return
     */
    public Boolean checkPositiveInteger(String numStr,Boolean includeZero){
        if (includeZero){
            if(numStr.matches("^[1-9]\\d*|0$")) {
                return true;
            }
            return false;
        }else {
            if(numStr.matches("^[1-9]\\d*$")) {
                return true;
            }
            return false;
        }
    }

    /**
     * 校验是否是整数，包括0
     * @param numStr
     * @return
     */
    public Boolean checkInteger(String numStr){
        if (numStr.matches("^-?[1-9]\\d*|0$")){
            return true;
        }
        return false;
    }

    /**
     * 校验是否是负整数
     * @param numStr
     * @param includeZero 校验包含0,true 为包含0.即负整数+0
     * @return
     */
    public Boolean checkNegativeInteger(String numStr,Boolean includeZero){
        if (includeZero){
            if(numStr.matches("^-[1-9]\\d*|0$")) {
                return true;
            }
            return false;
        }else {
            if(numStr.matches("^-[1-9]\\d*$")) {
                return true;
            }
            return false;
        }
    }

    /**
     * 校验中文
     * @param Chinese
     * @return
     */
    public Boolean checkChinese(String Chinese){
        if (Chinese.matches("[\\u4e00-\\u9fa5]")){
            return true;
        }
        return false;
    }

    /**
     * 校验email地址
     * @param email
     * @return
     */
    public Boolean checkEmail(String email){
        if (email.matches("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?")){
            return true;
        }
        return false;
    }

    /**
     * 校验18位身份证号
     * @param IDNumber
     * @return
     */
    public Boolean checkIDNumber(String IDNumber){
        if (IDNumber.matches("^(\\d{6})(\\d{4})(\\d{2})(\\d{2})(\\d{3})([0-9]|X)$")){
            return true;
        }
        return false;
    }

    /**
     * 匹配任意精度的浮点型
     * @param NUMERIC_PRECISION
     * @param NUMERIC_SCALE
     * @param numStr
     * @return
     */
    public Boolean CheckDcimal(Integer NUMERIC_PRECISION,Integer NUMERIC_SCALE,String numStr){
        String reg = "(-)?\\d{1,"+NUMERIC_PRECISION+"}(\\.\\d{1,"+NUMERIC_SCALE+"})?";
        if (numStr.matches(reg)){
            return true;
        }
        return false;
    }

    /**
     * 11位手机号
     * @param phoneStr
     * @return
     */
    public Boolean checkPhone(String phoneStr){
        String reg = "^1[3|4|5|8][0-9]\\d{4,8}$";
        if (phoneStr.matches(reg)){
            return true;
        }
        return false;
    }


}
