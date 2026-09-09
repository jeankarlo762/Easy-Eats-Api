package com.easy.eats.pedidocompra.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.fornecedor.model.Fornecedor;
import com.easy.eats.fornecedor.repository.FornecedorRepository;
import com.easy.eats.notificacao.service.NotificacaoService;
import com.easy.eats.pedidocompra.enums.StatusPedidoCompra;
import com.easy.eats.pedidocompra.model.PedidoCompra;
import com.easy.eats.pedidocompra.repository.PedidoCompraRepository;
import com.easy.eats.security.SecurityUtils;

@Service
public class PedidoCompraService {

    @Autowired
    private PedidoCompraRepository repository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    public List<PedidoCompra> listarTodos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        return repository.findAllByEmpresaIdOrderByDtCriacaoDesc(SecurityUtils.getEmpresaId());
    }

    public Optional<PedidoCompra> buscarPorId(Integer id) {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findById(id);
        }
        return repository.findByIdAndEmpresaId(id, SecurityUtils.getEmpresaId());
    }

    public PedidoCompra criar(PedidoCompra pedido) {
        Integer empresaId = SecurityUtils.getEmpresaId();

        pedido.setId(null);
        pedido.setEmpresa(empresaRepository.getReferenceById(empresaId));
        pedido.setFornecedor(fornecedorDaMesmaEmpresa(pedido.getFornecedor(), empresaId));
        pedido.setStatus(StatusPedidoCompra.AGUARDANDO);
        pedido.setDtCriacao(LocalDateTime.now());

        return repository.save(pedido);
    }

    /** Avança o pedido para a próxima etapa: Aguardando → Enviado → Recebido. */
    public PedidoCompra avancarStatus(Integer id) {
        PedidoCompra pedido = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido de compra não encontrado"));

        pedido.setStatus(switch (pedido.getStatus()) {
            case AGUARDANDO -> StatusPedidoCompra.ENVIADO;
            case ENVIADO -> StatusPedidoCompra.RECEBIDO;
            case RECEBIDO -> StatusPedidoCompra.RECEBIDO;
        });

        PedidoCompra salvo = repository.save(pedido);

        if (salvo.getStatus() == StatusPedidoCompra.RECEBIDO) {
            notificacaoService.notificarEmpresa(salvo.getEmpresa().getId(), "bi-truck", "azul",
                    "Entrega do fornecedor",
                    salvo.getFornecedor().getNome() + " confirmou a entrega do pedido #" + salvo.getId() + ".");
        }

        return salvo;
    }

    public void deletar(Integer id) {
        if (buscarPorId(id).isEmpty()) {
            return;
        }
        repository.deleteById(id);
    }

    private Fornecedor fornecedorDaMesmaEmpresa(Fornecedor fornecedorRecebido, Integer empresaId) {
        if (fornecedorRecebido == null || fornecedorRecebido.getId() == null) {
            throw new IllegalArgumentException("O fornecedor é obrigatório");
        }
        return fornecedorRepository.findByIdAndEmpresaId(fornecedorRecebido.getId(), empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Fornecedor informado não existe ou não pertence à sua empresa"));
    }
}
