package com.wkallil.service;

import com.wkallil.dto.BoardColumnInfoDTO;
import com.wkallil.dto.CardDetailsDTO;
import com.wkallil.exception.CardBlockedException;
import com.wkallil.exception.CardFinishedException;
import com.wkallil.exception.EntityNotFoundException;
import com.wkallil.persistence.dao.BlockDAO;
import com.wkallil.persistence.dao.CardDAO;
import com.wkallil.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.wkallil.persistence.entity.BoardColumnKindEnum.CANCEL;
import static com.wkallil.persistence.entity.BoardColumnKindEnum.FINAL;

@AllArgsConstructor
public class CardService {

    private final Connection connection;

    public CardEntity insert(final CardEntity entity) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            dao.insert(entity);
            connection.commit();
            return entity;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void moveToNextColumn(final Long cardId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
            );
            if (dto.blocked()) {
                var message = "O card %s está bloqueado, é necessário desbloqueá-lo para mover".formatted(dto.id());
                throw new CardBlockedException(message);
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId())).findFirst().orElseThrow(
                            () -> new IllegalStateException("O card informado pertence a outro board")
                    );
            if (currentColumn.kind().equals(FINAL)) {
                throw new CardFinishedException("O card já foi finalizado");
            }
            var nextColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.order() == currentColumn.order() + 1)
                    .findFirst().orElseThrow(() -> new IllegalStateException("O card está cancelado"));
            dao.moveToColumn(nextColumn.id(), cardId);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }

    }

    public void cancelCard(final Long cardId, final Long cancelColumnId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId))
            );
            if (dto.blocked()) {
                var message = "O card %s está bloqueado, é necessário desbloqueá-lo para mover".formatted(dto.id());
                throw new CardBlockedException(message);
            }
            var currentColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.id().equals(dto.columnId())).findFirst().orElseThrow(
                            () -> new IllegalStateException("O card informado pertence a outro board")
                    );
            if (currentColumn.kind().equals(FINAL)) {
                throw new CardFinishedException("O card já foi finalizado");
            }
            boardColumnsInfo.stream()
                    .filter(bc -> bc.order() == currentColumn.order() + 1)
                    .findFirst().orElseThrow(() -> new IllegalStateException("O card está cancelado"));
            dao.moveToColumn(cancelColumnId, cardId);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void block(final Long id, final String reason, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(id))
            );
            if (dto.blocked()) {
                var message = "O card %s já está bloqueado".formatted(dto.id());
                throw new CardBlockedException(message);
            }
            var currentColumn = boardColumnsInfo.stream().filter(bc ->
                            bc.id().equals(dto.columnId()))
                    .findFirst().orElseThrow();
            if (currentColumn.kind().equals(FINAL) || currentColumn.kind().equals(CANCEL)) {
                var message = "O card está em uma coluna do tipo %s e não pode ser bloqueado"
                        .formatted(currentColumn.kind());
                throw new IllegalStateException(message);
            }
            var blockDao = new BlockDAO(connection);
            blockDao.block(reason, id);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }

    }

    public void unblock(final Long id, final String reason) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(id))
            );
            if (!dto.blocked()) {
                var message = "O card %s não está bloqueado".formatted(dto.id());
                throw new CardBlockedException(message);
            }
            var blockDao = new BlockDAO(connection);
            blockDao.unblock(reason, id);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

}
