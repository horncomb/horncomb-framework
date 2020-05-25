package cn.horncomb.framework.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class TreeUtil<T> {
    /* *
     * @Description 用双重循环建树
     * @Param [treeNodes树节点列表, root根标志,id属性id名，pid属性父id名,sublist子集合属性名]
     * @Return java.util.List<T>
     */
    public  List<T> buildTree(List<T> treeNodes, String root,String id,String pid,String sublist) {
        List<T> trees = new ArrayList<>();
        treeNodes.forEach(bean ->{
            try {
//                if (StringUtils.equals(root, (String) PropertyUtils.getProperty(bean,pid))){
                if (StringUtils.equals(root, PropertyUtils.getProperty(bean,pid).toString())){
                    trees.add(bean);
                }
                treeNodes.forEach(bean2 ->{
                    try {
                        if (PropertyUtils.getProperty(bean,id).equals(PropertyUtils.getProperty(bean2,pid))){
                            List<T> list=new ArrayList<>();
                            Object property = PropertyUtils.getProperty(bean, sublist);
                            if (property!=null){
                                list=(List<T>)property;
                            }
                            list.add(bean2);
                            PropertyUtils.setProperty(bean,sublist,list);

                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

        });

        return trees;
    }
}
