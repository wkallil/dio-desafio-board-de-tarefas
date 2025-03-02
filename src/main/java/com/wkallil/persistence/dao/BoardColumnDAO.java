package com.wkallil.persistence.dao;

import com.mysql.cj.jdbc.StatementImpl;
import com.wkallil.dto.BoardColumnDTO;
import com.wkallil.persistence.entity.BoardColumnEntity;
import com.wkallil.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.wkallil.persistence.entity.BoardColumnKindEnum.findByName;
import static java.util.Objects.isNull;

@AllArgsConstructor
public class BoardColumnDAO {

    private final Connection connection;

    public BoardColumnEntity insert(final BoardColumnEntity entity) {
        var sql = "Insert INTO BOARDS_COLUMNS (name, `order`, kind, board_id) VALUES (?, ?, ?, ?)";
        try (var statement = connection.prepareStatement(sql)) {
            var i = 1;
            statement.setString(i++, entity.getName());
            statement.setInt(i++, entity.getOrder());
            statement.setString(i++, entity.getKind().name());
            statement.setLong(i, entity.getBoard().getId());
            statement.executeUpdate();
            if (statement instanceof StatementImpl impl) {
                entity.setId(impl.getLastInsertID());
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<BoardColumnEntity> findByBoardId(Long boardId) throws SQLException {
        List<BoardColumnEntity> entities = new ArrayList<>();
        var sql = "SELECT id, name, `order`, kind FROM BOARDS_COLUMNS WHERE board_id = ? ORDER BY `order`";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while (resultSet.next()) {
                var entity = new BoardColumnEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                entity.setOrder(resultSet.getInt("order"));
                entity.setKind(findByName(resultSet.getString("kind")));
                entities.add(entity);
            }
            return entities;
        }
    }

    public List<BoardColumnDTO> findByBoardIdWithDetails(Long boardId) throws SQLException {
        List<BoardColumnDTO> dtos = new ArrayList<>();
        var sql = """
                SELECT bc.id, 
                    bc.name, 
                    bc.kind,
                    (SELECT COUNT(c.id) 
                                FROM CARDS c 
                                WHERE c.board_column_id = bc.id) cards_amount
                 FROM BOARDS_COLUMNS bc
                 WHERE board_id = ? 
                 ORDER BY `order`
                """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while (resultSet.next()) {
                var dto = new BoardColumnDTO(resultSet.getLong("bc.id"),
                        resultSet.getString("bc.name"),
                        findByName(resultSet.getString("bc.kind")),
                        resultSet.getInt("cards_amount"));
                dtos.add(dto);
            }
            return dtos;
        }
    }

    public Optional<BoardColumnEntity> findById(Long boardId) throws SQLException {
        List<BoardColumnEntity> entities = new ArrayList<>();
        var sql =
                """
                        SELECT  bc.name,
                                bc.kind,
                                c.id,
                                c.title,
                                c.description
                           FROM BOARDS_COLUMNS bc
                           LEFT JOIN JOIN CARDS c
                           ON c.board_column_id = bc.id
                         WHERE bc.id = ?
                        """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if (resultSet.next()) {
                var entity = new BoardColumnEntity();
                entity.setName(resultSet.getString("bc.name"));
                entity.setKind(findByName(resultSet.getString("bc.kind")));

                do {
                    if (isNull(resultSet.getString("c.title"))) {
                        break;
                    }
                    var card = new CardEntity();
                    card.setId(resultSet.getLong("c.id"));
                    card.setTitle(resultSet.getString("c.title"));
                    card.setDescription(resultSet.getString("c.description"));
                    entity.getCards().add(card);
                } while (resultSet.next());
                return Optional.of(entity);
            }

            return Optional.empty();
        }
    }

}
