package com.example.ocrGUI.daos;

import com.example.ocrGUI.models.*;
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


public class MenuDaoImpl implements MenuDao {
    private NamedParameterJdbcTemplate template;
    public MenuDaoImpl(NamedParameterJdbcTemplate template){
        this.template = template;
    }

    @Override
    public void insertMenu(Menu menu) {

        Resource r=new ClassPathResource("applicationContext.xml");
        BeanFactory factory=new XmlBeanFactory(r);
        CategoryDao categoryDao = (CategoryDao)factory.getBean("cdao");

        int[] categoryIds = new int[menu.getMenuCategories().size()];
        int i = 0;
        for(Category category : menu.getMenuCategories()){
            categoryDao.insertCategory(menu.getMenuID(), category);
            categoryIds[i++] = category.getCategoryID();
        }
        SqlParameterSource emptyParam = new MapSqlParameterSource();
        int id = template.queryForObject("select nextval('menus_id_seq')", emptyParam, Integer.class);

        final String sql = "insert into menus(id, restaurant_name, categories) values (:id, :restaurantName, :categoryIds)";
        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("restaurantName", menu.getRestaurantName())
                .addValue("categoryIds", categoryIds);

        template.update(sql, parameterSource, holder);

        menu.setMenuID(id);

        for(Category category : menu.getMenuCategories()){
            category.setMenuID(menu.getMenuID());
            categoryDao.updateCategory(category);
        }
    }

    @Override
    public void updateMenu(Menu menu) {

        int[] categoryIds = new int[menu.getMenuCategories().size()];
        int i = 0;
        for(Category category : menu.getMenuCategories()){
            categoryIds[i++] = category.getCategoryID();
        }

        int menuId = menu.getMenuID();
        final String sql = "update menus set restaurant_name=:restaurantName, categories=:categoryIds where id=:menuId";

        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("menuId", menuId)
                .addValue("restaurantName", menu.getRestaurantName())
                .addValue("categoryIds", categoryIds);
        template.update(sql, param, holder);

    }

    @Override
    public void deleteMenu(Menu menu) {
        final String sql = "delete from menus where id=:menuID";
        Map<String, Object> map = new HashMap<>();
        map.put("menuID", menu.getMenuID());

        template.execute(sql, map, new PreparedStatementCallback<Object>() {
            @Override
            public Object doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                return preparedStatement.executeUpdate();
            }
        });
    }

    @Override
    public List<Menu> selectMenu(int menuId) {
        final String query = "select * from menus where id = :menuId";

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("menuId", menuId);

        return template.query(query, map, new MenuRowMapper());

    }

}
