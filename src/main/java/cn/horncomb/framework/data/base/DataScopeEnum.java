package cn.horncomb.framework.data.base;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataScopeEnum {
    /* 全部的数据权限 */
    ALL("ALL", "全部的数据权限1"),

    /* 自己部门的数据权限 */
    SELF("SELF", "自己部门的数据权限"),

    /* 自己部门的数据权限 */
    SELF_SUB("SELF_SUB", "自己部门及下级的数据权限"),

    /* 自定义的数据权限 */
    CUSTOMIZE("CUSTOMIZE", "自定义的数据权限");

    private final String code;
    private final String desc;

    public static DataScopeEnum find(String val) {
        for (DataScopeEnum dataScopeEnum : DataScopeEnum.values()) {
            if (val.equals(dataScopeEnum.getCode())) {
                return dataScopeEnum;
            }
        }
        return null;
    }
}
