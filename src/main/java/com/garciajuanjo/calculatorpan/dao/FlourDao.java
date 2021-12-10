package com.garciajuanjo.calculatorpan.dao;

import com.garciajuanjo.calculatorpan.App;
import com.garciajuanjo.calculatorpan.domain.Flour;
import com.garciajuanjo.calculatorpan.domain.Provider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.garciajuanjo.calculatorpan.domain.Flour.TypeFlour;
import static com.garciajuanjo.calculatorpan.domain.Flour.TypeIngredient;

public class FlourDao {

    /*
    Para resetear el id autoincrement y ponerlo al valor que queramos
    update sqlite_sequence set seq=3 where name='flours'

    // para cuando queremos borrar o modificar si tenemos claves foraneas. Tenemos primero que desactivar
       con OFF y cuando hagamos el borrado o modificación activarlo con ON
    PRAGMA foreign_keys = OFF;
     */

    /*
    Para resetear la base de datos ejecutamos esto:
    DELETE FROM flours;
    DELETE FROM ingredients;
    DELETE FROM providers;
    DELETE FROM prefermentos;
    DELETE FROM recipes;
    DELETE FROM floursProvider;
    DELETE FROM ingredientRecipe;
    DELETE FROM ingredientProvider;
    DELETE FROM floursPrefermento;

    update sqlite_sequence set seq=0 where name='ingredients';
    update sqlite_sequence set seq=0 where name='flours';
    update sqlite_sequence set seq=0 where name='providers';
    update sqlite_sequence set seq=0 where name='recipes';
    update sqlite_sequence set seq=0 where name='prefermentos';
     */

    public void addFlour(Flour flour) throws SQLException {
        String sql = "INSERT INTO flours (name, description, strength, protein, typeFlour, is_active) VALUES " +
                "(?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1,flour.getName());
        statement.setString(2, flour.getDescription());
        statement.setString(3, String.valueOf(flour.getStrength()));
        statement.setString(4, String.valueOf(flour.getProtein()));
        statement.setString(5, flour.getTypeFlour().name());
        statement.setInt(6, flour.getIsActive());
        statement.executeUpdate();

        addFlourProvider(flour);
        statement.close();

    }

    public ObservableList<Flour> getAllFlours(boolean is_active) throws SQLException {
        String sql;
        if (is_active) {
            sql = "SELECT * FROM flours WHERE is_active = 1 ORDER BY name";
        } else {
            sql = "SELECT * FROM flours ORDER BY name";
        }

        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        ObservableList<Flour> flours = FXCollections.observableArrayList();
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()){
            Flour flour = new Flour();

            flour.setIdIngredient( resultSet.getInt(1));
            flour.setName( resultSet.getString(2));
            flour.setDescription( resultSet.getString(3));
            flour.setStrength(Integer.parseInt(resultSet.getString(4)));
            flour.setProtein(Float.parseFloat(resultSet.getString(5)));
            flour.setTypeFlour(TypeFlour.valueOf(resultSet.getString(6)));
            flour.setTypeIngredient(TypeIngredient.HARINA);
            flour.setIsActive(resultSet.getInt(8));

            flour.setProviders(getProvidersFlour(flour));

            flours.add(flour);
        }
        statement.close();
        return flours;
    }

    public Flour getFlour(String name) throws SQLException {
        String sql = "SELECT * FROM flours WHERE name = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();

        Flour flour = new Flour();

        flour.setIdIngredient( resultSet.getInt(1));
        flour.setName( resultSet.getString(2));
        flour.setDescription( resultSet.getString(3));
        flour.setStrength(Integer.parseInt(resultSet.getString(4)));
        flour.setProtein(Float.parseFloat(resultSet.getString(5)));
        flour.setTypeFlour(TypeFlour.valueOf(resultSet.getString(6)));
        flour.setTypeIngredient(TypeIngredient.HARINA);
        flour.setIsActive(resultSet.getInt(8));

        flour.setProviders(getProvidersFlour(flour));

        statement.close();
        return flour;
    }

    public int getIdFlour(String name) throws SQLException{
        String sql = "SELECT idFlour FROM flours WHERE name = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();

        int id = 0;
        while (resultSet.next()){
             id = resultSet.getInt(1);
        }

        resultSet.close();

        return id;
    }

    public boolean existFlour(String name, boolean is_active) throws SQLException {
        String sql;
        if (is_active){
            sql = "SELECT * FROM flours WHERE name = ? AND is_active = 1";
        } else {
            sql = "SELECT * FROM flours WHERE name = ? AND is_active = 0";
        }

        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        boolean result = resultSet.next();
        statement.close();
        return result;
    }

    private ObservableList<Provider> getProvidersFlour(Flour flour) throws SQLException {
        String sql = "SELECT provider FROM floursProvider WHERE flour = (?)";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        ObservableList<Provider> providers = FXCollections.observableArrayList();
        statement.setString(1, flour.getName());
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()){
            String nameProvider = resultSet.getString(1);
            Provider provider = App.getProviderDao().getProvider(nameProvider);
            providers.add(provider);
        }
        statement.close();
        return providers;
    }

    public boolean updateFlour(Flour flour) throws SQLException {
        String sql = "UPDATE flours SET name = ?, description = ?, strength = ?, protein = ?, typeFlour = ?, is_active = ?" +
                " WHERE idFlour = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);

        statement.setString(1, flour.getName());
        statement.setString(2, flour.getDescription());
        statement.setInt(3, flour.getStrength());
        statement.setFloat(4, flour.getProtein());
        statement.setString(5, flour.getTypeFlour().toString());
        statement.setInt(6, flour.getIsActive());
        statement.setInt(7, flour.getIdIngredient());
        int rows = statement.executeUpdate();
        statement.close();

        deleteProvidersFlour(flour);
        addFlourProvider(flour);

        return rows != 0;
    }

    public boolean deleteFlour(Flour flour) throws SQLException {
        String sql = "UPDATE flours SET is_active = 0 WHERE name = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, flour.getName());
        int rows = statement.executeUpdate();
        statement.close();

        deleteProvidersFlour(flour);

        return rows != 0;
    }

    private void deleteProvidersFlour(Flour flour) throws SQLException {
        String sql = "DELETE FROM floursProvider WHERE flour = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, flour.getName());
        statement.executeUpdate();

        statement.close();
    }

    private void addFlourProvider(Flour flour) throws SQLException {
        String sql = "INSERT INTO floursProvider (provider, flour) VALUES (?,?)";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);

        flour.getProviders().forEach(provider -> {
            try {
                statement.setString(1, provider.getName());
                statement.setString(2, flour.getName());
                statement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        statement.close();
    }


}
