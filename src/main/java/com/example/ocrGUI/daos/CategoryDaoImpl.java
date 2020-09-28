package com.example.ocrGUI.daos;

import com.example.ocrGUI.models.Category;
import com.example.ocrGUI.models.Dish;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import static com.example.ocrGUI.models.Category.setCurrentCategoryId;

public class CategoryDaoImpl implements CategoryDao {

    private NamedParameterJdbcTemplate template;

    public CategoryDaoImpl(NamedParameterJdbcTemplate template){
        this.template = template;
    }

    @Override
    public void insertCategory(int menuId, Category category) {

        Resource r = new ClassPathResource("applicationContext.xml");
        BeanFactory factory=new XmlBeanFactory(r);
        DishDao dishDao = (DishDao)factory.getBean("ddao");

        int[] dishIds = new int[category.getDishes().size()];
        int i = 0;
        for(Dish dish : category.getDishes()){
            dishDao.insertDish(category.getCategoryID(), dish);
            dishIds[i] = dish.getDishID();
            i++;
        }

        SqlParameterSource emptyParam = new MapSqlParameterSource();
        int id = template.queryForObject("select nextval('categories_id_seq')", emptyParam, Integer.class);
        final String sql = "insert into categories(id, ocr_text, item_ids, menu_id) values (:id, :name, :dishIds, :menuId)";
        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("name", category.getName())
                .addValue("dishIds", dishIds)
                .addValue("menuId", menuId);
        template.update(sql, param, holder);

        category.setCategoryID(id);
        for(Dish dish : category.getDishes()){
            dish.setCategoryID(category.getCategoryID());
            dishDao.updateDish(dish);
        }
    }

    @Override
    public void updateCategory(Category category) {

        int categoryID = category.getCategoryID();

        int[] dishIds = new int[category.getDishes().size()];
        int i = 0;
        for(Dish dish : category.getDishes()){
            dishIds[i] = dish.getDishID();
            i++;
        }

        final String sql = "update categories set ocr_text=:ocrText, item_ids=:dishIds where id=:categoryID";
        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("categoryID", categoryID)
                .addValue("dishIds", dishIds)
                .addValue("ocrText", category.getName());
        template.update(sql, param, holder);

    }

    @Override
    public void deleteCategory(Category category) {

        final String sql = "delete from categories where id=:categoryID";
        Map<String, Object> map = new HashMap<>();
        map.put("categoryID", category.getCategoryID());

        template.execute(sql, map, new PreparedStatementCallback<Object>() {
            @Override
            public Object doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                return preparedStatement.executeUpdate();
            }
        });

    }

    @Override
    public List<Category> selectCategory(int categoryId) {
        final String query = "select * from categories where id = :categoryId";

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("categoryId", categoryId);

        return template.query(query, map, new CategoryRowMapper());

    }

}
