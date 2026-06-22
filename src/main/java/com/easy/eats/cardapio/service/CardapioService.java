package com.easy.eats.cardapio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.easy.eats.cardapio.model.Cardapio;
import com.easy.eats.cardapio.repository.CardapioRepository;

@Service
public class CardapioService {
    
    private final CardapioRepository repository;

    public CardapioService(CardapioRepository repository) {
        this.repository = repository;
    }

    public List<Cardapio> listarTodos() {
        return repository.findAll();
    }

    public Cardapio buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cardápio não encontrado"));
    }

    public Cardapio salvar(Cardapio cardapio) {
        return repository.save(cardapio);
    }

    public Cardapio atualizar(Integer id, Cardapio cardapio) {

        Cardapio cardapioExistente = buscarPorId(id);

        cardapioExistente.setNome(cardapio.getNome());
        cardapioExistente.setDescricao(cardapio.getDescricao());
        cardapioExistente.setFlAtivo(cardapio.getFlAtivo());
        cardapioExistente.setDtAlteracao(cardapio.getDtAlteracao());

        return repository.save(cardapioExistente);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }
}
