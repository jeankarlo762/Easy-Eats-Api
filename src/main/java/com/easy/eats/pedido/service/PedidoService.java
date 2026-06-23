package com.easy.eats.pedido.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.pedido.bst.ArvorePedido;
import com.easy.eats.pedido.model.Pedido;
import com.easy.eats.pedido.repository.PedidoRepository;
import com.easy.eats.venda.model.Venda;

import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository repository;

    public Pedido salvar(Pedido pedido) {
        return repository.save(pedido);
    }

    public List<Pedido> listarTodos() {
        return repository.findAll();
    }

    public Optional<Pedido> listarArvorePedido(Integer id) {
        List<Pedido> todosPedidos = repository.findAll();

        if (todosPedidos.isEmpty()) {
            return Optional.empty();
        }

        ArvorePedido arvore = new ArvorePedido();

        for (Pedido p : todosPedidos) {
            arvore.inserir(p);
        }

        Pedido encontrado = arvore.buscar(id);

        return Optional.ofNullable(encontrado);
    }

    public Optional<Pedido> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }
}