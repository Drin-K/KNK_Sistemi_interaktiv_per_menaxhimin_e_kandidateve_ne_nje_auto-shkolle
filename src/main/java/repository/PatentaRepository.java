package repository;


import models.Dto.patenta.CreatePatentaDto;
import models.Dto.patenta.UpdatePatentaDto;

import models.Patenta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PatentaRepository extends BaseRepository<Patenta, CreatePatentaDto, UpdatePatentaDto> {
    public PatentaRepository() {
        super("Patenta");
    }

    public Patenta fromResultSet(ResultSet result) throws SQLException {
        return Patenta.getInstance(result);
    }

    public Patenta create(CreatePatentaDto patentaDto) {
        String query = """
                INSERT INTO
                Patenta(ID_Kandidat, ID_Kategori, Data_Leshimit, Statusi)
                VALUES(?,?,?,?);
                """;
        try {
            PreparedStatement pstm = this.connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            pstm.setInt(1, patentaDto.getIdKandidat());
            pstm.setInt(2, patentaDto.getIdKategori());
            pstm.setObject(3, patentaDto.getDataLeshimit());
            pstm.setString(4, patentaDto.getStatusi());

            ResultSet res = pstm.getGeneratedKeys();
            if (res.next()) {
                int id = res.getInt(1);
                return this.getById(id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public Patenta update(UpdatePatentaDto patentaDto) {
        StringBuilder query = new StringBuilder("Update Patenta SET ");
        ArrayList<Object> params = new ArrayList<>();
        if (patentaDto.getIdKandidat() != 0) {
            query.append("ID_Kandidat=?, ");
            params.add(patentaDto.getIdKandidat());
        }
        if (patentaDto.getIdKategori() != 0) {
            query.append("ID_Kategori=?, ");
            params.add(patentaDto.getIdKategori());
        }
        if (patentaDto.getDataLeshimit() != null) {
            query.append("Data_Leshimit=?, ");
            params.add(patentaDto.getDataLeshimit());
        }
        if (patentaDto.getStatusi() != null) {
            query.append("Statusi=?, ");
            params.add(patentaDto.getStatusi());
        }
        if (params.isEmpty()) {
            return getById(patentaDto.getId());
        }
        query.setLength(query.length() - 2);//me largu "? "->se paraqet gabim ne sintakse
        query.append(" WHERE id= ?");
        params.add(patentaDto.getId());
        try {
            PreparedStatement pstm = this.connection.prepareStatement(query.toString());
            for (int i = 0; i < params.size(); i++) {
                pstm.setObject(i + 1, params.get(i));
            }
            int updated = pstm.executeUpdate();
            if (updated == 1) {
                return this.getById(patentaDto.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating the license.", e);
        }
        return null;
    }

    public List<Patenta> getLicensesIssued() throws SQLException {
        List<Patenta> patentat = new ArrayList<>();
        String query = "SELECT * FROM Patenta WHERE Statusi = 'E lëshuar'";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            patentat.add(fromResultSet(resultSet));
        }
        return patentat;
    }

    public boolean aprovoPatenten(int kandidatId) throws SQLException {
        String query = "UPDATE Patenta SET Statusi = 'E lëshuar' WHERE ID_Kandidat = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, kandidatId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new SQLException("Error approving the license.", e);
        }
    }


}

