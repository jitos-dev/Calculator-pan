package com.garciajuanjo.calculatorpan.dao;

import com.garciajuanjo.calculatorpan.App;
import com.garciajuanjo.calculatorpan.domain.Flour;
import com.garciajuanjo.calculatorpan.domain.FlourPrefermento;
import com.garciajuanjo.calculatorpan.domain.Prefermento;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PrefermentoDao {

    public void addPrefermento(Prefermento prefermento) throws SQLException {
        String sql = "INSERT INTO prefermentos (name, description, yeast, watter, is_active) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);

        statement.setString(1, prefermento.getName());
        statement.setString(2, prefermento.getDescription());
        statement.setString(3, String.valueOf(prefermento.getYeast()));
        statement.setInt(4, prefermento.getPercentageWatter());
        statement.setInt(5, prefermento.getIsActive());
        statement.executeUpdate();

        addFloursPrefermento(prefermento);
        statement.close();
    }

    public ObservableList<Prefermento> getAllPrefermentos(boolean is_acive) throws SQLException {
        String sql;
        if (is_acive) {
            sql = "SELECT * FROM prefermentos WHERE is_active = 1 ORDER BY name";
        } else {
            sql = "SELECT * FROM prefermentos ORDER BY name";
        }

        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        ObservableList<Prefermento> prefermentos = FXCollections.observableArrayList();
        ResultSet result = statement.executeQuery();

        while (result.next()){
            Prefermento prefermento = new Prefermento();
            prefermento.setIdIngredient(result.getInt(1));
            prefermento.setName(result.getString(2));
            prefermento.setDescription(result.getString(3));
            prefermento.setYeast(Float.parseFloat(result.getString(4)));
            prefermento.setPercentageWatter(Integer.parseInt(result.getString(5)));
            prefermento.setIsActive(result.getInt(7));

            //rellenamos la lista de harinas del prefermento con su porcentage de cada una
            ObservableList<FlourPrefermento> flourPrefermentos = getAllFloursPrefermento(prefermento);
            prefermento.setListFlour(flourPrefermentos);

            prefermentos.add(prefermento);
        }
        statement.close();

        return prefermentos;
    }

    public Boolean existPrefermento(String name, boolean is_active) throws SQLException {
        String sql;
        if (is_active) {
            sql = "SELECT * FROM prefermentos WHERE name = ? AND is_active = 1";
        } else {
            sql = "SELECT * FROM prefermentos WHERE name = ? AND is_active = 0";
        }

        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();
        boolean result = resultSet.next();
        statement.close();

        return result;
    }

    public Prefermento getPrefermento(String name) throws SQLException{
        String sql = "SELECT * FROM prefermentos WHERE name = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        ObservableList<FlourPrefermento> flourPrefermentos = FXCollections.observableArrayList();
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();

        Prefermento prefermento = new Prefermento();

        prefermento.setIdIngredient(resultSet.getInt(1));
        prefermento.setName(resultSet.getString(2));
        prefermento.setDescription(resultSet.getString(3));
        prefermento.setYeast(Float.parseFloat(resultSet.getString(4)));
        prefermento.setPercentageWatter(Integer.parseInt(resultSet.getString(5)));
        prefermento.setIsActive(resultSet.getInt(7));

        //rellenamos la lista de harinas del prefermento con su porcentage de cada una
        flourPrefermentos.clear();
        flourPrefermentos.addAll(getAllFloursPrefermento(prefermento));
        prefermento.setListFlour(flourPrefermentos);
        resultSet.close();

        return prefermento;
    }

    public void addFloursPrefermento(Prefermento prefermento) throws SQLException {
        String sql = "INSERT INTO floursPrefermento (prefermento, flour, percentage) VALUES (?, ?, ?)";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);

        prefermento.getListFlour().forEach(flourPrefermento -> {
            try {
                statement.setString(1, prefermento.getName());
                statement.setString(2, flourPrefermento.getFlour().getName());
                statement.setInt(3, flourPrefermento.getPercentage());
                statement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        statement.close();
    }

    private ObservableList<FlourPrefermento> getAllFloursPrefermento(Prefermento prefermento) throws SQLException {
        String sql = "SELECT * FROM floursPrefermento WHERE prefermento = ? ORDER BY percentage";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        ObservableList<FlourPrefermento> flourPrefermentos = FXCollections.observableArrayList();
        statement.setString(1, prefermento.getName());
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()){
            FlourPrefermento flourPrefermento = new FlourPrefermento();
            String nameFlour = resultSet.getString(2);
            Flour flour = App.getFlourDao().getFlour(nameFlour);
            flourPrefermento.setFlour(flour);
            flourPrefermento.setPercentage(resultSet.getInt(3));
            flourPrefermento.setIsActive(flour.getIsActive());
            flourPrefermentos.add(flourPrefermento);
        }

        statement.close();
        return flourPrefermentos;
    }

    public int getIdPrefermento(String name) throws SQLException{
        String sql = "SELECT idPrefermento FROM prefermentos WHERE name = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();

        int id = resultSet.getInt(1);
        resultSet.close();

        return id;
    }

    public boolean deletePrefermento(Prefermento prefermento) throws SQLException {
        String sql = "UPDATE prefermentos SET is_active = 0 WHERE idPrefermento = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setInt(1, prefermento.getIdIngredient());
        int rows = statement.executeUpdate();

        deleteFloursPrefermento(prefermento);
        statement.close();
        return rows != 0;
    }

    private void deleteFloursPrefermento(Prefermento prefermento) throws SQLException {
        String sql = "DELETE FROM floursPrefermento WHERE prefermento = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, prefermento.getName());
        statement.executeUpdate();
        statement.close();
    }

    public boolean updatePrefermento(Prefermento prefermento) throws SQLException {
        String sql = "UPDATE prefermentos SET name = ?, description = ?, yeast = ?, watter = ?, is_active = ? WHERE idPrefermento = ?";
        PreparedStatement statement = App.getAppDao().getConnection().prepareStatement(sql);
        statement.setString(1, prefermento.getName());
        statement.setString(2, prefermento.getDescription());
        statement.setFloat(3, prefermento.getYeast());
        statement.setInt(4, prefermento.getPercentageWatter());
        statement.setInt(5, prefermento.getIsActive());
        statement.setInt(6, prefermento.getIdIngredient());
        int rows = statement.executeUpdate();

        deleteFloursPrefermento(prefermento);
        addFloursPrefermento(prefermento);
        statement.close();

        return rows != 0;
    }
}
