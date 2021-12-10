package com.garciajuanjo.calculatorpan.dao;

import com.garciajuanjo.calculatorpan.App;
import com.garciajuanjo.calculatorpan.domain.Ingredient;
import com.garciajuanjo.calculatorpan.domain.Provider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IngredientDao {

    public void addIngredient(Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO ingredients (name, description, is_active) VALUES (?, ?, ?)";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);

        statement.setString(1, ingredient.getName());
        statement.setString(2, ingredient.getDescription());
        statement.setInt(3, ingredient.getIsActive());
        statement.executeUpdate();

        addIngredientProviders(ingredient);
        statement.close();
    }

    public ObservableList<Ingredient> getAllIngredients(boolean is_active) throws SQLException {
        String sql;
        if (is_active) sql = "SELECT * FROM ingredients WHERE is_active = 1 ORDER BY name";
        else sql = "SELECT * FROM ingredients ORDER BY name";

        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()){
            Ingredient ingredient = new Ingredient();
            ingredient.setIdIngredient(resultSet.getInt(1));
            ingredient.setName(resultSet.getString(2));
            ingredient.setDescription(resultSet.getString(3));
            ingredient.setTypeIngredient(Ingredient.TypeIngredient.OTRO);
            ingredient.setIsActive(resultSet.getInt(5));

            ingredient.setProviders(getProvidersIngredient(ingredient));

            ingredients.add(ingredient);
        }
        statement.close();
        return ingredients;
    }

    public Boolean existIngredient(String name, boolean is_active) throws SQLException {
        String sql;
        if (is_active) {
            sql = "SELECT * FROM ingredients WHERE name = ? AND is_active = 1";
        } else {
            sql = "SELECT * FROM ingredients WHERE name = ? AND is_active = 0";
        }

        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        boolean result = resultSet.next();
        statement.close();

        return result;
    }

    public Ingredient getIngredient(String name) throws SQLException{
        String sql = "SELECT * FROM ingredients WHERE name = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();

        Ingredient ingredient = new Ingredient();

        ingredient.setIdIngredient(resultSet.getInt(1));
        ingredient.setName(resultSet.getString(2));
        ingredient.setDescription(resultSet.getString(3));
        ingredient.setTypeIngredient(Ingredient.TypeIngredient.OTRO);
        ingredient.setProviders(getProvidersIngredient(ingredient));
        ingredient.setIsActive(resultSet.getInt(5));
        statement.close();

        return ingredient;
    }

    public int getIdIngredient(String name) throws SQLException {
        String sql = "SELECT idIngredient FROM ingredients WHERE name = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();

        int id = resultSet.getInt(1);
        statement.close();

        return id;
    }

    public void addIngredientProviders(Ingredient ingredient) throws SQLException {
        String sql = "INSERT INTO ingredientProvider (provider, ingredient) VALUES (?,?)";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);

        ingredient.getProviders().forEach(provider -> {
            try {
                statement.setString(1, provider.getName());
                statement.setString(2, ingredient.getName());
                statement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        statement.close();
    }

    private ObservableList<Provider> getProvidersIngredient(Ingredient ingredient) throws  SQLException {
        String sql = "SELECT provider FROM ingredientProvider WHERE ingredient = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        ObservableList<Provider> providers = FXCollections.observableArrayList();
        statement.setString(1, ingredient.getName());
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()){
            String nameProvider = resultSet.getString(1);
            Provider provider = App.getProviderDao().getProvider(nameProvider);
            providers.add(provider);
        }
        statement.close();

        return providers;
    }

    public boolean updateIngredient(Ingredient ingredient) throws SQLException {
        String sql = "UPDATE ingredients SET name = ?, description = ?, is_active = ? WHERE idIngredient = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);

        statement.setString(1, ingredient.getName());
        statement.setString(2, ingredient.getDescription());
        statement.setInt(3, ingredient.getIsActive());
        statement.setString(4, String.valueOf(ingredient.getIdIngredient()));
        int rows = statement.executeUpdate();

        deleteIngredientProvider(ingredient);
        addIngredientProviders(ingredient);

        statement.close();

        return rows != 0;
    }

    public boolean deleteIngredient(Ingredient ingredient) throws SQLException {
        String sql = "UPDATE ingredients SET is_active = 0 WHERE name = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, ingredient.getName());
        int rows = statement.executeUpdate();

        deleteIngredientProvider(ingredient);

        statement.close();

        return rows != 0;
    }

    private void deleteIngredientProvider(Ingredient ingredient) throws SQLException {
        String sql = "DELETE FROM ingredientProvider WHERE ingredient = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);

        statement.setString(1, ingredient.getName());
        statement.executeUpdate();

        statement.close();
    }

}
