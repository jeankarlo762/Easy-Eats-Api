package com.easy.eats.fichatecnica.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easy.eats.fichatecnica.model.FichaTecnicaItem;
import com.easy.eats.fichatecnica.repository.FichaTecnicaItemRepository;
import com.easy.eats.produto.enums.NaturezaProduto;
import com.easy.eats.produto.model.Produto;
import com.easy.eats.produto.service.ProdutoService;

@Service
public class FichaTecnicaItemService {

    private final FichaTecnicaItemRepository repository;

    @Autowired
    private ProdutoService produtoService;

    public FichaTecnicaItemService(FichaTecnicaItemRepository repository) {
        this.repository = repository;
    }

    public List<FichaTecnicaItem> listarPorProduto(Integer produtoId) {
        produtoService.buscarPorId(produtoId);
        return repository.findAllByProdutoId(produtoId);
    }

    public FichaTecnicaItem salvar(Integer produtoId, Integer insumoId, BigDecimal quantidade) {
        Produto produto = produtoService.buscarPorId(produtoId);
        Produto insumo = insumoDoProduto(insumoId);

        FichaTecnicaItem item = new FichaTecnicaItem();
        item.setProduto(produto);
        item.setInsumo(insumo);
        item.setQuantidade(quantidade);

        return repository.save(item);
    }

    public FichaTecnicaItem atualizar(Integer produtoId, Integer id, BigDecimal quantidade) {
        FichaTecnicaItem existente = buscarPorIdEProduto(produtoId, id);
        existente.setQuantidade(quantidade);
        return repository.save(existente);
    }

    public void deletar(Integer produtoId, Integer id) {
        buscarPorIdEProduto(produtoId, id);
        repository.deleteById(id);
    }

    /** Custo de produção de uma unidade do produto: soma de insumo.custo × quantidade. */
    public BigDecimal custoTotal(Integer produtoId) {
        return listarPorProduto(produtoId).stream()
                .map(item -> {
                    BigDecimal custoInsumo = item.getInsumo().getCusto() != null ? item.getInsumo().getCusto() : BigDecimal.ZERO;
                    return custoInsumo.multiply(item.getQuantidade());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Produto insumoDoProduto(Integer insumoId) {
        Produto insumo = produtoService.buscarPorId(insumoId);
        if (insumo.getNatureza() != NaturezaProduto.INSUMO) {
            throw new IllegalArgumentException("O produto informado como insumo não tem natureza INSUMO");
        }
        return insumo;
    }

    private FichaTecnicaItem buscarPorIdEProduto(Integer produtoId, Integer id) {
        produtoService.buscarPorId(produtoId);
        return repository.findByIdAndProdutoId(id, produtoId)
                .orElseThrow(() -> new RuntimeException("Item de ficha técnica não encontrado"));
    }
}
