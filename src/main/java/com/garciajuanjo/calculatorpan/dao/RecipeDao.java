package com.garciajuanjo.calculatorpan.dao;

import com.garciajuanjo.calculatorpan.App;
import com.garciajuanjo.calculatorpan.domain.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecipeDao {

    public void addRecipe(Recipe recipe) throws SQLException {
        String sql = "INSERT INTO recipes (name, observations, is_active) VALUES (?, ?, ?)";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);

        statement.setString(1, recipe.getName());
        statement.setString(2, recipe.getObservations());
        statement.setInt(3, recipe.getIsActive());
        statement.executeUpdate();

        addIngredientsRecipe(recipe);
        statement.close();
    }

    public ObservableList<Recipe> getAllRecipes() throws SQLException {
        String sql = "SELECT * FROM recipes WHERE is_active = 1 ORDER BY name";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();

        ObservableList<Recipe> recipes = FXCollections.observableArrayList();

        while (resultSet.next()){
            Recipe recipe = new Recipe();
            recipe.setIdRecipe( resultSet.getInt(1));
            recipe.setName( resultSet.getString(2));
            recipe.setObservations( resultSet.getString(3));
            recipe.setIngredients(getIngredientsRecipe(recipe.getName()));
            recipe.setIsActive(resultSet.getInt(5));
            recipes.add(recipe);
        }
        statement.close();

        return recipes;
    }

    public Recipe getRecipe(String name) throws SQLException {
        String sql = "SELECT * FROM recipes WHERE name = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();

        Recipe recipe = new Recipe();
        recipe.setIdRecipe(resultSet.getInt(1));
        recipe.setName(resultSet.getString(2));
        recipe.setObservations(resultSet.getString(3));
        recipe.setIngredients(getIngredientsRecipe(name));
        recipe.setIsActive(resultSet.getInt(5));
        statement.close();

        return recipe;
    }

    public boolean existRecipe(String name, boolean is_active) throws SQLException{
        String sql;
        PreparedStatement statement;
        if (is_active) {
            sql = "SELECT * FROM recipes WHERE name = ? AND is_active = ?";
            statement = App.getAppDao().getConnection().prepareStatement(sql);
            statement.setString(1, name);
            statement.setInt(2, 1);

        } else{
            sql = "SELECT * FROM recipes WHERE name = ?";
            statement = App.getAppDao().getConnection().prepareStatement(sql);
            statement.setString(1, name);
        }


        ResultSet resultSet = statement.executeQuery();
        boolean result = resultSet.next();
        statement.close();

        return result;
    }

    public void addIngredientsRecipe(Recipe recipe) throws SQLException {
        String sql = "INSERT INTO ingredientRecipe (ingredient, typeIngredient, percentage, recipe) VALUES (?, ?, ?, ?)";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);

        recipe.getIngredients().forEach(ingredient -> {
            try {
                statement.setString(1, ingredient.getIngredient().getName());
                statement.setString(2, ingredient.getIngredient().getTypeIngredient().toString());
                statement.setFloat(3, ingredient.getPercentage());
                statement.setString(4, recipe.getName());
                statement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        statement.close();
    }

    private ObservableList<IngredientRecipe> getIngredientsRecipe(String nameRecipe) throws SQLException {
        String sql = "SELECT ingredient, typeIngredient, percentage FROM ingredientRecipe WHERE recipe = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, nameRecipe);
        ResultSet resultSet = statement.executeQuery();
        ObservableList<IngredientRecipe> ingredients = FXCollections.observableArrayList();

        while (resultSet.next()){
            Ingredient ingredient;
            Flour flour;
            Prefermento prefermento;
            IngredientRecipe ingredientRecipe = new IngredientRecipe();

            String tipe = resultSet.getString(2);
            switch (tipe){
                case "HARINA":
                    flour = App.getFlourDao().getFlour(resultSet.getString(1));
                    ingredientRecipe.setIngredient(flour);
                    ingredientRecipe.setPercentage(resultSet.getFloat(3));
                    ingredients.add(ingredientRecipe);
                    break;
                case "PREFERMENTO":
                    prefermento = App.getPrefermentoDao().getPrefermento(resultSet.getString(1));
                    ingredientRecipe.setIngredient(prefermento);
                    ingredientRecipe.setPercentage(resultSet.getFloat(3));
                    ingredients.add(ingredientRecipe);
                    break;
                case "OTRO":
                    ingredient = App.getIngredientDao().getIngredient(resultSet.getString(1));
                    ingredientRecipe.setIngredient(ingredient);
                    ingredientRecipe.setPercentage(resultSet.getFloat(3));
                    ingredients.add(ingredientRecipe);
                    break;
            }
        }

        statement.close();

        return ingredients;
    }

    public boolean updateRecipe(Recipe recipe) throws SQLException {
        String sql = "UPDATE recipes SET name = ?, observations = ?, is_active = ? WHERE idRecipe = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, recipe.getName());
        statement.setString(2, recipe.getObservations());
        statement.setInt(3, recipe.getIsActive());
        statement.setInt(4, recipe.getIdRecipe());
        int rows = statement.executeUpdate();

        deleteIngredientRecipe(recipe);
        addIngredientsRecipe(recipe);
        statement.close();

        return rows != 0;
    }

    public boolean deleteRecipe(Recipe recipe, boolean delete) throws SQLException {
        String sql;
        if (delete) sql = "DELETE FROM recipes WHERE idRecipe = ?";
        else sql = "UPDATE recipes SET is_active = 0 WHERE idRecipe = ?";

        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setInt(1, recipe.getIdRecipe());
        int rows = statement.executeUpdate();

        deleteIngredientRecipe(recipe);
        statement.close();

        return rows != 0;
    }

    public void deleteIngredientRecipe(Recipe recipe) throws SQLException {
        String sql = "DELETE FROM ingredientRecipe WHERE recipe = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, recipe.getName());
        statement.executeUpdate();
        statement.close();
    }
}
