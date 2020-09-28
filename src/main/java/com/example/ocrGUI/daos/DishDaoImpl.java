package com.example.ocrGUI.daos;

import com.example.ocrGUI.models.Dish;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DishDaoImpl implements DishDao {

    private NamedParameterJdbcTemplate template;
    public DishDaoImpl(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public void insertDish(int categoryId, Dish dish) {

        String dishName = dish.getName();
        String price = String.valueOf(dish.getPrice());
        String description = dish.getDescription();
        int dishID = dish.getDishID();

        SqlParameterSource emptyParam = new MapSqlParameterSource();
        int id = template.queryForObject("select nextval('dishes_id_seq')", emptyParam, Integer.class);
        final String sql = "insert into dishes(id, ocr_text, price, description, category_id)  values(:id, :dishName,:price,:description, :categoryId)";
        KeyHolder holder = new GeneratedKeyHolder();
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("dishName", dishName)
                .addValue("price", price)
                .addValue("description", description)
                .addValue("categoryId", categoryId);
        template.update(sql,param, holder);

        dish.setDishID(id);
    }
    @Override
    public void updateDish(Dish dish) {
        int dishID = dish.getDishID();

        final String sql = "update dishes set ocr_text=:ocrText, price=:price, description=:description, category_id=:categoryId where id=:dishID";
        KeyHolder holder = new GeneratedKeyHolder();
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("dishID", dishID)
                .addValue("ocrText", dish.getName())
                .addValue("price", dish.getPrice())
                .addValue("description", dish.getDescription())
                .addValue("categoryId", dish.getCategoryID());

        template.update(sql, param, holder);
    }

    @Override
    public void deleteDish(Dish dish) {

        final String sql = "delete from dishes where id=:dishID";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("dishID", dish.getDishID());
        
        template.execute(sql, map, new PreparedStatementCallback<Object>() {
            @Override
            public Object doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                return preparedStatement.executeUpdate();
            }
        });
    }

    @Override
    public List<Dish> selectDish(int dishId) {
        final String query = "select * from dishes where id = :dishId";

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("dishId", dishId);

        return template.query(query, map, new DishRowMapper());
    }

}
