package com.ufes.delivery.application.usecase;

import com.ufes.delivery.application.dto.CriarPedidoDTO;
import com.ufes.delivery.application.dto.ItemDTO;
import com.ufes.delivery.application.port.out.PedidoRepositoryOutputPort;
import com.ufes.delivery.domain.entity.Pedido;
import com.ufes.delivery.domain.entity.StatusPedido;
import com.ufes.delivery.infrastructure.repository.PedidoRepositoryEmMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CriarPedidoUseCase — Application")
class CriarPedidoUseCaseTest {

    private CriarPedidoUseCase useCase;
    private PedidoRepositoryOutputPort repository;

    @BeforeEach
    void setUp() {
        repository = new PedidoRepositoryEmMemoria();
        useCase = new CriarPedidoUseCase(repository, 10.0);
    }

    @Test
    @DisplayName("Deve criar pedido com itens e persistir no repositorio")
    void deveCriarPedidoComItens() {
        CriarPedidoDTO dto = new CriarPedidoDTO(
                "Ana", "Bronze", 0,
                "Rua B", "Jardim", "Castelo",
                LocalDateTime.now(),
                List.of(new ItemDTO("Livro", 1, 30.0, "Lazer"))
        );

        Pedido pedido = useCase.executar(dto);

        assertNotNull(pedido);
        assertEquals("Ana", pedido.getCliente().getNome());
        assertEquals(1, pedido.getItens().size());
        assertEquals(StatusPedido.CRIADO, pedido.getStatus());
        assertEquals(1, repository.listarTodos().size());
    }

    @Test
    @DisplayName("DTO nulo deve lancar NullPointerException")
    void deveRejeitarDtoNulo() {
        assertThrows(NullPointerException.class, () -> useCase.executar(null));
    }

    @Test
    @DisplayName("Repositorio nulo no construtor deve lancar NullPointerException")
    void deveRejeitarRepositorioNulo() {
        assertThrows(NullPointerException.class,
                () -> new CriarPedidoUseCase(null, 10.0));
    }
}
