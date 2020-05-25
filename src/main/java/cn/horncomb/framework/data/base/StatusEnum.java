package cn.horncomb.framework.data.base;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum {
    /**
     * 通用禁启用状态
     */
    STATUS_ENABLE("启用","A"),
    STATUS_DISABLE("禁用","D");

    private final String desc;
    private final String code;
}

//import lombok.AllArgsConstructor;
//        import lombok.Getter;
//
//@Getter
//@AllArgsConstructor
//public enum StatusEnum {
//    /**
//     * 通用禁启用状态
//     */
//    STATUS_ENABLE("启用","A"),
//    STATUS_DISABLE("禁用","D");
//
//    private final String desc;
//    private final String code;
//
//    public static StatusEnum find(String val) {
//        for (StatusEnum statusEnum : StatusEnum.values()) {
//            if (val.equals(statusEnum.getCode())) {
//                return statusEnum;
//            }
//        }
//        return null;
//    }
//}
