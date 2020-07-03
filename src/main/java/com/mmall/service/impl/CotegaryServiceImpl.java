package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICotegaryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Service("iCotegaryService")
public class CotegaryServiceImpl implements ICotegaryService {
    private Logger logger = LoggerFactory.getLogger(CotegaryServiceImpl.class);
    @Autowired
    CategoryMapper categoryMapper;
    public ServerResponse addCategory(String cotegaryname, Integer parentId){
        //先判断输入 的 参数是否符合要求
        if(parentId==null || StringUtils.isBlank(cotegaryname)){
            return ServerResponse.createrByErrorMsg("添加类品参数错误");
        }
        Category category = new Category();
        category.setName(cotegaryname);
        category.setParentId(parentId);
        category.setStatus(true);//这个分类是可用的
        int rowCont = categoryMapper.insert(category);
        if(rowCont>0){
            return ServerResponse.createrBySuccessMsg("添加品类成功");
        }
        return ServerResponse.createrByErrorMsg("添加品类失败");
    }
    public ServerResponse updateCategoryName(Integer categoryId,String categoryName){
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createrByErrorMsg("更新品类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount > 0){
            return ServerResponse.createrBySuccess("更新品类名字成功");
        }
        return ServerResponse.createrByErrorMsg("更新品类名字失败");
    }
//获取子节点平缓的信息
public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
    List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
    if(CollectionUtils.isEmpty(categoryList)){
        logger.info("未找到当前分类的子分类");
    }
    return ServerResponse.createrBySuccess(categoryList);
}


    /**
     * 递归查询本节点的id及孩子节点的id
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet,categoryId);


        List<Integer> categoryIdList = Lists.newArrayList();
        if(categoryId != null){
            for(Category categoryItem : categorySet){
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createrBySuccess(categoryIdList);
    }


    //递归算法,算出子节点
    private Set<Category> findChildCategory(Set<Category> categorySet ,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null){
            categorySet.add(category);
        }
        //查找子节点,递归算法一定要有一个退出的条件
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        for(Category categoryItem : categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }





}
