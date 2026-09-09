package com.easy.eats.pagamento.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.caixa.enums.StatusCaixa;
import com.easy.eats.caixa.model.Caixa;
import com.easy.eats.caixa.repository.CaixaRepository;
import com.easy.eats.comanda.model.Comanda;
import com.easy.eats.comanda.repository.ComandaRepository;
import com.easy.eats.notificacao.service.NotificacaoService;
import com.easy.eats.pagamento.model.Pagamento;
import com.easy.eats.pagamento.repository.PagamentoRepository;
import com.easy.eats.security.SecurityUtils;
import com.easy.eats.venda.model.Venda;
import com.easy.eats.venda.repository.VendaRepository;

@Service
public class PagamentoService {

    @Autowired
    PagamentoRepository repository;

    @Autowired
    VendaRepository vendaRepository;

    @Autowired
    ComandaRepository comandaRepository;

    @Autowired
    CaixaRepository caixaRepository;

    @Autowired
    NotificacaoService notificacaoService;

    public Pagamento criar(Pagamento pagamento) {
        pagamento.setId(null);

        boolean temVenda = pagamento.getVenda() != null && pagamento.getVenda().getId() != null;
        boolean temComanda = pagamento.getComanda() != null && pagamento.getComanda().getId() != null;

        if (temVenda == temComanda) {
            throw new IllegalArgumentException("Informe a venda ou a comanda do pagamento, mas não as duas");
        }

        if (temVenda) {
            pagamento.setVenda(vendaDaMesmaEmpresa(pagamento.getVenda()));
            pagamento.setComanda(null);
        } else {
            pagamento.setComanda(comandaDaMesmaEmpresa(pagamento.getComanda()));
            pagamento.setVenda(null);
        }

        pagamento.setCaixa(caixaAbertoDaEmpresa());

        Pagamento salvo = repository.save(pagamento);

        notificacaoService.notificarEmpresa(SecurityUtils.getEmpresaId(), "bi-cash-coin", "verde",
                "Pagamento confirmado", "Recebido via " + salvo.getMetodo() + " — R$ " + salvo.getValor() + ".");

        return salvo;
    }

    /**
     * Vincula automaticamente à sessão de caixa aberta da empresa, se
     * houver — pagamentos originados do link público (sem usuário
     * autenticado) não passam por aqui, então ficam sem caixa mesmo.
     */
    private Caixa caixaAbertoDaEmpresa() {
        try {
            return caixaRepository.findByEmpresaIdAndStatus(SecurityUtils.getEmpresaId(), StatusCaixa.ABERTO).orElse(null);
        } catch (IllegalStateException semUsuarioAutenticado) {
            return null;
        }
    }

    public Pagamento salvar(Pagamento pagamento) {
        return repository.save(pagamento);
    }

    public List<Pagamento> listarTodos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        return repository.findAllByVenda_Empresa_Id(SecurityUtils.getEmpresaId());
    }

    public Optional<Pagamento> buscarPorId(Integer id) {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findById(id);
        }
        return repository.findByIdAndVenda_Empresa_Id(id, SecurityUtils.getEmpresaId());
    }

    public void deletar(Integer id) {
        if (buscarPorId(id).isEmpty()) {
            return;
        }
        repository.deleteById(id);
    }

    private Venda vendaDaMesmaEmpresa(Venda vendaRecebida) {
        if (vendaRecebida == null || vendaRecebida.getId() == null) {
            throw new IllegalArgumentException("A venda é obrigatória");
        }
        return vendaRepository.findByIdAndEmpresaId(vendaRecebida.getId(), SecurityUtils.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Venda informada não existe ou não pertence à sua empresa"));
    }

    private Comanda comandaDaMesmaEmpresa(Comanda comandaRecebida) {
        if (comandaRecebida == null || comandaRecebida.getId() == null) {
            throw new IllegalArgumentException("A comanda é obrigatória");
        }
        return comandaRepository.findByIdAndEmpresaId(comandaRecebida.getId(), SecurityUtils.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Comanda informada não existe ou não pertence à sua empresa"));
    }
}
