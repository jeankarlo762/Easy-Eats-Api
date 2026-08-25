package com.easy.eats.comanda.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.easy.eats.comanda.model.Comanda;
import com.easy.eats.comanda.model.ItensComandaRequest;
import com.easy.eats.comanda.repository.ComandaRepository;
import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.itemVenda.model.ItemVenda;
import com.easy.eats.itemVenda.service.ItemVendaService;
import com.easy.eats.mesa.model.Mesa;
import com.easy.eats.mesa.repository.MesaRepository;
import com.easy.eats.pagamento.model.Pagamento;
import com.easy.eats.pagamento.service.PagamentoService;
import com.easy.eats.security.SecurityUtils;
import com.easy.eats.usuario.repository.UsuarioRepository;
import com.easy.eats.venda.model.Venda;
import com.easy.eats.venda.service.VendaService;

@Service
public class ComandaService {

    private static final int MAX_TENTATIVAS_NUMERO = 5;

    @Autowired
    ComandaRepository repository;

    @Autowired
    MesaRepository mesaRepository;

    @Autowired
    EmpresaRepository empresaRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    VendaService vendaService;

    @Autowired
    ItemVendaService itemVendaService;

    @Autowired
    PagamentoService pagamentoService;

    public Comanda abrir(Comanda dados) {
        Integer empresaId = SecurityUtils.getEmpresaId();
        Mesa mesa = mesaDaMesmaEmpresa(dados.getMesa(), empresaId);

        if (!"LIVRE".equalsIgnoreCase(mesa.getStatus())) {
            throw new IllegalArgumentException("A mesa não está livre");
        }

        // Número sequencial por empresa: sob concorrência (dois garçons abrindo
        // mesa ao mesmo tempo) a constraint única (empresa_id, numero) rejeita a
        // segunda gravação — tenta de novo com o próximo número em vez de falhar.
        for (int tentativa = 0; tentativa < MAX_TENTATIVAS_NUMERO; tentativa++) {
            try {
                Comanda comanda = new Comanda();
                comanda.setNumero(proximoNumero(empresaId));
                comanda.setStatus("ABERTA");
                comanda.setNomeCliente(dados.getNomeCliente());
                comanda.setValorTotal(0.0);
                comanda.setDtAbertura(LocalDateTime.now());
                comanda.setMesa(mesa);
                comanda.setUsuarioAbertura(
                        usuarioRepository.getReferenceById(SecurityUtils.getUsuarioAutenticado().getUsuarioId()));
                comanda.setEmpresa(empresaRepository.getReferenceById(empresaId));

                Comanda salva = repository.save(comanda);

                mesa.setStatus("OCUPADA");
                mesaRepository.save(mesa);

                return salva;
            } catch (DataIntegrityViolationException conflito) {
                // outra comanda foi aberta com o mesmo número entre a leitura e a
                // gravação — tenta novamente com o número seguinte.
            }
        }
        throw new IllegalStateException("Não foi possível abrir a comanda, tente novamente");
    }

    public List<Comanda> listar(String status) {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        Integer empresaId = SecurityUtils.getEmpresaId();
        return status != null && !status.isBlank()
                ? repository.findAllByEmpresaIdAndStatus(empresaId, status)
                : repository.findAllByEmpresaId(empresaId);
    }

    public Comanda buscarPorId(Integer id) {
        Comanda comanda = SecurityUtils.isSuperadmin()
                ? repository.findById(id).orElse(null)
                : repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId()).orElse(null);

        if (comanda == null) {
            throw new RuntimeException("Comanda não encontrada");
        }
        return comanda;
    }

    public Venda adicionarItens(Integer comandaId, ItensComandaRequest request) {
        Comanda comanda = buscarPorId(comandaId);
        if (!"ABERTA".equalsIgnoreCase(comanda.getStatus())) {
            throw new IllegalArgumentException("A comanda não está aberta");
        }

        Venda venda = new Venda();
        venda.setStatus("Aguardando");
        venda.setTipo("Mesa");
        venda.setOrigem("COMANDA");
        venda.setMesa(comanda.getMesa());
        venda.setUsuario(request.getUsuario());
        venda.setComanda(comanda);

        Venda vendaCriada = vendaService.criar(venda);

        for (ItemVenda item : request.getItens()) {
            validarItem(item);
            item.setId(null);
            Venda referenciaVenda = new Venda();
            referenciaVenda.setId(vendaCriada.getId());
            item.setVenda(referenciaVenda);
            itemVendaService.criar(item);
        }

        return vendaService.buscarPorId(vendaCriada.getId()).orElse(vendaCriada);
    }

    public Comanda fechar(Integer id, String metodoPagamento) {
        Comanda comanda = buscarPorId(id);

        if ("FECHADA".equalsIgnoreCase(comanda.getStatus())) {
            throw new IllegalArgumentException("Comanda já está fechada");
        }
        if (metodoPagamento == null || metodoPagamento.isBlank()) {
            throw new IllegalArgumentException("Informe a forma de pagamento");
        }

        double total = somaItens(comanda);
        if (total <= 0) {
            throw new IllegalArgumentException("A comanda não possui itens para fechar");
        }

        comanda.setValorTotal(total);
        comanda.setStatus("FECHADA");
        comanda.setDtFechamento(LocalDateTime.now());
        Comanda salva = repository.save(comanda);

        Pagamento pagamento = new Pagamento();
        pagamento.setComanda(salva);
        pagamento.setValor(total);
        pagamento.setMetodo(metodoPagamento);
        pagamento.setStatus("PAGO");
        pagamento.setDt_pagamento(LocalDateTime.now().toString());
        pagamentoService.criar(pagamento);

        Mesa mesa = salva.getMesa();
        mesa.setStatus("LIVRE");
        mesaRepository.save(mesa);

        return salva;
    }

    /**
     * Substitui a validação em cascata que o ItensComandaRequest não pode fazer
     * (ver comentário lá): garante 400 com mensagem legível em vez de deixar a
     * violação estourar como 500 no momento do persist.
     */
    private void validarItem(ItemVenda item) {
        if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
            throw new IllegalArgumentException("A quantidade do item deve ser maior que zero");
        }
        if (item.getPreco_unitario() == null || item.getPreco_unitario() <= 0) {
            throw new IllegalArgumentException("O preço unitário do item deve ser maior que zero");
        }
    }

    private double somaItens(Comanda comanda) {
        double total = 0.0;
        if (comanda.getVendas() == null) {
            return total;
        }
        for (Venda venda : comanda.getVendas()) {
            if (venda.getItens() == null) {
                continue;
            }
            for (ItemVenda item : venda.getItens()) {
                total += item.getValor_total() != null ? item.getValor_total() : 0.0;
            }
        }
        return total;
    }

    private int proximoNumero(Integer empresaId) {
        return repository.findTopByEmpresaIdOrderByNumeroDesc(empresaId)
                .map(comanda -> comanda.getNumero() + 1)
                .orElse(1);
    }

    private Mesa mesaDaMesmaEmpresa(Mesa mesaRecebida, Integer empresaId) {
        if (mesaRecebida == null || mesaRecebida.getId() == null) {
            throw new IllegalArgumentException("A mesa é obrigatória");
        }
        return mesaRepository.findByIdAndEmpresaId(mesaRecebida.getId(), empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Mesa informada não existe ou não pertence à sua empresa"));
    }
}
