package com.wkallil.ui;

import com.wkallil.persistence.entity.BoardColumnEntity;
import com.wkallil.persistence.entity.BoardColumnKindEnum;
import com.wkallil.persistence.entity.BoardEntity;
import com.wkallil.service.BoardQueryService;
import com.wkallil.service.BoardService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.wkallil.persistence.config.ConnectionConfig.getConnection;
import static com.wkallil.persistence.entity.BoardColumnKindEnum.*;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);

    public void execute() throws SQLException{
        System.out.println("Bem vindo ao gerenciador de boards, escolha a opção desejada: ");
        var option = -1;
        while (true) {
            System.out.println("1 - Criar um novo board");
            System.out.println("2 - Selecionar um board existente");
            System.out.println("3 - excluir um board");
            System.out.println("4 - Sair");
            option = scanner.nextInt();
            switch (option) {
                case 1 -> createBoard();
                case 2 -> selectBoard();
                case 3 -> deleteBoard();
                case 4 -> System.exit(0);
                default -> System.out.println("Opção inválida, informe uma opção do menu");
            }
        }
    }


    private void createBoard() throws SQLException{
        var entity = new BoardEntity();
        System.out.println("Informe o nome do seu board: ");
        entity.setName(scanner.next());

        System.out.println("Seu board terá colunas além das 3 padrões? Se sim informe quantas, senão digite '0' ");
        var additionalColumns = scanner.nextInt();

        List<BoardColumnEntity> columns = new ArrayList<>();

        System.out.println("Informe o nome da coluna inicial do board: ");
        var initialColumnName = scanner.next();
        var initialColumn = createColumn(initialColumnName, INITIAL, 0);
        columns.add(initialColumn);

        for (int i = 0; i < additionalColumns; i++) {
            System.out.println("Informe o nome da coluna de tarefa pendente do board: ");
            var pendingColumnName = scanner.next();
            var pendingColumn = createColumn(pendingColumnName, PENDING, i + 1);
            columns.add(pendingColumn);
        }


        System.out.println("Informe o nome da coluna final: ");
        var finalColumnName = scanner.next();
        var finalColumn = createColumn(finalColumnName, FINAL, additionalColumns + 1);
        columns.add(finalColumn);

        System.out.println("Informe o nome da coluna de cancelamento do board: ");
        var cancelColumnName = scanner.next();
        var cancelColumn = createColumn(cancelColumnName, CANCEL, additionalColumns + 2);
        columns.add(cancelColumn);

        entity.setBoardColumns(columns);
        try (var connection = getConnection()) {
            var service = new BoardService(connection);
            service.insert(entity);
            System.out.println("Board criado com sucesso");
        }

    }

    private void selectBoard() throws SQLException{
        System.out.println("Informe o id do board que deseja selecionionar:");
        var id = scanner.nextLong();
        try(var connection = getConnection()) {
            var queryService = new BoardQueryService(connection);
            var optional = queryService.findById(id);
            optional.ifPresentOrElse(
                    b -> new BoardMenu(b).execute(),
                    () -> System.out.printf("Não foi encontrado um board com id %s\n", id)
            );
                    }

    }

    private void deleteBoard() throws SQLException {
        System.out.println("Informe o id do board que será excluído: ");
        var id = scanner.nextLong();
        try(var connection = getConnection()) {
        var service = new BoardService(connection);
        if(service.delete(id)) {
            System.out.printf("O Board com id %s excluído com sucesso\n", id);
        } else {
            System.out.printf("Não foi encontrado um board com id %s\n", id);
        }
    }
}

    private BoardColumnEntity createColumn(final String name, final BoardColumnKindEnum kind, final int order) {
        var boardColumn = new BoardColumnEntity();
        boardColumn.setName(name);
        boardColumn.setKind(kind);
        boardColumn.setOrder(order);
        return boardColumn;

    }
}
