package com.easy.eats.notificacao.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.notificacao.model.Notificacao;
import com.easy.eats.notificacao.repository.NotificacaoRepository;
import com.easy.eats.security.SecurityUtils;

@Service
public class NotificacaoService {

    private static final int LIMITE_LISTAGEM = 20;

    @Autowired
    private NotificacaoRepository repository;

    @Autowired
    private EmpresaRepository empresaRepository;

    public List<Notificacao> listar() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAllByEmpresaIdIsNullOrderByDtCriacaoDesc(PageRequest.of(0, LIMITE_LISTAGEM));
        }
        return repository.findAllByEmpresaIdOrderByDtCriacaoDesc(SecurityUtils.getEmpresaId(), PageRequest.of(0, LIMITE_LISTAGEM));
    }

    public Notificacao marcarComoLida(Integer id) {
        Notificacao notificacao = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada"));
        notificacao.setLida(true);
        return repository.save(notificacao);
    }

    public void marcarTodasComoLidas() {
        List<Notificacao> notificacoes = SecurityUtils.isSuperadmin()
                ? repository.findAllByEmpresaIdIsNull()
                : repository.findAllByEmpresaId(SecurityUtils.getEmpresaId());

        notificacoes.forEach(n -> n.setLida(true));
        repository.saveAll(notificacoes);
    }

    /** Notificação operacional de uma empresa cliente (novo pedido, estoque crítico etc.). */
    public void notificarEmpresa(Integer empresaId, String icone, String cor, String titulo, String descricao) {
        Notificacao notificacao = new Notificacao();
        notificacao.setEmpresa(empresaRepository.getReferenceById(empresaId));
        notificacao.setIcone(icone);
        notificacao.setCor(cor);
        notificacao.setTitulo(titulo);
        notificacao.setDescricao(descricao);
        notificacao.setLida(false);
        notificacao.setDtCriacao(LocalDateTime.now());
        repository.save(notificacao);
    }

    /** Notificação de plataforma, visível só para SUPERADMIN (ex.: nova empresa cadastrada). */
    public void notificarPlataforma(String icone, String cor, String titulo, String descricao) {
        Notificacao notificacao = new Notificacao();
        notificacao.setIcone(icone);
        notificacao.setCor(cor);
        notificacao.setTitulo(titulo);
        notificacao.setDescricao(descricao);
        notificacao.setLida(false);
        notificacao.setDtCriacao(LocalDateTime.now());
        repository.save(notificacao);
    }
}
