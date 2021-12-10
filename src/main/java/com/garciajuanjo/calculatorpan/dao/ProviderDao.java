package com.garciajuanjo.calculatorpan.dao;

import com.garciajuanjo.calculatorpan.App;
import com.garciajuanjo.calculatorpan.domain.Flour;
import com.garciajuanjo.calculatorpan.domain.Ingredient;
import com.garciajuanjo.calculatorpan.domain.Provider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProviderDao {

    public void addProvider(Provider provider) throws SQLException {
        String sql = "INSERT INTO providers (name, direction, phone, observations, is_active) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement estatement = App.getAppDao().getConnection().prepareStatement(sql);
        estatement.setString(1, provider.getName());
        estatement.setString(2, provider.getDirection());
        estatement.setString(3, provider.getPhone());
        estatement.setString(4, provider.getObservations());
        estatement.setInt(5, provider.getIsActive());
        estatement.executeUpdate();

        estatement.close();
    }

    public ObservableList<Provider> getAllProviders() throws SQLException {
        String sql = "SELECT * FROM providers WHERE is_active = 1 ORDER BY name";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        ObservableList<Provider> providers = FXCollections.observableArrayList();
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Provider provider = new Provider();
            List<Ingredient> ingredients = new ArrayList<>();

            provider.setIdProvider(resultSet.getInt(1));
            provider.setName(resultSet.getString(2));
            provider.setDirection(resultSet.getString(3));
            provider.setPhone(resultSet.getString(4));
            provider.setObservations(resultSet.getString(5));

            //le añadimos los ingredientes que tenga ese proveedor
            String ingredientProvider = "SELECT * FROM ingredientProvider WHERE provider = ?";
            PreparedStatement statement1 = App.getAppDao().getConnection().prepareStatement(ingredientProvider);
            statement1.setString(1, provider.getName());
            ResultSet resultSet1 = statement1.executeQuery();

            while (resultSet1.next()) {
                //de cada ingrediente solo cojo el nombre (el tipo de ingrediente se lo da el constructor)
                Ingredient ingredient = new Ingredient();
                ingredient.setName(resultSet1.getString(2));
                ingredients.add(ingredient);
            }
            statement1.close();

            //le añadimos los ingredientes harina que tenga ese proveedor
            String flourProvider = "SELECT * FROM floursProvider WHERE provider = ?";
            PreparedStatement statement2 = App.getAppDao().getConnection().prepareStatement(flourProvider);
            statement2.setString(1, provider.getName());
            ResultSet resultSet2 = statement2.executeQuery();

            while (resultSet2.next()) {
                //de cada harina solo cojo el nombre (el tipo de ingrediente se lo da el controlador)
                Flour flour = new Flour();
                flour.setName(resultSet2.getString(2));
                ingredients.add(flour);
            }
            statement2.close();

            provider.setIngredients(ingredients);
            providers.add(provider);
        }
        statement.close();

        return providers;
    }

    public Provider getProvider(String name) throws SQLException {
        String sql = "SELECT * FROM providers WHERE name = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();

        Provider provider = new Provider();

        provider.setIdProvider(resultSet.getInt(1));
        provider.setName(resultSet.getString(2));
        provider.setDirection(resultSet.getString(3));
        provider.setPhone(resultSet.getString(4));
        provider.setObservations(resultSet.getString(5));
        statement.close();

        return provider;
    }

    public int getIdProvider(String name) throws SQLException {
        String sql = "SELECT idProvider FROM providers WHERE name = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();

        int id = resultSet.getInt(1);
        statement.close();

        return id;
    }

    public boolean existProvider(String name, boolean is_active) throws SQLException {
        String sql;
        if (is_active){
            sql = "SELECT * FROM providers WHERE name = ? AND is_active = 1";
        } else {
            sql = "SELECT * FROM providers WHERE name = ? AND is_active = 0";
        }

        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        boolean result = resultSet.next();
        statement.close();

        return result;
    }

    public boolean deleteProvider(Provider provider) throws SQLException {
        String sql = "UPDATE providers SET is_active = 0 WHERE idProvider = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setInt(1, provider.getIdProvider());
        int rows = statement.executeUpdate();

        deleteProviderFlour(provider);
        deleteProviderIngredient(provider);
        statement.close();

        return rows != 0;
    }

    private void deleteProviderIngredient(Provider provider) {
        provider.getIngredients().forEach(ingredient -> {

            if(ingredient.getTypeIngredient() == Ingredient.TypeIngredient.OTRO) {
                try {
                    String count = "SELECT COUNT(*) FROM ingredientProvider WHERE ingredient = ?";
                    PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(count);
                    statement.setString(1, ingredient.getName());
                    ResultSet resultSet = statement.executeQuery();
                    int num = resultSet.getInt(1);
                    statement.close();

                    if (num == 1) { //si ese ingrediente solo tiene un proveedor lo modificamos asignéndole el genérico
                        String sql = "UPDATE ingredientProvider SET provider = ? WHERE ingredient = ?";
                        PreparedStatement statement2 = App.getAppDao().getConnection().prepareStatement(sql);
                        statement2.setString(1, "Proveedor generico");
                        statement2.setString(2, ingredient.getName());
                        statement2.executeUpdate();
                        statement2.close();

                    } else { //si tiene varios lo eliminamos y le dejamos los otros
                        String sql = "DELETE FROM ingredientProvider WHERE provider = ? AND ingredient = ?";
                        PreparedStatement statement2 = App.getAppDao().getConnection().prepareStatement(sql);
                        statement2.setString(1, provider.getName());
                        statement2.setString(2, ingredient.getName());
                        statement2.executeUpdate();
                        statement2.close();

                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }

    private void deleteProviderFlour(Provider provider) {
        provider.getIngredients().forEach(ingredient -> {
            if(ingredient.getTypeIngredient() == Ingredient.TypeIngredient.HARINA) {
                try {
                    String count = "SELECT COUNT(*) FROM floursProvider WHERE flour = ?";
                    PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(count);
                    statement.setString(1, ingredient.getName());
                    ResultSet resultSet = statement.executeQuery();
                    int num = resultSet.getInt(1);
                    statement.close();

                    if (num == 1) { //si esa harina solo tiene un proveedor la modificamos asignéndole el genérico
                        String sql = "UPDATE floursProvider SET provider = ? WHERE flour = ?";
                        PreparedStatement statement2 = App.getAppDao().getConnection().prepareStatement(sql);
                        statement2.setString(1, "Proveedor generico");
                        statement2.setString(2, ingredient.getName());
                        statement2.executeUpdate();
                        statement2.close();

                    } else { //si tiene varios lo eliminamos y le dejamos los otros
                        String sql = "DELETE FROM floursProvider WHERE provider = ? AND flour = ?";
                        PreparedStatement statement2 = App.getAppDao().getConnection().prepareStatement(sql);
                        statement2.setString(1, provider.getName());
                        statement2.setString(2, ingredient.getName());
                        statement2.executeUpdate();
                        statement2.close();

                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        });
    }

    public boolean updateProvider(Provider provider) throws SQLException {
        String sql = "UPDATE providers SET name = ?, direction = ?, phone = ?, observations = ?, is_active = ? WHERE idProvider = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, provider.getName());
        statement.setString(2, provider.getDirection());
        statement.setInt(3, Integer.parseInt(provider.getPhone()));
        statement.setString(4, provider.getObservations());
        statement.setInt(5, provider.getIsActive());
        statement.setInt(6, provider.getIdProvider());
        int rows = statement.executeUpdate();
        statement.close();

        return rows != 0;
    }
}
