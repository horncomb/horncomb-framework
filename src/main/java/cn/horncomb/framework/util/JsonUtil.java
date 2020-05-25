package cn.horncomb.framework.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JsonUtil {

    public static String toJson(String name, Object value) {
        if(value==null){
            value = "";
        }else if(value instanceof Long){
            value = ""+value;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(name,value);
        return jsonObject.toString();
    }
    public static String toJson(Object object){
        return JSON.toJSONString(object);
    }

    /**
     * 对json数组排序，
     * @param jsonArr
     * @param sortKey 排序关键字
     * @param is_desc is_desc-false升序列  is_desc-true降序 (排序字段为字符串)
     * @return
     */
    public static String jsonArraySort(JSONArray jsonArr, String sortKey, boolean is_desc) {
        //存放排序结果json数组
        JSONArray sortedJsonArray = new JSONArray();
        //用于排序的list
        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        //将参数json数组每一项取出，放入list
        for (int i = 0; i < jsonArr.size(); i++) {
            jsonValues.add(jsonArr.getJSONObject(i));
        }
        //快速排序，重写compare方法，完成按指定字段比较，完成排序
        Collections.sort(jsonValues, new Comparator<JSONObject>() {
            //排序字段
            private  final String KEY_NAME = sortKey;
            //重写compare方法
            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();
                try {
                    valA = a.getString(KEY_NAME);
                    if(StringUtils.isEmpty(valA)){
                        valA="0";
                    }
                    valB = b.getString(KEY_NAME);
                    if(StringUtils.isEmpty(valB)){
                        valB="0";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //是升序还是降序
                if (is_desc){
                    return Integer.valueOf(valB)-Integer.valueOf(valA);

                } else {
                    return Integer.valueOf(valA)-Integer.valueOf(valB);
                }

            }
        });
        //将排序后结果放入结果jsonArray
        for (int i = 0; i < jsonArr.size(); i++) {
            sortedJsonArray.add(jsonValues.get(i));
        }
        return sortedJsonArray.toString();
    }
}