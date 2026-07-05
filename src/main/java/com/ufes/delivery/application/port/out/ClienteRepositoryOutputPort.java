package com.ufes.delivery.application.port.out;
 
import com.ufes.delivery.domain.entity.Cliente;
import java.util.List;
import java.util.Optional;
 
public interface ClienteRepositoryOutputPort {
 
    void salvar(Cliente cliente);
 
    Optional<Cliente> buscarPorId(String id);
 
    List<Cliente> listarTodos();
}
 