package com.easy.eats.estoque.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.easy.eats.empresa.repository.EmpresaRepository;
import com.easy.eats.estoque.enums.TipoMovimentacaoEstoque;
import com.easy.eats.estoque.model.Estoque;
import com.easy.eats.estoque.model.ItemMaisConsumido;
import com.easy.eats.estoque.model.MovimentacaoEstoque;
import com.easy.eats.estoque.repository.EstoqueRepository;
import com.easy.eats.estoque.repository.MovimentacaoEstoqueRepository;
import com.easy.eats.notificacao.service.NotificacaoService;
import com.easy.eats.produto.model.Produto;
import com.easy.eats.produto.repository.ProdutoRepository;
import com.easy.eats.security.SecurityUtils;

@Service
public class EstoqueService {

    @Autowired
    private EstoqueRepository repository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    public List<Estoque> listarTodos() {
        if (SecurityUtils.isSuperadmin()) {
            return repository.findAll();
        }
        return repository.findAllByProduto_EmpresaId(SecurityUtils.getEmpresaId());
    }

    public List<Estoque> listarAbaixoDoMinimo() {
        return repository.findAbaixoDoMinimo(SecurityUtils.getEmpresaId());
    }

    public Estoque buscarPorProduto(Integer produtoId) {
        Estoque estoque = SecurityUtils.isSuperadmin()
                ? repository.findByProdutoId(produtoId).orElse(null)
                : repository.findByProdutoIdAndProduto_EmpresaId(produtoId, SecurityUtils.getEmpresaId()).orElse(null);

        if (estoque == null) {
            throw new RuntimeException("Estoque não encontrado para este produto");
        }
        return estoque;
    }

    public Estoque criar(Integer produtoId, Integer quantidadeAtual, Integer estoqueMinimo) {
        Integer empresaId = SecurityUtils.getEmpresaId();
        Produto produto = produtoDaMesmaEmpresa(produtoId, empresaId);

        if (repository.findByProdutoId(produtoId).isPresent()) {
            throw new IllegalArgumentException("Este produto já tem um registro de estoque; use atualizar");
        }

        Estoque estoque = new Estoque();
        estoque.setProduto(produto);
        estoque.setQuantidadeAtual(quantidadeAtual);
        estoque.setEstoqueMinimo(estoqueMinimo);
        estoque.setDtUltimaMovimentacao(LocalDateTime.now());

        return repository.save(estoque);
    }

    public Estoque atualizar(Integer produtoId, Integer quantidadeAtual, Integer estoqueMinimo) {
        Estoque existente = buscarPorProduto(produtoId);

        existente.setQuantidadeAtual(quantidadeAtual);
        existente.setEstoqueMinimo(estoqueMinimo);
        existente.setDtUltimaMovimentacao(LocalDateTime.now());

        return repository.save(existente);
    }

    public void remover(Integer produtoId) {
        Estoque existente = buscarPorProduto(produtoId);
        repository.delete(existente);
    }

    /**
     * Registra a movimentação e ajusta Estoque.quantidadeAtual no mesmo
     * lugar — a quantidade nunca é editada diretamente por fora daqui, para
     * o saldo sempre bater com a soma do histórico.
     */
    @Transactional
    public MovimentacaoEstoque registrarMovimentacao(Integer produtoId, TipoMovimentacaoEstoque tipo,
            Integer quantidade, String observacao) {
        Integer empresaId = SecurityUtils.getEmpresaId();
        Estoque estoque = buscarPorProduto(produtoId);

        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero");
        }

        int quantidadeAtual = estoque.getQuantidadeAtual() != null ? estoque.getQuantidadeAtual() : 0;
        int novaQuantidade = tipo == TipoMovimentacaoEstoque.ENTRADA
                ? quantidadeAtual + quantidade
                : quantidadeAtual - quantidade;

        if (novaQuantidade < 0) {
            throw new IllegalArgumentException("Saída maior que a quantidade disponível em estoque");
        }

        estoque.setQuantidadeAtual(novaQuantidade);
        estoque.setDtUltimaMovimentacao(LocalDateTime.now());
        repository.save(estoque);

        if (estoque.getEstoqueMinimo() != null && novaQuantidade < estoque.getEstoqueMinimo()) {
            notificacaoService.notificarEmpresa(empresaId, "bi-exclamation-triangle", "vermelho", "Estoque crítico",
                    "\"" + estoque.getProduto().getNome() + "\" está abaixo do mínimo recomendado.");
        }

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setProduto(estoque.getProduto());
        movimentacao.setTipo(tipo);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setObservacao(observacao);
        movimentacao.setDtMovimentacao(LocalDateTime.now());
        movimentacao.setEmpresa(empresaRepository.getReferenceById(empresaId));

        return movimentacaoRepository.save(movimentacao);
    }

    public List<MovimentacaoEstoque> listarMovimentacoesRecentes() {
        Integer empresaId = SecurityUtils.getEmpresaId();
        return movimentacaoRepository.findAllByEmpresaIdOrderByDtMovimentacaoDesc(empresaId, PageRequest.of(0, 20));
    }

    /** Top-5 produtos com mais saídas de estoque registradas — usado no relatório. */
    public List<ItemMaisConsumido> listarMaisConsumidos() {
        Integer empresaId = SecurityUtils.getEmpresaId();
        return movimentacaoRepository.itensMaisConsumidos(empresaId, PageRequest.of(0, 5));
    }

    private Produto produtoDaMesmaEmpresa(Integer produtoId, Integer empresaId) {
        if (produtoId == null) {
            throw new IllegalArgumentException("O produto é obrigatório");
        }
        return produtoRepository.findByIdAndEmpresaId(produtoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Produto informado não existe ou não pertence à sua empresa"));
    }
}
