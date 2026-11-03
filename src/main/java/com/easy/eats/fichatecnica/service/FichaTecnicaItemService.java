package com.easy.eats.fichatecnica.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.easy.eats.fichatecnica.dto.FichaTecnicaItemPayload;
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
        return repository.findAllByProdutoIdOrderByIdAsc(produtoId);
    }

    public FichaTecnicaItem salvar(Integer produtoId, Integer ingredienteId, BigDecimal quantidadeUtilizada) {
        Produto produto = produtoService.buscarPorId(produtoId);
        Produto ingrediente = ingredienteDoProduto(produto, ingredienteId);

        if (repository.existsByProdutoIdAndIngredienteId(produtoId, ingredienteId)) {
            throw new IllegalArgumentException("Este ingrediente já está na ficha técnica; edite a quantidade utilizada");
        }

        FichaTecnicaItem item = new FichaTecnicaItem();
        item.setProduto(produto);
        item.setIngrediente(ingrediente);
        item.setQuantidadeUtilizada(quantidadeUtilizada);

        return repository.save(item);
    }

    public FichaTecnicaItem atualizar(Integer produtoId, Integer id, BigDecimal quantidadeUtilizada) {
        FichaTecnicaItem existente = buscarPorIdEProduto(produtoId, id);
        existente.setQuantidadeUtilizada(quantidadeUtilizada);
        return repository.save(existente);
    }

    /**
     * Substitui a composição inteira do produto pela lista recebida (usado ao
     * salvar o cadastro do produto): atualiza a quantidade dos ingredientes que
     * continuam, remove os que saíram e inclui os novos — tudo ou nada.
     */
    @Transactional
    public List<FichaTecnicaItem> substituir(Integer produtoId, List<FichaTecnicaItemPayload> itens) {
        Produto produto = produtoService.buscarPorId(produtoId);

        Set<Integer> ingredientesRecebidos = new HashSet<>();
        for (FichaTecnicaItemPayload payload : itens) {
            if (payload == null || payload.ingredienteId() == null) {
                throw new IllegalArgumentException("O ingrediente é obrigatório em todos os itens da ficha técnica");
            }
            if (payload.quantidadeUtilizada() == null || payload.quantidadeUtilizada().signum() <= 0) {
                throw new IllegalArgumentException("A quantidade utilizada deve ser maior que zero em todos os itens");
            }
            if (!ingredientesRecebidos.add(payload.ingredienteId())) {
                throw new IllegalArgumentException("O mesmo ingrediente foi informado mais de uma vez na ficha técnica");
            }
        }

        Map<Integer, FichaTecnicaItem> existentesPorIngrediente = repository.findAllByProdutoId(produtoId).stream()
                .collect(Collectors.toMap(item -> item.getIngrediente().getId(), Function.identity()));

        List<FichaTecnicaItem> removidos = existentesPorIngrediente.values().stream()
                .filter(item -> !ingredientesRecebidos.contains(item.getIngrediente().getId()))
                .toList();
        repository.deleteAll(removidos);

        for (FichaTecnicaItemPayload payload : itens) {
            FichaTecnicaItem item = existentesPorIngrediente.get(payload.ingredienteId());
            if (item == null) {
                item = new FichaTecnicaItem();
                item.setProduto(produto);
                item.setIngrediente(ingredienteDoProduto(produto, payload.ingredienteId()));
            }
            item.setQuantidadeUtilizada(payload.quantidadeUtilizada());
            repository.save(item);
        }

        return repository.findAllByProdutoIdOrderByIdAsc(produtoId);
    }

    public void deletar(Integer produtoId, Integer id) {
        buscarPorIdEProduto(produtoId, id);
        repository.deleteById(id);
    }

    /** Custo de produção de uma unidade do produto: soma de ingrediente.custo × quantidadeUtilizada. */
    public BigDecimal custoTotal(Integer produtoId) {
        return listarPorProduto(produtoId).stream()
                .map(item -> {
                    BigDecimal custoIngrediente = item.getIngrediente().getCusto() != null
                            ? item.getIngrediente().getCusto()
                            : BigDecimal.ZERO;
                    return custoIngrediente.multiply(item.getQuantidadeUtilizada());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Produto ingredienteDoProduto(Produto produto, Integer ingredienteId) {
        if (produto.getId().equals(ingredienteId)) {
            throw new IllegalArgumentException("Um produto não pode ser ingrediente da própria ficha técnica");
        }
        Produto ingrediente = produtoService.buscarPorId(ingredienteId);
        if (ingrediente.getNatureza() != NaturezaProduto.INSUMO) {
            throw new IllegalArgumentException("O produto informado como ingrediente não tem natureza INSUMO");
        }
        if (Boolean.FALSE.equals(ingrediente.getFlAtivo())) {
            throw new IllegalArgumentException("O ingrediente \"" + ingrediente.getNome() + "\" foi excluído e não pode ser usado");
        }
        return ingrediente;
    }

    private FichaTecnicaItem buscarPorIdEProduto(Integer produtoId, Integer id) {
        produtoService.buscarPorId(produtoId);
        return repository.findByIdAndProdutoId(id, produtoId)
                .orElseThrow(() -> new RuntimeException("Item de ficha técnica não encontrado"));
    }
}
